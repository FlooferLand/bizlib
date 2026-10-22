package com.flooferland.bizlib

import org.antlr.v4.runtime.ParserRuleContext
import kotlin.math.min

data class ParserPosition(val start: Point, val end: Point, val startIndex: Int, val endIndex: Int) {
    data class Point(val line: Int, val column: Int)
    fun text(raw: String): String {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex || endIndex >= raw.length) return ""
        return raw.substring(startIndex, min(endIndex + 1, raw.length))
    }
}

val ParserRuleContext.position: ParserPosition?
    get() {
        val start = this.start ?: return null
        val stop = stop ?: start
        return ParserPosition(
            start = ParserPosition.Point(start.line, start.charPositionInLine),
            end = ParserPosition.Point(stop.line, stop.charPositionInLine + (stop.text?.length ?: 0)),
            startIndex = start.startIndex,
            endIndex = stop.stopIndex
        )
    }
