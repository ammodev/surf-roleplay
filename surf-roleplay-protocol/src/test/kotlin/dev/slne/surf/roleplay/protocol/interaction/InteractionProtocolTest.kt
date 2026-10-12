package dev.slne.surf.roleplay.protocol.interaction

import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketDirection
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.ProtocolCodec
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the interaction packets on the wire.
 */
class InteractionProtocolTest {

    /**
     * Encodes and decodes a packet through its channel.
     *
     * @param type the packet type
     * @param packet the packet
     * @return the decoded packet
     */
    private fun <P : Packet> roundTrip(type: PacketType<P>, packet: P): Packet =
        ProtocolCodec.decode(type.channel, ProtocolCodec.encode(type, packet))

    /**
     * Verifies that a target query and an execute survive a round trip.
     */
    @Test
    fun `serverbound packets round-trip`() {
        val query = InteractionTargetQuery(TargetKinds.BLOCK, BlockTargetId.format(10, 64, -3))
        val execute = InteractionExecute(offerId = 41, index = 2)

        assertEquals(query, roundTrip(Packets.INTERACTION_TARGET_QUERY, query))
        assertEquals(execute, roundTrip(Packets.INTERACTION_EXECUTE, execute))
        assertEquals(InteractionRadialRequest, roundTrip(Packets.INTERACTION_RADIAL_REQUEST, InteractionRadialRequest))
    }

    /**
     * Verifies that a target offer with options and an empty offer survive a round trip.
     */
    @Test
    fun `target offers round-trip`() {
        val offer = InteractionTargetOffer(
            offerId = 7,
            kind = TargetKinds.ENTITY,
            id = "4b1a2c3d-0000-4000-8000-000000000001",
            options = listOf(InteractionOption("{\"text\":\"Öffnen\"}", icon = "door-open"), InteractionOption("{\"text\":\"Klopfen\"}")),
        )
        val empty = InteractionTargetOffer(offerId = 0, kind = TargetKinds.BLOCK, id = "1,2,3")

        assertEquals(offer, roundTrip(Packets.INTERACTION_TARGET_OFFER, offer))
        assertEquals(empty, roundTrip(Packets.INTERACTION_TARGET_OFFER, empty))
    }

    /**
     * Verifies that a radial offer keeps the parent links and submenu flags of its nodes.
     */
    @Test
    fun `radial offer round-trips`() {
        val offer = InteractionRadialOffer(
            offerId = 9,
            nodes = listOf(
                RadialNode(parent = -1, label = "{\"text\":\"Ich\"}", submenu = true),
                RadialNode(parent = 0, label = "{\"text\":\"Test\"}", icon = "flask-conical", submenu = true),
                RadialNode(parent = 1, label = "{\"text\":\"Hallo\"}", icon = "hand"),
            ),
        )

        assertEquals(offer, roundTrip(Packets.INTERACTION_RADIAL_OFFER, offer))
    }

    /**
     * Verifies that the availability survives a round trip in both states.
     */
    @Test
    fun `availability round-trips`() {
        assertEquals(InteractionAvailability(true), roundTrip(Packets.INTERACTION_AVAILABILITY, InteractionAvailability(true)))
        assertEquals(InteractionAvailability(false), roundTrip(Packets.INTERACTION_AVAILABILITY, InteractionAvailability(false)))
    }

    /**
     * Verifies the direction of every interaction packet type.
     */
    @Test
    fun `interaction packets have their directions`() {
        assertEquals(PacketDirection.SERVERBOUND, Packets.INTERACTION_TARGET_QUERY.direction)
        assertEquals(PacketDirection.SERVERBOUND, Packets.INTERACTION_RADIAL_REQUEST.direction)
        assertEquals(PacketDirection.SERVERBOUND, Packets.INTERACTION_EXECUTE.direction)
        assertEquals(PacketDirection.CLIENTBOUND, Packets.INTERACTION_TARGET_OFFER.direction)
        assertEquals(PacketDirection.CLIENTBOUND, Packets.INTERACTION_RADIAL_OFFER.direction)
        assertEquals(PacketDirection.CLIENTBOUND, Packets.INTERACTION_AVAILABILITY.direction)
    }
}
