package dev.slne.surf.roleplay.api.client.common.screen

import net.kyori.adventure.text.Component

/**
 * Builds the elements of a builder block.
 *
 * @param children the builder block
 * @return the elements it added
 */
private fun build(children: ElementsBuilder.() -> Unit): List<ScreenElement> = ElementsBuilder().apply(children).elements.toList()

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
 * Adds a table. Its children are a [tableHeader], a [tableBody], a [tableFooter] and a
 * [tableCaption].
 *
 * @param id the id of the table
 * @param width how wide the table is laid out; fitting tables fill their container
 * @param children the builder of the parts
 */
fun ElementsBuilder.table(id: String, width: ElementSize = ElementSize.FIT, children: ElementsBuilder.() -> Unit) {
    elements += TableElement(id, build(children), width)
}

/**
 * Adds the caption of a table, shown below its rows.
 *
 * @param id the id of the caption
 * @param text the text
 */
fun ElementsBuilder.tableCaption(id: String, text: Component) {
    elements += TableCaptionElement(id, text)
}

/**
 * Adds the header of a table. Its children are [tableRow]s of [tableHead]s.
 *
 * @param id the id of the header
 * @param children the builder of the rows
 */
fun ElementsBuilder.tableHeader(id: String, children: ElementsBuilder.() -> Unit) {
    elements += TableSectionElement(id, build(children), TableSection.HEADER)
}

/**
 * Adds the body of a table. Its children are [tableRow]s.
 *
 * @param id the id of the body
 * @param children the builder of the rows
 */
fun ElementsBuilder.tableBody(id: String, children: ElementsBuilder.() -> Unit) {
    elements += TableSectionElement(id, build(children), TableSection.BODY)
}

/**
 * Adds the footer of a table. Its children are [tableRow]s.
 *
 * @param id the id of the footer
 * @param children the builder of the rows
 */
fun ElementsBuilder.tableFooter(id: String, children: ElementsBuilder.() -> Unit) {
    elements += TableSectionElement(id, build(children), TableSection.FOOTER)
}

/**
 * Adds a row of a table. Its children are [tableHead]s or [tableCell]s.
 *
 * @param id the id of the row
 * @param selected whether the row is drawn as selected
 * @param children the builder of the cells
 */
fun ElementsBuilder.tableRow(id: String, selected: Boolean = false, children: ElementsBuilder.() -> Unit) {
    elements += TableRowElement(id, build(children), selected)
}

/**
 * Adds a column heading with any content.
 *
 * @param id the id of the heading
 * @param align how the content is placed across the cell
 * @param children the builder of the content
 */
fun ElementsBuilder.tableHead(id: String, align: Alignment = Alignment.START, children: ElementsBuilder.() -> Unit) {
    elements += TableCellElement(id, build(children), head = true, align = align)
}

/**
 * Adds a column heading with a text, whose label has the id of the heading followed by
 * `_text`.
 *
 * @param id the id of the heading
 * @param text the text
 * @param align how the text is placed across the cell
 */
fun ElementsBuilder.tableHead(id: String, text: Component, align: Alignment = Alignment.START) {
    tableHead(id, align) { label("${id}_text", text) }
}

/**
 * Adds a cell with any content.
 *
 * @param id the id of the cell
 * @param align how the content is placed across the cell
 * @param children the builder of the content
 */
fun ElementsBuilder.tableCell(id: String, align: Alignment = Alignment.START, children: ElementsBuilder.() -> Unit) {
    elements += TableCellElement(id, build(children), align = align)
}

/**
 * Adds a cell with a text, whose label has the id of the cell followed by `_text`.
 *
 * @param id the id of the cell
 * @param text the text
 * @param align how the text is placed across the cell
 */
fun ElementsBuilder.tableCell(id: String, text: Component, align: Alignment = Alignment.START) {
    tableCell(id, align) { label("${id}_text", text) }
}

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
 * Adds a data table. Its children are [dataTableColumn]s, then [dataTableRow]s with one
 * [dataTableCell] per column.
 *
 * @param id the id of the table
 * @param pageSize the number of rows on a page, or `0` for one page of every row
 * @param selectable whether rows are selected with a checkbox column
 * @param filterColumn the key of the column the filter searches, or `null` for no filter
 * @param filterPlaceholder the placeholder of the filter input
 * @param value the view the table starts with
 * @param width how wide the table is laid out
 * @param onChange reports every change of the view at once, or `null` for none
 * @param children the builder of the columns and rows
 */
fun ElementsBuilder.dataTable(
    id: String,
    pageSize: Int = 10,
    selectable: Boolean = false,
    filterColumn: String? = null,
    filterPlaceholder: Component = Component.text("Filtern..."),
    value: DataTableView = DataTableView(),
    width: ElementSize = ElementSize.FIT,
    onChange: ChangeHandler? = null,
    children: ElementsBuilder.() -> Unit,
) {
    elements += DataTableElement(id, build(children), pageSize, selectable, filterColumn, filterPlaceholder, value, onChange, width)
}

/**
 * Adds a column of a data table.
 *
 * @param id the id of the column
 * @param key the key of the column, unique within its table
 * @param header the heading
 * @param sortable whether a click on the heading sorts by the column
 * @param align how the heading and the cells are placed across the column
 */
fun ElementsBuilder.dataTableColumn(id: String, key: String, header: Component, sortable: Boolean = false, align: Alignment = Alignment.START) {
    elements += DataTableColumnElement(id, key, header, sortable, align)
}

/**
 * Adds a row of a data table. Its children are [dataTableCell]s, one per column.
 *
 * @param id the id of the row, which the view's selection names it by
 * @param selectable whether the row can be selected
 * @param children the builder of the cells
 */
fun ElementsBuilder.dataTableRow(id: String, selectable: Boolean = true, children: ElementsBuilder.() -> Unit) {
    elements += DataTableRowElement(id, build(children), selectable)
}

/**
 * Adds a cell of a data table row with any content.
 *
 * @param id the id of the cell
 * @param sortKey the key the cell sorts and filters by
 * @param children the builder of the content
 */
fun ElementsBuilder.dataTableCell(id: String, sortKey: String, children: ElementsBuilder.() -> Unit) {
    elements += DataTableCellElement(id, build(children), sortKey)
}

/**
 * Adds a cell of a data table row with a text, whose label has the id of the cell followed by
 * `_text`.
 *
 * @param id the id of the cell
 * @param text the text
 * @param sortKey the key the cell sorts and filters by
 */
fun ElementsBuilder.dataTableCell(id: String, text: Component, sortKey: String) {
    dataTableCell(id, sortKey) { label("${id}_text", text) }
}

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
 * Adds a chart.
 *
 * @param id the id of the chart
 * @param kind the family of the chart
 * @param categories the labels of the categories, in order
 * @param series the series
 * @param width how wide the chart is laid out
 * @param height how tall the chart is laid out; a fitting chart is 16 by 9
 * @param categoryColors the chart colour numbers of the categories of pie and radial charts
 * @param stacked whether the series of area and bar charts are stacked
 * @param horizontal whether bars run from the left instead of from the bottom
 * @param curve how lines run between their points
 * @param dots whether line charts mark their points
 * @param grid whether the value grid is drawn
 * @param categoryAxis whether the categories are labelled
 * @param valueAxis whether the values are labelled
 * @param legend whether the series are listed below the chart
 * @param tooltip whether hovering shows a tooltip
 * @param indicator how the tooltip marks each series
 * @param donut whether a pie chart is drawn as a ring
 * @param labels whether pie slices and bars show their values
 */
fun ElementsBuilder.chart(
    id: String,
    kind: ChartKind,
    categories: List<Component>,
    series: List<ChartSeries>,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    categoryColors: List<Int> = emptyList(),
    stacked: Boolean = false,
    horizontal: Boolean = false,
    curve: ChartCurve = ChartCurve.NATURAL,
    dots: Boolean = false,
    grid: Boolean = true,
    categoryAxis: Boolean = true,
    valueAxis: Boolean = false,
    legend: Boolean = false,
    tooltip: Boolean = true,
    indicator: ChartIndicator = ChartIndicator.DOT,
    donut: Boolean = false,
    labels: Boolean = false,
) {
    elements += ChartElement(
        id, kind, categories, series, categoryColors, stacked, horizontal, curve, dots, grid, categoryAxis, valueAxis, legend, tooltip,
        indicator, donut, labels, width, height,
    )
}
