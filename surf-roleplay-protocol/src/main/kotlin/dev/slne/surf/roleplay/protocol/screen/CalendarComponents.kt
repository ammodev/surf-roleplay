package dev.slne.surf.roleplay.protocol.screen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeParseException

/**
 * How many dates a calendar selects.
 */
@Serializable
enum class CalendarMode {
    /**
     * At most one date.
     */
    @ProtoNumber(0)
    SINGLE,

    /**
     * Any number of dates.
     */
    @ProtoNumber(1)
    MULTIPLE,

    /**
     * A range from a first to a last date.
     */
    @ProtoNumber(2)
    RANGE,
}

/**
 * How a calendar shows its month caption.
 */
@Serializable
enum class CaptionLayout {
    /**
     * The month and year as a label between the navigation buttons.
     */
    @ProtoNumber(0)
    LABEL,

    /**
     * Lists to pick the month and the year.
     */
    @ProtoNumber(1)
    DROPDOWN,
}

/**
 * A month calendar for picking dates. Its value is empty, one ISO date for a single calendar,
 * ISO dates joined by commas in ascending order for a multiple calendar, or two ISO dates joined
 * by `/` for a range.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property mode how many dates the calendar selects
 * @property value the selected dates in the calendar's value form
 * @property month the displayed month as `yyyy-MM`, or `null` for the month of the first selected
 *           date or else the current month
 * @property min the first selectable date as an ISO date, or `null` for no limit
 * @property max the last selectable date as an ISO date, or `null` for no limit
 * @property disabled the dates that cannot be selected, as ISO dates
 * @property showOutsideDays whether days of the neighbouring months fill the first and last week
 * @property captionLayout how the month caption is shown
 * @property required whether an empty selection is invalid
 * @property enabled whether the player can select dates
 * @property notifyChange whether the mod reports every change of the value at once
 */
@Serializable
@SerialName("calendar")
data class CalendarNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val mode: CalendarMode = CalendarMode.SINGLE,
    @ProtoNumber(5) val value: String = "",
    @ProtoNumber(6) val month: String? = null,
    @ProtoNumber(7) val min: String? = null,
    @ProtoNumber(8) val max: String? = null,
    @ProtoNumber(9) val disabled: List<String> = emptyList(),
    @ProtoNumber(10) val showOutsideDays: Boolean = true,
    @ProtoNumber(11) val captionLayout: CaptionLayout = CaptionLayout.LABEL,
    @ProtoNumber(12) val required: Boolean = false,
    @ProtoNumber(13) val enabled: Boolean = true,
    @ProtoNumber(14) val notifyChange: Boolean = false,
) : ScreenNode

/**
 * The value form of calendar selections, shared by the server and the mod.
 */
object CalendarValues {

    /**
     * Parses an ISO date.
     *
     * @param text the date as `yyyy-MM-dd`
     * @return the date, or `null` if the text is not an ISO date
     */
    fun date(text: String): LocalDate? = try {
        LocalDate.parse(text)
    } catch (exception: DateTimeParseException) {
        null
    }

    /**
     * Parses a month.
     *
     * @param text the month as `yyyy-MM`
     * @return the month, or `null` if the text is not a month
     */
    fun month(text: String): YearMonth? = try {
        YearMonth.parse(text)
    } catch (exception: DateTimeParseException) {
        null
    }

    /**
     * Parses a calendar value.
     *
     * @param mode the calendar's mode
     * @param value the value
     * @return the selected dates, ascending, with a range as its first and last date; or `null` if
     *         the value does not have the mode's form, repeats a date or has a range ending before
     *         it starts
     */
    fun parse(mode: CalendarMode, value: String): List<LocalDate>? {
        if (value.isEmpty()) return emptyList()
        return when (mode) {
            CalendarMode.SINGLE -> date(value)?.let(::listOf)
            CalendarMode.MULTIPLE -> {
                val dates = value.split(',').map { date(it.trim()) ?: return null }
                dates.takeIf { it.toSet().size == it.size }?.sorted()
            }

            CalendarMode.RANGE -> {
                val parts = value.split('/')
                if (parts.size != 2) return null
                val from = date(parts[0]) ?: return null
                val to = date(parts[1]) ?: return null
                if (to.isBefore(from)) null else listOf(from, to)
            }
        }
    }

    /**
     * Formats selected dates as a calendar value.
     *
     * @param mode the calendar's mode
     * @param dates the selected dates; a range as its first and last date
     * @return the value
     */
    fun format(mode: CalendarMode, dates: List<LocalDate>): String = when {
        dates.isEmpty() -> ""
        mode == CalendarMode.RANGE -> "${dates.first()}/${dates.last()}"
        else -> dates.sorted().joinToString(",")
    }
}
