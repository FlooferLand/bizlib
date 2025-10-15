package com.flooferland.bizlib.formats

import com.flooferland.bizlib.IShowFormat
import com.flooferland.bizlib.RawShowData
import java.io.InputStream
import java.io.OutputStream

// TODO: Port the Java RshowFormat to Kotlin
class RshowFormatKt : IShowFormat {
    override fun read(stream: InputStream): RawShowData {
        return RawShowData(
            signal = IntArray(512),
            audio = ByteArray(512),
            video = ByteArray(512),
        )
    }

    override fun write(stream: OutputStream, data: RawShowData) {
        error("Write not implemented")
    }
}