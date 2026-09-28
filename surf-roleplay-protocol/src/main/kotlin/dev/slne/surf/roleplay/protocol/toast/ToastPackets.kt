package dev.slne.surf.roleplay.protocol.toast

import dev.slne.surf.roleplay.protocol.Packet
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * The type of a toast, which sets its icon.
 */
@Serializable
enum class ToastType {
    /**
     * A plain toast without an icon.
     */
    @ProtoNumber(0)
    DEFAULT,

    /**
     * A success, with a check mark.
     */
    @ProtoNumber(1)
    SUCCESS,

    /**
     * An information, with an info icon.
     */
    @ProtoNumber(2)
    INFO,

    /**
     * A warning, with a warning triangle.
     */
    @ProtoNumber(3)
    WARNING,

    /**
     * An error, with an octagon.
     */
    @ProtoNumber(4)
    ERROR,

    /**
     * Something in progress, with a turning spinner.
     */
    @ProtoNumber(5)
    LOADING,
}

/**
 * A button of a toast.
 */
@Serializable
enum class ToastButtonKind {
    /**
     * The action button.
     */
    @ProtoNumber(0)
    ACTION,

    /**
     * The cancel button.
     */
    @ProtoNumber(1)
    CANCEL,
}

/**
 * Shows a toast, or replaces the shown toast with the same id.
 *
 * @property id the id of the toast, chosen by the server
 * @property type the type of the toast
 * @property title the title as component JSON
 * @property description the description as component JSON, or `null` for none
 * @property actionLabel the label of the action button as component JSON, or `null` for none
 * @property cancelLabel the label of the cancel button as component JSON, or `null` for none
 * @property durationMillis how long the toast is shown, in milliseconds, not counting the time
 *           the mouse rests on the toasts; `0` shows it until it is dismissed
 * @property dismissible whether the player can close the toast
 * @property closeButton whether a close button is drawn on the toast
 */
@Serializable
data class ToastShow(
    @ProtoNumber(1) val id: String,
    @ProtoNumber(2) val type: ToastType = ToastType.DEFAULT,
    @ProtoNumber(3) val title: String = "",
    @ProtoNumber(4) val description: String? = null,
    @ProtoNumber(5) val actionLabel: String? = null,
    @ProtoNumber(6) val cancelLabel: String? = null,
    @ProtoNumber(7) val durationMillis: Int = 4000,
    @ProtoNumber(8) val dismissible: Boolean = true,
    @ProtoNumber(9) val closeButton: Boolean = false,
) : Packet

/**
 * Removes a shown toast.
 *
 * @property id the id of the toast
 */
@Serializable
data class ToastDismiss(
    @ProtoNumber(1) val id: String,
) : Packet

/**
 * Reports that the player clicked a button of a toast.
 *
 * @property id the id of the toast
 * @property button the button
 */
@Serializable
data class ToastAction(
    @ProtoNumber(1) val id: String,
    @ProtoNumber(2) val button: ToastButtonKind,
) : Packet
