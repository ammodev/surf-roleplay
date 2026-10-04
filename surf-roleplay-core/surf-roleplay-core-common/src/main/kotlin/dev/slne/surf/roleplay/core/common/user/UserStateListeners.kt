package dev.slne.surf.roleplay.core.common.user

import dev.slne.surf.api.core.util.logger
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

private val log = logger()

/**
 * Receives a notice whenever the in-memory identity state of a user changes: the active identity
 * is set or cleared, or a new state of the user's identities is applied.
 *
 * Listeners are called on the thread that made the change, possibly while the user's write lock
 * is held. They must return quickly and must not start a write operation of the user.
 */
fun interface UserStateListener {
    /**
     * Called after the identity state of a user changed.
     *
     * @param userUuid the UUID of the user whose state changed
     */
    fun stateChanged(userUuid: UUID)
}

/**
 * The registered [UserStateListener]s of this process.
 */
object UserStateListeners {
    /**
     * The registered listeners in registration order.
     */
    private val listeners = CopyOnWriteArrayList<UserStateListener>()

    /**
     * Registers a listener.
     *
     * @param listener the listener
     * @return a handle that unregisters the listener when closed
     */
    fun register(listener: UserStateListener): AutoCloseable {
        listeners += listener
        return AutoCloseable { listeners.remove(listener) }
    }

    /**
     * Notifies every registered listener that the identity state of a user changed. A listener
     * that throws is logged and does not keep the others from being notified.
     *
     * @param userUuid the UUID of the user whose state changed
     */
    internal fun notifyChanged(userUuid: UUID) {
        listeners.forEach { listener ->
            try {
                listener.stateChanged(userUuid)
            } catch (exception: Exception) {
                log.atWarning().withCause(exception).log("User state listener %s failed for user %s", listener, userUuid)
            }
        }
    }
}
