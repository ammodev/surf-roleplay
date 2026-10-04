package dev.slne.surf.roleplay.fabric.settings

import com.mojang.blaze3d.platform.InputConstants
import dev.slne.surf.roleplay.fabric.RoleplayClient
import dev.slne.surf.roleplay.fabric.server.RoleplayServerState
import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.RoleplayScreen
import dev.slne.surf.roleplay.fabric.ui.ScreenPanel
import dev.slne.surf.roleplay.fabric.ui.ScreenPanelListener
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.fabric.ui.widget.Widget
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetFactory
import dev.slne.surf.roleplay.protocol.screen.Presentation
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

/**
 * The roleplay settings screen: rebinds the key bindings of the roleplay category and sets the
 * cursor key mode.
 *
 * The screen is built and handled entirely on the client and never sends anything to the
 * server. Clicking a key button waits for the next key or mouse button, which becomes the new
 * key; Escape unbinds it instead. Key changes are written to the game options at once, the cursor
 * key mode to the client settings. Closing the screen returns to the screen it was opened from.
 * The screen is shown only while the roleplay server is active and closes when it is left.
 */
object SettingsScreen {

    /**
     * The prefix of the names of the mod's key bindings, which is cut off to form binding ids.
     */
    private const val NAME_PREFIX = "key.surf-roleplay."

    /**
     * The state that tells whether the current server is the roleplay server.
     */
    private lateinit var serverState: RoleplayServerState

    /**
     * The open settings screen, or `null` if it was never opened or is closed.
     */
    private var screen: RoleplayScreen? = null

    /**
     * The panel of the open settings screen, or `null` if none is open.
     */
    private var panel: ScreenPanel? = null

    /**
     * The screen the settings screen was opened from, shown again when it closes.
     */
    private var previous: Screen? = null

    /**
     * The id of the binding that waits for a new key, or `null` if none does.
     */
    private var capturing: String? = null

    /**
     * Closes the settings screen whenever the roleplay server becomes inactive.
     *
     * @param state the roleplay server state
     */
    fun register(state: RoleplayServerState) {
        serverState = state
        state.onChange { active -> if (!active) Minecraft.getInstance().execute { close(returnToPrevious = false) } }
    }

    /**
     * Opens the settings screen over the current screen. Does nothing unless the roleplay server
     * is active.
     */
    fun open() {
        if (!serverState.isActive) return
        val minecraft = Minecraft.getInstance()
        val current = minecraft.gui.screen()
        if (current != null && current === screen) return
        capturing = null
        previous = current
        val opened = RoleplayScreen()
        opened.keyInterceptor = ::interceptKey
        opened.mouseInterceptor = ::interceptMouse
        val created = ScreenPanel(
            SettingsView.text(SettingsView.TITLE),
            WidgetFactory.create(view()),
            closable = true,
            listener = Listener,
            style = PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK), Presentation.SCREEN),
        )
        opened.layers = listOf(created)
        screen = opened
        panel = created
        minecraft.gui.setScreen(opened)
    }

    /**
     * Closes the settings screen if it is shown and forgets its state.
     *
     * @param returnToPrevious whether to show the screen it was opened from instead of no screen
     */
    private fun close(returnToPrevious: Boolean) {
        val minecraft = Minecraft.getInstance()
        val shown = screen
        val back = previous
        screen = null
        panel = null
        previous = null
        capturing = null
        if (shown != null && minecraft.gui.screen() === shown) minecraft.gui.setScreen(if (returnToPrevious) back else null)
    }

    /**
     * Returns the key bindings of the roleplay category, in the order of the game options.
     *
     * @return the key bindings
     */
    private fun mappings(): List<KeyMapping> =
        Minecraft.getInstance().options.keyMappings.filter { it.category == RoleplayKeys.category }

    /**
     * Returns the id under which a key binding is shown.
     *
     * @param mapping the key binding
     * @return its name without the mod's prefix
     */
    private fun idOf(mapping: KeyMapping): String = mapping.name.removePrefix(NAME_PREFIX)

    /**
     * Builds the settings tree from the current key bindings and client settings.
     *
     * @return the root node
     */
    private fun view(): ScreenNode {
        val mappings = mappings()
        val rows = mappings.map {
            BindingRow(idOf(it), Component.translatable(it.name).string, it.translatedKeyMessage.string, it.isDefault, it.isUnbound)
        }
        val others = Minecraft.getInstance().options.keyMappings.filter { it.category != RoleplayKeys.category }.map { it.saveString() }
        val conflicts = KeyBindings.conflicts(mappings.map { idOf(it) to it.saveString() }, others)
        return SettingsView.build(rows, conflicts, capturing, RoleplayClient.settings.current)
    }

    /**
     * Shows the current state by replacing the panel's widget tree.
     */
    private fun render() {
        panel?.root = WidgetFactory.create(view())
    }

    /**
     * Takes the next key while a binding waits for one: Escape unbinds the binding, any other
     * key becomes its key.
     *
     * @param event the key event
     * @return whether the key was taken
     */
    private fun interceptKey(event: KeyEvent): Boolean {
        val id = capturing ?: return false
        bind(id, if (event.isEscape) InputConstants.UNKNOWN else InputConstants.getKey(event))
        return true
    }

    /**
     * Takes the next mouse button while a binding waits for a key, and makes it the binding's key.
     *
     * @param event the mouse event
     * @return whether the click was taken
     */
    private fun interceptMouse(event: MouseButtonEvent): Boolean {
        val id = capturing ?: return false
        bind(id, InputConstants.Type.MOUSE.getOrCreate(event.button()))
        return true
    }

    /**
     * Sets the key of one binding, ends the capture, saves and shows the result.
     *
     * @param id the id of the binding
     * @param key the new key
     */
    private fun bind(id: String, key: InputConstants.Key) {
        capturing = null
        mappings().firstOrNull { idOf(it) == id }?.setKey(key)
        applyKeys()
    }

    /**
     * Rebuilds the key lookup of the game, saves the game options and shows the result.
     */
    private fun applyKeys() {
        KeyMapping.resetMapping()
        Minecraft.getInstance().options.save()
        render()
    }

    /**
     * Receives what the player does on the settings panel.
     */
    private object Listener : ScreenPanelListener {

        /**
         * Starts capturing a key for a key button, restores the default key of one binding or of
         * every binding.
         *
         * @param panel the panel
         * @param widget the widget that triggered the action
         */
        override fun actionTriggered(panel: ScreenPanel, widget: Widget) {
            when (val action = SettingsAction.of(widget.id)) {
                SettingsAction.ResetAll -> {
                    capturing = null
                    mappings().forEach { it.setKey(it.defaultKey) }
                    applyKeys()
                }

                is SettingsAction.StartCapture -> {
                    capturing = action.id
                    render()
                }

                is SettingsAction.Reset -> {
                    capturing = null
                    mappings().firstOrNull { idOf(it) == action.id }?.let { it.setKey(it.defaultKey) }
                    applyKeys()
                }

                null -> Unit
            }
        }

        /**
         * Closes the settings screen and shows the screen it was opened from.
         *
         * @param panel the panel
         */
        override fun closeRequested(panel: ScreenPanel) {
            close(returnToPrevious = true)
        }

        /**
         * Stores a changed cursor key mode in the client settings.
         *
         * @param panel the panel
         * @param widget the input that changed
         */
        override fun valueChanged(panel: ScreenPanel, widget: Widget) {
            if (widget.id != SettingsView.CURSOR_MODE_ID) return
            val mode = KeyMode.entries.firstOrNull { it.name == widget.inputValue } ?: return
            RoleplayClient.settings.update { it.copy(cursorKeyMode = mode) }
        }
    }
}
