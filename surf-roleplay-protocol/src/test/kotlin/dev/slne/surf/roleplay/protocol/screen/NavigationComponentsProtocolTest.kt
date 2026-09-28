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
}
