package dev.slne.surf.roleplay.api.client.paper.screen.gui

import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
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
 * again and only the difference to the tree the player sees is sent, so inputs whose elements did
 * not change keep the text the player typed. Handlers always run the closure of the newest render.
 * A handler that throws is logged; the page is still rendered again and stays usable.
 *
 * Every member must be used on the player's region thread, where the page's handlers run.
 */
abstract class GuiPage {
    /**
     * The title shown above the screen.
     */
    abstract val title: Component

    /**
     * The name of the theme the screen is drawn with, or `null` for the default theme.
     */
    open val theme: String? get() = null

    /**
     * The light or dark variant of the theme, or `null` for the default variant.
     */
    open val variant: ScreenVariant? get() = null

    /**
     * Whether the player can close the screen with Escape.
     */
    open val closable: Boolean get() = true

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
    val player: Player get() = checkNotNull(session) { "The page ${javaClass.name} was not opened" }.player

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
     * Runs when the page's screen was closed for any reason.
     */
    open fun onClosed() {}

    /**
     * Renders the page and opens it as a screen for a player, replacing every screen the player
     * has open. Must be called on the player's region thread.
     *
     * @param player the player
     * @param service the service that opens the screen
     * @throws IllegalStateException if the page is already open
     * @throws IllegalArgumentException if an explicit element id starts with `_` or two elements
     *         share an id
     */
    fun open(player: Player, service: ScreenService = ScreenService.INSTANCE) {
        check(!isOpen) { "The page ${javaClass.name} is already open" }
        val session = GuiSession(this, player, service)
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
