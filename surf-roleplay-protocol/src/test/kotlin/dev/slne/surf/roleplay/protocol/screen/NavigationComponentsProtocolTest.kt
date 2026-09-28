package dev.slne.surf.roleplay.protocol.screen

import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.ProtocolCodec
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the navigation and layout components on the wire.
 */
class NavigationComponentsProtocolTest {

    /**
     * Sends a tree through a screen open and returns the decoded tree.
     *
     * @param root the root of the tree
     * @return the decoded root
     */
    private fun roundTrip(root: ScreenNode): ScreenNode {
        val open = ScreenOpen(sessionId = 1, title = "{}", body = WidgetScreenBody(root))
        val decoded = ProtocolCodec.decode(Packets.SCREEN_OPEN.channel, ProtocolCodec.encode(Packets.SCREEN_OPEN, open)) as ScreenOpen
        return (decoded.body as WidgetScreenBody).root
    }

    /**
     * Verifies that collapsibles and accordions with every part survive a round trip.
     */
    @Test
    fun `collapsibles and accordions round-trip`() {
        val root = ColumnNode(
            "root",
            children = listOf(
                CollapsibleNode(
                    "collapsible",
                    open = true,
                    notifyChange = true,
                    children = listOf(CollapsibleTriggerNode("trigger", children = listOf(ButtonNode("toggle"))), CollapsibleContentNode("content", children = listOf(LabelNode("hidden")))),
                ),
                AccordionNode(
                    "accordion",
                    type = AccordionType.MULTIPLE,
                    collapsible = true,
                    value = listOf("a", "b"),
                    notifyChange = true,
                    children = listOf(
                        AccordionItemNode("item_a", value = "a", enabled = false, children = listOf(AccordionTriggerNode("trigger_a", text = "\"A\""), AccordionContentNode("content_a"))),
                    ),
                ),
            ),
        )

        assertEquals(root, roundTrip(root))
    }

    /**
     * Verifies that tabs with every part survive a round trip.
     */
    @Test
    fun `tabs round-trip`() {
        val root = TabsNode(
            "tabs",
            value = "b",
            orientation = Orientation.VERTICAL,
            notifyChange = true,
            children = listOf(
                TabsListNode("list", variant = TabsVariant.LINE, children = listOf(TabsTriggerNode("trigger_a", value = "a", text = "\"A\"", icon = "user", enabled = false))),
                TabsContentNode("content_a", value = "a", children = listOf(LabelNode("text"))),
            ),
        )

        assertEquals(root, roundTrip(root))
    }

    /**
     * Verifies that breadcrumbs and paginations with every part survive a round trip.
     */
    @Test
    fun `breadcrumbs and paginations round-trip`() {
        val root = ColumnNode(
            "root",
            children = listOf(
                BreadcrumbNode(
                    "breadcrumb",
                    children = listOf(
                        BreadcrumbListNode(
                            "list",
                            children = listOf(
                                BreadcrumbItemNode("home", children = listOf(BreadcrumbLinkNode("home_link", text = "\"Start\"", enabled = false))),
                                BreadcrumbSeparatorNode("sep", icon = "slash"),
                                BreadcrumbItemNode("more", children = listOf(BreadcrumbEllipsisNode("ellipsis"))),
                                BreadcrumbItemNode("page", children = listOf(BreadcrumbPageNode("page_text", text = "\"Akte\""))),
                            ),
                        ),
                    ),
                ),
                PaginationNode(
                    "pagination",
                    children = listOf(
                        PaginationContentNode(
                            "content",
                            children = listOf(
                                PaginationItemNode("prev_item", children = listOf(PaginationPreviousNode("prev", text = "\"Zurück\"", enabled = false))),
                                PaginationItemNode("one_item", children = listOf(PaginationLinkNode("one", text = "\"1\"", active = true))),
                                PaginationItemNode("gap_item", children = listOf(PaginationEllipsisNode("gap"))),
                                PaginationItemNode("next_item", children = listOf(PaginationNextNode("next", text = "\"Weiter\""))),
                            ),
                        ),
                    ),
                ),
            ),
        )

        assertEquals(root, roundTrip(root))
    }

    /**
     * Verifies that a scroll area survives a round trip.
     */
    @Test
    fun `scroll areas round-trip`() {
        val root = ScrollAreaNode("area", width = Sizing.fixed(100), height = Sizing.fixed(60), orientation = ScrollOrientation.BOTH, children = listOf(LabelNode("text")))

        assertEquals(root, roundTrip(root))
    }

    /**
     * Verifies that a resizable panel group survives a round trip.
     */
    @Test
    fun `resizable groups round-trip`() {
        val root = ResizablePanelGroupNode(
            "group",
            orientation = Orientation.VERTICAL,
            notifyChange = true,
            children = listOf(
                ResizablePanelNode("one", defaultSize = 25.0, minSize = 10.0, maxSize = 60.0, children = listOf(LabelNode("a"))),
                ResizableHandleNode("handle", withHandle = true),
                ResizablePanelNode("two"),
            ),
        )

        assertEquals(root, roundTrip(root))
    }

    /**
     * Verifies that a carousel survives a round trip.
     */
    @Test
    fun `carousels round-trip`() {
        val root = CarouselNode(
            "carousel",
            orientation = Orientation.VERTICAL,
            loop = true,
            index = 2,
            notifyChange = true,
            children = listOf(
                CarouselContentNode("content", children = listOf(CarouselItemNode("item", basis = 50.0, children = listOf(LabelNode("a"))))),
                CarouselPreviousNode("prev", enabled = false),
                CarouselNextNode("next"),
            ),
        )

        assertEquals(root, roundTrip(root))
    }

    /**
     * Verifies that a navigation menu with every part survives a round trip.
     */
    @Test
    fun `navigation menus round-trip`() {
        val root = NavigationMenuNode(
            "nav",
            children = listOf(
                NavigationMenuListNode(
                    "list",
                    children = listOf(
                        NavigationMenuItemNode(
                            "item",
                            open = true,
                            notifyChange = true,
                            children = listOf(
                                NavigationMenuTriggerNode("trigger", text = "\"Dienste\"", enabled = false),
                                NavigationMenuContentNode("content", children = listOf(NavigationMenuLinkNode("link", active = true, enabled = false, children = listOf(LabelNode("title"))))),
                            ),
                        ),
                    ),
                ),
            ),
        )

        assertEquals(root, roundTrip(root))
    }

    /**
     * Verifies that a sidebar layout with every part survives a round trip.
     */
    @Test
    fun `sidebars round-trip`() {
        val menu = SidebarMenuNode(
            "menu",
            children = listOf(
                SidebarMenuItemNode(
                    "item",
                    children = listOf(
                        SidebarMenuButtonNode("button", text = "\"Akten\"", icon = "folder", size = SidebarMenuButtonSize.LG, variant = SidebarMenuButtonVariant.OUTLINE, active = true, tooltip = "\"Akten\"", enabled = false),
                        SidebarMenuActionNode("action", icon = "plus", showOnHover = true, enabled = false),
                        SidebarMenuBadgeNode("badge", text = "\"3\""),
                        SidebarMenuSubNode("sub", children = listOf(SidebarMenuSubItemNode("sub_item", children = listOf(SidebarMenuSubButtonNode("sub_button", text = "\"Neu\"", icon = "file", size = SidebarMenuSubButtonSize.SM, active = true, enabled = false))))),
                    ),
                ),
                SidebarMenuItemNode("loading", children = listOf(SidebarMenuSkeletonNode("skeleton", showIcon = true))),
            ),
        )
        val root = SidebarProviderNode(
            "provider",
            open = false,
            notifyChange = true,
            children = listOf(
                SidebarNode(
                    "sidebar",
                    side = SidebarSide.RIGHT,
                    variant = SidebarVariant.INSET,
                    collapsible = SidebarCollapsible.ICON,
                    children = listOf(
                        SidebarHeaderNode("header"),
                        SidebarContentNode(
                            "content",
                            children = listOf(
                                SidebarGroupNode(
                                    "group",
                                    children = listOf(SidebarGroupLabelNode("label", text = "\"Dienst\""), SidebarGroupActionNode("group_action", icon = "plus", enabled = false), SidebarGroupContentNode("group_content", children = listOf(menu))),
                                ),
                            ),
                        ),
                        SidebarFooterNode("footer"),
                        SidebarRailNode("rail"),
                    ),
                ),
                SidebarInsetNode("inset", children = listOf(SidebarTriggerNode("trigger", enabled = false))),
            ),
        )

        assertEquals(root, roundTrip(root))
    }
}
