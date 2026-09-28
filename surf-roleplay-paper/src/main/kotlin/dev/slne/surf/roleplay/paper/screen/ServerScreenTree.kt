package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.ButtonGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.ColumnElement
import dev.slne.surf.roleplay.api.client.common.screen.ContainerElement
import dev.slne.surf.roleplay.api.client.common.screen.ProgressElement
import dev.slne.surf.roleplay.api.client.common.screen.RowElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenChange
import dev.slne.surf.roleplay.api.client.common.screen.ScreenElement
import dev.slne.surf.roleplay.api.client.common.screen.ScrollListElement

/**
 * The server's copy of an open generic screen's element tree.
 *
 * It applies the same changes that are sent to the mod, so that it always matches what the
 * player sees, and it is what player actions are validated against.
 *
 * @param root the initial root element
 */
class ServerScreenTree(root: ScreenElement) {

    /**
     * The root element.
     */
    var root: ScreenElement = root
        private set

    /**
     * Finds an element by id.
     *
     * @param id the id
     * @return the element, or `null` if the tree has none with that id
     */
    fun find(id: String): ScreenElement? = find(root, id)

    /**
     * Returns every element of the tree, parents before children.
     *
     * @return the elements
     */
    fun elements(): List<ScreenElement> = mutableListOf<ScreenElement>().also { collect(root, it) }

    /**
     * Applies a change. A change whose target does not exist or does not fit the target, that
     * would remove the root, or that would give two elements the same id is refused.
     *
     * @param change the change
     * @return whether the change was applied
     */
    fun apply(change: ScreenChange): Boolean {
        val updated = when (change) {
            is ScreenChange.Replace -> {
                val target = find(change.targetId) ?: return false
                if (!idsStayUnique(change.element, removed = target)) return false
                if (root.id == change.targetId) change.element else replace(root, change.targetId) { change.element }
            }

            is ScreenChange.Insert -> {
                if (find(change.parentId) !is ContainerElement || !idsStayUnique(change.element, removed = null)) return false
                replace(root, change.parentId) { parent ->
                    val children = (parent as ContainerElement).children.toMutableList()
                    children.add(change.index.coerceIn(0, children.size), change.element)
                    withChildren(parent, children)
                }
            }

            is ScreenChange.Remove -> {
                if (root.id == change.targetId || find(change.targetId) == null) return false
                remove(root, change.targetId)
            }

            is ScreenChange.SetText -> update(change.targetId) { element ->
                ElementRules.rule(element)?.withText?.invoke(element, change.text)
            } ?: return false

            is ScreenChange.SetValue -> update(change.targetId) { element -> withValue(element, change.value) } ?: return false
            is ScreenChange.SetProgress -> update(change.targetId) { element ->
                (element as? ProgressElement)?.copy(progress = change.progress)
            } ?: return false

            is ScreenChange.SetEnabled -> update(change.targetId) { element ->
                ElementRules.rule(element)?.withEnabled?.invoke(element, change.enabled)
            } ?: return false
        }
        root = updated
        return true
    }

    /**
     * Stores validated input values in the tree, so that it holds what the player submitted.
     *
     * @param values the values keyed by input id, in their string form
     */
    fun storeValues(values: Map<String, String>) {
        for ((id, value) in values) apply(ScreenChange.SetValue(id, value))
    }

    /**
     * Returns an input element with a new value, or `null` if the element is not an input or the
     * value does not fit it.
     *
     * @param element the element
     * @param value the value in its string form
     * @return the updated element, or `null`
     */
    private fun withValue(element: ScreenElement, value: String): ScreenElement? =
        ElementRules.input(element)?.withValue?.invoke(element, value)

    /**
     * Rebuilds the tree with one element updated.
     *
     * @param id the id of the element
     * @param transform returns the updated element, or `null` if the change does not fit it
     * @return the new root, or `null` if the element does not exist or the change does not fit
     */
    private fun update(id: String, transform: (ScreenElement) -> ScreenElement?): ScreenElement? {
        val target = find(id) ?: return null
        val updated = transform(target) ?: return null
        return if (root.id == id) updated else replace(root, id) { updated }
    }

    /**
     * Checks that adding an element keeps every id unique.
     *
     * @param added the element to add, with its children
     * @param removed the element it replaces, with its children, or `null`
     * @return whether the ids stay unique
     */
    private fun idsStayUnique(added: ScreenElement, removed: ScreenElement?): Boolean {
        val removedIds = removed?.let { r -> mutableListOf<ScreenElement>().also { collect(r, it) }.map { it.id }.toSet() } ?: emptySet()
        val existing = elements().map { it.id }.filterNot { it in removedIds }.toSet()
        val addedIds = mutableListOf<ScreenElement>().also { collect(added, it) }.map { it.id }
        return addedIds.size == addedIds.toSet().size && addedIds.none { it in existing }
    }

    /**
     * Holds the tree walking helpers.
     */
    private companion object {
        /**
         * Finds an element by id below and including an element.
         *
         * @param element the element to search from
         * @param id the id
         * @return the element, or `null`
         */
        fun find(element: ScreenElement, id: String): ScreenElement? {
            if (element.id == id) return element
            if (element is ContainerElement) {
                for (child in element.children) find(child, id)?.let { return it }
            }
            return null
        }

        /**
         * Adds an element and its descendants to a list, parents first.
         *
         * @param element the element
         * @param into the list
         */
        fun collect(element: ScreenElement, into: MutableList<ScreenElement>) {
            into += element
            if (element is ContainerElement) element.children.forEach { collect(it, into) }
        }

        /**
         * Rebuilds a subtree with one descendant replaced.
         *
         * @param element the subtree
         * @param id the id of the descendant
         * @param transform creates the replacement
         * @return the rebuilt subtree
         */
        fun replace(element: ScreenElement, id: String, transform: (ScreenElement) -> ScreenElement): ScreenElement {
            if (element.id == id) return transform(element)
            if (element !is ContainerElement) return element
            return withChildren(element, element.children.map { replace(it, id, transform) })
        }

        /**
         * Rebuilds a subtree with one descendant removed.
         *
         * @param element the subtree
         * @param id the id of the descendant
         * @return the rebuilt subtree
         */
        fun remove(element: ScreenElement, id: String): ScreenElement {
            if (element !is ContainerElement) return element
            return withChildren(element, element.children.filter { it.id != id }.map { remove(it, id) })
        }

        /**
         * Returns a copy of a container with other children.
         *
         * @param container the container
         * @param children the new children
         * @return the copy
         */
        fun withChildren(container: ContainerElement, children: List<ScreenElement>): ScreenElement = when (container) {
            is RowElement -> container.copy(children = children)
            is ColumnElement -> container.copy(children = children)
            is ScrollListElement -> container.copy(children = children)
            is ButtonGroupElement -> container.copy(children = children)
        }
    }
}
