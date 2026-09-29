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
