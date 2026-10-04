package dev.slne.surf.roleplay.protocol.tablist

import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.ProtocolCodec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the tab list packet on the wire.
 */
class TabListProtocolTest {

    /**
     * Encodes and decodes a state through the packet registry.
     */
    private fun roundTrip(state: TabListState): Any =
        ProtocolCodec.decode(Packets.TAB_LIST_STATE.channel, ProtocolCodec.encode(Packets.TAB_LIST_STATE, state))

    /**
     * Verifies that a state with every field set survives a round trip.
     */
    @Test
    fun `full state round-trips`() {
        val state = TabListState(
            organisations = listOf(
                OrganisationCount("police", "\"Polizei\"", "shield", OnlineLevel.FEW),
                OrganisationCount("sar", "\"SAR\"", "life-buoy", OnlineLevel.MANY, exact = 7),
            ),
            onlineTotal = 42,
            serverTimeMillis = 1_700_000_000_000,
            zoneId = "Europe/Berlin",
            restartAtMillis = 1_700_003_600_000,
            weather = "\"Regen\"",
            announcement = "\"Wartung um 5 Uhr\"",
            characterName = "Max",
            job = "Bäcker",
            rank = "Wachtmeister",
            sessionStartMillis = 1_699_999_000_000,
        )

        assertEquals(state, roundTrip(state))
    }

    /**
     * Verifies that the default state survives a round trip.
     */
    @Test
    fun `empty state round-trips`() {
        val state = TabListState()

        assertEquals(state, roundTrip(state))
        assertEquals("Europe/Berlin", state.zoneId)
    }

    /**
     * Verifies that an exact count is kept when present and stays absent otherwise.
     */
    @Test
    fun `exact count is present or absent`() {
        val state = TabListState(
            organisations = listOf(
                OrganisationCount("a", "\"A\"", level = OnlineLevel.FEW),
                OrganisationCount("b", "\"B\"", level = OnlineLevel.MANY, exact = 0),
            ),
        )

        val decoded = roundTrip(state) as TabListState
        assertNull(decoded.organisations[0].exact)
        assertEquals(0, decoded.organisations[1].exact)
    }

    /**
     * Verifies that the packet is registered as clientbound in the play phase.
     */
    @Test
    fun `packet is registered`() {
        assertTrue(Packets.all.contains(Packets.TAB_LIST_STATE))
    }
}
