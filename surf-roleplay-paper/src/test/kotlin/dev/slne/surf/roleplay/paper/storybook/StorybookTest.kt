package dev.slne.surf.roleplay.paper.storybook

import dev.slne.surf.roleplay.api.client.common.screen.ContainerElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.ScreenElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenThemes
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.TextElement
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Screen
import dev.slne.surf.roleplay.paper.screen.ActionRateLimiter
import dev.slne.surf.roleplay.paper.screen.PlayerScreenState
import dev.slne.surf.roleplay.paper.screen.ScreenPacketSender
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import dev.slne.surf.roleplay.protocol.screen.ScreenInputChange as ScreenInputChangePacket

/**
 * Tests for the storybook page and its stories.
 */
class StorybookTest {

    /**
     * The packets sent to the client.
     */
    private val sent = mutableListOf<Packet>()

    /**
     * The screen state the rendered pages are opened in, which validates and maps them.
     */
    private val state = PlayerScreenState(
        UUID.randomUUID(),
        object : ScreenPacketSender {
            /**
             * Records a packet.
             *
             * @param type the packet type
             * @param packet the packet
             */
            override fun <P : Packet> send(type: PacketType<P>, packet: P) {
                sent += packet
            }
        },
        ActionRateLimiter(10_000),
    )

    /**
     * The reports of the page under test.
     */
    private val reports = mutableListOf<String>()

    /**
     * Two small stories in two categories.
     */
    private val testStories = listOf(
        Story("button", "Schaltfläche", StoryCategory.INPUTS) { context -> Button("Los", id = "go", onClick = context.clicked) },
        Story("badge", "Abzeichen", StoryCategory.DISPLAY) { _ -> Button("Neu", id = "new") },
    )

    /**
     * Renders a page into the definition its screen would show.
     *
     * @param page the page
     * @return the definition
     */
    private fun definition(page: StorybookPage): ScreenDefinition =
        Screen(page.title, page.theme, page.variant, page.closable) { with(page) { render() } }

    /**
     * Renders a page and opens it in [state].
     *
     * @param page the page
     * @return the session id of the opened screen
     */
    private fun open(page: StorybookPage): Int {
        state.open(definition(page), null)
        return (sent.last() as ScreenOpen).sessionId
    }

    /**
     * Returns every element of a tree, depth first.
     *
     * @param root the root
     * @return the elements
     */
    private fun elements(root: ScreenElement): List<ScreenElement> =
        listOf(root) + ((root as? ContainerElement)?.children.orEmpty().flatMap(::elements))

    /**
     * Returns the plain texts of the text elements of a definition.
     *
     * @param definition the definition
     * @return the texts
     */
    private fun texts(definition: ScreenDefinition): List<String> =
        elements(definition.root).filterIsInstance<TextElement>().map { PlainTextComponentSerializer.plainText().serialize(it.text) }

    /**
     * Checks that the storybook's stories of a category have exactly the given keys, and that each
     * of them renders and maps in every theme and variant.
     *
     * @param category the category
     * @param keys the expected story keys
     */
    private fun assertCategory(category: StoryCategory, keys: Set<String>) {
        val stories = storybookStories()
        val inCategory = stories.filter { it.category == category }
        assertEquals(keys, inCategory.map { it.key }.toSet())
        val page = StorybookPage(UUID.randomUUID(), reports::add, stories)
        for (story in inCategory) {
            for (theme in listOf(ScreenThemes.DEFAULT, ScreenThemes.SAR, ScreenThemes.POLICE)) {
                for (dark in listOf(true, false)) {
                    page.storyKey = story.key
                    page.themeName = theme
                    page.dark = dark
                    open(page)
                    assertIs<ScreenOpen>(sent.last(), "${story.key} in $theme")
                }
            }
        }
    }

    /**
     * The input stories cover the input components and render.
     */
    @Test
    fun `the input stories cover their components and render`() = assertCategory(
        StoryCategory.INPUTS,
        setOf(
            "button", "button-group", "calendar", "checkbox", "combobox", "field", "form", "input", "input-group", "input-otp",
            "label", "native-select", "radio-group", "select", "slider", "switch", "textarea", "toggle", "toggle-group",
        ),
    )

    /**
     * Every test story renders and maps in every theme and variant.
     */
    @Test
    fun `the page renders every story in every theme and variant`() {
        val page = StorybookPage(UUID.randomUUID(), reports::add, testStories)
        for (story in testStories) {
            for (theme in listOf(ScreenThemes.DEFAULT, ScreenThemes.SAR, ScreenThemes.POLICE)) {
                for (dark in listOf(true, false)) {
                    page.storyKey = story.key
                    page.themeName = theme
                    page.dark = dark
                    open(page)
                    assertIs<ScreenOpen>(sent.last(), "${story.key} in $theme")
                }
            }
        }
    }

    /**
     * The sidebar highlights the shown story, and a click on another story shows it.
     */
    @Test
    fun `a sidebar click selects a story and highlights it`() {
        val page = StorybookPage(UUID.randomUUID(), reports::add, testStories)
        val buttons = elements(definition(page).root).filterIsInstance<SidebarMenuButtonElement>()
        assertEquals(listOf("button"), buttons.filter { it.active }.map { it.id.removePrefix("storybook_story_") })

        val session = open(page)
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "storybook_story_badge")))

        assertEquals("badge", page.storyKey)
        val active = elements(definition(page).root).filterIsInstance<SidebarMenuButtonElement>().single { it.active }
        assertEquals("storybook_story_badge", active.id)
        assertTrue("Abzeichen" in texts(definition(page)))
    }

    /**
     * A click inside a story reports the story's name and the element id.
     */
    @Test
    fun `a click in a story is reported with the story name`() {
        val page = StorybookPage(UUID.randomUUID(), reports::add, testStories)
        val session = open(page)

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "go")))

        assertEquals(listOf("Schaltfläche: go"), reports)
    }

    /**
     * The theme select and the dark switch change the page's theme and variant.
     */
    @Test
    fun `the theme select and the dark switch change theme and variant`() {
        val page = StorybookPage(UUID.randomUUID(), reports::add, testStories)
        assertEquals(ScreenThemes.DEFAULT, page.theme)
        assertEquals(ScreenVariant.DARK, page.variant)
        val session = open(page)

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChangePacket(session, "storybook_theme", ScreenThemes.POLICE)))
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChangePacket(session, "storybook_dark", "false")))

        assertEquals(ScreenThemes.POLICE, page.theme)
        assertEquals(ScreenVariant.LIGHT, page.variant)
        assertEquals(Component.text("Storybook"), page.title)
    }
}
