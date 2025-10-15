package com.flooferland.bizlib.formats

import com.flooferland.bizlib.IShowFormat
import com.flooferland.bizlib.RawShowData
import java.io.InputStream
import java.io.OutputStream

class RshowFormat : IShowFormat {
    override fun read(stream: InputStream): RawShowData {
        return RawShowData(
            signal = ByteArray(512),
            audio = ByteArray(512),
            video = ByteArray(512),
        )
    }

    override fun write(stream: OutputStream, data: RawShowData) {
        error("Write not implemented")
    }
}