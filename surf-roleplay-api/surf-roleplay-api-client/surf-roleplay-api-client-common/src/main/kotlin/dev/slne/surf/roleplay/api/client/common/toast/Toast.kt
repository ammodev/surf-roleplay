package dev.slne.surf.roleplay.api.client.common.toast

import net.kyori.adventure.text.Component
import java.util.UUID

/**
 * The type of a toast, which sets its icon.
 */
enum class ToastType {
    /**
     * A plain toast without an icon.
     */
    DEFAULT,

    /**
     * A success, with a check mark.
     */
    SUCCESS,

    /**
     * An information, with an info icon.
     */
    INFO,

    /**
     * A warning, with a warning triangle.
     */
    WARNING,

    /**
     * An error, with an octagon.
     */
    ERROR,

    /**
     * Something in progress, with a turning spinner.
     */
    LOADING,
}

/**
 * A click on a button of a toast.
 *
 * @property playerId the UUID of the player who clicked
 * @property toastId the id of the toast
 */
data class ToastClick(val playerId: UUID, val toastId: String)

/**
 * Runs on the server when the player clicks a button of a toast.
 */
fun interface ToastHandler {
    /**
     * Handles a click. It runs on the player's region thread, only for a toast the server showed
     * to that player that is still active.
     *
     * @param click the click
     */
    fun onClick(click: ToastClick)
}

/**
 * A button of a toast.
 *
 * @property label the caption
 * @property onClick the handler run when the player clicks the button, or `null` for none
 */
data class ToastButton(val label: Component, val onClick: ToastHandler? = null)

/**
 * A short notification shown at the bottom right, over the HUD and over any open screen. A click
 * on one of its buttons closes it.
 *
 * @property title the title
 * @property description the description below the title, or `null` for none
 * @property type the type of the toast, which sets its icon
 * @property action the action button, or `null` for none
 * @property cancel the cancel button, or `null` for none
 * @property durationMillis how long the toast is shown, in milliseconds, not counting the time the
 *           mouse rests on the toasts; `0` shows it until it is dismissed
 * @property dismissible whether the player can close the toast
 * @property closeButton whether a close button is drawn on the toast
 * @property id the id of the toast, to replace or dismiss it later, or `null` for a new id
 */
data class Toast(
    val title: Component,
    val description: Component? = null,
    val type: ToastType = ToastType.DEFAULT,
    val action: ToastButton? = null,
    val cancel: ToastButton? = null,
    val durationMillis: Int = if (type == ToastType.LOADING) 0 else DEFAULT_DURATION_MILLIS,
    val dismissible: Boolean = true,
    val closeButton: Boolean = false,
    val id: String? = null,
) {
    init {
        require(durationMillis >= 0) { "A toast duration must not be negative, got $durationMillis" }
        require(id == null || id.length in 1..MAX_ID_LENGTH) { "A toast id must have 1 to $MAX_ID_LENGTH characters" }
    }

    /**
     * Holds the toast defaults.
     */
    companion object {
        /**
         * How long a toast is shown by default, in milliseconds.
         */
        const val DEFAULT_DURATION_MILLIS: Int = 4000

        /**
         * The longest id of a toast.
         */
        const val MAX_ID_LENGTH: Int = 64
    }
}
