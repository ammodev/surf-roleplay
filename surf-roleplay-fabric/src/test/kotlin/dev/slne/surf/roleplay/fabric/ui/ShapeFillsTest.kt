package dev.slne.surf.roleplay.fabric.ui

import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for filling shapes as few GUI elements.
 */
class ShapeFillsTest {

    /**
     * A fill target that records every batch it receives as one GUI element.
     */
    private class CountingTarget : FillTarget {
        /**
         * The rectangles of every batch, as left, top, right, bottom and colour.
         */
        val batches = mutableListOf<List<List<Int>>>()

        /**
         * Records a batch.
         *
         * @param data the rectangles, five numbers each
         * @param count the number of rectangles
         */
        override fun submit(data: IntArray, count: Int) {
            batches += (0 until count).map { index -> (0 until FillBatchRenderState.FIELDS).map { data[index * FillBatchRenderState.FIELDS + it] } }
        }
    }

    /**
     * Returns the pixels a list of spans covers.
     *
     * @param spans the spans
     * @return the pixels as column and row
     */
    private fun pixelsOf(spans: List<Span>): List<Pair<Int, Int>> =
        spans.flatMap { span -> (span.x0 until span.x1).map { it to span.y } }

    /**
     * Returns the pixels the rectangles of batches cover, once per time they are covered.
     *
     * @param batches the batches
     * @return the pixels as column and row
     */
    private fun batchPixels(batches: List<List<List<Int>>>): List<Pair<Int, Int>> =
        batches.flatten().flatMap { (x0, y0, x1, y1) -> (y0 until y1).flatMap { y -> (x0 until x1).map { it to y } } }

    /**
     * Verifies that a tall card with a shadow, a surface and a border submits a small, constant
     * number of GUI elements.
     */
    @Test
    fun `a tall card submits a few elements`() {
        val target = CountingTarget()
        val shapes = ShapeFills(target)
        val card = Rect(10, 10, 120, 200)

        shapes.rounded(card.copy(y = card.y + 1), 0x10000000, 6)
        shapes.rounded(card, 0xFF202020.toInt(), 6)
        shapes.roundedBorder(card, 0xFF404040.toInt(), 6)

        assertEquals(3, target.batches.size)
        assertEquals(3, shapes.elements)
        assertTrue(target.batches.all { it.size <= 30 })
    }

    /**
     * Verifies that a rounded fill covers exactly the pixels of its spans, each once, in its
     * colour, for many sizes, radii and corner sets.
     */
    @Test
    fun `rounded fills cover the pixels of the spans`() {
        val cornerSets = listOf(Corners.ALL, Corners.NONE, Corners.LEFT, Corners.RIGHT, Corners.TOP, Corners.BOTTOM)
        for (width in listOf(1, 2, 5, 12, 40)) for (height in listOf(1, 3, 8, 200)) for (radius in 0..7) for (corners in cornerSets) {
            val rect = Rect(3, 4, width, height)
            val target = CountingTarget()

            ShapeFills(target).rounded(rect, 7, radius, corners)

            val expected = pixelsOf(RoundedShape.spans(rect, radius, corners)).sortedWith(compareBy({ it.second }, { it.first }))
            assertEquals(expected, batchPixels(target.batches).sortedWith(compareBy({ it.second }, { it.first })), "$rect r=$radius $corners")
            assertTrue(target.batches.flatten().all { it[4] == 7 })
            assertTrue(target.batches.size <= 1)
        }
    }

    /**
     * Verifies that a rounded border covers exactly the pixels of its spans, each once, for many
     * sizes, radii and corner sets.
     */
    @Test
    fun `rounded borders cover the pixels of the spans`() {
        val cornerSets = listOf(Corners.ALL, Corners.NONE, Corners.LEFT, Corners.TOP)
        for (width in listOf(1, 2, 5, 12, 40)) for (height in listOf(1, 3, 8, 200)) for (radius in 0..7) for (corners in cornerSets) {
            val rect = Rect(-2, 5, width, height)
            val target = CountingTarget()

            ShapeFills(target).roundedBorder(rect, 9, radius, corners)

            val expected = pixelsOf(RoundedShape.borderSpans(rect, radius, corners)).sortedWith(compareBy({ it.second }, { it.first }))
            assertEquals(expected, batchPixels(target.batches).sortedWith(compareBy({ it.second }, { it.first })), "$rect r=$radius $corners")
            assertTrue(target.batches.size <= 1)
        }
    }

    /**
     * Verifies that rectangles become one element and that nothing is submitted for none.
     */
    @Test
    fun `rectangles become one element`() {
        val target = CountingTarget()
        val shapes = ShapeFills(target)

        shapes.rects(emptyList(), 1)
        shapes.rects(listOf(Rect(0, 0, 2, 2), Rect(5, 5, 1, 3), Rect(9, 9, 0, 4)), 3)

        assertEquals(listOf(listOf(listOf(0, 0, 2, 2, 3), listOf(5, 5, 6, 8, 3))), target.batches)
        assertEquals(1, shapes.elements)
    }

    /**
     * Verifies that a dashed border is one element with the dashes along the top and bottom
     * edges first and then along the left and right edges.
     */
    @Test
    fun `a dashed border is one element`() {
        val target = CountingTarget()
        val shapes = ShapeFills(target)

        shapes.dashedBorder(Rect(0, 0, 8, 4), 5, 3, 2)

        assertEquals(1, shapes.elements)
        assertEquals(
            listOf(
                listOf(0, 0, 3, 1, 5), listOf(0, 3, 3, 4, 5), listOf(5, 0, 8, 1, 5), listOf(5, 3, 8, 4, 5),
                listOf(0, 0, 1, 3, 5), listOf(7, 0, 8, 3, 5),
            ),
            target.batches.single(),
        )
    }
}
