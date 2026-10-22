package com.flooferland.bizlib.lsp

import com.flooferland.bizlib.bits.BitsMap.LogMessage
import com.flooferland.bizlib.bits.generated.BitsmapParser
import org.antlr.v4.runtime.atn.ATNState
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.atn.ATN
import org.antlr.v4.runtime.atn.RuleStopState
import org.antlr.v4.runtime.atn.RuleTransition
import org.antlr.v4.runtime.misc.IntervalSet
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range

val ParserRuleContext.lastRule: ParserRuleContext?
    get() {
        val tree = children?.lastOrNull { it !is ErrorNode && (it as? TerminalNode)?.symbol?.type != Token.EOF }
        return (tree as? ParserRuleContext)?.takeIf { it.exception == null }
    }

inline fun <reified T : ParserRuleContext> ParserRuleContext.closest(): T? =
    parents().filterIsInstance<T>().firstOrNull()

fun ParserRuleContext.parents() =
    generateSequence(this) { it.getParent() }

fun ParserRuleContext.followers(atn: ATN): Followers = atn.reachable(listOfNotNull(
    if (invokingState >= 0) (atn.states[invokingState]?.transition(0) as RuleTransition).followState else null,
    if (start?.type == BitsmapParser.CARET) atn.ruleToStartState?.get(ruleIndex) else null,
))

fun ParserRuleContext.range(): Range {
    val start = this.start ?: return Range(Position(0, 0), Position(0, 0))
    val stop = stop ?: start
    return Range(
        Position(start.line - 1, start.charPositionInLine),
        Position(stop.line - 1, stop.charPositionInLine + (stop.text?.length ?: 0))
    )
}

val ParserRuleContext.isComplete: Boolean
    get() {
        val last = children?.lastOrNull { (it as? TerminalNode)?.symbol?.type != Token.EOF }
        return exception == null && last !is ErrorNode && ((last as? ParserRuleContext)?.isComplete ?: true)
    }

operator fun Range.contains(pos: Position) =
    pos.line == start.line
    && pos.character >= start.character && pos.character < end.character

private fun ATN.reachable(from: List<ATNState>): Followers {
    val rules = mutableSetOf<Int>()
    val tokens = IntervalSet()
    val seen = mutableSetOf<Int>()
    fun walk(state: ATNState) {
        if (state is RuleStopState || !seen.add(state.stateNumber)) return
        for (t in state.transitions) {
            if (t is RuleTransition) rules += t.ruleIndex
            if (t.isEpsilon) walk(t.target) else t.label()?.let { tokens.addAll(it) }
        }
    }
    from.forEach { walk(it) }
    return Followers(rules, tokens)
}

fun ParserRuleContext.getFieldName(): String? {
    val suffix = "Field"
    val upward = generateSequence(this) { it.getParent() }
        .mapNotNull { BitsmapParser.ruleNames.getOrNull(it.ruleIndex) }
        .firstOrNull { it.endsWith(suffix) }
    if (upward != null) return upward.removeSuffix(suffix).lowercase()

    val fields = mutableListOf<ParserRuleContext>()
    fun walk(node: ParseTree?) {
        if (node is ParserRuleContext) {
            val name = BitsmapParser.ruleNames.getOrNull(node.ruleIndex)
            if (name != null && name.endsWith(suffix)) fields.add(node)
            for (i in 0 until node.childCount) walk(node.getChild(i))
        }
    }
    walk(this)

    return fields.lastOrNull()?.let {
        BitsmapParser.ruleNames[it.ruleIndex].removeSuffix(suffix).lowercase()
    }
}
