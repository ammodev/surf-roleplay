package dev.slne.surf.roleplay.api.client.paper.screen.gui

import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ChangeHandler
import dev.slne.surf.roleplay.api.client.common.screen.ScreenClick
import dev.slne.surf.roleplay.api.client.common.screen.ScreenInputChange
import dev.slne.surf.roleplay.api.client.common.screen.ScreenSearch
import dev.slne.surf.roleplay.api.client.common.screen.SearchHandler
import dev.slne.surf.roleplay.api.client.common.screen.dsl.HandlerBinder

/**
 * The kinds of handler an element can hold.
 */
internal enum class HandlerKind {
    /**
     * A click or press handler of a button or toggle.
     */
    BUTTON,

    /**
     * A change handler of an input.
     */
    CHANGE,

    /**
     * A search handler of a combobox.
     */
    SEARCH,
}

/**
 * Runs the handler registered for an element and kind, as the [GuiSession] of a page does.
 */
internal fun interface HandlerDispatcher {
    /**
     * Looks up the newest handler of an element and kind and runs it.
     *
     * @param elementId the id of the element
     * @param kind the kind of handler
     * @param invoke the function that runs the handler, which is of the type [kind] names
     */
    fun dispatch(elementId: String, kind: HandlerKind, invoke: (Any) -> Unit)
}

/**
 * A handler stored in an element in place of the handler given to the component. It runs the
 * handler of the newest render for its element and kind.
 *
 * Two bound handlers are equal if they belong to the same element id and kind, so that the
 * elements of two renders compare equal however their handler closures differ.
 *
 * @property elementId the id of the element the handler belongs to
 * @property kind the kind of handler
 * @property dispatcher the dispatcher that runs the newest handler
 */
internal sealed class BoundHandler(val elementId: String, val kind: HandlerKind, protected val dispatcher: HandlerDispatcher) {
    /**
     * Returns whether [other] is a bound handler of the same element id and kind.
     *
     * @param other the object to compare with
     * @return whether both are equal
     */
    override fun equals(other: Any?): Boolean = other is BoundHandler && other.elementId == elementId && other.kind == kind

    /**
     * Returns a hash code of the element id and kind.
     *
     * @return the hash code
     */
    override fun hashCode(): Int = 31 * elementId.hashCode() + kind.hashCode()

    /**
     * Returns a readable description of the handler.
     *
     * @return the description
     */
    override fun toString(): String = "BoundHandler($elementId, $kind)"
}

/**
 * A bound click or press handler.
 *
 * @param elementId the id of the element
 * @param dispatcher the dispatcher that runs the newest handler
 */
internal class BoundButtonHandler(elementId: String, dispatcher: HandlerDispatcher) :
    BoundHandler(elementId, HandlerKind.BUTTON, dispatcher), ButtonHandler {
    /**
     * Runs the newest click handler of the element.
     *
     * @param click the click
     */
    override fun onClick(click: ScreenClick) = dispatcher.dispatch(elementId, kind) { (it as ButtonHandler).onClick(click) }
}

/**
 * A bound change handler.
 *
 * @param elementId the id of the input
 * @param dispatcher the dispatcher that runs the newest handler
 */
internal class BoundChangeHandler(elementId: String, dispatcher: HandlerDispatcher) :
    BoundHandler(elementId, HandlerKind.CHANGE, dispatcher), ChangeHandler {
    /**
     * Runs the newest change handler of the input.
     *
     * @param change the change
     */
    override fun onChange(change: ScreenInputChange) = dispatcher.dispatch(elementId, kind) { (it as ChangeHandler).onChange(change) }
}

/**
 * A bound search handler.
 *
 * @param elementId the id of the combobox
 * @param dispatcher the dispatcher that runs the newest handler
 */
internal class BoundSearchHandler(elementId: String, dispatcher: HandlerDispatcher) :
    BoundHandler(elementId, HandlerKind.SEARCH, dispatcher), SearchHandler {
    /**
     * Runs the newest search handler of the combobox.
     *
     * @param search the changed query
     */
    override fun onSearch(search: ScreenSearch) = dispatcher.dispatch(elementId, kind) { (it as SearchHandler).onSearch(search) }
}

/**
 * The handlers of the newest render of one page, keyed by element id and kind.
 *
 * A render collects its handlers through the binder of [startRender]; they replace the handlers
 * of the previous render only once [commit] is called, so a failed render keeps the handlers the
 * open screen was built with.
 *
 * @property dispatcher the dispatcher every bound handler runs through
 */
internal class HandlerRegistry(private val dispatcher: HandlerDispatcher) {
    /**
     * The handlers of the committed render.
     */
    private var current: Map<Pair<String, HandlerKind>, Any> = emptyMap()

    /**
     * Returns the handler of the committed render for an element and kind.
     *
     * @param elementId the id of the element
     * @param kind the kind of handler
     * @return the handler, or `null` if the committed render has none
     */
    operator fun get(elementId: String, kind: HandlerKind): Any? = current[elementId to kind]

    /**
     * Starts collecting the handlers of a render.
     *
     * @return the render's collector, whose binder is passed to the component DSL
     */
    fun startRender(): Collector = Collector()

    /**
     * Makes the handlers a collector collected the handlers of the newest render.
     *
     * @param collector the collector of the finished render
     */
    fun commit(collector: Collector) {
        current = collector.handlers.toMap()
    }

    /**
     * Collects the handlers of one render and returns bound handlers in their place.
     */
    inner class Collector : HandlerBinder {
        /**
         * The collected handlers.
         */
        internal val handlers: MutableMap<Pair<String, HandlerKind>, Any> = HashMap()

        /**
         * Collects a click or press handler.
         *
         * @param elementId the id of the element
         * @param handler the handler given to the component
         * @return the bound handler
         */
        override fun button(elementId: String, handler: ButtonHandler): ButtonHandler {
            handlers[elementId to HandlerKind.BUTTON] = handler
            return BoundButtonHandler(elementId, dispatcher)
        }

        /**
         * Collects a change handler.
         *
         * @param elementId the id of the input
         * @param handler the handler given to the component
         * @return the bound handler
         */
        override fun change(elementId: String, handler: ChangeHandler): ChangeHandler {
            handlers[elementId to HandlerKind.CHANGE] = handler
            return BoundChangeHandler(elementId, dispatcher)
        }

        /**
         * Collects a search handler.
         *
         * @param elementId the id of the combobox
         * @param handler the handler given to the component
         * @return the bound handler
         */
        override fun search(elementId: String, handler: SearchHandler): SearchHandler {
            handlers[elementId to HandlerKind.SEARCH] = handler
            return BoundSearchHandler(elementId, dispatcher)
        }
    }
}
