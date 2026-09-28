package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.Orientation
import dev.slne.surf.roleplay.api.client.common.screen.ScreenClick
import dev.slne.surf.roleplay.api.client.common.screen.field
import dev.slne.surf.roleplay.api.client.common.screen.fieldContent
import dev.slne.surf.roleplay.api.client.common.screen.fieldDescription
import dev.slne.surf.roleplay.api.client.common.screen.fieldError
import dev.slne.surf.roleplay.api.client.common.screen.fieldGroup
import dev.slne.surf.roleplay.api.client.common.screen.fieldLabel
import dev.slne.surf.roleplay.api.client.common.screen.fieldLegend
import dev.slne.surf.roleplay.api.client.common.screen.fieldSeparator
import dev.slne.surf.roleplay.api.client.common.screen.fieldSet
import dev.slne.surf.roleplay.api.client.common.screen.form
import dev.slne.surf.roleplay.api.client.common.screen.screen
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.FieldContentNode
import dev.slne.surf.roleplay.protocol.screen.FieldGroupNode
import dev.slne.surf.roleplay.protocol.screen.FieldNode
import dev.slne.surf.roleplay.protocol.screen.FieldSeparatorNode
import dev.slne.surf.roleplay.protocol.screen.FieldSetNode
import dev.slne.surf.roleplay.protocol.screen.FieldTextKind
import dev.slne.surf.roleplay.protocol.screen.FieldTextNode
import dev.slne.surf.roleplay.protocol.screen.FormNode
import dev.slne.surf.roleplay.protocol.screen.InputValue
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenPatch
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import dev.slne.surf.roleplay.protocol.screen.SetInvalid
import dev.slne.surf.roleplay.protocol.screen.SetText
import dev.slne.surf.roleplay.protocol.screen.WidgetScreenBody
import net.kyori.adventure.text.Component
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import dev.slne.surf.roleplay.protocol.screen.Orientation as NodeOrientation

/**
 * Tests for fields, forms and server-side errors in the API and on Paper.
 */
class FieldFormTest {

    /**
     * The packets sent to the client.
     */
    private val sent = mutableListOf<Packet>()

    /**
     * The state under test.
     */
    private val state = PlayerScreenState(
        UUID.randomUUID(),
        object : ScreenPacketSender {
            /**
             * Records a packet.
             *
             * @param type the packet type
             * @param packet the packet
             */
            override fun <P : Packet> send(type: PacketType<P>, packet: P) {
                sent += packet
            }
        },
        ActionRateLimiter(100),
    )

    /**
     * Opens a form with two fields, whose submit handler rejects a taken name.
     *
     * @return the session id
     */
    private fun open(): Int = state.open(
        screen(Component.text("Konto")) {
            form("form", submitId = "send") {
                fieldSet("set") {
                    fieldLegend("legend", Component.text("Konto"))
                    fieldGroup("group") {
                        field("name_field") {
                            fieldLabel("name_label", Component.text("Name"), forId = "name")
                            textInput("name")
                            fieldDescription("name_hint", Component.text("Dein Rufname"))
                            fieldError("name_error")
                        }
                        fieldSeparator("sep", Component.text("oder"))
                        field("mail_field", orientation = Orientation.HORIZONTAL) {
                            fieldContent("mail_content") { fieldLabel("mail_label", Component.text("E-Mail"), forId = "mail") }
                            textInput("mail")
                            fieldError("mail_error")
                        }
                    }
                }
                button("send", Component.text("Senden")) { click: ScreenClick ->
                    if (click.values.text("name") == "Max") click.fail(mapOf("name" to Component.text("Name ist vergeben")))
                }
            }
        },
        null,
    ).sessionId

    /**
     * Verifies that forms and fields map to their nodes.
     */
    @Test
    fun `forms map to their nodes`() {
        open()

        val form = assertIs<FormNode>(assertIs<WidgetScreenBody>((sent.last() as ScreenOpen).body).root)
        assertEquals("send", form.submitId)
        val set = assertIs<FieldSetNode>(form.children[0])
        assertEquals(FieldTextKind.LEGEND, assertIs<FieldTextNode>(set.children[0]).kind)
        val group = assertIs<FieldGroupNode>(set.children[1])
        val name = assertIs<FieldNode>(group.children[0])
        assertEquals("name", assertIs<FieldTextNode>(name.children[0]).forId)
        assertEquals(FieldTextKind.DESCRIPTION, assertIs<FieldTextNode>(name.children[2]).kind)
        assertIs<FieldSeparatorNode>(group.children[1])
        val mail = assertIs<FieldNode>(group.children[2])
        assertEquals(NodeOrientation.HORIZONTAL, mail.orientation)
        assertIs<FieldContentNode>(mail.children[0])
    }

    /**
     * Verifies that failing a click fills the field's error text and marks the field and its
     * input invalid, while clearing every other field.
     */
    @Test
    fun `failed clicks show field errors`() {
        val session = open()

        state.handleWidgetAction(ScreenWidgetAction(session, "send", listOf(InputValue("name", "Max"), InputValue("mail", ""))))

        val operations = assertIs<ScreenPatch>(sent.last()).operations
        val nameError = operations.filterIsInstance<SetText>().single { it.targetId == "name_error" }
        assertEquals(ScreenMapper.text(Component.text("Name ist vergeben")), nameError.text)
        assertEquals(ScreenMapper.text(Component.empty()), operations.filterIsInstance<SetText>().single { it.targetId == "mail_error" }.text)
        val invalid = operations.filterIsInstance<SetInvalid>().associate { it.targetId to it.invalid }
        assertEquals(mapOf("name_field" to true, "mail_field" to false, "name" to true, "mail" to false), invalid)
    }
}
