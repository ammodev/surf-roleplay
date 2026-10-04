package dev.slne.surf.roleplay.api.client.common.screen.dsl

import dev.slne.surf.roleplay.api.client.common.screen.ChartKind
import dev.slne.surf.roleplay.api.client.common.screen.ChartSeries
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.LayoutDirection
import dev.slne.surf.roleplay.api.client.common.screen.RadioChoice
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoice
import dev.slne.surf.roleplay.api.client.common.screen.ToggleGroupChoice
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

/**
 * A screen that uses every component of the component DSL at least once.
 */
object KitchenSink {
    /**
     * Builds the screen.
     *
     * @return the definition of the screen
     */
    fun definition(): ScreenDefinition = Screen(Component.text("Alles")) {
        Column {
            layoutActionsAndInputs()
            display()
            overlays()
            navigation()
            data()
        }
    }

    /**
     * Adds the layout, action and input components.
     */
    private fun ComponentScope.layoutActionsAndInputs() {
        val options = listOf(SelectChoice("a", Component.text("A")))
        Row { ScrollList { Label("Text") } }
        ButtonGroup {
            Button("Eins")
            ButtonGroupSeparator()
            ButtonGroupText("Zwei")
        }
        Toggle("Fett")
        ToggleGroup(listOf(ToggleGroupChoice("a")))
        Form {
            FieldSet {
                FieldLegend("Person")
                FieldGroup {
                    Field {
                        FieldLabel("Name")
                        FieldContent { FieldTitle("Titel") }
                        FieldSeparator()
                    }
                }
            }
        }
        Input()
        NumberInput()
        Textarea()
        InputOtp()
        InputGroup {
            InputGroupAddon { InputGroupText("@") }
            Input()
        }
        Checkbox()
        Switch()
        RadioGroup(listOf(RadioChoice("a", Component.text("A"))))
        Slider(listOf(1.0))
        Select(options)
        Combobox(options)
        Calendar()
    }

    /**
     * Adds the display components.
     */
    private fun ComponentScope.display() {
        H1("H1"); H2("H2"); H3("H3"); H4("H4"); P("P"); Lead("Lead"); Large("Large"); Small("Small"); Muted("Muted")
        Blockquote("Zitat"); InlineCode("code")
        TypographyList(listOf(Component.text("a")))
        Separator()
        KbdGroup { Kbd("Strg") }
        Badge("Neu")
        Skeleton(ElementSize.fixed(10), ElementSize.fixed(10))
        Spinner()
        Progress(0.5f)
        AspectRatio(1f) { Image(Key.key("surf-roleplay", "textures/gui/logo.png")) }
        AvatarGroup { Avatar("AB"); AvatarGroupCount("+3") }
        Alert { AlertTitle("Titel"); AlertDescription("Text") }
        Card {
            CardHeader { CardTitle("Titel"); CardDescription("Text"); CardAction { Icon("x") } }
            CardContent { }
            CardFooter { }
        }
        Empty {
            EmptyHeader { EmptyMedia(); EmptyTitle("Leer"); EmptyDescription("Nichts") }
            EmptyContent { }
        }
        ItemGroup {
            Item {
                ItemHeader { }
                ItemMedia()
                ItemContent { ItemTitle("Titel"); ItemDescription("Text") }
                ItemActions { }
                ItemFooter { }
            }
            ItemSeparator()
        }
    }

    /**
     * Adds the overlay and menu components.
     */
    private fun ComponentScope.overlays() {
        OverlayContainer {
            Popover { Button("P"); PopoverContent { PopoverHeader { PopoverTitle("T"); PopoverDescription("D") } } }
            HoverCard { Button("H"); HoverCardContent { } }
            Tooltip("Tipp") { Button("T") }
            DropdownMenu {
                Button("M")
                MenuContent {
                    MenuLabel("L")
                    MenuGroup { MenuItem("I") }
                    MenuSeparator()
                    MenuCheckboxItem("C")
                    MenuRadioGroup { MenuRadioItem("R", "r") }
                    MenuSub { MenuSubTrigger("S"); MenuContent { } }
                }
            }
            ContextMenu { Label("Bereich"); MenuContent { } }
            Menubar { MenubarMenu { MenubarTrigger("Datei"); MenuContent { } } }
            Command {
                CommandInput()
                CommandList {
                    CommandEmpty("Nichts")
                    CommandGroup("G") { CommandItem("Eins") }
                    CommandSeparator()
                }
            }
            Dialog {
                Button("D")
                DialogContent {
                    DialogHeader { DialogTitle("T"); DialogDescription("D") }
                    DialogFooter { DialogClose { Button("Schließen") } }
                }
            }
            AlertDialog {
                Button("A")
                AlertDialogContent { AlertDialogMedia("trash"); AlertDialogAction("Ja"); AlertDialogCancel("Nein") }
            }
            Sheet {
                Button("S")
                SheetContent { SheetHeader { SheetTitle("T"); SheetDescription("D") }; SheetFooter { } }
            }
            Drawer { Button("D"); DrawerContent { } }
        }
    }

    /**
     * Adds the navigation and layout components.
     */
    private fun ComponentScope.navigation() {
        Collapsible { CollapsibleTrigger { Button("Auf") }; CollapsibleContent { } }
        Accordion { AccordionItem("a") { AccordionTrigger("A"); AccordionContent { } } }
        Tabs { TabsList { TabsTrigger("a", "A") }; TabsContent("a") { } }
        Breadcrumb {
            BreadcrumbList {
                BreadcrumbItem { BreadcrumbLink("Start") }
                BreadcrumbSeparator()
                BreadcrumbItem { BreadcrumbEllipsis() }
                BreadcrumbItem { BreadcrumbPage("Akte") }
            }
        }
        Pagination {
            PaginationContent {
                PaginationItem { PaginationPrevious() }
                PaginationItem { PaginationLink("1") }
                PaginationItem { PaginationEllipsis() }
                PaginationItem { PaginationNext() }
            }
        }
        ScrollArea(ElementSize.fixed(50), ElementSize.fixed(50)) { }
        ResizablePanelGroup { ResizablePanel { }; ResizableHandle(); ResizablePanel { } }
        Carousel(ElementSize.fixed(100)) { CarouselContent { CarouselItem { } }; CarouselPrevious(); CarouselNext() }
        NavigationMenu {
            NavigationMenuList {
                NavigationMenuItem { NavigationMenuTrigger("Mehr"); NavigationMenuContent { NavigationMenuLink { Label("Link") } } }
            }
        }
        SidebarProvider {
            Sidebar {
                SidebarHeader { SidebarInput() }
                SidebarContent {
                    SidebarGroup {
                        SidebarGroupLabel("Gruppe")
                        SidebarGroupAction()
                        SidebarGroupContent {
                            SidebarMenu {
                                SidebarMenuItem {
                                    SidebarMenuButton("Start")
                                    SidebarMenuAction()
                                    SidebarMenuBadge("3")
                                    SidebarMenuSub { SidebarMenuSubItem { SidebarMenuSubButton("Unter") } }
                                }
                                SidebarMenuItem { SidebarMenuSkeleton() }
                            }
                        }
                    }
                    SidebarSeparator()
                }
                SidebarFooter { }
                SidebarRail()
            }
            SidebarInset { SidebarTrigger() }
        }
        Direction(LayoutDirection.RTL) { }
    }

    /**
     * Adds the data, chart and chat components.
     */
    private fun ComponentScope.data() {
        Table {
            TableHeader { TableRow { TableHead("Name") } }
            TableBody { TableRow { TableCell("Ada") } }
            TableFooter { TableRow { TableCell { } } }
            TableCaption("Einheiten")
        }
        DataTable {
            DataTableColumn("name", "Name")
            DataTableRow { DataTableCell("Ada", "ada") }
        }
        Chart(ChartKind.LINE, listOf(Component.text("Jan")), listOf(ChartSeries("a", Component.text("A"), 1, listOf(1.0))))
        ChatView(ElementSize.fixed(80)) { ChatMessage { P("Hallo") } }
    }
}
