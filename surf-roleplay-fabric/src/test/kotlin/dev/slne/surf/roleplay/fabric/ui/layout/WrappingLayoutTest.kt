package dev.slne.surf.roleplay.fabric.ui.layout

import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.protocol.screen.Sizing
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the width-aware measuring of [FlexLayout] with wrapping leaves.
 */
class WrappingLayoutTest {

    /**
     * Creates a leaf that behaves like a text of equally wide words wrapped to the width it gets.
     *
     * @param words the number of words
     * @param wordWidth the width of one word
     * @param lineHeight the height of one line
     * @param width how wide the leaf is laid out
     * @return the leaf
     */
    private fun text(words: Int, wordWidth: Int = 20, lineHeight: Int = 10, width: Sizing = Sizing.FIT) = LayoutBox(
        width = width,
        measureContent = { limit ->
            val perLine = (limit / wordWidth).coerceIn(1, words)
            val lines = (words + perLine - 1) / perLine
            Size(perLine * wordWidth, lines * lineHeight)
        },
    )

    /**
     * Verifies that a column passes its inner width to a text, which wraps to it.
     */
    @Test
    fun `column wraps a text to its inner width`() {
        val text = text(5)
        val column = LayoutBox(width = Sizing.fixed(50), axis = Axis.VERTICAL, padding = Insets(5, 5, 5, 5), children = listOf(text))

        assertEquals(Size(50, 40), FlexLayout.measure(column))
        FlexLayout.layout(column, Rect(0, 0, 50, 40))

        assertEquals(Rect(5, 5, 40, 30), text.bounds)
    }

    /**
     * Verifies that a text that fits its container keeps one line.
     */
    @Test
    fun `text that fits keeps one line`() {
        val text = text(5)
        val column = LayoutBox(width = Sizing.fixed(200), axis = Axis.VERTICAL, children = listOf(text))

        FlexLayout.layout(column, Rect(0, 0, 200, 100))

        assertEquals(Rect(0, 0, 100, 10), text.bounds)
    }

    /**
     * Verifies that a row shrinks a text next to a fixed sibling instead of overflowing.
     */
    @Test
    fun `row shrinks a wrapping child next to a fixed sibling`() {
        val fixed = LayoutBox(width = Sizing.fixed(40), height = Sizing.fixed(10))
        val text = text(5)
        val row = LayoutBox(axis = Axis.HORIZONTAL, children = listOf(fixed, text))

        assertEquals(Size(100, 20), FlexLayout.measure(row, 100))
        FlexLayout.layout(row, Rect(0, 0, 100, 50))

        assertEquals(Rect(40, 0, 60, 20), text.bounds)
    }

    /**
     * Verifies that a growing text wraps to the share of the row it gets.
     */
    @Test
    fun `growing text wraps to its share`() {
        val fixed = LayoutBox(width = Sizing.fixed(40), height = Sizing.fixed(10))
        val text = text(5, width = Sizing.grow())
        val row = LayoutBox(axis = Axis.HORIZONTAL, children = listOf(fixed, text))

        FlexLayout.layout(row, Rect(0, 0, 100, 50))

        assertEquals(Rect(40, 0, 60, 20), text.bounds)
    }

    /**
     * Verifies that a scroll list wraps its children to its width and records the wrapped
     * height.
     */
    @Test
    fun `scroll list wraps its children`() {
        val text = text(5)
        val list = LayoutBox(axis = Axis.VERTICAL, scrolls = true, crossAlign = Align.STRETCH, children = listOf(text))

        FlexLayout.layout(list, Rect(0, 0, 60, 15))

        assertEquals(Rect(0, 0, 60, 20), text.bounds)
        assertEquals(20, list.contentExtent)
    }

    /**
     * Verifies that a column inside a row shrinks with the text it holds.
     */
    @Test
    fun `nested column shrinks with its text`() {
        val fixed = LayoutBox(width = Sizing.fixed(40), height = Sizing.fixed(10))
        val text = text(5)
        val column = LayoutBox(axis = Axis.VERTICAL, children = listOf(text))
        val row = LayoutBox(axis = Axis.HORIZONTAL, children = listOf(fixed, column))

        FlexLayout.layout(row, Rect(0, 0, 100, 50))

        assertEquals(Rect(40, 0, 60, 20), column.bounds)
        assertEquals(Rect(40, 0, 60, 20), text.bounds)
    }

    /**
     * Verifies that children that cannot wrap still overflow a row instead of shrinking.
     */
    @Test
    fun `children that cannot wrap keep their size`() {
        val a = LayoutBox(content = Size(70, 10))
        val b = LayoutBox(content = Size(70, 10))
        val row = LayoutBox(axis = Axis.HORIZONTAL, children = listOf(a, b))

        FlexLayout.layout(row, Rect(0, 0, 100, 10))

        assertEquals(Rect(70, 0, 70, 10), b.bounds)
    }

    /**
     * Verifies that measuring a row gives a growing text the width layout gives it, so that the
     * measured height is the laid-out height.
     */
    @Test
    fun `measured row height matches the laid-out growing text`() {
        val fixed = LayoutBox(width = Sizing.fixed(40), height = Sizing.fixed(10))
        val text = text(5, width = Sizing.grow())
        val row = LayoutBox(axis = Axis.HORIZONTAL, children = listOf(fixed, text))

        assertEquals(20, FlexLayout.measure(row, 100).height)
    }

    /**
     * Verifies that a growing sibling does not make a fitting text shrink while measuring, since
     * layout gives the growing sibling only what the text leaves.
     */
    @Test
    fun `growing sibling does not shrink a fitting text`() {
        val text = text(3)
        val grow = LayoutBox(width = Sizing.grow(), content = Size(50, 10))
        val row = LayoutBox(axis = Axis.HORIZONTAL, children = listOf(text, grow))

        assertEquals(10, FlexLayout.measure(row, 100).height)
    }

    /**
     * Verifies that a row does not shrink a text below its narrowest width, its longest word, and
     * takes the shrink from a text that can give it.
     */
    @Test
    fun `row shrinks texts no further than their longest word`() {
        val word = LayoutBox(
            measureContent = { limit -> if (limit >= 60) Size(60, 10) else Size(limit.coerceAtLeast(5), 10 * ((60 + limit - 1) / limit.coerceAtLeast(1))) },
            minWidth = 60,
        )
        val sentence = text(10)
        val row = LayoutBox(axis = Axis.HORIZONTAL, children = listOf(word, sentence))

        FlexLayout.layout(row, Rect(0, 0, 100, 100))

        assertEquals(60, word.bounds.width)
        assertEquals(40, sentence.bounds.width)
    }

    /**
     * Verifies that a box can be measured at exactly a width, ignoring its own fixed width, as
     * when a fixed root is laid out narrower than it asks for.
     */
    @Test
    fun `fixed box measured at a smaller width wraps its texts`() {
        val column = LayoutBox(width = Sizing.fixed(200), axis = Axis.VERTICAL, children = listOf(text(10)))

        assertEquals(Size(200, 10), FlexLayout.measure(column))
        assertEquals(Size(100, 20), FlexLayout.measureAt(column, 100))
    }
}
