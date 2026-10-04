package dev.slne.surf.roleplay.fabric.ui

import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [RoundedShape].
 */
class RoundedShapeTest {

    /**
     * Verifies that a radius of zero yields one span per row covering the whole width.
     */
    @Test
    fun `zero radius covers the rectangle`() {
        val spans = RoundedShape.spans(Rect(2, 3, 5, 4), 0)

        assertEquals(4, spans.size)
        spans.forEach { assertEquals(2, it.x0); assertEquals(7, it.x1) }
    }

    /**
     * Verifies that corner rows are inset symmetrically, more at the very top and bottom, and
     * that middle rows cover the whole width.
     */
    @Test
    fun `corner rows are inset`() {
        val spans = RoundedShape.spans(Rect(0, 0, 20, 10), 3)

        val top = spans.first()
        val middle = spans[5]
        assertTrue(top.x0 > 0 && top.x1 < 20)
        assertEquals(20 - top.x1, top.x0)
        assertEquals(0, middle.x0)
        assertEquals(20, middle.x1)
        assertEquals(spans.first().x0, spans.last().x0)
        assertTrue(spans[0].x0 >= spans[1].x0 && spans[1].x0 >= spans[2].x0)
    }

    /**
     * Verifies that the radius is limited to half the shorter side.
     */
    @Test
    fun `radius is limited to half the shorter side`() {
        val spans = RoundedShape.spans(Rect(0, 0, 40, 4), 10)

        assertEquals(4, spans.size)
        assertTrue(spans.all { it.x0 <= 2 && it.x1 >= 38 })
    }

    /**
     * Verifies that the border ring of a rounded rectangle leaves the inside empty.
     */
    @Test
    fun `border spans leave the inside empty`() {
        val border = RoundedShape.borderSpans(Rect(0, 0, 10, 6), 2)

        val middleRow = border.filter { it.y == 3 }
        assertEquals(2, middleRow.size)
        assertEquals(0, middleRow[0].x0)
        assertEquals(1, middleRow[0].x1)
        assertEquals(9, middleRow[1].x0)
        assertEquals(10, middleRow[1].x1)
        val topRow = border.filter { it.y == 0 }
        assertEquals(1, topRow.size)
    }

    /**
     * Verifies that spans of consecutive rows with the same columns join into one rectangle and
     * that other spans stay rectangles of their own.
     */
    @Test
    fun `merge joins equal spans of consecutive rows`() {
        val spans = listOf(Span(0, 2, 8), Span(1, 0, 10), Span(2, 0, 10), Span(3, 0, 10), Span(4, 2, 8), Span(5, 0, 1), Span(5, 9, 10), Span(6, 0, 1), Span(6, 9, 10))

        assertEquals(
            listOf(Rect(2, 0, 6, 1), Rect(0, 1, 10, 3), Rect(2, 4, 6, 1), Rect(0, 5, 1, 2), Rect(9, 5, 1, 2)),
            RoundedShape.merge(spans),
        )
    }
}
