package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.SelectChoice
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoiceGroup
import dev.slne.surf.roleplay.api.client.common.screen.TextInputElement
import dev.slne.surf.roleplay.api.client.common.screen.combobox
import dev.slne.surf.roleplay.api.client.common.screen.inputOtp
import dev.slne.surf.roleplay.api.client.common.screen.screen
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.InputValue
import dev.slne.surf.roleplay.protocol.screen.ScreenInputChange
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import net.kyori.adventure.text.Component
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests for submits that leave out inputs, option patches after several searches, invalid change
 * events and the default text limit.
 */
class InputReviewFixesTest {

    /**
     * The state under test.
     */
    private val state = PlayerScreenState(
        UUID.randomUUID(),
        object : ScreenPacketSender {
            /**
             * Ignores a packet.
             *
             * @param type the packet type
             * @param packet the packet
             */
            override fun <P : Packet> send(type: PacketType<P>, packet: P) = Unit
        },
        ActionRateLimiter(100),
    )

    /**
     * Returns option groups with one option per value.
     *
     * @param values the option values
     * @return one group without heading
     */
    private fun options(vararg values: String) = listOf(SelectChoiceGroup(null, values.map { SelectChoice(it, Component.text(it)) }))

    /**
     * Opens a screen with a required one-time code, a searching combobox with a change handler and
     * a submit button.
     *
     * @return the session id
     */
    private fun open(): Int = state.open(
        screen(Component.text("Test")) {
            column("root") {
                inputOtp("code", length = 4, required = true, onChange = {})
                combobox(
                    "city",
                    groups = options("north"),
                    multiple = true,
                    onSearch = { search -> search.screen.patch { setOptions("city", options(search.query)) } },
                )
                button("submit", Component.text("Senden"))
            }
        },
        null,
    ).sessionId

    /**
     * Verifies that a submit that leaves out a required input is rejected.
     */
    @Test
    fun `submits without a required input are rejected`() {
        val session = open()

        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleWidgetAction(ScreenWidgetAction(session, "submit")))
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "submit", listOf(InputValue("code", "1234")))))
    }

    /**
     * Verifies that an option offered by an earlier search stays valid after a later search
     * replaced the options.
     */
    @Test
    fun `options of earlier searches stay valid`() {
        val session = open()

        state.handleInputChange(ScreenInputChange(session, "city", "", query = "east"))
        state.handleInputChange(ScreenInputChange(session, "city", "", query = "west"))

        val outcome = state.handleWidgetAction(ScreenWidgetAction(session, "submit", listOf(InputValue("code", "1234"), InputValue("city", "east,west"))))
        assertIs<PlayerScreenState.Outcome.Accepted>(outcome)
    }

    /**
     * Verifies that a change event with a value that is not valid yet is rejected without being
     * treated as suspicious.
     */
    @Test
    fun `invalid change values are not suspicious`() {
        val session = open()

        val outcome = assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChange(session, "code", "12")))
        assertEquals(false, outcome.suspicious)
    }

    /**
     * Verifies that texts without their own limit are limited by default.
     */
    @Test
    fun `texts have a default limit`() {
        val input = TextInputElement("t")
        val rule = ElementRules.input(input)!!

        assertNull(rule.violation(input, "a".repeat(ElementRules.MAX_TEXT_LENGTH)))
        assertNotNull(rule.violation(input, "a".repeat(ElementRules.MAX_TEXT_LENGTH + 1)))
    }
}
