package dev.slne.surf.roleplay.api.client.paper.screen.gui

import dev.slne.surf.roleplay.api.client.common.screen.ScreenPresentation
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.api.client.common.screen.SheetSide
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
import dev.slne.surf.roleplay.api.client.paper.screen.ScreenService
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A screen written as a class: it renders its tree with the component DSL from its state, and
 * updates the open screen whenever its state changes.
 *
 * ```
 * class CounterPage : GuiPage() {
 *     var count by state(0)
 *     override val title = Component.text("Zähler")
 *     override fun ComponentScope.render() {
 *         Column {
 *             P("Wert: $count")
 *             Button("Plus") { count++ }
 *         }
 *     }
 * }
 * ```
 *
 * After every handler of the page, and after [update], a page whose state changed is rendered
 * again and only the difference to the tree the player sees is sent: inputs whose elements did not
 * change keep the text the player typed, and an input whose only change is its value gets just the
 * new value. Handlers always run the closure of the newest render. A handler that throws is
 * logged; the page is still rendered again and stays usable. A render that throws is logged and
 * leaves the screen unchanged until a later render succeeds. A render whose [title], [theme],
 * [variant] or [closable] changed reopens the page in place: on top of the same screen and with
 * the same presentation, without running [onClosed]; the pages opened on top of it are closed.
 *
 * A page can open other pages on top of its screen with [navigate], [dialog] and [sheet]; [back]
 * closes the page and [close] closes the whole GUI. Each open page renders and handles its own
 * screen.
 *
 * Every member must be used on the player's region thread, where the page's handlers run.
 */
abstract class GuiPage {
    /**
     * The title shown above the screen, read at every render.
     */
    abstract val title: Component

    /**
     * The name of the theme the screen is drawn with, or `null` for the parent screen's or the
     * default theme, read at every render.
     */
    open val theme: String? get() = null

    /**
     * The light or dark variant of the theme, or `null` for the parent screen's or the default
     * variant, read at every render.
     */
    open val variant: ScreenVariant? get() = null

    /**
     * Whether the player can close the screen with Escape, read at every render.
     */
    open val closable: Boolean get() = true

    /**
     * How the page's screen is shown relative to the screens below it when it is opened with
     * [open] or [navigate]. [dialog] and [sheet] choose their own presentation.
     */
    open val presentation: ScreenPresentation get() = ScreenPresentation.SCREEN

    /**
     * The window edge the page's screen is attached to when it is opened as a sheet with [open]
     * or [navigate]. [sheet] chooses its own side.
     */
    open val sheetSide: SheetSide get() = SheetSide.RIGHT

    /**
     * The runtime of the page once it was opened, or `null` before.
     */
    private var session: GuiSession? = null

    /**
     * Renders the page's tree from its current state. It must add exactly one root element.
     */
    abstract fun ComponentScope.render()

    /**
     * Declares a state of the page. Writing a value that differs from the current one marks the
     * page as changed, so that it is rendered again after the running handler or [update].
     *
     * @param initial the initial value
     * @return the delegate of the state property
     */
    protected fun <T> state(initial: T): ReadWriteProperty<GuiPage, T> = State(initial)

    /**
     * The player the page was opened for.
     *
     * @throws IllegalStateException if the page was not opened yet
     */
    val player: Player get() = checkNotNull(session) { "The page ${javaClass.name} was not opened" }.viewer as Player

    /**
     * Whether the page's screen is open.
     */
    val isOpen: Boolean get() = session?.isOpen == true

    /**
     * Runs a block, such as one that changes state from outside a handler, and renders the page
     * again afterwards if its state changed. Must be called on the player's region thread.
     *
     * @param block the block
     */
    fun update(block: () -> Unit) {
        val session = session
        if (session == null) block() else session.update(block)
    }

    /**
     * Runs when the page's screen was closed for any reason other than reopening the page in
     * place.
     */
    open fun onClosed() {}

    /**
     * Renders the page and opens it as a screen for a player, replacing every screen the player
     * has open, with the page's [presentation] and [sheetSide]. Must be called on the player's
     * region thread.
     *
     * @param player the player
     * @param service the service that opens the screen
     * @throws IllegalStateException if the page is already open
     * @throws IllegalArgumentException if an explicit element id starts with `_` or two elements
     *         share an id
     */
    fun open(player: Player, service: ScreenService = ScreenService.INSTANCE) {
        openFor(player) { definition, parent, presentation, sheetSide -> service.open(player, definition, parent, presentation, sheetSide) }
    }

    /**
     * Renders the page and opens it as the root of a GUI, with the page's [presentation] and
     * [sheetSide].
     *
     * @param viewer the viewer the page is opened for, which [player] returns
     * @param opener opens a definition as a screen and returns the open screen
     * @throws IllegalStateException if the page is already open
     * @throws IllegalArgumentException if an explicit element id starts with `_` or two elements
     *         share an id
     */
    internal fun openFor(viewer: Any, opener: ScreenOpener) {
        start(viewer, opener, null, presentation, sheetSide)
    }

    /**
     * Opens another page on top of this page's screen, with the presentation and sheet side the
     * other page declares. Must be called on the player's region thread.
     *
     * @param page the page to open
     * @throws IllegalStateException if this page is not open or [page] is already open
     * @throws IllegalArgumentException if an explicit element id of [page] starts with `_` or two
     *         of its elements share an id
     */
    protected fun navigate(page: GuiPage) {
        openChild(page, page.presentation, page.sheetSide)
    }

    /**
     * Opens another page as a dialog on top of this page's screen. Must be called on the player's
     * region thread.
     *
     * @param page the page to open
     * @throws IllegalStateException if this page is not open or [page] is already open
     * @throws IllegalArgumentException if an explicit element id of [page] starts with `_` or two
     *         of its elements share an id
     */
    protected fun dialog(page: GuiPage) {
        openChild(page, ScreenPresentation.DIALOG, page.sheetSide)
    }

    /**
     * Opens another page as a sheet on top of this page's screen. Must be called on the player's
     * region thread.
     *
     * @param page the page to open
     * @param side the window edge the sheet is attached to
     * @throws IllegalStateException if this page is not open or [page] is already open
     * @throws IllegalArgumentException if an explicit element id of [page] starts with `_` or two
     *         of its elements share an id
     */
    protected fun sheet(page: GuiPage, side: SheetSide = SheetSide.RIGHT) {
        openChild(page, ScreenPresentation.SHEET, side)
    }

    /**
     * Closes this page's screen together with the pages opened on top of it, and shows the page
     * below again. On the root page of a GUI it closes the GUI. Must be called on the player's
     * region thread.
     *
     * @throws IllegalStateException if this page is not open
     */
    protected fun back() {
        openSession().close()
    }

    /**
     * Closes the whole GUI this page belongs to: the screen of its root page and every page
     * opened on top of it. Must be called on the player's region thread.
     *
     * @throws IllegalStateException if this page is not open
     */
    protected fun close() {
        openSession().root.close()
    }

    /**
     * Returns the runtime of the page while its screen is open.
     *
     * @return the runtime
     * @throws IllegalStateException if the page is not open
     */
    private fun openSession(): GuiSession {
        val session = session
        check(session != null && session.isOpen) { "The page ${javaClass.name} is not open" }
        return session
    }

    /**
     * Opens another page on top of this page's screen.
     *
     * @param page the page to open
     * @param presentation how its screen is shown
     * @param sheetSide the window edge its screen is attached to as a sheet
     * @throws IllegalStateException if this page is not open or [page] is already open
     */
    private fun openChild(page: GuiPage, presentation: ScreenPresentation, sheetSide: SheetSide) {
        val parent = openSession()
        page.start(parent.viewer, parent.opener, parent, presentation, sheetSide)
    }

    /**
     * Renders the page and opens it.
     *
     * @param viewer the viewer the page is opened for
     * @param opener opens a definition as a screen
     * @param parent the runtime of the page to open on top of, or `null` for the root of a GUI
     * @param presentation how the screen is shown
     * @param sheetSide the window edge the screen is attached to as a sheet
     * @throws IllegalStateException if the page is already open
     */
    private fun start(viewer: Any, opener: ScreenOpener, parent: GuiSession?, presentation: ScreenPresentation, sheetSide: SheetSide) {
        check(!isOpen) { "The page ${javaClass.name} is already open" }
        val session = GuiSession(this, viewer, opener, parent, presentation, sheetSide)
        this.session = session
        session.open()
    }

    /**
     * Marks the page as changed, if it was opened.
     */
    private fun markDirty() {
        session?.markDirty()
    }

    /**
     * The delegate of a state property.
     *
     * @param T the type of the value
     * @property value the current value
     */
    private class State<T>(private var value: T) : ReadWriteProperty<GuiPage, T> {
        /**
         * Returns the current value.
         *
         * @param thisRef the page
         * @param property the property
         * @return the value
         */
        override fun getValue(thisRef: GuiPage, property: KProperty<*>): T = value

        /**
         * Sets the value and marks the page as changed if it differs from the current one.
         *
         * @param thisRef the page
         * @param property the property
         * @param value the new value
         */
        override fun setValue(thisRef: GuiPage, property: KProperty<*>, value: T) {
            if (this.value == value) return
            this.value = value
            thisRef.markDirty()
        }
    }
}
