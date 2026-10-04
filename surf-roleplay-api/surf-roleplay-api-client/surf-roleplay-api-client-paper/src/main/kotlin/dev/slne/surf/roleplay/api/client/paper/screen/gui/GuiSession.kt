package dev.slne.surf.roleplay.api.client.paper.screen.gui

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.roleplay.api.client.common.screen.CloseHandler
import dev.slne.surf.roleplay.api.client.common.screen.OpenScreen
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.diff.ScreenDiff
import dev.slne.surf.roleplay.api.client.common.screen.dsl.renderRoot

/**
 * The logger of failed page handlers.
 */
private val log = logger()

/**
 * The runtime of one opened [GuiPage]: it renders the page, keeps the tree the open screen shows,
 * runs the handlers of the newest render and sends the difference after a state change.
 *
 * Every member must be used on the viewer's region thread.
 *
 * @property page the page
 * @property viewer the viewer the page is open for, a player outside of tests
 * @property opener opens a definition as a screen for the viewer
 */
internal class GuiSession(
    private val page: GuiPage,
    val viewer: Any,
    private val opener: (ScreenDefinition) -> OpenScreen,
) : HandlerDispatcher {
    /**
     * The handlers of the newest render.
     */
    private val handlers = HandlerRegistry(this)

    /**
     * The open screen, or `null` before [open].
     */
    private var screen: OpenScreen? = null

    /**
     * The definition the open screen shows, or `null` before [open].
     */
    private var shown: ScreenDefinition? = null

    /**
     * Whether a state of the page changed since the last render.
     */
    private var dirty: Boolean = false

    /**
     * Whether the page's screen is open.
     */
    val isOpen: Boolean get() = screen?.isOpen == true

    /**
     * Renders the page and opens it as a screen.
     *
     * @throws IllegalArgumentException if an explicit element id starts with `_` or two elements
     *         share an id
     */
    fun open() {
        val definition = renderDefinition()
        shown = definition
        screen = opener(definition)
    }

    /**
     * Marks the page as changed, so that the next [flush] re-renders it.
     */
    fun markDirty() {
        dirty = true
    }

    /**
     * Runs a block and re-renders the page afterwards if it changed, even if the block throws.
     *
     * @param block the block
     */
    fun update(block: () -> Unit) {
        try {
            block()
        } finally {
            flush()
        }
    }

    /**
     * Runs the newest handler of an element and re-renders the page afterwards if it changed. An
     * exception of the handler is logged and does not stop the re-render.
     *
     * @param elementId the id of the element
     * @param kind the kind of handler
     * @param invoke the function that runs the handler
     */
    override fun dispatch(elementId: String, kind: HandlerKind, invoke: (Any) -> Unit) {
        val handler = handlers[elementId, kind]
        try {
            if (handler != null) invoke(handler)
        } catch (exception: Exception) {
            log.atWarning().withCause(exception).log("The %s handler of element %s on page %s failed", kind, elementId, page.javaClass.name)
        }
        flush()
    }

    /**
     * Re-renders the page if it changed and the screen is open, and patches the open screen with
     * the difference to the tree it shows. Sends nothing if the trees are equal.
     */
    fun flush() {
        if (!dirty) return
        val screen = screen ?: return
        if (!screen.isOpen) return
        dirty = false
        val old = shown ?: return
        val definition = renderDefinition()
        val changes = ScreenDiff.diff(old.root, definition.root)
        shown = definition
        if (changes.isNotEmpty()) screen.apply(changes)
    }

    /**
     * Renders the page into a definition and makes the handlers of the render the newest ones.
     * The definition checks that the element ids are unique.
     *
     * @return the definition
     */
    private fun renderDefinition(): ScreenDefinition {
        val collector = handlers.startRender()
        val root = renderRoot(collector) { with(page) { render() } }
        val definition = ScreenDefinition(
            page.title,
            root,
            page.closable,
            CloseHandler { page.onClosed() },
            page.theme,
            page.variant,
        )
        handlers.commit(collector)
        return definition
    }
}
