package dev.slne.surf.roleplay.fabric.ui

import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the clip that culling sees while drawing.
 */
class ClipStackTest {

    /**
     * Verifies that while a rounded clip is drawn row by row, the clip that culling sees is the
     * whole rounded rectangle within the outer clip, not the current row.
     */
    @Test
    fun `rounded clips keep the whole rectangle for culling`() {
        val clips = ClipStack()
        val rect = Rect(10, 10, 20, 20)
        val seen = mutableListOf<Rect?>()
        val rows = mutableListOf<Rect>()

        clips.push(Rect(0, 0, 100, 25)) {
            clips.roundedRows(rect, 4) { row ->
                rows += row
                seen += clips.current
            }
        }

        assertEquals(RoundedShape.merge(RoundedShape.spans(rect, 4)).filter { it.y < 25 }, rows)
        assertEquals<List<Rect?>>(List(rows.size) { Rect(10, 10, 20, 15) }, seen)
        assertNull(clips.current)
    }

    /**
     * Verifies that a rounded clip runs the drawing code once per run of rows with the same
     * columns, not once per row, and still covers every row of the rounded rectangle.
     */
    @Test
    fun `rounded clips join rows with the same columns`() {
        val rect = Rect(0, 0, 40, 200)
        val rows = mutableListOf<Rect>()

        ClipStack().roundedRows(rect, 6) { rows += it }

        assertTrue(rows.size <= 2 * 6 + 1)
        assertEquals(200, rows.sumOf { it.height })
    }
}
