package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
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
}
