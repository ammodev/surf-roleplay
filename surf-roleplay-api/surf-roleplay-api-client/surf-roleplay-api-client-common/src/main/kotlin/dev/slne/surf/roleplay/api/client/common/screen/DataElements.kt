package dev.slne.surf.roleplay.api.client.common.screen

import net.kyori.adventure.text.Component
import java.util.UUID

/**
 * A table: its header, body and footer rows with columns that line up across all rows, and an
 * optional caption below.
 *
 * @property id the id of this element
 * @property children the caption, header, body and footer
 * @property width how wide this element is laid out; fitting tables fill their container
 * @property height how tall this element is laid out
 */
data class TableElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The caption of a table, a muted text below it.
 *
 * @property id the id of this element
 * @property text the text
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class TableCaptionElement(
    override val id: String,
    val text: Component = Component.empty(),
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * The part of a table a group of rows belongs to.
 */
enum class TableSection {
    /**
     * The header rows, above the body.
     */
    HEADER,

    /**
     * The body rows.
     */
    BODY,

    /**
     * The footer rows, below the body, on a muted background.
     */
    FOOTER,
}

/**
 * A group of rows of a table: its header, body or footer.
 *
 * @property id the id of this element
 * @property children the rows
 * @property section which part of the table the rows are
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class TableSectionElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val section: TableSection = TableSection.BODY,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A row of a table: its cells, one per column.
 *
 * @property id the id of this element
 * @property children the cells
 * @property selected whether the row is drawn as selected
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class TableRowElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val selected: Boolean = false,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A cell of a table row: its content, stacked. A head cell is a column heading.
 *
 * @property id the id of this element
 * @property children the content, stacked
 * @property head whether the cell is a column heading
 * @property align how the content is placed across the cell's width
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class TableCellElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val head: Boolean = false,
    val align: Alignment = Alignment.START,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A data table: a table of rows that the player sorts by a column, filters by a text, pages
 * through and selects, all in the mod. Its view is its input value, a [DataTableView] as JSON,
 * validated on the server.
 *
 * @property id the id of this element
 * @property children the columns, then the rows
 * @property pageSize the number of rows on a page, or `0` for one page of every row
 * @property selectable whether rows are selected with a checkbox column
 * @property filterColumn the key of the column the filter searches, or `null` for no filter
 * @property filterPlaceholder the placeholder of the filter input
 * @property value the view the table shows
 * @property onChange reports every change of the view at once, or `null` for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class DataTableElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val pageSize: Int = 10,
    val selectable: Boolean = false,
    val filterColumn: String? = null,
    val filterPlaceholder: Component = Component.empty(),
    val value: DataTableView = DataTableView(),
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A column of a data table.
 *
 * @property id the id of this element
 * @property key the key of the column, unique within its table
 * @property header the heading
 * @property sortable whether a click on the heading sorts by the column
 * @property align how the heading and the cells are placed across the column
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class DataTableColumnElement(
    override val id: String,
    val key: String,
    val header: Component = Component.empty(),
    val sortable: Boolean = false,
    val align: Alignment = Alignment.START,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A row of a data table: one cell per column, in the order of the columns.
 *
 * @property id the id of this element, also the id the view's selection names the row by
 * @property children the cells
 * @property selectable whether the row can be selected
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class DataTableRowElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val selectable: Boolean = true,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A cell of a data table row: its content and the key it sorts and filters by. Keys that are
 * both numbers compare as numbers, others as texts ignoring case.
 *
 * @property id the id of this element
 * @property children the content, stacked
 * @property sortKey the key the cell sorts and filters by
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class DataTableCellElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val sortKey: String = "",
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The family of a chart.
 */
enum class ChartKind {
    /**
     * Areas filled below lines.
     */
    AREA,

    /**
     * Bars, vertical or horizontal.
     */
    BAR,

    /**
     * Lines through the values.
     */
    LINE,

    /**
     * Slices of a circle, one per category, or a ring for a donut.
     */
    PIE,

    /**
     * Polygons on axes around a centre, one axis per category.
     */
    RADAR,

    /**
     * Rings around a centre, one per category.
     */
    RADIAL,
}

/**
 * How the lines of line and area charts run between their points.
 */
enum class ChartCurve {
    /**
     * Straight from point to point.
     */
    LINEAR,

    /**
     * A smooth curve through the points that does not overshoot them.
     */
    NATURAL,

    /**
     * Horizontal steps that change halfway between two points.
     */
    STEP,
}

/**
 * How a chart tooltip marks each series.
 */
enum class ChartIndicator {
    /**
     * A small square in the series colour.
     */
    DOT,

    /**
     * A short vertical bar in the series colour.
     */
    LINE,

    /**
     * A dashed vertical outline in the series colour.
     */
    DASHED,
}

/**
 * A series of a chart: one value per category, drawn in one of the theme's five chart colours.
 *
 * @property key the key of the series, unique within its chart
 * @property label the name of the series in the tooltip and the legend
 * @property color the number of the theme's chart colour, 1 to 5
 * @property values the values, one per category
 */
data class ChartSeries(
    val key: String,
    val label: Component,
    val color: Int,
    val values: List<Double>,
)

/**
 * A chart of any family. It only shows data; hovering a category shows a tooltip with the value
 * of every series.
 *
 * @property id the id of this element
 * @property kind the family of the chart
 * @property categories the labels of the categories, in order
 * @property series the series
 * @property categoryColors the chart colour numbers of the categories of pie and radial charts;
 *           missing ones count up from 1
 * @property stacked whether the series of area and bar charts are stacked
 * @property horizontal whether bars run from the left instead of from the bottom
 * @property curve how lines run between their points
 * @property dots whether line charts mark their points
 * @property grid whether the value grid is drawn
 * @property categoryAxis whether the categories are labelled
 * @property valueAxis whether the values are labelled
 * @property legend whether the series, or the categories of pie and radial charts, are listed
 *           below the chart
 * @property tooltip whether hovering shows a tooltip
 * @property indicator how the tooltip marks each series
 * @property donut whether a pie chart is drawn as a ring
 * @property labels whether pie slices and bars show their values
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out; a fitting chart is 16 by 9
 */
data class ChartElement(
    override val id: String,
    val kind: ChartKind,
    val categories: List<Component>,
    val series: List<ChartSeries>,
    val categoryColors: List<Int> = emptyList(),
    val stacked: Boolean = false,
    val horizontal: Boolean = false,
    val curve: ChartCurve = ChartCurve.NATURAL,
    val dots: Boolean = false,
    val grid: Boolean = true,
    val categoryAxis: Boolean = true,
    val valueAxis: Boolean = false,
    val legend: Boolean = false,
    val tooltip: Boolean = true,
    val indicator: ChartIndicator = ChartIndicator.DOT,
    val donut: Boolean = false,
    val labels: Boolean = false,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A chat view: its messages, stacked in a scrolling column that starts at the newest message and
 * stays there while new messages are appended, unless the player scrolled up. Append messages
 * with [ScreenPatchBuilder.append].
 *
 * @property id the id of this element
 * @property children the messages, oldest first
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out, usually fixed
 */
data class ChatViewElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A message of a chat view: a bubble with any content, the name of the sender and the time above
 * it, and for messages of others the avatar of the sender beside it.
 *
 * @property id the id of this element
 * @property children the content of the bubble, stacked
 * @property own whether the message is the player's own, shown at the right
 * @property playerId the player whose head is the avatar, or `null`
 * @property texture the resource-pack texture of the avatar when there is no player, or `null`
 * @property fallback the initials shown while the avatar cannot be drawn
 * @property name the name of the sender, empty for none
 * @property time the time of the message, empty for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class ChatMessageElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val own: Boolean = false,
    val playerId: UUID? = null,
    val texture: String? = null,
    val fallback: String = "",
    val name: Component = Component.empty(),
    val time: Component = Component.empty(),
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement
