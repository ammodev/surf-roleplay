package dev.slne.surf.roleplay.fabric.ui.icon

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [LucideIndex].
 */
class LucideIndexTest {

    /**
     * A small index with one icon and one alias of it.
     */
    private val json = """
        {"version":"1.48.0","cell":64,"columns":64,"width":4096,"height":1856,"icons":{
          "trash":{"x":34,"y":26,"categories":["files","mail"]},
          "trash-2":{"x":34,"y":26,"alias":"trash"}
        }}
    """.trimIndent()

    /**
     * Verifies that icons are found by name with their atlas cell, texture coordinates and
     * categories.
     */
    @Test
    fun `icons are found by name`() {
        val index = LucideIndex.parse(json)

        val trash = index.find("trash")!!
        assertEquals(34 * 64f, trash.u)
        assertEquals(26 * 64f, trash.v)
        assertEquals(listOf("files", "mail"), trash.categories)
        assertEquals(64, index.cell)
        assertEquals(4096, index.width)
    }

    /**
     * Verifies that aliases resolve to their icon and that unknown names find nothing.
     */
    @Test
    fun `aliases resolve and unknown names do not`() {
        val index = LucideIndex.parse(json)

        assertEquals(index.find("trash")!!.u, index.find("trash-2")!!.u)
        assertNull(index.find("does-not-exist"))
        assertEquals(listOf("trash"), index.names)
    }
}
