package dev.slne.surf.roleplay.api.client.common.screen

import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Label
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Row
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Screen
import net.kyori.adventure.text.Component
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the patch builder, [ScreenValues] and screen definitions.
 */
class ScreenPatchTest {
    /**
     * Verifies that replacing an element with an explicit id keeps the id given to the new root.
     */
    @Test
    fun `replace builds the element with the component dsl`() {
        val changes = ScreenPatchBuilder().apply {
            replace("inputs") { Row(id = "inputs") { Label("Fertig", id = "hint") } }
        }.changes

        val replace = assertIs<ScreenChange.Replace>(changes.single())
        assertEquals("inputs", replace.element.id)
        assertEquals("hint", assertIs<RowElement>(replace.element).children.single().id)
    }

    /**
     * Verifies that replacing a generated element keeps the generated id of the target.
     */
    @Test
    fun `replace keeps the id of a generated target`() {
        val changes = ScreenPatchBuilder().apply {
            replace("_0.2") { Row { Label("Neu") } }
        }.changes

        val element = assertIs<ScreenChange.Replace>(changes.single()).element
        assertEquals("_0.2", element.id)
        assertEquals("_0.2.0", assertIs<RowElement>(element).children.single().id)
    }

    /**
     * Verifies that two appends get distinct generated ids, so that appended content never collides.
     */
    @Test
    fun `appends get distinct ids`() {
        val changes = ScreenPatchBuilder().apply {
            append("chat") { Label("Eins") }
            append("chat") { Label("Zwei") }
        }.changes

        val ids = changes.map { assertIs<ScreenChange.Insert>(it).element.id }
        assertEquals(2, ids.size)
        assertNotEquals(ids[0], ids[1])
        assertTrue(ids.all { it.startsWith("_+") })
        assertTrue(changes.all { assertIs<ScreenChange.Insert>(it).index == Int.MAX_VALUE })
    }

    /**
     * Verifies that several elements of one append get distinct ids below the same counter value.
     */
    @Test
    fun `an append of several elements gets distinct ids`() {
        val changes = ScreenPatchBuilder().apply {
            append("chat") {
                Label("Eins")
                Label("Zwei")
            }
        }.changes

        val ids = changes.map { assertIs<ScreenChange.Insert>(it).element.id }
        assertEquals(2, ids.toSet().size)
    }

    /**
     * Verifies that the patch builder records every change in order.
     */
    @Test
    fun `patch builder records changes in order`() {
        val changes = ScreenPatchBuilder().apply {
            replace("title", LabelElement("title", Component.text("Neu")))
            insert("list", 0, LabelElement("first", Component.empty()))
            remove("logo")
            setText("title", Component.text("B"))
            setValue("name", "Erika")
            setProgress("load", 0.75f)
            setEnabled("ok", false)
        }.changes

        assertEquals(
            listOf(
                ScreenChange.Replace("title", LabelElement("title", Component.text("Neu"))),
                ScreenChange.Insert("list", 0, LabelElement("first", Component.empty())),
                ScreenChange.Remove("logo"),
                ScreenChange.SetText("title", Component.text("B")),
                ScreenChange.SetValue("name", "Erika"),
                ScreenChange.SetProgress("load", 0.75f),
                ScreenChange.SetEnabled("ok", false),
            ),
            changes,
        )
    }

    /**
     * Verifies that screen values are read in the type of their input.
     */
    @Test
    fun `screen values read their input types`() {
        val values = ScreenValues(mapOf("name" to "Max", "age" to "30", "empty_age" to "", "agree" to "true", "city" to ""))

        assertEquals("Max", values.text("name"))
        assertEquals(30L, values.number("age"))
        assertNull(values.number("empty_age"))
        assertEquals(true, values.checked("agree"))
        assertNull(values.selected("city"))
        assertNull(values.text("missing"))
        assertNotNull(values.all["age"])
    }

    /**
     * Verifies that a definition built without the DSL also rejects duplicate ids.
     */
    @Test
    fun `direct definitions reject duplicate ids`() {
        assertFailsWith<IllegalArgumentException> {
            ScreenDefinition(
                Component.text("Doppelt"),
                ColumnElement("root", listOf(LabelElement("same", Component.empty()), LabelElement("same", Component.empty()))),
            )
        }
    }

    /**
     * Verifies that icon elements reject sizes that are not positive.
     */
    @Test
    fun `icons need a positive size`() {
        assertFailsWith<IllegalArgumentException> { IconElement("i", "x", size = 0) }
    }

    /**
     * Verifies that buttons submit input unless told otherwise.
     */
    @Test
    fun `buttons submit input by default`() {
        val definition = Screen(Component.text("Knöpfe")) {
            Row(id = "root") {
                Button("OK", id = "ok")
                Button("Zurück", submitsInput = false, id = "back")
            }
        }

        val buttons = (definition.root as RowElement).children.map { it as ButtonElement }
        assertEquals(listOf(true, false), buttons.map { it.submitsInput })
    }
}
