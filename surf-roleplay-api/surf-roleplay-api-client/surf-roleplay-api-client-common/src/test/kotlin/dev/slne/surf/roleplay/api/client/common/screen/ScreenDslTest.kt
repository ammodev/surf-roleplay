package dev.slne.surf.roleplay.api.client.common.screen

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

/**
 * Tests for the screen builder DSL, the patch builder and [ScreenValues].
 */
class ScreenDslTest {

    /**
     * Verifies that the DSL builds a definition with every element kind and their settings.
     */
    @Test
    fun `dsl builds every element kind`() {
        val handler = ButtonHandler { }
        val definition = screen(Component.text("Demo")) {
            closable = false
            column("root", gap = 4, padding = Spacing.all(2), crossAlign = Alignment.STRETCH, width = ElementSize.fixed(200)) {
                label("title", Component.text("Hallo"))
                row("inputs", gap = 2) {
                    textInput("name", value = "Max", placeholder = Component.text("Name"), maxLength = 16, required = true)
                    numberInput("age", value = 30, min = 18, max = 99, enabled = false)
                    checkbox("agree", Component.text("Ja"), checked = true)
                    select("city", listOf(SelectChoice("north", Component.text("Nord"))), selected = "north", required = true)
                }
                scrollList("list", height = ElementSize.fixed(60), gap = 1) {
                    button("open", Component.text("Öffnen"), onClick = handler)
                }
                image("logo", Key.key("surf-roleplay", "textures/gui/logo.png"))
                progress("load", 0.5f, Component.text("50%"))
            }
        }

        assertEquals(Component.text("Demo"), definition.title)
        assertEquals(false, definition.closable)
        val root = assertIs<ColumnElement>(definition.root)
        assertEquals(4, root.gap)
        assertEquals(Spacing.all(2), root.padding)
        assertEquals(Alignment.STRETCH, root.crossAlign)
        assertEquals(ElementSize.fixed(200), root.width)
        val inputs = assertIs<RowElement>(root.children[1])
        assertEquals(16, assertIs<TextInputElement>(inputs.children[0]).maxLength)
        assertEquals(false, assertIs<NumberInputElement>(inputs.children[1]).enabled)
        assertEquals(true, assertIs<CheckboxElement>(inputs.children[2]).checked)
        assertEquals("north", assertIs<SelectElement>(inputs.children[3]).selected)
        val list = assertIs<ScrollListElement>(root.children[2])
        assertSame(handler, assertIs<ButtonElement>(list.children[0]).onClick)
        assertEquals(Key.key("surf-roleplay", "textures/gui/logo.png"), assertIs<ImageElement>(root.children[3]).texture)
        assertEquals(0.5f, assertIs<ProgressElement>(root.children[4]).progress)
    }

    /**
     * Verifies that a screen needs exactly one root element.
     */
    @Test
    fun `a screen needs exactly one root`() {
        assertFailsWith<IllegalStateException> { screen(Component.text("Leer")) { } }
        assertFailsWith<IllegalStateException> {
            screen(Component.text("Zwei")) {
                label("a", Component.empty())
                label("b", Component.empty())
            }
        }
    }

    /**
     * Verifies that duplicate element ids in one screen are rejected.
     */
    @Test
    fun `duplicate element ids are rejected`() {
        assertFailsWith<IllegalArgumentException> {
            screen(Component.text("Doppelt")) {
                column("root") {
                    label("same", Component.empty())
                    label("same", Component.empty())
                }
            }
        }
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
     * Verifies that buttons submit input unless told otherwise.
     */
    @Test
    fun `buttons submit input by default`() {
        val definition = screen(Component.text("Knöpfe")) {
            row("root") {
                button("ok", Component.text("OK"))
                button("back", Component.text("Zurück"), submitsInput = false)
            }
        }

        val buttons = (definition.root as RowElement).children.map { it as ButtonElement }
        assertEquals(listOf(true, false), buttons.map { it.submitsInput })
    }

    /**
     * Verifies that icon elements reject sizes that are not positive.
     */
    @Test
    fun `icons need a positive size`() {
        assertFailsWith<IllegalArgumentException> { IconElement("i", "x", size = 0) }
    }
}
