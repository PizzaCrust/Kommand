package kommand.discord

import kommand.ArgumentParser
import kommand.specificLengthToken
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User

lateinit var discord: JDA

fun String.hasIntChar(index: Int): Boolean {
    if (this.toCharArray().size <= index) {
        return false
    }
    return this.toCharArray()[index].toString().toIntOrNull() != null
}

object MentionArgParser: ArgumentParser<User>(specificLengthToken(1)) {
    override fun parseToken(token: String): User? {
        val newToken = token.replace("!", "")
        // check for <@ and <@(number) to make sure its user not other
        if (!newToken.startsWith("<@") || !newToken.hasIntChar(2)) {
            return null
        }
        val id = newToken.replace("<@", "").replace(">", "").toLong()
        return discord.getUserById(id)
    }
}

object RoleMentionArgParser: ArgumentParser<Role>(specificLengthToken(1)) {
    override fun parseToken(token: String): Role? {
        if (!token.startsWith("<@&") || !token.hasIntChar(3)) {
            return null
        }
        val id = token.replace("<@&", "").replace(">", "").toLong()
        return discord.getRoleById(id)
    }
}