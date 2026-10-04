package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import net.minecraft.client.input.KeyEvent
import net.minecraft.util.StringUtil
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
     * @param oneWord whether the whole text counts as one word, as in password fields, so that
     *        word moves and word deletion reach the start or end of the text
     * @return what the key did
     */
    fun handle(edit: TextEditState, event: KeyEvent, oneWord: Boolean = false): Result {
        if (event.isSelectAll) {
            edit.selectAll()
            return Result.MOVED
        }
        val word = event.hasControlDownWithQuirk()
        val extend = event.hasShiftDown()
        when (event.key()) {
            GLFW.GLFW_KEY_BACKSPACE -> return changed(
                when {
                    !word -> edit.backspace()
                    oneWord -> edit.deleteTo(0)
                    else -> edit.deleteWordBackward()
                },
            )

            GLFW.GLFW_KEY_DELETE -> return changed(
                when {
                    !word -> edit.delete()
                    oneWord -> edit.deleteTo(edit.text.length)
                    else -> edit.deleteWordForward()
                },
            )

            GLFW.GLFW_KEY_LEFT -> when {
                !word -> edit.moveCursor(-1, extend)
                oneWord -> edit.moveCursorTo(0, extend)
                else -> edit.moveWord(forward = false, extend)
            }

            GLFW.GLFW_KEY_RIGHT -> when {
                !word -> edit.moveCursor(1, extend)
                oneWord -> edit.moveCursorTo(edit.text.length, extend)
                else -> edit.moveWord(forward = true, extend)
            }

            GLFW.GLFW_KEY_HOME -> edit.moveCursorTo(0, extend)
            GLFW.GLFW_KEY_END -> edit.moveCursorTo(edit.text.length, extend)
            else -> return Result.IGNORED
        }
        return Result.MOVED
    }

    /**
     * Applies the clipboard keys to the state: Control+C copies the selection, Control+X cuts
     * it, and Control+V replaces the selection with the clipboard text, cleaned by [cleanPaste]
     * and passed through the field's filter. A secret text is neither copied nor cut.
     *
     * @param edit the state of the field
     * @param event the key event
     * @param context the screen, which holds the clipboard
     * @param secret whether the text must not leave the field, as in password fields
     * @param lineBreak what replaces a pasted line break, or `null` to keep line breaks
     * @return what the key did, [Result.IGNORED] for keys other than the clipboard keys
     */
    fun handleClipboard(edit: TextEditState, event: KeyEvent, context: UiContext, secret: Boolean = false, lineBreak: String? = ""): Result {
        when {
            event.isCopy -> {
                if (!secret && edit.hasSelection) context.clipboard = edit.selectedText
                return Result.MOVED
            }

            event.isCut -> {
                if (secret || !edit.hasSelection) return Result.MOVED
                val selected = edit.selectedText
                if (!edit.backspace()) return Result.MOVED
                context.clipboard = selected
                return Result.CHANGED
            }

            event.isPaste -> return changed(edit.insert(cleanPaste(context.clipboard, lineBreak)))
            else -> return Result.IGNORED
        }
    }

    /**
     * Prepares clipboard text for a field: line breaks of every platform become `\n`, then are
     * replaced in single-line fields, and characters that cannot be typed into a field are
     * removed.
     *
     * @param text the clipboard text
     * @param lineBreak what replaces a line break, or `null` to keep line breaks
     * @return the text to insert
     */
    fun cleanPaste(text: String, lineBreak: String?): String {
        val unified = text.replace("\r\n", "\n").replace('\r', '\n')
        val joined = if (lineBreak == null) unified else unified.replace("\n", lineBreak)
        return StringUtil.filterText(joined, lineBreak == null)
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
