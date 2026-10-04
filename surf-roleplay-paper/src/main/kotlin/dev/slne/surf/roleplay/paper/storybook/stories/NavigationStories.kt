package dev.slne.surf.roleplay.paper.storybook.stories

import dev.slne.surf.roleplay.api.client.common.screen.AccordionType
import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.BadgeVariant
import dev.slne.surf.roleplay.api.client.common.screen.ButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.ButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.LayoutDirection
import dev.slne.surf.roleplay.api.client.common.screen.Orientation
import dev.slne.surf.roleplay.api.client.common.screen.ScrollOrientation
import dev.slne.surf.roleplay.api.client.common.screen.SidebarCollapsible
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuSubButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.SidebarSide
import dev.slne.surf.roleplay.api.client.common.screen.SidebarVariant
import dev.slne.surf.roleplay.api.client.common.screen.Spacing
import dev.slne.surf.roleplay.api.client.common.screen.TabsVariant
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
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Direction
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DropdownMenu
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DropdownMenuContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DropdownMenuItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.H3
import dev.slne.surf.roleplay.api.client.common.screen.dsl.H4
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Label
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Large
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
import dev.slne.surf.roleplay.paper.storybook.Story
import dev.slne.surf.roleplay.paper.storybook.StoryCategory
import dev.slne.surf.roleplay.paper.storybook.StoryContext
import dev.slne.surf.roleplay.paper.storybook.slug
import dev.slne.surf.roleplay.paper.storybook.storySection
import net.kyori.adventure.text.Component

/**
 * The stories of the navigation and layout components, in sidebar order.
 */
internal val NAVIGATION_STORIES: List<Story> = listOf(
    Story("accordion", "Akkordeon", StoryCategory.NAVIGATION) { accordionStory(it) },
    Story("collapsible", "Ausklappbereich", StoryCategory.NAVIGATION) { collapsibleStory(it) },
    Story("tabs", "Reiter", StoryCategory.NAVIGATION) { tabsStory(it) },
    Story("breadcrumb", "Brotkrumen", StoryCategory.NAVIGATION) { breadcrumbStory(it) },
    Story("pagination", "Seitenwahl", StoryCategory.NAVIGATION) { paginationStory(it) },
    Story("navigation-menu", "Navigationsmenü", StoryCategory.NAVIGATION) { navigationMenuStory(it) },
    Story("scroll-area", "Scrollbereich", StoryCategory.NAVIGATION) { scrollAreaStory() },
    Story("resizable", "Größenänderung", StoryCategory.NAVIGATION) { resizableStory(it) },
    Story("carousel", "Karussell", StoryCategory.NAVIGATION) { carouselStory(it) },
    Story("direction", "Leserichtung", StoryCategory.NAVIGATION) { directionStory(it) },
    Story("sidebar", "Seitenleiste", StoryCategory.NAVIGATION) { sidebarStory(it) },
)

/**
 * Shows accordions of every type, collapsible, with an open item and a disabled item.
 *
 * @param context the story context
 */
private fun ComponentScope.accordionStory(context: StoryContext) {
    storySection("Typen") {
        Row(gap = 12, width = ElementSize.grow()) {
            AccordionType.entries.forEach { type ->
                Column(width = ElementSize.grow(), gap = 4, crossAlign = Alignment.STRETCH) {
                    Muted(type.slug())
                    Accordion(type, collapsible = true, value = listOf("a"), id = "accordion_${type.slug()}", onChange = context.changed) {
                        AccordionItem("a") {
                            AccordionTrigger(Component.text("Fahrzeuge"))
                            AccordionContent { P("RTW, NEF, KTW") }
                        }
                        AccordionItem("b") {
                            AccordionTrigger(Component.text("Wachen"))
                            AccordionContent { P("Nord, Süd, Mitte") }
                        }
                        AccordionItem("locked", enabled = false) {
                            AccordionTrigger(Component.text("Gesperrt"))
                            AccordionContent { P("Nicht sichtbar.") }
                        }
                    }
                }
            }
        }
    }
    storySection("Beispiel") {
        Accordion(id = "accordion_faq", onChange = context.changed) {
            AccordionItem("report") {
                AccordionTrigger(Component.text("Wie melde ich einen Einsatz?"))
                AccordionContent { P("Über das Funkgerät oder die Leitstellen-App.") }
            }
            AccordionItem("alarm") {
                AccordionTrigger(Component.text("Wer darf Einheiten alarmieren?"))
                AccordionContent { P("Nur Disponenten mit Freigabe.") }
            }
        }
    }
}

/**
 * Shows a closed and an open collapsible.
 *
 * @param context the story context
 */
private fun ComponentScope.collapsibleStory(context: StoryContext) {
    storySection("Geschlossen und offen") {
        listOf(false, true).forEach { open ->
            Collapsible(open = open, id = "collapsible_$open", onChange = context.changed) {
                Row(width = ElementSize.grow(), gap = 6, crossAlign = Alignment.CENTER) {
                    Large("@ammo hat 3 Funkkanäle", width = ElementSize.grow())
                    CollapsibleTrigger {
                        Button(Component.empty(), submitsInput = false, icon = "chevrons-up-down", variant = ButtonVariant.GHOST, size = ButtonSize.ICON_SM, id = "collapsible_toggle_$open")
                    }
                }
                Label("Kanal 1: Leitstelle")
                CollapsibleContent {
                    Label("Kanal 2: Rettungsdienst")
                    Label("Kanal 3: Polizei")
                }
            }
        }
    }
}

/**
 * Shows tabs in every list variant and orientation, with an icon and a disabled tab.
 *
 * @param context the story context
 */
private fun ComponentScope.tabsStory(context: StoryContext) {
    storySection("Varianten und Ausrichtungen") {
        Orientation.entries.forEach { orientation ->
            Row(gap = 12, width = ElementSize.grow()) {
                TabsVariant.entries.forEach { variant -> missionTabs("tabs_${variant.slug()}_${orientation.slug()}", variant, orientation, context) }
            }
        }
    }
}

/**
 * Adds tabs with an overview, a report and a disabled tab.
 *
 * @param id the id of the tabs
 * @param variant how the list is drawn
 * @param orientation whether the list is above or beside the contents
 * @param context the story context
 */
private fun ComponentScope.missionTabs(id: String, variant: TabsVariant, orientation: Orientation, context: StoryContext) {
    Tabs(value = "overview", orientation = orientation, id = id, onChange = context.changed) {
        TabsList(variant) {
            TabsTrigger("overview", Component.text("Übersicht"), icon = "layout-dashboard")
            TabsTrigger("report", Component.text("Bericht"))
            TabsTrigger("locked", Component.text("Gesperrt"), enabled = false)
        }
        TabsContent("overview") { Muted("3 offene Einsätze") }
        TabsContent("report") { Muted("Bericht vom 29.09.2026") }
        TabsContent("locked") {}
    }
}

/**
 * Shows breadcrumbs with the default and a custom separator, an ellipsis menu and a disabled
 * link.
 *
 * @param context the story context
 */
private fun ComponentScope.breadcrumbStory(context: StoryContext) {
    storySection("Trenner") {
        listOf("chevron-right", "slash").forEach { separator -> missionTrail("breadcrumb_$separator", separator, context) }
    }
}

/**
 * Adds a breadcrumb from the start page to the current mission, with an ellipsis menu for the
 * hidden levels.
 *
 * @param id the prefix of the ids of the breadcrumb's links
 * @param separator the Lucide icon of the separators
 * @param context the story context
 */
private fun ComponentScope.missionTrail(id: String, separator: String, context: StoryContext) {
    Breadcrumb {
        BreadcrumbList {
            BreadcrumbItem { BreadcrumbLink("Start", id = "${id}_home", onClick = context.clicked) }
            BreadcrumbSeparator(separator)
            BreadcrumbItem {
                DropdownMenu(align = Alignment.START) {
                    BreadcrumbEllipsis()
                    DropdownMenuContent {
                        DropdownMenuItem("Dokumente", id = "${id}_docs", onClick = context.clicked)
                        DropdownMenuItem("Einheiten", id = "${id}_units", onClick = context.clicked)
                    }
                }
            }
            BreadcrumbSeparator(separator)
            BreadcrumbItem { BreadcrumbLink("Archiv", enabled = false, id = "${id}_archive") }
            BreadcrumbSeparator(separator)
            BreadcrumbItem { BreadcrumbLink("Einsätze", id = "${id}_calls", onClick = context.clicked) }
            BreadcrumbSeparator(separator)
            BreadcrumbItem { BreadcrumbPage(Component.text("Einsatz 42")) }
        }
    }
}

/**
 * Shows a pagination with an active page, an ellipsis, disabled links, and one at the start.
 *
 * @param context the story context
 */
private fun ComponentScope.paginationStory(context: StoryContext) {
    storySection("Mitte") {
        Pagination {
            PaginationContent {
                PaginationItem { PaginationPrevious(id = "pagination_prev", onClick = context.clicked) }
                PaginationItem { PaginationLink("1", id = "pagination_1", onClick = context.clicked) }
                PaginationItem { PaginationLink("2", active = true, id = "pagination_2", onClick = context.clicked) }
                PaginationItem { PaginationLink("3", id = "pagination_3", onClick = context.clicked) }
                PaginationItem { PaginationEllipsis() }
                PaginationItem { PaginationNext(id = "pagination_next", onClick = context.clicked) }
            }
        }
    }
    storySection("Erste Seite") {
        Pagination {
            PaginationContent {
                PaginationItem { PaginationPrevious(enabled = false, id = "pagination_first_prev") }
                PaginationItem { PaginationLink("1", active = true, id = "pagination_first_1", onClick = context.clicked) }
                PaginationItem { PaginationLink("2", id = "pagination_first_2", onClick = context.clicked) }
                PaginationItem { PaginationLink("3", enabled = false, id = "pagination_first_3") }
                PaginationItem { PaginationNext(id = "pagination_first_next", onClick = context.clicked) }
            }
        }
    }
}

/**
 * Shows a navigation menu with two items that open content and a link.
 *
 * @param context the story context
 */
private fun ComponentScope.navigationMenuStory(context: StoryContext) {
    storySection("Beispiel") {
        NavigationMenu {
            NavigationMenuList {
                NavigationMenuItem(id = "navigation_start", onChange = context.changed) {
                    NavigationMenuTrigger(Component.text("Einstieg"))
                    NavigationMenuContent {
                        Column(width = ElementSize.fixed(220), gap = 2, crossAlign = Alignment.STRETCH) {
                            navigationLink("navigation_intro", "Einführung", "Die Leitstelle in fünf Minuten.", context)
                            navigationLink("navigation_rules", "Regeln", "Was im Funk erlaubt ist.", context, active = true)
                        }
                    }
                }
                NavigationMenuItem(id = "navigation_units", onChange = context.changed) {
                    NavigationMenuTrigger(Component.text("Einheiten"))
                    NavigationMenuContent {
                        Column(width = ElementSize.fixed(180), gap = 2, crossAlign = Alignment.STRETCH) {
                            navigationLink("navigation_rtw", "Rettungswagen", "Notfallrettung", context)
                            navigationLink("navigation_ktw", "Krankentransport", "Planbare Fahrten", context, enabled = false)
                        }
                    }
                }
                NavigationMenuItem {
                    NavigationMenuLink(id = "navigation_docs", onClick = context.clicked) { Label("Dokumente") }
                }
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
 * @param context the story context
 * @param active whether the link is the current page
 * @param enabled whether the link can be used
 */
private fun ComponentScope.navigationLink(id: String, title: String, description: String, context: StoryContext, active: Boolean = false, enabled: Boolean = true) {
    NavigationMenuLink(active = active, enabled = enabled, id = id, onClick = context.clicked) {
        Small(title)
        Muted(description)
    }
}

/**
 * Shows scroll areas in every scroll orientation.
 */
private fun ComponentScope.scrollAreaStory() {
    storySection("Richtungen") {
        Row(gap = 12) {
            ScrollOrientation.entries.forEach { orientation ->
                Column(gap = 2) {
                    Muted(orientation.slug())
                    when (orientation) {
                        ScrollOrientation.VERTICAL -> ScrollArea(ElementSize.fixed(140), ElementSize.fixed(100), orientation) {
                            Column(width = ElementSize.grow(), gap = 4, padding = Spacing(4, 6, 4, 6), crossAlign = Alignment.STRETCH) {
                                (1..20).forEach { Label("RTW $it") }
                            }
                        }
                        ScrollOrientation.HORIZONTAL -> ScrollArea(ElementSize.fixed(160), ElementSize.fixed(40), orientation) {
                            Row(gap = 4, padding = Spacing(4, 4, 4, 4)) {
                                (1..15).forEach { Badge("Wache $it", variant = BadgeVariant.OUTLINE) }
                            }
                        }
                        ScrollOrientation.BOTH -> ScrollArea(ElementSize.fixed(140), ElementSize.fixed(100), orientation) {
                            Column(gap = 4, padding = Spacing(4, 4, 4, 4)) {
                                (1..12).forEach { row -> Row(gap = 4) { (1..8).forEach { Label("Feld $row.$it") } } }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Shows resizable groups in both orientations, with and without a grip, nested.
 *
 * @param context the story context
 */
private fun ComponentScope.resizableStory(context: StoryContext) {
    storySection("Beispiel") {
        ResizablePanelGroup(height = ElementSize.fixed(120), id = "resizable", onChange = context.changed) {
            ResizablePanel(defaultSize = 40.0, minSize = 20.0, maxSize = 70.0) { centeredLabel("Karte") }
            ResizableHandle(withHandle = true)
            ResizablePanel(defaultSize = 60.0) {
                ResizablePanelGroup(Orientation.VERTICAL, height = ElementSize.grow(), id = "resizable_nested", onChange = context.changed) {
                    ResizablePanel(defaultSize = 50.0, minSize = 25.0) { centeredLabel("Einsätze") }
                    ResizableHandle()
                    ResizablePanel(defaultSize = 50.0, minSize = 25.0) { centeredLabel("Funk") }
                }
            }
        }
    }
}

/**
 * Adds a centered text that fills its parent.
 *
 * @param text the text
 */
private fun ComponentScope.centeredLabel(text: String) {
    Column(width = ElementSize.grow(), height = ElementSize.grow(), mainAlign = Alignment.CENTER, crossAlign = Alignment.CENTER) {
        Large(text)
    }
}

/**
 * Shows carousels in both orientations, with one and two slides at a time, and looping.
 *
 * @param context the story context
 */
private fun ComponentScope.carouselStory(context: StoryContext) {
    storySection("Varianten") {
        Row(gap = 12, crossAlign = Alignment.CENTER) {
            numberCarousel("carousel_single", 100.0, Orientation.HORIZONTAL, loop = false, context)
            numberCarousel("carousel_half", 50.0, Orientation.HORIZONTAL, loop = true, context)
            numberCarousel("carousel_vertical", 50.0, Orientation.VERTICAL, loop = false, context)
        }
    }
}

/**
 * Adds a carousel of five numbered cards.
 *
 * @param id the id of the carousel
 * @param basis the share of the content every slide takes, in percent
 * @param orientation whether the slides move sideways or up and down
 * @param loop whether the last position is followed by the first
 * @param context the story context
 */
private fun ComponentScope.numberCarousel(id: String, basis: Double, orientation: Orientation, loop: Boolean, context: StoryContext) {
    val vertical = orientation == Orientation.VERTICAL
    Carousel(ElementSize.fixed(if (vertical) 120 else 160), orientation, loop, height = if (vertical) ElementSize.fixed(150) else ElementSize.FIT, id = id, onChange = context.changed) {
        CarouselPrevious()
        CarouselContent {
            (1..5).forEach { number ->
                CarouselItem(basis) {
                    Column(width = ElementSize.grow(), padding = Spacing(2, 2, 2, 2), crossAlign = Alignment.STRETCH) {
                        Card(width = ElementSize.grow()) {
                            Column(width = ElementSize.grow(), height = ElementSize.fixed(if (vertical) 40 else 60), mainAlign = Alignment.CENTER, crossAlign = Alignment.CENTER) {
                                H3("$number")
                            }
                        }
                    }
                }
            }
        }
        CarouselNext()
    }
}

/**
 * Shows every layout direction with mirrored navigation, and a nested direction.
 *
 * @param context the story context
 */
private fun ComponentScope.directionStory(context: StoryContext) {
    storySection("Richtungen") {
        LayoutDirection.entries.forEach { direction ->
            Direction(direction, width = ElementSize.grow()) {
                Column(width = ElementSize.grow(), gap = 6) {
                    Muted(direction.slug())
                    missionTrail("direction_${direction.slug()}", "chevron-right", context)
                    missionTabs("direction_tabs_${direction.slug()}", TabsVariant.LINE, Orientation.HORIZONTAL, context)
                }
            }
        }
    }
    storySection("Verschachtelt") {
        Direction(LayoutDirection.RTL, width = ElementSize.grow()) {
            Column(width = ElementSize.grow(), gap = 6) {
                Muted("Diese Zeile läuft von rechts nach links.")
                Direction(LayoutDirection.LTR) {
                    Row(gap = 6, crossAlign = Alignment.CENTER) {
                        Badge("Verschachtelt", variant = BadgeVariant.SECONDARY)
                        Muted("Diese Zeile läuft wieder von links nach rechts.")
                    }
                }
            }
        }
    }
}

/**
 * Shows a small sidebar layout for every side, variant and collapsing mode, and a full sidebar
 * with every part.
 *
 * @param context the story context
 */
private fun ComponentScope.sidebarStory(context: StoryContext) {
    storySection("Seiten, Varianten und Einklappen") {
        SidebarVariant.entries.forEach { variant -> miniSidebar(SidebarSide.LEFT, variant, SidebarCollapsible.ICON, context) }
        miniSidebar(SidebarSide.RIGHT, SidebarVariant.SIDEBAR, SidebarCollapsible.OFFCANVAS, context)
        miniSidebar(SidebarSide.LEFT, SidebarVariant.SIDEBAR, SidebarCollapsible.NONE, context)
    }
    storySection("Beispiel") {
        SidebarProvider(ElementSize.grow(), ElementSize.fixed(320), id = "sidebar_full", onChange = context.changed) {
            Sidebar(collapsible = SidebarCollapsible.ICON) {
                SidebarHeader {
                    SidebarMenu {
                        SidebarMenuItem {
                            SidebarMenuButton("Leitstelle", icon = "radio-tower", size = SidebarMenuButtonSize.LG, tooltip = Component.text("Leitstelle"), id = "sidebar_org", onClick = context.clicked)
                        }
                    }
                    SidebarInput(Component.text("Suchen …"))
                }
                SidebarSeparator()
                SidebarContent {
                    SidebarGroup {
                        SidebarGroupLabel("Dienst")
                        SidebarGroupAction(id = "sidebar_group_add", onClick = context.clicked)
                        SidebarGroupContent {
                            SidebarMenu {
                                SidebarMenuItem {
                                    SidebarMenuButton("Übersicht", icon = "house", active = true, id = "sidebar_home", onClick = context.clicked)
                                }
                                SidebarMenuItem {
                                    SidebarMenuButton("Einsätze", icon = "siren", id = "sidebar_calls", onClick = context.clicked)
                                    SidebarMenuBadge("12")
                                }
                                SidebarMenuItem {
                                    SidebarMenuButton("Einheiten", icon = "ambulance", id = "sidebar_units", onClick = context.clicked)
                                    SidebarMenuAction(showOnHover = true, id = "sidebar_units_action", onClick = context.clicked)
                                    SidebarMenuSub {
                                        SidebarMenuSubButtonSize.entries.forEach { size ->
                                            SidebarMenuSubItem {
                                                SidebarMenuSubButton("RTW ${size.slug()}", size = size, active = size == SidebarMenuSubButtonSize.MD, id = "sidebar_sub_${size.slug()}", onClick = context.clicked)
                                            }
                                        }
                                        SidebarMenuSubItem { SidebarMenuSubButton("KTW 3 (außer Dienst)", enabled = false, id = "sidebar_sub_disabled") }
                                    }
                                }
                                SidebarMenuItem {
                                    SidebarMenuButton("Archiv", icon = "archive", size = SidebarMenuButtonSize.SM, enabled = false, id = "sidebar_archive")
                                }
                                SidebarMenuItem {
                                    SidebarMenuButton("Einstellungen", icon = "settings", variant = SidebarMenuButtonVariant.OUTLINE, id = "sidebar_settings", onClick = context.clicked)
                                }
                                SidebarMenuItem { SidebarMenuSkeleton(showIcon = true) }
                                SidebarMenuItem { SidebarMenuSkeleton() }
                            }
                        }
                    }
                }
                SidebarFooter {
                    SidebarMenu {
                        SidebarMenuItem {
                            SidebarMenuButton("Ammo", icon = "circle-user", id = "sidebar_user", onClick = context.clicked)
                            SidebarMenuAction(icon = "log-out", id = "sidebar_logout", onClick = context.clicked)
                        }
                    }
                }
                SidebarRail()
            }
            SidebarInset {
                Row(gap = 6, crossAlign = Alignment.CENTER) {
                    SidebarTrigger()
                    H4("Übersicht")
                }
                Muted("Der Knopf oben links und die Leiste am Rand klappen die Seitenleiste ein und aus.", width = ElementSize.grow())
            }
        }
    }
}

/**
 * Adds a small sidebar layout with two menu buttons.
 *
 * @param side the side the sidebar is on
 * @param variant how the sidebar is drawn
 * @param collapsible how the sidebar collapses
 * @param context the story context
 */
private fun ComponentScope.miniSidebar(side: SidebarSide, variant: SidebarVariant, collapsible: SidebarCollapsible, context: StoryContext) {
    val id = "sidebar_${side.slug()}_${variant.slug()}_${collapsible.slug()}"
    SidebarProvider(ElementSize.grow(), ElementSize.fixed(110), id = id, onChange = context.changed) {
        Sidebar(side, variant, collapsible) {
            SidebarContent {
                SidebarGroup {
                    SidebarGroupLabel("${side.slug()}, ${variant.slug()}")
                    SidebarMenu {
                        SidebarMenuItem { SidebarMenuButton("Übersicht", icon = "house", active = true, id = "${id}_home", onClick = context.clicked) }
                        SidebarMenuItem { SidebarMenuButton("Einsätze", icon = "siren", id = "${id}_calls", onClick = context.clicked) }
                    }
                }
            }
        }
        SidebarInset {
            Row(gap = 6, crossAlign = Alignment.CENTER) {
                SidebarTrigger(enabled = collapsible != SidebarCollapsible.NONE)
                Muted("Einklappen: ${collapsible.slug()}")
            }
        }
    }
}
