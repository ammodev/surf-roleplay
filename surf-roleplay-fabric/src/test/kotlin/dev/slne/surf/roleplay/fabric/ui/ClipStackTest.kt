package dev.slne.surf.roleplay.fabric.ui

import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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

        assertEquals(15, rows.size)
        assertEquals<List<Rect?>>(List(15) { Rect(10, 10, 20, 15) }, seen)
        assertNull(clips.current)
    }
}
