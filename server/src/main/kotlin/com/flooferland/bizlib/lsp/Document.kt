package com.flooferland.bizlib.lsp

import com.flooferland.bizlib.bits.BitUtils
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.Trees

class Document(val tree: ParseTree, val text: String) {
    private val drawerBit = Regex("""(\d+)(td|bd)""")

    fun bitId(text: String): UShort? = text.toUShortOrNull() ?: drawerBit.matchEntire(text)?.let { match ->
        match.groupValues[1].toInt() + if (match.groupValues[2] == "bd") BitUtils.NEXT_DRAWER.toInt() else 0
    }?.toUShort()

    val fixtures: Map<String, String> = Trees.findAllRuleNodes(tree, BitsmapParser.RULE_setStmt)
        .filterIsInstance<BitsmapParser.SetStmtContext>()
        .mapNotNull { s ->
            val map = s.map()?.text ?: return@mapNotNull null
            s.fixture()?.ID()?.text?.let { map to it }
        }
        .toMap()

    fun findFixtures(ctx: ParserRuleContext, includeOld: Boolean): List<String> =
        ctx.closest<BitsmapParser.SetStmtContext>()?.map()?.text?.let { map ->
            val result = mutableSetOf<String>()
            if (includeOld) result += BitUtils.readBitmap("${map}_old")?.keys?.toList().orEmpty()
            result += BitUtils.readBitmap(map)?.keys?.toList().orEmpty()
            result.toList()
        }.orEmpty()

    fun movements(map: String, includeOld: Boolean): Map<String, UShort> {
        val fixture = fixtures[map] ?: return emptyMap()
        val result = mutableMapOf<String, UShort>()
        if (includeOld) result += BitUtils.readBitmap("${map}_old")?.get(fixture).orEmpty()
        result += BitUtils.readBitmap(map)?.get(fixture).orEmpty()
        return result
    }
}