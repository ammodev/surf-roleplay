package dev.slne.surf.roleplay.fabric.server

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Tracks whether the current connection is to the roleplay server.
 *
 * The state is active once the roleplay server has welcomed the client on the current
 * connection, and inactive otherwise. Roleplay behaviour of the mod runs only while it is active.
 */
class RoleplayServerState {

    /**
     * The listeners notified whenever the state changes.
     */
    private val listeners = CopyOnWriteArrayList<(Boolean) -> Unit>()

    /**
     * Whether the current connection is to the roleplay server.
     */
    @Volatile
    var isActive: Boolean = false
        private set

    /**
     * Registers a listener that is called with the new value whenever the state changes.
     *
     * @param listener the listener to call
     */
    fun onChange(listener: (Boolean) -> Unit) {
        listeners += listener
    }

    /**
     * Marks the current connection as the roleplay server. Does nothing if it already is.
     */
    fun activate() = set(true)

    /**
     * Marks the current connection as not the roleplay server. Does nothing if it already is not.
     */
    fun reset() = set(false)

    /**
     * Changes the state and notifies the listeners if the value differs from the current one.
     *
     * @param active the new value
     */
    @Synchronized
    private fun set(active: Boolean) {
        if (isActive == active) return
        isActive = active
        listeners.forEach { it(active) }
    }
}
