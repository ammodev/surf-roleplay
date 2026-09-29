package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.ScreenPanel
import dev.slne.surf.roleplay.fabric.ui.ScreenPanelListener
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.DirectionNode
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.LayoutDirection
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.TableCaptionNode
import dev.slne.surf.roleplay.protocol.screen.TableCellNode
import dev.slne.surf.roleplay.protocol.screen.TableNode
import dev.slne.surf.roleplay.protocol.screen.TableRowNode
import dev.slne.surf.roleplay.protocol.screen.TableSection
import dev.slne.surf.roleplay.protocol.screen.TableSectionNode
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for tables in the mod.
 */
class TableWidgetsTest {

    /**
     * A measurer in which every character is 5 pixels wide.
     */
    private val measurer = object : TextMeasurer {
        override val lineHeight: Int = 9
        override fun width(json: String): Int = json.length * 5
        override fun plainWidth(text: String): Int = text.length * 5
    }

    /**
     * A listener that ignores everything.
     */
    private val listener = object : ScreenPanelListener {
        override fun actionTriggered(panel: ScreenPanel, widget: Widget) = Unit
        override fun closeRequested(panel: ScreenPanel) = Unit
    }

    /**
     * Creates a laid-out panel around nodes placed in a column 300 wide.
     *
     * @param nodes the nodes
     * @return the panel
     */
    private fun panel(vararg nodes: ScreenNode): ScreenPanel =
        ScreenPanel("T", WidgetFactory.create(ColumnNode("root", width = Sizing.fixed(300), children = nodes.toList())), true, listener, PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)))
            .also { it.layoutIfNeeded(measurer, 400, 300) }

    /**
     * Returns the widget with an id.
     *
     * @param panel the panel
     * @param id the id
     * @return the widget
     */
    private fun widget(panel: ScreenPanel, id: String): Widget = WidgetTree.find(panel.root, id)!!

    /**
     * Creates a row of cells holding labels.
     *
     * @param id the id of the row
     * @param head whether the cells are heads
     * @param texts the texts of the cells
     * @return the row
     */
    private fun row(id: String, head: Boolean, vararg texts: String) = TableRowNode(
        id,
        children = texts.mapIndexed { index, text ->
            TableCellNode("${id}_$index", head = head, align = if (index == 2) Align.END else Align.START, children = listOf(LabelNode("${id}_${index}_text", text = text)))
        },
    )

    /**
     * Creates a table with a header, two body rows, a footer and a caption.
     *
     * @return the node
     */
    private fun table() = TableNode(
        "table",
        children = listOf(
            TableSectionNode("header", section = TableSection.HEADER, children = listOf(row("head", true, "Nr", "Einheit", "Betrag"))),
            TableSectionNode("body", children = listOf(row("one", false, "1", "RTW", "10"), row("two", false, "22", "Notarzteinsatz", "250"))),
            TableSectionNode("footer", section = TableSection.FOOTER, children = listOf(row("sum", false, "", "Summe", "260"))),
            TableCaptionNode("caption", text = "Einsätze"),
        ),
    )

    /**
     * Verifies that the table fills its width and that every column starts at the same x in
     * every row, with the widest cell setting the column's share.
     */
    @Test
    fun `columns line up across rows`() {
        val panel = panel(table())
        val table = assertIs<TableWidget>(widget(panel, "table"))

        assertEquals(300, table.bounds.width)
        assertEquals(300, table.columnWidths.sum())
        listOf("head", "one", "two", "sum").forEach { row ->
            assertEquals(widget(panel, "head_1").bounds.x, widget(panel, "${row}_1").bounds.x)
            assertEquals(widget(panel, "head_2").bounds.width, widget(panel, "${row}_2").bounds.width)
        }
        assertTrue(table.columnWidths[1] > table.columnWidths[0])
    }

    /**
     * Verifies that rows stack below each other, that head rows are at least the head height,
     * and that the caption sits below the rows.
     */
    @Test
    fun `rows stack with the caption below`() {
        val panel = panel(table())
        val head = widget(panel, "head").bounds
        val one = widget(panel, "one").bounds
        val sum = widget(panel, "sum").bounds

        assertTrue(head.height >= TableCellWidget.HEAD_HEIGHT)
        assertEquals(head.bottom, one.y)
        assertEquals(widget(panel, "two").bounds.bottom, sum.y)
        assertEquals(sum.bottom + TableWidget.BELOW_GAP, widget(panel, "caption").bounds.y)
    }

    /**
     * Verifies that cells place their content by their alignment, and that the last body row
     * has no border below while other rows do.
     */
    @Test
    fun `cells align and borders skip the last body row`() {
        val panel = panel(table())
        val cell = widget(panel, "one_2").bounds
        val text = widget(panel, "one_2_text").bounds

        assertEquals(cell.right - TableCellWidget.PADDING, text.right)
        assertEquals(widget(panel, "one_0").bounds.x + TableCellWidget.PADDING, widget(panel, "one_0_text").bounds.x)
        assertTrue(assertIs<TableRowWidget>(widget(panel, "one")).bottomBorder)
        assertFalse(assertIs<TableRowWidget>(widget(panel, "two")).bottomBorder)
        assertTrue(assertIs<TableRowWidget>(widget(panel, "head")).bottomBorder)
    }

    /**
     * Verifies that a table inside a right-to-left direction places its first column at the
     * right.
     */
    @Test
    fun `right-to-left tables start at the right`() {
        val panel = panel(DirectionNode("dir", width = Sizing.grow(), direction = LayoutDirection.RTL, children = listOf(table())))

        assertEquals(widget(panel, "table").bounds.right, widget(panel, "one_0").bounds.right)
        assertTrue(widget(panel, "one_2").bounds.x < widget(panel, "one_1").bounds.x)
    }
}
