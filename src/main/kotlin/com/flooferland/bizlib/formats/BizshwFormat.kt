package com.flooferland.bizlib.formats

import com.flooferland.bizlib.IShowFormat
import com.flooferland.bizlib.RawShowData
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

// TODO: Port this to C

class BizshwFormat : IShowFormat {
    override fun read(stream: InputStream): RawShowData {
        DataInputStream(stream).use { s ->
            val header = ByteArray(s.readByte().toInt()).also { s.readFully(it) }.let { String(it, Charsets.US_ASCII) }
            s.read()

            println("Read '$header'")
            val audio = ByteArray(s.readInt()).also { s.readFully(it) }
            val signal = IntArray(s.readInt()) { s.readByte().toInt() and 0xFF }
            val video = ByteArray(s.readInt()).also { s.readFully(it) }
            return RawShowData(
                signal = signal,
                audio = audio,
                video = video
            )
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun write(path: Path, data: RawShowData) {
        DataOutputStream(Files.newOutputStream(path)).use { s ->
            val header = "bizshw, 1.0"
            s.writeByte(header.length)
            s.write(header.toByteArray(Charsets.US_ASCII))
            s.write(0x00)

            s.writeInt(data.audio.size)
            s.write(data.audio)

            s.writeInt(data.signal.size)
            s.write(data.signal.map { it.toUByte() }.toUByteArray().asByteArray())

            s.writeInt(data.video.size)
            s.write(data.video)
        }
    }
}