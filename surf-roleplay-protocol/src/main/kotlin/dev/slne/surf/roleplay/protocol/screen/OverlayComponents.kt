package dev.slne.surf.roleplay.protocol.screen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * The side of its trigger an overlay opens on.
 */
@Serializable
enum class OverlaySide {
    /**
     * Below the trigger.
     */
    @ProtoNumber(0)
    BOTTOM,

    /**
     * Above the trigger.
     */
    @ProtoNumber(1)
    TOP,

    /**
     * To the right of the trigger.
     */
    @ProtoNumber(2)
    RIGHT,

    /**
     * To the left of the trigger.
     */
    @ProtoNumber(3)
    LEFT,
}

/**
 * Opens or closes an overlay, such as a popover, menu or dialog.
 *
 * @property targetId the id of the overlay
 * @property open whether the overlay is open
 */
@Serializable
@SerialName("set_open")
data class SetOpen(
    @ProtoNumber(1) val targetId: String,
    @ProtoNumber(2) val open: Boolean,
) : PatchOperation
