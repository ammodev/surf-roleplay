package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.CheckboxElement
import dev.slne.surf.roleplay.api.client.common.screen.ColumnElement
import dev.slne.surf.roleplay.api.client.common.screen.DropdownElement
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.ElementSizeMode
import dev.slne.surf.roleplay.api.client.common.screen.ImageElement
import dev.slne.surf.roleplay.api.client.common.screen.LabelElement
import dev.slne.surf.roleplay.api.client.common.screen.NumberInputElement
import dev.slne.surf.roleplay.api.client.common.screen.ProgressElement
import dev.slne.surf.roleplay.api.client.common.screen.RowElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenChange
import dev.slne.surf.roleplay.api.client.common.screen.ScreenElement
import dev.slne.surf.roleplay.api.client.common.screen.ScrollListElement
import dev.slne.surf.roleplay.api.client.common.screen.Spacing
import dev.slne.surf.roleplay.api.client.common.screen.TextInputElement
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.CheckboxNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.DropdownNode
import dev.slne.surf.roleplay.protocol.screen.DropdownOption
import dev.slne.surf.roleplay.protocol.screen.ImageNode
import dev.slne.surf.roleplay.protocol.screen.InsertNode
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.NumberInputNode
import dev.slne.surf.roleplay.protocol.screen.PatchOperation
import dev.slne.surf.roleplay.protocol.screen.ProgressNode
import dev.slne.surf.roleplay.protocol.screen.RemoveNode
import dev.slne.surf.roleplay.protocol.screen.ReplaceNode
import dev.slne.surf.roleplay.protocol.screen.RowNode
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.ScrollListNode
import dev.slne.surf.roleplay.protocol.screen.SetEnabled
import dev.slne.surf.roleplay.protocol.screen.SetProgress
import dev.slne.surf.roleplay.protocol.screen.SetText
import dev.slne.surf.roleplay.protocol.screen.SetValue
import dev.slne.surf.roleplay.protocol.screen.SizeMode
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.TextInputNode
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

/**
 * Maps the public screen model to the protocol's screen nodes and patch operations.
 */
object ScreenMapper {

    /**
     * Serializes a text to the component JSON sent to the mod.
     *
     * @param component the text
     * @return the component JSON
     */
    fun text(component: Component): String = GsonComponentSerializer.gson().serialize(component)

    /**
     * Maps an element, with its children, to a protocol node.
     *
     * @param element the element
     * @return the node
     */
    fun toNode(element: ScreenElement): ScreenNode {
        val width = size(element.width)
        val height = size(element.height)
        return when (element) {
            is RowElement -> RowNode(
                element.id, width, height, element.children.map(::toNode), element.gap, insets(element.padding),
                align(element.mainAlign), align(element.crossAlign),
            )

            is ColumnElement -> ColumnNode(
                element.id, width, height, element.children.map(::toNode), element.gap, insets(element.padding),
                align(element.mainAlign), align(element.crossAlign),
            )

            is ScrollListElement -> ScrollListNode(element.id, width, height, element.children.map(::toNode), element.gap)
            is LabelElement -> LabelNode(element.id, width, height, text(element.text))
            is ButtonElement -> ButtonNode(element.id, width, height, text(element.text), element.enabled, element.submitsInput)
            is TextInputElement -> TextInputNode(
                element.id, width, height, element.value, text(element.placeholder), element.maxLength, element.required, element.enabled,
            )

            is NumberInputElement -> NumberInputNode(
                element.id, width, height, element.value, element.min, element.max, element.required, element.enabled,
            )

            is CheckboxElement -> CheckboxNode(element.id, width, height, text(element.label), element.checked, element.enabled)
            is DropdownElement -> DropdownNode(
                element.id, width, height, element.options.map { DropdownOption(it.value, text(it.label)) },
                element.selected, element.required, element.enabled,
            )

            is ImageElement -> ImageNode(element.id, width, height, element.texture.asString())
            is ProgressElement -> ProgressNode(element.id, width, height, element.progress, element.label?.let(::text))
        }
    }

    /**
     * Maps a change to a patch operation.
     *
     * @param change the change
     * @return the operation
     */
    fun toOperation(change: ScreenChange): PatchOperation = when (change) {
        is ScreenChange.Replace -> ReplaceNode(change.targetId, toNode(change.element))
        is ScreenChange.Insert -> InsertNode(change.parentId, change.index, toNode(change.element))
        is ScreenChange.Remove -> RemoveNode(change.targetId)
        is ScreenChange.SetText -> SetText(change.targetId, text(change.text))
        is ScreenChange.SetValue -> SetValue(change.targetId, change.value)
        is ScreenChange.SetProgress -> SetProgress(change.targetId, change.progress)
        is ScreenChange.SetEnabled -> SetEnabled(change.targetId, change.enabled)
    }

    /**
     * Maps an element size to a sizing.
     *
     * @param size the size
     * @return the sizing
     */
    private fun size(size: ElementSize): Sizing = when (size.mode) {
        ElementSizeMode.FIT -> Sizing.FIT
        ElementSizeMode.FIXED -> Sizing(SizeMode.FIXED, size.value)
        ElementSizeMode.GROW -> Sizing(SizeMode.GROW, size.value)
    }

    /**
     * Maps a spacing to insets.
     *
     * @param spacing the spacing
     * @return the insets
     */
    private fun insets(spacing: Spacing): Insets = Insets(spacing.top, spacing.right, spacing.bottom, spacing.left)

    /**
     * Maps an alignment.
     *
     * @param alignment the alignment
     * @return the protocol alignment
     */
    private fun align(alignment: Alignment): Align = when (alignment) {
        Alignment.START -> Align.START
        Alignment.CENTER -> Align.CENTER
        Alignment.END -> Align.END
        Alignment.STRETCH -> Align.STRETCH
    }
}
