package dev.slne.surf.roleplay.fabric.ui

import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.protocol.screen.Presentation
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

/**
 * The Minecraft screen that shows the visible roleplay panels, bottom to top.
 *
 * Every dialog or sheet is drawn over a dimmed backdrop that covers the panels below it. Only
 * the top panel receives input. Escape, or a click on the backdrop of a dialog or sheet, asks the
 * top panel to close if it is closable; the screen never closes itself.
 */
class RoleplayScreen : Screen(Component.empty()) {

    /**
     * The visible panels, from bottom to top.
     */
    var layers: List<ScreenPanel> = emptyList()

    /**
     * Receives every key press before the panels; returning `true` takes the key, so that the
     * panels do not see it. `null` passes every key to the panels.
     */
    var keyInterceptor: ((KeyEvent) -> Boolean)? = null

    /**
     * Receives every mouse click before the panels; returning `true` takes the click, so that the
     * panels do not see it. `null` passes every click to the panels.
     */
    var mouseInterceptor: ((MouseButtonEvent) -> Boolean)? = null

    /**
     * The filter that lets held activation keys trigger only once.
     */
    private val repeats = KeyRepeatFilter()

    /**
     * The panel that receives input, or `null` if no panel is shown.
     */
    private val top: ScreenPanel? get() = layers.lastOrNull()

    /**
     * Lays every panel out again when the screen is initialised or resized.
     */
    override fun init() {
        layers.forEach { it.requestLayout() }
    }

    /**
     * Draws every visible panel, with a dimmed backdrop under each dialog and sheet.
     *
     * @param graphics the GUI graphics of this frame
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     * @param partialTick the partial tick
     */
    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick)
        layers.forEachIndexed { index, layer ->
            if (index > 0 && layer.style.presentation != Presentation.SCREEN) {
                graphics.nextStratum()
                graphics.fill(0, 0, width, height, ThemeColors.withAlpha(layer.tokens.background, BACKDROP_ALPHA))
            }
            graphics.nextStratum()
            layer.layoutIfNeeded(font, width, height)
            layer.render(graphics, font, mouseX, mouseY, active = index == layers.lastIndex)
        }
    }

    /**
     * Offers a click to the mouse interceptor, then passes it to the top panel. A left click on the backdrop of a dialog or sheet asks it to
     * close, unless the dialog or sheet opened only a moment ago.
     *
     * @param event the mouse event
     * @param doubleClick whether the click is a double click
     * @return `true`, because the screen takes every click
     */
    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        if (mouseInterceptor?.invoke(event) == true) return true
        val panel = top ?: return true
        val handled = panel.mouseClicked(event.x(), event.y(), event.button())
        val backdrop = !handled && panel.style.presentation != Presentation.SCREEN
        val settled = System.currentTimeMillis() - panel.openedAt >= BACKDROP_GRACE_MILLIS
        if (backdrop && settled && event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) panel.requestClose()
        return true
    }

    /**
     * Passes mouse movement with a button held to the top panel.
     *
     * @param event the mouse event
     * @param dragX the horizontal movement
     * @param dragY the vertical movement
     * @return `true`
     */
    override fun mouseDragged(event: MouseButtonEvent, dragX: Double, dragY: Double): Boolean {
        top?.mouseDragged(event.x(), event.y())
        return true
    }

    /**
     * Ends a drag in every panel when a mouse button is released.
     *
     * @param event the mouse event
     * @return `true`
     */
    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        layers.forEach { it.mouseReleased() }
        return true
    }

    /**
     * Passes wheel scrolling to the top panel.
     *
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     * @param scrollX the horizontal scroll amount
     * @param scrollY the vertical scroll amount
     * @return `true`
     */
    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        top?.mouseScrolled(mouseX, mouseY, scrollY)
        return true
    }

    /**
     * Offers a key to the key interceptor, then passes it to the top panel. Escape that the panel
     * does not use asks it to close. Repeats of a held activation key reach neither.
     *
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(event: KeyEvent): Boolean {
        if (!repeats.accept(event.key())) return true
        if (keyInterceptor?.invoke(event) == true) return true
        val panel = top ?: return super.keyPressed(event)
        if (panel.keyPressed(event)) return true
        if (event.isEscape) {
            panel.requestClose()
            return true
        }
        return false
    }

    /**
     * Records that a key was released, so that a held activation key can trigger again.
     *
     * @param event the key event
     * @return whether the release was handled by the default screen behaviour
     */
    override fun keyReleased(event: KeyEvent): Boolean {
        repeats.release(event.key())
        return super.keyReleased(event)
    }

    /**
     * Passes a typed character to the top panel.
     *
     * @param event the character event
     * @return whether the character was handled
     */
    override fun charTyped(event: CharacterEvent): Boolean = top?.charTyped(event) == true

    /**
     * Keeps the screen open on Escape; closing is decided by the panels' owner.
     *
     * @return `false`
     */
    override fun shouldCloseOnEsc(): Boolean = false

    /**
     * Keeps the game running while the screen is open.
     *
     * @return `false`
     */
    override fun isPauseScreen(): Boolean = false

    /**
     * Holds the backdrop opacity.
     */
    private companion object {
        /**
         * The opacity of the backdrop behind dialogs and sheets, relative to the background colour.
         */
        const val BACKDROP_ALPHA: Float = 0.6f

        /**
         * How long after a dialog or sheet opens a click on its backdrop is ignored, in milliseconds.
         */
        const val BACKDROP_GRACE_MILLIS: Long = 250
    }
}
