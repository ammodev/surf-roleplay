package dev.slne.surf.roleplay.fabric.screen

import dev.slne.surf.roleplay.fabric.RoleplayClient
import dev.slne.surf.roleplay.fabric.protocol.ClientPackets
import dev.slne.surf.roleplay.fabric.protocol.FabricPacketDispatcher
import dev.slne.surf.roleplay.fabric.server.RoleplayServerState
import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.RoleplayScreen
import dev.slne.surf.roleplay.fabric.ui.ScreenLayers
import dev.slne.surf.roleplay.fabric.ui.ScreenPanel
import dev.slne.surf.roleplay.fabric.ui.ScreenPanelListener
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.fabric.ui.widget.Widget
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetFactory
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.screen.ScreenClose
import dev.slne.surf.roleplay.protocol.screen.ScreenClosed
import dev.slne.surf.roleplay.protocol.screen.ScreenInputChange
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenPatch
import dev.slne.surf.roleplay.protocol.screen.ScreenStack
import dev.slne.surf.roleplay.protocol.screen.ScreenTypedAction
import dev.slne.surf.roleplay.protocol.screen.ScreenTypedUpdate
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import dev.slne.surf.roleplay.protocol.screen.TypedScreenBody
import dev.slne.surf.roleplay.protocol.screen.WidgetScreenBody
import net.minecraft.client.Minecraft

/**
 * An open server-driven screen on the client.
 */
sealed interface ClientScreen {
    /**
     * The panel that shows it.
     */
    val panel: ScreenPanel

    /**
     * A generic screen showing a widget tree.
     *
     * @property panel the panel showing the tree
     */
    class Widgets(override val panel: ScreenPanel) : ClientScreen

    /**
     * A typed screen implemented in the mod.
     *
     * @property view the view of the screen
     */
    class Typed(val view: TypedScreenView) : ClientScreen {
        /**
         * The panel of the view.
         */
        override val panel: ScreenPanel get() = view.panel
    }
}

/**
 * Mirrors the server's screen stack for the player and shows its top screen.
 *
 * Screen packets are handled only while the roleplay server is active. Leaving the roleplay
 * server closes every screen.
 */
object ClientScreenManager {

    /**
     * The open screens.
     */
    private val stack = ScreenStack<ClientScreen>()

    /**
     * The Minecraft screen that shows the visible panels, created on first use.
     */
    private val screen: RoleplayScreen by lazy { RoleplayScreen() }

    /**
     * The state that tells whether the current server is the roleplay server.
     */
    private lateinit var serverState: RoleplayServerState

    /**
     * Registers the screen packet handlers, and closes every screen when the roleplay server
     * becomes inactive.
     *
     * @param state the roleplay server state
     */
    fun register(state: RoleplayServerState) {
        serverState = state
        FabricPacketDispatcher.on(Packets.SCREEN_OPEN) { ifActive { open(it) } }
        FabricPacketDispatcher.on(Packets.SCREEN_PATCH) { ifActive { patch(it) } }
        FabricPacketDispatcher.on(Packets.SCREEN_TYPED_UPDATE) { ifActive { typedUpdate(it) } }
        FabricPacketDispatcher.on(Packets.SCREEN_CLOSE) { ifActive { close(it) } }
        state.onChange { active ->
            if (!active) Minecraft.getInstance().execute { clear() }
        }
    }

    /**
     * Runs a packet handler only while the roleplay server is active. An exception thrown by the
     * handler is logged and the packet dropped.
     *
     * @param handler the handler
     */
    private inline fun ifActive(handler: () -> Unit) {
        if (!serverState.isActive) {
            RoleplayClient.log.debug("Dropped a screen packet outside the roleplay server")
            return
        }
        try {
            handler()
        } catch (exception: RuntimeException) {
            RoleplayClient.log.error("Failed to handle a screen packet", exception)
        }
    }

    /**
     * Opens a screen from the server and shows it. An open whose parent is no longer open is
     * dropped and reported as closed.
     *
     * @param packet the open packet
     */
    private fun open(packet: ScreenOpen) {
        if (ScreenRules.isStaleOpen(stack, packet.parentSessionId)) {
            RoleplayClient.log.debug("Dropped session {}: its parent {} is closed", packet.sessionId, packet.parentSessionId)
            ClientPackets.send(Packets.SCREEN_CLOSED, ScreenClosed(packet.sessionId))
            return
        }
        val content = when (val body = packet.body) {
            is WidgetScreenBody -> ClientScreen.Widgets(
                ScreenPanel(packet.title, WidgetFactory.create(body.root), packet.closable, WidgetListener(packet.sessionId), style(packet)),
            )

            is TypedScreenBody -> {
                val factory = TypedScreens.factory(body.typeKey)
                if (factory == null) {
                    RoleplayClient.log.warn("Unknown screen type {} for session {}", body.typeKey, packet.sessionId)
                    ClientPackets.send(Packets.SCREEN_CLOSED, ScreenClosed(packet.sessionId))
                    return
                }
                ClientScreen.Typed(
                    factory.create(TypedSession(packet.sessionId), packet.title, packet.closable, body.state, style(packet)),
                )
            }
        }
        stack.open(packet.sessionId, packet.parentSessionId, packet.closable, content)
        show()
    }

    /**
     * Applies a patch to an open generic screen.
     *
     * @param packet the patch packet
     */
    private fun patch(packet: ScreenPatch) {
        val host = (stack.find(packet.sessionId)?.content as? ClientScreen.Widgets)?.panel ?: run {
            RoleplayClient.log.debug("Dropped a patch for unknown session {}", packet.sessionId)
            return
        }
        val result = ScreenPatcher.apply(host.root, packet.operations)
        if (result.skipped.isNotEmpty()) {
            RoleplayClient.log.warn("Patch for session {} addressed unknown widgets {}", packet.sessionId, result.skipped)
        }
        if (result.root !== host.root) host.root = result.root else host.treeChanged()
    }

    /**
     * Replaces the state of an open typed screen.
     *
     * @param packet the update packet
     */
    private fun typedUpdate(packet: ScreenTypedUpdate) {
        val view = (stack.find(packet.sessionId)?.content as? ClientScreen.Typed)?.view ?: run {
            RoleplayClient.log.debug("Dropped a typed update for unknown session {}", packet.sessionId)
            return
        }
        view.update(packet.state)
    }

    /**
     * Closes one screen with the screens above it, or every screen, as the server asks.
     *
     * @param packet the close packet
     */
    private fun close(packet: ScreenClose) {
        val sessionId = packet.sessionId
        if (sessionId == null) stack.closeAll() else stack.close(sessionId)
        show()
    }

    /**
     * Closes the top screen because the player pressed Escape, tells the server, and shows the
     * parent screen.
     *
     * @param sessionId the session of the screen the player closed
     */
    private fun closedByPlayer(sessionId: Int) {
        if (stack.top?.sessionId != sessionId) return
        stack.close(sessionId)
        ClientPackets.send(Packets.SCREEN_CLOSED, ScreenClosed(sessionId))
        show()
    }

    /**
     * Closes every screen without telling the server.
     */
    private fun clear() {
        stack.closeAll()
        show()
    }

    /**
     * Shows the visible panels of the stack in the roleplay screen, or closes the roleplay screen
     * if the stack is empty. Another screen that replaced the roleplay screen is left alone when
     * the stack is empty.
     */
    private fun show() {
        val minecraft = Minecraft.getInstance()
        val entries = stack.entries
        if (entries.isEmpty()) {
            screen.layers = emptyList()
            if (minecraft.gui.screen() === screen) minecraft.gui.setScreen(null)
            return
        }
        val visible = ScreenLayers.visible(entries.map { it.content.panel.style.presentation })
        screen.layers = visible.map { entries[it].content.panel }
        if (minecraft.gui.screen() !== screen) minecraft.gui.setScreen(screen)
    }

    /**
     * Creates the panel style a screen open asks for.
     *
     * @param packet the screen open
     * @return the style
     */
    private fun style(packet: ScreenOpen) =
        PanelStyle(Themes.resolve(packet.theme, packet.variant), packet.presentation, packet.sheetEdge)

    /**
     * Receives what the player does on the generic screen of one session.
     *
     * @property sessionId the session
     */
    private class WidgetListener(private val sessionId: Int) : ScreenPanelListener {

        /**
         * Sends a widget action with the screen's input values.
         *
         * @param panel the panel
         * @param widget the widget that triggered the action
         */
        override fun actionTriggered(panel: ScreenPanel, widget: Widget) {
            ClientPackets.send(Packets.SCREEN_WIDGET_ACTION, ScreenWidgetAction(sessionId, widget.id, panel.inputValues()))
        }

        /**
         * Closes the session.
         *
         * @param panel the panel
         */
        override fun closeRequested(panel: ScreenPanel) {
            closedByPlayer(sessionId)
        }

        /**
         * Sends the new value of an input that reports its changes.
         *
         * @param panel the panel
         * @param widget the input
         */
        override fun valueChanged(panel: ScreenPanel, widget: Widget) {
            val value = widget.inputValue ?: return
            ClientPackets.send(Packets.SCREEN_INPUT_CHANGE, ScreenInputChange(sessionId, widget.id, value))
        }
    }

    /**
     * The session of one typed screen.
     *
     * @property sessionId the session
     */
    private class TypedSession(override val sessionId: Int) : TypedScreenSession {

        /**
         * Sends a typed action for the session.
         *
         * @param action the encoded action
         */
        override fun sendAction(action: ByteArray) {
            ClientPackets.send(Packets.SCREEN_TYPED_ACTION, ScreenTypedAction(sessionId, action))
        }

        /**
         * Closes the session.
         */
        override fun requestClose() {
            closedByPlayer(sessionId)
        }
    }
}
