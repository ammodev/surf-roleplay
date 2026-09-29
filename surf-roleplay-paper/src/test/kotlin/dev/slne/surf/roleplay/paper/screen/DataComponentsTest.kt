package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.protocol.screen.ScreenPatch
import dev.slne.surf.roleplay.protocol.screen.InsertNode
import dev.slne.surf.roleplay.protocol.screen.ChatMessageNode
import dev.slne.surf.roleplay.protocol.screen.ChatViewNode
import dev.slne.surf.roleplay.api.client.common.screen.text
import dev.slne.surf.roleplay.api.client.common.screen.chatView
import dev.slne.surf.roleplay.api.client.common.screen.chatMessage
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.protocol.screen.ChartIndicator as NodeChartIndicator
import dev.slne.surf.roleplay.protocol.screen.ChartCurve as NodeChartCurve
import dev.slne.surf.roleplay.protocol.screen.ChartKind as NodeChartKind
import dev.slne.surf.roleplay.protocol.screen.ChartNode
import dev.slne.surf.roleplay.api.client.common.screen.chart
import dev.slne.surf.roleplay.api.client.common.screen.ChartSeries
import dev.slne.surf.roleplay.api.client.common.screen.ChartKind
import dev.slne.surf.roleplay.api.client.common.screen.ChartIndicator
import dev.slne.surf.roleplay.api.client.common.screen.ChartCurve
import dev.slne.surf.roleplay.protocol.screen.ScreenInputChange
import dev.slne.surf.roleplay.protocol.screen.DataTableRowNode
import dev.slne.surf.roleplay.protocol.screen.DataTableNode
import dev.slne.surf.roleplay.protocol.screen.DataTableColumnNode
import dev.slne.surf.roleplay.protocol.screen.DataTableCellNode
import dev.slne.surf.roleplay.api.client.common.screen.dataTableRow
import dev.slne.surf.roleplay.api.client.common.screen.dataTableColumn
import dev.slne.surf.roleplay.api.client.common.screen.dataTableCell
import dev.slne.surf.roleplay.api.client.common.screen.dataTable
import dev.slne.surf.roleplay.api.client.common.screen.DataTableView
import dev.slne.surf.roleplay.api.client.common.screen.screen
import dev.slne.surf.roleplay.api.client.common.screen.table
import dev.slne.surf.roleplay.api.client.common.screen.tableBody
import dev.slne.surf.roleplay.api.client.common.screen.tableCaption
import dev.slne.surf.roleplay.api.client.common.screen.tableCell
import dev.slne.surf.roleplay.api.client.common.screen.tableFooter
import dev.slne.surf.roleplay.api.client.common.screen.tableHead
import dev.slne.surf.roleplay.api.client.common.screen.tableHeader
import dev.slne.surf.roleplay.api.client.common.screen.tableRow
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.TableCaptionNode
import dev.slne.surf.roleplay.protocol.screen.TableCellNode
import dev.slne.surf.roleplay.protocol.screen.TableNode
import dev.slne.surf.roleplay.protocol.screen.TableRowNode
import dev.slne.surf.roleplay.protocol.screen.TableSection
import dev.slne.surf.roleplay.protocol.screen.TableSectionNode
import dev.slne.surf.roleplay.protocol.screen.WidgetScreenBody
import net.kyori.adventure.text.Component
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for the data, chat and chart components in the API and on Paper.
 */
class DataComponentsTest {

    /**
     * The packets sent to the client.
     */
    private val sent = mutableListOf<Packet>()

    /**
     * The state under test.
     */
    private val state = PlayerScreenState(
        UUID.randomUUID(),
        object : ScreenPacketSender {
            /**
             * Records a packet.
             *
             * @param type the packet type
             * @param packet the packet
             */
            override fun <P : Packet> send(type: PacketType<P>, packet: P) {
                sent += packet
            }
        },
        ActionRateLimiter(100),
    )

    /**
     * Returns the root node of the last opened screen.
     *
     * @return the root node
     */
    private fun root(): ScreenNode = assertIs<WidgetScreenBody>(assertIs<ScreenOpen>(sent.last()).body).root

    /**
     * Verifies that a table maps to its nodes, with sections, a selected row, head cells, text
     * cells and an aligned cell.
     */
    @Test
    fun `tables map to their nodes`() {
        state.open(
            screen(Component.text("Tabelle")) {
                table("table") {
                    tableHeader("header") { tableRow("head_row") { tableHead("head", Component.text("Einheit")) } }
                    tableBody("body") { tableRow("row", selected = true) { tableCell("cell", Component.text("10"), Alignment.END) } }
                    tableFooter("footer") { tableRow("sum_row") { tableCell("sum") { label("sum_text", Component.text("Summe")) } } }
                    tableCaption("caption", Component.text("Einsätze"))
                }
            },
            null,
        )

        val table = assertIs<TableNode>(root())
        val sections = table.children.filterIsInstance<TableSectionNode>()
        assertEquals(listOf(TableSection.HEADER, TableSection.BODY, TableSection.FOOTER), sections.map { it.section })
        val head = assertIs<TableCellNode>(assertIs<TableRowNode>(sections[0].children.single()).children.single())
        assertTrue(head.head)
        assertIs<LabelNode>(head.children.single())
        val row = assertIs<TableRowNode>(sections[1].children.single())
        assertTrue(row.selected)
        assertEquals(Align.END, assertIs<TableCellNode>(row.children.single()).align)
        assertIs<TableCaptionNode>(table.children.last())
    }

    /**
     * Opens a screen with a selectable data table of five rows, two per page, filtered by name,
     * whose last row cannot be selected, and returns its session id.
     *
     * @return the session id
     */
    private fun openDataTable(): Int = state.open(
        screen(Component.text("Einheiten")) {
            dataTable("units", pageSize = 2, selectable = true, filterColumn = "name", onChange = { reports += it.value }) {
                dataTableColumn("name_column", "name", Component.text("Name"), sortable = true)
                dataTableColumn("note_column", "note", Component.text("Notiz"))
                listOf("RTW", "NEF", "KTW", "RTW Nord", "Leitstelle").forEachIndexed { index, name ->
                    dataTableRow("r$index", selectable = index != 4) {
                        dataTableCell("r${index}_name", Component.text(name), name)
                        dataTableCell("r${index}_note", Component.text("-"), "")
                    }
                }
            }
        },
        null,
    ).sessionId

    /**
     * The values of the reported changes, in order.
     */
    private val reports = mutableListOf<String>()

    /**
     * Verifies that a data table maps to its nodes with its view as JSON.
     */
    @Test
    fun `data tables map to their nodes`() {
        openDataTable()

        val table = assertIs<DataTableNode>(root())
        assertEquals(2, table.pageSize)
        assertTrue(table.notifyChange)
        assertEquals(DataTableView().toJson(), table.value)
        assertEquals(listOf("name", "note"), table.children.filterIsInstance<DataTableColumnNode>().map { it.key })
        val row = table.children.filterIsInstance<DataTableRowNode>().last()
        assertEquals(false, row.selectable)
        assertEquals("Leitstelle", assertIs<DataTableCellNode>(row.children.first()).sortKey)
    }

    /**
     * Verifies that valid views are accepted and reported, and that views naming unknown or
     * unsortable columns, missing pages, unselectable or repeated rows, or malformed JSON are
     * rejected.
     */
    @Test
    fun `data table views are validated`() {
        val session = openDataTable()
        fun change(value: String) = state.handleInputChange(ScreenInputChange(session, "units", value))

        val valid = DataTableView(sort = "name", desc = true, filter = "rtw", page = 0, selected = listOf("r0", "r3")).toJson()
        assertIs<PlayerScreenState.Outcome.Accepted>(change(valid))
        assertIs<PlayerScreenState.Outcome.Accepted>(change(DataTableView(page = 2).toJson()))
        assertEquals(listOf(valid, DataTableView(page = 2).toJson()), reports)

        listOf(
            DataTableView(sort = "note"), DataTableView(sort = "missing"), DataTableView(page = 3),
            DataTableView(filter = "rtw", page = 1), DataTableView(selected = listOf("r4")),
            DataTableView(selected = listOf("r0", "r0")), DataTableView(selected = listOf("x")),
        ).forEach { assertIs<PlayerScreenState.Outcome.Rejected>(change(it.toJson()), it.toString()) }
        assertIs<PlayerScreenState.Outcome.Rejected>(change("{\"page\":"))
        assertEquals(2, reports.size)
    }

    /**
     * Verifies that a chart maps to its node with its series and options, keeping colour
     * numbers within the five chart colours and replacing values that are not finite with zero.
     */
    @Test
    fun `charts map to their nodes`() {
        state.open(
            screen(Component.text("Diagramm")) {
                chart(
                    "chart",
                    ChartKind.AREA,
                    listOf(Component.text("Jan"), Component.text("Feb")),
                    listOf(ChartSeries("calls", Component.text("Einsätze"), 9, listOf(1.0, Double.NaN))),
                    categoryColors = listOf(0, 3),
                    stacked = true,
                    curve = ChartCurve.STEP,
                    legend = true,
                    indicator = ChartIndicator.LINE,
                )
            },
            null,
        )

        val chart = assertIs<ChartNode>(root())
        assertEquals(NodeChartKind.AREA, chart.kind)
        assertEquals(2, chart.categories.size)
        assertEquals(5, chart.series.single().color)
        assertEquals(listOf(1.0, 0.0), chart.series.single().values)
        assertEquals(listOf(1, 3), chart.categoryColors)
        assertTrue(chart.stacked && chart.legend)
        assertEquals(NodeChartCurve.STEP, chart.curve)
        assertEquals(NodeChartIndicator.LINE, chart.indicator)
    }

    /**
     * Verifies that a chat view maps to its nodes, leaving out empty names and times, and that
     * appended messages reach the open screen as insert patches at the end.
     */
    @Test
    fun `chat views map and append messages`() {
        val screen = state.open(
            screen(Component.text("Chat")) {
                chatView("chat", ElementSize.fixed(100)) {
                    chatMessage("m1", name = Component.text("Leitstelle"), time = Component.text("12:30"), playerId = UUID(1, 2)) { text("m1_text", Component.text("Hallo")) }
                    chatMessage("m2", own = true) { text("m2_text", Component.text("Hi")) }
                }
            },
            null,
        )

        val view = assertIs<ChatViewNode>(root())
        val first = assertIs<ChatMessageNode>(view.children[0])
        assertEquals(UUID(1, 2).toString(), first.playerId)
        assertTrue(first.name.contains("Leitstelle"))
        val second = assertIs<ChatMessageNode>(view.children[1])
        assertTrue(second.own)
        assertEquals("", second.name)
        assertEquals("", second.time)

        screen.patch { append("chat") { chatMessage("m3") { text("m3_text", Component.text("Neu")) } } }
        val insert = assertIs<InsertNode>(assertIs<ScreenPatch>(sent.last()).operations.single())
        assertEquals("chat", insert.parentId)
        assertTrue(insert.index >= 2)
        assertEquals("m3", insert.node.id)
    }
}
