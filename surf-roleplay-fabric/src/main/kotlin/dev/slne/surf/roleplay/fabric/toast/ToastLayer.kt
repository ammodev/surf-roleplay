package dev.slne.surf.roleplay.fabric.toast

import dev.slne.surf.roleplay.fabric.RoleplayClient
import dev.slne.surf.roleplay.fabric.protocol.ClientPackets
import dev.slne.surf.roleplay.fabric.protocol.FabricPacketDispatcher
import dev.slne.surf.roleplay.fabric.server.RoleplayServerState
import dev.slne.surf.roleplay.fabric.ui.FontTextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.fabric.ui.widget.SpinnerWidget
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import dev.slne.surf.roleplay.protocol.toast.ToastAction
import dev.slne.surf.roleplay.protocol.toast.ToastType
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW

/**
 * Shows the toasts the server sends: over the HUD while no screen is open, and over every screen
 * otherwise. The stack expands while the mouse rests on it, which also pauses the toasts. Buttons
 * are clicked with the cursor of a screen or of the [HudCursor].
 */
object ToastLayer {

    /**
     * The shown toasts.
     */
    val stack: ToastStack = ToastStack()

    /**
     * Whether the mouse rested on the toasts in the last frame.
     */
    private var hovered: Boolean = false

    /**
     * The state that tells whether the current server is the roleplay server.
     */
    private lateinit var serverState: RoleplayServerState

    /**
     * Registers the toast packets, the HUD element, the screen hooks and the HUD cursor.
     *
     * @param state the roleplay server state
     */
    fun register(state: RoleplayServerState) {
        serverState = state
        HudCursor.key
        FabricPacketDispatcher.on(Packets.TOAST_SHOW) { packet ->
            Minecraft.getInstance().execute { if (serverState.isActive) stack.show(packet, System.currentTimeMillis()) }
        }
        FabricPacketDispatcher.on(Packets.TOAST_DISMISS) { packet -> Minecraft.getInstance().execute { stack.dismiss(packet.id) } }
        state.onChange { active -> if (!active) Minecraft.getInstance().execute { stack.clear() } }
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("surf-roleplay", "toasts")) { graphics, _ ->
            val mc = Minecraft.getInstance()
            if (mc.gui.screen() == null) {
                val (x, y) = if (HudCursor.active) HudCursor.position(mc) else HIDDEN to HIDDEN
                render(graphics, x.toInt(), y.toInt(), expandedByCursor = HudCursor.active)
            }
        }
        ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
            ScreenEvents.afterExtract(screen).register { _, graphics, mouseX, mouseY, _ ->
                graphics.nextStratum()
                render(graphics, mouseX, mouseY, expandedByCursor = false)
            }
            ScreenMouseEvents.allowMouseClick(screen).register { _, event -> !click(event.x(), event.y(), event.button()) }
        }
        ClientTickEvents.END_CLIENT_TICK.register { mc -> HudCursor.tick(mc, serverState.isActive) }
    }

    /**
     * Returns the GUI area of the window.
     *
     * @return the area
     */
    private fun window(): Rect {
        val window = Minecraft.getInstance().window
        return Rect(0, 0, window.guiScaledWidth, window.guiScaledHeight)
    }

    /**
     * Draws the toasts, expanded while the mouse rests on them or the HUD cursor is shown.
     *
     * @param graphics the GUI graphics of this frame
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     * @param expandedByCursor whether the HUD cursor is shown
     */
    private fun render(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, expandedByCursor: Boolean) {
        val entries = stack.update(System.currentTimeMillis(), paused = hovered)
        if (entries.isEmpty()) {
            hovered = false
            return
        }
        val mc = Minecraft.getInstance()
        val ui = UiGraphics(graphics, mc.font, Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK))
        val window = window()
        val expandedArea = ToastLayout.place(ui, entries, window, expanded = true)
        hovered = expandedArea.any { it.area.contains(mouseX.toDouble(), mouseY.toDouble()) }
        val placed = if (hovered || expandedByCursor) expandedArea else ToastLayout.place(ui, entries, window, expanded = false)
        placed.forEach { draw(ui, it, mouseX, mouseY) }
    }

    /**
     * Draws one toast: its surface, type icon, title, description, buttons and close button.
     *
     * @param ui the graphics to draw with
     * @param toast the placed toast
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    private fun draw(ui: UiGraphics, toast: PlacedToast, mouseX: Int, mouseY: Int) {
        val tokens = ui.tokens
        val area = toast.area
        ui.fillRounded(area, tokens.popover)
        ui.borderRounded(area, tokens.border)
        if (toast.depth > 0) return
        val packet = toast.entry.packet
        var x = area.x + ToastLayout.PADDING
        val y = area.y + ToastLayout.PADDING
        if (ToastLayout.hasIcon(toast.entry)) {
            val icon = Rect(x, y, ToastLayout.ICON, ToastLayout.ICON)
            when (packet.type) {
                ToastType.LOADING -> ui.rotatedIcon(SpinnerWidget.ICON, icon, tokens.popoverForeground, SpinnerWidget.angle(System.currentTimeMillis()))
                else -> ui.icon(iconOf(packet.type), icon, tokens.popoverForeground)
            }
            x += ToastLayout.ICON + ToastLayout.PADDING / 2
        }
        val width = ToastLayout.textWidth(toast.entry)
        ui.wrappedText(packet.title, x, y, width, tokens.popoverForeground)
        packet.description?.let {
            val titleHeight = dev.slne.surf.roleplay.fabric.ui.text.TextBlock.size(ui, packet.title, width).height
            ui.wrappedText(it, x, y + titleHeight + 2, width, tokens.mutedForeground)
        }
        toast.action?.let { button(ui, it, packet.actionLabel.orEmpty(), primary = true, mouseX, mouseY) }
        toast.cancel?.let { button(ui, it, packet.cancelLabel.orEmpty(), primary = false, mouseX, mouseY) }
        toast.close?.let { ui.icon("x", it, if (it.grow(2).contains(mouseX.toDouble(), mouseY.toDouble())) tokens.foreground else tokens.mutedForeground) }
    }

    /**
     * Draws a toast button.
     *
     * @param ui the graphics to draw with
     * @param area the area of the button
     * @param label the caption as component JSON
     * @param primary whether it is the action button
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    private fun button(ui: UiGraphics, area: Rect, label: String, primary: Boolean, mouseX: Int, mouseY: Int) {
        val tokens = ui.tokens
        val hovered = area.contains(mouseX.toDouble(), mouseY.toDouble())
        val fill = if (primary) tokens.primary else tokens.muted
        ui.fillRounded(area, if (hovered) dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors.blend(fill, tokens.background, HOVER_BLEND) else fill, 2)
        ui.centeredText(label, area, if (primary) tokens.primaryForeground else tokens.foreground)
    }

    /**
     * Returns the Lucide name of a type icon.
     *
     * @param type the toast type
     * @return the icon name
     */
    private fun iconOf(type: ToastType): String = when (type) {
        ToastType.SUCCESS -> "circle-check"
        ToastType.INFO -> "info"
        ToastType.WARNING -> "triangle-alert"
        ToastType.ERROR -> "octagon-x"
        ToastType.LOADING -> SpinnerWidget.ICON
        ToastType.DEFAULT -> "bell"
    }

    /**
     * Handles a click at a point: a click on a toast is taken, and a click on a button is
     * reported to the server.
     *
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on a toast
     */
    fun click(x: Double, y: Double, button: Int): Boolean {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false
        val measurer = FontTextMeasurer(Minecraft.getInstance().font)
        return stack.click(measurer, window(), x, y) { id, kind ->
            if (serverState.isActive) ClientPackets.send(Packets.TOAST_ACTION, ToastAction(id, kind))
            RoleplayClient.log.debug("Toast {} button {} clicked", id, kind)
        }
    }

    /**
     * A mouse position that no toast is under.
     */
    private const val HIDDEN: Double = -1000.0

    /**
     * How far a hovered button blends towards the background.
     */
    private const val HOVER_BLEND: Float = 0.15f
}
