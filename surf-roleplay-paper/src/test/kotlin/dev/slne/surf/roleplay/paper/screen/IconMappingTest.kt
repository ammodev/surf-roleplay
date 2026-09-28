package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.IconTint
import dev.slne.surf.roleplay.api.client.common.screen.screen
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.IconColor
import dev.slne.surf.roleplay.protocol.screen.IconNode
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.TextInputNode
import net.kyori.adventure.text.Component
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests for icons in the screen DSL and their mapping to protocol nodes.
 */
class IconMappingTest {

    /**
     * Verifies that icon elements and leading icons survive the DSL and the mapping.
     */
    @Test
    fun `icons map to their nodes`() {
        val definition = screen(Component.text("Icons")) {
            column("root") {
                icon("shield", "shield", size = 24, tint = IconTint.PRIMARY)
                label("info", Component.text("Hinweis"), icon = "info")
                button("delete", Component.text("Löschen"), icon = "trash")
                textInput("search", icon = "search")
            }
        }

        val children = assertIs<ColumnNode>(ScreenMapper.toNode(definition.root)).children
        assertEquals(IconNode("shield", icon = "shield", size = 24, color = IconColor.PRIMARY), children[0])
        assertEquals("info", assertIs<LabelNode>(children[1]).icon)
        assertEquals("trash", assertIs<ButtonNode>(children[2]).icon)
        assertEquals("search", assertIs<TextInputNode>(children[3]).icon)
    }
}
