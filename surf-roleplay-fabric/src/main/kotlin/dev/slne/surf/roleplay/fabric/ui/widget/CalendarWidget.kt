package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.UiMetrics
import dev.slne.surf.roleplay.protocol.screen.CalendarMode
import dev.slne.surf.roleplay.protocol.screen.CalendarValues
import dev.slne.surf.roleplay.protocol.screen.CaptionLayout
import dev.slne.surf.roleplay.protocol.screen.SelectGroup
import dev.slne.surf.roleplay.protocol.screen.SelectOption
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

/**
 * The German month grid and the selection rules of calendars.
 */
object CalendarGrid {

    /**
     * The German month names, January first.
     */
    val MONTHS: List<String> = listOf(
        "Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember",
    )

    /**
     * The German two-letter weekday names, Monday first.
     */
    val WEEKDAYS: List<String> = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")

    /**
     * Returns the weeks that show a month, each from Monday to Sunday, including the days of the
     * neighbouring months that fill the first and last week.
     *
     * @param month the month
     * @return the weeks, four to six
     */
    fun weeks(month: YearMonth): List<List<LocalDate>> {
        val first = month.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val last = month.atEndOfMonth().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        return generateSequence(first) { it.plusDays(1) }.takeWhile { !it.isAfter(last) }.toList().chunked(7)
    }

    /**
     * Returns the German caption of a month.
     *
     * @param month the month
     * @return the month name and the year, such as `September 2026`
     */
    fun caption(month: YearMonth): String = "${MONTHS[month.monthValue - 1]} ${month.year}"

    /**
     * Applies a click on a date to a selection.
     *
     * A single calendar selects the date, or clears it when it is clicked again. A multiple
     * calendar adds or removes the date. A range calendar starts a one-day range, extends a one-day
     * range to the clicked date in either direction, and starts over once a range spans several
     * days.
     *
     * @param mode the calendar's mode
     * @param current the selected dates; a range as its first and last date
     * @param date the clicked date
     * @return the new selection
     */
    fun select(mode: CalendarMode, current: List<LocalDate>, date: LocalDate): List<LocalDate> = when (mode) {
        CalendarMode.SINGLE -> if (current == listOf(date)) emptyList() else listOf(date)
        CalendarMode.MULTIPLE -> (if (date in current) current - date else current + date).sorted()
        CalendarMode.RANGE -> {
            val from = current.firstOrNull()
            val to = current.lastOrNull()
            when {
                from == null || to == null || from != to -> listOf(date, date)
                date.isBefore(from) -> listOf(date, from)
                else -> listOf(from, date)
            }
        }
    }
}

/**
 * A month calendar, in German with weeks starting on Monday. The arrow keys move a focused day,
 * crossing into the neighbouring months, Page Up and Page Down change the month, or the year
 * with Shift, Home and End jump to the start and end of the week, and Enter or Space select the
 * focused day.
 *
 * @param id the id of the widget
 * @property mode how many dates the calendar selects
 * @param selected the selected dates; a range as its first and last date
 * @param month the displayed month, or `null` for the month of the first selected date or else the
 *        current month
 * @property min the first selectable date, or `null` for no limit
 * @property max the last selectable date, or `null` for no limit
 * @property disabled the dates that cannot be selected
 * @property showOutsideDays whether days of the neighbouring months fill the first and last week
 * @property captionLayout how the month caption is shown
 * @property required whether an empty selection is invalid
 * @property today the current date, which is marked
 */
class CalendarWidget(
    id: String,
    val mode: CalendarMode,
    selected: List<LocalDate>,
    month: YearMonth?,
    val min: LocalDate?,
    val max: LocalDate?,
    val disabled: Set<LocalDate>,
    val showOutsideDays: Boolean,
    val captionLayout: CaptionLayout,
    val required: Boolean,
    val today: LocalDate = LocalDate.now(),
) : Widget(id) {

    /**
     * The selected dates; a range as its first and last date.
     */
    var selected: List<LocalDate> = selected
        private set

    /**
     * The displayed month.
     */
    var month: YearMonth = month ?: selected.firstOrNull()?.let(YearMonth::from) ?: YearMonth.from(today)
        private set

    /**
     * The day the keyboard acts on.
     */
    var focusedDay: LocalDate = selected.firstOrNull() ?: today.takeIf { YearMonth.from(it) == this.month } ?: this.month.atDay(1)
        private set

    /**
     * Whether the calendar can take the keyboard focus, which it can while enabled.
     */
    override val focusable: Boolean get() = enabled

    /**
     * The calendar draws its own ring around the focused day.
     */
    override val drawsOwnFocus: Boolean get() = true

    /**
     * The selection in the calendar's value form.
     */
    override val inputValue: String get() = CalendarValues.format(mode, selected)

    /**
     * Whether the calendar shows itself as invalid: once touched while required and empty.
     */
    override val showsInvalid: Boolean get() = touched && required && selected.isEmpty()

    /**
     * Returns the size of the caption, the weekday row and the weeks of the displayed month.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size =
        Size(7 * CELL + 2 * PADDING, 2 * PADDING + CAPTION + WEEKDAYS_HEIGHT + CalendarGrid.weeks(month).size * CELL)

    /**
     * Checks whether a date can be selected.
     *
     * @param date the date
     * @return whether it lies within the limits and is not disabled
     */
    fun isSelectable(date: LocalDate): Boolean =
        date !in disabled && (min == null || !date.isBefore(min)) && (max == null || !date.isAfter(max))

    /**
     * Checks whether a month can be displayed.
     *
     * @param target the month
     * @return whether it contains a date within the limits
     */
    private fun canShow(target: YearMonth): Boolean =
        (min == null || !target.isBefore(YearMonth.from(min))) && (max == null || !target.isAfter(YearMonth.from(max)))

    /**
     * Displays another month if it lies within the limits, and asks for a new layout since the
     * number of weeks can change.
     *
     * @param target the month
     * @param context the screen showing the widget
     */
    fun showMonth(target: YearMonth, context: UiContext) {
        if (!canShow(target) || target == month) return
        month = target
        if (YearMonth.from(focusedDay) != target) focusedDay = target.atDay(focusedDay.dayOfMonth.coerceAtMost(target.lengthOfMonth()))
        context.requestLayout()
    }

    /**
     * Selects or deselects a date by the calendar's mode, if it can be selected, and shows its
     * month.
     *
     * @param date the date
     * @param context the screen showing the widget
     */
    fun click(date: LocalDate, context: UiContext) {
        if (!enabled || !isSelectable(date)) return
        focusedDay = date
        showMonth(YearMonth.from(date), context)
        selected = CalendarGrid.select(mode, selected, date)
        markChanged(context, immediate = true)
    }

    /**
     * Moves the focused day, showing its month when it leaves the displayed one. The day stays
     * within the limits.
     *
     * @param target the new focused day
     * @param context the screen showing the widget
     */
    private fun moveFocus(target: LocalDate, context: UiContext) {
        var day = target
        if (min != null && day.isBefore(min)) day = min
        if (max != null && day.isAfter(max)) day = max
        focusedDay = day
        showMonth(YearMonth.from(day), context)
    }

    /**
     * The area of the previous-month button.
     */
    private val previousButton: Rect get() = Rect(bounds.x + PADDING, bounds.y + PADDING, CAPTION, CAPTION)

    /**
     * The area of the next-month button.
     */
    private val nextButton: Rect get() = Rect(bounds.right - PADDING - CAPTION, bounds.y + PADDING, CAPTION, CAPTION)

    /**
     * The area of the month picker of a dropdown caption.
     */
    val monthPicker: Rect get() = Rect(bounds.x + PADDING + CAPTION + 2, bounds.y + PADDING, MONTH_PICKER, CAPTION)

    /**
     * The area of the year picker of a dropdown caption.
     */
    val yearPicker: Rect get() = Rect(monthPicker.right + 2, bounds.y + PADDING, YEAR_PICKER, CAPTION)

    /**
     * Returns the area of a day cell.
     *
     * @param week the week row
     * @param weekday the weekday column, Monday first
     * @return the cell's area
     */
    private fun cell(week: Int, weekday: Int): Rect =
        Rect(bounds.x + PADDING + weekday * CELL, bounds.y + PADDING + CAPTION + WEEKDAYS_HEIGHT + week * CELL, CELL, CELL)

    /**
     * Draws the frame, the caption with its navigation, the weekday names and the days: the
     * selection in the primary colour, range days between in the accent colour, today marked,
     * outside and disabled days muted, and a ring around the focused day while focused.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val tokens = ui.tokens
        val fade: (Int) -> Int = { if (enabled) it else ui.disabled(it) }
        ui.fillRounded(bounds, tokens.background)
        ui.borderRounded(bounds, if (showsInvalid) tokens.destructive else tokens.border)

        val side = UiMetrics.INLINE_ICON
        listOf(previousButton to month.minusMonths(1), nextButton to month.plusMonths(1)).forEachIndexed { index, (button, target) ->
            val usable = enabled && canShow(target)
            if (usable && button.contains(mouseX.toDouble(), mouseY.toDouble())) ui.fillRounded(button, tokens.accent)
            val icon = if (index == 0) "chevron-left" else "chevron-right"
            ui.icon(icon, Rect(button.x + (button.width - side) / 2, button.y + (button.height - side) / 2, side, side), if (usable) tokens.foreground else ui.disabled(tokens.mutedForeground))
        }
        val captionY = bounds.y + PADDING + (CAPTION - ui.lineHeight + 1) / 2
        if (captionLayout == CaptionLayout.LABEL) {
            val caption = CalendarGrid.caption(month)
            ui.plainText(caption, bounds.x + (bounds.width - ui.plainWidth(caption)) / 2, captionY, fade(tokens.foreground))
        } else {
            listOf(monthPicker to CalendarGrid.MONTHS[month.monthValue - 1].take(3), yearPicker to month.year.toString()).forEach { (picker, text) ->
                ui.borderRounded(picker, fade(tokens.input))
                ui.plainText(text, picker.x + 3, captionY, fade(tokens.foreground))
                val chevron = 7
                ui.icon("chevron-down", Rect(picker.right - chevron - 2, picker.y + (picker.height - chevron) / 2, chevron, chevron), fade(tokens.mutedForeground))
            }
        }

        val weekdaysY = bounds.y + PADDING + CAPTION + (WEEKDAYS_HEIGHT - ui.lineHeight + 1) / 2
        CalendarGrid.WEEKDAYS.forEachIndexed { index, name ->
            val x = bounds.x + PADDING + index * CELL + (CELL - ui.plainWidth(name)) / 2
            ui.plainText(name, x, weekdaysY, fade(tokens.mutedForeground))
        }

        val rangeFrom = selected.firstOrNull().takeIf { mode == CalendarMode.RANGE }
        val rangeTo = selected.lastOrNull().takeIf { mode == CalendarMode.RANGE }
        val focused = context.focusedWidget === this
        CalendarGrid.weeks(month).forEachIndexed { week, days ->
            days.forEachIndexed { weekday, day ->
                val outside = YearMonth.from(day) != month
                if (outside && !showOutsideDays) return@forEachIndexed
                val area = cell(week, weekday)
                val selectable = enabled && isSelectable(day)
                val isSelected = day in selected
                val inRange = rangeFrom != null && rangeTo != null && day.isAfter(rangeFrom) && day.isBefore(rangeTo)
                val hovered = selectable && area.contains(mouseX.toDouble(), mouseY.toDouble())
                when {
                    inRange -> ui.fill(area, tokens.accent)
                    isSelected -> ui.fillRounded(area, fade(tokens.primary))
                    hovered -> ui.fillRounded(area, tokens.accent)
                    day == today -> ui.fillRounded(area, tokens.muted)
                }
                val color = when {
                    isSelected && !inRange -> tokens.primaryForeground
                    outside -> tokens.mutedForeground
                    inRange || hovered -> tokens.accentForeground
                    else -> tokens.foreground
                }
                val text = day.dayOfMonth.toString()
                ui.plainText(text, area.x + (area.width - ui.plainWidth(text) + 1) / 2, area.y + (area.height - ui.lineHeight + 1) / 2, if (selectable) color else ui.disabled(color))
                if (focused && day == focusedDay) ui.borderRounded(area, tokens.ring)
            }
        }
    }

    /**
     * Handles a click on the navigation buttons, the caption pickers or a day.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on this calendar
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (!isOver(x, y)) return false
        if (!enabled || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return true
        context.focus(this)
        when {
            previousButton.contains(x, y) -> showMonth(month.minusMonths(1), context)
            nextButton.contains(x, y) -> showMonth(month.plusMonths(1), context)
            captionLayout == CaptionLayout.DROPDOWN && monthPicker.contains(x, y) -> context.openPopover(CaptionPopover(this, years = false))
            captionLayout == CaptionLayout.DROPDOWN && yearPicker.contains(x, y) -> context.openPopover(CaptionPopover(this, years = true))
            else -> CalendarGrid.weeks(month).forEachIndexed { week, days ->
                days.forEachIndexed { weekday, day ->
                    val visible = showOutsideDays || YearMonth.from(day) == month
                    if (visible && cell(week, weekday).contains(x, y)) click(day, context)
                }
            }
        }
        return true
    }

    /**
     * Moves the focused day and the month with the keyboard, and selects the focused day.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        if (!enabled) return false
        when (event.key()) {
            GLFW.GLFW_KEY_LEFT -> moveFocus(focusedDay.minusDays(1), context)
            GLFW.GLFW_KEY_RIGHT -> moveFocus(focusedDay.plusDays(1), context)
            GLFW.GLFW_KEY_UP -> moveFocus(focusedDay.minusWeeks(1), context)
            GLFW.GLFW_KEY_DOWN -> moveFocus(focusedDay.plusWeeks(1), context)
            GLFW.GLFW_KEY_PAGE_UP -> moveFocus(if (event.hasShiftDown()) focusedDay.minusYears(1) else focusedDay.minusMonths(1), context)
            GLFW.GLFW_KEY_PAGE_DOWN -> moveFocus(if (event.hasShiftDown()) focusedDay.plusYears(1) else focusedDay.plusMonths(1), context)
            GLFW.GLFW_KEY_HOME -> moveFocus(focusedDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), context)
            GLFW.GLFW_KEY_END -> moveFocus(focusedDay.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)), context)
            else -> if (isActivation(event)) click(focusedDay, context) else return false
        }
        return true
    }

    /**
     * Sets the selection from the calendar's value form, ignoring values of another form.
     *
     * @param value the value
     */
    override fun applyValue(value: String) {
        selected = CalendarValues.parse(mode, value) ?: return
    }

    /**
     * Holds the calendar sizes.
     */
    companion object {
        /**
         * The side length of a day cell.
         */
        const val CELL: Int = 16

        /**
         * The space inside the calendar's edges.
         */
        const val PADDING: Int = 4

        /**
         * The height of the caption row.
         */
        const val CAPTION: Int = 16

        /**
         * The height of the weekday row.
         */
        const val WEEKDAYS_HEIGHT: Int = 12

        /**
         * The width of the month picker of a dropdown caption.
         */
        const val MONTH_PICKER: Int = 34

        /**
         * The width of the year picker of a dropdown caption.
         */
        const val YEAR_PICKER: Int = 36

        /**
         * How many years before and after the displayed year the year picker offers when the
         * calendar has no limit on that side.
         */
        const val YEAR_SPAN: Int = 10
    }
}

/**
 * The list of months or years a calendar's dropdown caption opens.
 *
 * @property owner the calendar that opened the list
 * @property years whether the list offers years instead of months
 */
class CaptionPopover(override val owner: CalendarWidget, private val years: Boolean) : Popover {

    /**
     * The months or years, with the displayed one highlighted.
     */
    private val list: OptionList = OptionList(
        listOf(
            SelectGroup(
                options = if (years) {
                    val first = owner.min?.year ?: (owner.month.year - CalendarWidget.YEAR_SPAN)
                    val last = owner.max?.year ?: (owner.month.year + CalendarWidget.YEAR_SPAN)
                    (first..last).map { SelectOption(it.toString(), "\"$it\"") }
                } else {
                    CalendarGrid.MONTHS.mapIndexed { index, name -> SelectOption((index + 1).toString(), "\"$name\"") }
                },
            ),
        ),
    ).also { it.highlightValue(if (years) owner.month.year.toString() else owner.month.monthValue.toString()) }

    /**
     * Places the list below the picker that opened it.
     *
     * @param window the window area
     * @param measurer the text measurer
     * @return the list's area
     */
    override fun area(window: Rect, measurer: TextMeasurer): Rect {
        val anchor = if (years) owner.yearPicker else owner.monthPicker
        val height = list.contentHeight.coerceAtMost(OptionList.MAX_HEIGHT)
        list.revealHighlight(height)
        return Popovers.below(anchor, maxOf(anchor.width, list.preferredWidth(measurer)), height, window)
    }

    /**
     * Draws the list with a check mark on the displayed month or year.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the popover
     * @param area the list's area
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, area: Rect, mouseX: Int, mouseY: Int) {
        drawPopoverFrame(ui, area)
        list.render(ui, area, listOf(if (years) owner.month.year.toString() else owner.month.monthValue.toString()), mouseX, mouseY, null)
    }

    /**
     * Shows the clicked month or year and closes the list.
     *
     * @param context the screen showing the popover
     * @param area the list's area
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     */
    override fun mouseClicked(context: UiContext, area: Rect, x: Double, y: Double, button: Int) {
        val row = list.rows.getOrNull(list.rowAt(area, y)) as? OptionList.Item ?: return
        choose(row.option.value, context)
    }

    /**
     * Shows a month or year of the list and closes it.
     *
     * @param value the month number or the year
     * @param context the screen showing the popover
     */
    private fun choose(value: String, context: UiContext) {
        val number = value.toInt()
        owner.showMonth(if (years) owner.month.withYear(number) else owner.month.withMonth(number), context)
        context.closePopover()
    }

    /**
     * Scrolls the list.
     *
     * @param area the list's area
     * @param amount the scroll amount; positive scrolls up
     */
    override fun mouseScrolled(area: Rect, amount: Double) {
        list.scrollBy(-(amount * UiMetrics.SCROLL_STEP).toInt(), area.height)
    }

    /**
     * Moves the highlight with Up and Down and shows the highlighted entry on Enter or Space.
     *
     * @param context the screen showing the popover
     * @param event the key event
     * @return `false` for Escape and Tab, `true` for every other key
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        when (event.key()) {
            GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_TAB -> return false
            GLFW.GLFW_KEY_UP -> list.moveHighlight(-1)
            GLFW.GLFW_KEY_DOWN -> list.moveHighlight(1)
            else -> if (isActivation(event)) list.highlightedOption?.let { choose(it.value, context) }
        }
        list.revealHighlight(OptionList.MAX_HEIGHT.coerceAtMost(list.contentHeight))
        return true
    }
}
