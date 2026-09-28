package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.CalendarElement
import dev.slne.surf.roleplay.api.client.common.screen.CalendarMode
import dev.slne.surf.roleplay.api.client.common.screen.CaptionLayout
import dev.slne.surf.roleplay.api.client.common.screen.ScreenClick
import dev.slne.surf.roleplay.api.client.common.screen.calendar
import dev.slne.surf.roleplay.api.client.common.screen.screen
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.CalendarNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.InputValue
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import dev.slne.surf.roleplay.protocol.screen.WidgetScreenBody
import net.kyori.adventure.text.Component
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import dev.slne.surf.roleplay.protocol.screen.CalendarMode as NodeCalendarMode
import dev.slne.surf.roleplay.protocol.screen.CaptionLayout as NodeCaptionLayout

/**
 * Tests for calendars in the API and on Paper.
 */
class CalendarTest {

    /**
     * The packets sent to the client.
     */
    private val sent = mutableListOf<Packet>()

    /**
     * The state under test.
     */
    private val state = PlayerScreenState(
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

    /**
     * Returns a date in September 2026.
     *
     * @param day the day of the month
     * @return the date
     */
    private fun sep(day: Int): LocalDate = LocalDate.of(2026, 9, day)

    /**
     * Verifies that a calendar maps to its node with ISO dates and submits its dates.
     */
    @Test
    fun `calendars map and submit ISO dates`() {
        val clicks = mutableListOf<ScreenClick>()
        val session = state.open(
            screen(Component.text("Kalender")) {
                column("root") {
                    calendar(
                        "trip",
                        CalendarMode.RANGE,
                        selected = listOf(sep(1), sep(5)),
                        month = YearMonth.of(2026, 9),
                        min = sep(1),
                        disabled = setOf(sep(20), sep(10)),
                        captionLayout = CaptionLayout.DROPDOWN,
                    )
                    button("submit", Component.text("Senden")) { clicks += it }
                }
            },
            null,
        ).sessionId

        val node = assertIs<CalendarNode>(assertIs<ColumnNode>(assertIs<WidgetScreenBody>((sent.last() as ScreenOpen).body).root).children[0])
        assertEquals(NodeCalendarMode.RANGE, node.mode)
        assertEquals("2026-09-01/2026-09-05", node.value)
        assertEquals("2026-09", node.month)
        assertEquals("2026-09-01", node.min)
        assertEquals(listOf("2026-09-10", "2026-09-20"), node.disabled)
        assertEquals(NodeCaptionLayout.DROPDOWN, node.captionLayout)

        val outcome = state.handleWidgetAction(ScreenWidgetAction(session, "submit", listOf(InputValue("trip", "2026-09-02/2026-09-08"))))
        assertIs<PlayerScreenState.Outcome.Accepted>(outcome)
        assertEquals(listOf(sep(2), sep(8)), clicks.single().values.dates("trip"))
    }

    /**
     * Verifies the calendar rules: the mode's form, the limits, disabled dates and required.
     */
    @Test
    fun `calendars validate their dates`() {
        val single = CalendarElement("c", min = sep(1), max = sep(30), disabled = setOf(sep(15)))
        val rule = ElementRules.input(single)!!

        assertNull(rule.violation(single, "2026-09-14"))
        assertNull(rule.violation(single, ""))
        assertNotNull(rule.violation(single, "2026-10-01"))
        assertNotNull(rule.violation(single, "2026-09-15"))
        assertNotNull(rule.violation(single, "14.09.2026"))
        assertNotNull(rule.violation(single, "2026-09-14,2026-09-16"))
        assertNotNull(rule.violation(single.copy(required = true), ""))
        val multiple = single.copy(mode = CalendarMode.MULTIPLE)
        assertNull(rule.violation(multiple, "2026-09-16,2026-09-14"))
        assertEquals("2026-09-14,2026-09-16", rule.current(rule.withValue(multiple, "2026-09-16,2026-09-14")!!))
        val range = single.copy(mode = CalendarMode.RANGE)
        assertNull(rule.violation(range, "2026-09-14/2026-09-16"))
        assertNotNull(rule.violation(range, "2026-09-16/2026-09-14"))
    }

    /**
     * Verifies the constraints the API checks when calendars are created.
     */
    @Test
    fun `calendars check their settings`() {
        assertFailsWith<IllegalArgumentException> { CalendarElement("c", min = sep(10), max = sep(1)) }
        assertFailsWith<IllegalArgumentException> { CalendarElement("c", selected = listOf(sep(1), sep(2))) }
        assertFailsWith<IllegalArgumentException> { CalendarElement("c", CalendarMode.RANGE, selected = listOf(sep(1))) }
        assertFailsWith<IllegalArgumentException> { CalendarElement("c", CalendarMode.RANGE, selected = listOf(sep(5), sep(1))) }
    }
}
