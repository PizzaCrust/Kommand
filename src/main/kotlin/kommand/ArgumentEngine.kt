package kommand

fun fullLengthToken(token: List<String>): Int {
    return token.size
}

fun specificLengthToken(tokenSize: Int): (List<String>) -> Int {
    return {
        tokenSize
    }
}

// main disadvantage, can't detect correct vararg token size by detecting next token due to lack
// of data
abstract class ArgumentParser<T>(val tokenSizeFunc: (List<String>) -> Int = ::fullLengthToken) {

    private fun readToken(tokenList: MutableList<String>): String {
        var sizeToRead = tokenSizeFunc(tokenList)
        if (tokenSizeFunc(tokenList) == -1) {
            sizeToRead = tokenList.size
        }
        val builder = StringBuilder()
        for (i in 0 until sizeToRead) {
            builder.append(tokenList[0]).append(" ")
            tokenList.removeAt(0)
        }
        return builder.toString().trim()
    }

    protected abstract fun parseToken(token: String): T?

    fun parse(tokenList: MutableList<String>): T? {
        return parseToken(readToken(tokenList))
    }

}

fun parseArguments(argList: MutableList<String>, parsers: List<ArgumentParser<*>>): List<Any?> {
    val values = mutableListOf<Any?>()
    parsers.forEach {
        if (argList.isNotEmpty()) {
            values.add(it.parse(argList))
        }
    }
    return values
}