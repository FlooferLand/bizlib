package com.flooferland.bizlib.formats

import com.flooferland.bizlib.BizlibNative
import com.flooferland.bizlib.IShowFormat
import com.flooferland.bizlib.RawShowData
import java.io.InputStream
import java.io.OutputStream
import com.sun.jna.*
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class RshowFormat : IShowFormat {
    override fun readFile(path: Path): RawShowData {
        val nativeData = BizlibNative.instance?.ReadRshwFile(path.absolutePathString())
        return try {
            processRead(nativeData)
        } finally {
            cleanup(nativeData)
        }
    }

    override fun read(stream: InputStream): RawShowData {
        val streamBytes = stream.readAllBytes()
        if (streamBytes.isEmpty()) error("${RshowFormat::class.simpleName}: Input stream is empty")

        val memory = Memory(streamBytes.size.toLong())
        memory.write(0, streamBytes, 0, streamBytes.size)

        val nativeData = BizlibNative.instance?.ReadRshw(memory, streamBytes.size)
        return try {
            processRead(nativeData)
        } finally {
            cleanup(nativeData)
        }
    }

    private fun processRead(data: BizlibNative.RshwData.ByValue?): RawShowData {
        if (data == null) error("Received null")
        if (data.hasError) error("Unknown error (${data::hasError.name}=true)")

        val audio = if (data.audioPtr != null && data.audioLen > 0) {
            data.audioPtr!!.getByteArray(0, data.audioLen)
        } else error("Missing audio")

        val signal = if (data.signalPtr != null && data.signalLen > 0) {
            data.signalPtr!!.getIntArray(0, data.signalLen)
        } else error("Missing signal")

        val video = if (data.videoPtr != null && data.videoLen > 0) {
            data.videoPtr!!.getByteArray(0, data.videoLen)
        } else ByteArray(0)

        return RawShowData(audio = audio, signal = signal, video = video)
    }

    private fun cleanup(data: BizlibNative.RshwData.ByValue?) {
        runCatching { data?.let { BizlibNative.instance?.FreeRshw(it) } }
            .onFailure { System.err.println("Failed to clean up rshw") }
    }

    override fun write(stream: OutputStream, data: RawShowData) {
        // TODO: Add rshw writing
        error("Write not implemented")
    }
}