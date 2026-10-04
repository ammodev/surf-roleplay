package dev.slne.surf.roleplay.api.client.common.screen.dsl

import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ChangeHandler
import dev.slne.surf.roleplay.api.client.common.screen.CloseHandler
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.ScreenElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.api.client.common.screen.SearchHandler
import net.kyori.adventure.text.Component

/**
 * Marks the scopes of the component DSL, so that an inner block cannot add elements to an outer
 * scope by accident.
 */
@DslMarker
annotation class ComponentDsl

/**
 * Binds the handlers created while a tree is rendered to the ids of their elements. Every
 * component passes each handler it receives through the binder and stores the handler the binder
 * returns.
 */
interface HandlerBinder {
    /**
     * Binds the click or press handler of a button or toggle.
     *
     * @param elementId the id of the element the handler belongs to
     * @param handler the handler given to the component
     * @return the handler the element stores
     */
    fun button(elementId: String, handler: ButtonHandler): ButtonHandler

    /**
     * Binds the change handler of an input.
     *
     * @param elementId the id of the input the handler belongs to
     * @param handler the handler given to the component
     * @return the handler the element stores
     */
    fun change(elementId: String, handler: ChangeHandler): ChangeHandler

    /**
     * Binds the search handler of a combobox.
     *
     * @param elementId the id of the combobox the handler belongs to
     * @param handler the handler given to the component
     * @return the handler the element stores
     */
    fun search(elementId: String, handler: SearchHandler): SearchHandler

    /**
     * Holds the identity binder.
     */
    companion object {
        /**
         * The binder that returns every handler unchanged.
         */
        val IDENTITY: HandlerBinder = object : HandlerBinder {
            /**
             * Returns [handler] unchanged.
             *
             * @param elementId the id of the element, unused
             * @param handler the handler
             * @return [handler]
             */
            override fun button(elementId: String, handler: ButtonHandler): ButtonHandler = handler

            /**
             * Returns [handler] unchanged.
             *
             * @param elementId the id of the input, unused
             * @param handler the handler
             * @return [handler]
             */
            override fun change(elementId: String, handler: ChangeHandler): ChangeHandler = handler

            /**
             * Returns [handler] unchanged.
             *
             * @param elementId the id of the combobox, unused
             * @param handler the handler
             * @return [handler]
             */
            override fun search(elementId: String, handler: SearchHandler): SearchHandler = handler
        }
    }
}

/**
 * Collects the elements built by a block of the component DSL, either the root of a tree or the
 * children of one container.
 *
 * Elements without an explicit id get a generated one from their position: `_` followed by the
 * child indices from the root, joined by dots. The root element is `_0`, and the children of the
 * element at path `[0, 2]` are `_0.2.0`, `_0.2.1` and so on. Every element added to a scope counts
 * for the position, whatever its id.
 *
 * A scope is closed once its block has run; components called later, such as from a handler,
 * fail instead of changing a tree that is already built.
 *
 * @property path the child indices from the root to the element whose children this scope holds;
 *           empty for the root scope
 * @property binder the binder every handler of the tree passes through
 */
@ComponentDsl
open class ComponentScope internal constructor(internal val path: List<Int>, internal val binder: HandlerBinder) {
    /**
     * The collected elements, in order.
     */
    internal val elements: MutableList<ScreenElement> = mutableListOf()

    /**
     * Whether the block of this scope has run, after which no element can be added.
     */
    private var closed: Boolean = false

    /**
     * Runs a block on this scope and closes the scope, even if the block fails.
     *
     * @param block the builder of the elements
     * @return this scope
     */
    internal fun build(block: ComponentScope.() -> Unit): ComponentScope {
        try {
            block()
        } finally {
            closed = true
        }
        return this
    }

    /**
     * Checks that elements can still be added to this scope.
     *
     * @throws IllegalStateException if the scope was already built
     */
    private fun checkOpen() {
        check(!closed) { "Components cannot be added after their scope was built" }
    }

    /**
     * Returns [explicit] after checking it does not start with `_`, or the generated id of the next
     * child.
     *
     * @param explicit the id given to the component, or `null` to generate one
     * @return the id of the next child
     * @throws IllegalArgumentException if [explicit] starts with `_`, which is reserved for
     *         generated ids
     * @throws IllegalStateException if the scope was already built
     */
    internal fun nextId(explicit: String?): String {
        checkOpen()
        if (explicit != null) {
            require(!explicit.startsWith(GENERATED_PREFIX)) { "Explicit element ids may not start with '$GENERATED_PREFIX': $explicit" }
            return explicit
        }
        return GENERATED_PREFIX + (path + elements.size).joinToString(".")
    }

    /**
     * Builds the children of the element with the next index into a fresh scope.
     *
     * @param block the builder of the children
     * @return the built children, in order
     */
    internal fun children(block: ComponentScope.() -> Unit): List<ScreenElement> =
        ComponentScope(path + elements.size, binder).build(block).elements.toList()

    /**
     * Adds an element as the next child.
     *
     * @param element the element
     * @return [element]
     * @throws IllegalStateException if the scope was already built
     */
    internal fun <E : ScreenElement> add(element: E): E {
        checkOpen()
        elements += element
        return element
    }

    /**
     * Passes a click or press handler through the binder.
     *
     * @param elementId the id of the element the handler belongs to
     * @param handler the handler, or `null` for none
     * @return the bound handler, or `null` if [handler] is `null`
     */
    internal fun bindButton(elementId: String, handler: ButtonHandler?): ButtonHandler? = handler?.let { binder.button(elementId, it) }

    /**
     * Passes a change handler through the binder.
     *
     * @param elementId the id of the input the handler belongs to
     * @param handler the handler, or `null` for none
     * @return the bound handler, or `null` if [handler] is `null`
     */
    internal fun bindChange(elementId: String, handler: ChangeHandler?): ChangeHandler? = handler?.let { binder.change(elementId, it) }

    /**
     * Passes a search handler through the binder.
     *
     * @param elementId the id of the combobox the handler belongs to
     * @param handler the handler, or `null` for none
     * @return the bound handler, or `null` if [handler] is `null`
     */
    internal fun bindSearch(elementId: String, handler: SearchHandler?): SearchHandler? = handler?.let { binder.search(elementId, it) }

    /**
     * Returns the only collected element.
     *
     * @return the element
     * @throws IllegalStateException if not exactly one element was collected
     */
    internal fun single(): ScreenElement {
        check(elements.size == 1) { "Expected exactly one root element, found ${elements.size}" }
        return elements.single()
    }

    /**
     * Holds the prefix of generated ids.
     */
    private companion object {
        /**
         * The prefix of every generated id, which explicit ids may not use.
         */
        const val GENERATED_PREFIX: String = "_"
    }
}

/**
 * Builds a generic screen with the component DSL.
 *
 * ```
 * val definition = Screen(Component.text("Anmeldung")) {
 *     Column(gap = 4) {
 *         val name = Input(id = "name", required = true)
 *         Button("Senden") { click -> greet(click.values[name]) }
 *     }
 * }
 * ```
 *
 * @param title the title shown above the screen
 * @param theme the name of the theme the screen is drawn with, or `null` for the parent's theme
 * @param variant the light or dark variant of the theme, or `null` for the parent's variant
 * @param closable whether the player can close the screen with Escape
 * @param onClose the handler run when the screen is closed for any reason, or `null` for none
 * @param binder the binder every handler of the tree passes through
 * @param content the builder that adds exactly one root element
 * @return the screen definition
 * @throws IllegalStateException if [content] does not add exactly one root element
 * @throws IllegalArgumentException if an explicit id starts with `_` or two elements share an id
 */
fun Screen(
    title: Component,
    theme: String? = null,
    variant: ScreenVariant? = null,
    closable: Boolean = true,
    onClose: CloseHandler? = null,
    binder: HandlerBinder = HandlerBinder.IDENTITY,
    content: ComponentScope.() -> Unit,
): ScreenDefinition = ScreenDefinition(title, renderRoot(binder, content), closable, onClose, theme, variant)

/**
 * Renders exactly one root element with the component DSL, for a whole screen or for an element
 * that replaces part of an open screen.
 *
 * @param binder the binder every handler of the tree passes through
 * @param content the builder that adds exactly one element
 * @return the element, whose generated ids start at `_0`
 * @throws IllegalStateException if [content] does not add exactly one element
 * @throws IllegalArgumentException if an explicit id starts with `_`
 */
fun renderRoot(binder: HandlerBinder = HandlerBinder.IDENTITY, content: ComponentScope.() -> Unit): ScreenElement =
    ComponentScope(emptyList(), binder).build(content).single()
