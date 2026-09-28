package dev.slne.surf.roleplay.paper.screen.debug

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.ElementsBuilder
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.ScreenThemes
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoice
import dev.slne.surf.roleplay.api.client.common.screen.SidebarCollapsible
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuSubButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.SidebarSide
import dev.slne.surf.roleplay.api.client.common.screen.SidebarVariant
import dev.slne.surf.roleplay.api.client.common.screen.TextKind
import dev.slne.surf.roleplay.api.client.common.screen.screen
import dev.slne.surf.roleplay.api.client.common.screen.select
import dev.slne.surf.roleplay.api.client.common.screen.sidebar
import dev.slne.surf.roleplay.api.client.common.screen.sidebarContent
import dev.slne.surf.roleplay.api.client.common.screen.sidebarFooter
import dev.slne.surf.roleplay.api.client.common.screen.sidebarGroup
import dev.slne.surf.roleplay.api.client.common.screen.sidebarGroupAction
import dev.slne.surf.roleplay.api.client.common.screen.sidebarGroupContent
import dev.slne.surf.roleplay.api.client.common.screen.sidebarGroupLabel
import dev.slne.surf.roleplay.api.client.common.screen.sidebarHeader
import dev.slne.surf.roleplay.api.client.common.screen.sidebarInput
import dev.slne.surf.roleplay.api.client.common.screen.sidebarInset
import dev.slne.surf.roleplay.api.client.common.screen.sidebarMenu
import dev.slne.surf.roleplay.api.client.common.screen.sidebarMenuAction
import dev.slne.surf.roleplay.api.client.common.screen.sidebarMenuBadge
import dev.slne.surf.roleplay.api.client.common.screen.sidebarMenuButton
import dev.slne.surf.roleplay.api.client.common.screen.sidebarMenuItem
import dev.slne.surf.roleplay.api.client.common.screen.sidebarMenuSkeleton
import dev.slne.surf.roleplay.api.client.common.screen.sidebarMenuSub
import dev.slne.surf.roleplay.api.client.common.screen.sidebarMenuSubButton
import dev.slne.surf.roleplay.api.client.common.screen.sidebarMenuSubItem
import dev.slne.surf.roleplay.api.client.common.screen.sidebarProvider
import dev.slne.surf.roleplay.api.client.common.screen.sidebarRail
import dev.slne.surf.roleplay.api.client.common.screen.sidebarSeparator
import dev.slne.surf.roleplay.api.client.common.screen.sidebarTrigger
import dev.slne.surf.roleplay.api.client.common.screen.text
import dev.slne.surf.roleplay.api.client.paper.screen.ScreenService
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

/**
 * The page of the screen debug command that shows the navigation and layout components. Clicks
 * and changes are reported in chat.
 */
object NavigationDemo {

    /**
     * How the sidebar of the page is set up.
     *
     * @property side the side the sidebar is on
     * @property variant how the sidebar is drawn
     * @property collapsible how the sidebar collapses
     */
    data class SidebarSetup(
        val side: SidebarSide = SidebarSide.LEFT,
        val variant: SidebarVariant = SidebarVariant.SIDEBAR,
        val collapsible: SidebarCollapsible = SidebarCollapsible.ICON,
    )

    /**
     * What the page needs from its surroundings.
     *
     * @property report sends a report of a click or change to the player
     * @property reopen opens the page again with another theme, variant and sidebar setup
     */
    class Hooks(
        val report: (Component) -> Unit,
        val reopen: (String, ScreenVariant, SidebarSetup) -> Unit,
    )

    /**
     * Opens the page.
     *
     * @param player the player
     * @param theme the name of the theme to draw the page with
     * @param variant the light or dark variant of the theme
     * @param setup how the sidebar is set up
     */
    fun open(player: Player, theme: String = ScreenThemes.DEFAULT, variant: ScreenVariant = ScreenVariant.DARK, setup: SidebarSetup = SidebarSetup()) {
        val hooks = Hooks(
            report = player::sendMessage,
            reopen = { chosenTheme, chosenVariant, chosenSetup -> open(player, chosenTheme, chosenVariant, chosenSetup) },
        )
        ScreenService.open(player, definition(hooks, theme, variant, setup))
    }

    /**
     * Builds the page.
     *
     * @param hooks what the page needs from its surroundings
     * @param theme the name of the theme
     * @param variant the variant of the theme
     * @param setup how the sidebar is set up
     * @return the page
     */
    fun definition(hooks: Hooks, theme: String, variant: ScreenVariant, setup: SidebarSetup): ScreenDefinition {
        val clicked = ButtonHandler { click -> hooks.report(Component.text("${click.buttonId} geklickt", NamedTextColor.GREEN)) }
        return screen(Component.text("Navigation")) {
            this.theme = theme
            this.variant = variant
            column("root", width = ElementSize.fixed(620), gap = 12, crossAlign = Alignment.STRETCH) {
                row("theme_row", gap = 4, crossAlign = Alignment.CENTER) {
                    select("theme", InputsDemo.THEMES, selected = theme, width = ElementSize.grow(), onChange = { change ->
                        hooks.reopen(change.value, variant, setup)
                    })
                    select("variant", InputsDemo.VARIANTS, selected = variant.name, width = ElementSize.fixed(90), onChange = { change ->
                        hooks.reopen(theme, ScreenVariant.valueOf(change.value), setup)
                    })
                }
                sidebars(hooks, clicked, theme, variant, setup)
            }
        }
    }

    /**
     * Adds a section with a heading.
     *
     * @param id the id of the section
     * @param title the heading
     * @param content the builder of the section's content
     */
    private fun ElementsBuilder.section(id: String, title: String, content: ElementsBuilder.() -> Unit) {
        column(id, gap = 6, crossAlign = Alignment.STRETCH) {
            text("${id}_title", Component.text(title), TextKind.H3)
            content()
        }
    }

    /**
     * Adds a sidebar layout with selects for its side, variant and collapsing, and a menu with
     * every sidebar part.
     *
     * @param hooks what the page needs from its surroundings
     * @param clicked the handler that reports clicks
     * @param theme the name of the theme
     * @param variant the variant of the theme
     * @param setup how the sidebar is set up
     */
    private fun ElementsBuilder.sidebars(hooks: Hooks, clicked: ButtonHandler, theme: String, variant: ScreenVariant, setup: SidebarSetup) = section("sidebars", "Sidebar") {
        row("sidebar_setup", gap = 4, crossAlign = Alignment.CENTER) {
            select("sidebar_side", SIDES, selected = setup.side.name, width = ElementSize.grow(), onChange = { change ->
                hooks.reopen(theme, variant, setup.copy(side = SidebarSide.valueOf(change.value)))
            })
            select("sidebar_variant", VARIANTS, selected = setup.variant.name, width = ElementSize.grow(), onChange = { change ->
                hooks.reopen(theme, variant, setup.copy(variant = SidebarVariant.valueOf(change.value)))
            })
            select("sidebar_collapsible", COLLAPSIBLES, selected = setup.collapsible.name, width = ElementSize.grow(), onChange = { change ->
                hooks.reopen(theme, variant, setup.copy(collapsible = SidebarCollapsible.valueOf(change.value)))
            })
        }
        sidebarProvider("sidebar_provider", ElementSize.grow(), ElementSize.fixed(320), onChange = { change ->
            val state = if (change.value == "true") "ausgeklappt" else "eingeklappt"
            hooks.report(Component.text("Sidebar $state", NamedTextColor.AQUA))
        }) {
            sidebar("sidebar", side = setup.side, variant = setup.variant, collapsible = setup.collapsible) {
                sidebarHeader("sidebar_header") {
                    sidebarMenu("sidebar_header_menu") {
                        sidebarMenuItem("sidebar_org_item") {
                            sidebarMenuButton("sidebar_org", Component.text("Leitstelle"), icon = "radio-tower", size = SidebarMenuButtonSize.LG, tooltip = Component.text("Leitstelle"), onClick = clicked)
                        }
                    }
                    sidebarInput("sidebar_search", Component.text("Suchen..."))
                }
                sidebarSeparator("sidebar_separator")
                sidebarContent("sidebar_content") {
                    sidebarGroup("sidebar_group_main") {
                        sidebarGroupLabel("sidebar_group_main_label", Component.text("Dienst"))
                        sidebarGroupAction("sidebar_group_main_action", onClick = clicked)
                        sidebarGroupContent("sidebar_group_main_content") {
                            sidebarMenu("sidebar_menu_main") {
                                sidebarMenuItem("sidebar_home_item") {
                                    sidebarMenuButton("sidebar_home", Component.text("Übersicht"), icon = "house", active = true, tooltip = Component.text("Übersicht"), onClick = clicked)
                                }
                                sidebarMenuItem("sidebar_calls_item") {
                                    sidebarMenuButton("sidebar_calls", Component.text("Einsätze"), icon = "siren", tooltip = Component.text("Einsätze"), onClick = clicked)
                                    sidebarMenuBadge("sidebar_calls_badge", Component.text("12"))
                                }
                                sidebarMenuItem("sidebar_units_item") {
                                    sidebarMenuButton("sidebar_units", Component.text("Einheiten"), icon = "ambulance", tooltip = Component.text("Einheiten"), onClick = clicked)
                                    sidebarMenuAction("sidebar_units_action", showOnHover = true, onClick = clicked)
                                    sidebarMenuSub("sidebar_units_sub") {
                                        sidebarMenuSubItem("sidebar_units_rtw_item") {
                                            sidebarMenuSubButton("sidebar_units_rtw", Component.text("RTW 1"), active = true, onClick = clicked)
                                        }
                                        sidebarMenuSubItem("sidebar_units_nef_item") {
                                            sidebarMenuSubButton("sidebar_units_nef", Component.text("NEF 2"), onClick = clicked)
                                        }
                                        sidebarMenuSubItem("sidebar_units_ktw_item") {
                                            sidebarMenuSubButton("sidebar_units_ktw", Component.text("KTW 3 (außer Dienst)"), size = SidebarMenuSubButtonSize.SM, enabled = false, onClick = clicked)
                                        }
                                    }
                                }
                                sidebarMenuItem("sidebar_archive_item") {
                                    sidebarMenuButton("sidebar_archive", Component.text("Archiv"), icon = "archive", size = SidebarMenuButtonSize.SM, tooltip = Component.text("Archiv"), enabled = false, onClick = clicked)
                                }
                            }
                        }
                    }
                    sidebarGroup("sidebar_group_more") {
                        sidebarGroupLabel("sidebar_group_more_label", Component.text("Weiteres"))
                        sidebarGroupContent("sidebar_group_more_content") {
                            sidebarMenu("sidebar_menu_more") {
                                sidebarMenuItem("sidebar_settings_item") {
                                    sidebarMenuButton("sidebar_settings", Component.text("Einstellungen"), icon = "settings", variant = SidebarMenuButtonVariant.OUTLINE, tooltip = Component.text("Einstellungen"), onClick = clicked)
                                }
                                sidebarMenuItem("sidebar_loading_1") { sidebarMenuSkeleton("sidebar_skeleton_1", showIcon = true) }
                                sidebarMenuItem("sidebar_loading_2") { sidebarMenuSkeleton("sidebar_skeleton_2") }
                            }
                        }
                    }
                }
                sidebarFooter("sidebar_footer") {
                    sidebarMenu("sidebar_footer_menu") {
                        sidebarMenuItem("sidebar_user_item") {
                            sidebarMenuButton("sidebar_user", Component.text("Ammo"), icon = "circle-user", tooltip = Component.text("Ammo"), onClick = clicked)
                            sidebarMenuAction("sidebar_user_action", icon = "log-out", onClick = clicked)
                        }
                    }
                }
                sidebarRail("sidebar_rail")
            }
            sidebarInset("sidebar_inset") {
                row("sidebar_inset_bar", gap = 6, crossAlign = Alignment.CENTER) {
                    sidebarTrigger("sidebar_trigger")
                    text("sidebar_inset_title", Component.text("Übersicht"), TextKind.H4)
                }
                text(
                    "sidebar_inset_text",
                    Component.text("Mit dem Knopf oben links, der Leiste am Rand oder Strg+B klappt die Sidebar ein und aus."),
                    TextKind.MUTED,
                )
            }
        }
    }

    /**
     * The options of the sidebar side select.
     */
    private val SIDES = listOf(
        SelectChoice(SidebarSide.LEFT.name, Component.text("Links")),
        SelectChoice(SidebarSide.RIGHT.name, Component.text("Rechts")),
    )

    /**
     * The options of the sidebar variant select.
     */
    private val VARIANTS = listOf(
        SelectChoice(SidebarVariant.SIDEBAR.name, Component.text("Sidebar")),
        SelectChoice(SidebarVariant.FLOATING.name, Component.text("Schwebend")),
        SelectChoice(SidebarVariant.INSET.name, Component.text("Eingelassen")),
    )

    /**
     * The options of the sidebar collapsing select.
     */
    private val COLLAPSIBLES = listOf(
        SelectChoice(SidebarCollapsible.ICON.name, Component.text("Zu Symbolen")),
        SelectChoice(SidebarCollapsible.OFFCANVAS.name, Component.text("Ganz ausblenden")),
        SelectChoice(SidebarCollapsible.NONE.name, Component.text("Nicht einklappbar")),
    )
}
