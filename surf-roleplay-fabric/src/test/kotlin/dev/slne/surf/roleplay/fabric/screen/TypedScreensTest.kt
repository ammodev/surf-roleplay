package dev.slne.surf.roleplay.fabric.screen

import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.ScreenPanel
import dev.slne.surf.roleplay.fabric.ui.ScreenPanelListener
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.fabric.ui.widget.Widget
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetFactory
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests for the typed screen registry and the open, update, action and close flow a typed screen
 * runs through, driven with a test-only factory and a recording session.
 */
class TypedScreensTest {

    /**
     * Everything the recording session and the test view saw, in order.
     */
    private val events = mutableListOf<String>()

    /**
     * The actions the screen sent through its session.
     */
    private val actions = mutableListOf<ByteArray>()

    /**
     * A session that records the actions and close requests of the screen.
     *
     * @property sessionId the session id
     */
    private inner class RecordingSession(override val sessionId: Int) : TypedScreenSession {

        /**
         * Records the action.
         *
         * @param action the encoded action
         */
        override fun sendAction(action: ByteArray) {
            actions += action
            events += "action:${action.decodeToString()}"
        }

        /**
         * Records the close request.
         */
        override fun requestClose() {
            events += "close:$sessionId"
        }
    }

    /**
     * A view that remembers its construction arguments and the states it received.
     *
     * @property session the session of the view
     * @property closable whether the screen is closable
     * @property state the latest state
     */
    private inner class TestView(
        val session: TypedScreenSession,
        val closable: Boolean,
        var state: ByteArray,
        override val panel: ScreenPanel,
    ) : TypedScreenView {

        /**
         * Stores the new state.
         *
         * @param state the new state
         */
        override fun update(state: ByteArray) {
            this.state = state
            events += "update:${state.decodeToString()}"
        }
    }

    /**
     * Creates a panel with an empty tree.
     *
     * @return the panel
     */
    private fun panel(): ScreenPanel {
        val listener = object : ScreenPanelListener {
            override fun actionTriggered(panel: ScreenPanel, widget: Widget) = Unit
            override fun closeRequested(panel: ScreenPanel) = Unit
        }
        return ScreenPanel("T", WidgetFactory.create(ColumnNode("root")), true, listener, PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)))
    }

    /**
     * Removes the factories the tests registered.
     */
    @AfterTest
    fun cleanUp() {
        TypedScreens.unregister(KEY)
        TypedScreens.unregister("test.other")
    }

    /**
     * Verifies that a type nobody registered has no factory, so the client reports the session as
     * closed instead of opening it.
     */
    @Test
    fun `an unregistered type has no factory`() {
        assertNull(TypedScreens.factory("test.unknown"))
    }

    /**
     * Verifies that a registered factory is found by its own key only, and that unregistering
     * removes it.
     */
    @Test
    fun `a factory is found by its own key only`() {
        TypedScreens.register(KEY) { session, _, closable, state, _ -> TestView(session, closable, state, panel()) }

        assertNotNull(TypedScreens.factory(KEY))
        assertNull(TypedScreens.factory("test.other"))
        TypedScreens.unregister(KEY)
        assertNull(TypedScreens.factory(KEY))
    }

    /**
     * Verifies the typed flow: the factory gets the open's session, title, closable flag and
     * initial state; an update reaches the view; the view's action and close request go through
     * its own session.
     */
    @Test
    fun `a typed screen opens updates sends actions and closes through its session`() {
        TypedScreens.register(KEY) { session, title, closable, state, _ ->
            events += "create:${session.sessionId}:$title:$closable:${state.decodeToString()}"
            TestView(session, closable, state, panel())
        }

        val factory = assertNotNull(TypedScreens.factory(KEY))
        val view = factory.create(RecordingSession(7), "{\"text\":\"Hi\"}", false, "s0".encodeToByteArray(), PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK))) as TestView
        view.update("s1".encodeToByteArray())
        view.session.sendAction("go".encodeToByteArray())
        view.session.requestClose()

        assertEquals(
            listOf("create:7:{\"text\":\"Hi\"}:false:s0", "update:s1", "action:go", "close:7"),
            events,
        )
        assertContentEquals("s1".encodeToByteArray(), view.state)
        assertEquals(1, actions.size)
        assertSame(view.panel, ClientScreen.Typed(view).panel)
    }

    /**
     * Verifies that registering a second factory for a key replaces the first one.
     */
    @Test
    fun `registering a key again replaces its factory`() {
        TypedScreens.register(KEY) { session, _, closable, state, _ -> TestView(session, closable, state, panel()).also { events += "first" } }
        TypedScreens.register(KEY) { session, _, closable, state, _ -> TestView(session, closable, state, panel()).also { events += "second" } }

        TypedScreens.factory(KEY)!!.create(RecordingSession(1), "", true, ByteArray(0), PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)))

        assertTrue(events == listOf("second"))
    }

    private companion object {
        /**
         * The screen type key the tests register.
         */
        const val KEY = "test.typed"
    }
}
