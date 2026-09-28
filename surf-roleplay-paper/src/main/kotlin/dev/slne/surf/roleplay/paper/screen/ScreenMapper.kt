package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldContentElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldSeparatorElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldSetElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldTextElement
import dev.slne.surf.roleplay.api.client.common.screen.FormElement
import dev.slne.surf.roleplay.protocol.screen.FieldContentNode
import dev.slne.surf.roleplay.protocol.screen.FieldGroupNode
import dev.slne.surf.roleplay.protocol.screen.FieldNode
import dev.slne.surf.roleplay.protocol.screen.FieldSeparatorNode
import dev.slne.surf.roleplay.protocol.screen.FieldSetNode
import dev.slne.surf.roleplay.protocol.screen.FieldTextNode
import dev.slne.surf.roleplay.protocol.screen.FormNode
import dev.slne.surf.roleplay.protocol.screen.SetInvalid
import dev.slne.surf.roleplay.api.client.common.screen.CalendarElement
import dev.slne.surf.roleplay.protocol.screen.CalendarNode
import dev.slne.surf.roleplay.protocol.screen.CalendarValues
import dev.slne.surf.roleplay.api.client.common.screen.RadioGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.SliderElement
import dev.slne.surf.roleplay.api.client.common.screen.SwitchElement
import dev.slne.surf.roleplay.protocol.screen.RadioGroupNode
import dev.slne.surf.roleplay.protocol.screen.RadioOption
import dev.slne.surf.roleplay.protocol.screen.SliderNode
import dev.slne.surf.roleplay.protocol.screen.SwitchNode
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupAddonElement
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupTextElement
import dev.slne.surf.roleplay.api.client.common.screen.InputOtpElement
import dev.slne.surf.roleplay.api.client.common.screen.TextareaElement
import dev.slne.surf.roleplay.protocol.screen.InputGroupAddonNode
import dev.slne.surf.roleplay.protocol.screen.InputGroupNode
import dev.slne.surf.roleplay.protocol.screen.InputGroupTextNode
import dev.slne.surf.roleplay.protocol.screen.InputOtpNode
import dev.slne.surf.roleplay.protocol.screen.TextareaNode
import dev.slne.surf.roleplay.api.client.common.screen.ButtonGroupSeparatorElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonGroupTextElement
import dev.slne.surf.roleplay.api.client.common.screen.ToggleElement
import dev.slne.surf.roleplay.api.client.common.screen.ToggleGroupElement
import dev.slne.surf.roleplay.protocol.screen.ButtonGroupNode
import dev.slne.surf.roleplay.protocol.screen.ButtonGroupSeparatorNode
import dev.slne.surf.roleplay.protocol.screen.ButtonGroupTextNode
import dev.slne.surf.roleplay.protocol.screen.ToggleGroupItem
import dev.slne.surf.roleplay.protocol.screen.ToggleNode
import dev.slne.surf.roleplay.protocol.screen.ToggleGroupNode
import dev.slne.surf.roleplay.api.client.common.screen.CheckboxElement
import dev.slne.surf.roleplay.api.client.common.screen.ColumnElement
import dev.slne.surf.roleplay.api.client.common.screen.ComboboxElement
import dev.slne.surf.roleplay.api.client.common.screen.NativeSelectElement
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoiceGroup
import dev.slne.surf.roleplay.api.client.common.screen.SelectElement
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.ElementSizeMode
import dev.slne.surf.roleplay.api.client.common.screen.IconElement
import dev.slne.surf.roleplay.api.client.common.screen.IconTint
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
import dev.slne.surf.roleplay.protocol.screen.ComboboxNode
import dev.slne.surf.roleplay.protocol.screen.NativeSelectNode
import dev.slne.surf.roleplay.protocol.screen.SelectGroup
import dev.slne.surf.roleplay.protocol.screen.SelectNode
import dev.slne.surf.roleplay.protocol.screen.SelectOption
import dev.slne.surf.roleplay.protocol.screen.SetOptions
import dev.slne.surf.roleplay.protocol.screen.IconColor
import dev.slne.surf.roleplay.protocol.screen.IconNode
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
import dev.slne.surf.roleplay.api.client.common.screen.ScreenPresentation
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.api.client.common.screen.SheetSide
import dev.slne.surf.roleplay.protocol.screen.Presentation
import dev.slne.surf.roleplay.protocol.screen.SheetEdge
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import dev.slne.surf.roleplay.api.client.common.screen.TextElement
import dev.slne.surf.roleplay.api.client.common.screen.TextListElement
import dev.slne.surf.roleplay.api.client.common.screen.SeparatorElement
import dev.slne.surf.roleplay.api.client.common.screen.KbdElement
import dev.slne.surf.roleplay.api.client.common.screen.KbdGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.BadgeElement
import dev.slne.surf.roleplay.protocol.screen.TextNode
import dev.slne.surf.roleplay.protocol.screen.TextListNode
import dev.slne.surf.roleplay.protocol.screen.SeparatorNode
import dev.slne.surf.roleplay.protocol.screen.KbdNode
import dev.slne.surf.roleplay.protocol.screen.KbdGroupNode
import dev.slne.surf.roleplay.protocol.screen.BadgeNode
import dev.slne.surf.roleplay.api.client.common.screen.SkeletonElement
import dev.slne.surf.roleplay.api.client.common.screen.SpinnerElement
import dev.slne.surf.roleplay.api.client.common.screen.AspectRatioElement
import dev.slne.surf.roleplay.protocol.screen.SkeletonNode
import dev.slne.surf.roleplay.protocol.screen.SpinnerNode
import dev.slne.surf.roleplay.protocol.screen.AspectRatioNode
import dev.slne.surf.roleplay.api.client.common.screen.AvatarElement
import dev.slne.surf.roleplay.api.client.common.screen.AvatarGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.AvatarGroupCountElement
import dev.slne.surf.roleplay.api.client.common.screen.AvatarSource
import dev.slne.surf.roleplay.protocol.screen.AvatarNode
import dev.slne.surf.roleplay.protocol.screen.AvatarGroupNode
import dev.slne.surf.roleplay.protocol.screen.AvatarGroupCountNode
import dev.slne.surf.roleplay.api.client.common.screen.AlertElement
import dev.slne.surf.roleplay.api.client.common.screen.CardElement
import dev.slne.surf.roleplay.api.client.common.screen.CardHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.CardActionElement
import dev.slne.surf.roleplay.api.client.common.screen.CardContentElement
import dev.slne.surf.roleplay.api.client.common.screen.CardFooterElement
import dev.slne.surf.roleplay.protocol.screen.AlertNode
import dev.slne.surf.roleplay.protocol.screen.CardNode
import dev.slne.surf.roleplay.protocol.screen.CardHeaderNode
import dev.slne.surf.roleplay.protocol.screen.CardActionNode
import dev.slne.surf.roleplay.protocol.screen.CardContentNode
import dev.slne.surf.roleplay.protocol.screen.CardFooterNode
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
            is LabelElement -> LabelNode(element.id, width, height, text(element.text), element.icon, element.forId)
            is ButtonElement -> ButtonNode(element.id, width, height, text(element.text), element.enabled, element.submitsInput, element.icon, enumOf(element.variant), enumOf(element.size))
            is TextInputElement -> TextInputNode(
                element.id, width, height, element.value, text(element.placeholder), element.maxLength, element.required, element.enabled, element.icon,
                element.onChange != null, enumOf(element.type),
            )

            is NumberInputElement -> NumberInputNode(
                element.id, width, height, element.value, element.min, element.max, element.required, element.enabled,
                element.onChange != null,
            )

            is CheckboxElement -> CheckboxNode(element.id, width, height, text(element.label), element.checked, element.enabled, element.onChange != null)
            is SelectElement -> SelectNode(
                element.id, width, height, groups(element.groups), element.selected, text(element.placeholder), enumOf(element.size), element.required,
                element.enabled, element.onChange != null,
            )

            is NativeSelectElement -> NativeSelectNode(
                element.id, width, height, groups(element.groups), element.selected, enumOf(element.size), element.required, element.enabled,
                element.onChange != null,
            )

            is ComboboxElement -> ComboboxNode(
                element.id, width, height, groups(element.groups), element.selected, element.multiple, text(element.placeholder), text(element.emptyText),
                element.showClear, element.required, element.enabled, element.onChange != null, element.onSearch != null,
            )

            is ImageElement -> ImageNode(element.id, width, height, element.texture.asString())
            is ProgressElement -> ProgressNode(element.id, width, height, element.progress, element.label?.let(::text))
            is IconElement -> IconNode(element.id, width, height, element.icon, element.size, tint(element.tint))
            is ButtonGroupElement -> ButtonGroupNode(element.id, width, height, element.children.map(::toNode), enumOf(element.orientation))
            is ButtonGroupTextElement -> ButtonGroupTextNode(element.id, width, height, text(element.text), element.icon)
            is ButtonGroupSeparatorElement -> ButtonGroupSeparatorNode(element.id, width, height)
            is ToggleElement -> ToggleNode(
                element.id, width, height, text(element.text), element.icon, element.pressed, enumOf(element.variant), enumOf(element.size), element.enabled,
            )

            is ToggleGroupElement -> ToggleGroupNode(
                element.id, width, height,
                element.items.map { ToggleGroupItem(it.value, text(it.text), it.icon, it.enabled) },
                element.multiple, element.selected, enumOf(element.variant), enumOf(element.size), element.spacing,
                enumOf(element.orientation), element.enabled, element.required, element.onChange != null,
            )

            is TextareaElement -> TextareaNode(
                element.id, width, height, element.value, text(element.placeholder), element.rows, element.maxLength, element.required,
                element.enabled, element.onChange != null,
            )

            is FormElement -> FormNode(element.id, width, height, element.children.map(::toNode), element.submitId)
            is FieldSetElement -> FieldSetNode(element.id, width, height, element.children.map(::toNode))
            is FieldGroupElement -> FieldGroupNode(element.id, width, height, element.children.map(::toNode))
            is FieldElement -> FieldNode(element.id, width, height, element.children.map(::toNode), enumOf(element.orientation))
            is FieldContentElement -> FieldContentNode(element.id, width, height, element.children.map(::toNode))
            is FieldTextElement -> FieldTextNode(element.id, width, height, enumOf(element.kind), text(element.text), element.forId)
            is FieldSeparatorElement -> FieldSeparatorNode(element.id, width, height, element.text?.let(::text))
            is CalendarElement -> CalendarNode(
                element.id, width, height, enumOf(element.mode), CalendarValues.format(enumOf(element.mode), element.selected), element.month?.toString(),
                element.min?.toString(), element.max?.toString(), element.disabled.sorted().map { it.toString() }, element.showOutsideDays,
                enumOf(element.captionLayout), element.required, element.enabled, element.onChange != null,
            )

            is SwitchElement -> SwitchNode(element.id, width, height, element.checked, enumOf(element.size), element.enabled, element.onChange != null)
            is RadioGroupElement -> RadioGroupNode(
                element.id, width, height, element.options.map { RadioOption(it.value, text(it.label), it.enabled) }, element.selected,
                enumOf(element.orientation), element.required, element.enabled, element.onChange != null,
            )

            is SliderElement -> SliderNode(
                element.id, width, height, element.values, element.min, element.max, element.step, enumOf(element.orientation), element.enabled,
                element.onChange != null,
            )

            is InputGroupElement -> InputGroupNode(element.id, width, height, element.children.map(::toNode))
            is InputGroupAddonElement -> InputGroupAddonNode(element.id, width, height, element.children.map(::toNode), enumOf(element.align))
            is InputGroupTextElement -> InputGroupTextNode(element.id, width, height, text(element.text), element.icon)
            is InputOtpElement -> InputOtpNode(
                element.id, width, height, element.value, element.length, element.groups, enumOf(element.pattern), element.required,
                element.enabled, element.onChange != null,
            )
            is TextElement -> TextNode(element.id, width, height, enumOf(element.kind), text(element.text), element.maxLines, align(element.align))
            is TextListElement -> TextListNode(element.id, width, height, element.items.map(::text), element.ordered)
            is SeparatorElement -> SeparatorNode(element.id, width, height, enumOf(element.orientation))
            is KbdElement -> KbdNode(element.id, width, height, text(element.text), element.icon)
            is KbdGroupElement -> KbdGroupNode(element.id, width, height, element.children.map(::toNode))
            is BadgeElement -> BadgeNode(element.id, width, height, text(element.text), element.icon, enumOf(element.variant))
            is SkeletonElement -> SkeletonNode(element.id, width, height, element.round)
            is SpinnerElement -> SpinnerNode(element.id, width, height, element.size, tint(element.tint))
            is AspectRatioElement -> AspectRatioNode(element.id, width, height, element.children.map(::toNode), element.ratio)
            is AvatarElement -> AvatarNode(
                element.id, width, height, (element.source as? AvatarSource.Player)?.playerId?.toString(),
                (element.source as? AvatarSource.Texture)?.texture?.asString(), text(element.fallback), enumOf(element.size),
                element.badge, element.badgeIcon,
            )
            is AvatarGroupElement -> AvatarGroupNode(element.id, width, height, element.children.map(::toNode))
            is AvatarGroupCountElement -> AvatarGroupCountNode(element.id, width, height, text(element.text), element.icon)
            is AlertElement -> AlertNode(element.id, width, height, element.children.map(::toNode), enumOf(element.variant), element.icon)
            is CardElement -> CardNode(element.id, width, height, element.children.map(::toNode))
            is CardHeaderElement -> CardHeaderNode(element.id, width, height, element.children.map(::toNode))
            is CardActionElement -> CardActionNode(element.id, width, height, element.children.map(::toNode))
            is CardContentElement -> CardContentNode(element.id, width, height, element.children.map(::toNode))
            is CardFooterElement -> CardFooterNode(element.id, width, height, element.children.map(::toNode))
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
        is ScreenChange.SetOptions -> SetOptions(change.targetId, groups(change.groups))
        is ScreenChange.SetInvalid -> SetInvalid(change.targetId, change.invalid)
    }

    /**
     * Maps option groups to their protocol form.
     *
     * @param groups the option groups
     * @return the protocol groups
     */
    fun groups(groups: List<SelectChoiceGroup>): List<SelectGroup> =
        groups.map { group -> SelectGroup(group.label?.let(::text), group.options.map { SelectOption(it.value, text(it.label), it.enabled) }) }

    /**
     * Maps an API enum constant to the protocol enum constant of the same name.
     *
     * @param T the protocol enum
     * @param value the API constant
     * @return the protocol constant
     */
    inline fun <reified T : Enum<T>> enumOf(value: Enum<*>): T = enumValueOf(value.name)

    /**
     * Maps an icon tint.
     *
     * @param tint the tint
     * @return the protocol icon colour
     */
    private fun tint(tint: IconTint): IconColor = when (tint) {
        IconTint.FOREGROUND -> IconColor.FOREGROUND
        IconTint.MUTED -> IconColor.MUTED
        IconTint.PRIMARY -> IconColor.PRIMARY
        IconTint.DESTRUCTIVE -> IconColor.DESTRUCTIVE
    }

    /**
     * Maps a theme variant.
     *
     * @param variant the variant
     * @return the protocol variant
     */
    fun variant(variant: ScreenVariant): ThemeVariant = when (variant) {
        ScreenVariant.DARK -> ThemeVariant.DARK
        ScreenVariant.LIGHT -> ThemeVariant.LIGHT
    }

    /**
     * Maps a presentation.
     *
     * @param presentation the presentation
     * @return the protocol presentation
     */
    fun presentation(presentation: ScreenPresentation): Presentation = when (presentation) {
        ScreenPresentation.SCREEN -> Presentation.SCREEN
        ScreenPresentation.DIALOG -> Presentation.DIALOG
        ScreenPresentation.SHEET -> Presentation.SHEET
    }

    /**
     * Maps a sheet side.
     *
     * @param side the side
     * @return the protocol sheet edge
     */
    fun sheetEdge(side: SheetSide): SheetEdge = when (side) {
        SheetSide.RIGHT -> SheetEdge.RIGHT
        SheetSide.LEFT -> SheetEdge.LEFT
        SheetSide.TOP -> SheetEdge.TOP
        SheetSide.BOTTOM -> SheetEdge.BOTTOM
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
