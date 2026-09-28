package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.roleplay.api.client.common.screen.OpenScreen
import dev.slne.surf.roleplay.api.client.common.screen.ScreenClick
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.ScreenPatchBuilder
import dev.slne.surf.roleplay.api.client.common.screen.ScreenPresentation
import dev.slne.surf.roleplay.api.client.common.screen.ScreenThemes
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.api.client.common.screen.SheetSide
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
 * their handlers. Only the top screen accepts actions. It is not thread-safe: every entry point,
 * including the members of the open screens it hands out, runs [threadCheck] first.
 *
 * @property viewer the player's unique id
 * @property sender the sender of the player's screen packets
 * @property limiter the action rate limiter
 * @property threadCheck throws [IllegalStateException] if the calling thread does not own the player
 */
class PlayerScreenState(
    val viewer: UUID,
    private val sender: ScreenPacketSender,
    private val limiter: ActionRateLimiter,
    private val threadCheck: () -> Unit = {},
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
         * @property suspicious whether a normal client could not have sent the action; `false` for
         *           actions that arrive for a screen that closed or was covered in the meantime
         */
        data class Rejected(val reason: String, val suspicious: Boolean = true) : Outcome
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
     * Whether the player left, after which no screen can be opened.
     */
    private var disposed = false

    /**
     * The open screens, from bottom to top.
     */
    val openScreens: List<OpenScreen>
        get() {
            threadCheck()
            return stack.entries.map { it.content }
        }

    /**
     * Opens a generic screen.
     *
     * @param definition the screen
     * @param parentSessionId the session to open on top of, or `null` to replace every open screen
     * @param presentation how the screen is shown relative to the screens below it
     * @param sheetSide the window edge a sheet is attached to
     * @return the open screen
     * @throws IllegalStateException if the player left
     */
    fun open(
        definition: ScreenDefinition,
        parentSessionId: Int?,
        presentation: ScreenPresentation = ScreenPresentation.SCREEN,
        sheetSide: SheetSide = SheetSide.RIGHT,
    ): OpenScreen {
        checkUsable()
        val (parentTheme, parentVariant) = themeOf(parentSessionId)
        val theme = definition.theme ?: parentTheme
        val variant = definition.variant ?: parentVariant
        val session = GenericSession(nextSessionId++, definition, theme, variant)
        push(
            session,
            ScreenOpen(
                sessionId = session.sessionId,
                parentSessionId = parentSessionId,
                title = ScreenMapper.text(definition.title),
                closable = definition.closable,
                body = WidgetScreenBody(ScreenMapper.toNode(definition.root)),
                theme = theme,
                variant = ScreenMapper.variant(variant),
                presentation = ScreenMapper.presentation(presentation),
                sheetEdge = ScreenMapper.sheetEdge(sheetSide),
            ),
        )
        return session
    }

    /**
     * Returns the theme and variant an open generic screen is drawn with.
     *
     * @param sessionId the session
     * @return the theme name and variant, or the default theme in dark if the session is not an
     *         open generic screen
     */
    fun themeOf(sessionId: Int?): Pair<String, ScreenVariant> {
        threadCheck()
        val session = sessionId?.let { stack.find(it)?.content } as? GenericSession
        return (session?.theme ?: ScreenThemes.DEFAULT) to (session?.variant ?: ScreenVariant.DARK)
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
     * @throws IllegalStateException if the player left
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
        checkUsable()
        val session = TypedSession(nextSessionId++, closable, type, onAction, onClose)
        push(
            session,
            ScreenOpen(session.sessionId, parentSessionId, ScreenMapper.text(title), closable, TypedScreenBody(type.key, type.encodeState(state))),
        )
        return session
    }

    /**
     * Checks that the calling thread owns the player and that the player has not left.
     *
     * @throws IllegalStateException if either does not hold
     */
    private fun checkUsable() {
        threadCheck()
        check(!disposed) { "The player $viewer left; no screen can be opened" }
    }

    /**
     * Puts a session on the stack, sends its open packet, and then runs the close handlers of the
     * screens it replaced, so that screens those handlers open follow the new one on the client.
     *
     * @param session the session
     * @param packet the open packet of the session
     */
    private fun push(session: Session, packet: ScreenOpen) {
        val removed = stack.open(session.sessionId, packet.parentSessionId, session.closable, session)
        sender.send(Packets.SCREEN_OPEN, packet)
        removed.forEach { it.content.closed() }
    }

    /**
     * Closes a screen with the screens above it and runs their close handlers.
     *
     * @param sessionId the session to close
     * @param notifyClient whether to tell the player's client
     */
    fun close(sessionId: Int, notifyClient: Boolean) {
        threadCheck()
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
        threadCheck()
        val removed = stack.closeAll()
        if (removed.isEmpty()) return
        if (notifyClient) sender.send(Packets.SCREEN_CLOSE, ScreenClose(null))
        removed.forEach { it.content.closed() }
    }

    /**
     * Closes every screen without telling the client, runs their close handlers, and refuses every
     * later open, including opens from those handlers.
     */
    fun dispose() {
        threadCheck()
        disposed = true
        closeAll(notifyClient = false)
    }

    /**
     * Handles the player's report that they closed a screen. Only a closable top screen can be
     * closed this way; other reports are ignored.
     *
     * @param packet the report
     * @return the outcome
     */
    fun handleClosed(packet: ScreenClosed): Outcome {
        threadCheck()
        val top = stack.top ?: return Outcome.Rejected("no open screen", suspicious = false)
        if (top.sessionId != packet.sessionId) return Outcome.Rejected("session ${packet.sessionId} is not the top screen", suspicious = false)
        if (!top.closable) return Outcome.Rejected("session ${packet.sessionId} is not closable")
        close(packet.sessionId, notifyClient = false)
        return Outcome.Accepted
    }

    /**
     * Returns the top session if it has the given id.
     *
     * @param sessionId the session id of the action
     * @return the top session, or `null` if the session is not open or covered by another screen
     */
    private fun topSession(sessionId: Int): Session? = stack.top?.takeIf { it.sessionId == sessionId }?.content

    /**
     * Validates a click on a generic screen and runs the button's handler.
     *
     * @param packet the click
     * @return the outcome
     */
    fun handleWidgetAction(packet: ScreenWidgetAction): Outcome {
        threadCheck()
        if (!limiter.tryAcquire(viewer)) return Outcome.Rejected("rate limit exceeded")
        val session = topSession(packet.sessionId) as? GenericSession
            ?: return Outcome.Rejected("session ${packet.sessionId} is not the top generic screen", suspicious = false)
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
        threadCheck()
        if (!limiter.tryAcquire(viewer)) return Outcome.Rejected("rate limit exceeded")
        val session = topSession(packet.sessionId) as? TypedSession<*, *>
            ?: return Outcome.Rejected("session ${packet.sessionId} is not the top typed screen", suspicious = false)
        return session.handle(packet.action)
    }

    /**
     * Runs a handler, logging and swallowing its exceptions.
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
        /**
         * The player who sees the screen.
         */
        override val viewer: UUID get() = this@PlayerScreenState.viewer

        /**
         * Whether this session is still on the player's stack.
         */
        override val isOpen: Boolean
            get() {
                threadCheck()
                return stack.find(sessionId)?.content === this
            }

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
     * @property theme the theme the screen is drawn with, after inheriting from its parent
     * @property variant the variant the screen is drawn with, after inheriting from its parent
     */
    private inner class GenericSession(
        sessionId: Int,
        val definition: ScreenDefinition,
        val theme: String,
        val variant: ScreenVariant,
    ) :
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
            threadCheck()
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
            threadCheck()
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
