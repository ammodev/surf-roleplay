package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW

/**
 * Applies the editing keys that all text fields share to a [TextEditState].
 *
 * Control+A selects everything. The arrows move by character and, with Control, by word;
 * Home and End move to the start and end of the text. Shift extends the selection with every
 * move. Backspace and Delete remove the selection or one character and, with Control, one word.
 * On macOS, Command takes the place of Control.
 */
object TextEditKeys {

    /**
     * What a key did to the state.
     */
    enum class Result {
        /**
         * The key has no editing meaning; the widget may use it.
         */
        IGNORED,

        /**
         * The key was used, and the text did not change.
         */
        MOVED,

        /**
         * The key was used, and the text changed.
         */
        CHANGED,
    }

    /**
     * Applies a key to the state.
     *
     * @param edit the state of the field
     * @param event the key event
     * @return what the key did
     */
    fun handle(edit: TextEditState, event: KeyEvent): Result {
        if (event.isSelectAll) {
            edit.selectAll()
            return Result.MOVED
        }
        val word = event.hasControlDownWithQuirk()
        val extend = event.hasShiftDown()
        when (event.key()) {
            GLFW.GLFW_KEY_BACKSPACE -> return changed(if (word) edit.deleteWordBackward() else edit.backspace())
            GLFW.GLFW_KEY_DELETE -> return changed(if (word) edit.deleteWordForward() else edit.delete())
            GLFW.GLFW_KEY_LEFT -> if (word) edit.moveWord(forward = false, extend) else edit.moveCursor(-1, extend)
            GLFW.GLFW_KEY_RIGHT -> if (word) edit.moveWord(forward = true, extend) else edit.moveCursor(1, extend)
            GLFW.GLFW_KEY_HOME -> edit.moveCursorTo(0, extend)
            GLFW.GLFW_KEY_END -> edit.moveCursorTo(edit.text.length, extend)
            else -> return Result.IGNORED
        }
        return Result.MOVED
    }

    /**
     * Turns whether an edit changed the text into a result.
     *
     * @param changed whether the text changed
     * @return [Result.CHANGED] or [Result.MOVED]
     */
    private fun changed(changed: Boolean): Result = if (changed) Result.CHANGED else Result.MOVED
}

/**
 * Highlights the selected part of a drawn piece of text and draws the selected characters over
 * the highlight in the colour that stands on it.
 *
 * @param text the drawn text, such as a field's text or its password mask
 * @param from the index of the first drawn character
 * @param to the index after the last drawn character
 * @param edit the state that holds the selection
 * @param x the x position the drawn piece starts at
 * @param y the y position the text is drawn at
 */
fun UiGraphics.textSelection(text: String, from: Int, to: Int, edit: TextEditState, x: Int, y: Int) {
    val start = maxOf(edit.selectionStart, from)
    val end = minOf(edit.selectionEnd, to)
    if (start >= end) return
    val selected = text.substring(start, end)
    val left = x + plainWidth(text.substring(from, start))
    fill(Rect(left, y - 1, plainWidth(selected), lineHeight + 1), tokens.primary)
    plainText(selected, left, y, tokens.primaryForeground)
}
