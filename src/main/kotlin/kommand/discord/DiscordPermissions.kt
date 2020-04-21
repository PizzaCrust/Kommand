package kommand.discord

import kommand.PermissionRequirement
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

internal typealias ReceiveMessageRequirement = PermissionRequirement<GuildMessageReceivedEvent>

private class DiscordRolePermissionsReq(private val permissions: List<Permission>): ReceiveMessageRequirement {
    override fun allowed(source: GuildMessageReceivedEvent): Boolean {
        for (permission in permissions) {
            if (!source.member!!.hasPermission(permission)) {
                return false
            }
        }
        return true
    }
}

fun require(vararg permissions: Permission): PermissionRequirement<*> {
    return DiscordRolePermissionsReq(permissions.toList())
}

private class DiscordRoleReq(private val roles: List<Role>): ReceiveMessageRequirement {
    override fun allowed(source: GuildMessageReceivedEvent): Boolean {
        return source.member!!.roles.containsAll(roles)
    }
}

fun require(vararg roles: Role): PermissionRequirement<*> {
    return DiscordRoleReq(roles.toList())
}

private class SpecificUserReq(private val userIds: List<Long>): ReceiveMessageRequirement {
    override fun allowed(source: GuildMessageReceivedEvent): Boolean {
        return userIds.contains(source.member!!.idLong)
    }
}

fun require(vararg users: Long): PermissionRequirement<*> {
    return SpecificUserReq(users.toList())
}