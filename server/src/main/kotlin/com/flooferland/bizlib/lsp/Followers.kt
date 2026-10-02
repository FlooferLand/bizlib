package com.flooferland.bizlib.lsp

import org.antlr.v4.runtime.misc.IntervalSet

class Followers(val rules: Set<Int>, val tokens: IntervalSet) {
    val isEmpty get() = rules.isEmpty() && tokens.isNil
}
