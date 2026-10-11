package dev.slne.surf.roleplay.fabric.interaction

import dev.slne.surf.roleplay.fabric.protocol.ClientPackets
import dev.slne.surf.roleplay.fabric.protocol.FabricPacketDispatcher
import dev.slne.surf.roleplay.fabric.toast.HudCursor
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.interaction.InteractionExecute
import dev.slne.surf.roleplay.protocol.interaction.InteractionTargetOffer
import dev.slne.surf.roleplay.protocol.interaction.InteractionTargetQuery
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW

/**
 * The target eye: while the HUD cursor is on and the server allows it, an eye marks the crosshair,
 * the target under it is queried after the dwell, and its options are listed beside the
 * crosshair for the cursor to pick.
 */
object TargetEyeLayer {

    /** Decides when to query. */
    private val dwell = TargetDwell()

    /** The offer being shown, or `null`. */
    private var shown: InteractionTargetOffer? = null

    /** The row areas of the shown list from the last frame. */
    private var rows: List<Rect> = emptyList()

    /**
     * The area of the shown option list, or `null` if none is shown.
     */
    val listArea: Rect?
        get() = rows.takeIf { it.isNotEmpty() }?.let { Rect(it.first().x, it.first().y, it.first().width, it.sumOf { r -> r.height }) }

    /**
     * Registers the offer packet, the tick that queries targets and the HUD element.
     */
    fun register() {
        FabricPacketDispatcher.on(Packets.INTERACTION_TARGET_OFFER) { offer -> Minecraft.getInstance().execute { receive(offer) } }
        ClientTickEvents.END_CLIENT_TICK.register { mc -> tick(mc) }
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("surf-roleplay", "target_eye")) { graphics, _ ->
            val mc = Minecraft.getInstance()
            if (!visible(mc)) return@addLast
            val ui = UiGraphics(graphics, mc.font, Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK))
            val centerX = mc.window.guiScaledWidth / 2
            val centerY = mc.window.guiScaledHeight / 2
            ui.icon("eye", Rect(centerX - EYE / 2, centerY - EYE / 2, EYE, EYE), ui.tokens.foreground)
            val offer = shown ?: return@addLast
            val (mouseX, mouseY) = HudCursor.position(mc)
            val width = offer.options.maxOf { ui.width(it.label) } + PADDING * 2 + EYE
            rows = OptionListLayout.place(offer.options.size, ROW, width, centerX, centerY, Rect(0, 0, mc.window.guiScaledWidth, mc.window.guiScaledHeight))
            ui.fillRounded(listArea!!.grow(2), ui.tokens.popover)
            offer.options.forEachIndexed { index, option ->
                val row = rows[index]
                if (row.contains(mouseX, mouseY)) ui.fillRounded(row, ThemeColors.blend(ui.tokens.popover, ui.tokens.foreground, HOVER_BLEND), 2)
                if (option.icon.isNotEmpty()) ui.icon(option.icon, Rect(row.x + PADDING, row.y + (ROW - ICON) / 2, ICON, ICON), ui.tokens.popoverForeground)
                ui.text(option.label, row.x + PADDING + EYE, row.y + (ROW - ui.lineHeight) / 2 + 1, ui.tokens.popoverForeground)
            }
        }
    }

    /**
     * Tells whether the eye is shown.
     *
     * @param mc the Minecraft client
     * @return whether the HUD cursor is on, no screen is open and the server allows it
     */
    private fun visible(mc: Minecraft) = HudCursor.active && mc.gui.screen() == null && InteractionClientState.available

    /**
     * Queries the target under the crosshair after the dwell and hides a list whose target is
     * gone, unless the cursor is over the list.
     *
     * @param mc the Minecraft client
     */
    private fun tick(mc: Minecraft) {
        if (!visible(mc)) {
            hide()
            dwell.reset()
            return
        }
        val (x, y) = HudCursor.position(mc)
        val overList = listArea?.contains(x, y) == true
        val target = TargetPicker.pick(mc)
        if (!overList && shown != null && target != shown?.let { TargetRef(it.kind, it.id) }) hide()
        dwell.update(System.currentTimeMillis(), target, paused = overList)?.let { ClientPackets.send(Packets.INTERACTION_TARGET_QUERY, InteractionTargetQuery(it.kind, it.id)) }
    }

    /**
     * Shows an offer if it is for the target under the crosshair and has options.
     *
     * @param offer the offer
     */
    private fun receive(offer: InteractionTargetOffer) {
        val mc = Minecraft.getInstance()
        if (!visible(mc) || offer.options.isEmpty() || TargetPicker.pick(mc) != TargetRef(offer.kind, offer.id)) return hide()
        shown = offer
    }

    /**
     * Hides the option list.
     */
    private fun hide() {
        shown = null
        rows = emptyList()
    }

    /**
     * Handles a cursor click: a left click on an option sends the pick and queries the target
     * again after the dwell.
     *
     * @param x the cursor x position
     * @param y the cursor y position
     * @param button the mouse button
     * @return whether the click was on the option list
     */
    fun click(x: Double, y: Double, button: Int): Boolean {
        val offer = shown ?: return false
        val index = OptionListLayout.indexAt(rows, x, y) ?: return false
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            ClientPackets.send(Packets.INTERACTION_EXECUTE, InteractionExecute(offer.offerId, index))
            hide()
            dwell.reset()
        }
        return true
    }

    /** The size of the eye and of the icon column. */
    private const val EYE: Int = 12

    /** The size of an option icon. */
    private const val ICON: Int = 9

    /** The height of an option row. */
    private const val ROW: Int = 14

    /** The inner padding of a row. */
    private const val PADDING: Int = 4

    /** How far a hovered row blends towards the foreground. */
    private const val HOVER_BLEND: Float = 0.12f
}
