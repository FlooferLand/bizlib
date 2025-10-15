package com.flooferland.bizlib

data class RawShowData(
    val signal: ByteArray,
    val audio: ByteArray,
    val video: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawShowData

        if (!signal.contentEquals(other.signal)) return false
        if (!audio.contentEquals(other.audio)) return false
        if (!video.contentEquals(other.video)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = signal.contentHashCode()
        result = 31 * result + audio.contentHashCode()
        result = 31 * result + video.contentHashCode()
        return result
    }
}