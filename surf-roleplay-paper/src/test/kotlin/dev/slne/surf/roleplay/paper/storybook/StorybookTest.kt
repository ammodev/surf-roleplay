package dev.slne.surf.roleplay.paper.storybook

import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ChangeHandler
import dev.slne.surf.roleplay.api.client.common.screen.ColumnElement
import dev.slne.surf.roleplay.api.client.common.screen.ContainerElement
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.IconElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationNextElement
import dev.slne.surf.roleplay.api.client.common.screen.RowElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenChange
import dev.slne.surf.roleplay.api.client.common.screen.ScreenClick
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.ScreenElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenInputChange
import dev.slne.surf.roleplay.api.client.common.screen.ScreenSearch
import dev.slne.surf.roleplay.api.client.common.screen.ScreenValues
import dev.slne.surf.roleplay.api.client.common.screen.SearchHandler
import dev.slne.surf.roleplay.api.client.common.screen.ScreenThemes
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.api.client.common.screen.ScrollAreaElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarContentElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarProviderElement
import dev.slne.surf.roleplay.api.client.common.screen.SelectElement
import dev.slne.surf.roleplay.api.client.common.screen.SwitchElement
import dev.slne.surf.roleplay.api.client.common.screen.TextElement
import dev.slne.surf.roleplay.api.client.common.screen.TextKind
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.diff.ScreenDiff
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.toast.Toast
import dev.slne.surf.roleplay.api.client.common.toast.ToastType
import dev.slne.surf.roleplay.api.client.common.screen.dsl.HandlerBinder
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Input
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Screen
import dev.slne.surf.roleplay.api.client.common.screen.dsl.renderRoot
import dev.slne.surf.roleplay.paper.screen.ActionRateLimiter
import dev.slne.surf.roleplay.paper.screen.PlayerScreenState
import dev.slne.surf.roleplay.paper.screen.ScreenPacketSender
import dev.slne.surf.roleplay.paper.storybook.stories.pageWindow
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
import kotlin.test.assertNotNull
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import dev.slne.surf.roleplay.protocol.screen.ScreenInputChange as ScreenInputChangePacket

/**
 * The registry names of the shadcn components the screen framework implements.
 */
private val SHADCN_COMPONENTS = setOf(
    "accordion", "alert", "alert-dialog", "aspect-ratio", "avatar", "badge", "breadcrumb", "button", "button-group", "calendar",
    "card", "carousel", "chart", "checkbox", "collapsible", "combobox", "command", "context-menu", "data-table", "dialog",
    "direction", "dropdown-menu", "empty", "field", "form", "hover-card", "input", "input-group", "input-otp",
    "item", "kbd", "label", "menubar", "navigation-menu", "pagination", "popover", "progress", "radio-group",
    "resizable", "scroll-area", "select", "separator", "sheet", "sidebar", "skeleton", "slider", "spinner",
    "switch", "table", "tabs", "textarea", "toast", "toggle", "toggle-group", "tooltip", "typography", "chat",
)

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
     * A catalog of 100 icons named `icon-000` to `icon-099`; the even ones are in the category
     * `even`, the odd ones in `odd`.
     */
    private val catalog = LucideCatalog(
        (0 until 100).map { IconInfo("icon-%03d".format(it), listOf(if (it % 2 == 0) "even" else "odd"), emptyList()) },
    )

    /**
     * Creates a storybook with every story, whose icon gallery lists a catalog.
     *
     * @param icons the catalog of the icon gallery
     * @return the page
     */
    private fun storybook(icons: LucideCatalog = catalog): StorybookPage =
        StorybookPage(UUID.randomUUID(), reports::add) { gallery -> storybookStories(icons, gallery) }

    /**
     * Returns the icon names the gallery of a page shows.
     *
     * @param page the page
     * @return the names, in grid order
     */
    private fun shownIcons(page: StorybookPage): List<String> =
        elements(definition(page).root).filterIsInstance<IconElement>().map { it.icon }

    /**
     * Checks that the storybook's stories of a category have exactly the given keys, and that each
     * of them renders and maps in every theme and variant.
     *
     * @param category the category
     * @param keys the expected story keys
     */
    private fun assertCategory(category: StoryCategory, keys: Set<String>) {
        val page = storybook()
        val inCategory = page.stories.filter { it.category == category }
        assertEquals(keys, inCategory.map { it.key }.toSet())
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
            "label", "radio-group", "select", "slider", "switch", "textarea", "toggle", "toggle-group",
            "state",
        ),
    )

    /**
     * The display stories cover the display components and render.
     */
    @Test
    fun `the display stories cover their components and render`() = assertCategory(
        StoryCategory.DISPLAY,
        setOf("alert", "aspect-ratio", "avatar", "badge", "card", "empty", "item", "kbd", "progress", "separator", "skeleton", "spinner", "typography"),
    )

    /**
     * The overlay stories cover the overlay and menu components and render.
     */
    @Test
    fun `the overlay stories cover their components and render`() = assertCategory(
        StoryCategory.OVERLAYS,
        setOf(
            "alert-dialog", "command", "context-menu", "dialog", "dropdown-menu", "hover-card", "menubar", "popover",
            "sheet", "toast", "tooltip",
        ),
    )

    /**
     * The navigation stories cover the navigation and layout components and render.
     */
    @Test
    fun `the navigation stories cover their components and render`() = assertCategory(
        StoryCategory.NAVIGATION,
        setOf("accordion", "breadcrumb", "carousel", "collapsible", "direction", "navigation-menu", "pagination", "resizable", "scroll-area", "sidebar", "tabs"),
    )

    /**
     * The data stories cover the table, chart and chat components and render.
     */
    @Test
    fun `the data stories cover their components and render`() = assertCategory(
        StoryCategory.DATA,
        setOf("chart", "chat", "data-table", "table"),
    )

    /**
     * The tabs, breadcrumb, scroll area and carousel stories end with a realistic example.
     */
    @Test
    fun `the navigation stories show an example`() {
        val page = storybook()
        for (key in listOf("tabs", "breadcrumb", "scroll-area", "carousel")) {
            page.storyKey = key
            assertTrue("Beispiel" in texts(definition(page)), key)
        }
    }

    /**
     * The toast story shows toasts of every type, toasts with actions, a loading toast that is
     * replaced, a toast that stays until it is closed, and an example.
     */
    @Test
    fun `the toast story shows every kind of toast`() {
        val page = storybook()
        page.storyKey = "toast"
        val definition = definition(page)

        assertTrue(texts(definition).containsAll(listOf("Typen", "Aktionen", "Laden und ersetzen", "Bleibt stehen", "Beispiel")))
        val ids = elements(definition.root).map { it.id }
        for (id in listOf("toast_success", "toast_action", "toast_upload", "toast_done", "toast_sticky", "toast_appointment")) {
            assertTrue(id in ids, id)
        }
    }

    /**
     * Clicking the upload button and then the done button of the toast story shows two toasts
     * with the same id, so the second replaces the first, as a loading toast and then a success.
     */
    @Test
    fun `the done button replaces the loading toast`() {
        val toasts = mutableListOf<Toast>()
        val context = StoryContext(UUID.randomUUID(), reports::add, showToast = { toasts += it })
        val story = storybook().stories.single { it.key == "toast" }
        val session = state.open(Screen(Component.text("Toast")) { Column(id = "root") { story.render(this, context) } }, null).sessionId

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "toast_upload")))
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "toast_done")))

        assertEquals(listOf(ToastType.LOADING, ToastType.SUCCESS), toasts.map { it.type })
        val id = toasts.first().id
        assertNotNull(id)
        assertEquals(id, toasts.last().id)
    }

    /**
     * The icon gallery story is the only story of its category and renders.
     */
    @Test
    fun `the icon gallery story renders`() = assertCategory(StoryCategory.ICONS, setOf("icons"))

    /**
     * The storybook has a story for every component of the shadcn registry the screen framework
     * implements, every category has a story, and no key repeats.
     */
    @Test
    fun `the stories cover the shadcn components`() {
        val stories = storybook().stories
        assertEquals(stories.size, stories.map { it.key }.toSet().size)
        assertEquals(emptySet(), SHADCN_COMPONENTS - stories.map { it.key }.toSet())
        StoryCategory.entries.forEach { category -> assertTrue(stories.any { it.category == category }, category.title) }
    }

    /**
     * The first gallery page shows the first 96 icons in rows of 8, the last page the rest, and
     * the next link is disabled on the last page.
     */
    @Test
    fun `the gallery pages through the icons`() {
        val page = storybook()
        page.storyKey = "icons"

        val first = shownIcons(page)
        assertEquals(IconPaging.PAGE_SIZE, first.size)
        assertEquals("icon-000", first.first())
        val rows = elements(definition(page).root).filterIsInstance<RowElement>().filter { row -> row.children.any { cell -> elements(cell).any { it is IconElement } } }
        assertTrue(rows.all { it.children.size <= 8 })
        assertEquals(12, rows.size)

        val session = open(page)
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "icon_next")))
        assertEquals(1, page.iconPage)
        assertEquals((96 until 100).map { "icon-%03d".format(it) }, shownIcons(page))
        val next = elements(definition(page).root).filterIsInstance<PaginationNextElement>().single()
        assertEquals(false, next.enabled)
    }

    /**
     * The gallery grid takes the width of the content area: the grid, every row and every cell
     * grow, every row has the same number of cells so the columns line up, and the last row is
     * filled up with empty cells.
     */
    @Test
    fun `the gallery grid fits the content width`() {
        val page = storybook()
        page.storyKey = "icons"
        page.iconPage = 1
        val root = definition(page).root
        val rows = elements(root).filterIsInstance<RowElement>().filter { row -> row.children.any { cell -> elements(cell).any { it is IconElement } } }
        val grid = elements(root).filterIsInstance<ContainerElement>().single { it.children.isNotEmpty() && it.children.all { child -> child in rows } }

        assertEquals(ElementSize.grow(), (grid as ColumnElement).width)
        assertTrue(rows.all { it.width == ElementSize.grow() })
        assertEquals(listOf(8), rows.map { it.children.size }.distinct())
        assertTrue(rows.flatMap { it.children }.all { (it as ColumnElement).width == ElementSize.grow() })
        assertEquals((96 until 100).map { "icon-%03d".format(it) }, shownIcons(page))
    }

    /**
     * Typing a search resets the page and filters by name, and the category select filters by
     * category.
     */
    @Test
    fun `the gallery searches and filters by category`() {
        val page = storybook()
        page.storyKey = "icons"
        page.iconPage = 1
        val session = open(page)

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChangePacket(session, "icon_search", "icon-01")))
        assertEquals(0, page.iconPage)
        assertEquals((10 until 20).map { "icon-%03d".format(it) }, shownIcons(page))

        val filtered = open(page)
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChangePacket(filtered, "icon_category", "odd")))
        assertEquals((11 until 20 step 2).map { "icon-%03d".format(it) }, shownIcons(page))
    }

    /**
     * The category select and the pagination of the gallery report their element ids; the search
     * input does not.
     */
    @Test
    fun `the gallery reports the category select and the pagination but not the search`() {
        val page = storybook()
        page.storyKey = "icons"
        val session = open(page)

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChangePacket(session, "icon_search", "icon")))
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "icon_next")))
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChangePacket(session, "icon_category", "odd")))

        assertEquals(listOf("Symbole: icon_next", "Symbole: icon_category"), reports)
    }

    /**
     * A search that matches nothing shows the empty state and no icons.
     */
    @Test
    fun `the gallery shows the empty state for a search without matches`() {
        val page = storybook()
        page.storyKey = "icons"
        page.iconQuery = "zzz"

        assertEquals(emptyList(), shownIcons(page))
        assertTrue("Keine Symbole gefunden" in texts(definition(page)))
        open(page)
        assertIs<ScreenOpen>(sent.last())
    }

    /**
     * The gallery lists the bundled Lucide index.
     */
    @Test
    fun `the gallery shows the bundled icons`() {
        val bundled = LucideCatalog.load()
        assertTrue(bundled.icons.size > IconPaging.PAGE_SIZE)
        val page = storybook(bundled)
        page.storyKey = "icons"

        assertEquals(bundled.icons.take(IconPaging.PAGE_SIZE).map { it.name }, shownIcons(page))
        open(page)
        assertIs<ScreenOpen>(sent.last())
    }

    /**
     * A handler binder that returns, for each element and kind, a handler that compares equal
     * across renders and runs the handler of the newest render, as the GUI runtime does.
     */
    private class StableBinder : HandlerBinder {
        /**
         * The click handlers of the newest render, keyed by element id.
         */
        val buttons = HashMap<String, ButtonHandler>()

        /**
         * The change handlers of the newest render, keyed by element id.
         */
        val changes = HashMap<String, ChangeHandler>()

        /**
         * The search handlers of the newest render, keyed by element id.
         */
        val searches = HashMap<String, SearchHandler>()

        /**
         * Stores a click handler and returns a stable one.
         *
         * @param elementId the element id
         * @param handler the handler
         * @return the stable handler
         */
        override fun button(elementId: String, handler: ButtonHandler): ButtonHandler {
            buttons[elementId] = handler
            return Stable(elementId, "button", this)
        }

        /**
         * Stores a change handler and returns a stable one.
         *
         * @param elementId the element id
         * @param handler the handler
         * @return the stable handler
         */
        override fun change(elementId: String, handler: ChangeHandler): ChangeHandler {
            changes[elementId] = handler
            return Stable(elementId, "change", this)
        }

        /**
         * Stores a search handler and returns a stable one.
         *
         * @param elementId the element id
         * @param handler the handler
         * @return the stable handler
         */
        override fun search(elementId: String, handler: SearchHandler): SearchHandler {
            searches[elementId] = handler
            return Stable(elementId, "search", this)
        }

        /**
         * Renders a page with this binder.
         *
         * @param page the page
         * @return the root of the rendered tree
         */
        fun render(page: StorybookPage): ScreenElement = renderRoot(this) { with(page) { render() } }
    }

    /**
     * A handler that runs the newest handler of its element; equal to every stable handler of the
     * same element and kind.
     *
     * @property elementId the element id
     * @property kind the kind of handler
     * @property binder the binder holding the newest handlers
     */
    private class Stable(val elementId: String, val kind: String, val binder: StableBinder) : ButtonHandler, ChangeHandler, SearchHandler {
        /**
         * Runs the newest click handler.
         *
         * @param click the click
         */
        override fun onClick(click: ScreenClick) = binder.buttons.getValue(elementId).onClick(click)

        /**
         * Runs the newest change handler.
         *
         * @param change the change
         */
        override fun onChange(change: ScreenInputChange) = binder.changes.getValue(elementId).onChange(change)

        /**
         * Runs the newest search handler.
         *
         * @param search the search
         */
        override fun onSearch(search: ScreenSearch) = binder.searches.getValue(elementId).onSearch(search)

        /**
         * Returns whether [other] is a stable handler of the same element and kind.
         *
         * @param other the other object
         * @return whether both are equal
         */
        override fun equals(other: Any?): Boolean = other is Stable && other.elementId == elementId && other.kind == kind

        /**
         * Returns a hash of the element id and kind.
         *
         * @return the hash
         */
        override fun hashCode(): Int = 31 * elementId.hashCode() + kind.hashCode()
    }

    /**
     * The reactive state story shows a counter and an input whose text is echoed below it.
     */
    @Test
    fun `the reactive state story renders the counter and the echo`() {
        val page = storybook()
        page.storyKey = "state"
        val texts = texts(definition(page))

        assertEquals("Reaktiver Zustand", page.stories.single { it.key == "state" }.name)
        assertTrue("0" in texts)
        assertTrue("Noch kein Name eingegeben." in texts)
        open(page)
        assertIs<ScreenOpen>(sent.last())
    }

    /**
     * The counter buttons change only the shown value, the input's text is echoed without
     * touching the input, and reset sets the counter back to zero.
     */
    @Test
    fun `the reactive state story patches only the changed elements`() {
        val page = storybook()
        page.storyKey = "state"
        val binder = StableBinder()
        val screen = state.open(definition(page), null)
        fun click(id: String) = binder.buttons.getValue(id).onClick(ScreenClick(screen, id, ScreenValues(emptyMap())))

        val start = binder.render(page)
        click("state_increment")
        val incremented = binder.render(page)
        val changes = ScreenDiff.diff(start, incremented)
        assertEquals(listOf("state_count"), changes.map { (it as ScreenChange.Replace).targetId })
        assertEquals("1", PlainTextComponentSerializer.plainText().serialize((changes.single() as ScreenChange.Replace).element.let { it as TextElement }.text))

        click("state_decrement")
        click("state_decrement")
        assertEquals(-1, page.stateCount)
        click("state_reset")
        assertEquals(0, page.stateCount)

        val before = binder.render(page)
        binder.changes.getValue("state_name").onChange(ScreenInputChange(screen, "state_name", "Max", ScreenValues(mapOf("state_name" to "Max"))))
        val echoed = ScreenDiff.diff(before, binder.render(page), mapOf("state_name" to "Max"))
        assertEquals(listOf("state_echo"), echoed.map { (it as ScreenChange.Replace).targetId })
        assertEquals("Hallo, Max!", PlainTextComponentSerializer.plainText().serialize(((echoed.single() as ScreenChange.Replace).element as TextElement).text))
    }

    /**
     * The page window keeps the first, the last and the neighbours of the current page, with gaps
     * between them.
     */
    @Test
    fun `the page window keeps the ends and the neighbours`() {
        assertEquals(listOf(0), pageWindow(0, 1))
        assertEquals(listOf(0, 1, null, 9), pageWindow(0, 10))
        assertEquals(listOf(0, null, 4, 5, 6, null, 9), pageWindow(5, 10))
        assertEquals(listOf(0, null, 8, 9), pageWindow(9, 10))
        assertEquals(listOf(0, 1, 2, 3), pageWindow(1, 4))
    }

    /**
     * Every test story renders and maps in every theme and variant.
     */
    @Test
    fun `the page renders every story in every theme and variant`() {
        val page = StorybookPage(UUID.randomUUID(), reports::add) { testStories }
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
     * The storybook fills the window height: its root grows in height, and the story list of the
     * sidebar and the story content each sit in a scroll area that takes the remaining height, so
     * that the sidebar header and the story header stay in place.
     */
    @Test
    fun `only the story list and the story content scroll`() {
        val page = StorybookPage(UUID.randomUUID(), reports::add) { testStories }
        val root = definition(page).root
        assertEquals(ElementSize.grow(), (root as SidebarProviderElement).height)

        val sidebar = elements(root).filterIsInstance<SidebarElement>().single()
        assertIs<SidebarHeaderElement>(sidebar.children.first())
        val list = assertIs<ScrollAreaElement>(sidebar.children.last())
        assertEquals(ElementSize.grow(), list.height)
        assertIs<SidebarContentElement>(list.children.single())

        val content = elements(root).filterIsInstance<ScrollAreaElement>().single { it.id == "storybook_content_button" }
        assertEquals(ElementSize.grow(), content.height)
    }

    /**
     * Each story has its own content scroll area, so that switching stories starts at the top.
     */
    @Test
    fun `each story has its own content scroll area`() {
        val page = StorybookPage(UUID.randomUUID(), reports::add) { testStories }
        val first = elements(definition(page).root).filterIsInstance<ScrollAreaElement>().map { it.id }.single { it.startsWith("storybook_content") }
        page.storyKey = testStories.last().key
        val second = elements(definition(page).root).filterIsInstance<ScrollAreaElement>().map { it.id }.single { it.startsWith("storybook_content") }
        assertNotEquals(first, second)
    }

    /**
     * The story name and key in the header take the width the controls leave and stay on one
     * line, and the theme select below keeps its fixed width.
     */
    @Test
    fun `the header clamps the story name to one line`() {
        val page = StorybookPage(UUID.randomUUID(), reports::add) { testStories }
        val all = elements(definition(page).root)
        val name = all.filterIsInstance<TextElement>().single { PlainTextComponentSerializer.plainText().serialize(it.text) == "Schaltfläche" && it.kind == TextKind.H2 }
        val key = all.filterIsInstance<TextElement>().single { PlainTextComponentSerializer.plainText().serialize(it.text) == "button" }

        assertEquals(1, name.maxLines)
        assertEquals(ElementSize.grow(), name.width)
        assertEquals(1, key.maxLines)
        assertEquals(ElementSize.grow(), key.width)
        assertEquals(ElementSize.fixed(120), all.filterIsInstance<SelectElement>().single { it.id == "storybook_theme" }.width)
        val nameRow = all.filterIsInstance<RowElement>().single { row -> elements(row).any { it === name } && elements(row).none { it is SelectElement } }
        assertTrue(elements(nameRow).none { it is SwitchElement })
    }

    /**
     * The sidebar header shows the title while the sidebar is expanded and only an icon while it
     * is collapsed to icons.
     */
    @Test
    fun `the collapsed sidebar header shows an icon instead of the title`() {
        val page = StorybookPage(UUID.randomUUID(), reports::add) { testStories }
        fun header() = elements(definition(page).root).filterIsInstance<SidebarHeaderElement>().single()

        assertEquals(listOf("Storybook"), header().children.filterIsInstance<TextElement>().map { PlainTextComponentSerializer.plainText().serialize(it.text) })
        val session = open(page)
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChangePacket(session, "storybook", "false")))

        assertEquals(false, page.sidebarOpen)
        assertEquals(emptyList(), header().children.filterIsInstance<TextElement>())
        assertEquals(1, header().children.filterIsInstance<IconElement>().size)
        assertEquals(false, (definition(page).root as SidebarProviderElement).open)
    }

    /**
     * The sidebar highlights the shown story, and a click on another story shows it.
     */
    @Test
    fun `a sidebar click selects a story and highlights it`() {
        val page = StorybookPage(UUID.randomUUID(), reports::add) { testStories }
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
        val page = StorybookPage(UUID.randomUUID(), reports::add) { testStories }
        val session = open(page)

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "go")))

        assertEquals(listOf("Schaltfläche: go"), reports)
    }

    /**
     * Change reports of one element are throttled to one per two seconds, per element; click
     * reports are not throttled.
     */
    @Test
    fun `change reports are throttled per element and clicks are not`() {
        var now = 0L
        val stories = listOf(
            Story("input", "Eingabefeld", StoryCategory.INPUTS) { context ->
                Column {
                    Input(id = "name", onChange = context.changed)
                    Input(id = "city", onChange = context.changed)
                    Button("Los", id = "go", onClick = context.clicked)
                }
            },
        )
        val page = StorybookPage(UUID.randomUUID(), reports::add, clock = { now }) { stories }
        val session = open(page)

        fun change(id: String) = assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChangePacket(session, id, "x$now")))
        fun click() = assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "go")))

        change("name")
        now = 1_000
        change("name")
        change("city")
        click()
        click()
        now = 1_999
        change("name")
        now = 2_000
        change("name")

        assertEquals(
            listOf("Eingabefeld: name", "Eingabefeld: city", "Eingabefeld: go", "Eingabefeld: go", "Eingabefeld: name"),
            reports,
        )
    }

    /**
     * The theme select and the dark switch change the page's theme and variant.
     */
    @Test
    fun `the theme select and the dark switch change theme and variant`() {
        val page = StorybookPage(UUID.randomUUID(), reports::add) { testStories }
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
