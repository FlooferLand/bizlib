package com.flooferland.bizlib

data class RawShowData(
    val signal: IntArray = intArrayOf(),
    val audio: ByteArray = byteArrayOf(),
    val video: ByteArray = byteArrayOf(),
    val format: String?
) {
    constructor(signal: IntArray, audio: ByteArray, video: ByteArray) : this(signal, audio, video, null)

    val hasSignal: Boolean
        get() = this.signal.isNotEmpty()
    val hasAudio: Boolean
        get() = this.audio.isNotEmpty()
    val hasVideo: Boolean
        get() = this.video.isNotEmpty()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RawShowData) return false
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