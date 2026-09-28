package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.layout.FlexLayout
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.protocol.screen.AvatarGroupCountNode
import dev.slne.surf.roleplay.protocol.screen.AvatarGroupNode
import dev.slne.surf.roleplay.protocol.screen.AvatarNode
import dev.slne.surf.roleplay.protocol.screen.AvatarSize
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for avatars and avatar groups in the mod.
 */
class AvatarWidgetsTest {

    /**
     * A measurer in which every character is 5 pixels wide.
     */
    private val measurer = object : TextMeasurer {
        override val lineHeight: Int = 9
        override fun width(json: String): Int = json.length * 5
        override fun plainWidth(text: String): Int = text.length * 5
    }

    /**
     * Restores the picture lookup after each test.
     */
    @AfterTest
    fun restorePictures() {
        AvatarPictures.reset()
    }

    /**
     * Verifies the side length of every avatar size.
     */
    @Test
    fun `avatar sizes`() {
        assertEquals(12, AvatarWidget.side(AvatarSize.SM))
        assertEquals(16, AvatarWidget.side(AvatarSize.DEFAULT))
        assertEquals(20, AvatarWidget.side(AvatarSize.LG))
        val avatar = WidgetFactory.create(AvatarNode("a", fallback = "CN", size = AvatarSize.LG))
        assertEquals(Size(20, 20), FlexLayout.measure(avatar.createLayout(measurer)))
    }

    /**
     * Verifies that a player source is read as a UUID, and that anything else is no player.
     */
    @Test
    fun `player sources are uuids`() {
        val id = UUID.randomUUID()

        assertEquals(id, AvatarWidget.playerId(id.toString()))
        assertNull(AvatarWidget.playerId("Notch"))
        assertNull(AvatarWidget.playerId(null))
    }

    /**
     * Verifies that an avatar shows its fallback text until its picture is available.
     */
    @Test
    fun `fallback shows until the picture is available`() {
        val id = UUID.randomUUID()
        var loaded = false
        AvatarPictures.player = { if (loaded && it == id) AvatarPicture { _, _ -> } else null }
        val avatar = WidgetFactory.create(AvatarNode("a", playerId = id.toString(), fallback = "CN")) as AvatarWidget

        assertTrue(avatar.showsFallback)
        loaded = true
        assertFalse(avatar.showsFallback)
    }

    /**
     * Verifies that an avatar without a picture source always shows its fallback.
     */
    @Test
    fun `avatar without source shows its fallback`() {
        val avatar = WidgetFactory.create(AvatarNode("a", fallback = "CN")) as AvatarWidget

        assertTrue(avatar.showsFallback)
    }

    /**
     * Verifies that avatars in a group overlap, that they get a ring, and that the count takes
     * the size of the avatars.
     */
    @Test
    fun `group overlaps its avatars`() {
        val group = WidgetFactory.create(
            AvatarGroupNode(
                "group",
                children = listOf(
                    AvatarNode("a", fallback = "A", size = AvatarSize.LG),
                    AvatarNode("b", fallback = "B", size = AvatarSize.LG),
                    AvatarGroupCountNode("count", text = "+3"),
                ),
            ),
        )
        val layout = group.createLayout(measurer)

        assertEquals(Size(3 * 20 - 2 * AvatarGroupWidget.OVERLAP, 20), FlexLayout.measure(layout))
        FlexLayout.layout(layout, Rect(0, 0, 100, 20))
        group.applyLayout()
        assertEquals(20 - AvatarGroupWidget.OVERLAP, WidgetTree.find(group, "b")!!.bounds.x)
        assertTrue((WidgetTree.find(group, "a") as AvatarWidget).ring)
        assertEquals(20, WidgetTree.find(group, "count")!!.bounds.width)
    }
}
