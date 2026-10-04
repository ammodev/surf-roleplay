package dev.slne.surf.roleplay.api.client.paper.screen.gui

import dev.slne.surf.roleplay.api.client.common.screen.ButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.ContainerElement
import dev.slne.surf.roleplay.api.client.common.screen.OpenScreen
import dev.slne.surf.roleplay.api.client.common.screen.ScreenChange
import dev.slne.surf.roleplay.api.client.common.screen.ScreenClick
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.ScreenElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenPatchBuilder
import dev.slne.surf.roleplay.api.client.common.screen.ScreenPresentation
import dev.slne.surf.roleplay.api.client.common.screen.ScreenValues
import dev.slne.surf.roleplay.api.client.common.screen.SheetSide
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Input
import dev.slne.surf.roleplay.api.client.common.screen.dsl.P
import dev.slne.surf.roleplay.api.client.paper.screen.ScreenService
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.lang.reflect.Proxy
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests for [GuiPage] and its runtime.
 */
class GuiPageTest {
    /**
     * A page with a counter shown in a paragraph and buttons that change the page's state.
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
         * The title of the page.
         */
        override val title: Component = Component.text("Zähler")

        /**
         * Renders the counter, a search input and the buttons.
         */
        override fun ComponentScope.render() {
            Column {
                Input(id = "query")
                P("Wert: $count", id = "value")
                Button("Plus", id = "plus") { count++ }
                Button("Unsichtbar", id = "hidden") { hidden++ }
                Button("Kaputt", id = "broken") {
                    count++
                    error("broken handler")
                }
            }
        }
    }

    /**
     * An open screen that records every patch applied to it.
     *
     * @property definition the definition the screen was opened with
     */
    class FakeOpenScreen(val definition: ScreenDefinition) : OpenScreen {
        /**
         * The changes of every patch, in order.
         */
        val patches = mutableListOf<List<ScreenChange>>()

        /**
         * The changes of the last patch, or an empty list if no patch was applied.
         */
        val applied: List<ScreenChange> get() = patches.lastOrNull().orEmpty()

        /**
         * Always `1`.
         */
        override val sessionId: Int = 1

        /**
         * A random viewer id.
         */
        override val viewer: UUID = UUID.randomUUID()

        /**
         * Whether [close] was not called yet.
         */
        override var isOpen: Boolean = true

        /**
         * Records the changes of the patch.
         *
         * @param changes the builder of the changes
         */
        override fun patch(changes: ScreenPatchBuilder.() -> Unit) {
            patches += ScreenPatchBuilder().apply(changes).changes
        }

        /**
         * Does nothing.
         *
         * @param errors unused
         */
        override fun showErrors(errors: Map<String, Component>) = Unit

        /**
         * Closes the screen and runs its close handler.
         */
        override fun close() {
            if (!isOpen) return
            isOpen = false
            definition.onClose?.onClose(this)
        }
    }

    /**
     * A screen service that records opened screens and lets a test click buttons.
     */
    class FakeScreenService : ScreenService {
        /**
         * Every opened screen, in order.
         */
        val opened = mutableListOf<FakeOpenScreen>()

        /**
         * The last opened screen.
         */
        val lastScreen: FakeOpenScreen get() = opened.last()

        /**
         * Records a screen as opened.
         *
         * @return the fake open screen
         */
        override fun open(player: Player, definition: ScreenDefinition, parent: OpenScreen?, presentation: ScreenPresentation, sheetSide: SheetSide): OpenScreen =
            FakeOpenScreen(definition).also { opened += it }

        /**
         * Not supported.
         */
        override fun confirm(
            player: Player, parent: OpenScreen?, title: Component, text: Component, confirmLabel: Component,
            cancelLabel: Component, destructive: Boolean, onConfirm: () -> Unit, onCancel: () -> Unit,
        ): OpenScreen = throw UnsupportedOperationException()

        /**
         * Returns the open fake screens.
         *
         * @return the screens
         */
        override fun openScreens(player: Player): List<OpenScreen> = opened.filter { it.isOpen }

        /**
         * Closes every fake screen.
         */
        override fun closeAll(player: Player) = opened.forEach { it.close() }

        /**
         * Clicks the button with an id on the last screen, using the newest copy of the button
         * found in the patches or in the opened tree.
         *
         * @param id the button id
         */
        fun click(id: String) {
            val screen = lastScreen
            val candidates = screen.patches.asReversed().flatMap { patch ->
                patch.mapNotNull {
                    when (it) {
                        is ScreenChange.Replace -> it.element
                        is ScreenChange.Insert -> it.element
                        else -> null
                    }
                }
            } + screen.definition.root
            val button = candidates.firstNotNullOfOrNull { findButton(it, id) } ?: error("No button $id")
            button.onClick!!.onClick(ScreenClick(screen, id, ScreenValues(emptyMap())))
        }

        /**
         * Finds a button by id in a tree.
         *
         * @param element the root of the tree
         * @param id the button id
         * @return the button, or `null` if the tree has none with that id
         */
        private fun findButton(element: ScreenElement, id: String): ButtonElement? = when {
            element is ButtonElement && element.id == id -> element
            element is ContainerElement -> element.children.firstNotNullOfOrNull { findButton(it, id) }
            else -> null
        }
    }

    /**
     * Creates a player whose methods return `null`, except the identity methods.
     *
     * @return the player
     */
    private fun fakePlayer(): Player = Proxy.newProxyInstance(Player::class.java.classLoader, arrayOf(Player::class.java)) { proxy, method, args ->
        when (method.name) {
            "equals" -> proxy === args?.get(0)
            "hashCode" -> System.identityHashCode(proxy)
            "toString" -> "FakePlayer"
            else -> null
        }
    } as Player

    /**
     * Verifies that opening a page opens its rendered tree with the page's title.
     */
    @Test
    fun `open renders the page`() {
        val fake = FakeScreenService()
        val player = fakePlayer()
        val page = CounterPage().apply { open(player, fake) }
        assertEquals(Component.text("Zähler"), fake.lastScreen.definition.title)
        assertTrue(page.isOpen)
        assertSame(player, page.player)
    }

    /**
     * Verifies that a state change patches only the element that shows the state.
     */
    @Test
    fun `a state change patches only the changed element`() {
        val fake = FakeScreenService()
        CounterPage().apply { open(fakePlayer(), fake) }
        fake.click("plus")
        assertEquals(listOf("value"), fake.lastScreen.applied.map { (it as ScreenChange.Replace).targetId })
    }

    /**
     * Verifies that each click runs the handler of the newest render.
     */
    @Test
    fun `the newest closure handles the next click`() {
        val fake = FakeScreenService()
        val page = CounterPage().apply { open(fakePlayer(), fake) }
        fake.click("plus")
        fake.click("plus")
        assertEquals(2, page.count)
        assertEquals(2, fake.lastScreen.patches.size)
    }

    /**
     * Verifies that a state change inside [GuiPage.update] re-renders the page.
     */
    @Test
    fun `update re-renders`() {
        val fake = FakeScreenService()
        val page = CounterPage().apply { open(fakePlayer(), fake) }
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
        val fake = FakeScreenService()
        val page = CounterPage().apply { open(fakePlayer(), fake) }
        fake.click("broken")
        assertEquals(listOf("value"), fake.lastScreen.applied.map { (it as ScreenChange.Replace).targetId })
        fake.click("plus")
        assertEquals(2, page.count)
        assertEquals(2, fake.lastScreen.patches.size)
    }

    /**
     * Verifies that a re-render whose tree did not change sends no patch.
     */
    @Test
    fun `a render without changes sends nothing`() {
        val fake = FakeScreenService()
        val page = CounterPage().apply { open(fakePlayer(), fake) }
        fake.click("hidden")
        assertEquals(1, page.hidden)
        assertTrue(fake.lastScreen.patches.isEmpty())
    }

    /**
     * Verifies that closing the screen runs [GuiPage.onClosed] and marks the page closed.
     */
    @Test
    fun `closing the screen closes the page`() {
        val fake = FakeScreenService()
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
        page.open(fakePlayer(), fake)
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
