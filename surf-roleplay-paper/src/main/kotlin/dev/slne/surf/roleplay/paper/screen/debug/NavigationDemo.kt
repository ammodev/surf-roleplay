package dev.slne.surf.roleplay.paper.screen.debug

import dev.slne.surf.roleplay.api.client.common.screen.dsl.Accordion
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AccordionContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AccordionItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AccordionTrigger
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Badge
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Breadcrumb
import dev.slne.surf.roleplay.api.client.common.screen.dsl.BreadcrumbEllipsis
import dev.slne.surf.roleplay.api.client.common.screen.dsl.BreadcrumbItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.BreadcrumbLink
import dev.slne.surf.roleplay.api.client.common.screen.dsl.BreadcrumbList
import dev.slne.surf.roleplay.api.client.common.screen.dsl.BreadcrumbPage
import dev.slne.surf.roleplay.api.client.common.screen.dsl.BreadcrumbSeparator
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Card
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Carousel
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CarouselContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CarouselItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CarouselNext
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CarouselPrevious
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Collapsible
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CollapsibleContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CollapsibleTrigger
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Direction
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DropdownMenu
import dev.slne.surf.roleplay.api.client.common.screen.dsl.H3
import dev.slne.surf.roleplay.api.client.common.screen.dsl.H4
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Label
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Large
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuSub
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuSubTrigger
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Muted
import dev.slne.surf.roleplay.api.client.common.screen.dsl.NavigationMenu
import dev.slne.surf.roleplay.api.client.common.screen.dsl.NavigationMenuContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.NavigationMenuItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.NavigationMenuLink
import dev.slne.surf.roleplay.api.client.common.screen.dsl.NavigationMenuList
import dev.slne.surf.roleplay.api.client.common.screen.dsl.NavigationMenuTrigger
import dev.slne.surf.roleplay.api.client.common.screen.dsl.P
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Pagination
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PaginationContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PaginationEllipsis
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PaginationItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PaginationLink
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PaginationNext
import dev.slne.surf.roleplay.api.client.common.screen.dsl.PaginationPrevious
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ResizableHandle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ResizablePanel
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ResizablePanelGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Row
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ScrollArea
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Select
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Sheet
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SheetContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SheetDescription
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SheetHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SheetTitle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Sidebar
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarFooter
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarGroupAction
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarGroupContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarGroupLabel
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarInput
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarInset
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarMenu
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarMenuAction
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarMenuBadge
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarMenuButton
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarMenuItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarMenuSkeleton
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarMenuSub
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarMenuSubButton
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarMenuSubItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarProvider
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarRail
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarSeparator
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarTrigger
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Small
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Tabs
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TabsContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TabsList
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TabsTrigger
import dev.slne.surf.roleplay.api.client.common.screen.AccordionType
import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.BadgeVariant
import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.ButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.ChangeHandler
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
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
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Screen
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
        return Screen(Component.text("Navigation"), theme = theme, variant = variant) {
            Column(width = ElementSize.fixed(620), gap = 12, crossAlign = Alignment.STRETCH, id = "root") {
                Row(gap = 4, crossAlign = Alignment.CENTER, id = "theme_row") {
                    Select(InputsDemo.THEMES, selected = theme, width = ElementSize.grow(), onChange = { change ->
                        hooks.reopen(change.value, variant, setup)
                    }, id = "theme")
                    Select(InputsDemo.VARIANTS, selected = variant.name, width = ElementSize.fixed(90), onChange = { change ->
                        hooks.reopen(theme, ScreenVariant.valueOf(change.value), setup)
                    }, id = "variant")
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
    private fun ComponentScope.section(id: String, title: String, content: ComponentScope.() -> Unit) {
        Column(gap = 6, crossAlign = Alignment.STRETCH, id = id) {
            H3(Component.text(title), id = "${id}_title")
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
    private fun ComponentScope.disclosures(hooks: Hooks) = section("disclosures", "Collapsible und Accordion") {
        Collapsible(onChange = changed(hooks, "Collapsible offen"), id = "collapsible") {
            Row(width = ElementSize.grow(), gap = 6, crossAlign = Alignment.CENTER, id = "collapsible_head") {
                Large(Component.text("@ammo hat 3 Funkkanäle"), width = ElementSize.grow(), id = "collapsible_title")
                CollapsibleTrigger(id = "collapsible_trigger") {
                    Button(Component.empty(), submitsInput = false, icon = "chevrons-up-down", variant = ButtonVariant.GHOST, size = ButtonSize.ICON_SM, id = "collapsible_toggle")
                }
            }
            Label(Component.text("Kanal 1: Leitstelle"), id = "collapsible_first")
            CollapsibleContent(id = "collapsible_content") {
                Label(Component.text("Kanal 2: Rettungsdienst"), id = "collapsible_second")
                Label(Component.text("Kanal 3: Polizei"), id = "collapsible_third")
            }
        }
        Row(gap = 12, id = "accordions") {
            Column(width = ElementSize.grow(), gap = 4, crossAlign = Alignment.STRETCH, id = "accordion_single_box") {
                Muted(Component.text("Einzeln, einklappbar"), id = "accordion_single_title")
                Accordion(AccordionType.SINGLE, collapsible = true, value = listOf("shipping"), onChange = changed(hooks, "Accordion einzeln"), id = "accordion_single") {
                    AccordionItem("shipping", id = "accordion_shipping") {
                        AccordionTrigger(Component.text("Wie melde ich einen Einsatz?"), id = "accordion_shipping_trigger")
                        AccordionContent(id = "accordion_shipping_content") {
                            P(Component.text("Über das Funkgerät oder die Leitstellen-App."), id = "accordion_shipping_text")
                        }
                    }
                    AccordionItem("returns", id = "accordion_returns") {
                        AccordionTrigger(Component.text("Wer darf Einheiten alarmieren?"), id = "accordion_returns_trigger")
                        AccordionContent(id = "accordion_returns_content") {
                            P(Component.text("Nur Disponenten mit Freigabe."), id = "accordion_returns_text")
                        }
                    }
                    AccordionItem("locked", enabled = false, id = "accordion_locked") {
                        AccordionTrigger(Component.text("Gesperrt"), id = "accordion_locked_trigger")
                        AccordionContent(id = "accordion_locked_content") { P(Component.text("Nicht sichtbar."), id = "accordion_locked_text") }
                    }
                }
            }
            Column(width = ElementSize.grow(), gap = 4, crossAlign = Alignment.STRETCH, id = "accordion_multiple_box") {
                Muted(Component.text("Mehrere"), id = "accordion_multiple_title")
                Accordion(AccordionType.MULTIPLE, onChange = changed(hooks, "Accordion mehrere"), id = "accordion_multiple") {
                    AccordionItem("a", id = "accordion_a") {
                        AccordionTrigger(Component.text("Fahrzeuge"), id = "accordion_a_trigger")
                        AccordionContent(id = "accordion_a_content") { P(Component.text("RTW, NEF, KTW"), id = "accordion_a_text") }
                    }
                    AccordionItem("b", id = "accordion_b") {
                        AccordionTrigger(Component.text("Wachen"), id = "accordion_b_trigger")
                        AccordionContent(id = "accordion_b_content") { P(Component.text("Nord, Süd, Mitte"), id = "accordion_b_text") }
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
    private fun ComponentScope.tabsSection(hooks: Hooks) = section("tabs_section", "Tabs") {
        Row(gap = 12, id = "tabs_row") {
            tabsDemo("tabs_default", TabsVariant.DEFAULT, Orientation.HORIZONTAL, hooks)
            tabsDemo("tabs_line", TabsVariant.LINE, Orientation.HORIZONTAL, hooks)
        }
        Row(gap = 12, id = "tabs_vertical_row") {
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
    private fun ComponentScope.tabsDemo(id: String, tabsVariant: TabsVariant, orientation: Orientation, hooks: Hooks) {
        Tabs(value = "overview", orientation = orientation, onChange = changed(hooks, "Tab $id"), id = id) {
            TabsList(tabsVariant, id = "${id}_list") {
                TabsTrigger("overview", Component.text("Übersicht"), icon = "layout-dashboard", id = "${id}_overview")
                TabsTrigger("report", Component.text("Bericht"), id = "${id}_report")
                TabsTrigger("locked", Component.text("Gesperrt"), enabled = false, id = "${id}_locked")
            }
            TabsContent("overview", id = "${id}_overview_content") {
                Muted(Component.text("3 offene Einsätze"), id = "${id}_overview_text")
            }
            TabsContent("report", id = "${id}_report_content") {
                Muted(Component.text("Bericht vom 29.09.2026"), id = "${id}_report_text")
            }
            TabsContent("locked", id = "${id}_locked_content") {}
        }
    }

    /**
     * Adds breadcrumbs with the default and a custom separator and an ellipsis menu, and a
     * pagination.
     *
     * @param clicked the handler that reports clicks
     */
    private fun ComponentScope.paths(clicked: ButtonHandler) = section("paths", "Breadcrumb und Pagination") {
        breadcrumbTrail("breadcrumb", "chevron-right", clicked)
        breadcrumbTrail("breadcrumb_slash", "slash", clicked)
        Pagination(id = "pagination") {
            PaginationContent(id = "pagination_content") {
                PaginationItem(id = "pagination_prev_item") { PaginationPrevious(onClick = clicked, id = "pagination_prev") }
                PaginationItem(id = "pagination_1_item") { PaginationLink(Component.text("1"), onClick = clicked, id = "pagination_1") }
                PaginationItem(id = "pagination_2_item") { PaginationLink(Component.text("2"), active = true, onClick = clicked, id = "pagination_2") }
                PaginationItem(id = "pagination_3_item") { PaginationLink(Component.text("3"), onClick = clicked, id = "pagination_3") }
                PaginationItem(id = "pagination_gap_item") { PaginationEllipsis(id = "pagination_gap") }
                PaginationItem(id = "pagination_next_item") { PaginationNext(onClick = clicked, id = "pagination_next") }
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
    private fun ComponentScope.breadcrumbTrail(id: String, separator: String, clicked: ButtonHandler) {
        Breadcrumb(id = id) {
            BreadcrumbList(id = "${id}_list") {
                BreadcrumbItem(id = "${id}_home_item") { BreadcrumbLink(Component.text("Start"), onClick = clicked, id = "${id}_home") }
                BreadcrumbSeparator(separator, id = "${id}_sep_1")
                BreadcrumbItem(id = "${id}_more_item") {
                    DropdownMenu(align = Alignment.START, id = "${id}_more_menu") {
                        BreadcrumbEllipsis(id = "${id}_more")
                        MenuContent(id = "${id}_more_content") {
                            MenuItem(Component.text("Dokumente"), onClick = clicked, id = "${id}_more_docs")
                            MenuItem(Component.text("Einheiten"), onClick = clicked, id = "${id}_more_units")
                        }
                    }
                }
                BreadcrumbSeparator(separator, id = "${id}_sep_2")
                BreadcrumbItem(id = "${id}_calls_item") { BreadcrumbLink(Component.text("Einsätze"), onClick = clicked, id = "${id}_calls") }
                BreadcrumbSeparator(separator, id = "${id}_sep_3")
                BreadcrumbItem(id = "${id}_page_item") { BreadcrumbPage(Component.text("Einsatz 42"), id = "${id}_page") }
            }
        }
    }

    /**
     * Adds scroll areas that scroll vertically, horizontally and both ways.
     */
    private fun ComponentScope.scrollAreas() = section("scroll_areas", "Scroll Area") {
        Row(gap = 12, id = "scroll_row") {
            ScrollArea(ElementSize.fixed(140), ElementSize.fixed(100), id = "scroll_vertical") {
                Column(width = ElementSize.grow(), gap = 4, padding = Spacing(4, 6, 4, 6), crossAlign = Alignment.STRETCH, id = "scroll_vertical_list") {
                    Small(Component.text("Einheiten"), id = "scroll_vertical_title")
                    (1..20).forEach { Label(Component.text("RTW $it"), id = "scroll_vertical_$it") }
                }
            }
            ScrollArea(ElementSize.fixed(200), ElementSize.fixed(40), ScrollOrientation.HORIZONTAL, id = "scroll_horizontal") {
                Row(gap = 4, padding = Spacing(4, 4, 4, 4), id = "scroll_horizontal_list") {
                    (1..15).forEach { Badge(Component.text("Wache $it"), variant = BadgeVariant.OUTLINE, id = "scroll_horizontal_$it") }
                }
            }
            ScrollArea(ElementSize.fixed(140), ElementSize.fixed(100), ScrollOrientation.BOTH, id = "scroll_both") {
                Column(gap = 4, padding = Spacing(4, 4, 4, 4), id = "scroll_both_grid") {
                    (1..12).forEach { rowIndex ->
                        Row(gap = 4, id = "scroll_both_row_$rowIndex") {
                            (1..8).forEach { Label(Component.text("Feld $rowIndex.$it"), id = "scroll_both_${rowIndex}_$it") }
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
    private fun ComponentScope.resizables(hooks: Hooks) = section("resizables", "Resizable") {
        ResizablePanelGroup(height = ElementSize.fixed(120), onChange = changed(hooks, "Größen"), id = "resizable") {
            ResizablePanel(defaultSize = 40.0, minSize = 20.0, maxSize = 70.0, id = "resizable_left") {
                resizableLabel("resizable_left_label", "Karte")
            }
            ResizableHandle(withHandle = true, id = "resizable_handle")
            ResizablePanel(defaultSize = 60.0, id = "resizable_right") {
                ResizablePanelGroup(Orientation.VERTICAL, height = ElementSize.grow(), onChange = changed(hooks, "Größen rechts"), id = "resizable_nested") {
                    ResizablePanel(defaultSize = 50.0, minSize = 25.0, id = "resizable_top") { resizableLabel("resizable_top_label", "Einsätze") }
                    ResizableHandle(id = "resizable_nested_handle")
                    ResizablePanel(defaultSize = 50.0, minSize = 25.0, id = "resizable_bottom") { resizableLabel("resizable_bottom_label", "Funk") }
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
    private fun ComponentScope.resizableLabel(id: String, title: String) {
        Column(width = ElementSize.grow(), height = ElementSize.grow(), mainAlign = Alignment.CENTER, crossAlign = Alignment.CENTER, id = "${id}_box") {
            Large(Component.text(title), id = id)
        }
    }

    /**
     * Adds carousels: horizontal with one and with two slides at a time, looping, and vertical.
     *
     * @param hooks what the page needs from its surroundings
     */
    private fun ComponentScope.carousels(hooks: Hooks) = section("carousels", "Carousel") {
        Row(gap = 12, crossAlign = Alignment.CENTER, id = "carousel_row") {
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
    private fun ComponentScope.carouselDemo(id: String, basis: Double, orientation: Orientation, loop: Boolean, hooks: Hooks) {
        val vertical = orientation == Orientation.VERTICAL
        Carousel(width = ElementSize.fixed(if (vertical) 120 else 160), orientation = orientation, loop = loop, height = if (vertical) ElementSize.fixed(150) else ElementSize.FIT, onChange = changed(hooks, "Folie $id"), id = id) {
            CarouselPrevious(id = "${id}_prev")
            CarouselContent(id = "${id}_content") {
                (1..5).forEach { number ->
                    CarouselItem(basis, id = "${id}_slide_$number") {
                        Column(width = ElementSize.grow(), padding = Spacing(2, 2, 2, 2), crossAlign = Alignment.STRETCH, id = "${id}_slide_${number}_pad") {
                            Card(width = ElementSize.grow(), id = "${id}_card_$number") {
                                Column(width = ElementSize.grow(), height = ElementSize.fixed(if (vertical) 40 else 60), mainAlign = Alignment.CENTER, crossAlign = Alignment.CENTER, id = "${id}_card_${number}_body") {
                                    H3(Component.text("$number"), id = "${id}_card_${number}_text")
                                }
                            }
                        }
                    }
                }
            }
            CarouselNext(id = "${id}_next")
        }
    }

    /**
     * Adds a navigation menu with two items that open content and a link.
     *
     * @param hooks what the page needs from its surroundings
     * @param clicked the handler that reports clicks
     */
    private fun ComponentScope.navigationMenus(hooks: Hooks, clicked: ButtonHandler) = section("navigation_menus", "Navigation Menu") {
        NavigationMenu(id = "navigation_menu") {
            NavigationMenuList(id = "navigation_menu_list") {
                NavigationMenuItem(onChange = changed(hooks, "Menü Einstieg offen"), id = "navigation_start_item") {
                    NavigationMenuTrigger(Component.text("Einstieg"), id = "navigation_start_trigger")
                    NavigationMenuContent(id = "navigation_start_content") {
                        Column(width = ElementSize.fixed(220), gap = 2, crossAlign = Alignment.STRETCH, id = "navigation_start_links") {
                            navigationLink("navigation_intro", "Einführung", "Die Leitstelle in fünf Minuten.", clicked)
                            navigationLink("navigation_rules", "Regeln", "Was im Funk erlaubt ist.", clicked)
                        }
                    }
                }
                NavigationMenuItem(onChange = changed(hooks, "Menü Einheiten offen"), id = "navigation_units_item") {
                    NavigationMenuTrigger(Component.text("Einheiten"), id = "navigation_units_trigger")
                    NavigationMenuContent(id = "navigation_units_content") {
                        Column(width = ElementSize.fixed(180), gap = 2, crossAlign = Alignment.STRETCH, id = "navigation_units_links") {
                            navigationLink("navigation_rtw", "Rettungswagen", "Notfallrettung", clicked)
                            navigationLink("navigation_nef", "Notarzt", "Notarzteinsatzfahrzeug", clicked)
                            navigationLink("navigation_ktw", "Krankentransport", "Planbare Fahrten", clicked, enabled = false)
                        }
                    }
                }
                NavigationMenuItem(id = "navigation_docs_item") {
                    NavigationMenuLink(onClick = clicked, id = "navigation_docs") { Label(Component.text("Dokumente"), id = "navigation_docs_text") }
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
    private fun ComponentScope.navigationLink(id: String, title: String, description: String, clicked: ButtonHandler, enabled: Boolean = true) {
        NavigationMenuLink(enabled = enabled, onClick = clicked, id = id) {
            Small(Component.text(title), id = "${id}_title")
            Muted(Component.text(description), id = "${id}_description")
        }
    }

    /**
     * Adds a right-to-left direction with a breadcrumb, a pagination, tabs, an accordion, a
     * carousel, a menu with a sub-menu, a sheet on the left and a nested left-to-right row.
     *
     * @param hooks what the page needs from its surroundings
     * @param clicked the handler that reports clicks
     */
    private fun ComponentScope.directions(hooks: Hooks, clicked: ButtonHandler) = section("directions", "Direction (rechts nach links)") {
        Direction(LayoutDirection.RTL, width = ElementSize.grow(), id = "direction_rtl") {
            Column(width = ElementSize.grow(), gap = 8, id = "direction_body") {
                breadcrumbTrail("direction_breadcrumb", "chevron-right", clicked)
                Pagination(id = "direction_pagination") {
                    PaginationContent(id = "direction_pagination_content") {
                        PaginationItem(id = "direction_prev_item") { PaginationPrevious(onClick = clicked, id = "direction_prev") }
                        PaginationItem(id = "direction_1_item") { PaginationLink(Component.text("1"), active = true, onClick = clicked, id = "direction_1") }
                        PaginationItem(id = "direction_2_item") { PaginationLink(Component.text("2"), onClick = clicked, id = "direction_2") }
                        PaginationItem(id = "direction_next_item") { PaginationNext(onClick = clicked, id = "direction_next") }
                    }
                }
                Row(width = ElementSize.grow(), gap = 12, id = "direction_row") {
                    tabsDemo("direction_tabs", TabsVariant.LINE, Orientation.HORIZONTAL, hooks)
                    Column(width = ElementSize.grow(), crossAlign = Alignment.STRETCH, id = "direction_accordion_box") {
                        Accordion(AccordionType.SINGLE, collapsible = true, onChange = changed(hooks, "Accordion rechts nach links"), id = "direction_accordion") {
                            AccordionItem("a", id = "direction_accordion_a") {
                                AccordionTrigger(Component.text("Gespiegelt"), id = "direction_accordion_a_trigger")
                                AccordionContent(id = "direction_accordion_a_content") { P(Component.text("Der Pfeil steht links."), id = "direction_accordion_a_text") }
                            }
                        }
                    }
                }
                Row(gap = 12, crossAlign = Alignment.CENTER, id = "direction_controls") {
                    carouselDemo("direction_carousel", 50.0, Orientation.HORIZONTAL, loop = false, hooks)
                    DropdownMenu(align = Alignment.START, id = "direction_menu") {
                        Button(Component.text("Menü"), submitsInput = false, icon = "menu", variant = ButtonVariant.OUTLINE, id = "direction_menu_trigger")
                        MenuContent(id = "direction_menu_content") {
                            MenuItem(Component.text("Profil"), icon = "user", shortcut = Component.text("⇧P"), onClick = clicked, id = "direction_menu_profile")
                            MenuSub(id = "direction_menu_sub") {
                                MenuSubTrigger(Component.text("Einladen"), icon = "user-plus", id = "direction_menu_sub_trigger")
                                MenuContent(id = "direction_menu_sub_content") {
                                    MenuItem(Component.text("Per Funk"), icon = "radio", onClick = clicked, id = "direction_menu_sub_radio")
                                }
                            }
                        }
                    }
                    Sheet(id = "direction_sheet") {
                        Button(Component.text("Sheet links"), submitsInput = false, variant = ButtonVariant.OUTLINE, id = "direction_sheet_trigger")
                        SheetContent(OverlaySide.LEFT, id = "direction_sheet_content") {
                            SheetHeader(id = "direction_sheet_header") {
                                SheetTitle(Component.text("Gespiegelt"), id = "direction_sheet_title")
                                SheetDescription(Component.text("Links angefordert, rechts geöffnet."), id = "direction_sheet_description")
                            }
                        }
                    }
                }
                Direction(LayoutDirection.LTR, id = "direction_ltr") {
                    Row(gap = 6, crossAlign = Alignment.CENTER, id = "direction_ltr_row") {
                        Badge(Component.text("Verschachtelt"), variant = BadgeVariant.SECONDARY, id = "direction_ltr_badge")
                        Muted(Component.text("Diese Zeile läuft wieder von links nach rechts."), id = "direction_ltr_text")
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
    private fun ComponentScope.sidebars(hooks: Hooks, clicked: ButtonHandler, theme: String, variant: ScreenVariant, setup: SidebarSetup) = section("sidebars", "Sidebar") {
        Row(gap = 4, crossAlign = Alignment.CENTER, id = "sidebar_setup") {
            Select(SIDES, selected = setup.side.name, width = ElementSize.grow(), onChange = { change ->
                hooks.reopen(theme, variant, setup.copy(side = SidebarSide.valueOf(change.value)))
            }, id = "sidebar_side")
            Select(VARIANTS, selected = setup.variant.name, width = ElementSize.grow(), onChange = { change ->
                hooks.reopen(theme, variant, setup.copy(variant = SidebarVariant.valueOf(change.value)))
            }, id = "sidebar_variant")
            Select(COLLAPSIBLES, selected = setup.collapsible.name, width = ElementSize.grow(), onChange = { change ->
                hooks.reopen(theme, variant, setup.copy(collapsible = SidebarCollapsible.valueOf(change.value)))
            }, id = "sidebar_collapsible")
        }
        SidebarProvider(ElementSize.grow(), ElementSize.fixed(320), onChange = { change ->
            val state = if (change.value == "true") "ausgeklappt" else "eingeklappt"
            hooks.report(Component.text("Sidebar $state", NamedTextColor.AQUA))
        }, id = "sidebar_provider") {
            Sidebar(side = setup.side, variant = setup.variant, collapsible = setup.collapsible, id = "sidebar") {
                SidebarHeader(id = "sidebar_header") {
                    SidebarMenu(id = "sidebar_header_menu") {
                        SidebarMenuItem(id = "sidebar_org_item") {
                            SidebarMenuButton(Component.text("Leitstelle"), icon = "radio-tower", size = SidebarMenuButtonSize.LG, tooltip = Component.text("Leitstelle"), onClick = clicked, id = "sidebar_org")
                        }
                    }
                    SidebarInput(Component.text("Suchen..."), id = "sidebar_search")
                }
                SidebarSeparator(id = "sidebar_separator")
                SidebarContent(id = "sidebar_content") {
                    SidebarGroup(id = "sidebar_group_main") {
                        SidebarGroupLabel(Component.text("Dienst"), id = "sidebar_group_main_label")
                        SidebarGroupAction(onClick = clicked, id = "sidebar_group_main_action")
                        SidebarGroupContent(id = "sidebar_group_main_content") {
                            SidebarMenu(id = "sidebar_menu_main") {
                                SidebarMenuItem(id = "sidebar_home_item") {
                                    SidebarMenuButton(Component.text("Übersicht"), icon = "house", active = true, tooltip = Component.text("Übersicht"), onClick = clicked, id = "sidebar_home")
                                }
                                SidebarMenuItem(id = "sidebar_calls_item") {
                                    SidebarMenuButton(Component.text("Einsätze"), icon = "siren", tooltip = Component.text("Einsätze"), onClick = clicked, id = "sidebar_calls")
                                    SidebarMenuBadge(Component.text("12"), id = "sidebar_calls_badge")
                                }
                                SidebarMenuItem(id = "sidebar_units_item") {
                                    SidebarMenuButton(Component.text("Einheiten"), icon = "ambulance", tooltip = Component.text("Einheiten"), onClick = clicked, id = "sidebar_units")
                                    SidebarMenuAction(showOnHover = true, onClick = clicked, id = "sidebar_units_action")
                                    SidebarMenuSub(id = "sidebar_units_sub") {
                                        SidebarMenuSubItem(id = "sidebar_units_rtw_item") {
                                            SidebarMenuSubButton(Component.text("RTW 1"), active = true, onClick = clicked, id = "sidebar_units_rtw")
                                        }
                                        SidebarMenuSubItem(id = "sidebar_units_nef_item") {
                                            SidebarMenuSubButton(Component.text("NEF 2"), onClick = clicked, id = "sidebar_units_nef")
                                        }
                                        SidebarMenuSubItem(id = "sidebar_units_ktw_item") {
                                            SidebarMenuSubButton(Component.text("KTW 3 (außer Dienst)"), size = SidebarMenuSubButtonSize.SM, enabled = false, onClick = clicked, id = "sidebar_units_ktw")
                                        }
                                    }
                                }
                                SidebarMenuItem(id = "sidebar_archive_item") {
                                    SidebarMenuButton(Component.text("Archiv"), icon = "archive", size = SidebarMenuButtonSize.SM, tooltip = Component.text("Archiv"), enabled = false, onClick = clicked, id = "sidebar_archive")
                                }
                            }
                        }
                    }
                    SidebarGroup(id = "sidebar_group_more") {
                        SidebarGroupLabel(Component.text("Weiteres"), id = "sidebar_group_more_label")
                        SidebarGroupContent(id = "sidebar_group_more_content") {
                            SidebarMenu(id = "sidebar_menu_more") {
                                SidebarMenuItem(id = "sidebar_settings_item") {
                                    SidebarMenuButton(Component.text("Einstellungen"), icon = "settings", variant = SidebarMenuButtonVariant.OUTLINE, tooltip = Component.text("Einstellungen"), onClick = clicked, id = "sidebar_settings")
                                }
                                SidebarMenuItem(id = "sidebar_loading_1") { SidebarMenuSkeleton(showIcon = true, id = "sidebar_skeleton_1") }
                                SidebarMenuItem(id = "sidebar_loading_2") { SidebarMenuSkeleton(id = "sidebar_skeleton_2") }
                            }
                        }
                    }
                }
                SidebarFooter(id = "sidebar_footer") {
                    SidebarMenu(id = "sidebar_footer_menu") {
                        SidebarMenuItem(id = "sidebar_user_item") {
                            SidebarMenuButton(Component.text("Ammo"), icon = "circle-user", tooltip = Component.text("Ammo"), onClick = clicked, id = "sidebar_user")
                            SidebarMenuAction(icon = "log-out", onClick = clicked, id = "sidebar_user_action")
                        }
                    }
                }
                SidebarRail(id = "sidebar_rail")
            }
            SidebarInset(id = "sidebar_inset") {
                Row(gap = 6, crossAlign = Alignment.CENTER, id = "sidebar_inset_bar") {
                    SidebarTrigger(id = "sidebar_trigger")
                    H4(Component.text("Übersicht"), id = "sidebar_inset_title")
                }
                Muted(Component.text("Mit dem Knopf oben links, der Leiste am Rand oder Strg+B klappt die Sidebar ein und aus."), id = "sidebar_inset_text")
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
