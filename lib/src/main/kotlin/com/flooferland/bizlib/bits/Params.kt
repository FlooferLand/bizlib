package com.flooferland.bizlib.bits

import com.flooferland.bizlib.bits.generated.BitsmapParser
import org.antlr.v4.runtime.ParserRuleContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

private typealias ParamFail = (ParserRuleContext, String) -> Unit

data class ParamId(val target: Boolean = false, val name: String? = null) {
    companion object {
        fun <T> from(param: Param<T>): ParamId {
            return if (param.name == ParamSet.TARGET) { ParamId(target = true) } else { ParamId(name = param.name) }
        }
        fun from(ctx: BitsmapParser.ParamNameContext?): ParamId {
            val name = ctx?.text
            return if (name == null) { ParamId(target = true) } else { ParamId(name = name) }
        }
    }
}

abstract class ParamSet(val field: String) {
    val params = mutableMapOf<ParamId, Param<*>>()

    fun fill(ctx: ParserRuleContext, paramsCtx: BitsmapParser.ParamsContext, fail: ParamFail) {
        for (paramCtx in paramsCtx.unnamedParam()) {
            val valueCtx = paramCtx.paramValue()

            val id = ParamId(target = true)
            val param = params[id] ?: run {
                fail(valueCtx, "Expected a parameter")
                continue
            }

            if (!param.parseSetValue(valueCtx)) {
                fail(valueCtx, "Failed to parse unnamed parameter. Expected type '${param.typeName}'")
                continue
            }
        }

        for (paramCtx in paramsCtx.namedParam()) {
            val nameCtx = paramCtx.paramName()
            val valueCtx = paramCtx.paramValue()

            val id = ParamId.from(nameCtx)
            if (id.name == TARGET) {
                fail(nameCtx ?: ctx, "'$TARGET' can't be used directly")
                continue
            }

            val param = params[id]
            if (param == null) {
                fail(nameCtx ?: ctx, "Failed to find parameter for name '${nameCtx?.text}'")
                continue
            }

            if (!param.parseSetValue(valueCtx)) {
                fail(valueCtx, "Failed to parse parameter '${nameCtx?.text}'. Expected type '${param.typeName}'")
                continue
            }
        }
    }

    companion object {
        /** Should be replaced with something less sketchy.. */
        const val TARGET = "target"

        val registry by lazy {
            listOf(
                FlowParams(),
                TransformParams("rotate"),
                TransformParams("move")
            ).associateBy { it.field }
        }
        fun init() { registry }
    }
}

@Suppress("unused")
abstract class Param<T>(val name: String) : ReadOnlyProperty<ParamSet, Param<T>> {
    private var nameless = false
    private var optional = false
    private var default: T? = null
    var completionResults = listOf<String>()
    var value: T? = null

    fun nameless() = also { nameless = true }
    fun optional() = also { optional = true }
    fun default(value: T) = also { default = value }

    operator fun provideDelegate(thisRef: ParamSet, property: KProperty<*>): Param<T> {
        val id = ParamId.from(this)
        thisRef.params[id] = this
        return this
    }

    override fun getValue(thisRef: ParamSet, property: KProperty<*>): Param<T> {
        return this
    }

    fun parseSetValue(ctx: BitsmapParser.ParamValueContext): Boolean {
        value = parse(ctx)
        return value != null
    }

    abstract val typeName: String
    abstract fun parse(ctx: BitsmapParser.ParamValueContext): T?

    internal class Num(name: String) : Param<Double>(name) {
        override val typeName = "num"
        override fun parse(ctx: BitsmapParser.ParamValueContext) = ctx.num()?.text?.toDoubleOrNull()
    }
    internal class Bool(name: String) : Param<BooleanType>(name) {
        override val typeName = "bool"
        override fun parse(ctx: BitsmapParser.ParamValueContext) = ctx.BOOLEAN()?.text?.let { BooleanType.from(it) }
    }
    internal class Str(name: String) : Param<String>(name) {
        override val typeName = "str"
        override fun parse(ctx: BitsmapParser.ParamValueContext) = ctx.STRING()?.text?.removeSurrounding("\"")
    }
    internal class FlowType : Param<String>(ParamSet.TARGET) {
        override val typeName = "flow_type"
        init {
            nameless()
            completionResults = listOf("speed", "accel", "smash", "smash_speed")
        }
        override fun parse(ctx: BitsmapParser.ParamValueContext) = ctx.FLOW_TYPE()?.text
    }
}

class FlowParams : ParamSet("flow") {
    val target by Param.FlowType()
    val valueIn by Param.Num("in").default(1.0)
    val valueOut by Param.Num("out").default(1.0)
}

class TransformParams(field: String) : ParamSet(field) {
    val x by Param.Num("x").default(0.0).optional()
    val y by Param.Num("y").default(0.0).optional()
    val z by Param.Num("z").default(0.0).optional()
    // init { forceOneOf("x", "y", "z") }
}