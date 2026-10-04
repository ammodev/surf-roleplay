package dev.slne.surf.roleplay.api.client.common.screen

import java.time.LocalDate
import java.time.YearMonth

/**
 * How many dates a calendar selects.
 */
enum class CalendarMode {
    /**
     * At most one date.
     */
    SINGLE,

    /**
     * Any number of dates.
     */
    MULTIPLE,

    /**
     * A range from a first to a last date.
     */
    RANGE,
}

/**
 * How a calendar shows its month caption.
 */
enum class CaptionLayout {
    /**
     * The month and year as a label between the navigation buttons.
     */
    LABEL,

    /**
     * Lists to pick the month and the year.
     */
    DROPDOWN,
}

/**
 * A month calendar for picking dates, shown in German with weeks starting on Monday. Handlers
 * read the selected dates with [ScreenValues.dates].
 *
 * @property id the id of this element
 * @property mode how many dates the calendar selects
 * @property selected the initially selected dates; for a range its first and last date
 * @property month the initially displayed month, or `null` for the month of the first selected
 *           date or else the current month
 * @property min the first selectable date, or `null` for no limit
 * @property max the last selectable date, or `null` for no limit
 * @property disabled the dates that cannot be selected
 * @property showOutsideDays whether days of the neighbouring months fill the first and last week
 * @property captionLayout how the month caption is shown
 * @property required whether an empty selection is invalid
 * @property enabled whether the player can select dates
 * @property onChange the handler run on every validated change, or `null`
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 * @throws IllegalArgumentException if the limits are reversed, or the selection does not fit the
 *         mode: more than one date for a single calendar, or not two ascending dates for a range
 */
data class CalendarElement(
    override val id: String,
    val mode: CalendarMode = CalendarMode.SINGLE,
    val selected: List<LocalDate> = emptyList(),
    val month: YearMonth? = null,
    val min: LocalDate? = null,
    val max: LocalDate? = null,
    val disabled: Set<LocalDate> = emptySet(),
    val showOutsideDays: Boolean = true,
    val captionLayout: CaptionLayout = CaptionLayout.LABEL,
    val required: Boolean = false,
    val enabled: Boolean = true,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement {
    init {
        require(min == null || max == null || !max.isBefore(min)) { "The calendar limits are reversed: $min to $max" }
        when (mode) {
            CalendarMode.SINGLE -> require(selected.size <= 1) { "A single calendar selects at most one date: $selected" }
            CalendarMode.MULTIPLE -> require(selected.toSet().size == selected.size) { "Selected dates repeat: $selected" }
            CalendarMode.RANGE -> require(selected.isEmpty() || (selected.size == 2 && !selected[1].isBefore(selected[0]))) {
                "A range is its first and last date: $selected"
            }
        }
    }
}
