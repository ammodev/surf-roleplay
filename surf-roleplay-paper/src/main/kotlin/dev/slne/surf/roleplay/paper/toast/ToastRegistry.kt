package dev.slne.surf.roleplay.paper.toast

import dev.slne.surf.roleplay.api.client.common.toast.Toast
import dev.slne.surf.roleplay.api.client.common.toast.ToastButton
import dev.slne.surf.roleplay.api.client.common.toast.ToastClick
import dev.slne.surf.roleplay.protocol.toast.ToastButtonKind
import java.util.UUID

/**
 * The toasts one player was shown that can still be clicked, with their button handlers.
 *
 * A toast stays active until it is dismissed, replaced, clicked, or old enough that the player can
 * no longer see it: its duration plus [GRACE_MILLIS], or [MAX_AGE_MILLIS] for toasts shown until
 * dismissed. At most [MAX_ACTIVE] toasts are kept; the oldest go first.
 *
 * @property viewer the UUID of the player
 */
class ToastRegistry(private val viewer: UUID = UUID(0, 0)) {

    /**
     * An active toast.
     *
     * @property toast the toast
     * @property expiresAt when it can no longer be clicked, in milliseconds
     */
    private data class Active(val toast: Toast, val expiresAt: Long)

    /**
     * The outcome of a click.
     */
    sealed interface Outcome {
        /**
         * The click is valid.
         *
         * @property run runs the button's handler, if it has one
         */
        data class Accepted(val run: () -> Unit) : Outcome

        /**
         * The click is invalid.
         *
         * @property reason why, for the log
         */
        data class Rejected(val reason: String) : Outcome
    }

    /**
     * The active toasts by id, oldest first.
     */
    private val active = LinkedHashMap<String, Active>()

    /**
     * The number of the next generated id.
     */
    private var next: Long = 0

    /**
     * Records a toast that is being shown, replacing an active toast with the same id.
     *
     * @param toast the toast
     * @param now the time in milliseconds
     * @return the id of the toast
     */
    @Synchronized
    fun register(toast: Toast, now: Long): String {
        val id = toast.id ?: "toast-${next++}"
        active.remove(id)
        val lifetime = if (toast.durationMillis == 0) MAX_AGE_MILLIS else toast.durationMillis + GRACE_MILLIS
        active[id] = Active(toast, now + lifetime)
        while (active.size > MAX_ACTIVE) active.remove(active.keys.first())
        return id
    }

    /**
     * Forgets a toast.
     *
     * @param id the id of the toast
     */
    @Synchronized
    fun dismiss(id: String) {
        active.remove(id)
    }

    /**
     * Validates a click on a button of a toast. An accepted click also ends the toast.
     *
     * @param id the id of the toast
     * @param button the button
     * @param now the time in milliseconds
     * @return the outcome
     */
    @Synchronized
    fun handle(id: String, button: ToastButtonKind, now: Long): Outcome {
        val entry = active[id] ?: return Outcome.Rejected("unknown toast")
        if (now >= entry.expiresAt) {
            active.remove(id)
            return Outcome.Rejected("expired toast")
        }
        val pressed: ToastButton = when (button) {
            ToastButtonKind.ACTION -> entry.toast.action
            ToastButtonKind.CANCEL -> entry.toast.cancel
        } ?: return Outcome.Rejected("toast has no ${button.name.lowercase()} button")
        active.remove(id)
        return Outcome.Accepted { pressed.onClick?.onClick(ToastClick(viewer, id)) }
    }

    /**
     * Holds the registry limits.
     */
    companion object {
        /**
         * How long after its duration a toast can still be clicked, covering the time the mouse
         * rested on the toasts.
         */
        const val GRACE_MILLIS: Long = 60_000

        /**
         * How long a toast shown until dismissed can be clicked.
         */
        const val MAX_AGE_MILLIS: Long = 600_000

        /**
         * The largest number of active toasts per player.
         */
        const val MAX_ACTIVE: Int = 32
    }
}
