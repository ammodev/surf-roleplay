package dev.slne.surf.roleplay.paper.tablist

import dev.slne.surf.roleplay.protocol.tablist.TabListState
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for remembering the last sent tab list states.
 */
class TabListSentStatesTest {

    /**
     * The first player.
     */
    private val player = UUID.randomUUID()

    /**
     * The sent states under test.
     */
    private val sent = TabListSentStates()

    /**
     * Verifies that nothing is unchanged before a state was recorded.
     */
    @Test
    fun `nothing sent yet is changed`() {
        assertFalse(sent.isUnchanged(player, TabListState()))
    }

    /**
     * Verifies that the same state is unchanged even when only the server time differs.
     */
    @Test
    fun `same state with another server time is unchanged`() {
        sent.record(player, TabListState(onlineTotal = 3, serverTimeMillis = 1000))
        assertTrue(sent.isUnchanged(player, TabListState(onlineTotal = 3, serverTimeMillis = 9000)))
    }

    /**
     * Verifies that a different state is changed.
     */
    @Test
    fun `different state is changed`() {
        sent.record(player, TabListState(onlineTotal = 3))
        assertFalse(sent.isUnchanged(player, TabListState(onlineTotal = 4)))
    }

    /**
     * Verifies that players are tracked independently.
     */
    @Test
    fun `players are independent`() {
        sent.record(player, TabListState(onlineTotal = 3))
        assertFalse(sent.isUnchanged(UUID.randomUUID(), TabListState(onlineTotal = 3)))
    }

    /**
     * Verifies that a forgotten player gets the next state in any case.
     */
    @Test
    fun `forgotten player is changed`() {
        sent.record(player, TabListState(onlineTotal = 3))
        sent.forget(player)
        assertFalse(sent.isUnchanged(player, TabListState(onlineTotal = 3)))
    }
}
