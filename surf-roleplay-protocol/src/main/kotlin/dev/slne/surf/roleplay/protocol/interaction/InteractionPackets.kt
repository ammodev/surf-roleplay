package dev.slne.surf.roleplay.protocol.interaction

import dev.slne.surf.roleplay.protocol.Packet
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * The limits shared by the server and the client mod for interaction packets.
 */
object InteractionLimits {
    /**
     * The longest reach any binding may have, in blocks; the client raycasts this far.
     */
    const val MAX_RANGE: Double = 8.0

    /**
     * The longest target id the server accepts.
     */
    const val MAX_TARGET_ID_LENGTH: Int = 64

    /**
     * The largest number of options in one target offer.
     */
    const val MAX_OPTIONS: Int = 16

    /**
     * The largest number of nodes in one radial offer.
     */
    const val MAX_RADIAL_NODES: Int = 128
}

/**
 * The target kinds the first version of the protocol uses. Kinds are an open set of
 * snake_case strings, so later systems add their own without changing the packets.
 */
object TargetKinds {
    /**
     * A block, identified by [BlockTargetId] in the player's world.
     */
    const val BLOCK: String = "block"

    /**
     * An entity, identified by its UUID in text form.
     */
    const val ENTITY: String = "entity"
}

/**
 * The text form `x,y,z` of a block position used as the id of a block target.
 */
object BlockTargetId {
    /**
     * A block position.
     *
     * @property x the x coordinate
     * @property y the y coordinate
     * @property z the z coordinate
     */
    data class Position(val x: Int, val y: Int, val z: Int)

    /**
     * The largest absolute horizontal coordinate the server accepts.
     */
    private const val MAX_HORIZONTAL: Int = 30_000_000

    /**
     * The largest absolute vertical coordinate the server accepts.
     */
    private const val MAX_VERTICAL: Int = 4096

    /**
     * The pattern of a valid id.
     */
    private val PATTERN = Regex("(-?\\d{1,9}),(-?\\d{1,9}),(-?\\d{1,9})")

    /**
     * Formats a block position as a target id.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return the id
     */
    fun format(x: Int, y: Int, z: Int): String = "$x,$y,$z"

    /**
     * Parses a target id into a block position.
     *
     * @param id the id
     * @return the position, or `null` if the id is malformed or outside the world limits
     */
    fun parse(id: String): Position? {
        val match = PATTERN.matchEntire(id) ?: return null
        val (x, y, z) = match.destructured.toList().map { it.toInt() }
        if (kotlin.math.abs(x) > MAX_HORIZONTAL || kotlin.math.abs(z) > MAX_HORIZONTAL || kotlin.math.abs(y) > MAX_VERTICAL) return null
        return Position(x, y, z)
    }
}

/**
 * Asks the server which options the player has for a target.
 *
 * @property kind the target kind, such as [TargetKinds.BLOCK]
 * @property id the target id within its kind
 */
@Serializable
data class InteractionTargetQuery(
    @ProtoNumber(1) val kind: String,
    @ProtoNumber(2) val id: String,
) : Packet

/**
 * One option of a target offer.
 *
 * @property label the caption as component JSON
 * @property icon the Lucide icon name, empty for none
 */
@Serializable
data class InteractionOption(
    @ProtoNumber(1) val label: String,
    @ProtoNumber(2) val icon: String = "",
)

/**
 * The options the player has for a target. An empty list means the target offers nothing.
 *
 * @property offerId the id an execute must reference, `0` when there are no options
 * @property kind the target kind of the query
 * @property id the target id of the query
 * @property options the options in display order
 */
@Serializable
data class InteractionTargetOffer(
    @ProtoNumber(1) val offerId: Int,
    @ProtoNumber(2) val kind: String,
    @ProtoNumber(3) val id: String,
    @ProtoNumber(4) val options: List<InteractionOption> = emptyList(),
) : Packet

/**
 * Asks the server for the player's radial menu.
 */
@Serializable
data object InteractionRadialRequest : Packet

/**
 * One node of a radial menu.
 *
 * @property parent the index of the parent node, or `-1` for a node of the top level
 * @property label the caption as component JSON
 * @property icon the Lucide icon name, empty for none
 * @property submenu whether picking the node opens its children instead of running an action
 */
@Serializable
data class RadialNode(
    @ProtoNumber(1) val parent: Int = -1,
    @ProtoNumber(2) val label: String,
    @ProtoNumber(3) val icon: String = "",
    @ProtoNumber(4) val submenu: Boolean = false,
)

/**
 * The player's radial menu as one tree. Every node comes after its parent. An empty list opens
 * nothing.
 *
 * @property offerId the id an execute must reference, `0` when the tree is empty
 * @property nodes the nodes, at most [InteractionLimits.MAX_RADIAL_NODES]
 */
@Serializable
data class InteractionRadialOffer(
    @ProtoNumber(1) val offerId: Int,
    @ProtoNumber(2) val nodes: List<RadialNode> = emptyList(),
) : Packet

/**
 * Reports that the player picked an option of a target offer or a node of a radial offer.
 *
 * @property offerId the offer the pick belongs to
 * @property index the option index or node index
 */
@Serializable
data class InteractionExecute(
    @ProtoNumber(1) val offerId: Int,
    @ProtoNumber(2) val index: Int,
) : Packet

/**
 * Tells the mod whether the target eye and the radial menu may be shown.
 *
 * @property available whether they may be shown
 */
@Serializable
data class InteractionAvailability(
    @ProtoNumber(1) val available: Boolean,
) : Packet
