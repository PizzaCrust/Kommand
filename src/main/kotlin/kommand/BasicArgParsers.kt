package kommand

object IntegerParser: ArgumentParser<Int>(1) {
    override fun parseToken(token: String): Int? {
        return token.toIntOrNull()
    }
}

class VarargParser<T>(private val singleTokenParser: ArgumentParser<T>): ArgumentParser<List<T>>() {
    override fun parseToken(token: String): List<T>? {
        TODO("Not yet implemented")
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