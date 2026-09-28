package dev.slne.surf.roleplay.fabric.screen

import dev.slne.surf.roleplay.fabric.ui.RoleplayScreenHost
import dev.slne.surf.roleplay.fabric.ui.ScreenHostListener
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeTokens
import dev.slne.surf.roleplay.fabric.ui.widget.ButtonWidget
import dev.slne.surf.roleplay.fabric.ui.widget.LabelWidget
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetFactory
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetTree
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.CounterAction
import dev.slne.surf.roleplay.protocol.screen.CounterState
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.RowNode
import dev.slne.surf.roleplay.protocol.screen.ScreenTypes
import dev.slne.surf.roleplay.protocol.screen.Sizing
import net.minecraft.client.gui.screens.Screen

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
     * The Minecraft screen that shows the view.
     */
    val screen: Screen

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
     * @param tokens the design tokens the screen is drawn with
     * @return the view
     */
    fun create(session: TypedScreenSession, title: String, closable: Boolean, state: ByteArray, tokens: ThemeTokens): TypedScreenView
}

/**
 * The typed screens the mod implements, keyed by screen type key.
 */
object TypedScreens {
    /**
     * The factories keyed by screen type key.
     */
    private val factories: Map<String, TypedScreenFactory> = mapOf(
        ScreenTypes.DEBUG_COUNTER.key to TypedScreenFactory(::DebugCounterView),
    )

    /**
     * Looks up the factory of a screen type.
     *
     * @param key the screen type key
     * @return the factory, or `null` if the mod does not implement the type
     */
    fun factory(key: String): TypedScreenFactory? = factories[key]
}

/**
 * The debug counter screen: a number with buttons that ask the server to lower or raise it.
 *
 * @param session the session of the screen
 * @param title the title as component JSON
 * @param closable whether the player can close the screen with Escape
 * @param state the initial state
 * @param tokens the design tokens the screen is drawn with
 */
class DebugCounterView(
    private val session: TypedScreenSession,
    title: String,
    closable: Boolean,
    state: ByteArray,
    tokens: ThemeTokens,
) : TypedScreenView, ScreenHostListener {

    /**
     * The host that shows the counter.
     */
    private val host = RoleplayScreenHost(title, WidgetFactory.create(tree()), closable, this, tokens)

    /**
     * The host that shows the counter.
     */
    override val screen: Screen get() = host

    init {
        update(state)
    }

    /**
     * Builds the counter's widget tree.
     *
     * @return the root node
     */
    private fun tree() = ColumnNode(
        id = "counter",
        gap = 8,
        crossAlign = Align.CENTER,
        children = listOf(
            LabelNode(VALUE_ID),
            RowNode(
                id = "buttons",
                gap = 6,
                children = listOf(
                    ButtonNode(DECREMENT_ID, width = Sizing.fixed(40), text = "\"-1\""),
                    ButtonNode(INCREMENT_ID, width = Sizing.fixed(40), text = "\"+1\""),
                ),
            ),
        ),
    )

    /**
     * Shows the number of a new state.
     *
     * @param state the encoded counter state
     */
    override fun update(state: ByteArray) {
        val counter: CounterState = ScreenTypes.DEBUG_COUNTER.decodeState(state)
        (WidgetTree.find(host.root, VALUE_ID) as LabelWidget).text = """{"text":"Wert: ${counter.value}"}"""
        host.requestLayout()
    }

    /**
     * Sends the counter action of a clicked button.
     *
     * @param host the screen
     * @param button the clicked button
     */
    override fun buttonClicked(host: RoleplayScreenHost, button: ButtonWidget) {
        val delta = when (button.id) {
            DECREMENT_ID -> -1
            INCREMENT_ID -> 1
            else -> return
        }
        session.sendAction(ScreenTypes.DEBUG_COUNTER.encodeAction(CounterAction(delta)))
    }

    /**
     * Closes the session.
     *
     * @param host the screen
     */
    override fun closeRequested(host: RoleplayScreenHost) {
        session.requestClose()
    }

    /**
     * Holds the widget ids of the counter.
     */
    private companion object {
        /**
         * The id of the label that shows the number.
         */
        const val VALUE_ID = "value"

        /**
         * The id of the button that lowers the number.
         */
        const val DECREMENT_ID = "decrement"

        /**
         * The id of the button that raises the number.
         */
        const val INCREMENT_ID = "increment"
    }
}
