package kommand

object IntegerParser: ArgumentParser<Int>(specificLengthToken(1)) {
    override fun parseToken(token: String): Int? {
        return token.toIntOrNull()
    }
}

class FullVarargParser<T>(private val singleTokenParser: ArgumentParser<T>): ArgumentParser<List<T?>>() {
    override fun parseToken(token: String): List<T?> {
        val list = mutableListOf<T?>()
        val tokenList = token.split(" ")
        val readerList = tokenList.toMutableList()
        for (s in tokenList) {
            if (readerList.isEmpty()) {
                break
            }
            list.add(singleTokenParser.parse(readerList))
        }
        return list
    }
}

object LeftoverParser: ArgumentParser<String>() {
    override fun parseToken(token: String): String? {
        if (token.isEmpty()) {
            return null
        }
        return token
    }
}