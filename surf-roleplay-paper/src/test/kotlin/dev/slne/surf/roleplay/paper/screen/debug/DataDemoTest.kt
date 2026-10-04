package dev.slne.surf.roleplay.paper.screen.debug

import dev.slne.surf.roleplay.api.client.common.screen.DataTableView
import dev.slne.surf.roleplay.api.client.common.screen.ScreenThemes
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.paper.screen.ActionRateLimiter
import dev.slne.surf.roleplay.paper.screen.PlayerScreenState
import dev.slne.surf.roleplay.paper.screen.ScreenPacketSender
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.ChatMessageNode
import dev.slne.surf.roleplay.protocol.screen.ContainerNode
import dev.slne.surf.roleplay.protocol.screen.InputValue
import dev.slne.surf.roleplay.protocol.screen.InsertNode
import dev.slne.surf.roleplay.protocol.screen.ScreenInputChange
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenPatch
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import dev.slne.surf.roleplay.protocol.screen.SetValue
import dev.slne.surf.roleplay.protocol.screen.TextNode
import dev.slne.surf.roleplay.protocol.screen.WidgetScreenBody
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for the data demo page.
 */
class DataDemoTest {

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
     * The reports the page sent to chat, as plain text.
     */
    private val reports = mutableListOf<String>()

    /**
     * Opens the page and returns its session id.
     *
     * @return the session id
     */
    private fun open(): Int {
        val hooks = DataDemo.Hooks(
            report = { reports += PlainTextComponentSerializer.plainText().serialize(it) },
            reopen = { _, _ -> },
            now = { "13:00" },
        )
        val session = state.open(DataDemo.definition(hooks, ScreenThemes.DEFAULT, ScreenVariant.DARK), null).sessionId
        assertIs<ScreenOpen>(sent.last())
        return session
    }

    /**
     * Verifies that a data table change is reported in chat with its sort, filter, page and
     * selection.
     */
    @Test
    fun `data table changes report in chat`() {
        val session = open()

        val view = DataTableView(sort = "calls", desc = true, filter = "rtw", selected = listOf("u1"))
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChange(session, "units", view.toJson())))
        assertEquals(listOf("Tabelle: calls absteigend, Filter \"rtw\", Seite 1, 1 ausgewählt"), reports)
    }

    /**
     * Verifies that sending a chat message appends it and an answer at the end of the chat view,
     * clears the composer and reports the message.
     */
    @Test
    fun `sent messages are appended and answered`() {
        val session = open()

        val action = ScreenWidgetAction(session, "chat_send", listOf(InputValue("chat_input", "Bin unterwegs")))
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(action))

        val patch = assertIs<ScreenPatch>(sent.last())
        val inserts = patch.operations.filterIsInstance<InsertNode>()
        assertEquals(listOf("chat_view", "chat_view"), inserts.map { it.parentId })
        val own = assertIs<ChatMessageNode>(inserts[0].node)
        assertTrue(own.own)
        assertTrue(own.time.contains("13:00"))
        assertEquals("chat_reply_1", inserts[1].node.id)
        assertEquals("", patch.operations.filterIsInstance<SetValue>().single { it.targetId == "chat_input" }.value)
        assertEquals(listOf("Nachricht gesendet: Bin unterwegs"), reports)
    }

    /**
     * Verifies that the section and chart headings are German.
     */
    @Test
    fun `headings are german`() {
        open()
        val texts = mutableListOf<String>()
        fun collect(node: ScreenNode) {
            if (node is TextNode) texts += node.text
            if (node is ContainerNode) node.children.forEach { collect(it) }
        }
        collect(assertIs<WidgetScreenBody>(assertIs<ScreenOpen>(sent.first()).body).root)

        listOf("Tabelle", "Datentabelle", "Diagramme", "Fläche", "Balken", "Linie", "Kreis", "Netz").forEach { heading ->
            assertTrue(texts.any { it.contains(heading) }, heading)
        }
        listOf("\"Table\"", "\"Data Table\"", "\"Chart\"").forEach { english -> assertTrue(texts.none { it.contains(english) }, english) }
    }
}
