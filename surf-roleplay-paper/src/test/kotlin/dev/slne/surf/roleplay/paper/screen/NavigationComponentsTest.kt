package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.AccordionType
import dev.slne.surf.roleplay.api.client.common.screen.ScreenInputChange as ApiInputChange
import dev.slne.surf.roleplay.api.client.common.screen.SidebarCollapsible
import dev.slne.surf.roleplay.api.client.common.screen.SidebarVariant
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.ScrollOrientation
import dev.slne.surf.roleplay.api.client.common.screen.LayoutDirection
import dev.slne.surf.roleplay.api.client.common.screen.Orientation
import dev.slne.surf.roleplay.api.client.common.screen.TabsVariant
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Accordion
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AccordionContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AccordionItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AccordionTrigger
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Breadcrumb
import dev.slne.surf.roleplay.api.client.common.screen.dsl.BreadcrumbEllipsis
import dev.slne.surf.roleplay.api.client.common.screen.dsl.BreadcrumbItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.BreadcrumbLink
import dev.slne.surf.roleplay.api.client.common.screen.dsl.BreadcrumbList
import dev.slne.surf.roleplay.api.client.common.screen.dsl.BreadcrumbPage
import dev.slne.surf.roleplay.api.client.common.screen.dsl.BreadcrumbSeparator
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Carousel
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CarouselContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CarouselItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CarouselNext
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CarouselPrevious
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Collapsible
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CollapsibleContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CollapsibleTrigger
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Direction
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Label
import dev.slne.surf.roleplay.api.client.common.screen.dsl.NavigationMenu
import dev.slne.surf.roleplay.api.client.common.screen.dsl.NavigationMenuContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.NavigationMenuItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.NavigationMenuLink
import dev.slne.surf.roleplay.api.client.common.screen.dsl.NavigationMenuList
import dev.slne.surf.roleplay.api.client.common.screen.dsl.NavigationMenuTrigger
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Pagination
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PaginationContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PaginationEllipsis
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PaginationItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PaginationLink
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PaginationNext
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PaginationPrevious
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ResizableHandle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ResizablePanel
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ResizablePanelGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Screen
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ScrollArea
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Sidebar
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarFooter
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarGroupAction
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarGroupContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarGroupLabel
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarInput
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarInset
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarMenu
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarMenuAction
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarMenuBadge
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarMenuButton
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarMenuItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarMenuSkeleton
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarMenuSub
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarMenuSubButton
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarMenuSubItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarProvider
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarRail
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarSeparator
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarTrigger
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Tabs
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TabsContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TabsList
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TabsTrigger
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
import dev.slne.surf.roleplay.protocol.screen.SidebarProviderNode
import dev.slne.surf.roleplay.protocol.screen.SidebarNode
import dev.slne.surf.roleplay.protocol.screen.SidebarInsetNode
import dev.slne.surf.roleplay.protocol.screen.SidebarContentNode
import dev.slne.surf.roleplay.protocol.screen.SidebarGroupNode
import dev.slne.surf.roleplay.protocol.screen.SidebarGroupContentNode
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuNode
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuItemNode
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuButtonNode
import dev.slne.surf.roleplay.protocol.screen.TextInputNode
import dev.slne.surf.roleplay.protocol.screen.SeparatorNode
import dev.slne.surf.roleplay.protocol.screen.SidebarCollapsible as NodeSidebarCollapsible
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuButtonSize as NodeSidebarMenuButtonSize
import dev.slne.surf.roleplay.protocol.screen.NavigationMenuNode
import dev.slne.surf.roleplay.protocol.screen.NavigationMenuListNode
import dev.slne.surf.roleplay.protocol.screen.NavigationMenuItemNode
import dev.slne.surf.roleplay.protocol.screen.NavigationMenuTriggerNode
import dev.slne.surf.roleplay.protocol.screen.NavigationMenuContentNode
import dev.slne.surf.roleplay.protocol.screen.NavigationMenuLinkNode
import dev.slne.surf.roleplay.protocol.screen.CarouselNode
import dev.slne.surf.roleplay.protocol.screen.CarouselContentNode
import dev.slne.surf.roleplay.protocol.screen.CarouselItemNode
import dev.slne.surf.roleplay.protocol.screen.CarouselPreviousNode
import dev.slne.surf.roleplay.protocol.screen.CarouselNextNode
import dev.slne.surf.roleplay.protocol.screen.ResizableHandleNode
import dev.slne.surf.roleplay.protocol.screen.ResizablePanelGroupNode
import dev.slne.surf.roleplay.protocol.screen.ResizablePanelNode
import dev.slne.surf.roleplay.protocol.screen.ScrollAreaNode
import dev.slne.surf.roleplay.protocol.screen.DirectionNode
import dev.slne.surf.roleplay.protocol.screen.ScrollOrientation as NodeScrollOrientation
import dev.slne.surf.roleplay.protocol.screen.LayoutDirection as NodeLayoutDirection
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
        Screen(Component.text("Navigation")) {
            Column(id = "root") {
                Collapsible(onChange = { report(it) }, id = "collapsible") {
                    CollapsibleTrigger(id = "trigger") { Button(Component.text("Mehr"), submitsInput = false, id = "toggle") }
                    CollapsibleContent(id = "content") { Label(Component.text("Versteckt"), id = "hidden") }
                }
                Accordion(collapsible = true, value = listOf("a"), onChange = { report(it) }, id = "single") {
                    for (v in listOf("a", "b", "c")) {
                        AccordionItem(v, enabled = v != "c", id = "single_$v") {
                            AccordionTrigger(Component.text(v), id = "single_trigger_$v")
                            AccordionContent(id = "single_content_$v") { Label(Component.text("Text"), id = "single_text_$v") }
                        }
                    }
                }
                Accordion(type = AccordionType.MULTIPLE, id = "multiple") {
                    for (v in listOf("a", "b")) {
                        AccordionItem(v, id = "multiple_$v") {
                            AccordionTrigger(Component.text(v), id = "multiple_trigger_$v")
                            AccordionContent(id = "multiple_content_$v") {}
                        }
                    }
                }
                Button(Component.text("Speichern"), id = "save") { reports += "save=${it.values.list("multiple")}" }
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
        Screen(Component.text("Tabs")) {
            Tabs(value = "account", orientation = Orientation.VERTICAL, onChange = { report(it) }, id = "tabs") {
                TabsList(TabsVariant.LINE, id = "list") {
                    TabsTrigger("account", Component.text("Konto"), icon = "user", id = "trigger_account")
                    TabsTrigger("password", Component.text("Passwort"), enabled = false, id = "trigger_password")
                    TabsTrigger("billing", Component.text("Rechnung"), id = "trigger_billing")
                }
                TabsContent("account", id = "content_account") {}
                TabsContent("password", id = "content_password") {}
                TabsContent("billing", id = "content_billing") {}
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
        Screen(Component.text("Pfade")) {
            Column(id = "root") {
                Breadcrumb(id = "breadcrumb") {
                    BreadcrumbList(id = "list") {
                        BreadcrumbItem(id = "home_item") { BreadcrumbLink(Component.text("Start"), id = "home") { reports += it.buttonId } }
                        BreadcrumbSeparator(id = "sep")
                        BreadcrumbItem(id = "more_item") { BreadcrumbEllipsis(id = "ellipsis") }
                        BreadcrumbSeparator(icon = "slash", id = "slash")
                        BreadcrumbItem(id = "page_item") { BreadcrumbPage(Component.text("Akte"), id = "page") }
                    }
                }
                Pagination(id = "pagination") {
                    PaginationContent(id = "content") {
                        PaginationItem(id = "i1") { PaginationPrevious(enabled = false, id = "prev") { reports += it.buttonId } }
                        PaginationItem(id = "i2") { PaginationLink(Component.text("1"), active = true, id = "p1") { reports += it.buttonId } }
                        PaginationItem(id = "i3") { PaginationEllipsis(id = "gap") }
                        PaginationItem(id = "i4") { PaginationNext(id = "next") { reports += it.buttonId } }
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
            Screen(Component.text("Scroll")) {
                ScrollArea(ElementSize.fixed(120), ElementSize.fixed(80), ScrollOrientation.BOTH, id = "area") { Label(Component.text("Lang"), id = "text") }
            },
            null,
        )

        val area = assertIs<ScrollAreaNode>(assertIs<WidgetScreenBody>((sent.last() as ScreenOpen).body).root)
        assertEquals(NodeScrollOrientation.BOTH, area.orientation)
        assertEquals(120, area.width.value)
        assertEquals(80, area.height.value)
        assertEquals(1, area.children.size)
    }

    /**
     * Verifies that nested directions map to their nodes with their directions.
     */
    @Test
    fun `directions map to their nodes`() {
        state.open(
            Screen(Component.text("Richtung")) {
                Direction(LayoutDirection.RTL, width = ElementSize.fixed(200), id = "rtl") {
                    Label(Component.text("Rechts"), id = "text")
                    Direction(LayoutDirection.LTR, id = "ltr") { Label(Component.text("Links"), id = "inner") }
                }
            },
            null,
        )

        val rtl = assertIs<DirectionNode>(assertIs<WidgetScreenBody>((sent.last() as ScreenOpen).body).root)
        assertEquals(NodeLayoutDirection.RTL, rtl.direction)
        assertEquals(200, rtl.width.value)
        val ltr = assertIs<DirectionNode>(rtl.children[1])
        assertEquals(NodeLayoutDirection.LTR, ltr.direction)
        assertEquals(1, ltr.children.size)
    }

    /**
     * Opens a screen with a vertical resizable group of a panel of 30 percent (between 20 and 60)
     * and a panel that takes the rest.
     *
     * @return the session id
     */
    private fun openResizable(): Int = state.open(
        Screen(Component.text("Resizable")) {
            ResizablePanelGroup(Orientation.VERTICAL, onChange = { report(it) }, id = "group") {
                ResizablePanel(defaultSize = 30.0, minSize = 20.0, maxSize = 60.0, id = "top") {}
                ResizableHandle(withHandle = true, id = "handle")
                ResizablePanel(id = "bottom") {}
            }
        },
        null,
    ).sessionId

    /**
     * Verifies that a resizable group maps to its nodes with its limits.
     */
    @Test
    fun `resizable groups map to their nodes`() {
        openResizable()

        val group = assertIs<ResizablePanelGroupNode>(assertIs<WidgetScreenBody>((sent.last() as ScreenOpen).body).root)
        assertEquals(NodeOrientation.VERTICAL, group.orientation)
        assertTrue(group.notifyChange)
        val top = assertIs<ResizablePanelNode>(group.children[0])
        assertEquals(30.0, top.defaultSize)
        assertEquals(20.0, top.minSize)
        assertEquals(60.0, top.maxSize)
        assertTrue(assertIs<ResizableHandleNode>(group.children[1]).withHandle)
    }

    /**
     * Verifies that the sizes must name every panel, stay inside the limits and sum to 100.
     */
    @Test
    fun `resizable sizes are validated`() {
        val session = openResizable()

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChange(session, "group", "45.5,54.5")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChange(session, "group", "70,30")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChange(session, "group", "30,30")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChange(session, "group", "100")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChange(session, "group", "a,b")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChange(session, "group", "NaN,NaN")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChange(session, "group", "30,NaN")))
        assertEquals(listOf("group=45.5,54.5"), reports)
    }

    /**
     * Opens a screen with a looping carousel of three slides of half the content each.
     *
     * @return the session id
     */
    private fun openCarousel(): Int = state.open(
        Screen(Component.text("Karussell")) {
            Carousel(ElementSize.fixed(160), loop = true, onChange = { report(it) }, id = "carousel") {
                CarouselContent(id = "content") {
                    for (i in 0 until 3) CarouselItem(basis = 50.0, id = "slide$i") { Label(Component.text("$i"), id = "text$i") }
                }
                CarouselPrevious(id = "prev")
                CarouselNext(id = "next")
            }
        },
        null,
    ).sessionId

    /**
     * Verifies that a carousel maps to its nodes with its settings.
     */
    @Test
    fun `carousels map to their nodes`() {
        openCarousel()

        val carousel = assertIs<CarouselNode>(assertIs<WidgetScreenBody>((sent.last() as ScreenOpen).body).root)
        assertTrue(carousel.loop)
        assertTrue(carousel.notifyChange)
        assertEquals(160, carousel.width.value)
        assertEquals(50.0, assertIs<CarouselItemNode>(assertIs<CarouselContentNode>(carousel.children[0]).children[0]).basis)
        assertIs<CarouselPreviousNode>(carousel.children[1])
        assertIs<CarouselNextNode>(carousel.children[2])
    }

    /**
     * Verifies that the index of a carousel must name a slide.
     */
    @Test
    fun `carousel indexes are validated`() {
        val session = openCarousel()

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChange(session, "carousel", "2")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChange(session, "carousel", "3")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChange(session, "carousel", "-1")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChange(session, "carousel", "x")))
        assertEquals(listOf("carousel=2"), reports)
    }

    /**
     * Opens a screen with a navigation menu of an item with content, an item whose trigger is
     * disabled, and a link item.
     *
     * @return the session id
     */
    private fun openNavigation(): Int = state.open(
        Screen(Component.text("Navigation")) {
            NavigationMenu(id = "nav") {
                NavigationMenuList(id = "list") {
                    NavigationMenuItem(onChange = { report(it) }, id = "services") {
                        NavigationMenuTrigger(Component.text("Dienste"), id = "services_trigger")
                        NavigationMenuContent(id = "services_content") {
                            NavigationMenuLink(active = true, onClick = { reports += it.buttonId }, id = "police") { Label(Component.text("Polizei"), id = "police_title") }
                        }
                    }
                    NavigationMenuItem(id = "locked") {
                        NavigationMenuTrigger(Component.text("Gesperrt"), enabled = false, id = "locked_trigger")
                        NavigationMenuContent(id = "locked_content") {
                            NavigationMenuLink(onClick = { reports += it.buttonId }, id = "secret") { Label(Component.text("Geheim"), id = "secret_title") }
                        }
                    }
                    NavigationMenuItem(id = "docs_item") {
                        NavigationMenuLink(onClick = { reports += it.buttonId }, id = "docs") { Label(Component.text("Doku"), id = "docs_title") }
                    }
                }
            }
        },
        null,
    ).sessionId

    /**
     * Verifies that a navigation menu maps to its nodes.
     */
    @Test
    fun `navigation menus map to their nodes`() {
        openNavigation()

        val nav = assertIs<NavigationMenuNode>(assertIs<WidgetScreenBody>((sent.last() as ScreenOpen).body).root)
        val list = assertIs<NavigationMenuListNode>(nav.children.single())
        val item = assertIs<NavigationMenuItemNode>(list.children[0])
        assertTrue(item.notifyChange)
        assertTrue(assertIs<NavigationMenuTriggerNode>(item.children[0]).text.contains("Dienste"))
        val link = assertIs<NavigationMenuLinkNode>(assertIs<NavigationMenuContentNode>(item.children[1]).children.single())
        assertTrue(link.active)
        assertIs<NavigationMenuLinkNode>(assertIs<NavigationMenuItemNode>(list.children[2]).children.single())
    }

    /**
     * Verifies that links fire their handlers, that the open state is reported, and that the
     * content behind a disabled trigger cannot be reached.
     */
    @Test
    fun `navigation menu links act`() {
        val session = openNavigation()

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChange(session, "services", "true")))
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "police")))
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "docs")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleWidgetAction(ScreenWidgetAction(session, "secret")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChange(session, "locked", "true")))
        assertEquals(listOf("services=true", "police", "docs"), reports)
    }

    /**
     * Opens a screen with a sidebar layout whose menu parts report their clicks.
     *
     * @return the session id
     */
    private fun openSidebar(): Int = state.open(
        Screen(Component.text("Sidebar")) {
            SidebarProvider(ElementSize.fixed(300), ElementSize.fixed(160), onChange = { report(it) }, id = "provider") {
                Sidebar(variant = SidebarVariant.FLOATING, collapsible = SidebarCollapsible.ICON, id = "sidebar") {
                    SidebarHeader(id = "header") { SidebarInput(Component.text("Suchen"), id = "search") }
                    SidebarSeparator(id = "sep")
                    SidebarContent(id = "content") {
                        SidebarGroup(id = "group") {
                            SidebarGroupLabel(Component.text("Dienst"), id = "label")
                            SidebarGroupAction(onClick = { reports += it.buttonId }, id = "group_action")
                            SidebarGroupContent(id = "group_content") {
                                SidebarMenu(id = "menu") {
                                    SidebarMenuItem(id = "home_item") {
                                        SidebarMenuButton(Component.text("Start"), icon = "house", size = SidebarMenuButtonSize.LG, active = true, tooltip = Component.text("Start"), id = "home") { reports += it.buttonId }
                                        SidebarMenuAction(id = "home_action") { reports += it.buttonId }
                                        SidebarMenuBadge(Component.text("3"), id = "home_badge")
                                        SidebarMenuSub(id = "sub") {
                                            SidebarMenuSubItem(id = "sub_item") { SidebarMenuSubButton(Component.text("Neu"), id = "sub_button") { reports += it.buttonId } }
                                        }
                                    }
                                    SidebarMenuItem(id = "loading") { SidebarMenuSkeleton(showIcon = true, id = "skeleton") }
                                }
                            }
                        }
                    }
                    SidebarFooter(id = "footer") {}
                    SidebarRail(id = "rail")
                }
                SidebarInset(id = "inset") { SidebarTrigger(id = "trigger") }
            }
        },
        null,
    ).sessionId

    /**
     * Verifies that a sidebar layout maps to its nodes, with the input and separator as their
     * regular nodes.
     */
    @Test
    fun `sidebars map to their nodes`() {
        openSidebar()

        val provider = assertIs<SidebarProviderNode>(assertIs<WidgetScreenBody>((sent.last() as ScreenOpen).body).root)
        assertTrue(provider.open)
        assertTrue(provider.notifyChange)
        val sidebar = assertIs<SidebarNode>(provider.children[0])
        assertEquals(NodeSidebarCollapsible.ICON, sidebar.collapsible)
        assertIs<TextInputNode>(assertIs<dev.slne.surf.roleplay.protocol.screen.SidebarHeaderNode>(sidebar.children[0]).children.single())
        assertIs<SeparatorNode>(sidebar.children[1])
        val content = assertIs<SidebarContentNode>(sidebar.children[2])
        val menu = assertIs<SidebarMenuNode>(assertIs<SidebarGroupContentNode>(assertIs<SidebarGroupNode>(content.children.single()).children[2]).children.single())
        val button = assertIs<SidebarMenuButtonNode>(assertIs<SidebarMenuItemNode>(menu.children[0]).children[0])
        assertEquals(NodeSidebarMenuButtonSize.LG, button.size)
        assertTrue(button.active)
        assertTrue(button.tooltip.contains("Start"))
        assertIs<SidebarInsetNode>(provider.children[1])
    }

    /**
     * Verifies that sidebar buttons and actions run their handlers, and that the expanded state
     * is reported and validated.
     */
    @Test
    fun `sidebar parts act`() {
        val session = openSidebar()

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChange(session, "provider", "false")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChange(session, "provider", "maybe")))
        for (id in listOf("home", "home_action", "group_action", "sub_button")) {
            assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, id)))
        }
        assertEquals(listOf("provider=false", "home", "home_action", "group_action", "sub_button"), reports)
    }
}
