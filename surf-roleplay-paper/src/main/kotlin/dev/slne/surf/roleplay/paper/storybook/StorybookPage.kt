package dev.slne.surf.roleplay.paper.storybook

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.EmptyMediaVariant
import dev.slne.surf.roleplay.api.client.common.screen.ScreenThemes
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoice
import dev.slne.surf.roleplay.api.client.common.screen.SidebarCollapsible
import dev.slne.surf.roleplay.api.client.common.screen.Spacing
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Empty
import dev.slne.surf.roleplay.api.client.common.screen.dsl.EmptyHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.EmptyMedia
import dev.slne.surf.roleplay.api.client.common.screen.dsl.EmptyTitle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.H2
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Icon
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Label
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Large
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Muted
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Row
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ScrollArea
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Select
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Separator
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Sidebar
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarGroupContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarGroupLabel
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarInset
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarMenu
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarMenuButton
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarMenuItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarProvider
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarTrigger
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Switch
import dev.slne.surf.roleplay.api.client.paper.screen.gui.GuiPage
import net.kyori.adventure.text.Component
import java.util.UUID

/**
 * The themes offered by the storybook's theme select.
 */
private val STORYBOOK_THEMES = listOf(
    SelectChoice(ScreenThemes.DEFAULT, Component.text("Standard")),
    SelectChoice(ScreenThemes.SAR, Component.text("Rettungsdienst")),
    SelectChoice(ScreenThemes.POLICE, Component.text("Polizei")),
)

/**
 * The fullscreen storybook: a sidebar that lists the stories by category and a content area that
 * shows the selected story below a header with its name, a theme select and a light/dark switch.
 *
 * Every click on or change of an element of a story is reported as `<story name>: <element id>`.
 * Changes of one element are reported at most once per [CHANGE_REPORT_INTERVAL_MILLIS]; clicks
 * are always reported.
 * The ids of the page's own elements start with `storybook_`.
 *
 * The page also holds the search, category filter and page of the icon gallery, and the counter
 * and text of the reactive state story.
 *
 * @property playerId the UUID of the viewing player, passed on to the stories
 * @property report shows a report of a click or change to the player
 * @property clock returns the current time in milliseconds, used to throttle change reports
 * @param stories creates the stories, in sidebar order within their category, from the story
 *        state the page holds
 */
class StorybookPage(
    private val playerId: UUID,
    private val report: (String) -> Unit,
    private val clock: () -> Long = System::currentTimeMillis,
    stories: (StorybookState) -> List<Story>,
) : GuiPage(), StorybookState {

    /**
     * The time of the last change report per story key and element id.
     */
    private val lastChangeReports = HashMap<String, Long>()

    /**
     * The text the icon gallery searches for.
     */
    override var iconQuery: String by state("")

    /**
     * The category the icon gallery is filtered by, or `null` for any.
     */
    override var iconCategory: String? by state(null)

    /**
     * The shown page of the icon gallery.
     */
    override var iconPage: Int by state(0)

    /**
     * The value of the reactive state story's counter.
     */
    override var stateCount: Int by state(0)

    /**
     * The text of the reactive state story's input.
     */
    override var stateName: String by state("")

    /**
     * The stories, in sidebar order within their category.
     */
    internal val stories: List<Story> = stories(this)

    /**
     * The key of the shown story, or `null` if there are no stories.
     */
    internal var storyKey: String? by state(this.stories.firstOrNull()?.key)

    /**
     * The name of the theme the storybook is drawn with.
     */
    internal var themeName: String by state(ScreenThemes.DEFAULT)

    /**
     * Whether the storybook is drawn in the dark variant of its theme.
     */
    internal var dark: Boolean by state(true)

    /**
     * Whether the storybook's sidebar is expanded, as the player last set it.
     */
    internal var sidebarOpen: Boolean by state(true)

    /**
     * The title of the storybook's screen.
     */
    override val title: Component = Component.text("Storybook")

    /**
     * The theme chosen with the theme select.
     */
    override val theme: String get() = themeName

    /**
     * The variant chosen with the light/dark switch.
     */
    override val variant: ScreenVariant get() = if (dark) ScreenVariant.DARK else ScreenVariant.LIGHT

    /**
     * Renders the sidebar and the content area with the shown story. The page fills the window
     * height; the story list and the story content scroll on their own, so the sidebar header and
     * the story header stay in place. The sidebar header shows the title while the sidebar is
     * expanded and an icon while it is collapsed to icons.
     */
    override fun ComponentScope.render() {
        val shown = stories.firstOrNull { it.key == storyKey }
        SidebarProvider(ElementSize.grow(), ElementSize.grow(), open = sidebarOpen, onChange = { sidebarOpen = it.value == "true" }, id = "storybook") {
            Sidebar(collapsible = SidebarCollapsible.ICON, id = "storybook_sidebar") {
                SidebarHeader {
                    if (sidebarOpen) Large("Storybook", maxLines = 1, id = "storybook_title") else Icon("book-open", id = "storybook_logo")
                }
                ScrollArea(ElementSize.grow(), ElementSize.grow(), id = "storybook_stories") {
                    SidebarContent {
                        StoryCategory.entries.forEach { category -> categoryGroup(category, shown) }
                    }
                }
            }
            SidebarInset {
                Column(ElementSize.grow(), ElementSize.grow(), gap = 8, padding = Spacing(8, 8, 8, 8), crossAlign = Alignment.STRETCH) {
                    header(shown)
                    Separator()
                    ScrollArea(ElementSize.grow(), ElementSize.grow(), id = "storybook_content_${shown?.key.orEmpty()}") {
                        Column(width = ElementSize.grow(), gap = 12, padding = Spacing(0, 8, 8, 0), crossAlign = Alignment.STRETCH) {
                            if (shown == null) noStories() else shown.render(this, storyContext(shown))
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates the context a story renders with: clicks are reported at once, changes of one
     * element at most once per [CHANGE_REPORT_INTERVAL_MILLIS].
     *
     * @param story the story
     * @return the context
     */
    private fun storyContext(story: Story): StoryContext = StoryContext(
        playerId,
        report = { elementId -> report("${story.name}: $elementId") },
        reportChange = { elementId ->
            val key = "${story.key}/$elementId"
            val now = clock()
            val last = lastChangeReports[key]
            if (last == null || now - last >= CHANGE_REPORT_INTERVAL_MILLIS) {
                lastChangeReports[key] = now
                report("${story.name}: $elementId")
            }
        },
    )

    /**
     * Holds the throttle interval of change reports.
     */
    companion object {
        /**
         * The shortest time between two change reports of the same element, in milliseconds.
         */
        const val CHANGE_REPORT_INTERVAL_MILLIS: Long = 2_000
    }

    /**
     * Adds the sidebar group of one category with a menu button per story. The button of the shown
     * story is highlighted; a click shows its story.
     *
     * @param category the category
     * @param shown the shown story, or `null` for none
     */
    private fun ComponentScope.categoryGroup(category: StoryCategory, shown: Story?) {
        val inCategory = stories.filter { it.category == category }
        if (inCategory.isEmpty()) return
        SidebarGroup {
            SidebarGroupLabel(category.title)
            SidebarGroupContent {
                SidebarMenu {
                    inCategory.forEach { story ->
                        SidebarMenuItem {
                            SidebarMenuButton(
                                story.name,
                                icon = category.icon,
                                active = story == shown,
                                tooltip = Component.text(story.name),
                                id = "storybook_story_${story.key}",
                            ) { storyKey = story.key }
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds the header row: the sidebar trigger, the name and key of the shown story, the theme
     * select and the light/dark switch. The name and key take the width the controls leave and
     * are shortened to one line each.
     *
     * @param shown the shown story, or `null` for none
     */
    private fun ComponentScope.header(shown: Story?) {
        Row(width = ElementSize.grow(), gap = 8, crossAlign = Alignment.CENTER) {
            SidebarTrigger()
            Column(width = ElementSize.grow(), gap = 2) {
                H2(shown?.name ?: "Storybook", maxLines = 1, width = ElementSize.grow())
                if (shown != null) Muted(shown.key, maxLines = 1, width = ElementSize.grow())
            }
            Select(STORYBOOK_THEMES, selected = themeName, width = ElementSize.fixed(120), id = "storybook_theme") { change -> themeName = change.value }
            Switch(checked = dark, id = "storybook_dark") { change -> dark = change.value == "true" }
            Label("Dunkel", forId = "storybook_dark")
        }
    }

    /**
     * Adds the empty state shown when the storybook has no stories.
     */
    private fun ComponentScope.noStories() {
        Empty {
            EmptyHeader {
                EmptyMedia(EmptyMediaVariant.ICON, icon = "book-open")
                EmptyTitle("Keine Komponenten vorhanden")
            }
        }
    }
}
