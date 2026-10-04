package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.InputGroupAlign
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.InputOtpElement
import dev.slne.surf.roleplay.api.client.common.screen.LabelElement
import dev.slne.surf.roleplay.api.client.common.screen.OtpPattern
import dev.slne.surf.roleplay.api.client.common.screen.ScreenClick
import dev.slne.surf.roleplay.api.client.common.screen.TextInputElement
import dev.slne.surf.roleplay.api.client.common.screen.TextInputType
import dev.slne.surf.roleplay.api.client.common.screen.TextareaElement
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Input
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputGroupAddon
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputGroupButton
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputGroupText
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputOtp
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Label
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Screen
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Textarea
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.InputGroupAddonNode
import dev.slne.surf.roleplay.protocol.screen.InputGroupNode
import dev.slne.surf.roleplay.protocol.screen.InputGroupTextNode
import dev.slne.surf.roleplay.protocol.screen.InputOtpNode
import dev.slne.surf.roleplay.protocol.screen.InputValue
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import dev.slne.surf.roleplay.protocol.screen.TextInputNode
import dev.slne.surf.roleplay.protocol.screen.TextareaNode
import dev.slne.surf.roleplay.protocol.screen.WidgetScreenBody
import net.kyori.adventure.text.Component
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import dev.slne.surf.roleplay.protocol.screen.ButtonVariant as NodeButtonVariant
import dev.slne.surf.roleplay.protocol.screen.InputGroupAlign as NodeInputGroupAlign
import dev.slne.surf.roleplay.protocol.screen.OtpPattern as NodeOtpPattern
import dev.slne.surf.roleplay.protocol.screen.TextInputType as NodeTextInputType

/**
 * Tests for input types, textareas, input groups, one-time code inputs and label targets in the
 * API and on Paper.
 */
class TextComponentsTest {

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
     * Opens a screen with every text component.
     *
     * @return the session id
     */
    private fun open(): Int = state.open(
        Screen(Component.text("Text")) {
            Column(id = "root") {
                Label(Component.text("E-Mail"), forId = "mail", id = "mail_label")
                Input(type = TextInputType.EMAIL, required = true, id = "mail")
                Input(type = TextInputType.PASSWORD, id = "password")
                Textarea(rows = 4, maxLength = 10, id = "bio")
                InputGroup(id = "search") {
                    InputGroupAddon(id = "search_start") { InputGroupText(Component.text("@"), icon = "at-sign", id = "search_hint") }
                    Input(id = "query")
                    InputGroupAddon(InputGroupAlign.INLINE_END, id = "search_end") { InputGroupButton(icon = "x", id = "clear") }
                }
                InputOtp(length = 4, groups = listOf(2, 2), pattern = OtpPattern.ALPHANUMERIC, id = "code")
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
        assertEquals("mail", assertIs<LabelNode>(children[0]).forId)
        assertEquals(NodeTextInputType.EMAIL, assertIs<TextInputNode>(children[1]).inputType)
        assertEquals(NodeTextInputType.PASSWORD, assertIs<TextInputNode>(children[2]).inputType)
        val bio = assertIs<TextareaNode>(children[3])
        assertEquals(4, bio.rows)
        assertEquals(10, bio.maxLength)
        val group = assertIs<InputGroupNode>(children[4])
        val start = assertIs<InputGroupAddonNode>(group.children[0])
        assertEquals(NodeInputGroupAlign.INLINE_START, start.align)
        assertEquals("at-sign", assertIs<InputGroupTextNode>(start.children.single()).icon)
        assertIs<TextInputNode>(group.children[1])
        val clear = assertIs<ButtonNode>(assertIs<InputGroupAddonNode>(group.children[2]).children.single())
        assertEquals(NodeButtonVariant.GHOST, clear.variant)
        assertEquals(false, clear.submitsInput)
        val code = assertIs<InputOtpNode>(children[5])
        assertEquals(listOf(2, 2), code.groups)
        assertEquals(NodeOtpPattern.ALPHANUMERIC, code.pattern)
    }

    /**
     * Verifies that a submit carries the values of inputs inside an input group and multi-line
     * text.
     */
    @Test
    fun `values inside groups and textareas are submitted`() {
        val session = open()

        val outcome = state.handleWidgetAction(
            ScreenWidgetAction(
                session,
                "submit",
                listOf(InputValue("mail", "max@example.de"), InputValue("bio", "a\nb"), InputValue("query", "suche"), InputValue("code", "A1b2")),
            ),
        )

        assertIs<PlayerScreenState.Outcome.Accepted>(outcome)
        val values = clicks.single().values
        assertEquals("suche", values.text("query"))
        assertEquals("a\nb", values.text("bio"))
        assertEquals("A1b2", values.text("code"))
    }

    /**
     * Verifies the email shape and that single-line fields refuse line breaks.
     */
    @Test
    fun `text inputs check their type`() {
        val mail = TextInputElement("m", type = TextInputType.EMAIL)
        val rule = ElementRules.input(mail)!!

        assertNull(rule.violation(mail, ""))
        assertNull(rule.violation(mail, "max@example.de"))
        assertNotNull(rule.violation(mail, "max"))
        assertNotNull(rule.violation(mail, "max@example"))
        assertNotNull(rule.violation(mail, "max muster@example.de"))
        val text = TextInputElement("t")
        assertNotNull(ElementRules.input(text)!!.violation(text, "a\nb"))
    }

    /**
     * Verifies the textarea rules for length and required.
     */
    @Test
    fun `textareas check length and required`() {
        val area = TextareaElement("a", maxLength = 5, required = true)
        val rule = ElementRules.input(area)!!

        assertNull(rule.violation(area, "a\nbc"))
        assertNotNull(rule.violation(area, "abcdef"))
        assertNotNull(rule.violation(area, ""))
        assertEquals("x\ny", rule.current(rule.withValue(area, "x\ny")!!))
    }

    /**
     * Verifies the one-time code rules: complete codes of the pattern only, and required.
     */
    @Test
    fun `one-time codes check their pattern`() {
        val digits = InputOtpElement("o", length = 4)
        val rule = ElementRules.input(digits)!!

        assertNull(rule.violation(digits, ""))
        assertNull(rule.violation(digits, "1234"))
        assertNotNull(rule.violation(digits, "123"))
        assertNotNull(rule.violation(digits, "12345"))
        assertNotNull(rule.violation(digits, "12a4"))
        assertNotNull(rule.violation(digits.copy(required = true), ""))
        val letters = digits.copy(pattern = OtpPattern.ALPHANUMERIC)
        assertNull(rule.violation(letters, "a1B2"))
    }

    /**
     * Verifies the constraints the API checks when elements are created.
     */
    @Test
    fun `elements check their settings`() {
        assertFailsWith<IllegalArgumentException> { InputOtpElement("o", length = 4, groups = listOf(2, 3)) }
        assertFailsWith<IllegalArgumentException> { InputOtpElement("o", length = 0) }
        assertFailsWith<IllegalArgumentException> { InputOtpElement("o", value = "abc") }
        assertFailsWith<IllegalArgumentException> { TextareaElement("a", rows = 0) }
        assertFailsWith<IllegalArgumentException> { InputGroupElement("g", listOf(LabelElement("l", Component.empty()))) }
        assertFailsWith<IllegalArgumentException> { InputGroupElement("g", listOf(TextInputElement("a"), TextInputElement("b"))) }
    }
}
