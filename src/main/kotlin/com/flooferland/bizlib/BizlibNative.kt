package com.flooferland.bizlib

import com.sun.jna.*

@Suppress("FunctionName")
interface BizlibNative : Library {

    open class RshwData : Structure() {
        @JvmField var audioPtr: Pointer? = null; @JvmField var audioLen: Int = 0
        @JvmField var signalPtr: Pointer? = null; @JvmField var signalLen: Int = 0
        @JvmField var videoPtr: Pointer? = null; @JvmField var videoLen: Int = 0
        @JvmField var hasError: Boolean = true

        override fun getFieldOrder() = listOf(
            RshwData::audioPtr.name, RshwData::audioLen.name,
            RshwData::signalPtr.name, RshwData::signalLen.name,
            RshwData::videoPtr.name, RshwData::videoLen.name,
            RshwData::hasError.name
        )

        class ByValue : RshwData(), Structure.ByValue
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