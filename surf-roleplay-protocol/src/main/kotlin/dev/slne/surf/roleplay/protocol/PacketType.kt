package dev.slne.surf.roleplay.protocol

import kotlinx.serialization.KSerializer

/**
 * The description of one kind of [Packet]: the channel it travels on, its direction, the
 * connection phases it may be sent in, and its serializer.
 *
 * @param P the packet class
 * @property name the snake_case name of the packet, which forms its channel
 * @property direction the side the packet travels to
 * @property phases the connection phases in which the packet may be sent
 * @property serializer the serializer that encodes and decodes the packet
 * @throws IllegalArgumentException if [name] is not snake_case or [phases] is empty
 */
class PacketType<P : Packet>(
    val name: String,
    val direction: PacketDirection,
    val phases: Set<ConnectionPhase>,
    val serializer: KSerializer<P>,
) {
    init {
        require(NAME_PATTERN.matches(name)) { "Packet name '$name' is not snake_case" }
        require(phases.isNotEmpty()) { "Packet '$name' has no connection phase" }
    }

    /**
     * The payload channel id of this packet, `roleplay:<name>`.
     */
    val channel: String = "$PROTOCOL_NAMESPACE:$name"

    /**
     * Returns a readable description of this packet type, naming its channel and direction.
     *
     * @return the description
     */
    override fun toString(): String = "PacketType($channel, $direction)"

    /**
     * Holds the name pattern shared by every packet type.
     */
    private companion object {
        /**
         * The pattern every packet name must match.
         */
        val NAME_PATTERN = Regex("[a-z0-9_]+")
    }
}
