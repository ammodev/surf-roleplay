package dev.slne.surf.roleplay.api.client.paper.screen.gui

import dev.slne.surf.roleplay.api.client.common.screen.ButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.ColumnElement
import dev.slne.surf.roleplay.api.client.common.screen.ComboboxElement
import dev.slne.surf.roleplay.api.client.common.screen.ContainerElement
import dev.slne.surf.roleplay.api.client.common.screen.OpenScreen
import dev.slne.surf.roleplay.api.client.common.screen.RowElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenChange
import dev.slne.surf.roleplay.api.client.common.screen.ScreenClick
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.ScreenElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenInputChange
import dev.slne.surf.roleplay.api.client.common.screen.ScreenPatchBuilder
import dev.slne.surf.roleplay.api.client.common.screen.ScreenPresentation
import dev.slne.surf.roleplay.api.client.common.screen.ScreenSearch
import dev.slne.surf.roleplay.api.client.common.screen.ScreenValues
import dev.slne.surf.roleplay.api.client.common.screen.SheetSide
import dev.slne.surf.roleplay.api.client.common.screen.TextInputElement
import net.kyori.adventure.text.Component
import java.util.UUID
import kotlin.test.assertIs
import kotlin.test.assertNotNull

/**
 * An open screen of a [FakeOpener] that keeps a copy of the element tree and applies patches to
 * it the way the server's tree does: a change whose target is missing, that would remove the
 * root, or that would give two elements the same id is refused and recorded.
 *
 * @property definition the definition the screen was opened with
 * @property owner the fake service whose stack holds the screen
 * @property parent the screen it was opened on top of, or `null`
 * @property presentation how it was opened relative to the screens below it
 * @property sheetSide the window edge it was opened at as a sheet
 */
class FakeOpenScreen(
    val definition: ScreenDefinition,
    private val owner: FakeOpener,
    val parent: OpenScreen?,
    val presentation: ScreenPresentation,
    val sheetSide: SheetSide,
) : OpenScreen {
    /**
     * The changes of every patch, in order.
     */
    val patches = mutableListOf<List<ScreenChange>>()

    /**
     * The changes that were refused, in order.
     */
    val refused = mutableListOf<ScreenChange>()

    /**
     * The number of times the close handler ran.
     */
    var closeHandlerRuns: Int = 0
        private set

    /**
     * The current tree.
     */
    var tree: ScreenElement = definition.root
        private set

    /**
     * The changes of the last patch, or an empty list if no patch was applied.
     */
    val applied: List<ScreenChange> get() = patches.lastOrNull().orEmpty()

    /**
     * A number unique among the screens of the owner.
     */
    override val sessionId: Int = owner.opened.size + 1

    /**
     * A random viewer id.
     */
    override val viewer: UUID = UUID.randomUUID()

    /**
     * Whether the screen is on the owner's stack.
     */
    override val isOpen: Boolean get() = owner.stack.any { it === this }

    /**
     * Records the changes of the patch and applies them to the tree.
     *
     * @param changes the builder of the changes
     */
    override fun patch(changes: ScreenPatchBuilder.() -> Unit) {
        val recorded = ScreenPatchBuilder().apply(changes).changes
        patches += recorded
        recorded.forEach { change -> applyChange(change)?.let { tree = it } ?: run { refused += change } }
    }

    /**
     * Does nothing.
     *
     * @param errors unused
     */
    override fun showErrors(errors: Map<String, Component>) = Unit

    /**
     * Closes the screen with the screens above it.
     */
    override fun close() = owner.close(this)

    /**
     * Runs the close handler of the definition, as the service does after removing the screen.
     */
    fun closed() {
        closeHandlerRuns++
        definition.onClose?.onClose(this)
    }

    /**
     * Returns the element with an id in the current tree.
     *
     * @param id the id
     * @return the element, or `null`
     */
    fun find(id: String): ScreenElement? = all(tree).firstOrNull { it.id == id }

    /**
     * Returns the id of the parent of an element in the current tree.
     *
     * @param id the id of the element
     * @return the parent id, or `null` if the element is missing or the root
     */
    fun parentOf(id: String): String? = all(tree).filterIsInstance<ContainerElement>().firstOrNull { parent -> parent.children.any { it.id == id } }?.id

    /**
     * Returns the tree after a change, or `null` if the change is refused.
     *
     * @param change the change
     * @return the new tree, or `null`
     */
    private fun applyChange(change: ScreenChange): ScreenElement? {
        val ids = all(tree).map { it.id }
        return when (change) {
            is ScreenChange.Replace -> {
                val target = find(change.targetId) ?: return null
                val remaining = ids - all(target).map { it.id }.toSet()
                if (all(change.element).any { it.id in remaining }) return null
                rebuild(tree, change.targetId) { change.element }
            }
            is ScreenChange.Insert -> {
                if (find(change.parentId) !is ContainerElement || all(change.element).any { it.id in ids }) return null
                rebuild(tree, change.parentId) { parent ->
                    val children = (parent as ContainerElement).children.toMutableList()
                    children.add(change.index.coerceIn(0, children.size), change.element)
                    withChildren(parent, children)
                }
            }
            is ScreenChange.Remove -> {
                if (tree.id == change.targetId || find(change.targetId) == null) return null
                removeFrom(tree, change.targetId)
            }
            is ScreenChange.SetValue -> {
                val input = find(change.targetId) as? TextInputElement ?: return null
                rebuild(tree, input.id) { input.copy(value = change.value) }
            }
            else -> null
        }
    }

    /**
     * Returns an element and all its descendants.
     *
     * @param element the element
     * @return the elements
     */
    private fun all(element: ScreenElement): List<ScreenElement> =
        listOf(element) + ((element as? ContainerElement)?.children?.flatMap { all(it) } ?: emptyList())

    /**
     * Rebuilds a tree with one element transformed.
     *
     * @param element the root of the tree
     * @param id the id of the element to transform
     * @param transform the transformation
     * @return the new tree
     */
    private fun rebuild(element: ScreenElement, id: String, transform: (ScreenElement) -> ScreenElement): ScreenElement = when {
        element.id == id -> transform(element)
        element is ContainerElement -> withChildren(element, element.children.map { rebuild(it, id, transform) })
        else -> element
    }

    /**
     * Rebuilds a tree without one element.
     *
     * @param element the root of the tree
     * @param id the id of the element to remove
     * @return the new tree
     */
    private fun removeFrom(element: ScreenElement, id: String): ScreenElement =
        if (element is ContainerElement) withChildren(element, element.children.filter { it.id != id }.map { removeFrom(it, id) }) else element

    /**
     * Returns a column or row with other children.
     *
     * @param container the container
     * @param children the new children
     * @return the copy
     */
    private fun withChildren(container: ContainerElement, children: List<ScreenElement>): ScreenElement = when (container) {
        is ColumnElement -> container.copy(children = children)
        is RowElement -> container.copy(children = children)
        else -> error("Unsupported container ${container.javaClass.simpleName}")
    }
}

/**
 * A screen service for tests that keeps a stack of [FakeOpenScreen]s the way the server does:
 * opening without a parent closes every screen, opening with a parent closes the screens above
 * it, and closing a screen closes the screens above it. Close handlers run after the screens were
 * removed, from top to bottom, and after the new screen of an open was pushed.
 */
class FakeOpener {
    /**
     * Every opened screen, in order.
     */
    val opened = mutableListOf<FakeOpenScreen>()

    /**
     * The open screens, from bottom to top.
     */
    val stack = mutableListOf<FakeOpenScreen>()

    /**
     * The viewer every page is opened for.
     */
    val viewer = Any()

    /**
     * Whether the next open fails with an [IllegalStateException] before changing the stack.
     */
    var failNextOpen: Boolean = false

    /**
     * The last opened screen.
     */
    val lastScreen: FakeOpenScreen get() = opened.last()

    /**
     * The opener pages are opened with.
     */
    internal val opener = ScreenOpener { definition, parent, presentation, sheetSide -> openScreen(definition, parent, presentation, sheetSide) }

    /**
     * Opens a page as the root of a GUI.
     *
     * @param page the page
     * @return [page]
     */
    fun <P : GuiPage> open(page: P): P = page.apply { openFor(viewer, opener) }

    /**
     * Opens a screen on the stack.
     *
     * @param definition the definition
     * @param parent the screen to open on top of, or `null` to replace every screen
     * @param presentation how the screen is shown
     * @param sheetSide the edge of a sheet
     * @return the screen
     * @throws IllegalStateException if [failNextOpen] was set
     */
    fun openScreen(definition: ScreenDefinition, parent: OpenScreen?, presentation: ScreenPresentation, sheetSide: SheetSide): FakeOpenScreen {
        if (failNextOpen) {
            failNextOpen = false
            throw IllegalStateException("open refused")
        }
        val index = if (parent == null) -1 else stack.indexOfFirst { it === parent }
        require(parent == null || index >= 0) { "The parent screen is not open" }
        val removed = removeAbove(index)
        val screen = FakeOpenScreen(definition, this, parent, presentation, sheetSide)
        opened += screen
        stack += screen
        removed.forEach { it.closed() }
        return screen
    }

    /**
     * Closes a screen with the screens above it.
     *
     * @param screen the screen
     */
    fun close(screen: FakeOpenScreen) {
        val index = stack.indexOfFirst { it === screen }
        if (index < 0) return
        removeAbove(index - 1).forEach { it.closed() }
    }

    /**
     * Removes the screens above a position.
     *
     * @param index the position of the last screen to keep, or `-1` for none
     * @return the removed screens, from top to bottom
     */
    private fun removeAbove(index: Int): List<FakeOpenScreen> {
        val removed = mutableListOf<FakeOpenScreen>()
        while (stack.size > index + 1) removed += stack.removeLast()
        return removed
    }

    /**
     * Returns the element with an id on a screen.
     *
     * @param id the id
     * @param screen the screen, the last opened one by default
     * @return the element
     */
    inline fun <reified E : ScreenElement> element(id: String, screen: FakeOpenScreen = lastScreen): E =
        assertIs<E>(assertNotNull(screen.find(id), "No element $id"))

    /**
     * Clicks a button on a screen.
     *
     * @param id the button id
     * @param screen the screen, the last opened one by default
     */
    fun click(id: String, screen: FakeOpenScreen = lastScreen) = click(element<ButtonElement>(id, screen), screen)

    /**
     * Clicks a button, which may be a copy from an earlier tree.
     *
     * @param button the button
     * @param screen the screen the click is reported for, the last opened one by default
     */
    fun click(button: ButtonElement, screen: FakeOpenScreen = lastScreen) =
        button.onClick!!.onClick(ScreenClick(screen, button.id, ScreenValues(emptyMap())))

    /**
     * Changes the value of a text input on the last screen.
     *
     * @param id the input id
     * @param value the new value
     */
    fun change(id: String, value: String) =
        element<TextInputElement>(id).onChange!!.onChange(ScreenInputChange(lastScreen, id, value, ScreenValues(mapOf(id to value))))

    /**
     * Types a query into a combobox on the last screen.
     *
     * @param id the combobox id
     * @param query the query
     */
    fun search(id: String, query: String) = element<ComboboxElement>(id).onSearch!!.onSearch(ScreenSearch(lastScreen, id, query))
}
