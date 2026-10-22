package com.flooferland.bizlib.bits

import com.flooferland.bizlib.bits.generated.BitsmapParser
import org.antlr.v4.runtime.ParserRuleContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

typealias ParamFail = (ParserRuleContext, String) -> Unit

abstract class ParamSet(val field: String) {
    private val validators = mutableMapOf<String, (BitsmapParser.ParamValueContext) -> Boolean>()
    private val requiredKeys = mutableSetOf<String>()
    private val atLeastOne = mutableListOf<List<String>>()
    private val given = mutableMapOf<String, BitsmapParser.ParamContext>()

    data class Param<T>(val field: String, val name: String, val value: T)

    abstract class ParamType<T> {
        abstract fun parse(ctx: BitsmapParser.ParamValueContext): T?

        object Num : ParamType<Double>() {
            override fun parse(ctx: BitsmapParser.ParamValueContext) = ctx.num()?.text?.toDoubleOrNull()
        }
        object Bool : ParamType<BooleanType>() {
            override fun parse(ctx: BitsmapParser.ParamValueContext) = ctx.BOOLEAN()?.text?.let { BooleanType.from(it) }
        }
        object Str : ParamType<String>() {
            override fun parse(ctx: BitsmapParser.ParamValueContext) = ctx.STRING()?.text?.removeSurrounding("\"")
        }
    }

    inner class ParamDelegate<T>(val key: String, val type: ParamType<T>, val default: T? = null) : ReadOnlyProperty<ParamSet, Param<T>?> {
        fun optional(): ParamDelegate<T> {
            requiredKeys -= key
            return ParamDelegate(key, type, default)
        }
        override fun getValue(thisRef: ParamSet, property: KProperty<*>): Param<T>? {
            return given[key]?.paramValue()?.let { type.parse(it) }?.let { Param(field, key, it) }
        }
    }

    protected fun <T> param(key: String, type: ParamType<T>, d: T?): ParamDelegate<T> {
        validators[key] = { type.parse(it) != null }
        requiredKeys += key
        fieldParamList.getOrPut(field) { mutableSetOf() }.add(key)
        return ParamDelegate(key, type, d)
    }

    // IDE inlays take up half my screen so D it is
    protected fun num(key: String, d: Double? = null) =
        param(key, ParamType.Num, d)
    protected fun bool(key: String, d: Boolean? = null) =
        param(key, ParamType.Bool, d?.let { if (it) BooleanType.Yes else BooleanType.No })
    protected fun str(key: String, d: String? = null) =
        param(key, ParamType.Str, d)

    protected fun atLeastOneOf(vararg keys: String) {
        atLeastOne += keys.toList()
    }

    fun fill(ctx: ParserRuleContext, params: List<BitsmapParser.ParamContext>, fail: ParamFail) {
        given.clear()
        val givenKeys = mutableSetOf<String>()
        for (param in params) {
            val key = param.ID().text
            val check = validators[key]
            if (check == null) {
                fail(param, "Parameter '$key' doesn't exist on '$field'")
                continue
            }
            if (!givenKeys.add(key)) {
                fail(param, "Parameter '$key' can't be defined twice on '$field'")
                continue
            }

            val value = param.paramValue()
            if (value == null) {
                fail(param, "Parameter '$key' needs a value")
                continue
            }
            if (!check(value)) {
                fail(param, "Parameter '$key' has the wrong value type")
            }
            given[key] = param
        }

        for (param in requiredKeys) {
            if (param !in givenKeys) {
                fail(ctx, "Missing required parameter '$param' on '$field'")
            }
        }
        for (group in atLeastOne) {
            if (group.none { it in givenKeys }) {
                fail(ctx, "At least one of ${group.joinToString { "'$it'" }} must be given on '$field'")
            }
        }
    }

    companion object {
        val fieldParamList = mutableMapOf<String, MutableSet<String>>()
        val registry = listOf(FlowParams(), TransformParams("rotate"), TransformParams("move")).associateBy { it.field }
        fun init() { registry }
    }
}

class FlowParams : ParamSet("flow") {
    val flowIn by num("in", 1.0)
    val flowOut by num("out", 1.0)

    val gravIn by num("grav_in", 1.0).optional()
    val gravOut by num("grav_out", 1.0).optional()

    val smashIn by num("smash_in", 0.0).optional()
    val smashOut by num("smash_out", 0.0).optional()
    val smashInSpeed by num("smash_in_speed", 0.0).optional()
    val smashOutSpeed by num("smash_out_speed", 0.0).optional()

    fun toCommand(): FlowCommand? = FlowCommand(
        flowIn = flowIn?.value ?: return null,
        flowOut = flowOut?.value ?: return null,
        gravityIn = gravIn?.value,
        gravityOut = gravOut?.value,
        smashIn = smashIn?.value,
        smashOut = smashOut?.value,
        smashInSpeed = smashInSpeed?.value,
        smashOutSpeed = smashOutSpeed?.value,
        easing = Easing.Default,  // TODO: Add easing
    )
}

class TransformParams(field: String) : ParamSet(field) {
    val x by num("x", 0.0).optional()
    val y by num("y", 0.0).optional()
    val z by num("z", 0.0).optional()
    init { atLeastOneOf("x", "y", "z") }
}