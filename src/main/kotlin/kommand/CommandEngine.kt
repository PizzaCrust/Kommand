package kommand

import java.lang.StringBuilder

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
                          val requirements: List<PermissionRequirement<S>> = listOf(),
                          val arguments: List<ArgumentDefinition<*>> = listOf()) {

    protected fun respond(msg: String) {
        println(msg)
    }

    protected abstract fun called(source: S, argumentValues: List<*>)

    private val argParsers: List<ArgumentParser<*>>
        get() {
            val parsers = mutableListOf<ArgumentParser<*>>()
            arguments.forEach {
                parsers.add(it.first)
            }
            return parsers
        }

    private fun verifyParameters(values: List<*>): Boolean {
        arguments.forEachIndexed { index, pair ->
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
            arguments.forEach {
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
        called(source, values)
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
                requirements = listOf(TestRequirement(true), TestRequirement(true)),
                arguments = listOf(IntegerParser() to true,
                        LeftoverParser() to false)
        ) {
    override fun called(source: Any, argumentValues: List<*>) {
        println(argumentValues)
    }
}

fun main() {
    TestCommand().execute(0, "!test 1")
}