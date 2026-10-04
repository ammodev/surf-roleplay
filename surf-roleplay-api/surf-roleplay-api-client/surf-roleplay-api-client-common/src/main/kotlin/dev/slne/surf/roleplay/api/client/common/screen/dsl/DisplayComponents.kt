package dev.slne.surf.roleplay.api.client.common.screen.dsl

import dev.slne.surf.roleplay.api.client.common.screen.AlertElement
import dev.slne.surf.roleplay.api.client.common.screen.AlertVariant
import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.AspectRatioElement
import dev.slne.surf.roleplay.api.client.common.screen.AvatarElement
import dev.slne.surf.roleplay.api.client.common.screen.AvatarGroupCountElement
import dev.slne.surf.roleplay.api.client.common.screen.AvatarGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.AvatarSize
import dev.slne.surf.roleplay.api.client.common.screen.AvatarSource
import dev.slne.surf.roleplay.api.client.common.screen.BadgeElement
import dev.slne.surf.roleplay.api.client.common.screen.BadgeVariant
import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.CardActionElement
import dev.slne.surf.roleplay.api.client.common.screen.CardContentElement
import dev.slne.surf.roleplay.api.client.common.screen.CardElement
import dev.slne.surf.roleplay.api.client.common.screen.CardFooterElement
import dev.slne.surf.roleplay.api.client.common.screen.CardHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.EmptyContentElement
import dev.slne.surf.roleplay.api.client.common.screen.EmptyElement
import dev.slne.surf.roleplay.api.client.common.screen.EmptyHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.EmptyMediaElement
import dev.slne.surf.roleplay.api.client.common.screen.EmptyMediaVariant
import dev.slne.surf.roleplay.api.client.common.screen.IconElement
import dev.slne.surf.roleplay.api.client.common.screen.IconTint
import dev.slne.surf.roleplay.api.client.common.screen.ImageElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemActionsElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemContentElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemFooterElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemMediaElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemMediaVariant
import dev.slne.surf.roleplay.api.client.common.screen.ItemSize
import dev.slne.surf.roleplay.api.client.common.screen.ItemVariant
import dev.slne.surf.roleplay.api.client.common.screen.KbdElement
import dev.slne.surf.roleplay.api.client.common.screen.KbdGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.Orientation
import dev.slne.surf.roleplay.api.client.common.screen.ProgressElement
import dev.slne.surf.roleplay.api.client.common.screen.SeparatorElement
import dev.slne.surf.roleplay.api.client.common.screen.SkeletonElement
import dev.slne.surf.roleplay.api.client.common.screen.SpinnerElement
import dev.slne.surf.roleplay.api.client.common.screen.TextElement
import dev.slne.surf.roleplay.api.client.common.screen.TextKind
import dev.slne.surf.roleplay.api.client.common.screen.TextListElement
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

/**
 * Adds a text of a given style.
 *
 * @param text the text
 * @param kind the style of the text
 * @param maxLines the largest number of lines shown, or `0` for the style's default
 * @param align how the lines are placed within the text's width
 * @param width how wide the text is laid out
 * @param id the id of the text, or `null` for a generated one
 * @return the text
 * @throws IllegalArgumentException if [id] starts with `_`
 */
internal fun ComponentScope.styledText(
    text: Component,
    kind: TextKind,
    maxLines: Int = 0,
    align: Alignment = Alignment.START,
    width: ElementSize = ElementSize.FIT,
    id: String? = null,
): TextElement = add(TextElement(nextId(id), text, kind, maxLines, align, width))

/**
 * Adds the largest heading.
 *
 * @param text the heading
 * @param maxLines the largest number of lines shown, or `0` for the style's default
 * @param align how the lines are placed within the text's width
 * @param width how wide the heading is laid out
 * @param id the id of the heading, or `null` for a generated one
 * @return the heading
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.H1(text: Component, maxLines: Int = 0, align: Alignment = Alignment.START, width: ElementSize = ElementSize.FIT, id: String? = null): TextElement =
    styledText(text, TextKind.H1, maxLines, align, width, id)

/**
 * Adds the largest heading with a plain text.
 *
 * @param text the heading
 * @param maxLines the largest number of lines shown, or `0` for the style's default
 * @param align how the lines are placed within the text's width
 * @param width how wide the heading is laid out
 * @param id the id of the heading, or `null` for a generated one
 * @return the heading
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.H1(text: String, maxLines: Int = 0, align: Alignment = Alignment.START, width: ElementSize = ElementSize.FIT, id: String? = null): TextElement =
    H1(Component.text(text), maxLines, align, width, id)

/**
 * Adds a section heading with a line below it.
 *
 * @param text the heading
 * @param maxLines the largest number of lines shown, or `0` for the style's default
 * @param align how the lines are placed within the text's width
 * @param width how wide the heading is laid out
 * @param id the id of the heading, or `null` for a generated one
 * @return the heading
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.H2(text: Component, maxLines: Int = 0, align: Alignment = Alignment.START, width: ElementSize = ElementSize.FIT, id: String? = null): TextElement =
    styledText(text, TextKind.H2, maxLines, align, width, id)

/**
 * Adds a section heading with a plain text and a line below it.
 *
 * @param text the heading
 * @param maxLines the largest number of lines shown, or `0` for the style's default
 * @param align how the lines are placed within the text's width
 * @param width how wide the heading is laid out
 * @param id the id of the heading, or `null` for a generated one
 * @return the heading
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.H2(text: String, maxLines: Int = 0, align: Alignment = Alignment.START, width: ElementSize = ElementSize.FIT, id: String? = null): TextElement =
    H2(Component.text(text), maxLines, align, width, id)

/**
 * Adds a subsection heading.
 *
 * @param text the heading
 * @param maxLines the largest number of lines shown, or `0` for the style's default
 * @param align how the lines are placed within the text's width
 * @param width how wide the heading is laid out
 * @param id the id of the heading, or `null` for a generated one
 * @return the heading
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.H3(text: Component, maxLines: Int = 0, align: Alignment = Alignment.START, width: ElementSize = ElementSize.FIT, id: String? = null): TextElement =
    styledText(text, TextKind.H3, maxLines, align, width, id)

/**
 * Adds a subsection heading with a plain text.
 *
 * @param text the heading
 * @param maxLines the largest number of lines shown, or `0` for the style's default
 * @param align how the lines are placed within the text's width
 * @param width how wide the heading is laid out
 * @param id the id of the heading, or `null` for a generated one
 * @return the heading
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.H3(text: String, maxLines: Int = 0, align: Alignment = Alignment.START, width: ElementSize = ElementSize.FIT, id: String? = null): TextElement =
    H3(Component.text(text), maxLines, align, width, id)

/**
 * Adds the smallest heading.
 *
 * @param text the heading
 * @param maxLines the largest number of lines shown, or `0` for the style's default
 * @param align how the lines are placed within the text's width
 * @param width how wide the heading is laid out
 * @param id the id of the heading, or `null` for a generated one
 * @return the heading
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.H4(text: Component, maxLines: Int = 0, align: Alignment = Alignment.START, width: ElementSize = ElementSize.FIT, id: String? = null): TextElement =
    styledText(text, TextKind.H4, maxLines, align, width, id)

/**
 * Adds the smallest heading with a plain text.
 *
 * @param text the heading
 * @param maxLines the largest number of lines shown, or `0` for the style's default
 * @param align how the lines are placed within the text's width
 * @param width how wide the heading is laid out
 * @param id the id of the heading, or `null` for a generated one
 * @return the heading
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.H4(text: String, maxLines: Int = 0, align: Alignment = Alignment.START, width: ElementSize = ElementSize.FIT, id: String? = null): TextElement =
    H4(Component.text(text), maxLines, align, width, id)

/**
 * Adds a paragraph of running text that wraps to the width it gets.
 *
 * @param text the paragraph
 * @param maxLines the largest number of lines shown, or `0` for no limit
 * @param align how the lines are placed within the text's width
 * @param width how wide the paragraph is laid out
 * @param id the id of the paragraph, or `null` for a generated one
 * @return the paragraph
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.P(text: Component, maxLines: Int = 0, align: Alignment = Alignment.START, width: ElementSize = ElementSize.FIT, id: String? = null): TextElement =
    styledText(text, TextKind.P, maxLines, align, width, id)

/**
 * Adds a paragraph of plain running text that wraps to the width it gets.
 *
 * @param text the paragraph
 * @param maxLines the largest number of lines shown, or `0` for no limit
 * @param align how the lines are placed within the text's width
 * @param width how wide the paragraph is laid out
 * @param id the id of the paragraph, or `null` for a generated one
 * @return the paragraph
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.P(text: String, maxLines: Int = 0, align: Alignment = Alignment.START, width: ElementSize = ElementSize.FIT, id: String? = null): TextElement =
    P(Component.text(text), maxLines, align, width, id)

/**
 * Adds a large muted introduction.
 *
 * @param text the introduction
 * @param maxLines the largest number of lines shown, or `0` for no limit
 * @param align how the lines are placed within the text's width
 * @param width how wide the introduction is laid out
 * @param id the id of the introduction, or `null` for a generated one
 * @return the introduction
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Lead(text: Component, maxLines: Int = 0, align: Alignment = Alignment.START, width: ElementSize = ElementSize.FIT, id: String? = null): TextElement =
    styledText(text, TextKind.LEAD, maxLines, align, width, id)

/**
 * Adds a large muted introduction with a plain text.
 *
 * @param text the introduction
 * @param maxLines the largest number of lines shown, or `0` for no limit
 * @param align how the lines are placed within the text's width
 * @param width how wide the introduction is laid out
 * @param id the id of the introduction, or `null` for a generated one
 * @return the introduction
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Lead(text: String, maxLines: Int = 0, align: Alignment = Alignment.START, width: ElementSize = ElementSize.FIT, id: String? = null): TextElement =
    Lead(Component.text(text), maxLines, align, width, id)

/**
 * Adds a large, bold text.
 *
 * @param text the text
 * @param maxLines the largest number of lines shown, or `0` for no limit
 * @param align how the lines are placed within the text's width
 * @param width how wide the text is laid out
 * @param id the id of the text, or `null` for a generated one
 * @return the text
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Large(text: Component, maxLines: Int = 0, align: Alignment = Alignment.START, width: ElementSize = ElementSize.FIT, id: String? = null): TextElement =
    styledText(text, TextKind.LARGE, maxLines, align, width, id)

/**
 * Adds a large, bold plain text.
 *
 * @param text the text
 * @param maxLines the largest number of lines shown, or `0` for no limit
 * @param align how the lines are placed within the text's width
 * @param width how wide the text is laid out
 * @param id the id of the text, or `null` for a generated one
 * @return the text
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Large(text: String, maxLines: Int = 0, align: Alignment = Alignment.START, width: ElementSize = ElementSize.FIT, id: String? = null): TextElement =
    Large(Component.text(text), maxLines, align, width, id)

/**
 * Adds a small text.
 *
 * @param text the text
 * @param maxLines the largest number of lines shown, or `0` for no limit
 * @param align how the lines are placed within the text's width
 * @param width how wide the text is laid out
 * @param id the id of the text, or `null` for a generated one
 * @return the text
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Small(text: Component, maxLines: Int = 0, align: Alignment = Alignment.START, width: ElementSize = ElementSize.FIT, id: String? = null): TextElement =
    styledText(text, TextKind.SMALL, maxLines, align, width, id)

/**
 * Adds a small plain text.
 *
 * @param text the text
 * @param maxLines the largest number of lines shown, or `0` for no limit
 * @param align how the lines are placed within the text's width
 * @param width how wide the text is laid out
 * @param id the id of the text, or `null` for a generated one
 * @return the text
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Small(text: String, maxLines: Int = 0, align: Alignment = Alignment.START, width: ElementSize = ElementSize.FIT, id: String? = null): TextElement =
    Small(Component.text(text), maxLines, align, width, id)

/**
 * Adds a muted text.
 *
 * @param text the text
 * @param maxLines the largest number of lines shown, or `0` for no limit
 * @param align how the lines are placed within the text's width
 * @param width how wide the text is laid out
 * @param id the id of the text, or `null` for a generated one
 * @return the text
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Muted(text: Component, maxLines: Int = 0, align: Alignment = Alignment.START, width: ElementSize = ElementSize.FIT, id: String? = null): TextElement =
    styledText(text, TextKind.MUTED, maxLines, align, width, id)

/**
 * Adds a muted plain text.
 *
 * @param text the text
 * @param maxLines the largest number of lines shown, or `0` for no limit
 * @param align how the lines are placed within the text's width
 * @param width how wide the text is laid out
 * @param id the id of the text, or `null` for a generated one
 * @return the text
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Muted(text: String, maxLines: Int = 0, align: Alignment = Alignment.START, width: ElementSize = ElementSize.FIT, id: String? = null): TextElement =
    Muted(Component.text(text), maxLines, align, width, id)

/**
 * Adds a quotation, italic and indented behind a line.
 *
 * @param text the quotation
 * @param maxLines the largest number of lines shown, or `0` for no limit
 * @param align how the lines are placed within the text's width
 * @param width how wide the quotation is laid out
 * @param id the id of the quotation, or `null` for a generated one
 * @return the quotation
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Blockquote(text: Component, maxLines: Int = 0, align: Alignment = Alignment.START, width: ElementSize = ElementSize.FIT, id: String? = null): TextElement =
    styledText(text, TextKind.BLOCKQUOTE, maxLines, align, width, id)

/**
 * Adds a plain quotation, italic and indented behind a line.
 *
 * @param text the quotation
 * @param maxLines the largest number of lines shown, or `0` for no limit
 * @param align how the lines are placed within the text's width
 * @param width how wide the quotation is laid out
 * @param id the id of the quotation, or `null` for a generated one
 * @return the quotation
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Blockquote(text: String, maxLines: Int = 0, align: Alignment = Alignment.START, width: ElementSize = ElementSize.FIT, id: String? = null): TextElement =
    Blockquote(Component.text(text), maxLines, align, width, id)

/**
 * Adds a piece of code on a muted background.
 *
 * @param text the code
 * @param maxLines the largest number of lines shown, or `0` for no limit
 * @param align how the lines are placed within the text's width
 * @param width how wide the code is laid out
 * @param id the id of the code, or `null` for a generated one
 * @return the code
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.InlineCode(text: Component, maxLines: Int = 0, align: Alignment = Alignment.START, width: ElementSize = ElementSize.FIT, id: String? = null): TextElement =
    styledText(text, TextKind.INLINE_CODE, maxLines, align, width, id)

/**
 * Adds a piece of plain code on a muted background.
 *
 * @param text the code
 * @param maxLines the largest number of lines shown, or `0` for no limit
 * @param align how the lines are placed within the text's width
 * @param width how wide the code is laid out
 * @param id the id of the code, or `null` for a generated one
 * @return the code
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.InlineCode(text: String, maxLines: Int = 0, align: Alignment = Alignment.START, width: ElementSize = ElementSize.FIT, id: String? = null): TextElement =
    InlineCode(Component.text(text), maxLines, align, width, id)

/**
 * Adds a bulleted or numbered list of texts.
 *
 * @param items the items, in order
 * @param ordered whether the items are numbered instead of bulleted
 * @param id the id of the list, or `null` for a generated one
 * @return the list
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.TypographyList(items: List<Component>, ordered: Boolean = false, id: String? = null): TextListElement =
    add(TextListElement(nextId(id), items, ordered))

/**
 * Adds a one-pixel line that separates content and grows along its orientation.
 *
 * @param orientation whether the line runs horizontally or vertically
 * @param id the id of the separator, or `null` for a generated one
 * @return the separator
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Separator(orientation: Orientation = Orientation.HORIZONTAL, id: String? = null): SeparatorElement {
    val elementId = nextId(id)
    return add(
        if (orientation == Orientation.HORIZONTAL) {
            SeparatorElement(elementId, orientation, width = ElementSize.grow())
        } else {
            SeparatorElement(elementId, orientation, height = ElementSize.grow())
        },
    )
}

/**
 * Adds a keyboard key, drawn as a small muted key cap.
 *
 * @param text the key
 * @param icon the Lucide name of an icon drawn before the text, or `null` for none
 * @param id the id of the key, or `null` for a generated one
 * @return the key
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Kbd(text: Component = Component.empty(), icon: String? = null, id: String? = null): KbdElement =
    add(KbdElement(nextId(id), text, icon))

/**
 * Adds a keyboard key with a plain text, drawn as a small muted key cap.
 *
 * @param text the key
 * @param icon the Lucide name of an icon drawn before the text, or `null` for none
 * @param id the id of the key, or `null` for a generated one
 * @return the key
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Kbd(text: String, icon: String? = null, id: String? = null): KbdElement = Kbd(Component.text(text), icon, id)

/**
 * Adds a row of keyboard keys, such as a shortcut.
 *
 * @param id the id of the group, or `null` for a generated one
 * @param children the builder of the keys and texts
 * @return the group
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.KbdGroup(id: String? = null, children: ComponentScope.() -> Unit): KbdGroupElement {
    val elementId = nextId(id)
    return add(KbdGroupElement(elementId, this.children(children)))
}

/**
 * Adds a small rounded label that marks something, such as a status.
 *
 * @param text the text
 * @param icon the Lucide name of an icon drawn before the text, or `null` for none
 * @param variant the look of the badge
 * @param id the id of the badge, or `null` for a generated one
 * @return the badge
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Badge(text: Component, icon: String? = null, variant: BadgeVariant = BadgeVariant.DEFAULT, id: String? = null): BadgeElement =
    add(BadgeElement(nextId(id), text, icon, variant))

/**
 * Adds a small rounded label with a plain text that marks something, such as a status.
 *
 * @param text the text
 * @param icon the Lucide name of an icon drawn before the text, or `null` for none
 * @param variant the look of the badge
 * @param id the id of the badge, or `null` for a generated one
 * @return the badge
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Badge(text: String, icon: String? = null, variant: BadgeVariant = BadgeVariant.DEFAULT, id: String? = null): BadgeElement =
    Badge(Component.text(text), icon, variant, id)

/**
 * Adds a placeholder that pulses while content is loading.
 *
 * @param width how wide the placeholder is laid out
 * @param height how tall the placeholder is laid out
 * @param round whether the placeholder is a circle or pill instead of a rounded rectangle
 * @param id the id of the placeholder, or `null` for a generated one
 * @return the placeholder
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Skeleton(width: ElementSize, height: ElementSize, round: Boolean = false, id: String? = null): SkeletonElement =
    add(SkeletonElement(nextId(id), round, width, height))

/**
 * Adds a turning loading indicator.
 *
 * @param size the side length of the indicator when it fits its content
 * @param tint the theme colour the indicator is drawn in
 * @param id the id of the indicator, or `null` for a generated one
 * @return the indicator
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Spinner(size: Int = 10, tint: IconTint = IconTint.FOREGROUND, id: String? = null): SpinnerElement =
    add(SpinnerElement(nextId(id), size, tint))

/**
 * Adds a bar that shows how far something has progressed.
 *
 * @param progress the filled fraction, from `0` to `1`
 * @param label the text drawn over the bar, or `null` for none
 * @param width how wide the bar is laid out
 * @param height how tall the bar is laid out
 * @param id the id of the bar, or `null` for a generated one
 * @return the bar
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Progress(
    progress: Float,
    label: Component? = null,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    id: String? = null,
): ProgressElement = add(ProgressElement(nextId(id), progress, label, width, height))

/**
 * Adds a box whose height follows its width in a fixed ratio, filled by its content.
 *
 * @param ratio the width divided by the height
 * @param width how wide the box is laid out
 * @param id the id of the box, or `null` for a generated one
 * @param children the builder of the content, drawn over the whole box
 * @return the box
 * @throws IllegalArgumentException if [id] starts with `_` or [ratio] is not positive
 */
fun ComponentScope.AspectRatio(
    ratio: Float,
    width: ElementSize = ElementSize.grow(),
    id: String? = null,
    children: ComponentScope.() -> Unit,
): AspectRatioElement {
    require(ratio > 0f) { "An aspect ratio must be positive, got $ratio" }
    val elementId = nextId(id)
    return add(AspectRatioElement(elementId, ratio, this.children(children), width))
}

/**
 * Adds a round picture of a person, with a fallback text while no picture is available.
 *
 * @param fallback the text shown while no picture is available, such as initials
 * @param source where the picture comes from, or `null` to always show the fallback
 * @param size the size of the avatar
 * @param badge whether a small badge is drawn at the bottom right; a badge icon implies a badge
 * @param badgeIcon the Lucide name of an icon in the badge, or `null` for a plain dot
 * @param id the id of the avatar, or `null` for a generated one
 * @return the avatar
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Avatar(
    fallback: Component,
    source: AvatarSource? = null,
    size: AvatarSize = AvatarSize.DEFAULT,
    badge: Boolean = false,
    badgeIcon: String? = null,
    id: String? = null,
): AvatarElement = add(AvatarElement(nextId(id), fallback, source, size, badge || badgeIcon != null, badgeIcon))

/**
 * Adds a round picture of a person, with a plain fallback text while no picture is available.
 *
 * @param fallback the text shown while no picture is available, such as initials
 * @param source where the picture comes from, or `null` to always show the fallback
 * @param size the size of the avatar
 * @param badge whether a small badge is drawn at the bottom right; a badge icon implies a badge
 * @param badgeIcon the Lucide name of an icon in the badge, or `null` for a plain dot
 * @param id the id of the avatar, or `null` for a generated one
 * @return the avatar
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Avatar(
    fallback: String,
    source: AvatarSource? = null,
    size: AvatarSize = AvatarSize.DEFAULT,
    badge: Boolean = false,
    badgeIcon: String? = null,
    id: String? = null,
): AvatarElement = Avatar(Component.text(fallback), source, size, badge, badgeIcon, id)

/**
 * Adds a row of overlapping avatars.
 *
 * @param id the id of the group, or `null` for a generated one
 * @param children the builder of the avatars and an optional count
 * @return the group
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.AvatarGroup(id: String? = null, children: ComponentScope.() -> Unit): AvatarGroupElement {
    val elementId = nextId(id)
    return add(AvatarGroupElement(elementId, this.children(children)))
}

/**
 * Adds the count of further people at the end of an avatar group.
 *
 * @param text the count, such as `+3`
 * @param icon the Lucide name of an icon shown instead of the text, or `null` for none
 * @param id the id of the count, or `null` for a generated one
 * @return the count
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.AvatarGroupCount(text: Component, icon: String? = null, id: String? = null): AvatarGroupCountElement =
    add(AvatarGroupCountElement(nextId(id), text, icon))

/**
 * Adds the plain count of further people at the end of an avatar group.
 *
 * @param text the count, such as `+3`
 * @param icon the Lucide name of an icon shown instead of the text, or `null` for none
 * @param id the id of the count, or `null` for a generated one
 * @return the count
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.AvatarGroupCount(text: String, icon: String? = null, id: String? = null): AvatarGroupCountElement =
    AvatarGroupCount(Component.text(text), icon, id)

/**
 * Adds a bordered callout, as wide as its container, with an optional icon beside its title and
 * description.
 *
 * @param variant the look of the alert
 * @param icon the Lucide name of the icon, or `null` for none
 * @param id the id of the alert, or `null` for a generated one
 * @param children the builder of the title, description and further content
 * @return the alert
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Alert(
    variant: AlertVariant = AlertVariant.DEFAULT,
    icon: String? = null,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): AlertElement {
    val elementId = nextId(id)
    return add(AlertElement(elementId, this.children(children), variant, icon))
}

/**
 * Adds the title of an alert, on one line.
 *
 * @param text the title
 * @param id the id of the title, or `null` for a generated one
 * @return the title
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.AlertTitle(text: Component, id: String? = null): TextElement = styledText(text, TextKind.ALERT_TITLE, id = id)

/**
 * Adds the plain title of an alert, on one line.
 *
 * @param text the title
 * @param id the id of the title, or `null` for a generated one
 * @return the title
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.AlertTitle(text: String, id: String? = null): TextElement = AlertTitle(Component.text(text), id)

/**
 * Adds the muted description of an alert.
 *
 * @param text the description
 * @param id the id of the description, or `null` for a generated one
 * @return the description
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.AlertDescription(text: Component, id: String? = null): TextElement = styledText(text, TextKind.ALERT_DESCRIPTION, id = id)

/**
 * Adds the plain, muted description of an alert.
 *
 * @param text the description
 * @param id the id of the description, or `null` for a generated one
 * @return the description
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.AlertDescription(text: String, id: String? = null): TextElement = AlertDescription(Component.text(text), id)

/**
 * Adds a bordered surface that stacks a header, content and footer.
 *
 * @param width how wide the card is laid out
 * @param id the id of the card, or `null` for a generated one
 * @param children the builder of the header, content and footer
 * @return the card
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Card(width: ElementSize = ElementSize.FIT, id: String? = null, children: ComponentScope.() -> Unit): CardElement {
    val elementId = nextId(id)
    return add(CardElement(elementId, this.children(children), width))
}

/**
 * Adds the header of a card, with its title and description and an optional action at the top
 * right.
 *
 * @param id the id of the header, or `null` for a generated one
 * @param children the builder of the title, description and action
 * @return the header
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CardHeader(id: String? = null, children: ComponentScope.() -> Unit): CardHeaderElement {
    val elementId = nextId(id)
    return add(CardHeaderElement(elementId, this.children(children)))
}

/**
 * Adds the bold title of a card.
 *
 * @param text the title
 * @param id the id of the title, or `null` for a generated one
 * @return the title
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CardTitle(text: Component, id: String? = null): TextElement = styledText(text, TextKind.CARD_TITLE, id = id)

/**
 * Adds the plain, bold title of a card.
 *
 * @param text the title
 * @param id the id of the title, or `null` for a generated one
 * @return the title
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CardTitle(text: String, id: String? = null): TextElement = CardTitle(Component.text(text), id)

/**
 * Adds the muted description of a card.
 *
 * @param text the description
 * @param id the id of the description, or `null` for a generated one
 * @return the description
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CardDescription(text: Component, id: String? = null): TextElement = styledText(text, TextKind.CARD_DESCRIPTION, id = id)

/**
 * Adds the plain, muted description of a card.
 *
 * @param text the description
 * @param id the id of the description, or `null` for a generated one
 * @return the description
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CardDescription(text: String, id: String? = null): TextElement = CardDescription(Component.text(text), id)

/**
 * Adds the action of a card header, placed at its top right.
 *
 * @param id the id of the action, or `null` for a generated one
 * @param children the builder of the action content
 * @return the action
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CardAction(id: String? = null, children: ComponentScope.() -> Unit): CardActionElement {
    val elementId = nextId(id)
    return add(CardActionElement(elementId, this.children(children)))
}

/**
 * Adds the content of a card.
 *
 * @param id the id of the content, or `null` for a generated one
 * @param children the builder of the content
 * @return the content
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CardContent(id: String? = null, children: ComponentScope.() -> Unit): CardContentElement {
    val elementId = nextId(id)
    return add(CardContentElement(elementId, this.children(children)))
}

/**
 * Adds the footer of a card, a row centered on one line.
 *
 * @param id the id of the footer, or `null` for a generated one
 * @param children the builder of the footer content
 * @return the footer
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CardFooter(id: String? = null, children: ComponentScope.() -> Unit): CardFooterElement {
    val elementId = nextId(id)
    return add(CardFooterElement(elementId, this.children(children)))
}

/**
 * Adds an empty state: a centered stack of a header and content that says there is nothing to
 * show.
 *
 * @param outline whether a dashed border is drawn around the state
 * @param id the id of the empty state, or `null` for a generated one
 * @param children the builder of the header and content
 * @return the empty state
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Empty(outline: Boolean = false, id: String? = null, children: ComponentScope.() -> Unit): EmptyElement {
    val elementId = nextId(id)
    return add(EmptyElement(elementId, this.children(children), outline))
}

/**
 * Adds the header of an empty state: its media, title and description, centered.
 *
 * @param id the id of the header, or `null` for a generated one
 * @param children the builder of the media, title and description
 * @return the header
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.EmptyHeader(id: String? = null, children: ComponentScope.() -> Unit): EmptyHeaderElement {
    val elementId = nextId(id)
    return add(EmptyHeaderElement(elementId, this.children(children)))
}

/**
 * Adds the media of an empty state, such as an icon or avatars.
 *
 * @param variant the look of the media
 * @param icon the Lucide name of the icon of the icon variant, or `null` for none
 * @param id the id of the media, or `null` for a generated one
 * @param children the builder of the content of the default variant
 * @return the media
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.EmptyMedia(
    variant: EmptyMediaVariant = EmptyMediaVariant.DEFAULT,
    icon: String? = null,
    id: String? = null,
    children: ComponentScope.() -> Unit = {},
): EmptyMediaElement {
    val elementId = nextId(id)
    return add(EmptyMediaElement(elementId, this.children(children), variant, icon))
}

/**
 * Adds the large, centered title of an empty state.
 *
 * @param text the title
 * @param id the id of the title, or `null` for a generated one
 * @return the title
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.EmptyTitle(text: Component, id: String? = null): TextElement = styledText(text, TextKind.EMPTY_TITLE, id = id)

/**
 * Adds the plain, large and centered title of an empty state.
 *
 * @param text the title
 * @param id the id of the title, or `null` for a generated one
 * @return the title
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.EmptyTitle(text: String, id: String? = null): TextElement = EmptyTitle(Component.text(text), id)

/**
 * Adds the muted, centered description of an empty state.
 *
 * @param text the description
 * @param id the id of the description, or `null` for a generated one
 * @return the description
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.EmptyDescription(text: Component, id: String? = null): TextElement = styledText(text, TextKind.EMPTY_DESCRIPTION, id = id)

/**
 * Adds the plain, muted and centered description of an empty state.
 *
 * @param text the description
 * @param id the id of the description, or `null` for a generated one
 * @return the description
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.EmptyDescription(text: String, id: String? = null): TextElement = EmptyDescription(Component.text(text), id)

/**
 * Adds the content of an empty state, such as buttons, centered below its header.
 *
 * @param id the id of the content, or `null` for a generated one
 * @param children the builder of the content
 * @return the content
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.EmptyContent(id: String? = null, children: ComponentScope.() -> Unit): EmptyContentElement {
    val elementId = nextId(id)
    return add(EmptyContentElement(elementId, this.children(children)))
}

/**
 * Adds an item, as wide as its container: a row of media, content and actions, with an optional
 * header above and footer below. An item with a click handler highlights under the mouse, can
 * take the focus, and runs the handler when it is clicked or activated; it never requires the
 * screen's input to be valid.
 *
 * @param variant the look of the item
 * @param size the size of the item
 * @param onClick the handler run when the player clicks the item, or `null` for an item that
 *        cannot be clicked
 * @param id the id of the item, or `null` for a generated one
 * @param children the builder of the header, media, content, actions and footer
 * @return the item
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Item(
    variant: ItemVariant = ItemVariant.DEFAULT,
    size: ItemSize = ItemSize.DEFAULT,
    onClick: ButtonHandler? = null,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): ItemElement {
    val elementId = nextId(id)
    return add(ItemElement(elementId, this.children(children), variant, size, bindButton(elementId, onClick), width = ElementSize.grow()))
}

/**
 * Adds the media at the start of an item, such as an icon, avatar or image.
 *
 * @param variant the look of the media
 * @param icon the Lucide name of the icon of the icon variant, or `null` for none
 * @param id the id of the media, or `null` for a generated one
 * @param children the builder of the content of the default and image variants
 * @return the media
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ItemMedia(
    variant: ItemMediaVariant = ItemMediaVariant.DEFAULT,
    icon: String? = null,
    id: String? = null,
    children: ComponentScope.() -> Unit = {},
): ItemMediaElement {
    val elementId = nextId(id)
    return add(ItemMediaElement(elementId, this.children(children), variant, icon))
}

/**
 * Adds the stacked title and description of an item, which takes the space its media and actions
 * leave.
 *
 * @param id the id of the content, or `null` for a generated one
 * @param children the builder of the title and description
 * @return the content
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ItemContent(id: String? = null, children: ComponentScope.() -> Unit): ItemContentElement {
    val elementId = nextId(id)
    return add(ItemContentElement(elementId, this.children(children)))
}

/**
 * Adds the title of an item.
 *
 * @param text the title
 * @param id the id of the title, or `null` for a generated one
 * @return the title
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ItemTitle(text: Component, id: String? = null): TextElement = styledText(text, TextKind.ITEM_TITLE, id = id)

/**
 * Adds the plain title of an item.
 *
 * @param text the title
 * @param id the id of the title, or `null` for a generated one
 * @return the title
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ItemTitle(text: String, id: String? = null): TextElement = ItemTitle(Component.text(text), id)

/**
 * Adds the muted description of an item, shown on at most two lines.
 *
 * @param text the description
 * @param id the id of the description, or `null` for a generated one
 * @return the description
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ItemDescription(text: Component, id: String? = null): TextElement = styledText(text, TextKind.ITEM_DESCRIPTION, id = id)

/**
 * Adds the plain, muted description of an item, shown on at most two lines.
 *
 * @param text the description
 * @param id the id of the description, or `null` for a generated one
 * @return the description
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ItemDescription(text: String, id: String? = null): TextElement = ItemDescription(Component.text(text), id)

/**
 * Adds the actions at the end of an item, such as buttons.
 *
 * @param id the id of the actions, or `null` for a generated one
 * @param children the builder of the actions
 * @return the actions
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ItemActions(id: String? = null, children: ComponentScope.() -> Unit): ItemActionsElement {
    val elementId = nextId(id)
    return add(ItemActionsElement(elementId, this.children(children)))
}

/**
 * Adds a line above the row of an item; its first part is placed at the start and the others at
 * the end.
 *
 * @param id the id of the header, or `null` for a generated one
 * @param children the builder of the content
 * @return the header
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ItemHeader(id: String? = null, children: ComponentScope.() -> Unit): ItemHeaderElement {
    val elementId = nextId(id)
    return add(ItemHeaderElement(elementId, this.children(children)))
}

/**
 * Adds a line below the row of an item; its first part is placed at the start and the others at
 * the end.
 *
 * @param id the id of the footer, or `null` for a generated one
 * @param children the builder of the content
 * @return the footer
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ItemFooter(id: String? = null, children: ComponentScope.() -> Unit): ItemFooterElement {
    val elementId = nextId(id)
    return add(ItemFooterElement(elementId, this.children(children)))
}

/**
 * Adds a stack of items and separators, each as wide as the group.
 *
 * @param id the id of the group, or `null` for a generated one
 * @param children the builder of the items and separators
 * @return the group
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ItemGroup(id: String? = null, children: ComponentScope.() -> Unit): ItemGroupElement {
    val elementId = nextId(id)
    return add(ItemGroupElement(elementId, this.children(children)))
}

/**
 * Adds a horizontal separator between the items of a group.
 *
 * @param id the id of the separator, or `null` for a generated one
 * @return the separator
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ItemSeparator(id: String? = null): SeparatorElement = Separator(id = id)

/**
 * Adds a Lucide icon, drawn square and tinted with a theme colour.
 *
 * @param icon the Lucide name of the icon, such as `trash`
 * @param size the side length when the icon fits its content, in GUI pixels
 * @param tint the theme colour the icon is tinted with
 * @param width how wide the icon is laid out
 * @param height how tall the icon is laid out
 * @param id the id of the icon, or `null` for a generated one
 * @return the icon
 * @throws IllegalArgumentException if [id] starts with `_` or [size] is not positive
 */
fun ComponentScope.Icon(
    icon: String,
    size: Int = 16,
    tint: IconTint = IconTint.FOREGROUND,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    id: String? = null,
): IconElement = add(IconElement(nextId(id), icon, size, tint, width, height))

/**
 * Adds a texture drawn over the element's area.
 *
 * @param texture the texture, such as `surf-roleplay:textures/gui/logo.png`
 * @param width how wide the image is laid out
 * @param height how tall the image is laid out
 * @param id the id of the image, or `null` for a generated one
 * @return the image
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Image(texture: Key, width: ElementSize = ElementSize.FIT, height: ElementSize = ElementSize.FIT, id: String? = null): ImageElement =
    add(ImageElement(nextId(id), texture, width, height))
