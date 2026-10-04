package dev.slne.surf.roleplay.api.client.common.screen.diff

import dev.slne.surf.roleplay.api.client.common.screen.AlertDialogElement
import dev.slne.surf.roleplay.api.client.common.screen.CheckboxElement
import dev.slne.surf.roleplay.api.client.common.screen.ContainerElement
import dev.slne.surf.roleplay.api.client.common.screen.ContextMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.DialogElement
import dev.slne.surf.roleplay.api.client.common.screen.DrawerElement
import dev.slne.surf.roleplay.api.client.common.screen.DropdownMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.HoverCardElement
import dev.slne.surf.roleplay.api.client.common.screen.InputOtpElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuSubElement
import dev.slne.surf.roleplay.api.client.common.screen.MenubarMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.NativeSelectElement
import dev.slne.surf.roleplay.api.client.common.screen.NavigationMenuItemElement
import dev.slne.surf.roleplay.api.client.common.screen.NumberInputElement
import dev.slne.surf.roleplay.api.client.common.screen.PopoverElement
import dev.slne.surf.roleplay.api.client.common.screen.RadioGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenChange
import dev.slne.surf.roleplay.api.client.common.screen.ScreenElement
import dev.slne.surf.roleplay.api.client.common.screen.SelectElement
import dev.slne.surf.roleplay.api.client.common.screen.SheetElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarProviderElement
import dev.slne.surf.roleplay.api.client.common.screen.SwitchElement
import dev.slne.surf.roleplay.api.client.common.screen.TextInputElement
import dev.slne.surf.roleplay.api.client.common.screen.TextareaElement
import dev.slne.surf.roleplay.api.client.common.screen.ToggleElement
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap

/**
 * Compares two element trees of a screen and computes the changes that turn the open screen
 * showing the old tree into the new tree.
 *
 * Elements are matched by id:
 * - An element equal to its counterpart produces no change.
 * - An input whose only difference is its value gets a [ScreenChange.SetValue] with the value in
 *   the string form of `ScreenValues.all`. This applies to text inputs, textareas, one-time
 *   password inputs, number inputs, checkboxes, switches, toggles, selects, native selects and
 *   radio groups.
 * - A sidebar provider whose fields other than its children differ only in its expanded state
 *   gets a [ScreenChange.SetValue] of `"true"` or `"false"`, followed by the changes of its
 *   children.
 * - A popover, hover card, dropdown, context, menubar or sub-menu, dialog, alert dialog, sheet,
 *   drawer or navigation menu item whose fields other than its children differ only in its open
 *   state gets a [ScreenChange.SetOpen], followed by the changes of its children.
 * - A container whose fields other than its children are equal is patched in place if the child
 *   ids both trees share keep their relative order: vanished children are removed, shared children
 *   are compared recursively, and new children are inserted at their index in ascending order.
 * - An element that moved to another parent replaces the lowest common ancestor of its old and
 *   new parents, so that no change adds an id the screen still holds elsewhere.
 * - Anything else, including another id or class at the same place, is replaced.
 *
 * The value of an input, or the open state of an overlay, is sent only if it changed between the
 * two trees and differs from the value the player last reported for that element, if any. An
 * unchanged value sends nothing, so the screen keeps whatever the player typed, and a new value
 * that equals the reported one is not echoed back.
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
     * The name of the field that holds the open state of an overlay.
     */
    private const val OPEN_FIELD: String = "open"

    /**
     * The value field of every input class whose value can be changed alone, with the function
     * that writes the field's value in its string form.
     */
    private val valueFields: Map<Class<*>, ValueField> = mapOf(
        TextInputElement::class.java to ValueField("value") { it as String },
        TextareaElement::class.java to ValueField("value") { it as String },
        InputOtpElement::class.java to ValueField("value") { it as String },
        NumberInputElement::class.java to ValueField("value") { it?.toString() ?: "" },
        CheckboxElement::class.java to ValueField("checked") { it.toString() },
        SwitchElement::class.java to ValueField("checked") { it.toString() },
        ToggleElement::class.java to ValueField("pressed") { it.toString() },
        SelectElement::class.java to ValueField("selected") { it as String? ?: "" },
        NativeSelectElement::class.java to ValueField("selected") { it as String? ?: "" },
        RadioGroupElement::class.java to ValueField("selected") { it as String? ?: "" },
    )

    /**
     * The value field of every container class whose value can be changed alone, with the
     * function that writes the field's value in its string form.
     */
    private val containerValueFields: Map<Class<*>, ValueField> = mapOf(
        SidebarProviderElement::class.java to ValueField("open") { it.toString() },
    )

    /**
     * The overlay classes whose open state can be changed alone.
     */
    private val openableClasses: Set<Class<*>> = setOf(
        PopoverElement::class.java,
        HoverCardElement::class.java,
        DropdownMenuElement::class.java,
        MenuSubElement::class.java,
        ContextMenuElement::class.java,
        MenubarMenuElement::class.java,
        DialogElement::class.java,
        AlertDialogElement::class.java,
        SheetElement::class.java,
        DrawerElement::class.java,
        NavigationMenuItemElement::class.java,
    )

    /**
     * The declared non-static fields of each element class, made accessible.
     */
    private val fieldsByClass = ConcurrentHashMap<Class<*>, List<Field>>()

    /**
     * Computes the changes that turn [old] into [new].
     *
     * @param old the tree the open screen shows
     * @param new the tree the screen should show
     * @param reportedValues the values the player last reported for inputs and overlays of the
     *        open screen, keyed by element id, in the string form of `ScreenValues.all`; `"true"`
     *        or `"false"` for the open state of an overlay. A changed value equal to the reported
     *        one is not sent.
     * @return the changes in the order they must be applied, empty if the trees are equal
     */
    fun diff(old: ScreenElement, new: ScreenElement, reportedValues: Map<String, String> = emptyMap()): List<ScreenChange> {
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
        diffInto(old, new, Context(replaced, reportedValues), changes)
        return changes
    }

    /**
     * Adds the changes that turn [old] into [new] to [changes].
     *
     * @param old the element the open screen shows
     * @param new the element that takes its place
     * @param context the state of the comparison
     * @param changes the list the changes are added to
     */
    private fun diffInto(old: ScreenElement, new: ScreenElement, context: Context, changes: MutableList<ScreenChange>) {
        if (old == new) return
        if (old.id != new.id || old.javaClass != new.javaClass || old.id in context.replaced) {
            changes += ScreenChange.Replace(old.id, new)
            return
        }
        val reported = context.reportedValues[old.id]
        val differing = differingFields(old, new)
        if (old is ContainerElement && new is ContainerElement) {
            val openField = if (old.javaClass in openableClasses) fieldNamed(old.javaClass, OPEN_FIELD) else null
            val containerValue = containerValueFields[old.javaClass]
            val shell = differing.filter { it.name != CHILDREN_FIELD && it.name != openField?.name && it.name != containerValue?.name }
            if (shell.isEmpty()) {
                val childChanges = mutableListOf<ScreenChange>()
                if (diffChildren(old, new, context, childChanges)) {
                    if (openField != null) {
                        val open = openField.get(new) as Boolean
                        if (open != openField.get(old) && open.toString() != reported) changes += ScreenChange.SetOpen(new.id, open)
                    }
                    if (containerValue != null) {
                        val field = fieldNamed(old.javaClass, containerValue.name)
                        val value = containerValue.encode(field.get(new))
                        if (value != containerValue.encode(field.get(old)) && value != reported) changes += ScreenChange.SetValue(new.id, value)
                    }
                    changes += childChanges
                    return
                }
            }
        } else {
            val valueField = valueFields[old.javaClass]
            if (valueField != null && differing.all { it.name == valueField.name }) {
                val field = fieldNamed(old.javaClass, valueField.name)
                val value = valueField.encode(field.get(new))
                if (value != valueField.encode(field.get(old)) && value != reported) changes += ScreenChange.SetValue(new.id, value)
                return
            }
        }
        changes += ScreenChange.Replace(old.id, new)
    }

    /**
     * Adds the changes that turn the children of [old] into those of [new] to [changes], if the
     * child ids both share keep their relative order.
     *
     * @param old the container the open screen shows
     * @param new the container with the same id and class
     * @param context the state of the comparison
     * @param changes the list the changes are added to
     * @return `true` if the changes were added, `false` if the shared children changed their
     *         relative order and the container must be replaced
     */
    private fun diffChildren(old: ContainerElement, new: ContainerElement, context: Context, changes: MutableList<ScreenChange>): Boolean {
        val oldById = old.children.associateBy { it.id }
        val newById = new.children.associateBy { it.id }
        val sharedInOld = old.children.filter { it.id in newById }.map { it.id }
        val sharedInNew = new.children.filter { it.id in oldById }.map { it.id }
        if (sharedInOld != sharedInNew) return false

        old.children.filter { it.id !in newById }.forEach { changes += ScreenChange.Remove(it.id) }
        sharedInNew.forEach { id -> diffInto(oldById.getValue(id), newById.getValue(id), context, changes) }
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

    /**
     * Returns the declared field of an element class with a name.
     *
     * @param type the element class
     * @param name the field name
     * @return the field, made accessible
     */
    private fun fieldNamed(type: Class<*>, name: String): Field = fieldsOf(type).first { it.name == name }

    /**
     * The state of one comparison.
     *
     * @property replaced the ids of elements that must be replaced as a whole
     * @property reportedValues the values the player last reported, keyed by element id
     */
    private class Context(val replaced: Set<String>, val reportedValues: Map<String, String>)

    /**
     * The field that holds the value of an input.
     *
     * @property name the field name
     * @property encode writes a value of the field in its string form
     */
    private class ValueField(val name: String, val encode: (Any?) -> String)
}
