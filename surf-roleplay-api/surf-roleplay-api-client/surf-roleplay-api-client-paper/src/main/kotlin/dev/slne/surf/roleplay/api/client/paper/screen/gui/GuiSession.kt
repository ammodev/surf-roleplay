package dev.slne.surf.roleplay.api.client.paper.screen.gui

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.roleplay.api.client.common.screen.CloseHandler
import dev.slne.surf.roleplay.api.client.common.screen.ContainerElement
import dev.slne.surf.roleplay.api.client.common.screen.OpenScreen
import dev.slne.surf.roleplay.api.client.common.screen.ScreenChange
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.ScreenElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenPresentation
import dev.slne.surf.roleplay.api.client.common.screen.SheetSide
import dev.slne.surf.roleplay.api.client.common.screen.diff.ScreenDiff
import dev.slne.surf.roleplay.api.client.common.screen.dsl.renderRoot

/**
 * The logger of failed page handlers and renders.
 */
private val log = logger()

/**
 * Opens a screen definition for the viewer of a GUI.
 */
internal fun interface ScreenOpener {
    /**
     * Opens a definition as a screen.
     *
     * @param definition the definition
     * @param parent the open screen to open on top of, or `null` to replace every open screen
     * @param presentation how the screen is shown relative to the screens below it
     * @param sheetSide the window edge a sheet is attached to
     * @return the open screen
     */
    fun open(definition: ScreenDefinition, parent: OpenScreen?, presentation: ScreenPresentation, sheetSide: SheetSide): OpenScreen
}

/**
 * The runtime of one opened [GuiPage]: it renders the page, keeps the tree the open screen shows,
 * runs the handlers of the newest render and sends the difference after a state change. A render
 * whose title, theme, variant or closability differs from the open screen's reopens the page in
 * place, on top of the same parent and with the same presentation.
 *
 * Every member must be used on the viewer's region thread.
 *
 * @property page the page
 * @property viewer the viewer the page is open for, a player outside of tests
 * @property opener opens a definition as a screen for the viewer
 * @property parent the session of the page this page was opened on top of, or `null` for the
 *           root page of a GUI
 * @property presentation how the page's screen is shown relative to the screens below it
 * @property sheetSide the window edge the page's screen is attached to as a sheet
 */
internal class GuiSession(
    private val page: GuiPage,
    val viewer: Any,
    val opener: ScreenOpener,
    val parent: GuiSession?,
    private val presentation: ScreenPresentation,
    private val sheetSide: SheetSide,
) : HandlerDispatcher {
    /**
     * The handlers of the newest successful render.
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
     * The values the player last reported through change events or submitted with clicks, keyed
     * by element id, until a patch sends the element a new value or the element leaves the tree.
     */
    private val reported = HashMap<String, String>()

    /**
     * Whether a state of the page changed since the tree the screen shows was rendered.
     */
    private var dirty: Boolean = false

    /**
     * Whether the page is being reopened in place, so that the close of its previous screen does
     * not close the page.
     */
    private var reopening: Boolean = false

    /**
     * Whether the page's screen is open.
     */
    val isOpen: Boolean get() = screen?.isOpen == true

    /**
     * The session of the root page of the GUI this page belongs to.
     */
    val root: GuiSession get() = parent?.root ?: this

    /**
     * Renders the page and opens it as a screen on top of the parent's screen.
     *
     * @throws IllegalArgumentException if an explicit element id starts with `_` or two elements
     *         share an id
     */
    fun open() {
        val definition = renderDefinition()
        shown = definition
        screen = opener.open(definition, parent?.screen, presentation, sheetSide)
    }

    /**
     * Closes the page's screen together with the screens above it.
     */
    fun close() {
        screen?.close()
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
     * Runs the newest handler of an element and re-renders the page afterwards if it changed,
     * followed by every page below it whose state the handler changed. An exception of the handler
     * is logged and does not stop the re-render. Does nothing once the screen is closed, or if the
     * newest render has no such handler.
     *
     * Reported values, such as the value of a changed input or the input values submitted with a
     * click, are remembered before the handler runs: a later render that changes an input's value
     * to the reported one sends no change for it.
     *
     * @param elementId the id of the element
     * @param kind the kind of handler
     * @param reportedValues the values the screen now shows for inputs, keyed by input id
     * @param invoke the function that runs the handler
     */
    override fun dispatch(elementId: String, kind: HandlerKind, reportedValues: Map<String, String>, invoke: (Any) -> Unit) {
        if (!isOpen) return
        reported.putAll(reportedValues)
        val handler = handlers[elementId, kind] ?: return
        try {
            invoke(handler)
        } catch (exception: Exception) {
            log.atWarning().withCause(exception).log("The %s handler of element %s on page %s failed", kind, elementId, page.javaClass.name)
        }
        flush()
        flushAncestors()
    }

    /**
     * Re-renders every page below this one, from the nearest to the root page, whose state
     * changed and whose screen is open.
     */
    private fun flushAncestors() {
        var ancestor = parent
        while (ancestor != null) {
            ancestor.flush()
            ancestor = ancestor.parent
        }
    }

    /**
     * Re-renders the page if it changed and the screen is open, and patches the open screen with
     * the difference to the tree it shows. Sends nothing if the trees are equal. If the title,
     * theme, variant or closability changed, the page is reopened in place instead.
     *
     * A render that throws is logged; the page stays changed and the screen keeps its tree, so a
     * later flush sends the change. If applying the patch throws, the page also stays changed and
     * the exception is rethrown.
     */
    fun flush() {
        if (!dirty) return
        val screen = screen ?: return
        val old = shown ?: return
        if (!screen.isOpen) return
        dirty = false
        val definition = try {
            renderDefinition()
        } catch (exception: Exception) {
            dirty = true
            log.atWarning().withCause(exception).log("Rendering page %s failed", page.javaClass.name)
            return
        }
        if (needsReopen(old, definition)) {
            reopen(screen, definition)
            return
        }
        val changes = ScreenDiff.diff(old.root, definition.root, reported)
        if (changes.isNotEmpty()) {
            try {
                screen.apply(changes)
            } catch (exception: Exception) {
                dirty = true
                throw exception
            }
        }
        shown = definition
        forgetSuperseded(changes, definition.root)
    }

    /**
     * Forgets the reported values that a patch superseded: those of elements the patch set a
     * value or open state on, replaced or inserted, and those of elements the new tree no longer
     * holds.
     *
     * @param changes the changes of the patch
     * @param root the root of the new tree
     */
    private fun forgetSuperseded(changes: List<ScreenChange>, root: ScreenElement) {
        if (reported.isEmpty()) return
        val present = HashSet<String>().also { collectIds(root, it) }
        val sent = HashSet<String>()
        for (change in changes) {
            when (change) {
                is ScreenChange.SetValue -> sent += change.targetId
                is ScreenChange.SetOpen -> sent += change.targetId
                is ScreenChange.Replace -> collectIds(change.element, sent)
                is ScreenChange.Insert -> collectIds(change.element, sent)
                else -> Unit
            }
        }
        reported.keys.removeIf { it !in present || it in sent }
    }

    /**
     * Adds the id of an element and of its descendants to a set.
     *
     * @param element the element
     * @param into the set
     */
    private fun collectIds(element: ScreenElement, into: MutableSet<String>) {
        into += element.id
        if (element is ContainerElement) element.children.forEach { collectIds(it, into) }
    }

    /**
     * Returns whether a new definition differs from the shown one in a property the open screen
     * cannot change: the title, theme, variant or closability.
     *
     * @param old the definition the screen shows
     * @param new the new definition
     * @return whether the page must be reopened to show [new]
     */
    private fun needsReopen(old: ScreenDefinition, new: ScreenDefinition): Boolean =
        old.title != new.title || old.theme != new.theme || old.variant != new.variant || old.closable != new.closable

    /**
     * Opens a definition in place of the page's screen, on top of the same parent and with the
     * same presentation. The service closes the previous screen and the screens above it; the
     * page itself stays open. If opening throws, the page stays changed, it is closed if its
     * previous screen was closed, and the exception is rethrown.
     *
     * @param old the page's open screen
     * @param definition the new definition
     */
    private fun reopen(old: OpenScreen, definition: ScreenDefinition) {
        reopening = true
        val opened = try {
            opener.open(definition, parent?.screen, presentation, sheetSide)
        } catch (exception: Exception) {
            dirty = true
            reopening = false
            if (!old.isOpen) page.onClosed()
            throw exception
        } finally {
            reopening = false
        }
        screen = opened
        shown = definition
        reported.clear()
    }

    /**
     * Runs the page's close handler when its screen closed, unless the screen closed because the
     * page is reopened in place, and then re-renders every page below it whose state changed and
     * whose screen is still open.
     */
    private fun screenClosed() {
        if (reopening) return
        try {
            page.onClosed()
        } finally {
            flushAncestors()
        }
    }

    /**
     * Renders the page into a definition and makes the handlers of the render the newest ones.
     * The definition checks that the element ids are unique. A render that throws leaves the
     * handlers unchanged.
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
            CloseHandler { screenClosed() },
            page.theme,
            page.variant,
        )
        handlers.commit(collector)
        return definition
    }
}
