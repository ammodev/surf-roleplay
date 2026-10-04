package dev.slne.surf.roleplay.fabric.screen

import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.ScreenPanel

/**
 * What a typed screen can ask of its session.
 */
interface TypedScreenSession {
    /**
     * The session id of the screen.
     */
    val sessionId: Int

    /**
     * Sends an action of the screen, encoded with its screen type, to the server.
     *
     * @param action the encoded action
     */
    fun sendAction(action: ByteArray)

    /**
     * Closes the screen as if the player pressed Escape.
     */
    fun requestClose()
}

/**
 * A typed screen implemented in the mod.
 */
interface TypedScreenView {
    /**
     * The panel that shows the view.
     */
    val panel: ScreenPanel

    /**
     * Replaces the view's state.
     *
     * @param state the new state, encoded with the screen's type
     */
    fun update(state: ByteArray)
}

/**
 * Creates the view of one screen type.
 */
fun interface TypedScreenFactory {
    /**
     * Creates a view.
     *
     * @param session the session of the new screen
     * @param title the title as component JSON
     * @param closable whether the player can close the screen with Escape
     * @param state the initial state, encoded with the screen's type
     * @param style how the screen is drawn
     * @return the view
     */
    fun create(session: TypedScreenSession, title: String, closable: Boolean, state: ByteArray, style: PanelStyle): TypedScreenView
}

/**
 * The typed screens the mod implements, keyed by screen type key.
 */
object TypedScreens {
    /**
     * The factories keyed by screen type key.
     */
    private val factories: MutableMap<String, TypedScreenFactory> = mutableMapOf()

    /**
     * Looks up the factory of a screen type.
     *
     * @param key the screen type key
     * @return the factory, or `null` if the mod does not implement the type
     */
    fun factory(key: String): TypedScreenFactory? = factories[key]

    /**
     * Registers the factory of a screen type, replacing any factory registered for the same key.
     *
     * @param key the screen type key
     * @param factory the factory that creates the views of the type
     */
    internal fun register(key: String, factory: TypedScreenFactory) {
        factories[key] = factory
    }

    /**
     * Removes the factory of a screen type.
     *
     * @param key the screen type key
     */
    internal fun unregister(key: String) {
        factories.remove(key)
    }
}
