package dev.slne.surf.roleplay.api.client.common.screen.diff

import dev.slne.surf.roleplay.api.client.common.screen.ContainerElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenChange
import dev.slne.surf.roleplay.api.client.common.screen.ScreenElement
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap

/**
 * Compares two element trees of a screen and computes the changes that turn the open screen
 * showing the old tree into the new tree.
 *
 * Elements are matched by id. An element equal to its counterpart produces no change. An element
 * whose counterpart at the same place has another id or class, or differs in any way that is not
 * covered below, is replaced. A container whose fields other than its children are equal to its
 * counterpart's is patched in place if the child ids both trees share keep their relative order:
 * vanished children are removed, shared children are compared recursively, and new children are
 * inserted at their index in ascending order. A container whose shared children changed their
 * relative order is replaced.
 *
 * Elements are compared with `equals`, so handler fields compare equal only if their handlers do.
 */
object ScreenDiff {
    /**
     * The name of the field that holds the children of a container.
     */
    private const val CHILDREN_FIELD: String = "children"

    /**
     * The fields of each container class that are compared for the "same shell" check.
     */
    private val shellFields = ConcurrentHashMap<Class<*>, List<Field>>()

    /**
     * Computes the changes that turn [old] into [new].
     *
     * @param old the tree the open screen shows
     * @param new the tree the screen should show
     * @return the changes in the order they must be applied, empty if the trees are equal
     */
    fun diff(old: ScreenElement, new: ScreenElement): List<ScreenChange> {
        val changes = mutableListOf<ScreenChange>()
        diffInto(old, new, changes)
        return changes
    }

    /**
     * Adds the changes that turn [old] into [new] to [changes].
     *
     * @param old the element the open screen shows
     * @param new the element that takes its place
     * @param changes the list the changes are added to
     */
    private fun diffInto(old: ScreenElement, new: ScreenElement, changes: MutableList<ScreenChange>) {
        if (old == new) return
        if (old.id == new.id && old.javaClass == new.javaClass &&
            old is ContainerElement && new is ContainerElement &&
            sameShell(old, new) && diffChildren(old, new, changes)
        ) {
            return
        }
        changes += ScreenChange.Replace(old.id, new)
    }

    /**
     * Adds the changes that turn the children of [old] into those of [new] to [changes], if the
     * child ids both share keep their relative order.
     *
     * @param old the container the open screen shows
     * @param new the container with the same id, class and fields other than its children
     * @param changes the list the changes are added to
     * @return `true` if the changes were added, `false` if nothing was added and the container
     *         must be replaced
     */
    private fun diffChildren(old: ContainerElement, new: ContainerElement, changes: MutableList<ScreenChange>): Boolean {
        val oldById = old.children.associateBy { it.id }
        val newById = new.children.associateBy { it.id }
        if (oldById.size != old.children.size || newById.size != new.children.size) return false
        val sharedInOld = old.children.filter { it.id in newById }.map { it.id }
        val sharedInNew = new.children.filter { it.id in oldById }.map { it.id }
        if (sharedInOld != sharedInNew) return false

        old.children.filter { it.id !in newById }.forEach { changes += ScreenChange.Remove(it.id) }
        sharedInNew.forEach { id -> diffInto(oldById.getValue(id), newById.getValue(id), changes) }
        new.children.forEachIndexed { index, child ->
            if (child.id !in oldById) changes += ScreenChange.Insert(new.id, index, child)
        }
        return true
    }

    /**
     * Returns whether two containers of the same class have equal values in every declared
     * non-static field except their children.
     *
     * @param old the first container
     * @param new the second container, of the same class as [old]
     * @return whether the containers differ at most in their children
     */
    private fun sameShell(old: ContainerElement, new: ContainerElement): Boolean =
        shellFieldsOf(old.javaClass).all { field -> field.get(old) == field.get(new) }

    /**
     * Returns the declared non-static fields of a container class other than its children, made
     * accessible.
     *
     * @param type the container class
     * @return the fields
     */
    private fun shellFieldsOf(type: Class<*>): List<Field> = shellFields.computeIfAbsent(type) {
        type.declaredFields
            .filter { !Modifier.isStatic(it.modifiers) && it.name != CHILDREN_FIELD }
            .onEach { it.isAccessible = true }
    }
}
