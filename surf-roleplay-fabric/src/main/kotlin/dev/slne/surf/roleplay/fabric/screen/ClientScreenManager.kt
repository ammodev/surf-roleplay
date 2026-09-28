package dev.slne.surf.roleplay.fabric.screen

import dev.slne.surf.roleplay.fabric.RoleplayClient
import dev.slne.surf.roleplay.fabric.protocol.ClientPackets
import dev.slne.surf.roleplay.fabric.protocol.FabricPacketDispatcher
import dev.slne.surf.roleplay.fabric.server.RoleplayServerState
import dev.slne.surf.roleplay.fabric.ui.RoleplayScreenHost
import dev.slne.surf.roleplay.fabric.ui.ScreenHostListener
import dev.slne.surf.roleplay.fabric.ui.widget.ButtonWidget
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetFactory
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.screen.ScreenClose
import dev.slne.surf.roleplay.protocol.screen.ScreenClosed
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenPatch
import dev.slne.surf.roleplay.protocol.screen.ScreenTypedAction
import dev.slne.surf.roleplay.protocol.screen.ScreenTypedUpdate
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import dev.slne.surf.roleplay.protocol.screen.TypedScreenBody
import dev.slne.surf.roleplay.protocol.screen.WidgetScreenBody
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen

/**
 * An open server-driven screen on the client.
 */
sealed interface ClientScreen {
    /**
     * The Minecraft screen that shows it.
     */
    val screen: Screen

    /**
     * A generic screen showing a widget tree.
     *
     * @property host the host showing the tree
     */
    class Widgets(val host: RoleplayScreenHost) : ClientScreen {
        override val screen: Screen get() = host
    }

    /**
     * A typed screen implemented in the mod.
     *
     * @property view the view of the screen
     */
    class Typed(val view: TypedScreenView) : ClientScreen {
        override val screen: Screen get() = view.screen
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
     * The Minecraft screen this manager showed last, or `null` if it closed it or showed none.
     */
    private var shown: Screen? = null

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
     * Runs a packet handler only while the roleplay server is active.
     *
     * @param handler the handler
     */
    private inline fun ifActive(handler: () -> Unit) {
        if (serverState.isActive) handler() else RoleplayClient.log.debug("Dropped a screen packet outside the roleplay server")
    }

    /**
     * Opens a screen from the server and shows it.
     *
     * @param packet the open packet
     */
    private fun open(packet: ScreenOpen) {
        val content = when (val body = packet.body) {
            is WidgetScreenBody -> ClientScreen.Widgets(
                RoleplayScreenHost(packet.title, WidgetFactory.create(body.root), packet.closable, WidgetListener(packet.sessionId)),
            )

            is TypedScreenBody -> {
                val factory = TypedScreens.factory(body.typeKey)
                if (factory == null) {
                    RoleplayClient.log.warn("Unknown screen type {} for session {}", body.typeKey, packet.sessionId)
                    ClientPackets.send(Packets.SCREEN_CLOSED, ScreenClosed(packet.sessionId))
                    return
                }
                ClientScreen.Typed(factory.create(TypedSession(packet.sessionId), packet.title, packet.closable, body.state))
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
        val host = (stack.find(packet.sessionId)?.content as? ClientScreen.Widgets)?.host ?: run {
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
     * Shows the top screen of the stack, or closes the screen this manager showed if the stack is
     * empty. Another screen that replaced it in the meantime is left alone.
     */
    private fun show() {
        val minecraft = Minecraft.getInstance()
        val top = stack.top?.content?.screen
        if (top != null) {
            if (minecraft.gui.screen() !== top) minecraft.gui.setScreen(top)
            shown = top
            return
        }
        if (shown != null && minecraft.gui.screen() === shown) minecraft.gui.setScreen(null)
        shown = null
    }

    /**
     * Receives what the player does on the generic screen of one session.
     *
     * @property sessionId the session
     */
    private class WidgetListener(private val sessionId: Int) : ScreenHostListener {

        /**
         * Sends a widget action with the screen's input values.
         *
         * @param host the screen
         * @param button the clicked button
         */
        override fun buttonClicked(host: RoleplayScreenHost, button: ButtonWidget) {
            ClientPackets.send(Packets.SCREEN_WIDGET_ACTION, ScreenWidgetAction(sessionId, button.id, host.inputValues()))
        }

        /**
         * Closes the session.
         *
         * @param host the screen
         */
        override fun closeRequested(host: RoleplayScreenHost) {
            closedByPlayer(sessionId)
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
