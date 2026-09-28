package dev.slne.surf.roleplay.fabric.ui.layout

import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.protocol.screen.Sizing
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [FlexLayout].
 */
class FlexLayoutTest {

    /**
     * Creates a leaf with a fixed size.
     *
     * @param width the width in GUI pixels
     * @param height the height in GUI pixels
     * @return the leaf
     */
    private fun fixed(width: Int, height: Int) = LayoutBox(width = Sizing.fixed(width), height = Sizing.fixed(height))

    /**
     * Creates a leaf that fits a content of the given size.
     *
     * @param width the content width
     * @param height the content height
     * @return the leaf
     */
    private fun content(width: Int, height: Int) = LayoutBox(content = Size(width, height))

    /**
     * Verifies that a row places fixed children after its padding, separated by its gap.
     */
    @Test
    fun `row places children with padding and gap`() {
        val a = fixed(10, 5)
        val b = fixed(20, 5)
        val row = LayoutBox(axis = Axis.HORIZONTAL, gap = 3, padding = Insets(1, 2, 3, 4), children = listOf(a, b))

        FlexLayout.layout(row, Rect(0, 0, 100, 50))

        assertEquals(Rect(4, 1, 10, 5), a.bounds)
        assertEquals(Rect(17, 1, 20, 5), b.bounds)
    }

    /**
     * Verifies that growing children share the remaining space by weight.
     */
    @Test
    fun `grow children share remaining space by weight`() {
        val fixedChild = fixed(60, 10)
        val one = LayoutBox(width = Sizing.grow(1))
        val two = LayoutBox(width = Sizing.grow(2))
        val row = LayoutBox(axis = Axis.HORIZONTAL, children = listOf(fixedChild, one, two))

        FlexLayout.layout(row, Rect(0, 0, 150, 20))

        assertEquals(Rect(60, 0, 30, 0), one.bounds)
        assertEquals(Rect(90, 0, 60, 0), two.bounds)
    }

    /**
     * Verifies that rounding leftovers of grow shares go to the last growing child, so that the
     * shares fill the space exactly.
     */
    @Test
    fun `grow shares fill the space exactly`() {
        val children = List(3) { LayoutBox(width = Sizing.grow()) }
        val row = LayoutBox(axis = Axis.HORIZONTAL, children = children)

        FlexLayout.layout(row, Rect(0, 0, 100, 10))

        assertEquals(listOf(33, 33, 34), children.map { it.bounds.width })
        assertEquals(listOf(0, 33, 66), children.map { it.bounds.x })
    }

    /**
     * Verifies that main-axis alignment shifts children when no child grows.
     */
    @Test
    fun `main alignment positions children in free space`() {
        val centered = fixed(20, 10)
        val ended = fixed(20, 10)

        FlexLayout.layout(LayoutBox(axis = Axis.HORIZONTAL, mainAlign = Align.CENTER, children = listOf(centered)), Rect(0, 0, 100, 10))
        FlexLayout.layout(LayoutBox(axis = Axis.HORIZONTAL, mainAlign = Align.END, children = listOf(ended)), Rect(0, 0, 100, 10))

        assertEquals(40, centered.bounds.x)
        assertEquals(80, ended.bounds.x)
    }

    /**
     * Verifies cross-axis alignment in a row: stretch fills the height, center and end place the
     * child vertically.
     */
    @Test
    fun `cross alignment places children on the cross axis`() {
        val stretched = content(10, 4)
        val centered = content(10, 4)
        val ended = content(10, 4)

        FlexLayout.layout(LayoutBox(axis = Axis.HORIZONTAL, crossAlign = Align.STRETCH, children = listOf(stretched)), Rect(0, 0, 50, 20))
        FlexLayout.layout(LayoutBox(axis = Axis.HORIZONTAL, crossAlign = Align.CENTER, children = listOf(centered)), Rect(0, 0, 50, 20))
        FlexLayout.layout(LayoutBox(axis = Axis.HORIZONTAL, crossAlign = Align.END, children = listOf(ended)), Rect(0, 0, 50, 20))

        assertEquals(Rect(0, 0, 10, 20), stretched.bounds)
        assertEquals(Rect(0, 8, 10, 4), centered.bounds)
        assertEquals(Rect(0, 16, 10, 4), ended.bounds)
    }

    /**
     * Verifies that a fitting column measures the sum of its children's heights and the widest
     * child, including gap and padding, when nested in a row.
     */
    @Test
    fun `nested fitting column measures its children`() {
        val column = LayoutBox(
            axis = Axis.VERTICAL,
            gap = 2,
            padding = Insets.all(1),
            children = listOf(content(30, 10), content(50, 10)),
        )
        val row = LayoutBox(axis = Axis.HORIZONTAL, children = listOf(column))

        FlexLayout.layout(row, Rect(0, 0, 200, 100))

        assertEquals(Rect(0, 0, 52, 24), column.bounds)
        assertEquals(Rect(1, 13, 50, 10), column.children[1].bounds)
    }

    /**
     * Verifies that children wider than the available space keep their size and overflow, and
     * that growing children then get no space.
     */
    @Test
    fun `overflowing children keep their size`() {
        val a = fixed(80, 10)
        val b = fixed(80, 10)
        val grow = LayoutBox(width = Sizing.grow())
        val row = LayoutBox(axis = Axis.HORIZONTAL, children = listOf(a, grow, b))

        FlexLayout.layout(row, Rect(0, 0, 100, 10))

        assertEquals(Rect(0, 0, 80, 10), a.bounds)
        assertEquals(0, grow.bounds.width)
        assertEquals(Rect(80, 0, 80, 10), b.bounds)
    }

    /**
     * Verifies that a scrolling column lays its children out at their own height beyond its
     * bounds and records the height of its content.
     */
    @Test
    fun `scrolling column records its content height`() {
        val children = List(5) { content(10, 20) }
        val list = LayoutBox(axis = Axis.VERTICAL, scrolls = true, gap = 5, children = children)

        FlexLayout.layout(list, Rect(0, 0, 40, 50))

        assertEquals(Rect(0, 100, 10, 20), children[4].bounds)
        assertEquals(120, list.contentExtent)
    }

    /**
     * Verifies that the root is sized by its own sizing within the available area and centered
     * in it.
     */
    @Test
    fun `root is sized and centered in the available area`() {
        val root = LayoutBox(width = Sizing.fixed(100), height = Sizing.grow(), axis = Axis.VERTICAL)

        FlexLayout.layoutRoot(root, 300, 200)

        assertEquals(Rect(100, 0, 100, 200), root.bounds)
    }

    /**
     * Verifies that a fixed root larger than the available area is shrunk to fit it.
     */
    @Test
    fun `root never exceeds the available area`() {
        val root = fixed(500, 500)

        FlexLayout.layoutRoot(root, 300, 200)

        assertEquals(Rect(0, 0, 300, 200), root.bounds)
    }
}
