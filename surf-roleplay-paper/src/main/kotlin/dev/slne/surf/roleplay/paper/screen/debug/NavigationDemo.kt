package dev.slne.surf.roleplay.paper.screen.debug

import dev.slne.surf.roleplay.api.client.common.screen.AccordionType
import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.BadgeVariant
import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.ButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.ChangeHandler
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.ElementsBuilder
import dev.slne.surf.roleplay.api.client.common.screen.LayoutDirection
import dev.slne.surf.roleplay.api.client.common.screen.Orientation
import dev.slne.surf.roleplay.api.client.common.screen.OverlaySide
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.ScreenThemes
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.api.client.common.screen.ScrollOrientation
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoice
import dev.slne.surf.roleplay.api.client.common.screen.SidebarCollapsible
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuSubButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.SidebarSide
import dev.slne.surf.roleplay.api.client.common.screen.SidebarVariant
import dev.slne.surf.roleplay.api.client.common.screen.Spacing
import dev.slne.surf.roleplay.api.client.common.screen.TabsVariant
import dev.slne.surf.roleplay.api.client.common.screen.TextKind
import dev.slne.surf.roleplay.api.client.common.screen.accordion
import dev.slne.surf.roleplay.api.client.common.screen.accordionContent
import dev.slne.surf.roleplay.api.client.common.screen.accordionItem
import dev.slne.surf.roleplay.api.client.common.screen.accordionTrigger
import dev.slne.surf.roleplay.api.client.common.screen.badge
import dev.slne.surf.roleplay.api.client.common.screen.breadcrumb
import dev.slne.surf.roleplay.api.client.common.screen.breadcrumbEllipsis
import dev.slne.surf.roleplay.api.client.common.screen.breadcrumbItem
import dev.slne.surf.roleplay.api.client.common.screen.breadcrumbLink
import dev.slne.surf.roleplay.api.client.common.screen.breadcrumbList
import dev.slne.surf.roleplay.api.client.common.screen.breadcrumbPage
import dev.slne.surf.roleplay.api.client.common.screen.breadcrumbSeparator
import dev.slne.surf.roleplay.api.client.common.screen.card
import dev.slne.surf.roleplay.api.client.common.screen.carousel
import dev.slne.surf.roleplay.api.client.common.screen.carouselContent
import dev.slne.surf.roleplay.api.client.common.screen.carouselItem
import dev.slne.surf.roleplay.api.client.common.screen.carouselNext
import dev.slne.surf.roleplay.api.client.common.screen.carouselPrevious
import dev.slne.surf.roleplay.api.client.common.screen.collapsible
import dev.slne.surf.roleplay.api.client.common.screen.collapsibleContent
import dev.slne.surf.roleplay.api.client.common.screen.collapsibleTrigger
import dev.slne.surf.roleplay.api.client.common.screen.direction
import dev.slne.surf.roleplay.api.client.common.screen.dropdownMenu
import dev.slne.surf.roleplay.api.client.common.screen.menuContent
import dev.slne.surf.roleplay.api.client.common.screen.menuItem
import dev.slne.surf.roleplay.api.client.common.screen.menuSub
import dev.slne.surf.roleplay.api.client.common.screen.menuSubTrigger
import dev.slne.surf.roleplay.api.client.common.screen.navigationMenu
import dev.slne.surf.roleplay.api.client.common.screen.navigationMenuContent
import dev.slne.surf.roleplay.api.client.common.screen.navigationMenuItem
import dev.slne.surf.roleplay.api.client.common.screen.navigationMenuLink
import dev.slne.surf.roleplay.api.client.common.screen.navigationMenuList
import dev.slne.surf.roleplay.api.client.common.screen.navigationMenuTrigger
import dev.slne.surf.roleplay.api.client.common.screen.pagination
import dev.slne.surf.roleplay.api.client.common.screen.paginationContent
import dev.slne.surf.roleplay.api.client.common.screen.paginationEllipsis
import dev.slne.surf.roleplay.api.client.common.screen.paginationItem
import dev.slne.surf.roleplay.api.client.common.screen.paginationLink
import dev.slne.surf.roleplay.api.client.common.screen.paginationNext
import dev.slne.surf.roleplay.api.client.common.screen.paginationPrevious
import dev.slne.surf.roleplay.api.client.common.screen.resizableHandle
import dev.slne.surf.roleplay.api.client.common.screen.resizablePanel
import dev.slne.surf.roleplay.api.client.common.screen.resizablePanelGroup
import dev.slne.surf.roleplay.api.client.common.screen.screen
import dev.slne.surf.roleplay.api.client.common.screen.scrollArea
import dev.slne.surf.roleplay.api.client.common.screen.select
import dev.slne.surf.roleplay.api.client.common.screen.sheet
import dev.slne.surf.roleplay.api.client.common.screen.sheetContent
import dev.slne.surf.roleplay.api.client.common.screen.sheetDescription
import dev.slne.surf.roleplay.api.client.common.screen.sheetHeader
import dev.slne.surf.roleplay.api.client.common.screen.sheetTitle
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
import dev.slne.surf.roleplay.api.client.common.screen.tabs
import dev.slne.surf.roleplay.api.client.common.screen.tabsContent
import dev.slne.surf.roleplay.api.client.common.screen.tabsList
import dev.slne.surf.roleplay.api.client.common.screen.tabsTrigger
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
                disclosures(hooks)
                tabsSection(hooks)
                paths(clicked)
                scrollAreas()
                resizables(hooks)
                carousels(hooks)
                navigationMenus(hooks, clicked)
                directions(hooks, clicked)
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
     * Returns a handler that reports the new value of a change under a name.
     *
     * @param hooks what the page needs from its surroundings
     * @param name the name the change is reported under
     * @return the handler
     */
    private fun changed(hooks: Hooks, name: String) = ChangeHandler { change ->
        hooks.report(Component.text("$name: ${change.value.ifEmpty { "(keiner)" }}", NamedTextColor.AQUA))
    }

    /**
     * Adds a collapsible and accordions of type single, collapsible single and multiple.
     *
     * @param hooks what the page needs from its surroundings
     */
    private fun ElementsBuilder.disclosures(hooks: Hooks) = section("disclosures", "Collapsible und Accordion") {
        collapsible("collapsible", onChange = changed(hooks, "Collapsible offen")) {
            row("collapsible_head", width = ElementSize.grow(), gap = 6, crossAlign = Alignment.CENTER) {
                text("collapsible_title", Component.text("@ammo hat 3 Funkkanäle"), TextKind.LARGE, width = ElementSize.grow())
                collapsibleTrigger("collapsible_trigger") {
                    button("collapsible_toggle", Component.empty(), submitsInput = false, icon = "chevrons-up-down", variant = ButtonVariant.GHOST, size = ButtonSize.ICON_SM)
                }
            }
            label("collapsible_first", Component.text("Kanal 1: Leitstelle"))
            collapsibleContent("collapsible_content") {
                label("collapsible_second", Component.text("Kanal 2: Rettungsdienst"))
                label("collapsible_third", Component.text("Kanal 3: Polizei"))
            }
        }
        row("accordions", gap = 12) {
            column("accordion_single_box", width = ElementSize.grow(), gap = 4, crossAlign = Alignment.STRETCH) {
                text("accordion_single_title", Component.text("Einzeln, einklappbar"), TextKind.MUTED)
                accordion("accordion_single", AccordionType.SINGLE, collapsible = true, value = listOf("shipping"), onChange = changed(hooks, "Accordion einzeln")) {
                    accordionItem("accordion_shipping", "shipping") {
                        accordionTrigger("accordion_shipping_trigger", Component.text("Wie melde ich einen Einsatz?"))
                        accordionContent("accordion_shipping_content") {
                            text("accordion_shipping_text", Component.text("Über das Funkgerät oder die Leitstellen-App."))
                        }
                    }
                    accordionItem("accordion_returns", "returns") {
                        accordionTrigger("accordion_returns_trigger", Component.text("Wer darf Einheiten alarmieren?"))
                        accordionContent("accordion_returns_content") {
                            text("accordion_returns_text", Component.text("Nur Disponenten mit Freigabe."))
                        }
                    }
                    accordionItem("accordion_locked", "locked", enabled = false) {
                        accordionTrigger("accordion_locked_trigger", Component.text("Gesperrt"))
                        accordionContent("accordion_locked_content") { text("accordion_locked_text", Component.text("Nicht sichtbar.")) }
                    }
                }
            }
            column("accordion_multiple_box", width = ElementSize.grow(), gap = 4, crossAlign = Alignment.STRETCH) {
                text("accordion_multiple_title", Component.text("Mehrere"), TextKind.MUTED)
                accordion("accordion_multiple", AccordionType.MULTIPLE, onChange = changed(hooks, "Accordion mehrere")) {
                    accordionItem("accordion_a", "a") {
                        accordionTrigger("accordion_a_trigger", Component.text("Fahrzeuge"))
                        accordionContent("accordion_a_content") { text("accordion_a_text", Component.text("RTW, NEF, KTW")) }
                    }
                    accordionItem("accordion_b", "b") {
                        accordionTrigger("accordion_b_trigger", Component.text("Wachen"))
                        accordionContent("accordion_b_content") { text("accordion_b_text", Component.text("Nord, Süd, Mitte")) }
                    }
                }
            }
        }
    }

    /**
     * Adds tabs in the default and line variants, horizontal and vertical.
     *
     * @param hooks what the page needs from its surroundings
     */
    private fun ElementsBuilder.tabsSection(hooks: Hooks) = section("tabs_section", "Tabs") {
        row("tabs_row", gap = 12) {
            tabsDemo("tabs_default", TabsVariant.DEFAULT, Orientation.HORIZONTAL, hooks)
            tabsDemo("tabs_line", TabsVariant.LINE, Orientation.HORIZONTAL, hooks)
        }
        row("tabs_vertical_row", gap = 12) {
            tabsDemo("tabs_vertical", TabsVariant.DEFAULT, Orientation.VERTICAL, hooks)
            tabsDemo("tabs_vertical_line", TabsVariant.LINE, Orientation.VERTICAL, hooks)
        }
    }

    /**
     * Adds tabs with an overview, a report and a disabled tab.
     *
     * @param id the id of the tabs
     * @param tabsVariant how the list is drawn
     * @param orientation whether the list is above or beside the contents
     * @param hooks what the page needs from its surroundings
     */
    private fun ElementsBuilder.tabsDemo(id: String, tabsVariant: TabsVariant, orientation: Orientation, hooks: Hooks) {
        tabs(id, value = "overview", orientation = orientation, onChange = changed(hooks, "Tab $id")) {
            tabsList("${id}_list", tabsVariant) {
                tabsTrigger("${id}_overview", "overview", Component.text("Übersicht"), icon = "layout-dashboard")
                tabsTrigger("${id}_report", "report", Component.text("Bericht"))
                tabsTrigger("${id}_locked", "locked", Component.text("Gesperrt"), enabled = false)
            }
            tabsContent("${id}_overview_content", "overview") {
                text("${id}_overview_text", Component.text("3 offene Einsätze"), TextKind.MUTED)
            }
            tabsContent("${id}_report_content", "report") {
                text("${id}_report_text", Component.text("Bericht vom 29.09.2026"), TextKind.MUTED)
            }
            tabsContent("${id}_locked_content", "locked") {}
        }
    }

    /**
     * Adds breadcrumbs with the default and a custom separator and an ellipsis menu, and a
     * pagination.
     *
     * @param clicked the handler that reports clicks
     */
    private fun ElementsBuilder.paths(clicked: ButtonHandler) = section("paths", "Breadcrumb und Pagination") {
        breadcrumbTrail("breadcrumb", "chevron-right", clicked)
        breadcrumbTrail("breadcrumb_slash", "slash", clicked)
        pagination("pagination") {
            paginationContent("pagination_content") {
                paginationItem("pagination_prev_item") { paginationPrevious("pagination_prev", onClick = clicked) }
                paginationItem("pagination_1_item") { paginationLink("pagination_1", Component.text("1"), onClick = clicked) }
                paginationItem("pagination_2_item") { paginationLink("pagination_2", Component.text("2"), active = true, onClick = clicked) }
                paginationItem("pagination_3_item") { paginationLink("pagination_3", Component.text("3"), onClick = clicked) }
                paginationItem("pagination_gap_item") { paginationEllipsis("pagination_gap") }
                paginationItem("pagination_next_item") { paginationNext("pagination_next", onClick = clicked) }
            }
        }
    }

    /**
     * Adds a breadcrumb from the start page to the current mission, with an ellipsis menu for the
     * hidden levels.
     *
     * @param id the id of the breadcrumb
     * @param separator the Lucide icon of the separators
     * @param clicked the handler that reports clicks
     */
    private fun ElementsBuilder.breadcrumbTrail(id: String, separator: String, clicked: ButtonHandler) {
        breadcrumb(id) {
            breadcrumbList("${id}_list") {
                breadcrumbItem("${id}_home_item") { breadcrumbLink("${id}_home", Component.text("Start"), onClick = clicked) }
                breadcrumbSeparator("${id}_sep_1", separator)
                breadcrumbItem("${id}_more_item") {
                    dropdownMenu("${id}_more_menu", align = Alignment.START) {
                        breadcrumbEllipsis("${id}_more")
                        menuContent("${id}_more_content") {
                            menuItem("${id}_more_docs", Component.text("Dokumente"), onClick = clicked)
                            menuItem("${id}_more_units", Component.text("Einheiten"), onClick = clicked)
                        }
                    }
                }
                breadcrumbSeparator("${id}_sep_2", separator)
                breadcrumbItem("${id}_calls_item") { breadcrumbLink("${id}_calls", Component.text("Einsätze"), onClick = clicked) }
                breadcrumbSeparator("${id}_sep_3", separator)
                breadcrumbItem("${id}_page_item") { breadcrumbPage("${id}_page", Component.text("Einsatz 42")) }
            }
        }
    }

    /**
     * Adds scroll areas that scroll vertically, horizontally and both ways.
     */
    private fun ElementsBuilder.scrollAreas() = section("scroll_areas", "Scroll Area") {
        row("scroll_row", gap = 12) {
            scrollArea("scroll_vertical", ElementSize.fixed(140), ElementSize.fixed(100)) {
                column("scroll_vertical_list", width = ElementSize.grow(), gap = 4, padding = Spacing(4, 6, 4, 6), crossAlign = Alignment.STRETCH) {
                    text("scroll_vertical_title", Component.text("Einheiten"), TextKind.SMALL)
                    (1..20).forEach { label("scroll_vertical_$it", Component.text("RTW $it")) }
                }
            }
            scrollArea("scroll_horizontal", ElementSize.fixed(200), ElementSize.fixed(40), ScrollOrientation.HORIZONTAL) {
                row("scroll_horizontal_list", gap = 4, padding = Spacing(4, 4, 4, 4)) {
                    (1..15).forEach { badge("scroll_horizontal_$it", Component.text("Wache $it"), variant = BadgeVariant.OUTLINE) }
                }
            }
            scrollArea("scroll_both", ElementSize.fixed(140), ElementSize.fixed(100), ScrollOrientation.BOTH) {
                column("scroll_both_grid", gap = 4, padding = Spacing(4, 4, 4, 4)) {
                    (1..12).forEach { rowIndex ->
                        row("scroll_both_row_$rowIndex", gap = 4) {
                            (1..8).forEach { label("scroll_both_${rowIndex}_$it", Component.text("Feld $rowIndex.$it")) }
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds a horizontal resizable group with a grip whose right side is split vertically.
     *
     * @param hooks what the page needs from its surroundings
     */
    private fun ElementsBuilder.resizables(hooks: Hooks) = section("resizables", "Resizable") {
        resizablePanelGroup("resizable", height = ElementSize.fixed(120), onChange = changed(hooks, "Größen")) {
            resizablePanel("resizable_left", defaultSize = 40.0, minSize = 20.0, maxSize = 70.0) {
                resizableLabel("resizable_left_label", "Karte")
            }
            resizableHandle("resizable_handle", withHandle = true)
            resizablePanel("resizable_right", defaultSize = 60.0) {
                resizablePanelGroup("resizable_nested", Orientation.VERTICAL, height = ElementSize.grow(), onChange = changed(hooks, "Größen rechts")) {
                    resizablePanel("resizable_top", defaultSize = 50.0, minSize = 25.0) { resizableLabel("resizable_top_label", "Einsätze") }
                    resizableHandle("resizable_nested_handle")
                    resizablePanel("resizable_bottom", defaultSize = 50.0, minSize = 25.0) { resizableLabel("resizable_bottom_label", "Funk") }
                }
            }
        }
    }

    /**
     * Adds a centered text that fills a resizable panel.
     *
     * @param id the id of the text
     * @param title the text
     */
    private fun ElementsBuilder.resizableLabel(id: String, title: String) {
        column("${id}_box", width = ElementSize.grow(), height = ElementSize.grow(), mainAlign = Alignment.CENTER, crossAlign = Alignment.CENTER) {
            text(id, Component.text(title), TextKind.LARGE)
        }
    }

    /**
     * Adds carousels: horizontal with one and with two slides at a time, looping, and vertical.
     *
     * @param hooks what the page needs from its surroundings
     */
    private fun ElementsBuilder.carousels(hooks: Hooks) = section("carousels", "Carousel") {
        row("carousel_row", gap = 12, crossAlign = Alignment.CENTER) {
            carouselDemo("carousel_single", 100.0, Orientation.HORIZONTAL, loop = false, hooks)
            carouselDemo("carousel_half", 50.0, Orientation.HORIZONTAL, loop = true, hooks)
            carouselDemo("carousel_vertical", 50.0, Orientation.VERTICAL, loop = false, hooks)
        }
    }

    /**
     * Adds a carousel of five numbered cards.
     *
     * @param id the id of the carousel
     * @param basis the share of the content every slide takes, in percent
     * @param orientation whether the slides move sideways or up and down
     * @param loop whether the last position is followed by the first
     * @param hooks what the page needs from its surroundings
     */
    private fun ElementsBuilder.carouselDemo(id: String, basis: Double, orientation: Orientation, loop: Boolean, hooks: Hooks) {
        val vertical = orientation == Orientation.VERTICAL
        carousel(
            id,
            width = ElementSize.fixed(if (vertical) 120 else 160),
            orientation = orientation,
            loop = loop,
            height = if (vertical) ElementSize.fixed(150) else ElementSize.FIT,
            onChange = changed(hooks, "Folie $id"),
        ) {
            carouselPrevious("${id}_prev")
            carouselContent("${id}_content") {
                (1..5).forEach { number ->
                    carouselItem("${id}_slide_$number", basis) {
                        column("${id}_slide_${number}_pad", width = ElementSize.grow(), padding = Spacing(2, 2, 2, 2), crossAlign = Alignment.STRETCH) {
                            card("${id}_card_$number", width = ElementSize.grow()) {
                                column("${id}_card_${number}_body", width = ElementSize.grow(), height = ElementSize.fixed(if (vertical) 40 else 60), mainAlign = Alignment.CENTER, crossAlign = Alignment.CENTER) {
                                    text("${id}_card_${number}_text", Component.text("$number"), TextKind.H3)
                                }
                            }
                        }
                    }
                }
            }
            carouselNext("${id}_next")
        }
    }

    /**
     * Adds a navigation menu with two items that open content and a link.
     *
     * @param hooks what the page needs from its surroundings
     * @param clicked the handler that reports clicks
     */
    private fun ElementsBuilder.navigationMenus(hooks: Hooks, clicked: ButtonHandler) = section("navigation_menus", "Navigation Menu") {
        navigationMenu("navigation_menu") {
            navigationMenuList("navigation_menu_list") {
                navigationMenuItem("navigation_start_item", onChange = changed(hooks, "Menü Einstieg offen")) {
                    navigationMenuTrigger("navigation_start_trigger", Component.text("Einstieg"))
                    navigationMenuContent("navigation_start_content") {
                        column("navigation_start_links", width = ElementSize.fixed(220), gap = 2, crossAlign = Alignment.STRETCH) {
                            navigationLink("navigation_intro", "Einführung", "Die Leitstelle in fünf Minuten.", clicked)
                            navigationLink("navigation_rules", "Regeln", "Was im Funk erlaubt ist.", clicked)
                        }
                    }
                }
                navigationMenuItem("navigation_units_item", onChange = changed(hooks, "Menü Einheiten offen")) {
                    navigationMenuTrigger("navigation_units_trigger", Component.text("Einheiten"))
                    navigationMenuContent("navigation_units_content") {
                        column("navigation_units_links", width = ElementSize.fixed(180), gap = 2, crossAlign = Alignment.STRETCH) {
                            navigationLink("navigation_rtw", "Rettungswagen", "Notfallrettung", clicked)
                            navigationLink("navigation_nef", "Notarzt", "Notarzteinsatzfahrzeug", clicked)
                            navigationLink("navigation_ktw", "Krankentransport", "Planbare Fahrten", clicked, enabled = false)
                        }
                    }
                }
                navigationMenuItem("navigation_docs_item") {
                    navigationMenuLink("navigation_docs", onClick = clicked) { label("navigation_docs_text", Component.text("Dokumente")) }
                }
            }
        }
    }

    /**
     * Adds a navigation menu link with a title and a muted description.
     *
     * @param id the id of the link
     * @param title the title
     * @param description the description
     * @param clicked the handler that reports clicks
     * @param enabled whether the link can be used
     */
    private fun ElementsBuilder.navigationLink(id: String, title: String, description: String, clicked: ButtonHandler, enabled: Boolean = true) {
        navigationMenuLink(id, enabled = enabled, onClick = clicked) {
            text("${id}_title", Component.text(title), TextKind.SMALL)
            text("${id}_description", Component.text(description), TextKind.MUTED)
        }
    }

    /**
     * Adds a right-to-left direction with a breadcrumb, a pagination, tabs, an accordion, a
     * carousel, a menu with a sub-menu, a sheet on the left and a nested left-to-right row.
     *
     * @param hooks what the page needs from its surroundings
     * @param clicked the handler that reports clicks
     */
    private fun ElementsBuilder.directions(hooks: Hooks, clicked: ButtonHandler) = section("directions", "Direction (rechts nach links)") {
        direction("direction_rtl", LayoutDirection.RTL, width = ElementSize.grow()) {
            column("direction_body", width = ElementSize.grow(), gap = 8) {
                breadcrumbTrail("direction_breadcrumb", "chevron-right", clicked)
                pagination("direction_pagination") {
                    paginationContent("direction_pagination_content") {
                        paginationItem("direction_prev_item") { paginationPrevious("direction_prev", onClick = clicked) }
                        paginationItem("direction_1_item") { paginationLink("direction_1", Component.text("1"), active = true, onClick = clicked) }
                        paginationItem("direction_2_item") { paginationLink("direction_2", Component.text("2"), onClick = clicked) }
                        paginationItem("direction_next_item") { paginationNext("direction_next", onClick = clicked) }
                    }
                }
                row("direction_row", width = ElementSize.grow(), gap = 12) {
                    tabsDemo("direction_tabs", TabsVariant.LINE, Orientation.HORIZONTAL, hooks)
                    column("direction_accordion_box", width = ElementSize.grow(), crossAlign = Alignment.STRETCH) {
                        accordion("direction_accordion", AccordionType.SINGLE, collapsible = true, onChange = changed(hooks, "Accordion rechts nach links")) {
                            accordionItem("direction_accordion_a", "a") {
                                accordionTrigger("direction_accordion_a_trigger", Component.text("Gespiegelt"))
                                accordionContent("direction_accordion_a_content") { text("direction_accordion_a_text", Component.text("Der Pfeil steht links.")) }
                            }
                        }
                    }
                }
                row("direction_controls", gap = 12, crossAlign = Alignment.CENTER) {
                    carouselDemo("direction_carousel", 50.0, Orientation.HORIZONTAL, loop = false, hooks)
                    dropdownMenu("direction_menu", align = Alignment.START) {
                        button("direction_menu_trigger", Component.text("Menü"), submitsInput = false, icon = "menu", variant = ButtonVariant.OUTLINE)
                        menuContent("direction_menu_content") {
                            menuItem("direction_menu_profile", Component.text("Profil"), icon = "user", shortcut = Component.text("⇧P"), onClick = clicked)
                            menuSub("direction_menu_sub") {
                                menuSubTrigger("direction_menu_sub_trigger", Component.text("Einladen"), icon = "user-plus")
                                menuContent("direction_menu_sub_content") {
                                    menuItem("direction_menu_sub_radio", Component.text("Per Funk"), icon = "radio", onClick = clicked)
                                }
                            }
                        }
                    }
                    sheet("direction_sheet") {
                        button("direction_sheet_trigger", Component.text("Sheet links"), submitsInput = false, variant = ButtonVariant.OUTLINE)
                        sheetContent("direction_sheet_content", OverlaySide.LEFT) {
                            sheetHeader("direction_sheet_header") {
                                sheetTitle("direction_sheet_title", Component.text("Gespiegelt"))
                                sheetDescription("direction_sheet_description", Component.text("Links angefordert, rechts geöffnet."))
                            }
                        }
                    }
                }
                direction("direction_ltr", LayoutDirection.LTR) {
                    row("direction_ltr_row", gap = 6, crossAlign = Alignment.CENTER) {
                        badge("direction_ltr_badge", Component.text("Verschachtelt"), variant = BadgeVariant.SECONDARY)
                        text("direction_ltr_text", Component.text("Diese Zeile läuft wieder von links nach rechts."), TextKind.MUTED)
                    }
                }
            }
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
