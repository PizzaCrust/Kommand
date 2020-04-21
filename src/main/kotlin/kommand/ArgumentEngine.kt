package kommand

abstract class ArgumentParser<T>(val tokenSize: Int = -1) {

    private fun readToken(tokenList: MutableList<String>): String {
        var sizeToRead = tokenSize
        if (tokenSize == -1) {
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