package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.LayoutBox
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.theme.UiMetrics
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.ButtonSize
import dev.slne.surf.roleplay.protocol.screen.ButtonVariant
import dev.slne.surf.roleplay.protocol.screen.DataTableView
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.TableSection
import dev.slne.surf.roleplay.protocol.screen.TextKind
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent

/**
 * A column of a data table.
 *
 * @property key the key of the column, unique within its table
 * @property header the heading as component JSON
 * @property sortable whether a click on the heading sorts by the column
 * @property align how the heading and the cells are placed across the column
 */
data class DataTableColumn(val key: String, val header: String, val sortable: Boolean, val align: Align)

/**
 * A data table: an optional filter input, a table whose rows are sorted, filtered, paged and
 * selected in the mod, a text when no row matches, and a footer with the selection count and the
 * page buttons. Its view is its input value, a [DataTableView] as JSON. The heading buttons, row
 * checkboxes, filter and page buttons are made by the table itself and report nothing to the
 * server; only the view does.
 *
 * @param id the id of the widget
 * @property pageSize the number of rows on a page, or `0` for one page of every row
 * @property selectable whether rows are selected with a checkbox column
 * @property filterColumn the key of the column the filter searches, or `null` for no filter
 * @param filterPlaceholder the placeholder of the filter input as component JSON
 */
class DataTableWidget(
    id: String,
    val pageSize: Int,
    val selectable: Boolean,
    val filterColumn: String?,
    filterPlaceholder: String,
) : ContainerWidget(id, Axis.VERTICAL), ActionInterceptor {

    init {
        gap = GAP
        crossAlign = Align.STRETCH
    }

    /**
     * The columns, in order.
     */
    var columns: List<DataTableColumn> = emptyList()

    /**
     * The current view.
     */
    var view: DataTableView = DataTableView()
        private set

    /**
     * The filter input, or `null` if the table has no filter.
     */
    val filter: DataTableFilterWidget? = filterColumn?.let { DataTableFilterWidget("$id:filter", this, filterPlaceholder) }

    /**
     * The table that shows the heading row and the rows.
     */
    val table: TableWidget = TableWidget("$id:table")

    /**
     * The header section of [table].
     */
    private val header = TableSectionWidget("$id:header", TableSection.HEADER)

    /**
     * The row of column headings.
     */
    private val headRow = TableRowWidget("$id:head_row", false)

    /**
     * The body section of [table], holding every row.
     */
    val body: TableSectionWidget = TableSectionWidget("$id:body", TableSection.BODY)

    /**
     * The text shown when no row matches the filter.
     */
    val empty: TextWidget = TextWidget("$id:empty", TextKind.MUTED, json("Keine Ergebnisse."), 0, Align.CENTER)

    /**
     * The footer with the selection count and the page controls.
     */
    private val footer = ContainerWidget("$id:footer", Axis.HORIZONTAL).apply {
        gap = GAP
        crossAlign = Align.CENTER
    }

    /**
     * The text that counts the selected rows.
     */
    val selectionText: TextWidget = TextWidget("$id:selection", TextKind.MUTED, "", 0, Align.START).apply { width = Sizing.grow() }

    /**
     * The text that names the shown page.
     */
    val pageText: TextWidget = TextWidget("$id:page", TextKind.SMALL, "", 0, Align.START)

    /**
     * The button that shows the previous page.
     */
    val previous: ButtonWidget = ButtonWidget("$id:previous", json("Zurück"), null, false, ButtonVariant.OUTLINE, ButtonSize.SM)

    /**
     * The button that shows the next page.
     */
    val next: ButtonWidget = ButtonWidget("$id:next", json("Weiter"), null, false, ButtonVariant.OUTLINE, ButtonSize.SM)

    /**
     * The checkbox in the heading row that selects or clears every matching row.
     */
    val selectAll: DataTableCheckboxWidget = DataTableCheckboxWidget("$id:select_all") { context -> toggleAll(context) }

    /**
     * Every row, in the order it is currently shown.
     */
    val rows: List<DataTableRowWidget> get() = body.childList.filterIsInstance<DataTableRowWidget>()

    /**
     * The rows that match the filter, in the current sort order.
     */
    var matching: List<DataTableRowWidget> = emptyList()
        private set

    /**
     * The number of pages of the matching rows, at least one.
     */
    val pages: Int get() = if (pageSize <= 0) 1 else maxOf(1, (matching.size + pageSize - 1) / pageSize)

    /**
     * The view as JSON.
     */
    override val inputValue: String get() = view.toJson()

    /**
     * Sets up the parts of the table around its rows.
     *
     * @param rows the rows, in the order the server sent them
     */
    fun build(rows: List<DataTableRowWidget>) {
        childList.clear()
        filter?.let { input -> childList += ContainerWidget("$id:filter_row", Axis.HORIZONTAL).apply { childList += input } }
        childList += table
        table.fixedColumns = if (selectable) setOf(0) else emptySet()
        childList += empty
        childList += footer
        table.childList.clear()
        table.childList += header
        table.childList += body
        header.childList.clear()
        header.childList += headRow
        body.childList.clear()
        body.childList += rows
        footer.childList.clear()
        footer.childList += selectionText
        footer.childList += pageText
        footer.childList += previous
        footer.childList += next
        headRow.childList.clear()
        if (selectable) headRow.childList += TableCellWidget("$id:select_head", true, Align.START).apply { childList += selectAll }
        columns.forEach { column ->
            val heading = if (column.sortable) {
                ButtonWidget("$id:sort_${column.key}", column.header, SORT_ICON, false, ButtonVariant.GHOST, ButtonSize.SM)
            } else {
                LabelWidget("$id:label_${column.key}", column.header)
            }
            headRow.childList += TableCellWidget("$id:head_${column.key}", true, column.align).apply { childList += heading }
        }
        refresh()
    }

    /**
     * Shows the view the server names, without reporting the change. A value that is not a view
     * is ignored.
     *
     * @param value the view as JSON
     */
    override fun applyValue(value: String) {
        view = DataTableView.parse(value) ?: return
        filter?.edit?.text = view.filter
        refresh()
    }

    /**
     * Takes over rows that were inserted into the data table itself, gives every row its cell
     * alignment and, when selectable, its checkbox cell, then sorts, filters and pages the rows
     * for the view and updates the heading, selection and page texts.
     */
    fun refresh() {
        val strays = childList.filterIsInstance<DataTableRowWidget>()
        if (strays.isNotEmpty()) {
            childList.removeAll(strays)
            body.childList += strays
        }
        rows.forEach { prepare(it) }
        val filterIndex = columns.indexOfFirst { it.key == filterColumn }
        val text = view.filter.trim()
        val kept = rows.filter { row -> text.isEmpty() || filterIndex < 0 || row.key(filterIndex).contains(text, ignoreCase = true) }
        val sortIndex = columns.indexOfFirst { it.key == view.sort && it.sortable }
        val ordered = if (sortIndex < 0) serverOrder(kept) else kept.sortedWith { a, b -> compareKeys(a.key(sortIndex), b.key(sortIndex)) }.let { if (view.desc) it.reversed() else it }
        matching = ordered
        val page = view.page.coerceIn(0, pages - 1)
        if (page != view.page) view = view.copy(page = page)
        val shown = if (pageSize <= 0) ordered else ordered.drop(page * pageSize).take(pageSize)
        val others = rows.filter { it !in ordered }
        body.childList.removeAll(rows.toSet())
        body.childList += ordered
        body.childList += others
        val selected = view.selected.toSet()
        rows.forEach { row ->
            row.hidden = row !in shown
            row.selected = row.id in selected
            row.checkbox?.checked = row.id in selected
        }
        val choosable = ordered.filter { it.selectable }
        val chosen = choosable.count { it.id in selected }
        selectAll.checked = choosable.isNotEmpty() && chosen == choosable.size
        selectAll.indeterminate = chosen in 1 until choosable.size
        selectAll.enabled = choosable.isNotEmpty()
        empty.hidden = ordered.isNotEmpty()
        selectionText.text = if (selectable) json("${ordered.count { it.id in selected }} von ${ordered.size} Zeile(n) ausgewählt.") else ""
        pageText.text = json("Seite ${page + 1} von $pages")
        pageText.hidden = pageSize <= 0
        previous.hidden = pageSize <= 0
        next.hidden = pageSize <= 0
        previous.enabled = page > 0
        next.enabled = page < pages - 1
        columns.forEach { column ->
            val button = headRow.childList.flatMap { it.children }.firstOrNull { it.id == "$id:sort_${column.key}" } as? ButtonWidget ?: return@forEach
            button.icon = when {
                view.sort != column.key -> SORT_ICON
                view.desc -> "arrow-down"
                else -> "arrow-up"
            }
        }
    }

    /**
     * Gives a row its cell alignment from the columns and, when the table is selectable, a
     * leading cell with its checkbox.
     *
     * @param row the row
     */
    private fun prepare(row: DataTableRowWidget) {
        row.cells.forEachIndexed { index, cell -> cell.crossAlign = columns.getOrNull(index)?.align ?: Align.START }
        if (selectable && row.checkbox == null) {
            val checkbox = DataTableCheckboxWidget("${row.id}:check") { context -> toggleRow(context, row) }
            checkbox.enabled = row.selectable
            row.childList.add(0, TableCellWidget("${row.id}:select", false, Align.START).apply { childList += checkbox })
        }
    }

    /**
     * Returns rows in the order the server sent them.
     *
     * @param rows the rows
     * @return the rows ordered by their position among the rows as first built
     */
    private fun serverOrder(rows: List<DataTableRowWidget>): List<DataTableRowWidget> = rows.sortedBy { it.order }

    /**
     * Changes the view, lays the screen out again and reports the change.
     *
     * @param context the screen showing the widget
     * @param changed the new view
     */
    fun change(context: UiContext, changed: DataTableView) {
        if (!enabled || changed == view) return
        view = changed
        refresh()
        markChanged(context, immediate = true)
        context.requestLayout()
    }

    /**
     * Sorts by a column: ascending first, then descending, then in the server's order, going back
     * to the first page.
     *
     * @param context the screen showing the widget
     * @param key the key of the column
     */
    fun cycleSort(context: UiContext, key: String) {
        val changed = when {
            view.sort != key -> view.copy(sort = key, desc = false)
            !view.desc -> view.copy(desc = true)
            else -> view.copy(sort = null, desc = false)
        }
        change(context, changed.copy(page = 0))
    }

    /**
     * Filters the rows by a text, going back to the first page.
     *
     * @param context the screen showing the widget
     * @param text the filter text
     */
    fun filterChanged(context: UiContext, text: String) = change(context, view.copy(filter = text, page = 0))

    /**
     * Selects or clears a row.
     *
     * @param context the screen showing the widget
     * @param row the row
     */
    fun toggleRow(context: UiContext, row: DataTableRowWidget) {
        if (!row.selectable) return
        val selected = if (row.id in view.selected) view.selected - row.id else view.selected + row.id
        change(context, view.copy(selected = rows.map { it.id }.filter { it in selected }.let { order -> sortedByServer(order) }))
    }

    /**
     * Selects every matching, selectable row, or clears them all when they already are.
     *
     * @param context the screen showing the widget
     */
    fun toggleAll(context: UiContext) {
        val choosable = matching.filter { it.selectable }.map { it.id }
        val selected = if (choosable.all { it in view.selected }) view.selected - choosable.toSet() else (view.selected + choosable).distinct()
        change(context, view.copy(selected = sortedByServer(selected)))
    }

    /**
     * Orders row ids by the rows' positions as the server sent them.
     *
     * @param ids the ids
     * @return the ids in row order
     */
    private fun sortedByServer(ids: List<String>): List<String> {
        val order = rows.associate { it.id to it.order }
        return ids.distinct().sortedBy { order[it] ?: Int.MAX_VALUE }
    }

    /**
     * Handles the actions of the heading buttons and the page buttons.
     *
     * @param context the screen showing the widget
     * @param widget the widget whose action fired
     * @param via the child holding the widget
     * @return whether the action came from one of the table's own buttons
     */
    override fun interceptAction(context: UiContext, widget: Widget, via: Widget): Boolean {
        when {
            widget === previous -> change(context, view.copy(page = (view.page - 1).coerceAtLeast(0)))
            widget === next -> change(context, view.copy(page = (view.page + 1).coerceAtMost(pages - 1)))
            widget.id.startsWith("$id:sort_") -> cycleSort(context, widget.id.removePrefix("$id:sort_"))
            else -> return false
        }
        return true
    }

    /**
     * Creates the layout after refreshing, so that inserted rows are taken over first.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        if (childList.any { it is DataTableRowWidget }) refresh()
        return super.createLayout(measurer)
    }

    /**
     * Holds the data table metrics and helpers.
     */
    companion object {
        /**
         * The space between the filter, the table and the footer.
         */
        const val GAP: Int = 8

        /**
         * The icon of a heading that does not sort.
         */
        const val SORT_ICON: String = "arrow-up-down"

        /**
         * Compares two sort keys: as numbers when both are numbers, otherwise as texts ignoring
         * case.
         *
         * @param a the first key
         * @param b the second key
         * @return a negative number, zero or a positive number as [a] sorts before, with or after
         *         [b]
         */
        fun compareKeys(a: String, b: String): Int {
            val x = a.trim().replace(',', '.').toDoubleOrNull()
            val y = b.trim().replace(',', '.').toDoubleOrNull()
            return if (x != null && y != null) x.compareTo(y) else String.CASE_INSENSITIVE_ORDER.compare(a, b)
        }

        /**
         * Returns a plain text as component JSON.
         *
         * @param text the text
         * @return the JSON string
         */
        fun json(text: String): String = "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
    }
}

/**
 * A row of a data table: its cells, one per column, after a checkbox cell when the table is
 * selectable.
 *
 * @param id the id of the widget, also the id the view's selection names it by
 * @property selectable whether the row can be selected
 */
class DataTableRowWidget(id: String, val selectable: Boolean) : TableRowWidget(id, false) {

    /**
     * The position of the row among the rows the server sent.
     */
    var order: Int = 0

    /**
     * The cells of the columns, in order.
     */
    val cells: List<DataTableCellWidget> get() = childList.filterIsInstance<DataTableCellWidget>()

    /**
     * The checkbox of the row, or `null` if it has none.
     */
    val checkbox: DataTableCheckboxWidget?
        get() = childList.firstOrNull { it.id == "$id:select" }?.children?.firstOrNull() as? DataTableCheckboxWidget

    /**
     * Returns the sort key of a column's cell.
     *
     * @param column the index of the column
     * @return the key, or an empty text if the row has no such cell
     */
    fun key(column: Int): String = cells.getOrNull(column)?.sortKey.orEmpty()
}

/**
 * A cell of a data table row: its content and the key it sorts and filters by.
 *
 * @param id the id of the widget
 * @property sortKey the key the cell sorts and filters by
 */
class DataTableCellWidget(id: String, val sortKey: String) : TableCellWidget(id, false, Align.START)

/**
 * A checkbox of a data table that reports no value of its own but calls its table when toggled.
 * The select-all checkbox shows a dash while only some rows are selected.
 *
 * @param id the id of the widget
 * @param onToggle called with the screen when the player toggles the checkbox
 */
class DataTableCheckboxWidget(id: String, private val onToggle: (UiContext) -> Unit) : CheckboxWidget(id) {

    /**
     * Whether the checkbox shows a dash because only some rows are selected.
     */
    var indeterminate: Boolean = false

    /**
     * Reports no value, since the table's view holds the selection.
     */
    override val inputValue: String? get() = null

    /**
     * Asks the table to toggle, instead of toggling itself.
     *
     * @param context the screen showing the widget
     */
    override fun toggle(context: UiContext) = onToggle(context)

    /**
     * Draws the checkbox, or a filled box with a dash while [indeterminate].
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        if (!indeterminate || checked) return super.render(ui, context, mouseX, mouseY)
        val size = UiMetrics.CHECKBOX_SIZE
        val box = Rect(bounds.x, bounds.y + (bounds.height - size) / 2, size, size)
        ui.fillRounded(box, if (enabled) ui.tokens.primary else ui.disabled(ui.tokens.primary), RADIUS)
        ui.icon("minus", Rect(box.x + 1, box.y + 1, size - 2, size - 2), ui.tokens.primaryForeground)
    }

    /**
     * Holds the checkbox metrics.
     */
    private companion object {
        /**
         * The corner radius of the box.
         */
        const val RADIUS: Int = 2
    }
}

/**
 * The filter input of a data table: a text input that reports no value of its own but tells its
 * table every change of its text.
 *
 * @param id the id of the widget
 * @param owner the data table
 * @param placeholder the placeholder as component JSON
 */
class DataTableFilterWidget(id: String, private val owner: DataTableWidget, placeholder: String) :
    TextInputWidget(id, TextEditState("", TextFilter.maxLength(MAX_LENGTH)), placeholder) {

    init {
        width = Sizing.fixed(WIDTH)
    }

    /**
     * Reports no value, since the table's view holds the filter.
     */
    override val inputValue: String? get() = null

    /**
     * Edits the text and tells the table when it changed.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        val before = edit.text
        val handled = super.keyPressed(context, event)
        if (edit.text != before) owner.filterChanged(context, edit.text)
        return handled
    }

    /**
     * Types a character and tells the table.
     *
     * @param context the screen showing the widget
     * @param event the character event
     * @return whether the character was handled
     */
    override fun charTyped(context: UiContext, event: CharacterEvent): Boolean {
        val before = edit.text
        val handled = super.charTyped(context, event)
        if (edit.text != before) owner.filterChanged(context, edit.text)
        return handled
    }

    /**
     * Holds the filter metrics.
     */
    companion object {
        /**
         * The width of the filter input.
         */
        const val WIDTH: Int = 160

        /**
         * The longest filter text.
         */
        const val MAX_LENGTH: Int = 100
    }
}
