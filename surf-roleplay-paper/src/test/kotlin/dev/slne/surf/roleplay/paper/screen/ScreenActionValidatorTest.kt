package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.ButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.CheckboxElement
import dev.slne.surf.roleplay.api.client.common.screen.ColumnElement
import dev.slne.surf.roleplay.api.client.common.screen.DropdownChoice
import dev.slne.surf.roleplay.api.client.common.screen.DropdownElement
import dev.slne.surf.roleplay.api.client.common.screen.LabelElement
import dev.slne.surf.roleplay.api.client.common.screen.NumberInputElement
import dev.slne.surf.roleplay.api.client.common.screen.TextInputElement
import dev.slne.surf.roleplay.protocol.screen.InputValue
import net.kyori.adventure.text.Component
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests for [ScreenActionValidator].
 */
class ScreenActionValidatorTest {

    /**
     * A tree with one input of each kind, a disabled input, a label and two buttons.
     */
    private val tree = ServerScreenTree(
        ColumnElement(
            "root",
            children = listOf(
                LabelElement("title", Component.text("A")),
                TextInputElement("name", maxLength = 5, required = true),
                NumberInputElement("age", min = 18, max = 99),
                CheckboxElement("agree"),
                DropdownElement("city", listOf(DropdownChoice("north", Component.text("Nord"))), required = true),
                TextInputElement("locked", value = "fest", enabled = false),
                ButtonElement("ok", Component.text("OK")),
                ButtonElement("off", Component.text("Aus"), enabled = false),
                ButtonElement("back", Component.text("Zurück"), submitsInput = false),
            ),
        ),
    )

    /**
     * Valid values for every editable input.
     */
    private val valid = listOf(
        InputValue("name", "Max"),
        InputValue("age", "30"),
        InputValue("agree", "true"),
        InputValue("city", "north"),
    )

    /**
     * Validates a click on a widget with values.
     *
     * @param widgetId the clicked widget
     * @param values the submitted values
     * @return the result
     */
    private fun validate(widgetId: String = "ok", values: List<InputValue> = valid) =
        ScreenActionValidator.validate(tree, widgetId, values)

    /**
     * Asserts that a result is a rejection whose reason contains a text.
     *
     * @param result the result
     * @param reason the expected part of the reason
     */
    private fun assertRejected(result: ScreenActionValidator.Result, reason: String) {
        val rejected = assertIs<ScreenActionValidator.Result.Rejected>(result)
        kotlin.test.assertContains(rejected.reason, reason)
    }

    /**
     * Verifies that a valid click is accepted with every input's value, filling in the current
     * value of inputs that were not submitted.
     */
    @Test
    fun `valid click is accepted with every input value`() {
        val accepted = assertIs<ScreenActionValidator.Result.Accepted>(validate())

        assertEquals(
            mapOf("name" to "Max", "age" to "30", "agree" to "true", "city" to "north", "locked" to "fest"),
            accepted.values,
        )
    }

    /**
     * Verifies that clicks on unknown, disabled and non-button widgets are rejected.
     */
    @Test
    fun `clicks need an enabled button`() {
        assertRejected(validate("missing"), "unknown widget")
        assertRejected(validate("off"), "disabled")
        assertRejected(validate("title"), "not a button")
    }

    /**
     * Verifies that values for unknown ids and for widgets that are not inputs are rejected.
     */
    @Test
    fun `values must belong to inputs`() {
        assertRejected(validate(values = valid + InputValue("missing", "x")), "unknown input")
        assertRejected(validate(values = valid + InputValue("title", "x")), "not an input")
        assertRejected(validate(values = valid + InputValue("name", "Max")), "twice")
    }

    /**
     * Verifies that a disabled input must keep its value.
     */
    @Test
    fun `disabled inputs keep their value`() {
        assertEquals(true, validate(values = valid + InputValue("locked", "fest")) is ScreenActionValidator.Result.Accepted)
        assertRejected(validate(values = valid + InputValue("locked", "anders")), "disabled")
    }

    /**
     * Verifies the text input rules: maximum length and required.
     */
    @Test
    fun `text inputs enforce length and required`() {
        assertRejected(validate(values = valid.replace("name", "Maximilian")), "too long")
        assertRejected(validate(values = valid.replace("name", "")), "required")
    }

    /**
     * Verifies the number input rules: a whole number within the range, or empty when optional.
     */
    @Test
    fun `number inputs enforce format and range`() {
        assertRejected(validate(values = valid.replace("age", "abc")), "not a number")
        assertRejected(validate(values = valid.replace("age", "17")), "range")
        assertRejected(validate(values = valid.replace("age", "100")), "range")
        assertEquals(true, validate(values = valid.replace("age", "")) is ScreenActionValidator.Result.Accepted)
    }

    /**
     * Verifies that checkboxes accept only `true` and `false`.
     */
    @Test
    fun `checkboxes accept only booleans`() {
        assertRejected(validate(values = valid.replace("agree", "yes")), "true or false")
    }

    /**
     * Verifies that dropdowns accept only their options, and no selection only when optional.
     */
    @Test
    fun `dropdowns accept only their options`() {
        assertRejected(validate(values = valid.replace("city", "south")), "option")
        assertRejected(validate(values = valid.replace("city", "")), "required")
    }

    /**
     * Verifies that a button that does not submit input is accepted with invalid inputs, and that
     * the invalid inputs keep their last valid value.
     */
    @Test
    fun `non-submitting buttons accept invalid inputs`() {
        val invalid = valid.replace("name", "").replace("age", "5")

        val accepted = assertIs<ScreenActionValidator.Result.Accepted>(validate("back", invalid))

        assertEquals("", accepted.values["name"])
        assertEquals("", accepted.values["age"])
        assertEquals("north", accepted.values["city"])
    }

    /**
     * Verifies that non-submitting buttons still reject values for unknown inputs.
     */
    @Test
    fun `non-submitting buttons still reject unknown inputs`() {
        assertRejected(validate("back", valid + InputValue("missing", "x")), "unknown input")
    }

    /**
     * Verifies that client-supplied ids in rejection reasons are shortened and stripped of control
     * characters.
     */
    @Test
    fun `rejection reasons sanitise client ids`() {
        val rejected = assertIs<ScreenActionValidator.Result.Rejected>(validate("x".repeat(500) + "\n"))

        kotlin.test.assertTrue(rejected.reason.length < 120, rejected.reason)
        kotlin.test.assertFalse(rejected.reason.contains('\n'))
    }

    /**
     * Returns the values with one value replaced.
     *
     * @param id the input id
     * @param value the new value
     * @return the values
     */
    private fun List<InputValue>.replace(id: String, value: String) = map { if (it.widgetId == id) InputValue(id, value) else it }
}
