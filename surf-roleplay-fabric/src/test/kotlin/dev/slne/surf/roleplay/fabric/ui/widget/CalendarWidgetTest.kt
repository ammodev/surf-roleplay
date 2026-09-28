package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.protocol.screen.CalendarMode
import dev.slne.surf.roleplay.protocol.screen.CaptionLayout
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for the German month grid, calendar selection and keyboard navigation in the mod.
 */
class CalendarWidgetTest {

    /**
     * A context that ignores everything.
     */
    private val context = object : UiContext {
        override val focusedWidget: Widget? = null
        override fun focus(widget: Widget?) = Unit
        override fun requestLayout() = Unit
        override fun actionTriggered(widget: Widget, submitsInput: Boolean) = Unit
        override var clipboard: String = ""
    }

    /**
     * Returns a date in 2026.
     *
     * @param month the month
     * @param day the day of the month
     * @return the date
     */
    private fun date(month: Int, day: Int): LocalDate = LocalDate.of(2026, month, day)

    /**
     * Creates a calendar showing September 2026.
     *
     * @param mode how many dates the calendar selects
     * @param min the first selectable date, or `null`
     * @param max the last selectable date, or `null`
     * @param disabled the dates that cannot be selected
     * @return the calendar
     */
    private fun calendar(mode: CalendarMode = CalendarMode.SINGLE, min: LocalDate? = null, max: LocalDate? = null, disabled: Set<LocalDate> = emptySet()) =
        CalendarWidget("c", mode, emptyList(), YearMonth.of(2026, 9), min, max, disabled, true, CaptionLayout.LABEL, false, today = date(9, 28))

    /**
     * Returns a key event for a key without modifiers.
     *
     * @param key the GLFW key code
     * @return the event
     */
    private fun key(key: Int) = KeyEvent(key, 0, 0)

    /**
     * Verifies that weeks start on Monday, include the neighbouring months' days, and handle leap
     * years.
     */
    @Test
    fun `month grids start on Monday`() {
        val september = CalendarGrid.weeks(YearMonth.of(2026, 9))
        assertEquals(5, september.size)
        assertEquals(date(8, 31), september.first().first())
        assertEquals(date(10, 4), september.last().last())

        val february = CalendarGrid.weeks(YearMonth.of(2024, 2))
        assertTrue(february.flatten().contains(LocalDate.of(2024, 2, 29)))
        assertEquals(LocalDate.of(2024, 1, 29), february.first().first())
        assertTrue(CalendarGrid.weeks(YearMonth.of(2027, 2)).all { it.size == 7 })
        assertEquals("September 2026", CalendarGrid.caption(YearMonth.of(2026, 9)))
        assertEquals("März", CalendarGrid.MONTHS[2])
    }

    /**
     * Verifies the selection rules of the three modes, including the order of a range picked
     * backwards.
     */
    @Test
    fun `selection follows the mode`() {
        assertEquals(listOf(date(9, 2)), CalendarGrid.select(CalendarMode.SINGLE, listOf(date(9, 1)), date(9, 2)))
        assertEquals(emptyList(), CalendarGrid.select(CalendarMode.SINGLE, listOf(date(9, 2)), date(9, 2)))
        assertEquals(listOf(date(9, 1), date(9, 3)), CalendarGrid.select(CalendarMode.MULTIPLE, listOf(date(9, 3)), date(9, 1)))
        assertEquals(listOf(date(9, 3)), CalendarGrid.select(CalendarMode.MULTIPLE, listOf(date(9, 1), date(9, 3)), date(9, 1)))

        val start = CalendarGrid.select(CalendarMode.RANGE, emptyList(), date(9, 10))
        assertEquals(listOf(date(9, 10), date(9, 10)), start)
        assertEquals(listOf(date(9, 4), date(9, 10)), CalendarGrid.select(CalendarMode.RANGE, start, date(9, 4)))
        assertEquals(listOf(date(9, 20), date(9, 20)), CalendarGrid.select(CalendarMode.RANGE, listOf(date(9, 4), date(9, 10)), date(9, 20)))
    }

    /**
     * Verifies that clicks outside the limits or on disabled dates select nothing, and that a
     * range calendar reports its value as two ISO dates.
     */
    @Test
    fun `limits and disabled dates block selection`() {
        val range = calendar(CalendarMode.RANGE, min = date(9, 5), max = date(9, 25), disabled = setOf(date(9, 12)))

        range.click(date(9, 3), context)
        range.click(date(9, 12), context)
        assertEquals("", range.inputValue)
        range.click(date(9, 20), context)
        range.click(date(9, 8), context)
        assertEquals("2026-09-08/2026-09-20", range.inputValue)
    }

    /**
     * Verifies that the arrow keys move the focused day across months, that Page Down changes the
     * month, and that Enter selects the focused day.
     */
    @Test
    fun `keyboard moves the focused day`() {
        val calendar = calendar()

        assertEquals(date(9, 28), calendar.focusedDay)
        calendar.keyPressed(context, key(GLFW.GLFW_KEY_DOWN))
        assertEquals(date(10, 5), calendar.focusedDay)
        assertEquals(YearMonth.of(2026, 10), calendar.month)
        calendar.keyPressed(context, key(GLFW.GLFW_KEY_HOME))
        assertEquals(date(10, 5), calendar.focusedDay)
        calendar.keyPressed(context, key(GLFW.GLFW_KEY_END))
        assertEquals(date(10, 11), calendar.focusedDay)
        calendar.keyPressed(context, key(GLFW.GLFW_KEY_PAGE_UP))
        assertEquals(date(9, 11), calendar.focusedDay)
        calendar.keyPressed(context, key(GLFW.GLFW_KEY_ENTER))
        assertEquals("2026-09-11", calendar.inputValue)
    }

    /**
     * Verifies that the focused day stays within the limits.
     */
    @Test
    fun `keyboard stays within the limits`() {
        val calendar = calendar(min = date(9, 20), max = date(9, 30))

        calendar.keyPressed(context, key(GLFW.GLFW_KEY_PAGE_DOWN))
        assertEquals(date(9, 30), calendar.focusedDay)
        calendar.keyPressed(context, key(GLFW.GLFW_KEY_PAGE_UP))
        assertEquals(date(9, 20), calendar.focusedDay)
    }
}
