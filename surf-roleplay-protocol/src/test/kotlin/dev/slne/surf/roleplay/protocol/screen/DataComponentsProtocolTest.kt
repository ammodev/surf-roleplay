package dev.slne.surf.roleplay.protocol.screen

import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.ProtocolCodec
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the data, chat and chart components on the wire.
 */
class DataComponentsProtocolTest {

    /**
     * Sends a tree through a screen open and returns the decoded tree.
     *
     * @param root the root of the tree
     * @return the decoded root
     */
    private fun roundTrip(root: ScreenNode): ScreenNode {
        val open = ScreenOpen(sessionId = 1, title = "{}", body = WidgetScreenBody(root))
        val decoded = ProtocolCodec.decode(Packets.SCREEN_OPEN.channel, ProtocolCodec.encode(Packets.SCREEN_OPEN, open)) as ScreenOpen
        return (decoded.body as WidgetScreenBody).root
    }

    /**
     * Verifies that a table with every part survives a round trip.
     */
    @Test
    fun `tables round-trip`() {
        val root = TableNode(
            "table",
            width = Sizing.grow(),
            children = listOf(
                TableCaptionNode("caption", text = "\"Einsätze\""),
                TableSectionNode(
                    "header",
                    section = TableSection.HEADER,
                    children = listOf(TableRowNode("head_row", children = listOf(TableCellNode("head", head = true, children = listOf(LabelNode("h")))))),
                ),
                TableSectionNode(
                    "body",
                    children = listOf(TableRowNode("row", selected = true, children = listOf(TableCellNode("cell", align = Align.END, children = listOf(LabelNode("c")))))),
                ),
                TableSectionNode("footer", section = TableSection.FOOTER),
            ),
        )

        assertEquals(root, roundTrip(root))
    }

    /**
     * Verifies that a data table with columns, rows and cells survives a round trip.
     */
    @Test
    fun `data tables round-trip`() {
        val root = DataTableNode(
            "units",
            pageSize = 5,
            selectable = true,
            filterColumn = "name",
            filterPlaceholder = "\"Filtern\"",
            value = DataTableView(sort = "name").toJson(),
            notifyChange = true,
            children = listOf(
                DataTableColumnNode("name_column", key = "name", header = "\"Name\"", sortable = true),
                DataTableColumnNode("amount_column", key = "amount", header = "\"Betrag\"", align = Align.END),
                DataTableRowNode("r1", selectable = false, children = listOf(DataTableCellNode("r1_name", sortKey = "RTW", children = listOf(LabelNode("t"))), DataTableCellNode("r1_amount", sortKey = "10"))),
            ),
        )

        assertEquals(root, roundTrip(root))
    }

    /**
     * Verifies that a view survives writing and reading, including texts that need escapes.
     */
    @Test
    fun `data table views round-trip as json`() {
        val view = DataTableView(sort = "na\"me", desc = true, filter = "a,b;c\\\n{}\u0001", page = 3, selected = listOf("r1", "r\"2"))

        assertEquals(view, DataTableView.parse(view.toJson()))
        assertEquals(DataTableView(), DataTableView.parse(""))
        assertEquals(DataTableView(page = 2), DataTableView.parse(" { \"page\" : 2 } "))
        assertEquals(DataTableView(filter = "ä"), DataTableView.parse("{\"filter\":\"\\u00e4\"}"))
    }

    /**
     * Verifies that texts that are not a valid view are rejected.
     */
    @Test
    fun `invalid data table views are rejected`() {
        listOf(
            "[]", "{", "{\"page\":-1}", "{\"page\":1.5}", "{\"page\":\"1\"}", "{\"desc\":1}",
            "{\"selected\":[1]}", "{\"other\":true}", "{} {}", "{\"sort\":\"a\"", "null",
        ).forEach { assertEquals(null, DataTableView.parse(it), it) }
    }

    /**
     * Verifies that a chart with series and every option survives a round trip.
     */
    @Test
    fun `charts round-trip`() {
        val root = ChartNode(
            "chart",
            height = Sizing.fixed(120),
            kind = ChartKind.AREA,
            categories = listOf("\"Jan\"", "\"Feb\""),
            series = listOf(ChartSeries("calls", "\"Einsätze\"", 2, listOf(3.0, 4.5)), ChartSeries("units", "\"Einheiten\"", 5, listOf(1.0, 0.0))),
            categoryColors = listOf(3, 1),
            stacked = true,
            horizontal = true,
            curve = ChartCurve.STEP,
            dots = true,
            grid = false,
            xAxis = false,
            yAxis = true,
            legend = true,
            tooltip = false,
            indicator = ChartIndicator.DASHED,
            donut = true,
            labels = true,
        )

        assertEquals(root, roundTrip(root))
    }
}
