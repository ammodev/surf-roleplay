package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.Orientation
import dev.slne.surf.roleplay.api.client.common.screen.RadioChoice
import dev.slne.surf.roleplay.api.client.common.screen.RadioGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenClick
import dev.slne.surf.roleplay.api.client.common.screen.SliderElement
import dev.slne.surf.roleplay.api.client.common.screen.SwitchElement
import dev.slne.surf.roleplay.api.client.common.screen.SwitchSize
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.RadioGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Screen
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Slider
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Switch
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.InputValue
import dev.slne.surf.roleplay.protocol.screen.RadioGroupNode
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import dev.slne.surf.roleplay.protocol.screen.SliderNode
import dev.slne.surf.roleplay.protocol.screen.SwitchNode
import dev.slne.surf.roleplay.protocol.screen.WidgetScreenBody
import net.kyori.adventure.text.Component
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import dev.slne.surf.roleplay.protocol.screen.Orientation as NodeOrientation
import dev.slne.surf.roleplay.protocol.screen.SwitchSize as NodeSwitchSize

/**
 * Tests for switches, radio groups and sliders in the API and on Paper.
 */
class ChoiceComponentsTest {

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
     * The clicks of the submit button.
     */
    private val clicks = mutableListOf<ScreenClick>()

    /**
     * Opens a screen with every choice component.
     *
     * @return the session id
     */
    private fun open(): Int = state.open(
        Screen(Component.text("Auswahl")) {
            Column(id = "root") {
                Switch(checked = true, size = SwitchSize.SM, id = "notify")
                RadioGroup(
                    listOf(RadioChoice("free", Component.text("Frei")), RadioChoice("pro", Component.text("Pro"), enabled = false)),
                    selected = "free",
                    orientation = Orientation.HORIZONTAL,
                    id = "plan",
                )
                Slider(listOf(20.0, 80.0), step = 5.0, id = "price")
                Button(Component.text("Senden"), id = "submit") { clicks += it }
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
        val switch = assertIs<SwitchNode>(children[0])
        assertEquals(true, switch.checked)
        assertEquals(NodeSwitchSize.SM, switch.size)
        val radio = assertIs<RadioGroupNode>(children[1])
        assertEquals(NodeOrientation.HORIZONTAL, radio.orientation)
        assertEquals(false, radio.options[1].enabled)
        val slider = assertIs<SliderNode>(children[2])
        assertEquals(listOf(20.0, 80.0), slider.values)
        assertEquals(5.0, slider.step)
    }

    /**
     * Verifies that a submit carries the new values in their typed forms.
     */
    @Test
    fun `values are submitted`() {
        val session = open()

        val outcome = state.handleWidgetAction(
            ScreenWidgetAction(session, "submit", listOf(InputValue("notify", "false"), InputValue("plan", ""), InputValue("price", "25,75"))),
        )

        assertIs<PlayerScreenState.Outcome.Accepted>(outcome)
        val values = clicks.single().values
        assertEquals(false, values.checked("notify"))
        assertEquals("", values.text("plan"))
        assertEquals(listOf(25.0, 75.0), values.numbers("price"))
    }

    /**
     * Verifies the switch and radio group rules.
     */
    @Test
    fun `switches and radio groups validate their values`() {
        val switch = SwitchElement("s")
        assertNotNull(ElementRules.input(switch)!!.violation(switch, "yes"))
        val radio = RadioGroupElement("r", listOf(RadioChoice("a", Component.empty()), RadioChoice("b", Component.empty(), enabled = false)))
        val rule = ElementRules.input(radio)!!

        assertNull(rule.violation(radio, "a"))
        assertNull(rule.violation(radio, ""))
        assertNotNull(rule.violation(radio, "x"))
        assertNotNull(rule.violation(radio, "b"))
        assertNull(rule.violation(radio.copy(selected = "b"), "b"))
        assertNotNull(rule.violation(radio.copy(required = true), ""))
    }

    /**
     * Verifies the slider rules: the thumb count, the range, the step and the order.
     */
    @Test
    fun `sliders validate range and step`() {
        val slider = SliderElement("v", listOf(0.5), min = 0.0, max = 1.0, step = 0.1)
        val rule = ElementRules.input(slider)!!

        assertNull(rule.violation(slider, "0.3"))
        assertNotNull(rule.violation(slider, "0.35"))
        assertNotNull(rule.violation(slider, "1.1"))
        assertNotNull(rule.violation(slider, "0.1,0.2"))
        assertNotNull(rule.violation(slider, "x"))
        val range = slider.copy(values = listOf(0.2, 0.4))
        assertNull(rule.violation(range, "0.1,0.9"))
        assertNotNull(rule.violation(range, "0.9,0.1"))
        assertEquals("0.1,0.9", rule.current(rule.withValue(range, "0.1,0.9")!!))
    }

    /**
     * Verifies the constraints the API checks when elements are created.
     */
    @Test
    fun `elements check their settings`() {
        assertFailsWith<IllegalArgumentException> { SliderElement("v", listOf(5.0), min = 10.0, max = 0.0) }
        assertFailsWith<IllegalArgumentException> { SliderElement("v", listOf(5.0), step = 0.0) }
        assertFailsWith<IllegalArgumentException> { SliderElement("v", listOf(1.0, 2.0, 3.0)) }
        assertFailsWith<IllegalArgumentException> { SliderElement("v", listOf(50.0, 10.0)) }
        assertFailsWith<IllegalArgumentException> { SliderElement("v", listOf(2.5)) }
        assertFailsWith<IllegalArgumentException> { RadioGroupElement("r", listOf(RadioChoice("a", Component.empty())), selected = "b") }
    }
}
