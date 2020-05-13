package kommand

import kommand.DurationUnit.Companion.from

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


enum class DurationUnit(val ms: Long, vararg val aliases: String) {
    MS(1, "milliseconds", "millisecond"),
    MIN(1000 * 60, "minutes", "minute", "m"),
    HR(MIN.ms * 60, "hours", "hour", "hrs", "hr", "h"),
    DAY(HR.ms * 24, "days", "day", "d"),
    SEC(MS.ms * 1000, "seconds", "second", "s");

    fun interval(str: String): Int? {
        var string = str.toLowerCase().replace(name, "")
        aliases.forEach {
            string = string.replace(it, "")
        }
        return string.trim().toIntOrNull()
    }

    companion object {

        fun from(str: String): DurationUnit? {
            for (value in values()) {
                if (value.name.endsWith(str.toLowerCase())) {
                    return value
                }
                for (alias in value.aliases) {
                    if (str.toLowerCase().endsWith(alias)) {
                        return value
                    }
                }
            }
            return null
        }

    }
}

data class Duration(val time: Int, val unit: DurationUnit) {
    val ms = unit.ms * time
}

//ex: 1d or 1hr or 1hour or 2hours OR 1 hr 1 d
fun parseDuration(str: String): Duration? {
    val duration = from(str) ?: return null
    return Duration(duration.interval(str) ?: return null, duration)
}

object DurationParser: ArgumentParser<Duration>({
    if (from(it[0]) == null && it.size >= 2 && from(it[1]) != null) {
        2
    } else {
        1
    }
}) {
    override fun parseToken(token: String): Duration? {
        return parseDuration(token)
    }
}
