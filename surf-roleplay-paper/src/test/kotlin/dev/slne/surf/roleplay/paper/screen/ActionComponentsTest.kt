package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.ButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.ButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.Orientation
import dev.slne.surf.roleplay.api.client.common.screen.ScreenClick
import dev.slne.surf.roleplay.api.client.common.screen.ToggleGroupChoice
import dev.slne.surf.roleplay.api.client.common.screen.ToggleGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.ToggleSize
import dev.slne.surf.roleplay.api.client.common.screen.ToggleVariant
import dev.slne.surf.roleplay.api.client.common.screen.buttonGroup
import dev.slne.surf.roleplay.api.client.common.screen.buttonGroupSeparator
import dev.slne.surf.roleplay.api.client.common.screen.buttonGroupText
import dev.slne.surf.roleplay.api.client.common.screen.screen
import dev.slne.surf.roleplay.api.client.common.screen.toggle
import dev.slne.surf.roleplay.api.client.common.screen.toggleGroup
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.ButtonGroupNode
import dev.slne.surf.roleplay.protocol.screen.ButtonGroupSeparatorNode
import dev.slne.surf.roleplay.protocol.screen.ButtonGroupTextNode
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.InputValue
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import dev.slne.surf.roleplay.protocol.screen.ToggleGroupNode
import dev.slne.surf.roleplay.protocol.screen.ToggleNode
import dev.slne.surf.roleplay.protocol.screen.WidgetScreenBody
import net.kyori.adventure.text.Component
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import dev.slne.surf.roleplay.protocol.screen.ButtonSize as NodeButtonSize
import dev.slne.surf.roleplay.protocol.screen.ButtonVariant as NodeButtonVariant
import dev.slne.surf.roleplay.protocol.screen.Orientation as NodeOrientation
import dev.slne.surf.roleplay.protocol.screen.ToggleSize as NodeToggleSize
import dev.slne.surf.roleplay.protocol.screen.ToggleVariant as NodeToggleVariant

/**
 * Tests for button variants, button groups, toggles and toggle groups in the API and on Paper.
 */
class ActionComponentsTest {

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
     * The clicks toggles reported.
     */
    private val toggles = mutableListOf<ScreenClick>()

    /**
     * Opens a screen with every new component.
     *
     * @return the session id
     */
    private fun open(): Int = state.open(
        screen(Component.text("Aktionen")) {
            column("root") {
                button("delete", Component.text("Löschen"), variant = ButtonVariant.DESTRUCTIVE, size = ButtonSize.SM)
                buttonGroup("group", orientation = Orientation.VERTICAL) {
                    buttonGroupText("group_text", Component.text("Text"), icon = "info")
                    button("group_a", Component.text("A"))
                    buttonGroupSeparator("group_sep")
                    button("group_b", Component.text("B"))
                }
                toggle("bold", Component.text("B"), icon = "bold", variant = ToggleVariant.OUTLINE, size = ToggleSize.LG) { toggles += it }
                toggleGroup(
                    "align",
                    listOf(ToggleGroupChoice("left", icon = "align-left"), ToggleGroupChoice("center"), ToggleGroupChoice("right", enabled = false)),
                    multiple = false,
                    selected = listOf("left"),
                    variant = ToggleVariant.OUTLINE,
                )
            }
        },
        null,
    ).sessionId

    /**
     * Verifies that the new elements map to their nodes with all their settings.
     */
    @Test
    fun `elements map to their nodes`() {
        open()

        val children = assertIs<ColumnNode>(assertIs<WidgetScreenBody>((sent.last() as ScreenOpen).body).root).children
        val button = assertIs<ButtonNode>(children[0])
        assertEquals(NodeButtonVariant.DESTRUCTIVE, button.variant)
        assertEquals(NodeButtonSize.SM, button.size)
        val group = assertIs<ButtonGroupNode>(children[1])
        assertEquals(NodeOrientation.VERTICAL, group.orientation)
        assertIs<ButtonGroupTextNode>(group.children[0])
        assertIs<ButtonGroupSeparatorNode>(group.children[2])
        val toggle = assertIs<ToggleNode>(children[2])
        assertEquals(NodeToggleVariant.OUTLINE, toggle.variant)
        assertEquals(NodeToggleSize.LG, toggle.size)
        val toggleGroup = assertIs<ToggleGroupNode>(children[3])
        assertEquals(listOf("left"), toggleGroup.selected)
        assertEquals(false, toggleGroup.items[2].enabled)
    }

    /**
     * Verifies that pressing a toggle runs its handler with its new state, without requiring the
     * other inputs to be valid.
     */
    @Test
    fun `toggles fire actions with their new state`() {
        val session = open()

        val outcome = state.handleWidgetAction(ScreenWidgetAction(session, "bold", listOf(InputValue("bold", "true"))))

        assertIs<PlayerScreenState.Outcome.Accepted>(outcome)
        assertEquals(true, toggles.single().values.checked("bold"))
    }

    /**
     * Verifies that buttons inside a group are found and fire actions.
     */
    @Test
    fun `buttons in groups fire actions`() {
        val session = open()

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "group_b")))
    }

    /**
     * Verifies the toggle group rules: known enabled items only, a single item unless multiple,
     * and required.
     */
    @Test
    fun `toggle groups validate their items`() {
        val single = ToggleGroupElement("g", listOf(ToggleGroupChoice("a"), ToggleGroupChoice("b"), ToggleGroupChoice("c", enabled = false)))
        val multiple = single.copy(multiple = true, required = true)
        val rule = ElementRules.input(single)!!

        assertNull(rule.violation(single, "a"))
        assertNull(rule.violation(single, ""))
        assertNotNull(rule.violation(single, "a,b"))
        assertNotNull(rule.violation(single, "x"))
        assertNotNull(rule.violation(single, "c"))
        assertNull(rule.violation(multiple, "a,b"))
        assertNotNull(rule.violation(multiple, ""))
        assertEquals("a,b", rule.current(rule.withValue(multiple, "a,b")!!))
    }
}
