package kommand

import java.lang.StringBuilder
import kotlin.reflect.KProperty

interface PermissionRequirement<S> {
    fun allowed(source: S): Boolean
}


abstract class Command<S>(val prefix: String = "!",
                          val name: String,
                          val requirements: List<PermissionRequirement<S>> = listOf()) {

    protected open fun respond(msg: String) {
        println(msg)
    }

    protected abstract fun called(source: S)

    val usage: String
        get() {
            val builder = StringBuilder()
            builder.append("Usage: $prefix$name ")
            argDefinitions.forEach {
                builder.append("<")
                if (it.required) {
                    builder.append("REQUIRED: ")
                } else {
                    builder.append("OPTIONAL: ")
                }
                builder.append(it.type.simpleName).append("> ")
            }
            return builder.toString().trim()
        }

    data class ArgumentDefinition<T>(val typeParser: ArgumentParser<T>,
                                     val type: Class<T>, val required: Boolean) {
        var value: T? = null
        fun parse(argList: MutableList<String>): Boolean {
            value = typeParser.parse(argList)
            if (value == null && required) return false
            return true
        }
    }

    @Suppress("UNCHECKED_CAST")
    abstract class ArgDelegate<T>(val definition: ArgumentDefinition<T>) {
        open operator fun getValue(thisRef: Any?, property: KProperty<*>): T = definition.value as T
    }

    class OptArgDelegate<T: Any?>(definition: ArgumentDefinition<T>): ArgDelegate<T>(definition)

    class ReqArgDelegate<T: Any>(definition: ArgumentDefinition<T>): ArgDelegate<T>(definition)

    val argDefinitions: MutableList<ArgumentDefinition<*>> = mutableListOf()

    inline fun <reified T: Any?> optionalArgument(parser: ArgumentParser<T>): ArgDelegate<T> {
        return OptArgDelegate(ArgumentDefinition(parser, T::class.java,false).apply { argDefinitions.add(this) })
    }

    inline fun <reified T: Any> argument(parser: ArgumentParser<T>): ArgDelegate<T> {
        return ReqArgDelegate(ArgumentDefinition(parser, T::class.java, true).apply { argDefinitions.add(this) })
    }

    protected open fun initLogging(source: S) {}

    fun execute(source: S, cmd: String) {
        argDefinitions.forEach {
            it.value = null
        }
        val tokens = cmd.trim().split(" ").toMutableList()
        if (tokens[0].replace(prefix, "") != name) {
            return
        }
        tokens.removeAt(0)
        initLogging(source)
        if (!requirements.all { it.allowed(source) }) {
            respond("Missing permission(s) to execute this command.")
            return
        }
        for (argDefinition in argDefinitions) {
            if (tokens.isEmpty()) {
                if (argDefinition.required) {
                    respond(usage)
                    return
                }
                break
            }
            val success = argDefinition.parse(tokens)
            if (!success) {
                respond(usage)
                return
            }
        }
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

    //val integer: List<Int?> by argument(FullVarargParser(IntegerParser))
    val integer1: Int by argument(IntegerParser)
    val integer2: Int? by optionalArgument(IntegerParser)
    //val leftover by optionalArgument(LeftoverParser)

    override fun called(source: Any) {
        println("$integer1 $integer2")
    }
}

fun main() {
    TestCommand().execute(0, "!test")
}