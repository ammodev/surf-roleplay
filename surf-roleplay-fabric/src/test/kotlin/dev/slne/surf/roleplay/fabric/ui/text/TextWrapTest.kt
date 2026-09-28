package dev.slne.surf.roleplay.fabric.ui.text

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.layout.FlexLayout
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.widget.FieldTextWidget
import dev.slne.surf.roleplay.fabric.ui.widget.LabelWidget
import dev.slne.surf.roleplay.protocol.screen.FieldTextKind
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [TextWrap], [TextBlock] and wrapping labels.
 */
class TextWrapTest {

    /**
     * A measurer in which every character is 5 pixels wide.
     */
    private val measurer = object : TextMeasurer {
        override val lineHeight: Int = 9
        override fun width(json: String): Int = json.length * 5
        override fun plainWidth(text: String): Int = text.length * 5
    }

    /**
     * Measures a plain string at 5 pixels per character.
     *
     * @param text the string
     * @return its width
     */
    private fun width(text: String): Int = text.length * 5

    /**
     * Verifies that words are placed on a line while they fit.
     */
    @Test
    fun `greedy wrap breaks at spaces`() {
        assertEquals(listOf("aa bb", "cc dd", "ee"), TextWrap.lines("aa bb cc dd ee", 25, ::width))
    }

    /**
     * Verifies that a text that fits stays on one line.
     */
    @Test
    fun `fitting text stays on one line`() {
        assertEquals(listOf("aa bb cc"), TextWrap.lines("aa bb cc", 100, ::width))
    }

    /**
     * Verifies that a word wider than the line is broken between characters.
     */
    @Test
    fun `long word is broken between characters`() {
        assertEquals(listOf("abcd", "efgh", "ij"), TextWrap.lines("abcdefghij", 20, ::width))
    }

    /**
     * Verifies that line breaks in the text start new lines.
     */
    @Test
    fun `line breaks start new lines`() {
        assertEquals(listOf("aa", "bb cc"), TextWrap.lines("aa\nbb cc", 100, ::width))
    }

    /**
     * Verifies the size of a wrapped block: the widest line, and the lines with a gap between
     * them.
     */
    @Test
    fun `block size counts lines with gaps`() {
        assertEquals(Size(25, 3 * 9 + 2 * TextBlock.LINE_GAP), TextBlock.size(measurer, "aa bb cc dd ee", 25))
    }

    /**
     * Verifies that a line limit caps the height of a block.
     */
    @Test
    fun `line limit caps the block`() {
        assertEquals(Size(25, 2 * 9 + TextBlock.LINE_GAP), TextBlock.size(measurer, "aa bb cc dd ee", 25, maxLines = 2))
    }

    /**
     * Verifies that a label measured in a narrow container wraps, and keeps one line when it
     * fits.
     */
    @Test
    fun `label wraps to the width it gets`() {
        val label = LabelWidget("label", "aa bb cc dd ee")
        val box = label.createLayout(measurer)

        assertEquals(Size(70, 9), FlexLayout.measure(box))
        assertEquals(Size(25, 3 * 9 + 2 * TextBlock.LINE_GAP), FlexLayout.measure(box, 25))
    }

    /**
     * Verifies that a field description wraps, and that an empty error takes no space at any
     * width.
     */
    @Test
    fun `field texts wrap`() {
        val description = FieldTextWidget("description", FieldTextKind.DESCRIPTION, "aa bb cc dd ee", null)
        val error = FieldTextWidget("error", FieldTextKind.ERROR, "", null)

        assertEquals(Size(25, 3 * 9 + 2 * TextBlock.LINE_GAP), FlexLayout.measure(description.createLayout(measurer), 25))
        assertEquals(Size.ZERO, FlexLayout.measure(error.createLayout(measurer), 25))
    }
}
