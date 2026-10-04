package dev.slne.surf.roleplay.paper.storybook.stories

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.ButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Input
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Large
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Muted
import dev.slne.surf.roleplay.api.client.common.screen.dsl.P
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Row
import dev.slne.surf.roleplay.paper.storybook.Story
import dev.slne.surf.roleplay.paper.storybook.StoryCategory
import dev.slne.surf.roleplay.paper.storybook.StoryContext
import dev.slne.surf.roleplay.paper.storybook.storySection
import net.kyori.adventure.text.Component

/**
 * The state the reactive state story shows and changes. Writing a property re-renders the page
 * that holds it.
 */
interface ReactiveStoryState {
    /**
     * The value of the counter.
     */
    var stateCount: Int

    /**
     * The text of the state-bound input.
     */
    var stateName: String
}

/**
 * Creates the story that shows how a GUI page re-renders from its state: a counter with buttons
 * that change it and a reset, and an input whose text is stored in the state and echoed below
 * it. Every change re-renders the page, and only the elements that changed are sent. Clicks and
 * changes are reported like those of every story.
 *
 * @param state the counter and text the story shows and changes
 * @return the story
 */
fun reactiveStateStory(state: ReactiveStoryState): Story =
    Story("state", "Reaktiver Zustand", StoryCategory.INPUTS) { context -> reactiveState(state, context) }

/**
 * Adds the counter and the bound input with its echo.
 *
 * @param state the counter and text
 * @param context the story context the buttons and the input report to
 */
private fun ComponentScope.reactiveState(state: ReactiveStoryState, context: StoryContext) {
    Muted("Jede Änderung des Zustands rendert die Seite neu; gesendet werden nur die geänderten Elemente.", width = ElementSize.grow())
    storySection("Zähler") {
        Row(gap = 6, crossAlign = Alignment.CENTER) {
            Button("", icon = "minus", variant = ButtonVariant.OUTLINE, size = ButtonSize.ICON, id = "state_decrement") { click ->
                state.stateCount--
                context.clicked.onClick(click)
            }
            Large("${state.stateCount}", id = "state_count")
            Button("", icon = "plus", variant = ButtonVariant.OUTLINE, size = ButtonSize.ICON, id = "state_increment") { click ->
                state.stateCount++
                context.clicked.onClick(click)
            }
            Button("Zurücksetzen", icon = "rotate-ccw", variant = ButtonVariant.SECONDARY, id = "state_reset") { click ->
                state.stateCount = 0
                context.clicked.onClick(click)
            }
        }
    }
    storySection("Gebundene Eingabe") {
        Input(value = state.stateName, placeholder = Component.text("Name eingeben …"), maxLength = 32, width = ElementSize.fixed(200), id = "state_name") { change ->
            state.stateName = change.value
            context.changed.onChange(change)
        }
        P(if (state.stateName.isEmpty()) "Noch kein Name eingegeben." else "Hallo, ${state.stateName}!", id = "state_echo")
    }
}
