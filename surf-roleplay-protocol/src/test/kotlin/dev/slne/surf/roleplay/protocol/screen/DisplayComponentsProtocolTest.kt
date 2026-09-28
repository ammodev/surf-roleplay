package dev.slne.surf.roleplay.protocol.screen

import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.ProtocolCodec
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the display components on the wire.
 */
class DisplayComponentsProtocolTest {

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
     * Verifies that texts of every kind, lists, separators, keys and badges survive a round trip.
     */
    @Test
    fun `texts, separators, keys and badges round-trip`() {
        val root = ColumnNode(
            "root",
            children = TextKind.entries.map { TextNode("text_${it.name}", kind = it, text = "\"Text\"", maxLines = 2, align = Align.CENTER) } + listOf(
                TextListNode("list", items = listOf("\"Eins\"", "\"Zwei\""), ordered = true),
                SeparatorNode("separator", orientation = Orientation.VERTICAL),
                KbdGroupNode("keys", children = listOf(KbdNode("ctrl", text = "\"Strg\""), KbdNode("cmd", icon = "command"))),
            ) + BadgeVariant.entries.map { BadgeNode("badge_${it.name}", text = "\"Neu\"", icon = "check", variant = it) },
        )

        assertEquals(root, roundTrip(root))
    }

    /**
     * Verifies that skeletons, spinners and aspect ratio boxes survive a round trip.
     */
    @Test
    fun `loading components round-trip`() {
        val root = ColumnNode(
            "root",
            children = listOf(
                SkeletonNode("skeleton", width = Sizing.fixed(40), round = true),
                SpinnerNode("spinner", size = 14, color = IconColor.MUTED),
                AspectRatioNode("ratio", ratio = 16f / 9f, children = listOf(ImageNode("image", texture = "minecraft:x"))),
            ),
        )

        assertEquals(root, roundTrip(root))
    }

    /**
     * Verifies that avatars, avatar groups and counts survive a round trip.
     */
    @Test
    fun `avatars round-trip`() {
        val root = AvatarGroupNode(
            "group",
            children = listOf(
                AvatarNode("player", playerId = "069a79f4-44e9-4726-a5be-fca90e38aaf5", fallback = "\"NO\"", size = AvatarSize.LG, badge = true, badgeIcon = "check"),
                AvatarNode("texture", texture = "minecraft:textures/item/apple.png", fallback = "\"AP\"", size = AvatarSize.SM),
                AvatarGroupCountNode("count", text = "\"+3\""),
            ),
        )

        assertEquals(root, roundTrip(root))
    }

    /**
     * Verifies that alerts and cards with every part survive a round trip.
     */
    @Test
    fun `alerts and cards round-trip`() {
        val root = ColumnNode(
            "root",
            children = listOf(
                AlertNode(
                    "alert",
                    variant = AlertVariant.DESTRUCTIVE,
                    icon = "circle-alert",
                    children = listOf(TextNode("title", kind = TextKind.ALERT_TITLE), TextNode("description", kind = TextKind.ALERT_DESCRIPTION)),
                ),
                CardNode(
                    "card",
                    children = listOf(
                        CardHeaderNode("header", children = listOf(TextNode("card_title", kind = TextKind.CARD_TITLE), CardActionNode("action", children = listOf(ButtonNode("more"))))),
                        CardContentNode("content", children = listOf(TextNode("body"))),
                        CardFooterNode("footer", children = listOf(ButtonNode("save"))),
                    ),
                ),
            ),
        )

        assertEquals(root, roundTrip(root))
    }
}
