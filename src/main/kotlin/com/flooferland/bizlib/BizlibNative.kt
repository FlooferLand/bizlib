package com.flooferland.bizlib

import com.sun.jna.*

@Suppress("FunctionName")
interface BizlibNative : Library {

    open class RshwData : Structure() {
        @JvmField var audioPtr: Pointer? = null; @JvmField var audioLen: Int = 0
        @JvmField var signalPtr: Pointer? = null; @JvmField var signalLen: Int = 0
        @JvmField var videoPtr: Pointer? = null; @JvmField var videoLen: Int = 0
        @JvmField var errorPtr: Pointer? = null

        override fun getFieldOrder() = listOf(
            RshwData::audioPtr.name, RshwData::audioLen.name,
            RshwData::signalPtr.name, RshwData::signalLen.name,
            RshwData::videoPtr.name, RshwData::videoLen.name,
            RshwData::errorPtr.name
        )

        fun getError(): String? = if (Pointer.NULL == errorPtr) return null
            else errorPtr?.getString(0)?.ifEmpty { null }

        class ByValue(data: RawShowData? = null) : RshwData(), Structure.ByValue {
            init {
                data?.let {
                    if (it.hasAudio) {
                        audioPtr = Memory(it.audio.size.toLong()).apply { write(0, it.audio, 0, it.audio.size) }
                        audioLen = it.audio.size
                    }
                    if (it.hasSignal) {
                        signalPtr = Memory(it.signal.size * 4L).apply { write(0, it.signal, 0, it.signal.size) }
                        signalLen = it.signal.size
                    }
                    if (it.hasVideo) {
                        videoPtr = Memory(it.video.size.toLong()).apply { write(0, it.video, 0, it.video.size) }
                        videoLen = it.video.size
                    }
                }
            }
        }
    }

    fun ReadRshw(handle: Pointer, size: Int): RshwData.ByValue
    fun ReadRshwFile(path: String): RshwData.ByValue
    fun FreeRshw(data: RshwData.ByValue)

    companion object {
        var instance: BizlibNative? = null

        init {
            try {
                instance = Native.load("BizlibNative", BizlibNative::class.java)
            } catch (e: UnsatisfiedLinkError) {
                System.err.println("Bizlib: Failed to load native library. Defaulting to Java-driven rshw parser:\n$e")
            }
        }
    }
}