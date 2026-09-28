package dev.slne.surf.roleplay.protocol.screen

import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.ProtocolCodec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for input types, textareas, input groups, one-time code inputs and label targets on the
 * wire.
 */
class TextComponentsProtocolTest {

    /**
     * Encodes and decodes a tree inside a screen open.
     *
     * @param root the tree
     * @return the decoded tree
     */
    private fun roundTrip(root: ScreenNode): ScreenNode {
        val open = ScreenOpen(sessionId = 1, title = "{}", body = WidgetScreenBody(root))
        val decoded = ProtocolCodec.decode(Packets.SCREEN_OPEN.channel, ProtocolCodec.encode(Packets.SCREEN_OPEN, open)) as ScreenOpen
        return (decoded.body as WidgetScreenBody).root
    }

    /**
     * Verifies that every text component survives a round trip with all its settings.
     */
    @Test
    fun `text components round-trip`() {
        val root = ColumnNode(
            "root",
            children = listOf(
                LabelNode("name_label", text = "\"Name\"", forId = "name"),
                TextInputNode("name", inputType = TextInputType.EMAIL),
                TextInputNode("secret", inputType = TextInputType.PASSWORD),
                TextareaNode("bio", value = "a\nb", placeholder = "\"Bio\"", rows = 5, maxLength = 200, required = true, enabled = false, notifyChange = true),
                InputGroupNode(
                    "search",
                    children = listOf(
                        InputGroupAddonNode("search_start", align = InputGroupAlign.INLINE_START, children = listOf(IconNode("i", icon = "search"))),
                        TextInputNode("query"),
                        InputGroupAddonNode(
                            "search_end",
                            align = InputGroupAlign.BLOCK_END,
                            children = listOf(InputGroupTextNode("count", text = "\"12\"", icon = "hash"), ButtonNode("go", variant = ButtonVariant.GHOST, size = ButtonSize.XS)),
                        ),
                    ),
                ),
                InputOtpNode("code", value = "12", length = 6, groups = listOf(3, 3), pattern = OtpPattern.ALPHANUMERIC, required = true, enabled = false, notifyChange = true),
            ),
        )

        assertEquals(root, roundTrip(root))
    }

    /**
     * Verifies the defaults of the new fields and nodes.
     */
    @Test
    fun `defaults are plain`() {
        assertEquals(TextInputType.TEXT, TextInputNode("t").inputType)
        assertNull(LabelNode("l").forId)
        assertEquals(3, TextareaNode("a").rows)
        assertEquals(InputGroupAlign.INLINE_START, InputGroupAddonNode("g").align)
        val otp = InputOtpNode("o")
        assertEquals(6, otp.length)
        assertEquals(emptyList(), otp.groups)
        assertEquals(OtpPattern.DIGITS, otp.pattern)
    }
}
