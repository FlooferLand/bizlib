package com.flooferland.bizlib.lsp

import org.antlr.v4.runtime.atn.ATNState
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.atn.ATN
import org.antlr.v4.runtime.atn.RuleStopState
import org.antlr.v4.runtime.atn.RuleTransition
import org.antlr.v4.runtime.misc.IntervalSet
import org.antlr.v4.runtime.tree.ErrorNode
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
    generateSequence(this) { it.parent as? ParserRuleContext }

fun ParserRuleContext.followers(atn: ATN): Followers = atn.reachable(listOfNotNull(
    if (invokingState >= 0) (atn.states[invokingState].transition(0) as RuleTransition).followState else null,
    if (start?.type == BitsmapParser.CARET) atn.ruleToStartState[ruleIndex] else null,
))

fun ParserRuleContext.range() = Range(
    Position(start.line - 1, start.charPositionInLine),
    Position(stop.line - 1, stop.charPositionInLine + stop.text.length),
)

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
