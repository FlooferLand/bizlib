package com.flooferland.bizlib

import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path

interface IShowFormat {
    /** @throws java.io.IOException */
    fun readFile(path: Path): RawShowData = read(Files.newInputStream(path))

    /** @throws java.io.IOException */
    fun read(stream: InputStream): RawShowData

    /** @throws java.io.IOException */
    fun write(stream: OutputStream, data: RawShowData)
}