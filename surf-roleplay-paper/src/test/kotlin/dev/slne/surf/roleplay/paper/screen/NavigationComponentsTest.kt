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
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.ScrollOrientation
import dev.slne.surf.roleplay.api.client.common.screen.scrollArea
import dev.slne.surf.roleplay.api.client.common.screen.breadcrumb
import dev.slne.surf.roleplay.api.client.common.screen.breadcrumbList
import dev.slne.surf.roleplay.api.client.common.screen.breadcrumbItem
import dev.slne.surf.roleplay.api.client.common.screen.breadcrumbLink
import dev.slne.surf.roleplay.api.client.common.screen.breadcrumbPage
import dev.slne.surf.roleplay.api.client.common.screen.breadcrumbSeparator
import dev.slne.surf.roleplay.api.client.common.screen.breadcrumbEllipsis
import dev.slne.surf.roleplay.api.client.common.screen.pagination
import dev.slne.surf.roleplay.api.client.common.screen.paginationContent
import dev.slne.surf.roleplay.api.client.common.screen.paginationItem
import dev.slne.surf.roleplay.api.client.common.screen.paginationLink
import dev.slne.surf.roleplay.api.client.common.screen.paginationPrevious
import dev.slne.surf.roleplay.api.client.common.screen.paginationNext
import dev.slne.surf.roleplay.api.client.common.screen.paginationEllipsis
import dev.slne.surf.roleplay.api.client.common.screen.Orientation
import dev.slne.surf.roleplay.api.client.common.screen.TabsVariant
import dev.slne.surf.roleplay.api.client.common.screen.tabs
import dev.slne.surf.roleplay.api.client.common.screen.tabsContent
import dev.slne.surf.roleplay.api.client.common.screen.tabsList
import dev.slne.surf.roleplay.api.client.common.screen.tabsTrigger
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
import dev.slne.surf.roleplay.protocol.screen.ScrollAreaNode
import dev.slne.surf.roleplay.protocol.screen.ScrollOrientation as NodeScrollOrientation
import dev.slne.surf.roleplay.protocol.screen.BreadcrumbNode
import dev.slne.surf.roleplay.protocol.screen.BreadcrumbListNode
import dev.slne.surf.roleplay.protocol.screen.BreadcrumbItemNode
import dev.slne.surf.roleplay.protocol.screen.BreadcrumbLinkNode
import dev.slne.surf.roleplay.protocol.screen.BreadcrumbSeparatorNode
import dev.slne.surf.roleplay.protocol.screen.PaginationNode
import dev.slne.surf.roleplay.protocol.screen.PaginationContentNode
import dev.slne.surf.roleplay.protocol.screen.PaginationItemNode
import dev.slne.surf.roleplay.protocol.screen.PaginationLinkNode
import dev.slne.surf.roleplay.protocol.screen.SizeMode
import dev.slne.surf.roleplay.protocol.screen.TabsContentNode
import dev.slne.surf.roleplay.protocol.screen.TabsListNode
import dev.slne.surf.roleplay.protocol.screen.TabsNode
import dev.slne.surf.roleplay.protocol.screen.TabsTriggerNode
import dev.slne.surf.roleplay.protocol.screen.Orientation as NodeOrientation
import dev.slne.surf.roleplay.protocol.screen.TabsVariant as NodeTabsVariant
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

    /**
     * Opens a screen with vertical line tabs of three tabs, the second one disabled.
     *
     * @return the session id
     */
    private fun openTabs(): Int = state.open(
        screen(Component.text("Tabs")) {
            tabs("tabs", value = "account", orientation = Orientation.VERTICAL, onChange = { report(it) }) {
                tabsList("list", TabsVariant.LINE) {
                    tabsTrigger("trigger_account", "account", Component.text("Konto"), icon = "user")
                    tabsTrigger("trigger_password", "password", Component.text("Passwort"), enabled = false)
                    tabsTrigger("trigger_billing", "billing", Component.text("Rechnung"))
                }
                tabsContent("content_account", "account") {}
                tabsContent("content_password", "password") {}
                tabsContent("content_billing", "billing") {}
            }
        },
        null,
    ).sessionId

    /**
     * Verifies that tabs map to their nodes with their settings.
     */
    @Test
    fun `tabs map to their nodes`() {
        openTabs()

        val tabs = assertIs<TabsNode>(assertIs<WidgetScreenBody>((sent.last() as ScreenOpen).body).root)
        assertEquals("account", tabs.value)
        assertEquals(NodeOrientation.VERTICAL, tabs.orientation)
        assertTrue(tabs.notifyChange)
        val list = assertIs<TabsListNode>(tabs.children[0])
        assertEquals(NodeTabsVariant.LINE, list.variant)
        val trigger = assertIs<TabsTriggerNode>(list.children[0])
        assertEquals("account", trigger.value)
        assertEquals("user", trigger.icon)
        assertEquals(false, assertIs<TabsTriggerNode>(list.children[1]).enabled)
        assertEquals("billing", assertIs<TabsContentNode>(tabs.children[3]).value)
    }

    /**
     * Verifies that tabs accept only the value of a known, enabled tab.
     */
    @Test
    fun `tab values are validated`() {
        val session = openTabs()

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChange(session, "tabs", "billing")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChange(session, "tabs", "password")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChange(session, "tabs", "unknown")))
        assertEquals(listOf("tabs=billing"), reports)
    }

    /**
     * Opens a screen with a breadcrumb and a pagination whose links report their clicks.
     *
     * @return the session id
     */
    private fun openPaths(): Int = state.open(
        screen(Component.text("Pfade")) {
            column("root") {
                breadcrumb("breadcrumb") {
                    breadcrumbList("list") {
                        breadcrumbItem("home_item") { breadcrumbLink("home", Component.text("Start")) { reports += it.buttonId } }
                        breadcrumbSeparator("sep")
                        breadcrumbItem("more_item") { breadcrumbEllipsis("ellipsis") }
                        breadcrumbSeparator("slash", icon = "slash")
                        breadcrumbItem("page_item") { breadcrumbPage("page", Component.text("Akte")) }
                    }
                }
                pagination("pagination") {
                    paginationContent("content") {
                        paginationItem("i1") { paginationPrevious("prev", enabled = false) { reports += it.buttonId } }
                        paginationItem("i2") { paginationLink("p1", Component.text("1"), active = true) { reports += it.buttonId } }
                        paginationItem("i3") { paginationEllipsis("gap") }
                        paginationItem("i4") { paginationNext("next") { reports += it.buttonId } }
                    }
                }
            }
        },
        null,
    ).sessionId

    /**
     * Verifies that breadcrumbs and paginations map to their nodes, with German default texts
     * and a pagination spanning the available width.
     */
    @Test
    fun `paths map to their nodes`() {
        openPaths()

        val root = assertIs<ColumnNode>(assertIs<WidgetScreenBody>((sent.last() as ScreenOpen).body).root)
        val list = assertIs<BreadcrumbListNode>(assertIs<BreadcrumbNode>(root.children[0]).children.single())
        assertTrue(assertIs<BreadcrumbLinkNode>(assertIs<BreadcrumbItemNode>(list.children[0]).children.single()).text.contains("Start"))
        assertEquals("chevron-right", assertIs<BreadcrumbSeparatorNode>(list.children[1]).icon)
        assertEquals("slash", assertIs<BreadcrumbSeparatorNode>(list.children[3]).icon)
        val pagination = assertIs<PaginationNode>(root.children[1])
        assertEquals(SizeMode.GROW, pagination.width.mode)
        val content = assertIs<PaginationContentNode>(pagination.children.single())
        assertTrue(assertIs<PaginationLinkNode>(assertIs<PaginationItemNode>(content.children[1]).children.single()).active)
        assertTrue((assertIs<PaginationItemNode>(content.children[0]).children.single() as dev.slne.surf.roleplay.protocol.screen.PaginationPreviousNode).text.contains("Zurück"))
        assertTrue((assertIs<PaginationItemNode>(content.children[3]).children.single() as dev.slne.surf.roleplay.protocol.screen.PaginationNextNode).text.contains("Weiter"))
    }

    /**
     * Verifies that link clicks run their handlers, that disabled links and pages are rejected,
     * and that an ellipsis is accepted without a handler.
     */
    @Test
    fun `path links act`() {
        val session = openPaths()

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "home")))
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "p1")))
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "next")))
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "ellipsis")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleWidgetAction(ScreenWidgetAction(session, "prev")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleWidgetAction(ScreenWidgetAction(session, "page")))
        assertEquals(listOf("home", "p1", "next"), reports)
    }

    /**
     * Verifies that a scroll area maps to its node with its size and orientation.
     */
    @Test
    fun `scroll areas map to their nodes`() {
        state.open(
            screen(Component.text("Scroll")) {
                scrollArea("area", ElementSize.fixed(120), ElementSize.fixed(80), ScrollOrientation.BOTH) { label("text", Component.text("Lang")) }
            },
            null,
        )

        val area = assertIs<ScrollAreaNode>(assertIs<WidgetScreenBody>((sent.last() as ScreenOpen).body).root)
        assertEquals(NodeScrollOrientation.BOTH, area.orientation)
        assertEquals(120, area.width.value)
        assertEquals(80, area.height.value)
        assertEquals(1, area.children.size)
    }
}
