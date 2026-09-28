package dev.slne.surf.roleplay.paper.screen.debug

import dev.slne.surf.roleplay.api.client.common.screen.ScreenThemes
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.paper.screen.ActionRateLimiter
import dev.slne.surf.roleplay.paper.screen.PlayerScreenState
import dev.slne.surf.roleplay.paper.screen.ScreenPacketSender
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertIs

/**
 * Tests for the inputs page of the screen debug command.
 */
class InputsDemoTest {

    /**
     * Verifies that the page builds with unique ids and valid settings and maps to a screen open.
     */
    @Test
    fun `the inputs page builds and maps`() {
        val sent = mutableListOf<Packet>()
        val state = PlayerScreenState(
            UUID.randomUUID(),
            object : ScreenPacketSender {
                /**
                 * Records a packet.
                 *
                 * @param type the packet type
                 * @param packet the packet
                 */
                override fun <P : Packet> send(type: PacketType<P>, packet: P) {
                    sent += packet
                }
            },
            ActionRateLimiter(100),
        )

        state.open(InputsDemo.definition({ }, ScreenThemes.DEFAULT, ScreenVariant.DARK), null)

        assertIs<ScreenOpen>(sent.single())
    }
}
