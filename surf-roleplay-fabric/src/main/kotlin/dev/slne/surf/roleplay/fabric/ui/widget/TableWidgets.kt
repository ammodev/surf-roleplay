package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.FlexLayout
import dev.slne.surf.roleplay.fabric.ui.layout.LayoutBox
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.protocol.screen.TableSection

/**
 * A table: its header, body and footer sections, whose cells line up in columns across all rows,
 * and any other children, such as a caption, below. Every column is as wide as its widest cell;
 * a table wider than its columns shares the rest among them by their widths, and a narrower one
 * shrinks them the same way, so that wrapping cells grow taller.
 *
 * @param id the id of the widget
 */
open class TableWidget(id: String) : ContainerWidget(id, Axis.VERTICAL) {

    /**
     * The sections of the table, in order.
     */
    val sections: List<TableSectionWidget> get() = shownChildren.filterIsInstance<TableSectionWidget>()

    /**
     * The shown rows of every section, in order.
     */
    val rows: List<TableRowWidget> get() = sections.flatMap { it.shownRows }

    /**
     * The shown children that are not sections, placed below the table.
     */
    private val below: List<Widget> get() = shownChildren.filter { it !is TableSectionWidget }

    /**
     * The widths of the columns at the last layout.
     */
    var columnWidths: IntArray = IntArray(0)
        private set

    /**
     * Creates a leaf layout box for the whole table that measures its rows and keeps the boxes
     * of the cells and the parts below.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        rows.forEach { row -> row.shownCells.forEach { it.createLayout(measurer) } }
        below.forEach { it.createLayout(measurer) }
        return LayoutBox(
            width = width,
            height = height,
            measureContent = { limit ->
                val preferred = preferredWidths()
                val natural = preferred.sum()
                val tableWidth = if (limit >= FlexLayout.UNBOUNDED) natural else minOf(natural, limit)
                val widths = widthsFor(if (limit >= FlexLayout.UNBOUNDED) natural else limit, preferred)
                Size(tableWidth, contentHeight(widths, if (limit >= FlexLayout.UNBOUNDED) natural else limit))
            },
        ).also { layoutBox = it }
    }

    /**
     * Returns the width every column prefers: that of its widest cell.
     *
     * @return the widths, one per column
     */
    private fun preferredWidths(): IntArray {
        val columns = rows.maxOfOrNull { it.shownCells.size } ?: 0
        val widths = IntArray(columns)
        rows.forEach { row ->
            row.shownCells.forEachIndexed { index, cell ->
                val box = cell.layoutBox ?: return@forEachIndexed
                widths[index] = maxOf(widths[index], FlexLayout.measure(box).width)
            }
        }
        return widths
    }

    /**
     * Shares a table width among the columns in proportion to the widths they prefer; the
     * rounding leftover goes to the last column.
     *
     * @param total the table width
     * @param preferred the widths the columns prefer
     * @return the widths of the columns
     */
    private fun widthsFor(total: Int, preferred: IntArray): IntArray {
        if (preferred.isEmpty()) return preferred
        val sum = preferred.sum()
        val widths = IntArray(preferred.size) { index ->
            if (sum == 0) total / preferred.size else (preferred[index].toLong() * total / sum).toInt()
        }
        widths[widths.lastIndex] += total - widths.sum()
        return widths
    }

    /**
     * Returns the height of a row laid out with column widths: that of its tallest cell, at
     * least the head height for rows of heads.
     *
     * @param row the row
     * @param widths the column widths
     * @return the height
     */
    private fun rowHeight(row: TableRowWidget, widths: IntArray): Int {
        var height = 0
        row.shownCells.forEachIndexed { index, cell ->
            val box = cell.layoutBox ?: return@forEachIndexed
            val cellHeight = FlexLayout.measureAt(box, widths.getOrElse(index) { 0 }).height
            height = maxOf(height, if (cell.head) maxOf(cellHeight, TableCellWidget.HEAD_HEIGHT) else cellHeight)
        }
        return height
    }

    /**
     * Returns the height of the rows and the parts below at a table width.
     *
     * @param widths the column widths
     * @param tableWidth the table width
     * @return the height
     */
    private fun contentHeight(widths: IntArray, tableWidth: Int): Int {
        val rowsHeight = rows.sumOf { rowHeight(it, widths) }
        val belowHeight = below.sumOf { part -> part.layoutBox?.let { FlexLayout.measureAt(it, tableWidth).height + BELOW_GAP } ?: 0 }
        return rowsHeight + belowHeight
    }

    /**
     * Lays the rows out row by row, their cells at the column widths (from the right when
     * right-to-left), and the parts below the rows.
     */
    override fun applyLayout() {
        bounds = layoutBox?.bounds ?: Rect.EMPTY
        childList.filter { it.hidden }.forEach { it.bounds = Rect.EMPTY }
        val widths = widthsFor(bounds.width, preferredWidths())
        columnWidths = widths
        var y = bounds.y
        sections.forEach { section ->
            val top = y
            section.childList.filter { it.hidden }.forEach { it.bounds = Rect.EMPTY }
            val shown = section.shownRows
            shown.forEachIndexed { rowIndex, row ->
                val height = rowHeight(row, widths)
                row.bounds = Rect(bounds.x, y, bounds.width, height)
                row.bottomBorder = section.section != TableSection.BODY || rowIndex < shown.lastIndex
                row.childList.filter { it.hidden }.forEach { it.bounds = Rect.EMPTY }
                var x = bounds.x
                row.shownCells.forEachIndexed { index, cell ->
                    val cellWidth = widths.getOrElse(index) { 0 }
                    val area = mirrored(Rect(x, y, cellWidth, height))
                    cell.layoutBox?.let { FlexLayout.layout(it, area) }
                    cell.applyLayout()
                    x += cellWidth
                }
                y += height
            }
            section.bounds = Rect(bounds.x, top, bounds.width, y - top)
        }
        below.forEach { part ->
            y += BELOW_GAP
            val box = part.layoutBox ?: return@forEach
            val height = FlexLayout.measureAt(box, bounds.width).height
            FlexLayout.layout(box, Rect(bounds.x, y, bounds.width, height))
            part.applyLayout()
            y += height
        }
    }

    /**
     * Moves the table, its sections and their rows by an offset.
     *
     * @param dx the horizontal offset
     * @param dy the vertical offset
     */
    override fun offset(dx: Int, dy: Int) {
        bounds = bounds.copy(x = bounds.x + dx, y = bounds.y + dy)
        shownChildren.forEach { it.offset(dx, dy) }
    }

    /**
     * Holds the table metrics.
     */
    companion object {
        /**
         * The space between the rows and a part below them, such as the caption.
         */
        const val BELOW_GAP: Int = 8
    }
}

/**
 * The header, body or footer of a table: its rows. The footer has a muted background and a
 * border above.
 *
 * @param id the id of the widget
 * @property section which part of the table the rows are
 */
class TableSectionWidget(id: String, val section: TableSection) : ContainerWidget(id, Axis.VERTICAL) {

    /**
     * The rows that are not hidden, in order.
     */
    val shownRows: List<TableRowWidget> get() = shownChildren.filterIsInstance<TableRowWidget>()

    /**
     * Moves the section and its rows by an offset.
     *
     * @param dx the horizontal offset
     * @param dy the vertical offset
     */
    override fun offset(dx: Int, dy: Int) {
        bounds = bounds.copy(x = bounds.x + dx, y = bounds.y + dy)
        shownChildren.forEach { it.offset(dx, dy) }
    }

    /**
     * Draws the footer background and border, then the rows.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        if (section == TableSection.FOOTER && bounds.height > 0) {
            ui.fill(bounds, ThemeColors.withAlpha(ui.tokens.muted, MUTED_ALPHA))
            ui.fill(Rect(bounds.x, bounds.y, bounds.width, 1), ui.tokens.border)
        }
        super.render(ui, context, mouseX, mouseY)
    }

    /**
     * Holds the section colours.
     */
    companion object {
        /**
         * The opacity of the muted colour behind footers and hovered rows.
         */
        const val MUTED_ALPHA: Float = 0.5f
    }
}

/**
 * A row of a table: its cells, laid out by the table. It is highlighted under the mouse or while
 * selected, and has a border below unless it is the last row of the body.
 *
 * @param id the id of the widget
 * @property selected whether the row is drawn as selected
 */
open class TableRowWidget(id: String, var selected: Boolean) : ContainerWidget(id, Axis.HORIZONTAL) {

    /**
     * Whether the row draws a border along its bottom edge, set by the table.
     */
    var bottomBorder: Boolean = true

    /**
     * The cells that are not hidden, in order.
     */
    val shownCells: List<TableCellWidget> get() = shownChildren.filterIsInstance<TableCellWidget>()

    /**
     * Moves the row and its cells by an offset.
     *
     * @param dx the horizontal offset
     * @param dy the vertical offset
     */
    override fun offset(dx: Int, dy: Int) {
        bounds = bounds.copy(x = bounds.x + dx, y = bounds.y + dy)
        shownChildren.forEach { it.offset(dx, dy) }
    }

    /**
     * Draws the highlight and the border, then the cells.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        when {
            selected -> ui.fill(bounds, ui.tokens.muted)
            isOver(mouseX, mouseY) -> ui.fill(bounds, ThemeColors.withAlpha(ui.tokens.muted, TableSectionWidget.MUTED_ALPHA))
        }
        if (bottomBorder) ui.fill(Rect(bounds.x, bounds.bottom - 1, bounds.width, 1), ui.tokens.border)
        super.render(ui, context, mouseX, mouseY)
    }
}

/**
 * A cell of a table row: its content, stacked and centred vertically, with padding. A head cell
 * is at least [HEAD_HEIGHT] tall.
 *
 * @param id the id of the widget
 * @property head whether the cell is a column heading
 * @param align how the content is placed across the cell's width
 */
open class TableCellWidget(id: String, val head: Boolean, align: Align) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        padding = Insets(PADDING, PADDING, PADDING, PADDING)
        mainAlign = Align.CENTER
        crossAlign = align
    }

    /**
     * Holds the cell metrics.
     */
    companion object {
        /**
         * The space inside a cell's edges.
         */
        const val PADDING: Int = 4

        /**
         * The smallest height of a row of head cells.
         */
        const val HEAD_HEIGHT: Int = 20
    }
}
