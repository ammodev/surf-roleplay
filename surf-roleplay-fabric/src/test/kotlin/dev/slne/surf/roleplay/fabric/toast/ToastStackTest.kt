package dev.slne.surf.roleplay.fabric.toast

import dev.slne.surf.roleplay.fabric.settings.KeyMode
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.protocol.toast.ToastButtonKind
import dev.slne.surf.roleplay.protocol.toast.ToastShow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the toast stack and the HUD cursor in the mod.
 */
class ToastStackTest {

    /**
     * A measurer in which every character is 5 pixels wide.
     */
    private val measurer = object : TextMeasurer {
        override val lineHeight: Int = 9
        override fun width(json: String): Int = json.length * 5
        override fun plainWidth(text: String): Int = text.length * 5
    }

    /**
     * Verifies that a toast is replaced in place by a toast with the same id, and removed by a
     * dismissal.
     */
    @Test
    fun `toasts are replaced and dismissed by id`() {
        val stack = ToastStack()
        stack.show(ToastShow("a", title = "Laden"), now = 0)
        stack.show(ToastShow("b", title = "B"), now = 0)
        stack.show(ToastShow("a", title = "Fertig"), now = 10)

        assertEquals(listOf("a", "b"), stack.update(now = 20, paused = false).map { it.packet.id })
        assertEquals("Fertig", stack.update(now = 20, paused = false).first().packet.title)
        stack.dismiss("b")
        assertEquals(listOf("a"), stack.update(now = 30, paused = false).map { it.packet.id })
    }

    /**
     * Verifies that a toast expires after its duration, that pausing stops the clock, and that a
     * toast without a duration stays.
     */
    @Test
    fun `toasts expire unless paused`() {
        val stack = ToastStack()
        stack.show(ToastShow("timed", durationMillis = 1000), now = 0)
        stack.show(ToastShow("forever", durationMillis = 0), now = 0)

        stack.update(now = 500, paused = false)
        stack.update(now = 5000, paused = true)
        assertEquals(listOf("timed", "forever"), stack.update(now = 5000, paused = false).map { it.packet.id })
        assertEquals(listOf("forever"), stack.update(now = 5600, paused = false).map { it.packet.id })
    }

    /**
     * Verifies that the stack is laid out at the bottom right, newest at the bottom, and that only
     * the newest toasts are shown.
     */
    @Test
    fun `layout stacks the newest at the bottom right`() {
        val stack = ToastStack()
        repeat(5) { stack.show(ToastShow("t$it", title = "Toast $it"), now = it.toLong()) }
        val window = Rect(0, 0, 400, 300)

        val placed = ToastLayout.place(measurer, stack.update(now = 10, paused = false), window, expanded = true)

        assertEquals(ToastLayout.VISIBLE, placed.size)
        assertEquals("t4", placed.last().entry.packet.id)
        assertEquals(window.right - ToastLayout.MARGIN, placed.last().area.right)
        assertEquals(window.bottom - ToastLayout.MARGIN, placed.last().area.bottom)
        assertTrue(placed[0].area.bottom <= placed[1].area.y)
    }

    /**
     * Verifies that a click on a toast's action button reports the action and removes the toast,
     * and that the close button removes it without a report.
     */
    @Test
    fun `buttons report and close`() {
        val stack = ToastStack()
        stack.show(ToastShow("call", title = "Anruf", actionLabel = "Annehmen", cancelLabel = "Ablehnen", closeButton = true), now = 0)
        stack.show(ToastShow("note", title = "Notiz", closeButton = true), now = 0)
        val window = Rect(0, 0, 400, 300)
        val sent = mutableListOf<Pair<String, ToastButtonKind>>()

        val call = ToastLayout.place(measurer, stack.update(0, false), window, expanded = true).first { it.entry.packet.id == "call" }
        val action = call.action!!
        assertTrue(stack.click(measurer, window, action.x + 1.0, action.y + 1.0) { id, button -> sent += id to button })
        assertEquals(listOf("call" to ToastButtonKind.ACTION), sent)

        val note = ToastLayout.place(measurer, stack.update(0, false), window, expanded = true).single()
        val close = note.close!!
        assertTrue(stack.click(measurer, window, close.x + 1.0, close.y + 1.0) { id, button -> sent += id to button })
        assertEquals(1, sent.size)
        assertTrue(stack.update(0, false).isEmpty())
    }

    /**
     * Verifies that a click beside the toasts is not taken.
     */
    @Test
    fun `clicks beside the toasts pass`() {
        val stack = ToastStack()
        stack.show(ToastShow("a", title = "A"), now = 0)

        assertTrue(!stack.click(measurer, Rect(0, 0, 400, 300), 5.0, 5.0) { _, _ -> })
    }

    /**
     * Verifies when the HUD cursor releases and captures the mouse.
     */
    @Test
    fun `hud cursor follows the key`() {
        assertEquals(HudCursor.Change.RELEASE, HudCursor.change(keyDown = true, keyPressed = true, screenOpen = false, active = false, mode = KeyMode.HOLD))
        assertNull(HudCursor.change(keyDown = true, keyPressed = true, screenOpen = false, active = true, mode = KeyMode.HOLD))
        assertEquals(HudCursor.Change.CAPTURE, HudCursor.change(keyDown = false, keyPressed = false, screenOpen = false, active = true, mode = KeyMode.HOLD))
        assertEquals(HudCursor.Change.END, HudCursor.change(keyDown = true, keyPressed = true, screenOpen = true, active = true, mode = KeyMode.HOLD))
        assertNull(HudCursor.change(keyDown = true, keyPressed = true, screenOpen = true, active = false, mode = KeyMode.HOLD))
    }
}
