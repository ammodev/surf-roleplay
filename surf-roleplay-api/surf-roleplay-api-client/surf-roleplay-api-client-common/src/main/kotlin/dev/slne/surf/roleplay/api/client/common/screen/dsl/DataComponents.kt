package dev.slne.surf.roleplay.api.client.common.screen.dsl

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ChangeHandler
import dev.slne.surf.roleplay.api.client.common.screen.ChartCurve
import dev.slne.surf.roleplay.api.client.common.screen.ChartElement
import dev.slne.surf.roleplay.api.client.common.screen.ChartIndicator
import dev.slne.surf.roleplay.api.client.common.screen.ChartKind
import dev.slne.surf.roleplay.api.client.common.screen.ChartSeries
import dev.slne.surf.roleplay.api.client.common.screen.ChatMessageElement
import dev.slne.surf.roleplay.api.client.common.screen.ChatViewElement
import dev.slne.surf.roleplay.api.client.common.screen.DataTableCellElement
import dev.slne.surf.roleplay.api.client.common.screen.DataTableColumnElement
import dev.slne.surf.roleplay.api.client.common.screen.DataTableElement
import dev.slne.surf.roleplay.api.client.common.screen.DataTableRowElement
import dev.slne.surf.roleplay.api.client.common.screen.DataTableView
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.ScreenPatchBuilder
import dev.slne.surf.roleplay.api.client.common.screen.TableCaptionElement
import dev.slne.surf.roleplay.api.client.common.screen.TableCellElement
import dev.slne.surf.roleplay.api.client.common.screen.TableElement
import dev.slne.surf.roleplay.api.client.common.screen.TableRowElement
import dev.slne.surf.roleplay.api.client.common.screen.TableSection
import dev.slne.surf.roleplay.api.client.common.screen.TableSectionElement
import net.kyori.adventure.text.Component
import java.util.UUID

/**
 * Adds a table: a [TableHeader], a [TableBody], a [TableFooter] and a [TableCaption], with columns
 * that line up across all rows.
 *
 * @param width how wide the table is laid out; fitting tables fill their container
 * @param id the id of the table, or `null` for a generated one
 * @param children the builder of the parts
 * @return the table
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Table(width: ElementSize = ElementSize.FIT, id: String? = null, children: ComponentScope.() -> Unit): TableElement {
    val elementId = nextId(id)
    return add(TableElement(elementId, this.children(children), width))
}

/**
 * Adds the caption of a table, a muted text shown below its rows.
 *
 * @param text the text
 * @param id the id of the caption, or `null` for a generated one
 * @return the caption
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.TableCaption(text: Component, id: String? = null): TableCaptionElement = add(TableCaptionElement(nextId(id), text))

/**
 * Adds the caption of a table, a plain muted text shown below its rows.
 *
 * @param text the text
 * @param id the id of the caption, or `null` for a generated one
 * @return the caption
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.TableCaption(text: String, id: String? = null): TableCaptionElement = TableCaption(Component.text(text), id)

/**
 * Adds a group of rows of a table.
 *
 * @param section which part of the table the rows are
 * @param id the id of the group, or `null` for a generated one
 * @param children the builder of the rows
 * @return the group
 * @throws IllegalArgumentException if [id] starts with `_`
 */
private fun ComponentScope.tableSection(section: TableSection, id: String?, children: ComponentScope.() -> Unit): TableSectionElement {
    val elementId = nextId(id)
    return add(TableSectionElement(elementId, this.children(children), section))
}

/**
 * Adds the header of a table: [TableRow]s of [TableHead]s above the body.
 *
 * @param id the id of the header, or `null` for a generated one
 * @param children the builder of the rows
 * @return the header
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.TableHeader(id: String? = null, children: ComponentScope.() -> Unit): TableSectionElement =
    tableSection(TableSection.HEADER, id, children)

/**
 * Adds the body of a table: its [TableRow]s.
 *
 * @param id the id of the body, or `null` for a generated one
 * @param children the builder of the rows
 * @return the body
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.TableBody(id: String? = null, children: ComponentScope.() -> Unit): TableSectionElement =
    tableSection(TableSection.BODY, id, children)

/**
 * Adds the footer of a table: [TableRow]s below the body, on a muted background.
 *
 * @param id the id of the footer, or `null` for a generated one
 * @param children the builder of the rows
 * @return the footer
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.TableFooter(id: String? = null, children: ComponentScope.() -> Unit): TableSectionElement =
    tableSection(TableSection.FOOTER, id, children)

/**
 * Adds a row of a table: its [TableHead]s or [TableCell]s, one per column.
 *
 * @param selected whether the row is drawn as selected
 * @param id the id of the row, or `null` for a generated one
 * @param children the builder of the cells
 * @return the row
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.TableRow(selected: Boolean = false, id: String? = null, children: ComponentScope.() -> Unit): TableRowElement {
    val elementId = nextId(id)
    return add(TableRowElement(elementId, this.children(children), selected))
}

/**
 * Adds a column heading with any content, stacked.
 *
 * @param align how the content is placed across the cell
 * @param id the id of the heading, or `null` for a generated one
 * @param children the builder of the content
 * @return the heading
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.TableHead(align: Alignment = Alignment.START, id: String? = null, children: ComponentScope.() -> Unit): TableCellElement {
    val elementId = nextId(id)
    return add(TableCellElement(elementId, this.children(children), head = true, align = align))
}

/**
 * Adds a column heading that holds a label with a text.
 *
 * @param text the text
 * @param align how the text is placed across the cell
 * @param id the id of the heading, or `null` for a generated one
 * @return the heading
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.TableHead(text: Component, align: Alignment = Alignment.START, id: String? = null): TableCellElement =
    TableHead(align, id) { Label(text) }

/**
 * Adds a column heading that holds a label with a plain text.
 *
 * @param text the text
 * @param align how the text is placed across the cell
 * @param id the id of the heading, or `null` for a generated one
 * @return the heading
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.TableHead(text: String, align: Alignment = Alignment.START, id: String? = null): TableCellElement =
    TableHead(Component.text(text), align, id)

/**
 * Adds a cell of a table row with any content, stacked.
 *
 * @param align how the content is placed across the cell
 * @param id the id of the cell, or `null` for a generated one
 * @param children the builder of the content
 * @return the cell
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.TableCell(align: Alignment = Alignment.START, id: String? = null, children: ComponentScope.() -> Unit): TableCellElement {
    val elementId = nextId(id)
    return add(TableCellElement(elementId, this.children(children), align = align))
}

/**
 * Adds a cell of a table row that holds a label with a text.
 *
 * @param text the text
 * @param align how the text is placed across the cell
 * @param id the id of the cell, or `null` for a generated one
 * @return the cell
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.TableCell(text: Component, align: Alignment = Alignment.START, id: String? = null): TableCellElement =
    TableCell(align, id) { Label(text) }

/**
 * Adds a cell of a table row that holds a label with a plain text.
 *
 * @param text the text
 * @param align how the text is placed across the cell
 * @param id the id of the cell, or `null` for a generated one
 * @return the cell
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.TableCell(text: String, align: Alignment = Alignment.START, id: String? = null): TableCellElement =
    TableCell(Component.text(text), align, id)

/**
 * Adds a data table: [DataTableColumn]s, then [DataTableRow]s with one [DataTableCell] per column,
 * which the player sorts by a column, filters by a text, pages through and selects, all in the
 * mod. Its view is its input value, validated on the server.
 *
 * @param pageSize the number of rows on a page, or `0` for one page of every row
 * @param selectable whether rows are selected with a checkbox column
 * @param filterColumn the key of the column the filter searches, or `null` for no filter
 * @param filterPlaceholder the placeholder of the filter input
 * @param value the view the table starts with
 * @param width how wide the table is laid out
 * @param onChange the handler run on every change of the view, or `null` for none
 * @param id the id of the table, or `null` for a generated one
 * @param children the builder of the columns and rows
 * @return the reference to the view of the table
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.DataTable(
    pageSize: Int = 10,
    selectable: Boolean = false,
    filterColumn: String? = null,
    filterPlaceholder: Component = Component.text("Filtern..."),
    value: DataTableView = DataTableView(),
    width: ElementSize = ElementSize.FIT,
    onChange: ChangeHandler? = null,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): InputRef<DataTableView> {
    val elementId = nextId(id)
    add(
        DataTableElement(
            elementId, this.children(children), pageSize, selectable, filterColumn, filterPlaceholder, value,
            bindChange(elementId, onChange), width,
        ),
    )
    return InputRef(elementId, InputParsers.dataTableView)
}

/**
 * Adds a column of a data table.
 *
 * @param key the key of the column, unique within its table
 * @param header the heading
 * @param sortable whether a click on the heading sorts by the column
 * @param align how the heading and the cells are placed across the column
 * @param id the id of the column, or `null` for a generated one
 * @return the column
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.DataTableColumn(
    key: String,
    header: Component,
    sortable: Boolean = false,
    align: Alignment = Alignment.START,
    id: String? = null,
): DataTableColumnElement = add(DataTableColumnElement(nextId(id), key, header, sortable, align))

/**
 * Adds a column of a data table with a plain heading.
 *
 * @param key the key of the column, unique within its table
 * @param header the heading
 * @param sortable whether a click on the heading sorts by the column
 * @param align how the heading and the cells are placed across the column
 * @param id the id of the column, or `null` for a generated one
 * @return the column
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.DataTableColumn(
    key: String,
    header: String,
    sortable: Boolean = false,
    align: Alignment = Alignment.START,
    id: String? = null,
): DataTableColumnElement = DataTableColumn(key, Component.text(header), sortable, align, id)

/**
 * Adds a row of a data table: one [DataTableCell] per column, in the order of the columns. The
 * view's selection names the row by its id, so a row needs an explicit id derived from its data,
 * such as a record key, for the selection to stay on the same record when rows are added,
 * removed or reordered; a generated id follows the row's position instead.
 *
 * @param selectable whether the row can be selected
 * @param id the id of the row, or `null` for a generated one
 * @param children the builder of the cells
 * @return the row
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.DataTableRow(selectable: Boolean = true, id: String? = null, children: ComponentScope.() -> Unit): DataTableRowElement {
    val elementId = nextId(id)
    return add(DataTableRowElement(elementId, this.children(children), selectable))
}

/**
 * Adds a cell of a data table row with any content, stacked, and the key it sorts and filters by.
 * Keys that are both numbers compare as numbers, others as texts ignoring case.
 *
 * @param sortKey the key the cell sorts and filters by
 * @param id the id of the cell, or `null` for a generated one
 * @param children the builder of the content
 * @return the cell
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.DataTableCell(sortKey: String, id: String? = null, children: ComponentScope.() -> Unit): DataTableCellElement {
    val elementId = nextId(id)
    return add(DataTableCellElement(elementId, this.children(children), sortKey))
}

/**
 * Adds a cell of a data table row that holds a label with a text, and the key it sorts and filters
 * by.
 *
 * @param text the text
 * @param sortKey the key the cell sorts and filters by
 * @param id the id of the cell, or `null` for a generated one
 * @return the cell
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.DataTableCell(text: Component, sortKey: String, id: String? = null): DataTableCellElement =
    DataTableCell(sortKey, id) { Label(text) }

/**
 * Adds a cell of a data table row that holds a label with a plain text, and the key it sorts and
 * filters by.
 *
 * @param text the text
 * @param sortKey the key the cell sorts and filters by
 * @param id the id of the cell, or `null` for a generated one
 * @return the cell
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.DataTableCell(text: String, sortKey: String, id: String? = null): DataTableCellElement =
    DataTableCell(Component.text(text), sortKey, id)

/**
 * Adds a chart of any family. It only shows data; hovering a category shows a tooltip with the
 * value of every series.
 *
 * @param kind the family of the chart
 * @param categories the labels of the categories, in order
 * @param series the series
 * @param width how wide the chart is laid out
 * @param height how tall the chart is laid out; a fitting chart is 16 by 9
 * @param categoryColors the chart colour numbers of the categories of pie and radial charts;
 *        missing ones count up from 1
 * @param stacked whether the series of area and bar charts are stacked
 * @param horizontal whether bars run from the left instead of from the bottom
 * @param curve how lines run between their points
 * @param dots whether line charts mark their points
 * @param grid whether the value grid is drawn
 * @param categoryAxis whether the categories are labelled
 * @param valueAxis whether the values are labelled
 * @param legend whether the series, or the categories of pie and radial charts, are listed below
 *        the chart
 * @param tooltip whether hovering shows a tooltip
 * @param indicator how the tooltip marks each series
 * @param donut whether a pie chart is drawn as a ring
 * @param labels whether pie slices and bars show their values
 * @param id the id of the chart, or `null` for a generated one
 * @return the chart
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Chart(
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
    id: String? = null,
): ChartElement = add(
    ChartElement(
        nextId(id), kind, categories, series, categoryColors, stacked, horizontal, curve, dots, grid, categoryAxis, valueAxis, legend,
        tooltip, indicator, donut, labels, width, height,
    ),
)

/**
 * Adds a chat view: its [ChatMessage]s, oldest first, stacked in a scrolling column that starts
 * at the newest message and stays there while new messages are appended with
 * [ScreenPatchBuilder.append], unless the player scrolled up.
 *
 * @param height how tall the view is laid out, usually fixed
 * @param width how wide the view is laid out
 * @param id the id of the view, or `null` for a generated one
 * @param children the builder of the messages
 * @return the view
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ChatView(
    height: ElementSize,
    width: ElementSize = ElementSize.FIT,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): ChatViewElement {
    val elementId = nextId(id)
    return add(ChatViewElement(elementId, this.children(children), width, height))
}

/**
 * Adds a message of a chat view: a bubble with any content, the name of the sender and the time
 * above it, and for messages of others the avatar of the sender beside it.
 *
 * @param own whether the message is the player's own, shown at the right
 * @param name the name of the sender, empty for none
 * @param time the time of the message, empty for none
 * @param playerId the player whose head is the avatar, or `null`
 * @param texture the resource-pack texture of the avatar when there is no player, or `null`
 * @param fallback the initials shown while the avatar cannot be drawn
 * @param id the id of the message, or `null` for a generated one
 * @param children the builder of the content of the bubble
 * @return the message
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ChatMessage(
    own: Boolean = false,
    name: Component = Component.empty(),
    time: Component = Component.empty(),
    playerId: UUID? = null,
    texture: String? = null,
    fallback: String = "",
    id: String? = null,
    children: ComponentScope.() -> Unit,
): ChatMessageElement {
    val elementId = nextId(id)
    return add(ChatMessageElement(elementId, this.children(children), own, playerId, texture, fallback, name, time))
}
