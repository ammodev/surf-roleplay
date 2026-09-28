package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.ScreenSearch
import dev.slne.surf.roleplay.api.client.common.screen.command
import dev.slne.surf.roleplay.api.client.common.screen.commandEmpty
import dev.slne.surf.roleplay.api.client.common.screen.commandGroup
import dev.slne.surf.roleplay.api.client.common.screen.commandInput
import dev.slne.surf.roleplay.api.client.common.screen.commandItem
import dev.slne.surf.roleplay.api.client.common.screen.commandList
import dev.slne.surf.roleplay.api.client.common.screen.commandSeparator
import dev.slne.surf.roleplay.api.client.common.screen.screen
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.CommandEmptyNode
import dev.slne.surf.roleplay.protocol.screen.CommandGroupNode
import dev.slne.surf.roleplay.protocol.screen.CommandInputNode
import dev.slne.surf.roleplay.protocol.screen.CommandItemNode
import dev.slne.surf.roleplay.protocol.screen.CommandListNode
import dev.slne.surf.roleplay.protocol.screen.CommandNode
import dev.slne.surf.roleplay.protocol.screen.CommandSeparatorNode
import dev.slne.surf.roleplay.protocol.screen.ScreenInputChange
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import dev.slne.surf.roleplay.protocol.screen.WidgetScreenBody
import net.kyori.adventure.text.Component
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for the command menu in the API and on Paper.
 */
class CommandComponentsTest {

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
     * The clicks and searches the command reported.
     */
    private val events = mutableListOf<String>()

    /**
     * Opens a screen with a client-filtered command and a server-searched command.
     *
     * @return the session id
     */
    private fun open(): Int = state.open(
        screen(Component.text("Befehle")) {
            column("root") {
                command("command") {
                    commandInput("input", Component.text("Befehl suchen"))
                    commandList("list") {
                        commandEmpty("empty", Component.text("Nichts gefunden"))
                        commandGroup("group", Component.text("Vorschläge")) {
                            commandItem("calendar", Component.text("Kalender"), icon = "calendar", shortcut = Component.text("K"), keywords = listOf("termin")) { events += it.buttonId }
                        }
                        commandSeparator("sep")
                    }
                }
                command("server", onSearch = { search: ScreenSearch -> events += "search:${search.query}" }) {
                    commandInput("server_input")
                    commandList("server_list") {}
                }
            }
        },
        null,
    ).sessionId

    /**
     * Verifies that every command part maps to its node with its settings.
     */
    @Test
    fun `commands map to their nodes`() {
        open()

        val root = assertIs<ColumnNode>(assertIs<WidgetScreenBody>((sent.last() as ScreenOpen).body).root)
        val command = assertIs<CommandNode>(root.children[0])
        assertEquals(false, command.notifySearch)
        assertIs<CommandInputNode>(command.children[0])
        val list = assertIs<CommandListNode>(command.children[1])
        assertIs<CommandEmptyNode>(list.children[0])
        val item = assertIs<CommandItemNode>(assertIs<CommandGroupNode>(list.children[1]).children[0])
        assertEquals(listOf("termin"), item.keywords)
        assertIs<CommandSeparatorNode>(list.children[2])
        assertTrue(assertIs<CommandNode>(root.children[1]).notifySearch)
    }

    /**
     * Verifies that choosing an item runs its handler.
     */
    @Test
    fun `items fire actions`() {
        val session = open()

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "calendar")))
        assertEquals(listOf("calendar"), events)
    }

    /**
     * Verifies that a query reaches the search handler of a command that has one, and is
     * rejected for a command without one.
     */
    @Test
    fun `searches reach the handler`() {
        val session = open()

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChange(session, "server", "", query = "kal")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChange(session, "command", "", query = "kal")))
        assertEquals(listOf("search:kal"), events)
    }
}
