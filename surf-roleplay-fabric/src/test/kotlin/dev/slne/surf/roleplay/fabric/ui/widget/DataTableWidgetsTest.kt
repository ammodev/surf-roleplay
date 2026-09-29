package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.ScreenPanel
import dev.slne.surf.roleplay.fabric.ui.ScreenPanelListener
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.DataTableCellNode
import dev.slne.surf.roleplay.protocol.screen.DataTableColumnNode
import dev.slne.surf.roleplay.protocol.screen.DataTableNode
import dev.slne.surf.roleplay.protocol.screen.DataTableRowNode
import dev.slne.surf.roleplay.protocol.screen.DataTableView
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import net.minecraft.client.input.CharacterEvent
import org.lwjgl.glfw.GLFW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for data tables in the mod.
 */
class DataTableWidgetsTest {

    /**
     * A measurer in which every character is 5 pixels wide.
     */
    private val measurer = object : TextMeasurer {
        override val lineHeight: Int = 9
        override fun width(json: String): Int = json.length * 5
        override fun plainWidth(text: String): Int = text.length * 5
    }

    /**
     * The values of the data table's reported changes, in order.
     */
    private val changes = mutableListOf<String>()

    /**
     * The widgets whose actions reached the listener, in order.
     */
    private val actions = mutableListOf<String>()

    /**
     * A listener that records actions and changes.
     */
    private val listener = object : ScreenPanelListener {
        override fun actionTriggered(panel: ScreenPanel, widget: Widget) {
            actions += widget.id
        }
        override fun closeRequested(panel: ScreenPanel) = Unit
        override fun valueChanged(panel: ScreenPanel, widget: Widget) {
            changes += widget.inputValue.orEmpty()
        }
    }

    /**
     * The units of the table: id, name and amount.
     */
    private val units = listOf(
        Triple("r1", "RTW", "30"),
        Triple("r2", "NEF", "5"),
        Triple("r3", "KTW", "120"),
        Triple("r4", "RTW Nord", "12"),
        Triple("r5", "Leitstelle", "7"),
    )

    /**
     * Creates a data table of the units with sortable name and amount columns.
     *
     * @param pageSize the number of rows on a page
     * @param value the view the table starts with
     * @return the node
     */
    private fun dataTable(pageSize: Int = 10, value: String = "") = DataTableNode(
        "units",
        width = Sizing.fixed(360),
        pageSize = pageSize,
        selectable = true,
        filterColumn = "name",
        filterPlaceholder = "\"Filtern\"",
        value = value,
        notifyChange = true,
        children = listOf(
            DataTableColumnNode("name_column", key = "name", header = "\"Name\"", sortable = true),
            DataTableColumnNode("amount_column", key = "amount", header = "\"Betrag\"", sortable = true, align = Align.END),
            DataTableColumnNode("note_column", key = "note", header = "\"Notiz\""),
        ) + units.map { (id, name, amount) ->
            DataTableRowNode(
                id,
                selectable = id != "r5",
                children = listOf(
                    DataTableCellNode("${id}_name", sortKey = name, children = listOf(LabelNode("${id}_name_text", text = name))),
                    DataTableCellNode("${id}_amount", sortKey = amount, children = listOf(LabelNode("${id}_amount_text", text = amount))),
                    DataTableCellNode("${id}_note", children = listOf(LabelNode("${id}_note_text", text = "-"))),
                ),
            )
        },
    )

    /**
     * Creates a laid-out panel around a data table.
     *
     * @param table the table node
     * @return the panel
     */
    private fun panel(table: DataTableNode): ScreenPanel =
        ScreenPanel("T", WidgetFactory.create(ColumnNode("root", children = listOf(table))), true, listener, PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)))
            .also { it.layoutIfNeeded(measurer, 500, 400) }

    /**
     * Clicks the middle of a widget and lays the panel out again.
     *
     * @param panel the panel
     * @param id the id of the widget
     */
    private fun click(panel: ScreenPanel, id: String) {
        val b = widget(panel, id).bounds
        panel.mouseClicked(b.x + b.width / 2.0, b.y + b.height / 2.0, GLFW.GLFW_MOUSE_BUTTON_LEFT)
        panel.layoutIfNeeded(measurer, 500, 400)
    }

    /**
     * Returns the widget with an id.
     *
     * @param panel the panel
     * @param id the id
     * @return the widget
     */
    private fun widget(panel: ScreenPanel, id: String): Widget = WidgetTree.find(panel.root, id)!!

    /**
     * Returns the data table of a panel.
     *
     * @param panel the panel
     * @return the data table
     */
    private fun table(panel: ScreenPanel): DataTableWidget = assertIs<DataTableWidget>(widget(panel, "units"))

    /**
     * Returns the ids of the shown rows, top to bottom.
     *
     * @param panel the panel
     * @return the ids
     */
    private fun shown(panel: ScreenPanel): List<String> = table(panel).rows.filter { !it.hidden }.sortedBy { it.bounds.y }.map { it.id }

    /**
     * Verifies that a heading click sorts ascending, then descending, then in the server's
     * order, comparing numbers as numbers, without sending an action.
     */
    @Test
    fun `headings cycle the sort`() {
        val panel = panel(dataTable())

        click(panel, "units:sort_amount")
        assertEquals(listOf("r2", "r5", "r4", "r1", "r3"), shown(panel))
        click(panel, "units:sort_amount")
        assertEquals(listOf("r3", "r1", "r4", "r5", "r2"), shown(panel))
        click(panel, "units:sort_amount")
        assertEquals(listOf("r1", "r2", "r3", "r4", "r5"), shown(panel))
        click(panel, "units:sort_name")
        assertEquals(listOf("r3", "r5", "r2", "r1", "r4"), shown(panel))

        assertEquals(DataTableView(sort = "name"), DataTableView.parse(table(panel).inputValue))
        assertEquals(4, changes.size)
        assertTrue(actions.isEmpty())
    }

    /**
     * Verifies that typing in the filter keeps the rows whose filter column contains the text,
     * ignoring case, and shows the empty text when nothing matches.
     */
    @Test
    fun `the filter keeps matching rows`() {
        val panel = panel(dataTable())
        panel.focus(widget(panel, "units:filter"))

        "rtw".forEach { panel.charTyped(CharacterEvent(it.code)) }
        panel.layoutIfNeeded(measurer, 500, 400)
        assertEquals(listOf("r1", "r4"), shown(panel))
        assertEquals("rtw", DataTableView.parse(table(panel).inputValue)?.filter)
        assertTrue(table(panel).empty.hidden)

        panel.charTyped(CharacterEvent('x'.code))
        panel.layoutIfNeeded(measurer, 500, 400)
        assertEquals(emptyList(), shown(panel))
        assertFalse(table(panel).empty.hidden)
    }

    /**
     * Verifies that rows are paged, that the page buttons move between pages and stop at the
     * ends, and that the page text names the page.
     */
    @Test
    fun `pages move with the buttons`() {
        val panel = panel(dataTable(pageSize = 2))

        assertEquals(listOf("r1", "r2"), shown(panel))
        assertFalse(table(panel).previous.enabled)
        assertEquals("\"Seite 1 von 3\"", table(panel).pageText.text)
        click(panel, "units:next")
        click(panel, "units:next")
        assertEquals(listOf("r5"), shown(panel))
        assertFalse(table(panel).next.enabled)
        click(panel, "units:previous")
        assertEquals(listOf("r3", "r4"), shown(panel))
        assertEquals(1, DataTableView.parse(table(panel).inputValue)?.page)
    }

    /**
     * Verifies that row checkboxes select rows, that the heading checkbox selects every
     * selectable matching row and clears them again, and that the footer counts them.
     */
    @Test
    fun `checkboxes select rows`() {
        val panel = panel(dataTable())

        click(panel, "r3:check")
        assertEquals(listOf("r3"), DataTableView.parse(table(panel).inputValue)?.selected)
        assertTrue(assertIs<DataTableRowWidget>(widget(panel, "r3")).selected)
        assertTrue(table(panel).selectAll.indeterminate)

        click(panel, "units:select_all")
        assertEquals(listOf("r1", "r2", "r3", "r4"), DataTableView.parse(table(panel).inputValue)?.selected)
        assertEquals("\"4 von 5 Zeile(n) ausgewählt.\"", table(panel).selectionText.text)
        click(panel, "r5:check")
        assertEquals(4, DataTableView.parse(table(panel).inputValue)?.selected?.size)

        click(panel, "units:select_all")
        assertEquals(emptyList(), DataTableView.parse(table(panel).inputValue)?.selected)
    }

    /**
     * Verifies that a view from the server is applied, including the filter text, and that the
     * table's own controls submit no values of their own.
     */
    @Test
    fun `server views apply and only the view submits`() {
        val panel = panel(dataTable(pageSize = 2, value = DataTableView(sort = "name", desc = true, filter = "t", page = 1, selected = listOf("r1")).toJson()))

        assertEquals(listOf("r5", "r3"), shown(panel))
        assertEquals("t", assertIs<DataTableFilterWidget>(widget(panel, "units:filter")).edit.text)
        assertTrue(assertIs<DataTableRowWidget>(widget(panel, "r1")).selected)
        assertEquals(listOf("units"), WidgetTree.inputValues(panel.root).map { it.widgetId })
    }
}
