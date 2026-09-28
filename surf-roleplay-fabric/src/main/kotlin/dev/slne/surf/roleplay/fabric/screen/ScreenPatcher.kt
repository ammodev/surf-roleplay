package dev.slne.surf.roleplay.fabric.screen

import dev.slne.surf.roleplay.fabric.ui.widget.ContainerWidget
import dev.slne.surf.roleplay.fabric.ui.widget.ComboboxWidget
import dev.slne.surf.roleplay.fabric.ui.widget.ProgressWidget
import dev.slne.surf.roleplay.fabric.ui.widget.Widget
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetFactory
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetTree
import dev.slne.surf.roleplay.protocol.screen.InsertNode
import dev.slne.surf.roleplay.protocol.screen.PatchOperation
import dev.slne.surf.roleplay.protocol.screen.RemoveNode
import dev.slne.surf.roleplay.protocol.screen.ReplaceNode
import dev.slne.surf.roleplay.protocol.screen.SetEnabled
import dev.slne.surf.roleplay.protocol.screen.SetInvalid
import dev.slne.surf.roleplay.protocol.screen.SetOpen
import dev.slne.surf.roleplay.fabric.ui.widget.OverlayHostWidget
import dev.slne.surf.roleplay.protocol.screen.SetOptions
import dev.slne.surf.roleplay.protocol.screen.SetProgress
import dev.slne.surf.roleplay.protocol.screen.SetText
import dev.slne.surf.roleplay.protocol.screen.SetValue

/**
 * Applies patch operations to a widget tree.
 *
 * Widgets that an operation does not replace or remove stay the same objects, so their focus,
 * scroll position and typed text survive the patch.
 */
object ScreenPatcher {

    /**
     * The outcome of applying a patch.
     *
     * @property root the root of the tree after the patch, which differs from the old root only if
     *           the root was replaced
     * @property skipped the target ids of operations that could not be applied, in order
     */
    data class Result(val root: Widget, val skipped: List<String>)

    /**
     * Applies operations to a tree in order. An operation whose target does not exist, or that
     * would remove the root, is skipped.
     *
     * @param root the root of the tree
     * @param operations the operations
     * @return the new root and the skipped target ids
     */
    fun apply(root: Widget, operations: List<PatchOperation>): Result {
        var current = root
        val skipped = mutableListOf<String>()
        for (operation in operations) {
            val (newRoot, applied) = applyOne(current, operation)
            current = newRoot
            if (!applied) skipped += targetOf(operation)
        }
        return Result(current, skipped)
    }

    /**
     * Applies one operation.
     *
     * @param root the root of the tree
     * @param operation the operation
     * @return the root after the operation and whether the operation was applied
     */
    private fun applyOne(root: Widget, operation: PatchOperation): Pair<Widget, Boolean> {
        when (operation) {
            is ReplaceNode -> {
                val replacement = WidgetFactory.create(operation.node)
                if (root.id == operation.targetId) return replacement to true
                val parent = WidgetTree.parentOf(root, operation.targetId) ?: return root to false
                val index = parent.childList.indexOfFirst { it.id == operation.targetId }
                parent.childList[index] = replacement
            }

            is InsertNode -> {
                val parent = WidgetTree.find(root, operation.parentId) as? ContainerWidget ?: return root to false
                parent.childList.add(operation.index.coerceIn(0, parent.childList.size), WidgetFactory.create(operation.node))
            }

            is RemoveNode -> {
                val parent = WidgetTree.parentOf(root, operation.targetId) ?: return root to false
                parent.childList.removeIf { it.id == operation.targetId }
            }

            is SetText -> (WidgetTree.find(root, operation.targetId) ?: return root to false).applyText(operation.text)
            is SetValue -> (WidgetTree.find(root, operation.targetId) ?: return root to false).applyValue(operation.value)
            is SetProgress -> {
                val progress = WidgetTree.find(root, operation.targetId) as? ProgressWidget ?: return root to false
                progress.progress = operation.progress
            }

            is SetEnabled -> (WidgetTree.find(root, operation.targetId) ?: return root to false).enabled = operation.enabled
            is SetOptions -> (WidgetTree.find(root, operation.targetId) as? ComboboxWidget ?: return root to false).replaceOptions(operation.groups)
            is SetInvalid -> (WidgetTree.find(root, operation.targetId) ?: return root to false).serverInvalid = operation.invalid
            is SetOpen -> (WidgetTree.find(root, operation.targetId) as? OverlayHostWidget ?: return root to false).requestOpen(operation.open)
        }
        return root to true
    }

    /**
     * Returns the id an operation addresses.
     *
     * @param operation the operation
     * @return the target or parent id
     */
    private fun targetOf(operation: PatchOperation): String = when (operation) {
        is ReplaceNode -> operation.targetId
        is InsertNode -> operation.parentId
        is RemoveNode -> operation.targetId
        is SetText -> operation.targetId
        is SetValue -> operation.targetId
        is SetProgress -> operation.targetId
        is SetEnabled -> operation.targetId
        is SetOptions -> operation.targetId
        is SetInvalid -> operation.targetId
        is SetOpen -> operation.targetId
    }
}
