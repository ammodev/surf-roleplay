package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.AccordionType
import dev.slne.surf.roleplay.api.client.common.screen.ScreenInputChange as ApiInputChange
import dev.slne.surf.roleplay.api.client.common.screen.accordion
import dev.slne.surf.roleplay.api.client.common.screen.accordionContent
import dev.slne.surf.roleplay.api.client.common.screen.accordionItem
import dev.slne.surf.roleplay.api.client.common.screen.accordionTrigger
import dev.slne.surf.roleplay.api.client.common.screen.collapsible
import dev.slne.surf.roleplay.api.client.common.screen.collapsibleContent
import dev.slne.surf.roleplay.api.client.common.screen.collapsibleTrigger
import dev.slne.surf.roleplay.api.client.common.screen.screen
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.AccordionContentNode
import dev.slne.surf.roleplay.protocol.screen.AccordionItemNode
import dev.slne.surf.roleplay.protocol.screen.AccordionNode
import dev.slne.surf.roleplay.protocol.screen.AccordionTriggerNode
import dev.slne.surf.roleplay.protocol.screen.CollapsibleContentNode
import dev.slne.surf.roleplay.protocol.screen.CollapsibleNode
import dev.slne.surf.roleplay.protocol.screen.CollapsibleTriggerNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.InputValue
import dev.slne.surf.roleplay.protocol.screen.ScreenInputChange
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import dev.slne.surf.roleplay.protocol.screen.WidgetScreenBody
import net.kyori.adventure.text.Component
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import dev.slne.surf.roleplay.protocol.screen.AccordionType as NodeAccordionType

/**
 * Tests for the navigation and layout components in the API and on Paper.
 */
class NavigationComponentsTest {

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
     * The changes and clicks the screen reported.
     */
    private val reports = mutableListOf<String>()

    /**
     * Records a change.
     *
     * @param change the change
     */
    private fun report(change: ApiInputChange) {
        reports += "${change.inputId}=${change.value}"
    }

    /**
     * Opens a screen with a collapsible, a single accordion with a disabled item and a multiple
     * accordion.
     *
     * @return the session id
     */
    private fun open(): Int = state.open(
        screen(Component.text("Navigation")) {
            column("root") {
                collapsible("collapsible", onChange = { report(it) }) {
                    collapsibleTrigger("trigger") { button("toggle", Component.text("Mehr"), submitsInput = false) }
                    collapsibleContent("content") { label("hidden", Component.text("Versteckt")) }
                }
                accordion("single", collapsible = true, value = listOf("a"), onChange = { report(it) }) {
                    for (v in listOf("a", "b", "c")) {
                        accordionItem("single_$v", v, enabled = v != "c") {
                            accordionTrigger("single_trigger_$v", Component.text(v))
                            accordionContent("single_content_$v") { label("single_text_$v", Component.text("Text")) }
                        }
                    }
                }
                accordion("multiple", type = AccordionType.MULTIPLE) {
                    for (v in listOf("a", "b")) {
                        accordionItem("multiple_$v", v) {
                            accordionTrigger("multiple_trigger_$v", Component.text(v))
                            accordionContent("multiple_content_$v") {}
                        }
                    }
                }
                button("save", Component.text("Speichern")) { reports += "save=${it.values.list("multiple")}" }
            }
        },
        null,
    ).sessionId

    /**
     * Verifies that collapsibles and accordions map to their nodes with their settings.
     */
    @Test
    fun `disclosures map to their nodes`() {
        open()

        val root = assertIs<ColumnNode>(assertIs<WidgetScreenBody>((sent.last() as ScreenOpen).body).root)
        val collapsible = assertIs<CollapsibleNode>(root.children[0])
        assertTrue(collapsible.notifyChange)
        assertIs<CollapsibleTriggerNode>(collapsible.children[0])
        assertIs<CollapsibleContentNode>(collapsible.children[1])
        val single = assertIs<AccordionNode>(root.children[1])
        assertEquals(NodeAccordionType.SINGLE, single.type)
        assertTrue(single.collapsible)
        assertEquals(listOf("a"), single.value)
        val item = assertIs<AccordionItemNode>(single.children[2])
        assertEquals("c", item.value)
        assertEquals(false, item.enabled)
        assertTrue(assertIs<AccordionTriggerNode>(item.children[0]).text.contains("\"c\""))
        assertIs<AccordionContentNode>(item.children[1])
        assertEquals(NodeAccordionType.MULTIPLE, assertIs<AccordionNode>(root.children[2]).type)
    }

    /**
     * Verifies that a collapsible reports its open state and rejects anything but true or false.
     */
    @Test
    fun `collapsible open state is validated`() {
        val session = open()

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChange(session, "collapsible", "true")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChange(session, "collapsible", "maybe")))
        assertEquals(listOf("collapsible=true"), reports)
    }

    /**
     * Verifies that a single accordion accepts at most one known, enabled item.
     */
    @Test
    fun `single accordion values are validated`() {
        val session = open()

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChange(session, "single", "b")))
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChange(session, "single", "")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChange(session, "single", "a,b")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChange(session, "single", "z")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChange(session, "single", "c")))
        assertEquals(listOf("single=b", "single="), reports)
    }

    /**
     * Verifies that a multiple accordion submits its open items with an action.
     */
    @Test
    fun `multiple accordion submits its open items`() {
        val session = open()

        val outcome = state.handleWidgetAction(ScreenWidgetAction(session, "save", listOf(InputValue("multiple", "a,b"))))

        assertIs<PlayerScreenState.Outcome.Accepted>(outcome)
        assertEquals(listOf("save=[a, b]"), reports)
    }
}
