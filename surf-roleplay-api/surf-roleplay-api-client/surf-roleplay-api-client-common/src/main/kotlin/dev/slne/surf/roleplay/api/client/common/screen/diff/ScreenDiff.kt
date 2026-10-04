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
 * Elements are matched by id:
 * - An element equal to its counterpart produces no change.
 * - A container whose fields other than its children are equal is patched in place if the child
 *   ids both trees share keep their relative order: vanished children are removed, shared children
 *   are compared recursively, and new children are inserted at their index in ascending order.
 * - An element that moved to another parent replaces the lowest common ancestor of its old and
 *   new parents, so that no change adds an id the screen still holds elsewhere.
 * - Anything else, including another id or class at the same place, is replaced.
 *
 * Elements are compared with `equals`, so handler fields compare equal only if their handlers do.
 * Fields are read with Java reflection over the declared non-static fields of the element class.
 */
object ScreenDiff {
    /**
     * The name of the field that holds the children of a container.
     */
    private const val CHILDREN_FIELD: String = "children"

    /**
     * The declared non-static fields of each element class, made accessible.
     */
    private val fieldsByClass = ConcurrentHashMap<Class<*>, List<Field>>()

    /**
     * Computes the changes that turn [old] into [new].
     *
     * @param old the tree the open screen shows
     * @param new the tree the screen should show
     * @return the changes in the order they must be applied, empty if the trees are equal
     */
    fun diff(old: ScreenElement, new: ScreenElement): List<ScreenChange> {
        if (old == new) return emptyList()
        val oldParents = parents(old) ?: return listOf(ScreenChange.Replace(old.id, new))
        val newParents = parents(new) ?: return listOf(ScreenChange.Replace(old.id, new))
        val replaced = HashSet<String>()
        for ((id, oldParent) in oldParents) {
            if (id !in newParents) continue
            val newParent = newParents[id]
            if (oldParent == newParent) continue
            val ancestor = commonAncestor(oldParent, oldParents, newParent, newParents) ?: return listOf(ScreenChange.Replace(old.id, new))
            replaced += ancestor
        }
        val changes = mutableListOf<ScreenChange>()
        diffInto(old, new, replaced, changes)
        return changes
    }

    /**
     * Adds the changes that turn [old] into [new] to [changes].
     *
     * @param old the element the open screen shows
     * @param new the element that takes its place
     * @param replaced the ids of elements that must be replaced as a whole
     * @param changes the list the changes are added to
     */
    private fun diffInto(old: ScreenElement, new: ScreenElement, replaced: Set<String>, changes: MutableList<ScreenChange>) {
        if (old == new) return
        if (old.id != new.id || old.javaClass != new.javaClass || old.id in replaced) {
            changes += ScreenChange.Replace(old.id, new)
            return
        }
        if (old is ContainerElement && new is ContainerElement) {
            val shell = differingFields(old, new).filter { it.name != CHILDREN_FIELD }
            if (shell.isEmpty() && diffChildren(old, new, replaced, changes)) return
        }
        changes += ScreenChange.Replace(old.id, new)
    }

    /**
     * Adds the changes that turn the children of [old] into those of [new] to [changes], if the
     * child ids both share keep their relative order.
     *
     * @param old the container the open screen shows
     * @param new the container with the same id and class
     * @param replaced the ids of elements that must be replaced as a whole
     * @param changes the list the changes are added to
     * @return `true` if the changes were added, `false` if the shared children changed their
     *         relative order and the container must be replaced
     */
    private fun diffChildren(old: ContainerElement, new: ContainerElement, replaced: Set<String>, changes: MutableList<ScreenChange>): Boolean {
        val oldById = old.children.associateBy { it.id }
        val newById = new.children.associateBy { it.id }
        val sharedInOld = old.children.filter { it.id in newById }.map { it.id }
        val sharedInNew = new.children.filter { it.id in oldById }.map { it.id }
        if (sharedInOld != sharedInNew) return false

        old.children.filter { it.id !in newById }.forEach { changes += ScreenChange.Remove(it.id) }
        sharedInNew.forEach { id -> diffInto(oldById.getValue(id), newById.getValue(id), replaced, changes) }
        new.children.forEachIndexed { index, child ->
            if (child.id !in oldById) changes += ScreenChange.Insert(new.id, index, child)
        }
        return true
    }

    /**
     * Maps the id of every element of a tree to the id of its parent.
     *
     * @param root the root of the tree
     * @return the parent id of every element, `null` for the root, or `null` if two elements of
     *         the tree share an id
     */
    private fun parents(root: ScreenElement): Map<String, String?>? {
        val parents = HashMap<String, String?>()
        val pending = ArrayDeque<Pair<ScreenElement, String?>>()
        pending += root to null
        while (pending.isNotEmpty()) {
            val (element, parent) = pending.removeLast()
            if (element.id in parents) return null
            parents[element.id] = parent
            if (element is ContainerElement) element.children.forEach { pending += it to element.id }
        }
        return parents
    }

    /**
     * Returns the lowest element id that is an ancestor of [oldParent], or the element itself, in
     * the old tree and an ancestor of [newParent], or the element itself, in the new tree.
     *
     * @param oldParent the parent id in the old tree
     * @param oldParents the parent ids of the old tree
     * @param newParent the parent id in the new tree
     * @param newParents the parent ids of the new tree
     * @return the id, or `null` if the two chains share no id
     */
    private fun commonAncestor(oldParent: String?, oldParents: Map<String, String?>, newParent: String?, newParents: Map<String, String?>): String? {
        val newChain = generateSequence(newParent) { newParents[it] }.toSet()
        return generateSequence(oldParent) { oldParents[it] }.firstOrNull { it in newChain }
    }

    /**
     * Returns the declared non-static fields in which two elements of the same class differ.
     *
     * @param old the first element
     * @param new the second element, of the same class as [old]
     * @return the differing fields
     */
    private fun differingFields(old: ScreenElement, new: ScreenElement): List<Field> =
        fieldsOf(old.javaClass).filter { field -> field.get(old) != field.get(new) }

    /**
     * Returns the declared non-static fields of an element class, made accessible.
     *
     * @param type the element class
     * @return the fields
     */
    private fun fieldsOf(type: Class<*>): List<Field> = fieldsByClass.computeIfAbsent(type) {
        type.declaredFields
            .filter { !Modifier.isStatic(it.modifiers) }
            .onEach { it.isAccessible = true }
    }
}
