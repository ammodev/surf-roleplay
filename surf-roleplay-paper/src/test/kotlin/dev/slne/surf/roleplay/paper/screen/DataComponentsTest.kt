package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ChartCurve
import dev.slne.surf.roleplay.api.client.common.screen.ChartIndicator
import dev.slne.surf.roleplay.api.client.common.screen.ChartKind
import dev.slne.surf.roleplay.api.client.common.screen.ChartSeries
import dev.slne.surf.roleplay.api.client.common.screen.DataTableView
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Chart
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ChatMessage
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ChatView
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DataTable
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DataTableCell
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DataTableColumn
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DataTableRow
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Input
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Label
import dev.slne.surf.roleplay.api.client.common.screen.dsl.P
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Screen
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Table
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TableBody
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TableCaption
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TableCell
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TableFooter
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TableHead
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TableHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TableRow
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.ChartNode
import dev.slne.surf.roleplay.protocol.screen.ChatMessageNode
import dev.slne.surf.roleplay.protocol.screen.ChatViewNode
import dev.slne.surf.roleplay.protocol.screen.DataTableCellNode
import dev.slne.surf.roleplay.protocol.screen.DataTableColumnNode
import dev.slne.surf.roleplay.protocol.screen.DataTableNode
import dev.slne.surf.roleplay.protocol.screen.DataTableRowNode
import dev.slne.surf.roleplay.protocol.screen.InsertNode
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.ScreenInputChange
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenPatch
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import dev.slne.surf.roleplay.protocol.screen.SetValue
import dev.slne.surf.roleplay.protocol.screen.InputValue
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
import dev.slne.surf.roleplay.protocol.screen.ChartCurve as NodeChartCurve
import dev.slne.surf.roleplay.protocol.screen.ChartIndicator as NodeChartIndicator
import dev.slne.surf.roleplay.protocol.screen.ChartKind as NodeChartKind

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
            Screen(Component.text("Tabelle")) {
                Table(id = "table") {
                    TableHeader(id = "header") { TableRow(id = "head_row") { TableHead(Component.text("Einheit"), id = "head") } }
                    TableBody(id = "body") { TableRow(selected = true, id = "row") { TableCell(Component.text("10"), Alignment.END, id = "cell") } }
                    TableFooter(id = "footer") { TableRow(id = "sum_row") { TableCell(id = "sum") { Label(Component.text("Summe"), id = "sum_text") } } }
                    TableCaption(Component.text("Einsätze"), id = "caption")
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
        Screen(Component.text("Einheiten")) {
            DataTable(pageSize = 2, selectable = true, filterColumn = "name", onChange = { reports += it.value }, id = "units") {
                DataTableColumn("name", Component.text("Name"), sortable = true, id = "name_column")
                DataTableColumn("note", Component.text("Notiz"), id = "note_column")
                listOf("RTW", "NEF", "KTW", "RTW Nord", "Leitstelle").forEachIndexed { index, name ->
                    DataTableRow(selectable = index != 4, id = "r$index") {
                        DataTableCell(Component.text(name), name, id = "r${index}_name")
                        DataTableCell(Component.text("-"), "", id = "r${index}_note")
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
        assertIs<PlayerScreenState.Outcome.Rejected>(change("{\"selected\":" + "[".repeat(5000)))
        assertEquals(2, reports.size)
    }

    /**
     * Verifies that a chart maps to its node with its series and options, keeping colour
     * numbers within the five chart colours and replacing values that are not finite with zero.
     */
    @Test
    fun `charts map to their nodes`() {
        state.open(
            Screen(Component.text("Diagramm")) {
                Chart(
                    ChartKind.AREA,
                    listOf(Component.text("Jan"), Component.text("Feb")),
                    listOf(ChartSeries("calls", Component.text("Einsätze"), 9, listOf(1.0, Double.NaN))),
                    categoryColors = listOf(0, 3),
                    stacked = true,
                    curve = ChartCurve.STEP,
                    legend = true,
                    indicator = ChartIndicator.LINE,
                    id = "chart",
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
            Screen(Component.text("Chat")) {
                ChatView(ElementSize.fixed(100), id = "chat") {
                    ChatMessage(name = Component.text("Leitstelle"), time = Component.text("12:30"), playerId = UUID(1, 2), id = "m1") { P(Component.text("Hallo"), id = "m1_text") }
                    ChatMessage(own = true, id = "m2") { P(Component.text("Hi"), id = "m2_text") }
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

        screen.patch { append("chat") { ChatMessage(id = "m3") { P(Component.text("Neu"), id = "m3_text") } } }
        val insert = assertIs<InsertNode>(assertIs<ScreenPatch>(sent.last()).operations.single())
        assertEquals("chat", insert.parentId)
        assertTrue(insert.index >= 2)
        assertEquals("m3", insert.node.id)
    }

    /**
     * Verifies that a send button handler which appends a message and clears the composer in one
     * patch reaches the client as a single patch holding the insert and the empty value, and that
     * the handler sees the text the player typed.
     */
    @Test
    fun `a composer is cleared in the patch that appends the message`() {
        var typed: String? = null
        val session = state.open(
            Screen(Component.text("Chat")) {
                Column(id = "root") {
                    ChatView(ElementSize.fixed(100), id = "chat_view") {
                        ChatMessage(id = "m1") { P(Component.text("Hallo"), id = "m1_text") }
                    }
                    Input(id = "chat_input")
                    Button("Senden", id = "chat_send") { click ->
                        typed = click.values.text("chat_input")
                        click.screen.patch {
                            append("chat_view") { ChatMessage(own = true, id = "m2") { P(Component.text(typed ?: ""), id = "m2_text") } }
                            setValue("chat_input", "")
                        }
                    }
                }
            },
            null,
        ).sessionId
        val before = sent.size

        val action = ScreenWidgetAction(session, "chat_send", listOf(InputValue("chat_input", "Bin unterwegs")))
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(action))

        assertEquals("Bin unterwegs", typed)
        val patches = sent.drop(before).filterIsInstance<ScreenPatch>()
        assertEquals(1, patches.size)
        val operations = patches.single().operations
        assertEquals("chat_view", operations.filterIsInstance<InsertNode>().single().parentId)
        assertEquals("", operations.filterIsInstance<SetValue>().single { it.targetId == "chat_input" }.value)
    }
}
