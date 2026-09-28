package dev.slne.surf.roleplay.protocol.screen

import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.ProtocolCodec
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for the calendar node on the wire and the calendar value form.
 */
class CalendarProtocolTest {

    /**
     * Verifies that a calendar survives a round trip with all its settings.
     */
    @Test
    fun `calendars round-trip`() {
        val calendar = CalendarNode(
            "c",
            mode = CalendarMode.RANGE,
            value = "2026-09-01/2026-09-05",
            month = "2026-09",
            min = "2026-01-01",
            max = "2026-12-31",
            disabled = listOf("2026-09-03"),
            showOutsideDays = false,
            captionLayout = CaptionLayout.DROPDOWN,
            required = true,
            enabled = false,
            notifyChange = true,
        )
        val open = ScreenOpen(sessionId = 1, title = "{}", body = WidgetScreenBody(calendar))

        val decoded = ProtocolCodec.decode(Packets.SCREEN_OPEN.channel, ProtocolCodec.encode(Packets.SCREEN_OPEN, open)) as ScreenOpen

        assertEquals(calendar, (decoded.body as WidgetScreenBody).root)
    }

    /**
     * Verifies that values parse by mode and format back.
     */
    @Test
    fun `values parse by mode`() {
        val first = LocalDate.of(2026, 9, 1)
        val fifth = LocalDate.of(2026, 9, 5)

        assertEquals(listOf(first), CalendarValues.parse(CalendarMode.SINGLE, "2026-09-01"))
        assertNull(CalendarValues.parse(CalendarMode.SINGLE, "2026-09-01,2026-09-05"))
        assertEquals(listOf(first, fifth), CalendarValues.parse(CalendarMode.MULTIPLE, "2026-09-05,2026-09-01"))
        assertNull(CalendarValues.parse(CalendarMode.MULTIPLE, "2026-09-01,2026-09-01"))
        assertEquals(listOf(first, fifth), CalendarValues.parse(CalendarMode.RANGE, "2026-09-01/2026-09-05"))
        assertNull(CalendarValues.parse(CalendarMode.RANGE, "2026-09-05/2026-09-01"))
        assertNull(CalendarValues.parse(CalendarMode.RANGE, "2026-09-01"))
        assertNull(CalendarValues.parse(CalendarMode.SINGLE, "2026-02-30"))
        assertEquals(emptyList(), CalendarValues.parse(CalendarMode.RANGE, ""))
        assertEquals("2026-09-01/2026-09-05", CalendarValues.format(CalendarMode.RANGE, listOf(first, fifth)))
        assertEquals("2026-09-01,2026-09-05", CalendarValues.format(CalendarMode.MULTIPLE, listOf(fifth, first)))
    }
}
