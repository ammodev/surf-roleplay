package dev.slne.surf.roleplay.protocol.screen

import dev.slne.surf.roleplay.protocol.ConnectionPhase
import dev.slne.surf.roleplay.protocol.PacketDirection
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.ProtocolCodec
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the input change packet and the change flag of input nodes.
 */
class InputChangeProtocolTest {

    /**
     * Verifies that an input change survives a round trip and travels serverbound in the play
     * phase.
     */
    @Test
    fun `input change round-trips`() {
        val packet = ScreenInputChange(4, "search", "Max")

        assertEquals(packet, ProtocolCodec.decode(Packets.SCREEN_INPUT_CHANGE.channel, ProtocolCodec.encode(Packets.SCREEN_INPUT_CHANGE, packet)))
        assertEquals(PacketDirection.SERVERBOUND, Packets.SCREEN_INPUT_CHANGE.direction)
        assertEquals(setOf(ConnectionPhase.PLAY), Packets.SCREEN_INPUT_CHANGE.phases)
    }

    /**
     * Verifies that every input node carries its change flag through a round trip.
     */
    @Test
    fun `input nodes carry the change flag`() {
        val root = ColumnNode(
            "root",
            children = listOf(
                TextInputNode("t", notifyChange = true),
                NumberInputNode("n", notifyChange = true),
                CheckboxNode("c", notifyChange = true),
                SelectNode("d", notifyChange = true),
            ),
        )
        val open = ScreenOpen(sessionId = 1, title = "{}", body = WidgetScreenBody(root))

        val decoded = ProtocolCodec.decode(Packets.SCREEN_OPEN.channel, ProtocolCodec.encode(Packets.SCREEN_OPEN, open)) as ScreenOpen

        assertEquals(root, (decoded.body as WidgetScreenBody).root)
    }
}
