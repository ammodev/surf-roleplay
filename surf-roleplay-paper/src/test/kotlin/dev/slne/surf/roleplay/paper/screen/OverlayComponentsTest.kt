package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.OverlaySide
import dev.slne.surf.roleplay.api.client.common.screen.ScreenInputChange
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.HoverCard
import dev.slne.surf.roleplay.api.client.common.screen.dsl.HoverCardContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Input
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Label
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Popover
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PopoverContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PopoverDescription
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PopoverHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PopoverTitle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Screen
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Tooltip
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.HoverCardContentNode
import dev.slne.surf.roleplay.protocol.screen.HoverCardNode
import dev.slne.surf.roleplay.protocol.screen.InputValue
import dev.slne.surf.roleplay.protocol.screen.PopoverContentNode
import dev.slne.surf.roleplay.protocol.screen.PopoverHeaderNode
import dev.slne.surf.roleplay.protocol.screen.PopoverNode
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenPatch
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import dev.slne.surf.roleplay.protocol.screen.SetOpen
import dev.slne.surf.roleplay.protocol.screen.TextNode
import dev.slne.surf.roleplay.protocol.screen.TooltipNode
import dev.slne.surf.roleplay.protocol.screen.WidgetScreenBody
import net.kyori.adventure.text.Component
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import dev.slne.surf.roleplay.protocol.screen.Align as NodeAlign
import dev.slne.surf.roleplay.protocol.screen.OverlaySide as NodeOverlaySide
import dev.slne.surf.roleplay.protocol.screen.ScreenInputChange as ScreenInputChangePacket
import dev.slne.surf.roleplay.protocol.screen.TextKind as NodeTextKind

/**
 * Tests for popovers, hover cards and tooltips in the API and on Paper.
 */
class OverlayComponentsTest {

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
     * The open-state changes the popover reported.
     */
    private val changes = mutableListOf<ScreenInputChange>()

    /**
     * Opens a screen with a popover holding a required input, a hover card and a tooltip.
     *
     * @return the session id
     */
    private fun open(): Int = state.open(
        Screen(Component.text("Overlays")) {
            Column(id = "root") {
                Popover(OverlaySide.RIGHT, Alignment.START, onChange = { changes += it }, id = "popover") {
                    Button(Component.text("Öffnen"), submitsInput = false, id = "trigger")
                    PopoverContent(id = "content") {
                        PopoverHeader(id = "header") {
                            PopoverTitle(Component.text("Maße"), id = "title")
                            PopoverDescription(Component.text("Lege die Maße fest."), id = "description")
                        }
                        Input(required = true, id = "width")
                    }
                }
                HoverCard(openDelay = 500, id = "card") {
                    Label(Component.text("@max"), id = "name")
                    HoverCardContent(id = "card_content") { Label(Component.text("Max Mustermann"), id = "bio") }
                }
                Tooltip(Component.text("Speichern"), OverlaySide.BOTTOM, id = "tip") {
                    Button(Component.text("S"), id = "save")
                }
            }
        },
        null,
    ).sessionId

    /**
     * Verifies that popovers, hover cards and tooltips map to their nodes with their settings,
     * and that popover contents default to shadcn's width.
     */
    @Test
    fun `overlays map to their nodes`() {
        open()

        val root = assertIs<ColumnNode>(assertIs<WidgetScreenBody>((sent.last() as ScreenOpen).body).root)
        val popover = assertIs<PopoverNode>(root.children[0])
        assertEquals(NodeOverlaySide.RIGHT, popover.side)
        assertEquals(NodeAlign.START, popover.align)
        assertTrue(popover.notifyChange)
        val content = assertIs<PopoverContentNode>(popover.children[1])
        assertEquals(144, content.width.value)
        val header = assertIs<PopoverHeaderNode>(content.children[0])
        assertEquals(NodeTextKind.POPOVER_TITLE, assertIs<TextNode>(header.children[0]).kind)
        val card = assertIs<HoverCardNode>(root.children[1])
        assertEquals(500, card.openDelay)
        assertIs<HoverCardContentNode>(card.children[1])
        val tip = assertIs<TooltipNode>(root.children[2])
        assertEquals(NodeOverlaySide.BOTTOM, tip.side)
    }

    /**
     * Verifies that an open-state change is validated and reported, and that anything but true or
     * false is rejected.
     */
    @Test
    fun `open state changes are validated`() {
        val session = open()

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChangePacket(session, "popover", "true")))
        assertEquals("true", changes.single().value)
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChangePacket(session, "popover", "maybe")))
    }

    /**
     * Verifies that an input inside a closed popover is still validated on a submit.
     */
    @Test
    fun `inputs inside popovers are validated`() {
        val session = open()

        val outcome = state.handleWidgetAction(ScreenWidgetAction(session, "save", listOf(InputValue("width", ""), InputValue("popover", "false"), InputValue("card", "false"))))

        assertIs<PlayerScreenState.Outcome.Rejected>(outcome)
    }

    /**
     * Verifies that the server can open an overlay by patch.
     */
    @Test
    fun `set open patches the overlay`() {
        val session = open()
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChangePacket(session, "popover", "true")))

        changes.single().screen.patch { setOpen("popover", false) }

        val patch = assertIs<ScreenPatch>(sent.last())
        assertEquals(listOf(SetOpen("popover", false)), patch.operations)
    }

    /**
     * Verifies that a change event cannot open an overlay whose trigger is disabled, nor change an
     * input inside it.
     */
    @Test
    fun `change events respect unreachable overlays`() {
        val session = state.open(
            Screen(Component.text("Gesperrt")) {
                Column(id = "root") {
                    Popover(onChange = { changes += it }, id = "locked") {
                        Button(Component.text("Öffnen"), enabled = false, id = "locked_trigger")
                        PopoverContent(id = "locked_content") { Input(onChange = { changes += it }, id = "note") }
                    }
                }
            },
            null,
        ).sessionId

        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChangePacket(session, "locked", "true")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChangePacket(session, "note", "x")))
        assertEquals(emptyList(), changes)
    }
}
