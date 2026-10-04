package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.AlertVariant
import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.AvatarSize
import dev.slne.surf.roleplay.api.client.common.screen.AvatarSource
import dev.slne.surf.roleplay.api.client.common.screen.BadgeElement
import dev.slne.surf.roleplay.api.client.common.screen.BadgeVariant
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.IconTint
import dev.slne.surf.roleplay.api.client.common.screen.KbdElement
import dev.slne.surf.roleplay.api.client.common.screen.Orientation
import dev.slne.surf.roleplay.api.client.common.screen.TextElement
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Alert
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AlertDescription
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AlertTitle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AspectRatio
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Avatar
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AvatarGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AvatarGroupCount
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Badge
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Card
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CardAction
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CardContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CardDescription
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CardFooter
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CardHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CardTitle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
import dev.slne.surf.roleplay.api.client.common.screen.dsl.H2
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Image
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Kbd
import dev.slne.surf.roleplay.api.client.common.screen.dsl.KbdGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.P
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Screen
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Separator
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Skeleton
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Spinner
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TypographyList
import dev.slne.surf.roleplay.protocol.screen.AlertNode
import dev.slne.surf.roleplay.protocol.screen.AspectRatioNode
import dev.slne.surf.roleplay.protocol.screen.AvatarGroupCountNode
import dev.slne.surf.roleplay.protocol.screen.AvatarGroupNode
import dev.slne.surf.roleplay.protocol.screen.AvatarNode
import dev.slne.surf.roleplay.protocol.screen.BadgeNode
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.CardActionNode
import dev.slne.surf.roleplay.protocol.screen.CardContentNode
import dev.slne.surf.roleplay.protocol.screen.CardFooterNode
import dev.slne.surf.roleplay.protocol.screen.CardHeaderNode
import dev.slne.surf.roleplay.protocol.screen.CardNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.IconColor
import dev.slne.surf.roleplay.protocol.screen.ImageNode
import dev.slne.surf.roleplay.protocol.screen.KbdGroupNode
import dev.slne.surf.roleplay.protocol.screen.KbdNode
import dev.slne.surf.roleplay.protocol.screen.SeparatorNode
import dev.slne.surf.roleplay.protocol.screen.SizeMode
import dev.slne.surf.roleplay.protocol.screen.SkeletonNode
import dev.slne.surf.roleplay.protocol.screen.SpinnerNode
import dev.slne.surf.roleplay.protocol.screen.TextListNode
import dev.slne.surf.roleplay.protocol.screen.TextNode
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import dev.slne.surf.roleplay.protocol.screen.AlertVariant as NodeAlertVariant
import dev.slne.surf.roleplay.protocol.screen.Align as NodeAlign
import dev.slne.surf.roleplay.protocol.screen.AvatarSize as NodeAvatarSize
import dev.slne.surf.roleplay.protocol.screen.BadgeVariant as NodeBadgeVariant
import dev.slne.surf.roleplay.protocol.screen.Orientation as NodeOrientation
import dev.slne.surf.roleplay.protocol.screen.TextKind as NodeTextKind

/**
 * Tests for the display components in the API and on Paper.
 */
class DisplayComponentsTest {

    /**
     * Maps the root of a screen built with the DSL.
     *
     * @param build the builder of the root
     * @return the mapped root node
     */
    private fun map(build: ComponentScope.() -> Unit) =
        ScreenMapper.toNode(Screen(Component.text("Anzeige")) { Column(id = "root") { build() } }.root)

    /**
     * Verifies that texts, lists, separators, keys and badges map to their nodes with all their
     * settings, and that separators grow along their orientation.
     */
    @Test
    fun `texts, separators, keys and badges map to their nodes`() {
        val root = assertIs<ColumnNode>(
            map {
                H2(Component.text("Titel"), maxLines = 1, align = Alignment.CENTER, id = "title")
                TypographyList(listOf(Component.text("Eins"), Component.text("Zwei")), ordered = true, id = "list")
                Separator(id = "line")
                Separator(orientation = Orientation.VERTICAL, id = "upright")
                KbdGroup(id = "keys") {
                    Kbd(Component.text("Strg"), id = "ctrl")
                    Kbd(icon = "command", id = "cmd")
                }
                Badge(Component.text("Neu"), icon = "check", variant = BadgeVariant.DESTRUCTIVE, id = "badge")
            },
        )

        val text = assertIs<TextNode>(root.children[0])
        assertEquals(NodeTextKind.H2, text.kind)
        assertEquals(1, text.maxLines)
        assertEquals(NodeAlign.CENTER, text.align)
        assertEquals(2, assertIs<TextListNode>(root.children[1]).items.size)
        val line = assertIs<SeparatorNode>(root.children[2])
        assertEquals(SizeMode.GROW, line.width.mode)
        val upright = assertIs<SeparatorNode>(root.children[3])
        assertEquals(NodeOrientation.VERTICAL, upright.orientation)
        assertEquals(SizeMode.GROW, upright.height.mode)
        val keys = assertIs<KbdGroupNode>(root.children[4])
        assertEquals("command", assertIs<KbdNode>(keys.children[1]).icon)
        val badge = assertIs<BadgeNode>(root.children[5])
        assertEquals(NodeBadgeVariant.DESTRUCTIVE, badge.variant)
        assertEquals("check", badge.icon)
    }

    /**
     * Verifies that the texts of texts, keys and badges can be changed by patches.
     */
    @Test
    fun `display texts can be patched`() {
        val text = TextElement("t", Component.text("a"))
        val key = KbdElement("k", Component.text("b"))
        val badge = BadgeElement("b", Component.text("c"))

        assertEquals(Component.text("Z"), (ElementRules.rule(text)!!.withText(text, Component.text("Z")) as TextElement).text)
        assertEquals(Component.text("Z"), (ElementRules.rule(key)!!.withText(key, Component.text("Z")) as KbdElement).text)
        assertEquals(Component.text("Z"), (ElementRules.rule(badge)!!.withText(badge, Component.text("Z")) as BadgeElement).text)
    }

    /**
     * Verifies that skeletons, spinners and aspect ratio boxes map to their nodes, and that an
     * aspect ratio box grows by default.
     */
    @Test
    fun `loading components map to their nodes`() {
        val root = assertIs<ColumnNode>(
            map {
                Skeleton(ElementSize.fixed(40), ElementSize.fixed(8), round = true, id = "skeleton")
                Spinner(size = 14, tint = IconTint.MUTED, id = "spinner")
                AspectRatio(16f / 9f, id = "ratio") {
                    Image(Key.key("minecraft", "textures/block/stone.png"), id = "image")
                }
            },
        )

        val skeleton = assertIs<SkeletonNode>(root.children[0])
        assertEquals(40, skeleton.width.value)
        assertEquals(true, skeleton.round)
        val spinner = assertIs<SpinnerNode>(root.children[1])
        assertEquals(14, spinner.size)
        assertEquals(IconColor.MUTED, spinner.color)
        val ratio = assertIs<AspectRatioNode>(root.children[2])
        assertEquals(16f / 9f, ratio.ratio)
        assertEquals(SizeMode.GROW, ratio.width.mode)
        assertIs<ImageNode>(ratio.children.single())
    }

    /**
     * Verifies that avatars map their player, texture and fallback sources, and that groups hold
     * avatars and a count.
     */
    @Test
    fun `avatars map to their nodes`() {
        val player = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5")
        val root = assertIs<ColumnNode>(
            map {
                AvatarGroup(id = "group") {
                    Avatar(Component.text("NO"), AvatarSource.Player(player), AvatarSize.LG, badgeIcon = "check", id = "player")
                    Avatar(Component.text("AP"), AvatarSource.Texture(Key.key("minecraft", "textures/item/apple.png")), AvatarSize.SM, id = "texture")
                    Avatar(Component.text("CN"), id = "fallback")
                    AvatarGroupCount(Component.text("+3"), id = "count")
                }
            },
        )

        val group = assertIs<AvatarGroupNode>(root.children.single())
        val playerNode = assertIs<AvatarNode>(group.children[0])
        assertEquals(player.toString(), playerNode.playerId)
        assertEquals(NodeAvatarSize.LG, playerNode.size)
        assertEquals(true, playerNode.badge)
        assertEquals("check", playerNode.badgeIcon)
        val textureNode = assertIs<AvatarNode>(group.children[1])
        assertEquals("minecraft:textures/item/apple.png", textureNode.texture)
        assertEquals(null, textureNode.playerId)
        val fallbackNode = assertIs<AvatarNode>(group.children[2])
        assertEquals(null, fallbackNode.texture)
        assertIs<AvatarGroupCountNode>(group.children[3])
    }

    /**
     * Verifies that alerts and cards map to their nodes, and that alerts grow by default.
     */
    @Test
    fun `alerts and cards map to their nodes`() {
        val root = assertIs<ColumnNode>(
            map {
                Alert(AlertVariant.DESTRUCTIVE, icon = "circle-alert", id = "alert") {
                    AlertTitle(Component.text("Fehler"), id = "alert_title")
                    AlertDescription(Component.text("Etwas ging schief."), id = "alert_description")
                }
                Card(id = "card") {
                    CardHeader(id = "header") {
                        CardTitle(Component.text("Konto"), id = "title")
                        CardDescription(Component.text("Deine Daten"), id = "description")
                        CardAction(id = "action") { Button(Component.text("Mehr"), id = "more") }
                    }
                    CardContent(id = "content") { P(Component.text("Inhalt"), id = "body") }
                    CardFooter(id = "footer") { Button(Component.text("Speichern"), id = "save") }
                }
            },
        )

        val alert = assertIs<AlertNode>(root.children[0])
        assertEquals(NodeAlertVariant.DESTRUCTIVE, alert.variant)
        assertEquals("circle-alert", alert.icon)
        assertEquals(SizeMode.GROW, alert.width.mode)
        assertEquals(NodeTextKind.ALERT_TITLE, assertIs<TextNode>(alert.children[0]).kind)
        assertEquals(NodeTextKind.ALERT_DESCRIPTION, assertIs<TextNode>(alert.children[1]).kind)
        val card = assertIs<CardNode>(root.children[1])
        val header = assertIs<CardHeaderNode>(card.children[0])
        assertEquals(NodeTextKind.CARD_TITLE, assertIs<TextNode>(header.children[0]).kind)
        assertEquals(NodeTextKind.CARD_DESCRIPTION, assertIs<TextNode>(header.children[1]).kind)
        assertIs<ButtonNode>(assertIs<CardActionNode>(header.children[2]).children.single())
        assertIs<CardContentNode>(card.children[1])
        assertIs<CardFooterNode>(card.children[2])
    }
}
