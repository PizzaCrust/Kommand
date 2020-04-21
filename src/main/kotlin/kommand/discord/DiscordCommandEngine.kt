package kommand.discord

import kommand.Command
import kommand.PermissionRequirement
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

fun TextChannel.msg(msg: String) {
    sendMessage(msg).queue()
}

abstract class DiscordCommand(prefix: String = "!",
                     name: String,
                     requirements: List<PermissionRequirement<GuildMessageReceivedEvent>>):
        Command<GuildMessageReceivedEvent>(prefix, name, requirements) {
    private var channel: TextChannel? = null

    override fun respond(msg: String) {
        if (channel == null) {
            throw Exception("Attempting to send message with no channel")
        }
        channel!!.msg(msg)
    }

    override fun initLogging(source: GuildMessageReceivedEvent) {
        channel = source.channel
    }

}