package dev.slne.surf.roleplay.fabric.interaction

import com.mojang.blaze3d.platform.InputConstants
import dev.slne.surf.roleplay.fabric.RoleplayClient
import dev.slne.surf.roleplay.fabric.protocol.ClientPackets
import dev.slne.surf.roleplay.fabric.protocol.FabricPacketDispatcher
import dev.slne.surf.roleplay.fabric.server.RoleplayServerState
import dev.slne.surf.roleplay.fabric.settings.KeyMode
import dev.slne.surf.roleplay.fabric.settings.RoleplayKeys
import dev.slne.surf.roleplay.fabric.toast.HudCursor
import dev.slne.surf.roleplay.fabric.ui.TextAlign
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.interaction.InteractionExecute
import dev.slne.surf.roleplay.protocol.interaction.InteractionRadialOffer
import dev.slne.surf.roleplay.protocol.interaction.InteractionRadialRequest
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * The radial menu: its key requests the menu from the server, and the returned tree is shown as
 * a ring around the screen centre. Submenus open locally; a picked action is sent to the server.
 * With the hold mode the menu closes when the key is released, with the toggle mode on the next
 * press. A right click goes back one level.
 */
object RadialMenuLayer {

    /**
     * The key binding that opens the radial menu, R by default, in the roleplay category.
     */
    val key: KeyMapping by lazy {
        KeyMappingHelper.registerKeyMapping(
            KeyMapping("key.surf-roleplay.radial_menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, RoleplayKeys.category),
        )
    }

    /** The offer being shown, or `null`. */
    private var offer: InteractionRadialOffer? = null

    /** The navigation through the shown offer. */
    private var navigation: RadialNavigation? = null

    /** Whether a request was sent and its answer is awaited. */
    private var requested: Boolean = false

    /**
     * Whether the radial menu is shown.
     */
    val isOpen: Boolean get() = navigation != null

    /**
     * Registers the key, the offer packet, the tick and the HUD element.
     *
     * @param state the roleplay server state
     */
    fun register(state: RoleplayServerState) {
        key
        FabricPacketDispatcher.on(Packets.INTERACTION_RADIAL_OFFER) { packet -> Minecraft.getInstance().execute { receive(packet) } }
        state.onChange { active -> if (!active) Minecraft.getInstance().execute { close() } }
        ClientTickEvents.END_CLIENT_TICK.register { mc -> tick(mc, state.isActive) }
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("surf-roleplay", "radial_menu")) { graphics, _ ->
            draw(UiGraphics(graphics, Minecraft.getInstance().font, Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)))
        }
    }

    /**
     * Draws the current ring around the screen centre and highlights the item under the cursor.
     *
     * @param ui the drawing helper
     */
    private fun draw(ui: UiGraphics) {
        val mc = Minecraft.getInstance()
        val shown = offer ?: return
        val items = navigation?.items ?: return
        val (cx, cy) = center(mc)
        val (mx, my) = HudCursor.position(mc)
        val hovered = RadialLayout.hovered(mx - cx, my - cy, items.size, INNER.toDouble())
        items.forEachIndexed { slot, item ->
            val angle = RadialLayout.angle(slot, items.size)
            val x = (cx + cos(angle) * RADIUS).roundToInt()
            val y = (cy + sin(angle) * RADIUS).roundToInt()
            val (label, icon) = when (item) {
                is RingItem.Node -> shown.nodes[item.index].let { it.label to it.icon }
                is RingItem.More -> MORE_LABEL to "ellipsis"
            }
            val textWidth = minOf(ui.width(label), MAX_LABEL_WIDTH)
            val width = maxOf(textWidth + PADDING * 2, ITEM)
            val area = Rect(x - width / 2, y - ITEM / 2, width, ITEM)
            val fill = if (slot == hovered) ThemeColors.blend(ui.tokens.popover, ui.tokens.foreground, HOVER_BLEND) else ui.tokens.popover
            ui.fillRounded(area, fill)
            ui.borderRounded(area, ui.tokens.border)
            if (icon.isNotEmpty()) ui.icon(icon, Rect(x - ICON / 2, area.y + 3, ICON, ICON), ui.tokens.popoverForeground)
            ui.wrappedText(label, area.x + PADDING, area.y + ICON + 4, width - PADDING * 2, ui.tokens.popoverForeground, maxLines = 1, align = TextAlign.CENTER)
        }
    }

    /**
     * Returns the screen centre in GUI pixels.
     *
     * @param mc the Minecraft client
     * @return the x and y position
     */
    private fun center(mc: Minecraft): Pair<Double, Double> = mc.window.guiScaledWidth / 2.0 to mc.window.guiScaledHeight / 2.0

    /**
     * Applies the key: requests the menu on press, and closes it on release (hold) or on the next
     * press (toggle). Ignored while the HUD cursor is on, a screen is open or interaction is not
     * available.
     *
     * @param mc the Minecraft client
     * @param active whether the roleplay server is active
     */
    private fun tick(mc: Minecraft, active: Boolean) {
        val pressed = key.consumeClick()
        val usable = active && mc.player != null && mc.gui.screen() == null && InteractionClientState.available
        if (!usable) return close()
        val mode = RoleplayClient.settings.current.radialKeyMode
        when {
            isOpen && mode == KeyMode.HOLD && !key.isDown -> close()
            isOpen && mode == KeyMode.TOGGLE && pressed -> close()
            !isOpen && !requested && pressed && !HudCursor.active -> {
                requested = ClientPackets.send(Packets.INTERACTION_RADIAL_REQUEST, InteractionRadialRequest)
            }
        }
    }

    /**
     * Shows a received tree if it was requested, the menu may still open (a player is present, no
     * screen is open, the HUD cursor is off and interaction is available), it is not empty and it
     * passes [RadialNavigation.isValid]; any other offer is dropped.
     *
     * @param packet the offer
     */
    private fun receive(packet: InteractionRadialOffer) {
        if (!requested) return
        requested = false
        val mc = Minecraft.getInstance()
        if (mc.player == null || mc.gui.screen() != null || HudCursor.active || !InteractionClientState.available) return
        if (packet.nodes.isEmpty() || !RadialNavigation.isValid(packet.nodes)) return
        offer = packet
        navigation = RadialNavigation(packet.nodes)
        mc.mouseHandler.releaseMouse()
    }

    /**
     * Handles a cursor click: a left click picks the hovered item, and a right click goes back
     * one level or closes the menu at the top level.
     *
     * @param x the cursor x position
     * @param y the cursor y position
     * @param button the mouse button
     */
    fun click(x: Double, y: Double, button: Int) {
        val nav = navigation ?: return
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (!nav.back()) close()
            return
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return
        val (cx, cy) = center(Minecraft.getInstance())
        val items = nav.items
        val slot = RadialLayout.hovered(x - cx, y - cy, items.size, INNER.toDouble()) ?: return
        val picked = nav.enter(items[slot]) ?: return
        ClientPackets.send(Packets.INTERACTION_EXECUTE, InteractionExecute(offer!!.offerId, picked))
        close()
    }

    /**
     * Closes the menu and captures the mouse again unless the HUD cursor or a screen needs it.
     */
    private fun close() {
        requested = false
        if (navigation == null) return
        navigation = null
        offer = null
        val mc = Minecraft.getInstance()
        if (mc.player != null && !HudCursor.active && mc.gui.screen() == null) mc.mouseHandler.grabMouse()
    }

    /** The label of the overflow item as component JSON. */
    private const val MORE_LABEL: String = "{\"text\":\"Mehr…\"}"

    /** The distance from the centre to the item centres. */
    private const val RADIUS: Int = 60

    /** The radius of the centre area that selects nothing. */
    private const val INNER: Int = 20

    /** The height and least width of an item. */
    private const val ITEM: Int = 28

    /** The size of an item icon. */
    private const val ICON: Int = 10

    /** The horizontal padding inside an item. */
    private const val PADDING: Int = 6

    /** The widest an item label is drawn; longer labels end with an ellipsis. */
    private const val MAX_LABEL_WIDTH: Int = 120

    /** How far a hovered item blends towards the foreground. */
    private const val HOVER_BLEND: Float = 0.15f
}
