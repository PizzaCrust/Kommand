package kommand

import java.lang.StringBuilder
import kotlin.reflect.KProperty

interface PermissionRequirement<S> {
    fun allowed(source: S): Boolean
}

fun <S> hasAllRequirements(source: S, reqs: List<PermissionRequirement<S>>): Boolean {
    var allowed = true
    reqs.forEach {
        if (!it.allowed(source)) {
            allowed = false
        }
    }
    return allowed
}

// parser and whether its required
typealias ArgumentDefinition<T> = Pair<ArgumentParser<T>, Boolean>

abstract class Command<S>(val prefix: String = "!",
                          val name: String,
                          val requirements: List<PermissionRequirement<S>> = listOf()) {

    protected fun respond(msg: String) {
        println(msg)
    }

    protected abstract fun called(source: S)

    private val argParsers: List<ArgumentParser<*>>
        get() {
            val parsers = mutableListOf<ArgumentParser<*>>()
            argDefinitions.forEach {
                parsers.add(it.first)
            }
            return parsers
        }

    private fun verifyParameters(values: List<*>): Boolean {
        argDefinitions.forEachIndexed { index, pair ->
            if (pair.second) {
                if (values.size <= index) {
                    return false
                }
                if (values[index] == null) {
                    return false
                }
            }
        }
        return true
    }

    val usage: String
        get() {
            val builder = StringBuilder()
            builder.append("Usage: $prefix$name ")
            argDefinitions.forEach {
                builder.append("<")
                if (it.second) {
                    builder.append("REQUIRED: ")
                } else {
                    builder.append("OPTIONAL: ")
                }
                builder.append(it.first.javaClass.simpleName).append("> ")
            }
            return builder.toString().trim()
        }

    private var argsValues = mutableListOf<Any?>()

    @Suppress("UNCHECKED_CAST")
    class ArgDelegate<T> internal constructor(val index: Int, val argsValues: List<Any?>) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
            if (argsValues.size <= index) {
                return null
            }
            return argsValues[index] as T
        }
    }

    @Suppress("UNCHECKED_CAST")
    class ReqArgDelegate<T> internal constructor(val index: Int, val argsValues: List<Any?>) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return argsValues[index] as T
        }
    }

    private val argDefinitions: MutableList<ArgumentDefinition<*>> = mutableListOf()

    private var delegationIndex = 0

    fun <T> optionalArgument(parser: ArgumentParser<T>): ArgDelegate<T> {
        argDefinitions.add(parser to false)
        var delegate = ArgDelegate<T>(delegationIndex, argsValues)
        delegationIndex++
        return delegate
    }

    fun <T> argument(parser: ArgumentParser<T>): ReqArgDelegate<T> {
        argDefinitions.add(parser to true)
        var delegate = ReqArgDelegate<T>(delegationIndex, argsValues)
        delegationIndex++
        return delegate
    }

    fun execute(source: S, cmd: String) {
        if (!cmd.startsWith("$prefix$name")) {
            return
        }
        if (!hasAllRequirements(source, requirements)) {
            respond("Missing permission(s) to execute this command.")
            return
        }
        val tokens = cmd.trim().split(" ").toMutableList()
        tokens.removeAt(0)
        val values = parseArguments(tokens, argParsers)
        if (!verifyParameters(values)) {
            respond(usage)
            return
        }
        argsValues.clear()
        argsValues.addAll(values)
        called(source)
    }

}

class TestRequirement(val value: Boolean): PermissionRequirement<Any> {
    override fun allowed(source: Any): Boolean {
        return value
    }
}

class TestCommand:
        Command<Any>(
                name = "test",
                requirements = listOf(TestRequirement(true), TestRequirement(true))
        ) {

    val integer: Int by argument(IntegerParser)
    val integer1: Int by argument(IntegerParser)
    val integer2: Int? by optionalArgument(IntegerParser)
    val leftover by optionalArgument(LeftoverParser)

    override fun called(source: Any) {
        println("$integer $integer1 $integer2 $leftover")
    }
}

fun main() {
    TestCommand().execute(0, "!test 1 1 test test test test")
}