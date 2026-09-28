package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.roleplay.api.client.common.screen.OpenScreen
import dev.slne.surf.roleplay.api.client.common.screen.ScreenClick
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.ScreenPatchBuilder
import dev.slne.surf.roleplay.api.client.common.screen.ScreenValues
import dev.slne.surf.roleplay.api.client.common.screen.ButtonElement
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.screen.ScreenClose
import dev.slne.surf.roleplay.protocol.screen.ScreenClosed
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenPatch
import dev.slne.surf.roleplay.protocol.screen.ScreenStack
import dev.slne.surf.roleplay.protocol.screen.ScreenType
import dev.slne.surf.roleplay.protocol.screen.ScreenTypedAction
import dev.slne.surf.roleplay.protocol.screen.ScreenTypedUpdate
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import dev.slne.surf.roleplay.protocol.screen.TypedScreenBody
import dev.slne.surf.roleplay.protocol.screen.WidgetScreenBody
import net.kyori.adventure.text.Component
import java.util.UUID

private val log = logger()

/**
 * Sends clientbound screen packets to one player.
 */
interface ScreenPacketSender {
    /**
     * Sends a packet.
     *
     * @param type the packet type
     * @param packet the packet
     */
    fun <P : Packet> send(type: PacketType<P>, packet: P)
}

/**
 * An open typed screen.
 *
 * @param S the state class of the screen type
 */
interface TypedOpenScreen<S> : OpenScreen {
    /**
     * Replaces the screen's state on the player's client. Does nothing if the screen is closed.
     *
     * @param state the new state
     */
    fun update(state: S)
}

/**
 * The open screens of one player: the server side of the player's screen stack.
 *
 * It opens, patches and closes screens, and validates and routes the player's screen actions to
 * their handlers. It is not thread-safe; every call must happen on the player's thread.
 *
 * @property viewer the player's unique id
 * @property sender the sender of the player's screen packets
 * @property limiter the action rate limiter
 */
class PlayerScreenState(
    val viewer: UUID,
    private val sender: ScreenPacketSender,
    private val limiter: ActionRateLimiter,
) {
    /**
     * The outcome of handling a player's action.
     */
    sealed interface Outcome {
        /**
         * The action was valid and its handler ran.
         */
        data object Accepted : Outcome

        /**
         * The action was rejected, and no handler ran.
         *
         * @property reason why the action was rejected
         */
        data class Rejected(val reason: String) : Outcome
    }

    /**
     * The open screens.
     */
    private val stack = ScreenStack<Session>()

    /**
     * The session id of the next screen.
     */
    private var nextSessionId = 1

    /**
     * The open screens, from bottom to top.
     */
    val openScreens: List<OpenScreen> get() = stack.entries.map { it.content }

    /**
     * Opens a generic screen.
     *
     * @param definition the screen
     * @param parentSessionId the session to open on top of, or `null` to replace every open screen
     * @return the open screen
     */
    fun open(definition: ScreenDefinition, parentSessionId: Int?): OpenScreen {
        val session = GenericSession(nextSessionId++, definition)
        push(session, parentSessionId)
        sender.send(
            Packets.SCREEN_OPEN,
            ScreenOpen(session.sessionId, parentSessionId, ScreenMapper.text(definition.title), definition.closable, WidgetScreenBody(ScreenMapper.toNode(definition.root))),
        )
        return session
    }

    /**
     * Opens a typed screen.
     *
     * @param S the state class
     * @param A the action class
     * @param type the screen type
     * @param title the title
     * @param state the initial state
     * @param closable whether the player can close the screen with Escape
     * @param parentSessionId the session to open on top of, or `null` to replace every open screen
     * @param onAction the handler of the screen's decoded actions
     * @param onClose the handler run when the screen closes, or `null`
     * @return the open screen
     */
    fun <S, A> openTyped(
        type: ScreenType<S, A>,
        title: Component,
        state: S,
        closable: Boolean,
        parentSessionId: Int?,
        onAction: (TypedOpenScreen<S>, A) -> Unit,
        onClose: ((TypedOpenScreen<S>) -> Unit)? = null,
    ): TypedOpenScreen<S> {
        val session = TypedSession(nextSessionId++, closable, type, onAction, onClose)
        push(session, parentSessionId)
        sender.send(
            Packets.SCREEN_OPEN,
            ScreenOpen(session.sessionId, parentSessionId, ScreenMapper.text(title), closable, TypedScreenBody(type.key, type.encodeState(state))),
        )
        return session
    }

    /**
     * Puts a session on the stack and runs the close handlers of the screens it replaces.
     *
     * @param session the session
     * @param parentSessionId the parent session, or `null`
     */
    private fun push(session: Session, parentSessionId: Int?) {
        stack.open(session.sessionId, parentSessionId, session.closable, session).forEach { it.content.closed() }
    }

    /**
     * Closes a screen with the screens above it and runs their close handlers.
     *
     * @param sessionId the session to close
     * @param notifyClient whether to tell the player's client
     */
    fun close(sessionId: Int, notifyClient: Boolean) {
        val removed = stack.close(sessionId)
        if (removed.isEmpty()) return
        if (notifyClient) sender.send(Packets.SCREEN_CLOSE, ScreenClose(sessionId))
        removed.forEach { it.content.closed() }
    }

    /**
     * Closes every screen and runs their close handlers.
     *
     * @param notifyClient whether to tell the player's client
     */
    fun closeAll(notifyClient: Boolean) {
        val removed = stack.closeAll()
        if (removed.isEmpty()) return
        if (notifyClient) sender.send(Packets.SCREEN_CLOSE, ScreenClose(null))
        removed.forEach { it.content.closed() }
    }

    /**
     * Handles the player's report that they closed a screen.
     *
     * @param packet the report
     */
    fun handleClosed(packet: ScreenClosed) = close(packet.sessionId, notifyClient = false)

    /**
     * Validates a click on a generic screen and runs the button's handler.
     *
     * @param packet the click
     * @return the outcome
     */
    fun handleWidgetAction(packet: ScreenWidgetAction): Outcome {
        if (!limiter.tryAcquire(viewer)) return Outcome.Rejected("rate limit exceeded")
        val session = stack.find(packet.sessionId)?.content as? GenericSession
            ?: return Outcome.Rejected("no open generic screen with session ${packet.sessionId}")
        return when (val result = ScreenActionValidator.validate(session.tree, packet.widgetId, packet.values)) {
            is ScreenActionValidator.Result.Rejected -> Outcome.Rejected(result.reason)
            is ScreenActionValidator.Result.Accepted -> {
                session.tree.storeValues(result.values)
                val button = session.tree.find(packet.widgetId) as ButtonElement
                runHandler("click on '${packet.widgetId}'") {
                    button.onClick?.onClick(ScreenClick(session, packet.widgetId, ScreenValues(result.values)))
                }
                Outcome.Accepted
            }
        }
    }

    /**
     * Decodes an action of a typed screen and runs the screen's action handler.
     *
     * @param packet the action
     * @return the outcome
     */
    fun handleTypedAction(packet: ScreenTypedAction): Outcome {
        if (!limiter.tryAcquire(viewer)) return Outcome.Rejected("rate limit exceeded")
        val session = stack.find(packet.sessionId)?.content as? TypedSession<*, *>
            ?: return Outcome.Rejected("no open typed screen with session ${packet.sessionId}")
        return session.handle(packet.action)
    }

    /**
     * Runs a handler, logging and swallowing its exceptions so that one failing handler does not
     * break the player's screens.
     *
     * @param what a description of the handler, for the log
     * @param handler the handler
     */
    private inline fun runHandler(what: String, handler: () -> Unit) {
        try {
            handler()
        } catch (exception: Exception) {
            log.atSevere().withCause(exception).log("Screen handler for %s of %s failed", what, viewer)
        }
    }

    /**
     * An open screen session of this player.
     *
     * @property sessionId the session id
     * @property closable whether the player can close the screen with Escape
     */
    private abstract inner class Session(override val sessionId: Int, val closable: Boolean) : OpenScreen {
        override val viewer: UUID get() = this@PlayerScreenState.viewer

        override val isOpen: Boolean get() = stack.find(sessionId)?.content === this

        /**
         * Closes this screen with the screens above it and tells the player's client.
         */
        override fun close() = close(sessionId, notifyClient = true)

        /**
         * Runs the screen's close handler after it was removed from the stack.
         */
        abstract fun closed()
    }

    /**
     * An open generic screen.
     *
     * @param sessionId the session id
     * @property definition the screen's definition
     */
    private inner class GenericSession(sessionId: Int, private val definition: ScreenDefinition) :
        Session(sessionId, definition.closable) {

        /**
         * The server's copy of the screen's tree.
         */
        val tree = ServerScreenTree(definition.root)

        /**
         * Applies changes to the server's tree and sends the ones that applied to the client.
         * Changes that do not apply are logged.
         *
         * @param changes the builder of the changes
         */
        override fun patch(changes: ScreenPatchBuilder.() -> Unit) {
            if (!isOpen) return
            val (applied, refused) = ScreenPatchBuilder().apply(changes).changes.partition { tree.apply(it) }
            if (refused.isNotEmpty()) log.atWarning().log("Screen patch for %s refused changes %s", viewer, refused)
            if (applied.isNotEmpty()) sender.send(Packets.SCREEN_PATCH, ScreenPatch(sessionId, applied.map(ScreenMapper::toOperation)))
        }

        /**
         * Runs the definition's close handler.
         */
        override fun closed() {
            runHandler("close of session $sessionId") { definition.onClose?.onClose(this) }
        }
    }

    /**
     * An open typed screen.
     *
     * @param S the state class
     * @param A the action class
     * @param sessionId the session id
     * @param closable whether the player can close the screen with Escape
     * @property type the screen type
     * @property onAction the handler of decoded actions
     * @property onClose the close handler, or `null`
     */
    private inner class TypedSession<S, A>(
        sessionId: Int,
        closable: Boolean,
        private val type: ScreenType<S, A>,
        private val onAction: (TypedOpenScreen<S>, A) -> Unit,
        private val onClose: ((TypedOpenScreen<S>) -> Unit)?,
    ) : Session(sessionId, closable), TypedOpenScreen<S> {

        /**
         * Does nothing, because typed screens change through their state.
         *
         * @param changes ignored
         */
        override fun patch(changes: ScreenPatchBuilder.() -> Unit) = Unit

        /**
         * Sends a new state to the client.
         *
         * @param state the new state
         */
        override fun update(state: S) {
            if (!isOpen) return
            sender.send(Packets.SCREEN_TYPED_UPDATE, ScreenTypedUpdate(sessionId, type.encodeState(state)))
        }

        /**
         * Decodes an action and runs the action handler.
         *
         * @param bytes the encoded action
         * @return the outcome
         */
        fun handle(bytes: ByteArray): Outcome {
            val action = try {
                type.decodeAction(bytes)
            } catch (exception: Exception) {
                return Outcome.Rejected("undecodable ${type.key} action: ${exception.message}")
            }
            runHandler("${type.key} action of session $sessionId") { onAction(this, action) }
            return Outcome.Accepted
        }

        /**
         * Runs the close handler.
         */
        override fun closed() {
            runHandler("close of session $sessionId") { onClose?.invoke(this) }
        }
    }
}
