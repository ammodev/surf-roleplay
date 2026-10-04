package dev.slne.surf.roleplay.paper.storybook

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ChangeHandler
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
import dev.slne.surf.roleplay.api.client.common.screen.dsl.H4
import dev.slne.surf.roleplay.paper.storybook.stories.DISPLAY_STORIES
import dev.slne.surf.roleplay.paper.storybook.stories.INPUT_STORIES
import dev.slne.surf.roleplay.paper.storybook.stories.NAVIGATION_STORIES
import dev.slne.surf.roleplay.paper.storybook.stories.OVERLAY_STORIES
import java.util.UUID

/**
 * One page of the storybook: a component shown in every variant, size and state it offers.
 *
 * @property key the registry name of the component, such as `button`, unique in the storybook
 * @property name the German name shown in the sidebar and above the story
 * @property category the sidebar group the story is listed in
 * @property render adds the story's elements to the scrollable content area
 */
data class Story(val key: String, val name: String, val category: StoryCategory, val render: ComponentScope.(StoryContext) -> Unit)

/**
 * The sidebar groups of the storybook, in sidebar order.
 *
 * @property title the German group title
 * @property icon the Lucide icon of the group's menu buttons
 */
enum class StoryCategory(val title: String, val icon: String) {
    /**
     * Components the player types, picks or clicks in.
     */
    INPUTS("Eingaben", "text-cursor-input"),

    /**
     * Components that only show content.
     */
    DISPLAY("Anzeige", "layout-template"),

    /**
     * Components shown above the screen, such as dialogs, menus and toasts.
     */
    OVERLAYS("Overlays und Menüs", "layers"),

    /**
     * Components that move between content or arrange it.
     */
    NAVIGATION("Navigation und Layout", "panels-top-left"),

    /**
     * Tables, charts and the chat view.
     */
    DATA("Daten und Diagramme", "chart-column"),

    /**
     * The gallery of the bundled Lucide icons.
     */
    ICONS("Symbole", "shapes"),
}

/**
 * What a story needs from the storybook while it renders.
 *
 * @property playerId the UUID of the viewing player, whose face avatars can show
 * @property report reports a click on or a change of the element with the given id
 */
class StoryContext(val playerId: UUID, val report: (String) -> Unit) {
    /**
     * A click handler that reports the id of the clicked element.
     */
    val clicked: ButtonHandler = ButtonHandler { click -> report(click.buttonId) }

    /**
     * A change handler that reports the id of the changed input.
     */
    val changed: ChangeHandler = ChangeHandler { change -> report(change.inputId) }
}

/**
 * Returns every story of the storybook, grouped by category in sidebar order.
 *
 * @return the stories
 */
internal fun storybookStories(): List<Story> = INPUT_STORIES + DISPLAY_STORIES + OVERLAY_STORIES + NAVIGATION_STORIES

/**
 * Adds a titled block of a story, such as all variants of a component.
 *
 * @param title the German heading of the block
 * @param content the builder of the block's elements
 */
internal fun ComponentScope.storySection(title: String, content: ComponentScope.() -> Unit) {
    Column(width = ElementSize.grow(), gap = 6, crossAlign = Alignment.START) {
        H4(title)
        content()
    }
}

/**
 * Returns the lower-case name of an enum constant, such as `icon_sm` for `ICON_SM`, used as a
 * label and id part for variants and sizes.
 *
 * @return the name
 */
internal fun Enum<*>.slug(): String = name.lowercase()
