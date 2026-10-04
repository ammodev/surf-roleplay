package dev.slne.surf.roleplay.api.client.common.screen.dsl

import dev.slne.surf.roleplay.api.client.common.screen.ColumnElement
import dev.slne.surf.roleplay.api.client.common.screen.LabelElement
import dev.slne.surf.roleplay.api.client.common.screen.OpenScreen
import dev.slne.surf.roleplay.api.client.common.screen.ScreenClick
import dev.slne.surf.roleplay.api.client.common.screen.ScreenInputChange
import dev.slne.surf.roleplay.api.client.common.screen.ScreenPatchBuilder
import dev.slne.surf.roleplay.api.client.common.screen.ScreenValues
import net.kyori.adventure.text.Component
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the component DSL: generated ids, typed input references and the components.
 */
class ComponentDslTest {

    /**
     * Adds a column whose children are built by a block, using only the scope primitives.
     *
     * @param id the explicit id, or `null` for a generated one
     * @param block the builder of the children
     * @return the column
     */
    private fun ComponentScope.column(id: String? = null, block: ComponentScope.() -> Unit): ColumnElement {
        val elementId = nextId(id)
        return add(ColumnElement(elementId, children(block)))
    }

    /**
     * Adds a label, using only the scope primitives.
     *
     * @param id the explicit id, or `null` for a generated one
     * @return the label
     */
    private fun ComponentScope.label(id: String? = null): LabelElement = add(LabelElement(nextId(id), Component.empty()))

    /**
     * Verifies that the scope generates ids from the position of every element.
     */
    @Test
    fun `the scope generates ids from the position`() {
        val root = assertIs<ColumnElement>(
            renderRoot {
                column {
                    label()
                    label(id = "named")
                    column { label() }
                }
            },
        )
        assertEquals("_0", root.id)
        assertEquals("_0.0", root.children[0].id)
        assertEquals("named", root.children[1].id)
        assertEquals("_0.2", root.children[2].id)
        assertEquals("_0.2.0", (root.children[2] as ColumnElement).children[0].id)
    }

    /**
     * Verifies that explicit ids starting with an underscore are rejected.
     */
    @Test
    fun `the scope rejects explicit ids with an underscore`() {
        assertFailsWith<IllegalArgumentException> { renderRoot { label(id = "_x") } }
    }

    /**
     * Verifies that rendering needs exactly one root element.
     */
    @Test
    fun `rendering needs exactly one root`() {
        assertFailsWith<IllegalStateException> { renderRoot { } }
        assertFailsWith<IllegalStateException> { renderRoot { label(); label() } }
    }

    /**
     * Verifies that a screen carries its options and the rendered root.
     */
    @Test
    fun `a screen carries its options`() {
        val definition = Screen(Component.text("T"), theme = "police", closable = false) { label(id = "root") }
        assertEquals(Component.text("T"), definition.title)
        assertEquals("police", definition.theme)
        assertEquals(false, definition.closable)
        assertEquals("root", definition.root.id)
    }

    /**
     * Verifies that the parsers of input references read every wire format.
     */
    @Test
    fun `input reference parsers read the wire formats`() {
        val values = ScreenValues(
            mapOf(
                "text" to "Ada",
                "number" to "42",
                "emptyNumber" to "",
                "checked" to "true",
                "unchecked" to "false",
                "selected" to "north",
                "noSelection" to "",
                "list" to "a, b",
                "emptyList" to "",
                "slider" to "10.0,20.5",
                "dates" to "2026-01-02,2026-01-05",
                "range" to "2026-01-02/2026-01-05",
            ),
        )
        assertEquals("Ada", values[InputRef("text", InputParsers.text)])
        assertEquals("", values[InputRef("missing", InputParsers.text)])
        assertEquals(42L, values[InputRef("number", InputParsers.number)])
        assertNull(values[InputRef("emptyNumber", InputParsers.number)])
        assertTrue(values[InputRef("checked", InputParsers.checked)])
        assertEquals(false, values[InputRef("unchecked", InputParsers.checked)])
        assertEquals(false, values[InputRef("missing", InputParsers.checked)])
        assertEquals("north", values[InputRef("selected", InputParsers.selected)])
        assertNull(values[InputRef("noSelection", InputParsers.selected)])
        assertEquals(listOf("a", "b"), values[InputRef("list", InputParsers.list)])
        assertEquals(emptyList(), values[InputRef("emptyList", InputParsers.list)])
        assertEquals(listOf(10.0, 20.5), values[InputRef("slider", InputParsers.numbers)])
        assertEquals(emptyList(), values[InputRef("missing", InputParsers.numbers)])
        val dates = listOf(LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 5))
        assertEquals(dates, values[InputRef("dates", InputParsers.dates)])
        assertEquals(dates, values[InputRef("range", InputParsers.dates)])
        assertEquals(emptyList(), values[InputRef("missing", InputParsers.dates)])
    }

    /**
     * Verifies that clicks and changes forward typed reads to their values.
     */
    @Test
    fun `clicks and changes read typed values`() {
        val screen = object : OpenScreen {
            /** A fixed session id. */
            override val sessionId: Int = 1

            /** A fixed viewer. */
            override val viewer: UUID = UUID(0, 0)

            /** Always open. */
            override val isOpen: Boolean = true

            /** Ignores the patch. */
            override fun patch(changes: ScreenPatchBuilder.() -> Unit) = Unit

            /** Ignores the errors. */
            override fun showErrors(errors: Map<String, Component>) = Unit

            /** Does nothing. */
            override fun close() = Unit
        }
        val values = ScreenValues(mapOf("age" to "7"))
        val age = InputRef("age", InputParsers.number)
        assertEquals(7L, ScreenClick(screen, "save", values)[age])
        assertEquals(7L, ScreenInputChange(screen, "age", "7", values)[age])
    }
}
