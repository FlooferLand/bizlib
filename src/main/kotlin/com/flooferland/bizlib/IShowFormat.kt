package com.flooferland.bizlib

import java.io.InputStream
import java.io.OutputStream

interface IShowFormat {
    /** @throws java.io.IOException */
    fun read(stream: InputStream): RawShowData

    /** @throws java.io.IOException */
    fun write(stream: OutputStream, data: RawShowData)
}