package dev.slne.surf.roleplay.api.client.paper.screen.gui

import dev.slne.surf.roleplay.api.client.common.screen.ButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenChange
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Combobox
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Input
import dev.slne.surf.roleplay.api.client.common.screen.dsl.P
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Row
import net.kyori.adventure.text.Component
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for [GuiPage] and its runtime.
 */
class GuiPageTest {
    /**
     * A page with a counter, a conditional paragraph and buttons whose handlers capture the state
     * of their render.
     */
    class CounterPage : GuiPage() {
        /**
         * The shown counter.
         */
        var count by state(0)

        /**
         * A counter that is never shown.
         */
        var hidden by state(0)

        /**
         * Whether the extra paragraph and the hide button are shown.
         */
        var show by state(false)

        /**
         * Whether the next render throws.
         */
        var explode by state(false)

        /**
         * The title of the page.
         */
        override val title: Component = Component.text("Zähler")

        /**
         * Renders the counter, a search input, the conditional elements and the buttons.
         */
        override fun ComponentScope.render() {
            check(!explode) { "render failed" }
            val current = count
            Column {
                Input(id = "query")
                P("Wert: $current", id = "value")
                if (show) {
                    P("Extra", id = "extra")
                    Button("Ausblenden", id = "hide") { show = false }
                }
                Button("Plus", id = "plus") { count = current + 1 }
                Button("Unsichtbar", id = "hidden") { hidden++ }
                Button("Einblenden", id = "show") { show = true }
                Button("Kaputt", id = "broken") {
                    count = current + 1
                    error("broken handler")
                }
                Button("Explodieren", id = "boom") {
                    count = current + 1
                    explode = true
                }
            }
        }
    }

    /**
     * A page whose input and combobox handlers capture the state of their render.
     */
    class FormPage : GuiPage() {
        /**
         * The text of the name input, written by its change handler.
         */
        var name by state("")

        /**
         * The number of handled changes.
         */
        var edits by state(0)

        /**
         * The last query of the combobox.
         */
        var query by state("")

        /**
         * The number of handled searches.
         */
        var searches by state(0)

        /**
         * The title of the page.
         */
        override val title: Component = Component.text("Formular")

        /**
         * Renders the name input, the combobox and the counters.
         */
        override fun ComponentScope.render() {
            val currentEdits = edits
            val currentSearches = searches
            Column {
                Input(value = name, id = "name") { change ->
                    name = change.value
                    edits = currentEdits + 1
                }
                Combobox(id = "combo", onSearch = { search ->
                    query = search.query
                    searches = currentSearches + 1
                })
                P("Änderungen: $edits, Suchen: $searches", id = "counts")
            }
        }
    }

    /**
     * A page with one input bound to a state that its change handler writes, and a button that
     * clears it.
     */
    class BoundInputPage : GuiPage() {
        /**
         * The text of the input.
         */
        var name by state("")

        /**
         * The title of the page.
         */
        override val title: Component = Component.text("Eingabe")

        /**
         * Renders the input and the clear button.
         */
        override fun ComponentScope.render() {
            Column {
                Input(value = name, id = "name") { name = it.value }
                Button("Leeren", id = "clear") { name = "" }
            }
        }
    }

    /**
     * A page that moves the paragraph `x` between the rows `A` and `B`.
     */
    class MovePage : GuiPage() {
        /**
         * Whether `x` is in row `A`.
         */
        var inA by state(true)

        /**
         * The title of the page.
         */
        override val title: Component = Component.text("Verschieben")

        /**
         * Renders both rows and the move button.
         */
        override fun ComponentScope.render() {
            Column {
                Row(id = "A") { if (inA) P("x", id = "x") }
                Row(id = "B") { if (!inA) P("x", id = "x") }
                Button("Verschieben", id = "move") { inA = !inA }
            }
        }
    }

    /**
     * Verifies that opening a page opens its rendered tree with the page's title.
     */
    @Test
    fun `open renders the page`() {
        val fake = FakeOpener()
        val page = fake.open(CounterPage())
        assertEquals(Component.text("Zähler"), fake.lastScreen.definition.title)
        assertTrue(page.isOpen)
    }

    /**
     * Verifies that a state change patches only the element that shows the state.
     */
    @Test
    fun `a state change patches only the changed element`() {
        val fake = FakeOpener()
        fake.open(CounterPage())
        fake.click("plus")
        assertEquals(listOf("value"), fake.lastScreen.applied.map { (it as ScreenChange.Replace).targetId })
    }

    /**
     * Verifies that each click runs the handler of the newest render.
     */
    @Test
    fun `the newest closure handles the next click`() {
        val fake = FakeOpener()
        val page = fake.open(CounterPage())
        fake.click("plus")
        fake.click("plus")
        assertEquals(2, page.count)
        assertEquals(2, fake.lastScreen.patches.size)
    }

    /**
     * Verifies that change handlers are bound to their input and run the newest closure, and that
     * the value the player typed is not sent back to the input.
     */
    @Test
    fun `change handlers run the newest closure`() {
        val fake = FakeOpener()
        val page = fake.open(FormPage())
        fake.change("name", "a")
        fake.change("name", "ab")
        assertEquals(2, page.edits)
        assertEquals("ab", page.name)
        assertEquals(listOf("counts"), fake.lastScreen.applied.map { (it as ScreenChange.Replace).targetId })
        assertEquals(emptyList(), fake.lastScreen.refused)
    }

    /**
     * Verifies that a state-bound input whose change handler stores the reported value sends no
     * patch, because the screen already shows that value.
     */
    @Test
    fun `storing the reported value sends nothing`() {
        val fake = FakeOpener()
        val page = fake.open(BoundInputPage())
        fake.change("name", "abc")
        assertEquals("abc", page.name)
        assertTrue(fake.lastScreen.patches.isEmpty())
    }

    /**
     * Verifies that a state change after a reported value sends the state's value to the input,
     * because the screen shows the reported value and not the one rendered before.
     */
    @Test
    fun `a value set after a reported value is sent`() {
        val fake = FakeOpener()
        val page = fake.open(BoundInputPage())
        fake.change("name", "abc")
        fake.click("clear")
        assertEquals("", page.name)
        assertEquals(listOf(ScreenChange.SetValue("name", "")), fake.lastScreen.applied)
    }

    /**
     * Verifies that search handlers are bound to their combobox and run the newest closure.
     */
    @Test
    fun `search handlers run the newest closure`() {
        val fake = FakeOpener()
        val page = fake.open(FormPage())
        fake.search("combo", "a")
        fake.search("combo", "ab")
        assertEquals(2, page.searches)
        assertEquals("ab", page.query)
        assertEquals(listOf("counts"), fake.lastScreen.applied.map { (it as ScreenChange.Replace).targetId })
    }

    /**
     * Verifies that conditionally rendered elements are inserted and removed.
     */
    @Test
    fun `conditional elements are inserted and removed`() {
        val fake = FakeOpener()
        fake.open(CounterPage())
        fake.click("show")
        val inserts = fake.lastScreen.applied.map { assertIs<ScreenChange.Insert>(it) }
        assertEquals(listOf(2 to "extra", 3 to "hide"), inserts.map { it.index to it.element.id })
        fake.click("hide")
        assertEquals(listOf(ScreenChange.Remove("extra"), ScreenChange.Remove("hide")), fake.lastScreen.applied)
        assertEquals(emptyList(), fake.lastScreen.refused)
    }

    /**
     * Verifies that a click on a button whose element is no longer rendered does nothing.
     */
    @Test
    fun `a click on a vanished element does nothing`() {
        val fake = FakeOpener()
        val page = fake.open(CounterPage())
        fake.click("show")
        val hide = fake.element<ButtonElement>("hide")
        fake.click(hide)
        val patches = fake.lastScreen.patches.size
        page.update { page.count = 7 }
        fake.click(hide)
        assertFalse(page.show)
        assertEquals(patches + 1, fake.lastScreen.patches.size)
    }

    /**
     * Verifies that a click after the screen closed runs no handler and sends nothing.
     */
    @Test
    fun `a click after close does nothing`() {
        val fake = FakeOpener()
        val page = fake.open(CounterPage())
        val plus = fake.element<ButtonElement>("plus")
        fake.lastScreen.close()
        fake.click(plus)
        assertEquals(0, page.count)
        assertTrue(fake.lastScreen.patches.isEmpty())
    }

    /**
     * Verifies that a state change inside [GuiPage.update] re-renders the page.
     */
    @Test
    fun `update re-renders`() {
        val fake = FakeOpener()
        val page = fake.open(CounterPage())
        page.update { page.count = 5 }
        val change = fake.lastScreen.applied.single() as ScreenChange.Replace
        assertEquals("value", change.targetId)
    }

    /**
     * Verifies that a handler that throws still re-renders the dirty page and that the page keeps
     * handling clicks.
     */
    @Test
    fun `a throwing handler still re-renders and the page stays usable`() {
        val fake = FakeOpener()
        val page = fake.open(CounterPage())
        fake.click("broken")
        assertEquals(listOf("value"), fake.lastScreen.applied.map { (it as ScreenChange.Replace).targetId })
        fake.click("plus")
        assertEquals(2, page.count)
        assertEquals(2, fake.lastScreen.patches.size)
    }

    /**
     * Verifies that a render that throws leaves the screen unchanged and the page dirty, so that
     * the next successful render sends the change.
     */
    @Test
    fun `a throwing render keeps the page dirty and the screen unchanged`() {
        val fake = FakeOpener()
        val page = fake.open(CounterPage())
        fake.click("boom")
        assertEquals(1, page.count)
        assertTrue(fake.lastScreen.patches.isEmpty())
        page.update { page.explode = false }
        assertEquals(listOf("value"), fake.lastScreen.applied.map { (it as ScreenChange.Replace).targetId })
        assertEquals(emptyList(), fake.lastScreen.refused)
    }

    /**
     * Verifies that a re-render whose tree did not change sends no patch.
     */
    @Test
    fun `a render without changes sends nothing`() {
        val fake = FakeOpener()
        val page = fake.open(CounterPage())
        fake.click("hidden")
        assertEquals(1, page.hidden)
        assertTrue(fake.lastScreen.patches.isEmpty())
    }

    /**
     * Verifies that an element moving between containers back and forth is never refused and
     * that the screen's tree follows the page.
     */
    @Test
    fun `a moving element keeps the screen consistent`() {
        val fake = FakeOpener()
        fake.open(MovePage())
        fake.click("move")
        assertEquals("B", fake.lastScreen.parentOf("x"))
        fake.click("move")
        assertEquals("A", fake.lastScreen.parentOf("x"))
        assertEquals(emptyList(), fake.lastScreen.refused)
    }

    /**
     * Verifies that closing the screen runs [GuiPage.onClosed] and marks the page closed.
     */
    @Test
    fun `closing the screen closes the page`() {
        val fake = FakeOpener()
        var closed = 0
        val page = object : GuiPage() {
            /**
             * The title of the page.
             */
            override val title: Component = Component.text("Leer")

            /**
             * Renders one paragraph.
             */
            override fun ComponentScope.render() {
                P("leer")
            }

            /**
             * Counts the calls.
             */
            override fun onClosed() {
                closed++
            }
        }
        fake.open(page)
        fake.lastScreen.close()
        assertFalse(page.isOpen)
        assertEquals(1, closed)
    }

    /**
     * Verifies that [GuiPage.player] cannot be read before the page was opened.
     */
    @Test
    fun `player fails before open`() {
        assertFailsWith<IllegalStateException> { CounterPage().player }
    }
}
