package com.flooferland.bizlib.formats

import com.flooferland.bizlib.BizlibNative
import com.flooferland.bizlib.IShowFormat
import com.flooferland.bizlib.RawShowData
import java.io.InputStream
import java.io.OutputStream
import com.sun.jna.*

class RshowFormat : IShowFormat {
    override fun read(stream: InputStream): RawShowData {
        val streamBytes = stream.readAllBytes()
        if (streamBytes.isEmpty()) error("${RshowFormat::class.simpleName}: Input stream is empty")

        val memory = Memory(streamBytes.size.toLong())
        memory.write(0, streamBytes, 0, streamBytes.size)

        var audio: ByteArray
        var signal: IntArray
        var video = ByteArray(0)

        val nativeData = BizlibNative.instance?.ReadRshw(memory, streamBytes.size)
        try {
            if (nativeData == null) error("Received null")
            if (nativeData.statusCode != 0) error("statusCode:${nativeData.statusCode}")

            if (nativeData.audioPtr != null && nativeData.audioLen > 0) {
                audio = nativeData.audioPtr!!.getByteArray(0, nativeData.audioLen)
            } else error("Missing audio")

            if (nativeData.signalPtr != null && nativeData.signalLen > 0) {
                signal = nativeData.signalPtr!!.getIntArray(0, nativeData.signalLen)
            } else error("Missing signal")

            if (nativeData.videoPtr != null && nativeData.videoLen > 0) {
                video = nativeData.videoPtr!!.getByteArray(0, nativeData.videoLen)
            }
        } catch (e: Exception) {
            throw e
        } finally {
            runCatching { nativeData?.let { BizlibNative.instance?.FreeRshw(it) } }
                .onFailure { System.err.println("Failed to clean up rshw") }
        }

        return RawShowData(signal, audio, video)
    }

    override fun write(stream: OutputStream, data: RawShowData) {
        // TODO: Add rshw writing
        error("Write not implemented")
    }
}