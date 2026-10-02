package com.flooferland.showbiz

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import javax.swing.Icon
import org.jetbrains.annotations.NonNls
import kotlin.jvm.java

object BitsmapLanguage : Language("bitsmap") {
    private fun readResolve(): Any = this
    val icon: Icon? = IconLoader.findIcon("/bitsmapIcon.svg", BitsmapLanguage::class.java)
}

class BitsmapLanguageType : LanguageFileType(BitsmapLanguage) {
    override fun getName(): @NonNls String = "Bitsmap"
    override fun getDescription(): @NlsContexts.Label String = "Bitsmap language file"
    override fun getDefaultExtension(): @NlsSafe String = "bits"
    override fun getIcon(): Icon? = BitsmapLanguage.icon
}