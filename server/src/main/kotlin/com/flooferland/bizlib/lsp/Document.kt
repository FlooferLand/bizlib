package com.flooferland.bizlib.lsp

import com.flooferland.bizlib.bits.BitUtils
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.Trees

class Document(val tree: ParseTree, val text: String) {
    private val drawerBit = Regex("""(\d+)(td|bd)""")

    fun bitId(text: String): Int? = text.toIntOrNull() ?: drawerBit.matchEntire(text)?.let { match ->
        match.groupValues[1].toInt() + if (match.groupValues[2] == "bd") BitUtils.NEXT_DRAWER.toInt() else 0
    }

    val fixtures: Map<String, String> = Trees.findAllRuleNodes(tree, BitsmapParser.RULE_setStmt)
        .filterIsInstance<BitsmapParser.SetStmtContext>()
        .mapNotNull { s ->
            val map = s.map()?.text ?: return@mapNotNull null
            s.fixture()?.ID()?.text?.let { map to it }
        }
        .toMap()

    fun movements(map: String): Map<String, UShort> {
        val fixture = fixtures[map] ?: return emptyMap()
        return BitUtils.readBitmap("${map}_old")?.get(fixture).orEmpty() +
                BitUtils.readBitmap(map)?.get(fixture).orEmpty()
    }
}