package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.RoleplayClient
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.LayoutBox
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.AvatarSize
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.PlayerFaceExtractor
import net.minecraft.client.renderer.PlayerSkinRenderCache
import net.minecraft.world.item.component.ResolvableProfile
import java.util.Optional
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * A picture an avatar can draw.
 */
fun interface AvatarPicture {
    /**
     * Draws the picture stretched over a square.
     *
     * @param ui the graphics to draw with
     * @param rect the square
     */
    fun draw(ui: UiGraphics, rect: Rect)
}

/**
 * Looks up the pictures of avatars.
 */
object AvatarPictures {

    /**
     * The skin lookups started so far, keyed by player UUID.
     */
    private val lookups = ConcurrentHashMap<UUID, CompletableFuture<Optional<PlayerSkinRenderCache.RenderInfo>>>()

    /**
     * Returns the face of a player's skin once it is loaded, or `null` while it is loading or if
     * it cannot be loaded.
     */
    var player: (UUID) -> AvatarPicture? = ::skinFace

    /**
     * Restores the lookup of player faces through Minecraft's skin cache.
     */
    fun reset() {
        player = ::skinFace
    }

    /**
     * Returns the face of a player's skin from Minecraft's skin cache, starting the lookup on the
     * first request.
     *
     * @param id the UUID of the player
     * @return the face, or `null` while it is loading or if it cannot be loaded
     */
    private fun skinFace(id: UUID): AvatarPicture? {
        val lookup = lookups.computeIfAbsent(id) {
            Minecraft.getInstance().playerSkinRenderCache().lookup(ResolvableProfile.createUnresolved(it))
        }
        if (!lookup.isDone || lookup.isCompletedExceptionally) return null
        val skin = try {
            lookup.join().orElse(null)?.playerSkin()
        } catch (exception: RuntimeException) {
            RoleplayClient.log.debug("Skin lookup for {} failed", id, exception)
            null
        } ?: return null
        return AvatarPicture { ui, rect -> PlayerFaceExtractor.extractRenderState(ui.graphics, skin, rect.x, rect.y, rect.width) }
    }
}

/**
 * A round picture of a person: the face of a player's skin, or a resource-pack texture, with a
 * fallback text on a muted circle while no picture is available.
 *
 * @param id the id of the widget
 * @property playerId the UUID of the player whose face is shown, or `null`
 * @property texture the identifier of the texture shown when no player is named, or `null`
 * @property fallback the fallback text as component JSON
 * @property size the size of the avatar
 * @property badge whether a small badge is drawn at the bottom right
 * @property badgeIcon the Lucide name of an icon in the badge, or `null` for a plain dot
 */
class AvatarWidget(
    id: String,
    val playerId: UUID?,
    val texture: String?,
    var fallback: String,
    val size: AvatarSize,
    val badge: Boolean,
    val badgeIcon: String?,
) : Widget(id) {

    /**
     * Whether a ring in the background colour is drawn around the avatar, set by avatar groups.
     */
    var ring: Boolean = false

    /**
     * The picture to draw, or `null` while none is available.
     */
    private val picture: AvatarPicture?
        get() {
            playerId?.let { return AvatarPictures.player(it) }
            val identifier = texture ?: return null
            return AvatarPicture { ui, rect -> ui.image(identifier, rect) }
        }

    /**
     * Whether the avatar currently shows its fallback text instead of a picture.
     */
    val showsFallback: Boolean get() = picture == null

    /**
     * Returns the square of the avatar size.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(side(size), side(size))

    /**
     * Draws the ring, the picture or fallback clipped to a circle, and the badge.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val tokens = ui.tokens
        val side = minOf(bounds.width, bounds.height)
        val rect = Rect(bounds.x + (bounds.width - side) / 2, bounds.y + (bounds.height - side) / 2, side, side)
        if (ring) ui.fillRounded(rect.grow(1), tokens.background, side / 2 + 1)
        val shown = picture
        if (shown != null) {
            ui.clippedRound(rect, side / 2) { shown.draw(ui, rect) }
        } else {
            ui.fillRounded(rect, tokens.muted, side / 2)
            ui.centeredText(fallback, rect, tokens.mutedForeground)
        }
        if (badge) drawBadge(ui, rect)
    }

    /**
     * Draws the badge at the bottom right of the avatar, with a ring in the background colour.
     *
     * @param ui the graphics to draw with
     * @param rect the square of the avatar
     */
    private fun drawBadge(ui: UiGraphics, rect: Rect) {
        val tokens = ui.tokens
        val dot = badgeSide(size)
        val badgeRect = Rect(rect.right - dot, rect.bottom - dot, dot, dot)
        ui.fillRounded(badgeRect.grow(1), tokens.background, dot / 2 + 1)
        ui.fillRounded(badgeRect, tokens.primary, dot / 2)
        val icon = badgeIcon ?: return
        if (size == AvatarSize.SM) return
        ui.icon(icon, Rect(badgeRect.x + 1, badgeRect.y + 1, dot - 2, dot - 2), tokens.primaryForeground)
    }

    /**
     * Replaces the fallback text.
     *
     * @param json the new text as component JSON
     */
    override fun applyText(json: String) {
        fallback = json
    }

    /**
     * Holds the avatar metrics and source parsing.
     */
    companion object {
        /**
         * Returns the side length of an avatar size.
         *
         * @param size the size
         * @return the side length in GUI pixels
         */
        fun side(size: AvatarSize): Int = when (size) {
            AvatarSize.SM -> 12
            AvatarSize.DEFAULT -> 16
            AvatarSize.LG -> 20
        }

        /**
         * Returns the side length of the badge of an avatar size.
         *
         * @param size the size
         * @return the side length in GUI pixels
         */
        fun badgeSide(size: AvatarSize): Int = when (size) {
            AvatarSize.SM -> 4
            AvatarSize.DEFAULT -> 6
            AvatarSize.LG -> 7
        }

        /**
         * Reads a player source as a UUID in its canonical form.
         *
         * @param value the source, or `null`
         * @return the UUID, or `null` if the source is missing or not a canonical UUID
         */
        fun playerId(value: String?): UUID? {
            if (value == null || value.length != UUID_LENGTH) return null
            return try {
                UUID.fromString(value).takeIf { it.toString().equals(value, ignoreCase = true) }
            } catch (exception: IllegalArgumentException) {
                null
            }
        }

        /**
         * The length of a UUID in its canonical form.
         */
        private const val UUID_LENGTH: Int = 36
    }
}

/**
 * The count of further people at the end of an avatar group, drawn on a muted circle.
 *
 * @param id the id of the widget
 * @property text the count as component JSON
 * @property icon the Lucide name of an icon shown instead of the text, or `null` for none
 */
class AvatarGroupCountWidget(id: String, var text: String, val icon: String?) : Widget(id) {

    /**
     * The side length of the circle, set by the group to the size of its avatars.
     */
    var side: Int = AvatarWidget.side(AvatarSize.DEFAULT)

    /**
     * Whether a ring in the background colour is drawn around the count, set by avatar groups.
     */
    var ring: Boolean = false

    /**
     * Returns the square of the circle.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(side, side)

    /**
     * Draws the ring, the muted circle and the count or icon.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val tokens = ui.tokens
        val length = minOf(bounds.width, bounds.height)
        val rect = Rect(bounds.x + (bounds.width - length) / 2, bounds.y + (bounds.height - length) / 2, length, length)
        if (ring) ui.fillRounded(rect.grow(1), tokens.background, length / 2 + 1)
        ui.fillRounded(rect, tokens.muted, length / 2)
        val shown = icon
        if (shown != null) {
            val iconSide = length / 2
            ui.icon(shown, Rect(rect.x + (length - iconSide) / 2, rect.y + (length - iconSide) / 2, iconSide, iconSide), tokens.mutedForeground)
        } else {
            ui.centeredText(text, rect, tokens.mutedForeground)
        }
    }

    /**
     * Replaces the count.
     *
     * @param json the new count as component JSON
     */
    override fun applyText(json: String) {
        text = json
    }
}

/**
 * A row of overlapping avatars, each with a ring in the background colour, ending in an optional
 * count of the size of the avatars.
 *
 * @param id the id of the widget
 */
class AvatarGroupWidget(id: String) : ContainerWidget(id, Axis.HORIZONTAL) {
    init {
        gap = -OVERLAP
        crossAlign = Align.CENTER
    }

    /**
     * Gives the avatars and the count their ring and the count the avatars' size, then creates
     * the layout box of the group.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        val side = childList.filterIsInstance<AvatarWidget>().firstOrNull()?.let { AvatarWidget.side(it.size) }
        childList.forEach { child ->
            when (child) {
                is AvatarWidget -> child.ring = true
                is AvatarGroupCountWidget -> {
                    child.ring = true
                    if (side != null) child.side = side
                }
            }
        }
        return super.createLayout(measurer)
    }

    /**
     * Holds the overlap.
     */
    companion object {
        /**
         * How far each avatar overlaps the one before it.
         */
        const val OVERLAP: Int = 4
    }
}
