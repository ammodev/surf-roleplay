package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.ButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.CheckboxElement
import dev.slne.surf.roleplay.api.client.common.screen.LabelElement
import dev.slne.surf.roleplay.api.client.common.screen.NumberInputElement
import dev.slne.surf.roleplay.api.client.common.screen.ProgressElement
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoice
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoiceGroup
import dev.slne.surf.roleplay.api.client.common.screen.SelectElement
import dev.slne.surf.roleplay.api.client.common.screen.TextInputElement
import net.kyori.adventure.text.Component
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests for [ElementRules].
 */
class ElementRulesTest {

    /**
     * Verifies the value rules of every input kind: current value, constraint check and update.
     */
    @Test
    fun `inputs have value rules`() {
        val text = TextInputElement("t", value = "ab", maxLength = 3, required = true)
        val number = NumberInputElement("n", value = 5, min = 1, max = 9)
        val checkbox = CheckboxElement("c", checked = true)
        val dropdown = SelectElement("d", listOf(SelectChoiceGroup(null, listOf(SelectChoice("x", Component.text("X"))))), selected = "x")

        assertEquals(listOf("ab", "5", "true", "x"), listOf(text, number, checkbox, dropdown).map { ElementRules.input(it)!!.current(it) })
        assertNull(ElementRules.input(text)!!.violation(text, "abc"))
        assertNotNull(ElementRules.input(text)!!.violation(text, "abcd"))
        assertNotNull(ElementRules.input(number)!!.violation(number, "10"))
        assertNotNull(ElementRules.input(checkbox)!!.violation(checkbox, "ja"))
        assertNotNull(ElementRules.input(dropdown)!!.violation(dropdown, "y"))
        assertEquals(7L, (ElementRules.input(number)!!.withValue(number, "7") as NumberInputElement).value)
    }

    /**
     * Verifies that labels, buttons and progress bars are not inputs but take text.
     */
    @Test
    fun `non-inputs take text but no value`() {
        val label = LabelElement("l", Component.text("A"))
        val button = ButtonElement("b", Component.text("B"))
        val progress = ProgressElement("p", 0f)

        assertNull(ElementRules.input(label))
        assertEquals(Component.text("Z"), (ElementRules.rule(label)!!.withText(label, Component.text("Z")) as LabelElement).text)
        assertEquals(Component.text("Z"), (ElementRules.rule(button)!!.withText(button, Component.text("Z")) as ButtonElement).text)
        assertEquals(Component.text("Z"), (ElementRules.rule(progress)!!.withText(progress, Component.text("Z")) as ProgressElement).label)
    }

    /**
     * Verifies that buttons and inputs can be enabled and disabled, and labels cannot.
     */
    @Test
    fun `buttons and inputs can be disabled`() {
        val button = ButtonElement("b", Component.text("B"))
        val label = LabelElement("l", Component.text("A"))

        assertEquals(false, ElementRules.rule(button)!!.withEnabled(button, false)?.let { ElementRules.isEnabled(it) })
        assertNull(ElementRules.rule(label)!!.withEnabled(label, false))
    }
}
