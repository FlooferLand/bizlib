package com.flooferland.bizlib.lsp

import org.antlr.v4.runtime.atn.ATNState
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.atn.ATN
import org.antlr.v4.runtime.atn.RuleStopState
import org.antlr.v4.runtime.atn.RuleTransition
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.TerminalNode

val ParserRuleContext.lastRule: ParserRuleContext?
    get() {
        val tree = children?.lastOrNull { it !is ErrorNode && (it as? TerminalNode)?.symbol?.type != Token.EOF }
        return (tree as? ParserRuleContext)?.takeIf { it.exception == null }
    }

inline fun <reified T : ParserRuleContext> ParserRuleContext.closest(): T? =
    parents().filterIsInstance<T>().firstOrNull()

fun ParserRuleContext.parents() =
    generateSequence(this) { it.parent as? ParserRuleContext }

fun ParserRuleContext.rulesAfter(atn: ATN): Set<Int> {
    if (invokingState < 0) return emptySet()
    val found = mutableSetOf<Int>()
    val seen = mutableSetOf<Int>()
    fun walk(state: ATNState) {
        if (state is RuleStopState || !seen.add(state.stateNumber)) return
        for (t in state.transitions) {
            if (t is RuleTransition) found += t.ruleIndex
            if (t.isEpsilon) walk(t.target)
        }
    }
    walk((atn.states[invokingState].transition(0) as RuleTransition).followState)
    return found
}
