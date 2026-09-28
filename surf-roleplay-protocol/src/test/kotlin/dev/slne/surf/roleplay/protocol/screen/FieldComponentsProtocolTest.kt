package dev.slne.surf.roleplay.protocol.screen

import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.ProtocolCodec
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for fields, forms and the invalid patch on the wire.
 */
class FieldComponentsProtocolTest {

    /**
     * Verifies that a form with every field part survives a round trip.
     */
    @Test
    fun `forms round-trip`() {
        val form = FormNode(
            "form",
            submitId = "send",
            children = listOf(
                FieldSetNode(
                    "set",
                    children = listOf(
                        FieldTextNode("legend", kind = FieldTextKind.LEGEND, text = "\"Konto\""),
                        FieldGroupNode(
                            "group",
                            children = listOf(
                                FieldNode(
                                    "name_field",
                                    orientation = Orientation.HORIZONTAL,
                                    children = listOf(
                                        FieldContentNode("content", children = listOf(FieldTextNode("label", text = "\"Name\"", forId = "name"), FieldTextNode("desc", kind = FieldTextKind.DESCRIPTION))),
                                        TextInputNode("name"),
                                        FieldTextNode("error", kind = FieldTextKind.ERROR),
                                    ),
                                ),
                                FieldSeparatorNode("sep", text = "\"oder\""),
                            ),
                        ),
                    ),
                ),
                ButtonNode("send"),
            ),
        )
        val open = ScreenOpen(sessionId = 1, title = "{}", body = WidgetScreenBody(form))

        val decoded = ProtocolCodec.decode(Packets.SCREEN_OPEN.channel, ProtocolCodec.encode(Packets.SCREEN_OPEN, open)) as ScreenOpen

        assertEquals(form, (decoded.body as WidgetScreenBody).root)
    }

    /**
     * Verifies that the invalid patch survives a round trip.
     */
    @Test
    fun `invalid patches round-trip`() {
        val patch = ScreenPatch(sessionId = 1, operations = listOf(SetInvalid("name", true), SetInvalid("name_field", false)))

        assertEquals(patch, ProtocolCodec.decode(Packets.SCREEN_PATCH.channel, ProtocolCodec.encode(Packets.SCREEN_PATCH, patch)))
    }
}
