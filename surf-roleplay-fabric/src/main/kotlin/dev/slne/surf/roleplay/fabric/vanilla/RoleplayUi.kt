package dev.slne.surf.roleplay.fabric.vanilla

import dev.slne.surf.roleplay.fabric.RoleplayClient

/**
 * Entry point for the vanilla user interface mixins to ask whether the roleplay interface rules
 * apply to the current connection.
 */
object RoleplayUi {

    /**
     * Returns whether the client is connected to the roleplay server, which is when the vanilla
     * interface is restricted.
     */
    @JvmStatic
    fun isActive(): Boolean = RoleplayClient.serverState.isActive
}
