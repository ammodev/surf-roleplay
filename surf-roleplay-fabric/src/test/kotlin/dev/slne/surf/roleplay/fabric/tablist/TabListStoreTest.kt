package dev.slne.surf.roleplay.fabric.tablist

import dev.slne.surf.roleplay.protocol.tablist.TabListState
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests for the client-side store of the last tab list state.
 */
class TabListStoreTest {

    /**
     * The warnings the store under test logged.
     */
    private val warnings = mutableListOf<String>()

    /**
     * The store under test.
     */
    private val store = TabListStore { warnings.add(it) }

    /**
     * Verifies that a state received while the roleplay state is inactive is dropped.
     */
    @Test
    fun `inactive drops packets`() {
        assertFalse(store.receive(TabListState(onlineTotal = 3), active = false, nowNanos = 10))
        assertNull(store.state)
    }

    /**
     * Verifies that the newest state replaces an older one together with its receive time and zone.
     */
    @Test
    fun `newest state wins`() {
        val first = TabListState(onlineTotal = 1)
        val second = TabListState(onlineTotal = 2, zoneId = "UTC")
        assertTrue(store.receive(first, active = true, nowNanos = 10))
        assertTrue(store.receive(second, active = true, nowNanos = 20))
        assertSame(second, store.state)
        assertEquals(20L, store.receivedAtNanos)
        assertEquals(ZoneId.of("UTC"), store.zone)
    }

    /**
     * Verifies that clearing forgets the state and resets the zone.
     */
    @Test
    fun `clear forgets the state`() {
        store.receive(TabListState(onlineTotal = 1, zoneId = "UTC"), active = true, nowNanos = 10)
        store.clear()
        assertNull(store.state)
        assertEquals(TabListStore.DEFAULT_ZONE, store.zone)
    }

    /**
     * Verifies that an invalid zone falls back to the default zone and is logged once per value.
     */
    @Test
    fun `invalid zone is logged once per value`() {
        repeat(3) { store.receive(TabListState(zoneId = "Mars/Olympus"), active = true, nowNanos = 10) }
        store.receive(TabListState(zoneId = "Not/AZone"), active = true, nowNanos = 10)
        store.receive(TabListState(zoneId = "Mars/Olympus"), active = true, nowNanos = 10)
        assertEquals(TabListStore.DEFAULT_ZONE, store.zone)
        assertEquals(2, warnings.size)
    }

    /**
     * Verifies that the logged invalid zones are bounded, so a stream of distinct values cannot
     * grow memory or flood the log.
     */
    @Test
    fun `logged invalid zones are bounded`() {
        repeat(TabListStore.MAX_WARNED_ZONES + 10) { store.receive(TabListState(zoneId = "Bad/Zone$it"), active = true, nowNanos = 10) }
        assertEquals(TabListStore.MAX_WARNED_ZONES, warnings.size)
    }
}
