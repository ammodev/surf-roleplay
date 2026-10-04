package dev.slne.surf.roleplay.api.client.paper.screen.gui

import dev.slne.surf.roleplay.api.client.common.screen.ScreenChange
import dev.slne.surf.roleplay.api.client.common.screen.ScreenPresentation
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.api.client.common.screen.SheetSide
import dev.slne.surf.roleplay.api.client.common.screen.TextElement
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
import dev.slne.surf.roleplay.api.client.common.screen.dsl.P
import net.kyori.adventure.text.Component
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests for the navigation between [GuiPage]s and for reopening a page in place.
 */
class GuiNavigationTest {
    /**
     * A page with a counter, a button per navigation call, and states for its title, theme,
     * variant and closability.
     *
     * @property declaredPresentation the presentation the page declares
     */
    class NavPage(private val declaredPresentation: ScreenPresentation = ScreenPresentation.SCREEN) : GuiPage() {
        /**
         * The page the navigation buttons open.
         */
        var target: GuiPage? = null

        /**
         * The shown counter.
         */
        var count by state(0)

        /**
         * Whether the page uses the dark variant.
         */
        var dark by state(false)

        /**
         * The text of the title.
         */
        var heading by state("Seite")

        /**
         * The theme name.
         */
        var themeName by state<String?>(null)

        /**
         * Whether the page can be closed with Escape.
         */
        var escapable by state(true)

        /**
         * The number of times [onClosed] ran.
         */
        var closedCount = 0

        /**
         * The title made of [heading].
         */
        override val title: Component get() = Component.text(heading)

        /**
         * The theme in [themeName].
         */
        override val theme: String? get() = themeName

        /**
         * The variant chosen by [dark].
         */
        override val variant: ScreenVariant get() = if (dark) ScreenVariant.DARK else ScreenVariant.LIGHT

        /**
         * The closability in [escapable].
         */
        override val closable: Boolean get() = escapable

        /**
         * The presentation given to the constructor.
         */
        override val presentation: ScreenPresentation get() = declaredPresentation

        /**
         * Renders the counter and the buttons.
         */
        override fun ComponentScope.render() {
            Column {
                P("Wert: $count", id = "value")
                Button("Plus", id = "plus") { count++ }
                Button("Weiter", id = "navigate") { navigate(target!!) }
                Button("Dialog", id = "dialog") { dialog(target!!) }
                Button("Leiste", id = "sheet") { sheet(target!!, SheetSide.LEFT) }
                Button("Zurück", id = "back") { back() }
                Button("Schließen", id = "close") { close() }
                Button("Dunkel", id = "dark") { dark = !dark }
            }
        }

        /**
         * Counts the calls.
         */
        override fun onClosed() {
            closedCount++
        }

        /**
         * Calls [navigate] from outside a handler.
         *
         * @param page the page to open
         */
        fun navigateNow(page: GuiPage) = navigate(page)

        /**
         * Calls [back] from outside a handler.
         */
        fun backNow() = back()
    }

    /**
     * Verifies that `navigate` opens the page on top of the current screen as a screen.
     */
    @Test
    fun `navigate opens a child screen`() {
        val fake = FakeOpener()
        val root = fake.open(NavPage())
        val child = NavPage()
        root.target = child
        val rootScreen = fake.lastScreen
        fake.click("navigate")
        val childScreen = fake.lastScreen
        assertNotSame(rootScreen, childScreen)
        assertSame(rootScreen, childScreen.parent)
        assertEquals(ScreenPresentation.SCREEN, childScreen.presentation)
        assertTrue(child.isOpen)
        assertTrue(root.isOpen)
    }

    /**
     * Verifies that `navigate` uses the presentation the child page declares.
     */
    @Test
    fun `navigate uses the declared presentation`() {
        val fake = FakeOpener()
        val root = fake.open(NavPage())
        root.target = NavPage(ScreenPresentation.DIALOG)
        fake.click("navigate")
        assertEquals(ScreenPresentation.DIALOG, fake.lastScreen.presentation)
    }

    /**
     * Verifies that a root page opens with the presentation it declares.
     */
    @Test
    fun `a root page opens with its declared presentation`() {
        val fake = FakeOpener()
        fake.open(NavPage(ScreenPresentation.DIALOG))
        assertEquals(ScreenPresentation.DIALOG, fake.lastScreen.presentation)
        assertEquals(null, fake.lastScreen.parent)
    }

    /**
     * Verifies that `dialog` opens the page on top of the current screen as a dialog, whatever the
     * page declares.
     */
    @Test
    fun `dialog opens a child dialog`() {
        val fake = FakeOpener()
        val root = fake.open(NavPage())
        root.target = NavPage(ScreenPresentation.SHEET)
        val rootScreen = fake.lastScreen
        fake.click("dialog")
        assertSame(rootScreen, fake.lastScreen.parent)
        assertEquals(ScreenPresentation.DIALOG, fake.lastScreen.presentation)
    }

    /**
     * Verifies that `sheet` opens the page on top of the current screen as a sheet at the given
     * side.
     */
    @Test
    fun `sheet opens a child sheet`() {
        val fake = FakeOpener()
        val root = fake.open(NavPage())
        root.target = NavPage(ScreenPresentation.DIALOG)
        val rootScreen = fake.lastScreen
        fake.click("sheet")
        assertSame(rootScreen, fake.lastScreen.parent)
        assertEquals(ScreenPresentation.SHEET, fake.lastScreen.presentation)
        assertEquals(SheetSide.LEFT, fake.lastScreen.sheetSide)
    }

    /**
     * Verifies that `back` closes the current page and runs its `onClosed`, and that the parent
     * page keeps its state and still handles clicks.
     */
    @Test
    fun `back closes the child and the parent stays usable`() {
        val fake = FakeOpener()
        val root = fake.open(NavPage())
        val rootScreen = fake.lastScreen
        fake.click("plus", rootScreen)
        val child = NavPage()
        root.target = child
        fake.click("navigate", rootScreen)
        val childScreen = fake.lastScreen
        fake.click("back", childScreen)
        assertFalse(childScreen.isOpen)
        assertFalse(child.isOpen)
        assertEquals(1, child.closedCount)
        assertTrue(rootScreen.isOpen)
        assertEquals(0, root.closedCount)
        assertEquals(1, root.count)
        fake.click("plus", rootScreen)
        assertEquals(2, root.count)
        assertEquals(listOf("value"), rootScreen.applied.map { (it as ScreenChange.Replace).targetId })
    }

    /**
     * Verifies that `back` on the root page closes the GUI.
     */
    @Test
    fun `back on the root page closes it`() {
        val fake = FakeOpener()
        val root = fake.open(NavPage())
        fake.click("back")
        assertFalse(root.isOpen)
        assertEquals(1, root.closedCount)
        assertTrue(fake.stack.isEmpty())
    }

    /**
     * Verifies that `close` on a child closes the root screen and runs every page's `onClosed`
     * once.
     */
    @Test
    fun `close closes the whole GUI`() {
        val fake = FakeOpener()
        val root = fake.open(NavPage())
        val middle = NavPage()
        val top = NavPage()
        root.target = middle
        middle.target = top
        fake.click("navigate")
        fake.click("dialog")
        val topScreen = fake.lastScreen
        fake.click("close", topScreen)
        assertTrue(fake.stack.isEmpty())
        assertEquals(listOf(1, 1, 1), listOf(root, middle, top).map { it.closedCount })
        assertEquals(listOf(1, 1, 1), fake.opened.map { it.closeHandlerRuns })
    }

    /**
     * Verifies that a page whose variant changes is reopened once with the new variant, with the
     * same parent and presentation, without running its `onClosed`, and keeps working.
     */
    @Test
    fun `a variant change reopens the page in place`() {
        val fake = FakeOpener()
        val root = fake.open(NavPage())
        val rootScreen = fake.lastScreen
        val child = NavPage()
        root.target = child
        fake.click("sheet", rootScreen)
        val oldScreen = fake.lastScreen
        fake.click("dark", oldScreen)
        val newScreen = fake.lastScreen
        assertNotSame(oldScreen, newScreen)
        assertEquals(3, fake.opened.size)
        assertEquals(ScreenVariant.DARK, newScreen.definition.variant)
        assertSame(rootScreen, newScreen.parent)
        assertEquals(ScreenPresentation.SHEET, newScreen.presentation)
        assertEquals(SheetSide.LEFT, newScreen.sheetSide)
        assertFalse(oldScreen.isOpen)
        assertTrue(oldScreen.patches.isEmpty())
        assertEquals(0, child.closedCount)
        assertTrue(child.isOpen)
        fake.click("plus", newScreen)
        assertEquals(1, child.count)
        assertEquals(listOf("value"), newScreen.applied.map { (it as ScreenChange.Replace).targetId })
        fake.click("back", newScreen)
        assertEquals(1, child.closedCount)
        assertTrue(rootScreen.isOpen)
    }

    /**
     * Verifies that a change of the title, theme or closability reopens the page.
     */
    @Test
    fun `title, theme and closability changes reopen the page`() {
        val fake = FakeOpener()
        val page = fake.open(NavPage())
        page.update { page.heading = "Neu" }
        assertEquals(Component.text("Neu"), fake.lastScreen.definition.title)
        page.update { page.themeName = "rose" }
        assertEquals("rose", fake.lastScreen.definition.theme)
        page.update { page.escapable = false }
        assertFalse(fake.lastScreen.definition.closable)
        assertEquals(4, fake.opened.size)
        assertEquals(0, page.closedCount)
        assertEquals(1, fake.stack.size)
    }

    /**
     * Verifies that the child pages of a reopened page are closed and their `onClosed` runs.
     */
    @Test
    fun `reopening closes the child pages`() {
        val fake = FakeOpener()
        val root = fake.open(NavPage())
        val rootScreen = fake.lastScreen
        val child = NavPage()
        root.target = child
        fake.click("navigate", rootScreen)
        fake.click("dark", rootScreen)
        assertFalse(child.isOpen)
        assertEquals(1, child.closedCount)
        assertEquals(0, root.closedCount)
        assertTrue(root.isOpen)
        assertEquals(1, fake.stack.size)
    }

    /**
     * Verifies that a reopen whose open fails keeps the page open on its old screen without
     * running `onClosed`, and that the next flush reopens it.
     */
    @Test
    fun `a failed reopen keeps the page open and retries`() {
        val fake = FakeOpener()
        val page = fake.open(NavPage())
        val screen = fake.lastScreen
        fake.failNextOpen = true
        assertFailsWith<IllegalStateException> { page.update { page.dark = true } }
        assertTrue(page.isOpen)
        assertTrue(screen.isOpen)
        assertEquals(0, page.closedCount)
        assertEquals(1, fake.opened.size)
        page.update {}
        assertEquals(2, fake.opened.size)
        assertEquals(ScreenVariant.DARK, fake.lastScreen.definition.variant)
        assertTrue(page.isOpen)
        assertEquals(0, page.closedCount)
    }

    /**
     * A child page that changes the counter of the page below it.
     *
     * @property below the page whose counter the child changes
     */
    class ParentWritingPage(private val below: NavPage) : GuiPage() {
        /**
         * A fixed title.
         */
        override val title: Component = Component.text("Kind")

        /**
         * Renders a button that changes the parent's counter and a button that does so and closes
         * the child.
         */
        override fun ComponentScope.render() {
            Column {
                Button("Plus unten", id = "plus_below") { below.count++ }
                Button("Plus und zurück", id = "plus_back") {
                    below.count++
                    back()
                }
            }
        }

        /**
         * Changes the parent's counter when the child closes.
         */
        override fun onClosed() {
            below.count += 10
        }
    }

    /**
     * Verifies that a child handler that changes the parent's state and goes back patches the
     * parent's screen.
     */
    @Test
    fun `a child handler that changes the parent state refreshes the parent`() {
        val fake = FakeOpener()
        val root = fake.open(NavPage())
        val rootScreen = fake.lastScreen
        root.target = ParentWritingPage(root)
        fake.click("navigate", rootScreen)
        fake.click("plus_below")
        assertEquals(Component.text("Wert: 1"), (rootScreen.find("value") as TextElement).text)
        fake.click("plus_back")
        assertEquals(12, root.count)
        assertEquals(Component.text("Wert: 12"), (rootScreen.find("value") as TextElement).text)
    }

    /**
     * Verifies that a child closed by the player whose `onClosed` changes the parent's state
     * patches the parent's screen.
     */
    @Test
    fun `a child closed by the player refreshes the parent`() {
        val fake = FakeOpener()
        val root = fake.open(NavPage())
        val rootScreen = fake.lastScreen
        root.target = ParentWritingPage(root)
        fake.click("navigate", rootScreen)
        fake.close(fake.lastScreen)
        assertEquals(10, root.count)
        assertEquals(Component.text("Wert: 10"), (rootScreen.find("value") as TextElement).text)
    }

    /**
     * Verifies that navigation calls fail while the page is not open.
     */
    @Test
    fun `navigation needs an open page`() {
        val fake = FakeOpener()
        val page = NavPage()
        assertFailsWith<IllegalStateException> { page.navigateNow(NavPage()) }
        fake.open(page)
        fake.lastScreen.close()
        assertFailsWith<IllegalStateException> { page.backNow() }
    }
}
