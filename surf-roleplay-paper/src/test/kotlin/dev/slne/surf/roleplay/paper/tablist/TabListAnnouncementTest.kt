package dev.slne.surf.roleplay.paper.tablist

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * Tests for updating the announcement of the tab list settings.
 */
class TabListAnnouncementTest {

    /** Settings with a custom restart time and no announcement. */
    private val base = TabListConfig(
        organisations = mapOf("police" to TabListConfig.Organisation(exact = true)),
        restartTime = java.time.LocalTime.of(4, 30),
    )

    /**
     * Verifies that setting an announcement keeps all other settings.
     */
    @Test
    fun `set stores the text and keeps the other settings`() {
        val result = TabListAnnouncement.set(base, "Wartung um 20 Uhr")

        val updated = assertIs<TabListAnnouncement.Result.Updated>(result)
        assertEquals("Wartung um 20 Uhr", updated.config.announcement)
        assertEquals(base.organisations, updated.config.organisations)
        assertEquals(base.restartTime, updated.config.restartTime)
    }

    /**
     * Verifies that clearing removes the announcement.
     */
    @Test
    fun `clear removes the announcement`() {
        val result = TabListAnnouncement.set(base.copy(announcement = "alt"), null)

        assertNull(assertIs<TabListAnnouncement.Result.Updated>(result).config.announcement)
    }

    /**
     * Verifies the length cap: 120 characters pass, 121 are rejected.
     */
    @Test
    fun `text is capped at 120 characters`() {
        assertIs<TabListAnnouncement.Result.Updated>(TabListAnnouncement.set(base, "a".repeat(120)))
        assertIs<TabListAnnouncement.Result.TooLong>(TabListAnnouncement.set(base, "a".repeat(121)))
    }

    /**
     * Verifies that a blank text is treated as clearing.
     */
    @Test
    fun `blank text clears the announcement`() {
        val result = TabListAnnouncement.set(base.copy(announcement = "alt"), "   ")

        assertNull(assertIs<TabListAnnouncement.Result.Updated>(result).config.announcement)
    }
}
