package kommand

class IntegerParser: ArgumentParser<Int>(1) {
    override fun parseToken(token: String): Int? {
        return token.toIntOrNull()
    }
}

class RemainderContentParser: ArgumentParser<String>() {
    override fun parseToken(token: String): String? {
        if (token.isEmpty()) {
            return null
        }
        return token
    }
}