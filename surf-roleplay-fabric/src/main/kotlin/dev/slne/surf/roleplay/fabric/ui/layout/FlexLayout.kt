package dev.slne.surf.roleplay.fabric.ui.layout

import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.protocol.screen.SizeMode
import dev.slne.surf.roleplay.protocol.screen.Sizing
import kotlin.math.roundToInt

/**
 * A rectangle in GUI pixels.
 *
 * @property x the left edge
 * @property y the top edge
 * @property width the width
 * @property height the height
 */
data class Rect(val x: Int, val y: Int, val width: Int, val height: Int) {
    /**
     * The right edge, exclusive.
     */
    val right: Int get() = x + width

    /**
     * The bottom edge, exclusive.
     */
    val bottom: Int get() = y + height

    /**
     * Checks whether a point lies inside this rectangle.
     *
     * @param px the x coordinate
     * @param py the y coordinate
     * @return whether the point is inside
     */
    fun contains(px: Double, py: Double): Boolean = px >= x && px < right && py >= y && py < bottom

    /**
     * Returns this rectangle enlarged on every side.
     *
     * @param amount the distance to enlarge each side by
     * @return the enlarged rectangle
     */
    fun grow(amount: Int): Rect = Rect(x - amount, y - amount, width + 2 * amount, height + 2 * amount)

    /**
     * Holds the empty rectangle.
     */
    companion object {
        /**
         * The empty rectangle at the origin.
         */
        val EMPTY: Rect = Rect(0, 0, 0, 0)
    }
}

/**
 * A size in GUI pixels.
 *
 * @property width the width
 * @property height the height
 */
data class Size(val width: Int, val height: Int) {
    /**
     * Holds the empty size.
     */
    companion object {
        /**
         * The size of nothing.
         */
        val ZERO: Size = Size(0, 0)
    }
}

/**
 * The axis along which a container lays out its children.
 */
enum class Axis {
    /**
     * From left to right.
     */
    HORIZONTAL,

    /**
     * From top to bottom.
     */
    VERTICAL,
}

/**
 * A box in a layout tree: a leaf with a content size, or a container that lays out its children
 * along an axis.
 *
 * [FlexLayout] writes the computed [bounds] and [contentExtent] into each box.
 *
 * @property width how wide the box is laid out
 * @property height how tall the box is laid out
 * @property content the size a leaf's content needs, used by [SizeMode.FIT]
 * @property axis the axis of a container, or `null` for a leaf
 * @property children the children of a container, in layout order
 * @property gap the space between two children of a container
 * @property padding the space inside a container's edges
 * @property mainAlign how a container places its children along its axis
 * @property crossAlign how a container places its children across its axis
 * @property scrolls whether a container's children may extend beyond it along its axis, to be
 *           scrolled into view
 * @property measureContent computes the size a leaf's content needs when it may be at most the
 *           given width wide, for content such as wrapping text; `null` uses [content] at every
 *           width
 * @property aspectRatio for a container whose height follows its width, the width divided by the
 *           height, and `0` otherwise; such a container is as wide as it may be and lays out every
 *           child over its whole inner area
 * @property minWidth the narrowest width a leaf whose content wraps can take, such as the width
 *           of its longest word, or `null` for the width it takes when it may be no wider than
 *           nothing
 */
class LayoutBox(
    val width: Sizing = Sizing.FIT,
    val height: Sizing = Sizing.FIT,
    val content: Size = Size.ZERO,
    val axis: Axis? = null,
    val children: List<LayoutBox> = emptyList(),
    val gap: Int = 0,
    val padding: Insets = Insets.NONE,
    val mainAlign: Align = Align.START,
    val crossAlign: Align = Align.START,
    val scrolls: Boolean = false,
    val measureContent: ((Int) -> Size)? = null,
    val aspectRatio: Float = 0f,
    val minWidth: Int? = null,
) {
    /**
     * The sizes measured for this box, keyed by the width that was available.
     */
    internal val measured: MutableMap<Int, Size> = HashMap(4)

    /**
     * The area the box was laid out in.
     */
    var bounds: Rect = Rect.EMPTY
        internal set

    /**
     * For a container, the length of its content along its axis, including padding and gaps. For a
     * leaf, zero.
     */
    var contentExtent: Int = 0
        internal set
}

/**
 * Computes the positions and sizes of a tree of [LayoutBox]es.
 *
 * Sizes are computed height-for-width: a container passes the width it can give to its children
 * when it measures them, so that wrapping content grows taller in narrow containers. Along a
 * container's axis, children with a fixed or fitting size get that size, and growing children
 * share what is left by weight. In a row, children whose content wraps shrink towards their
 * narrowest width before the row overflows; children that still do not fit keep their size and
 * overflow the container. Across the axis, growing children and children of a stretching
 * container fill the container; other children keep their size, clamped to the container when
 * they fit their content.
 */
object FlexLayout {

    /**
     * The width available to a box that has no width limit.
     */
    const val UNBOUNDED: Int = Int.MAX_VALUE / 4

    /**
     * Lays out a root box within an area, sized by its own sizing and centered in the area. The
     * root never exceeds the area.
     *
     * @param root the root box
     * @param availableWidth the width of the area
     * @param availableHeight the height of the area
     */
    fun layoutRoot(root: LayoutBox, availableWidth: Int, availableHeight: Int) =
        layoutRoot(root, Rect(0, 0, availableWidth, availableHeight))

    /**
     * Lays out a root box within an area, sized by its own sizing and centered in the area. The
     * root never exceeds the area.
     *
     * @param root the root box
     * @param area the area to lay the root out in
     */
    fun layoutRoot(root: LayoutBox, area: Rect) {
        val width = resolveRoot(root.width, measure(root, area.width).width, area.width)
        val height = resolveRoot(root.height, measureAt(root, width).height, area.height)
        layout(root, Rect(area.x + (area.width - width) / 2, area.y + (area.height - height) / 2, width, height))
    }

    /**
     * Lays out a box in exactly the given area, and its children within it.
     *
     * @param box the box
     * @param area the area the box occupies
     */
    fun layout(box: LayoutBox, area: Rect) {
        box.bounds = area
        val axis = box.axis
        if (axis == null) {
            box.contentExtent = 0
            return
        }

        val padding = box.padding
        val innerMainStart = if (axis == Axis.HORIZONTAL) area.x + padding.left else area.y + padding.top
        val innerCrossStart = if (axis == Axis.HORIZONTAL) area.y + padding.top else area.x + padding.left
        val innerMain = (if (axis == Axis.HORIZONTAL) area.width - padding.left - padding.right else area.height - padding.top - padding.bottom).coerceAtLeast(0)
        val innerCross = (if (axis == Axis.HORIZONTAL) area.height - padding.top - padding.bottom else area.width - padding.left - padding.right).coerceAtLeast(0)

        val children = box.children
        if (box.aspectRatio > 0f) {
            val inner = Rect(area.x + padding.left, area.y + padding.top, (area.width - padding.left - padding.right).coerceAtLeast(0), (area.height - padding.top - padding.bottom).coerceAtLeast(0))
            children.forEach { layout(it, inner) }
            box.contentExtent = if (axis == Axis.HORIZONTAL) area.width else area.height
            return
        }
        val gaps = box.gap * (children.size - 1).coerceAtLeast(0)
        val crossSizes = IntArray(children.size)
        val mainSizes = if (axis == Axis.HORIZONTAL) {
            rowWidths(box, innerMain)
        } else {
            IntArray(children.size) { index ->
                val child = children[index]
                crossSizes[index] = crossSize(box, child, measure(child, innerCross).width, innerCross)
                if (growsAlong(box, child, axis)) 0 else measure(child, crossSizes[index]).height
            }
        }

        var fixedTotal = 0
        var weightTotal = 0
        children.forEachIndexed { index, child ->
            if (growsAlong(box, child, axis)) weightTotal += child.sizing(axis).value.coerceAtLeast(1) else fixedTotal += mainSizes[index]
        }
        val remaining = (innerMain - gaps - fixedTotal).coerceAtLeast(0)
        if (weightTotal > 0) distributeGrowth(box, axis, remaining, weightTotal, mainSizes)

        val used = mainSizes.sum() + gaps
        val free = (innerMain - used).coerceAtLeast(0)
        var cursor = innerMainStart + when (box.mainAlign) {
            Align.CENTER -> free / 2
            Align.END -> free
            Align.START, Align.STRETCH -> 0
        }

        children.forEachIndexed { index, child ->
            val mainSize = mainSizes[index]
            val crossSize = if (axis == Axis.HORIZONTAL) crossSize(box, child, measure(child, mainSize).height, innerCross) else crossSizes[index]
            val crossOffset = when (box.crossAlign) {
                Align.CENTER -> (innerCross - crossSize) / 2
                Align.END -> innerCross - crossSize
                Align.START, Align.STRETCH -> 0
            }
            val childArea = if (axis == Axis.HORIZONTAL) {
                Rect(cursor, innerCrossStart + crossOffset, mainSize, crossSize)
            } else {
                Rect(innerCrossStart + crossOffset, cursor, crossSize, mainSize)
            }
            layout(child, childArea)
            cursor += mainSize + box.gap
        }

        val paddingMain = if (axis == Axis.HORIZONTAL) padding.left + padding.right else padding.top + padding.bottom
        box.contentExtent = used + paddingMain
    }

    /**
     * Computes the size a box prefers when it may be at most a given width wide: its fixed size,
     * or the size its content or children need. A growing box prefers the size its content needs.
     * Content that cannot become narrower may still exceed the width.
     *
     * @param box the box
     * @param maxWidth the width available to the box
     * @return the preferred size
     */
    fun measure(box: LayoutBox, maxWidth: Int = UNBOUNDED): Size {
        val limit = if (box.width.mode == SizeMode.FIXED) box.width.value else maxWidth.coerceIn(0, UNBOUNDED)
        box.measured[limit]?.let { return it }
        val natural = naturalSize(box, limit)
        if (box.aspectRatio > 0f) {
            val width = if (box.width.mode == SizeMode.FIXED || limit < UNBOUNDED) limit else natural.width
            return Size(width, (width / box.aspectRatio).roundToInt()).also { box.measured[limit] = it }
        }
        val size = Size(
            if (box.width.mode == SizeMode.FIXED) box.width.value else natural.width,
            if (box.height.mode == SizeMode.FIXED) box.height.value else natural.height,
        )
        box.measured[limit] = size
        return size
    }

    /**
     * Computes the size of a box laid out at exactly a width, even if its own width is fixed to
     * another: the width itself, and the height its content or children need at that width.
     *
     * @param box the box
     * @param width the width the box is laid out at
     * @return the size
     */
    fun measureAt(box: LayoutBox, width: Int): Size {
        val height = when {
            box.height.mode == SizeMode.FIXED -> box.height.value
            box.aspectRatio > 0f -> (width / box.aspectRatio).roundToInt()
            else -> naturalSize(box, width.coerceIn(0, UNBOUNDED)).height
        }
        return Size(width, height)
    }

    /**
     * Computes the narrowest width a box can take without breaking words: its fixed width, the
     * narrowest width of a wrapping leaf, the width of any other leaf, or the narrowest widths of
     * a container's children with its gaps and padding.
     *
     * @param box the box
     * @return the narrowest width
     */
    private fun narrowest(box: LayoutBox): Int {
        if (box.width.mode == SizeMode.FIXED) return box.width.value
        val axis = box.axis ?: return box.minWidth ?: measure(box, 0).width
        val paddingX = box.padding.left + box.padding.right
        if (box.aspectRatio > 0f) return paddingX
        val widths = box.children.map { narrowest(it) }
        return if (axis == Axis.HORIZONTAL) {
            widths.sum() + box.gap * (widths.size - 1).coerceAtLeast(0) + paddingX
        } else {
            (widths.maxOrNull() ?: 0) + paddingX
        }
    }

    /**
     * Computes the size a box's content or children need at a width, ignoring its own sizing.
     *
     * @param box the box
     * @param limit the width available to the box
     * @return the size of a leaf's content, or of a container's children with gaps and padding
     */
    private fun naturalSize(box: LayoutBox, limit: Int): Size {
        val axis = box.axis ?: return box.measureContent?.invoke(limit) ?: box.content
        val padding = box.padding
        val paddingX = padding.left + padding.right
        val paddingY = padding.top + padding.bottom
        val inner = if (limit >= UNBOUNDED) UNBOUNDED else (limit - paddingX).coerceAtLeast(0)
        val gaps = box.gap * (box.children.size - 1).coerceAtLeast(0)
        return if (axis == Axis.HORIZONTAL) {
            val widths = rowWidths(box, inner)
            val growing = box.children.indices.filter { growsAlong(box, box.children[it], Axis.HORIZONTAL) }
            val preferred = growing.sumOf { measure(box.children[it], inner).width }
            val fixed = widths.sum() + gaps
            if (inner >= UNBOUNDED) {
                growing.forEach { widths[it] = measure(box.children[it], inner).width }
            } else if (growing.isNotEmpty()) {
                val space = ((fixed + preferred).coerceAtMost(inner) - fixed).coerceAtLeast(0)
                distributeGrowth(box, Axis.HORIZONTAL, space, growing.sumOf { box.children[it].width.value.coerceAtLeast(1) }, widths)
            }
            val height = box.children.indices.maxOfOrNull { measure(box.children[it], widths[it]).height } ?: 0
            Size(fixed + preferred + paddingX, height + paddingY)
        } else {
            val sizes = box.children.map { measure(it, inner) }
            Size((sizes.maxOfOrNull { it.width } ?: 0) + paddingX, sizes.sumOf { it.height } + gaps + paddingY)
        }
    }

    /**
     * Computes the widths of a row's fixed and fitting children. Every such child gets the width
     * it prefers within the row; if they do not fit, fitting children whose content can become
     * narrower shrink in proportion to how much they can shrink, down to their narrowest width.
     * Growing children get no width here, since they share the rest afterwards.
     *
     * @param box the row
     * @param inner the width inside the row's padding
     * @return the widths of the children
     */
    private fun rowWidths(box: LayoutBox, inner: Int): IntArray {
        val children = box.children
        val widths = IntArray(children.size)
        val shrinkable = IntArray(children.size)
        children.forEachIndexed { index, child ->
            if (growsAlong(box, child, Axis.HORIZONTAL)) return@forEachIndexed
            widths[index] = measure(child, inner).width
            if (child.width.mode == SizeMode.FIT && inner < UNBOUNDED) {
                shrinkable[index] = (widths[index] - narrowest(child)).coerceAtLeast(0)
            }
        }
        if (inner >= UNBOUNDED || box.scrolls) return widths
        val gaps = box.gap * (children.size - 1).coerceAtLeast(0)
        val overflow = widths.sum() + gaps - inner
        val shrinkTotal = shrinkable.sum()
        if (overflow <= 0 || shrinkTotal == 0) return widths
        val shrinkBy = overflow.coerceAtMost(shrinkTotal)
        var taken = 0
        children.indices.forEach { index ->
            val share = (shrinkBy.toLong() * shrinkable[index] / shrinkTotal).toInt()
            widths[index] -= share
            shrinkable[index] -= share
            taken += share
        }
        var index = 0
        while (taken < shrinkBy) {
            if (shrinkable[index] > 0) {
                widths[index]--
                shrinkable[index]--
                taken++
            }
            index = (index + 1) % children.size
        }
        return widths
    }

    /**
     * Returns the size of a child across its container's axis.
     *
     * @param box the container
     * @param child the child
     * @param preferred the child's preferred size across the axis
     * @param innerCross the size inside the container across the axis
     * @return the child's cross size
     */
    private fun crossSize(box: LayoutBox, child: LayoutBox, preferred: Int, innerCross: Int): Int {
        val axis = box.axis ?: return preferred
        val sizing = child.sizing(axis.other())
        return when {
            sizing.mode == SizeMode.GROW || box.crossAlign == Align.STRETCH -> innerCross
            sizing.mode == SizeMode.FIXED -> sizing.value
            else -> preferred.coerceAtMost(innerCross)
        }
    }

    /**
     * Returns whether a child grows along its container's axis.
     *
     * @param box the container
     * @param child the child
     * @param axis the container's axis
     * @return whether the child shares the container's remaining space
     */
    private fun growsAlong(box: LayoutBox, child: LayoutBox, axis: Axis): Boolean = child.sizing(axis).mode == SizeMode.GROW && !box.scrolls

    /**
     * Shares space among a container's growing children by weight. The rounding leftover goes to
     * the last growing child, so that the shares add up to the space exactly.
     *
     * @param box the container
     * @param axis the container's axis
     * @param space the space to share
     * @param weightTotal the sum of the growing children's weights
     * @param mainSizes the children's sizes along the axis, filled in for the growing children
     */
    private fun distributeGrowth(box: LayoutBox, axis: Axis, space: Int, weightTotal: Int, mainSizes: IntArray) {
        var given = 0
        val lastGrowing = box.children.indexOfLast { it.sizing(axis).mode == SizeMode.GROW }
        box.children.forEachIndexed { index, child ->
            val sizing = child.sizing(axis)
            if (sizing.mode != SizeMode.GROW) return@forEachIndexed
            val share = if (index == lastGrowing) space - given else space * sizing.value.coerceAtLeast(1) / weightTotal
            mainSizes[index] = share
            given += share
        }
    }

    /**
     * Resolves the size of the root along one axis.
     *
     * @param sizing the root's sizing on that axis
     * @param preferred the root's preferred size on that axis
     * @param available the available size on that axis
     * @return the size, never larger than [available]
     */
    private fun resolveRoot(sizing: Sizing, preferred: Int, available: Int): Int =
        if (sizing.mode == SizeMode.GROW) available else preferred.coerceAtMost(available)

    /**
     * Returns the box's sizing along an axis.
     *
     * @param axis the axis
     * @return the width sizing for [Axis.HORIZONTAL], the height sizing otherwise
     */
    private fun LayoutBox.sizing(axis: Axis): Sizing = if (axis == Axis.HORIZONTAL) width else height

    /**
     * Returns the axis perpendicular to this one.
     *
     * @return the other axis
     */
    private fun Axis.other(): Axis = if (this == Axis.HORIZONTAL) Axis.VERTICAL else Axis.HORIZONTAL
}
