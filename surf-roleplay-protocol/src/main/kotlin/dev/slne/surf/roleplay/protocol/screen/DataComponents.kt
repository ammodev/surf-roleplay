package dev.slne.surf.roleplay.protocol.screen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * A table: its header, body and footer rows with columns that line up across all rows, and an
 * optional caption below.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the caption, header, body and footer
 */
@Serializable
@SerialName("table")
data class TableNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): TableNode = copy(children = children)
}

/**
 * The caption of a table, a muted text below it.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text as component JSON
 */
@Serializable
@SerialName("table_caption")
data class TableCaptionNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
) : ScreenNode

/**
 * The part of a table a group of rows belongs to.
 */
@Serializable
enum class TableSection {
    /**
     * The header rows, above the body.
     */
    @ProtoNumber(0)
    HEADER,

    /**
     * The body rows.
     */
    @ProtoNumber(1)
    BODY,

    /**
     * The footer rows, below the body, on a muted background.
     */
    @ProtoNumber(2)
    FOOTER,
}

/**
 * A group of rows of a table: its header, body or footer.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the rows
 * @property section which part of the table the rows are
 */
@Serializable
@SerialName("table_section")
data class TableSectionNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val section: TableSection = TableSection.BODY,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): TableSectionNode = copy(children = children)
}

/**
 * A row of a table: its cells, one per column, with a border below and a highlight on hover.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the cells
 * @property selected whether the row is drawn as selected
 */
@Serializable
@SerialName("table_row")
data class TableRowNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val selected: Boolean = false,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): TableRowNode = copy(children = children)
}

/**
 * A cell of a table row: its content, stacked. A head cell is a column heading in a medium weight
 * and a fixed height.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content, stacked
 * @property head whether the cell is a column heading
 * @property align how the content is placed across the cell's width
 */
@Serializable
@SerialName("table_cell")
data class TableCellNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val head: Boolean = false,
    @ProtoNumber(6) val align: Align = Align.START,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): TableCellNode = copy(children = children)
}

/**
 * A data table: a table of rows that the player sorts by a column, filters by a text, pages
 * through and selects, all in the mod. Its view is its input value, a [DataTableView] as JSON.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the columns, then the rows
 * @property pageSize the number of rows on a page, or `0` for one page of every row
 * @property selectable whether rows are selected with a checkbox column
 * @property filterColumn the key of the column the filter searches, or `null` for no filter
 * @property filterPlaceholder the placeholder of the filter input as component JSON
 * @property value the view the table starts with, as JSON; empty for the default view
 * @property notifyChange whether the mod reports every change of the view
 */
@Serializable
@SerialName("data_table")
data class DataTableNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val pageSize: Int = 10,
    @ProtoNumber(6) val selectable: Boolean = false,
    @ProtoNumber(7) val filterColumn: String? = null,
    @ProtoNumber(8) val filterPlaceholder: String = "",
    @ProtoNumber(9) val value: String = "",
    @ProtoNumber(10) val notifyChange: Boolean = false,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): DataTableNode = copy(children = children)
}

/**
 * A column of a data table: its key, its heading and whether the rows can be sorted by it.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property key the key of the column, unique within its table
 * @property header the heading as component JSON
 * @property sortable whether a click on the heading sorts by the column
 * @property align how the heading and the cells are placed across the column
 */
@Serializable
@SerialName("data_table_column")
data class DataTableColumnNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val key: String = "",
    @ProtoNumber(5) val header: String = "",
    @ProtoNumber(6) val sortable: Boolean = false,
    @ProtoNumber(7) val align: Align = Align.START,
) : ScreenNode

/**
 * A row of a data table: one cell per column, in the order of the columns.
 *
 * @property id the id of this node, also the id the view's selection names the row by
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the cells
 * @property selectable whether the row can be selected
 */
@Serializable
@SerialName("data_table_row")
data class DataTableRowNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val selectable: Boolean = true,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): DataTableRowNode = copy(children = children)
}

/**
 * A cell of a data table row: its content, stacked, and the key it sorts and filters by. Keys
 * that are both numbers compare as numbers, others as texts ignoring case.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content, stacked
 * @property sortKey the key the cell sorts and filters by
 */
@Serializable
@SerialName("data_table_cell")
data class DataTableCellNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val sortKey: String = "",
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): DataTableCellNode = copy(children = children)
}

/**
 * The family of a chart.
 */
@Serializable
enum class ChartKind {
    /**
     * Areas filled below lines.
     */
    @ProtoNumber(0)
    AREA,

    /**
     * Bars, vertical or horizontal.
     */
    @ProtoNumber(1)
    BAR,

    /**
     * Lines through the values.
     */
    @ProtoNumber(2)
    LINE,

    /**
     * Slices of a circle, one per category, or a ring for a donut.
     */
    @ProtoNumber(3)
    PIE,

    /**
     * Polygons on axes around a centre, one axis per category.
     */
    @ProtoNumber(4)
    RADAR,

    /**
     * Rings around a centre, one per category.
     */
    @ProtoNumber(5)
    RADIAL,
}

/**
 * How the lines of line and area charts run between their points.
 */
@Serializable
enum class ChartCurve {
    /**
     * Straight from point to point.
     */
    @ProtoNumber(0)
    LINEAR,

    /**
     * A smooth curve through the points that does not overshoot them.
     */
    @ProtoNumber(1)
    NATURAL,

    /**
     * Horizontal steps that change halfway between two points.
     */
    @ProtoNumber(2)
    STEP,
}

/**
 * How a chart tooltip marks each series.
 */
@Serializable
enum class ChartIndicator {
    /**
     * A small square in the series colour.
     */
    @ProtoNumber(0)
    DOT,

    /**
     * A short vertical bar in the series colour.
     */
    @ProtoNumber(1)
    LINE,

    /**
     * A dashed vertical outline in the series colour.
     */
    @ProtoNumber(2)
    DASHED,
}

/**
 * A series of a chart: one value per category, drawn in one of the theme's five chart colours.
 *
 * @property key the key of the series, unique within its chart
 * @property label the name of the series in the tooltip and the legend, as component JSON
 * @property color the number of the theme's chart colour, 1 to 5
 * @property values the values, one per category
 */
@Serializable
data class ChartSeries(
    @ProtoNumber(1) val key: String = "",
    @ProtoNumber(2) val label: String = "",
    @ProtoNumber(3) val color: Int = 1,
    @ProtoNumber(4) val values: List<Double> = emptyList(),
)

/**
 * A chart of any family: its categories, its series and how it is drawn. A chart only shows
 * data; hovering a category shows a tooltip with every series' value.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out; a fitting chart is 16 by 9
 * @property kind the family of the chart
 * @property categories the labels of the categories as component JSON, in order
 * @property series the series
 * @property categoryColors the chart colour numbers of the categories of pie and radial charts;
 *           missing ones count up from 1
 * @property stacked whether the series of area and bar charts are stacked
 * @property horizontal whether bars run from the left instead of from the bottom
 * @property curve how lines run between their points
 * @property dots whether line charts mark their points
 * @property grid whether the value grid is drawn
 * @property xAxis whether the category axis is labelled
 * @property yAxis whether the value axis is labelled
 * @property legend whether the series are listed below the chart
 * @property tooltip whether hovering shows a tooltip
 * @property indicator how the tooltip marks each series
 * @property donut whether a pie chart is drawn as a ring
 * @property labels whether pie slices and bars show their values
 */
@Serializable
@SerialName("chart")
data class ChartNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val kind: ChartKind = ChartKind.BAR,
    @ProtoNumber(5) val categories: List<String> = emptyList(),
    @ProtoNumber(6) val series: List<ChartSeries> = emptyList(),
    @ProtoNumber(7) val categoryColors: List<Int> = emptyList(),
    @ProtoNumber(8) val stacked: Boolean = false,
    @ProtoNumber(9) val horizontal: Boolean = false,
    @ProtoNumber(10) val curve: ChartCurve = ChartCurve.NATURAL,
    @ProtoNumber(11) val dots: Boolean = false,
    @ProtoNumber(12) val grid: Boolean = true,
    @ProtoNumber(13) val xAxis: Boolean = true,
    @ProtoNumber(14) val yAxis: Boolean = false,
    @ProtoNumber(15) val legend: Boolean = false,
    @ProtoNumber(16) val tooltip: Boolean = true,
    @ProtoNumber(17) val indicator: ChartIndicator = ChartIndicator.DOT,
    @ProtoNumber(18) val donut: Boolean = false,
    @ProtoNumber(19) val labels: Boolean = false,
) : ScreenNode

/**
 * A chat view: its messages, stacked in a scrolling column that starts at the newest message and
 * stays there while new messages are appended, unless the player scrolled up.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out, usually fixed
 * @property children the messages, oldest first
 */
@Serializable
@SerialName("chat_view")
data class ChatViewNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): ChatViewNode = copy(children = children)
}

/**
 * A message of a chat view: a bubble with any content, the sender's name and the time above it,
 * and for messages of others the sender's avatar beside it. The player's own messages sit at the
 * right in the primary colour, those of others at the left on a muted background.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content of the bubble, stacked
 * @property own whether the message is the player's own
 * @property playerId the UUID of the player whose head is the avatar, or `null`
 * @property texture the resource-pack texture of the avatar when there is no player, or `null`
 * @property fallback the initials shown while the avatar cannot be drawn
 * @property name the sender's name as component JSON, empty for none
 * @property time the time of the message as component JSON, empty for none
 */
@Serializable
@SerialName("chat_message")
data class ChatMessageNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val own: Boolean = false,
    @ProtoNumber(6) val playerId: String? = null,
    @ProtoNumber(7) val texture: String? = null,
    @ProtoNumber(8) val fallback: String = "",
    @ProtoNumber(9) val name: String = "",
    @ProtoNumber(10) val time: String = "",
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): ChatMessageNode = copy(children = children)
}
