package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.IconTint
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Icon
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Input
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Label
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Screen
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
        val definition = Screen(Component.text("Icons")) {
            Column(id = "root") {
                Icon("shield", size = 24, tint = IconTint.PRIMARY, id = "shield")
                Label(Component.text("Hinweis"), icon = "info", id = "info")
                Button(Component.text("Löschen"), icon = "trash", id = "delete")
                Input(icon = "search", id = "search")
            }
        }

        val children = assertIs<ColumnNode>(ScreenMapper.toNode(definition.root)).children
        assertEquals(IconNode("shield", icon = "shield", size = 24, color = IconColor.PRIMARY), children[0])
        assertEquals("info", assertIs<LabelNode>(children[1]).icon)
        assertEquals("trash", assertIs<ButtonNode>(children[2]).icon)
        assertEquals("search", assertIs<TextInputNode>(children[3]).icon)
    }
}
