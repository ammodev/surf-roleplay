package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldContentElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldSeparatorElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldSetElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldTextElement
import dev.slne.surf.roleplay.api.client.common.screen.FormElement
import dev.slne.surf.roleplay.protocol.screen.FieldContentNode
import dev.slne.surf.roleplay.protocol.screen.FieldGroupNode
import dev.slne.surf.roleplay.protocol.screen.FieldNode
import dev.slne.surf.roleplay.protocol.screen.FieldSeparatorNode
import dev.slne.surf.roleplay.protocol.screen.FieldSetNode
import dev.slne.surf.roleplay.protocol.screen.FieldTextNode
import dev.slne.surf.roleplay.protocol.screen.FormNode
import dev.slne.surf.roleplay.protocol.screen.SetInvalid
import dev.slne.surf.roleplay.api.client.common.screen.CalendarElement
import dev.slne.surf.roleplay.protocol.screen.CalendarNode
import dev.slne.surf.roleplay.protocol.screen.CalendarValues
import dev.slne.surf.roleplay.api.client.common.screen.RadioGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.SliderElement
import dev.slne.surf.roleplay.api.client.common.screen.SwitchElement
import dev.slne.surf.roleplay.protocol.screen.RadioGroupNode
import dev.slne.surf.roleplay.protocol.screen.RadioOption
import dev.slne.surf.roleplay.protocol.screen.SliderNode
import dev.slne.surf.roleplay.protocol.screen.SwitchNode
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupAddonElement
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupTextElement
import dev.slne.surf.roleplay.api.client.common.screen.InputOtpElement
import dev.slne.surf.roleplay.api.client.common.screen.TextareaElement
import dev.slne.surf.roleplay.protocol.screen.InputGroupAddonNode
import dev.slne.surf.roleplay.protocol.screen.InputGroupNode
import dev.slne.surf.roleplay.protocol.screen.InputGroupTextNode
import dev.slne.surf.roleplay.protocol.screen.InputOtpNode
import dev.slne.surf.roleplay.protocol.screen.TextareaNode
import dev.slne.surf.roleplay.api.client.common.screen.ButtonGroupSeparatorElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonGroupTextElement
import dev.slne.surf.roleplay.api.client.common.screen.ToggleElement
import dev.slne.surf.roleplay.api.client.common.screen.ToggleGroupElement
import dev.slne.surf.roleplay.protocol.screen.ButtonGroupNode
import dev.slne.surf.roleplay.protocol.screen.ButtonGroupSeparatorNode
import dev.slne.surf.roleplay.protocol.screen.ButtonGroupTextNode
import dev.slne.surf.roleplay.protocol.screen.ToggleGroupItem
import dev.slne.surf.roleplay.protocol.screen.ToggleNode
import dev.slne.surf.roleplay.protocol.screen.ToggleGroupNode
import dev.slne.surf.roleplay.api.client.common.screen.CheckboxElement
import dev.slne.surf.roleplay.api.client.common.screen.ColumnElement
import dev.slne.surf.roleplay.api.client.common.screen.ComboboxElement
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoiceGroup
import dev.slne.surf.roleplay.api.client.common.screen.SelectElement
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.ElementSizeMode
import dev.slne.surf.roleplay.api.client.common.screen.IconElement
import dev.slne.surf.roleplay.api.client.common.screen.IconTint
import dev.slne.surf.roleplay.api.client.common.screen.ImageElement
import dev.slne.surf.roleplay.api.client.common.screen.LabelElement
import dev.slne.surf.roleplay.api.client.common.screen.NumberInputElement
import dev.slne.surf.roleplay.api.client.common.screen.ProgressElement
import dev.slne.surf.roleplay.api.client.common.screen.RowElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenChange
import dev.slne.surf.roleplay.api.client.common.screen.ScreenElement
import dev.slne.surf.roleplay.api.client.common.screen.ScrollListElement
import dev.slne.surf.roleplay.api.client.common.screen.Spacing
import dev.slne.surf.roleplay.api.client.common.screen.TextInputElement
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.CheckboxNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.ComboboxNode
import dev.slne.surf.roleplay.protocol.screen.SelectGroup
import dev.slne.surf.roleplay.protocol.screen.SelectNode
import dev.slne.surf.roleplay.protocol.screen.SelectOption
import dev.slne.surf.roleplay.protocol.screen.SetOptions
import dev.slne.surf.roleplay.protocol.screen.IconColor
import dev.slne.surf.roleplay.protocol.screen.IconNode
import dev.slne.surf.roleplay.protocol.screen.ImageNode
import dev.slne.surf.roleplay.protocol.screen.InsertNode
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.NumberInputNode
import dev.slne.surf.roleplay.protocol.screen.PatchOperation
import dev.slne.surf.roleplay.protocol.screen.ProgressNode
import dev.slne.surf.roleplay.protocol.screen.RemoveNode
import dev.slne.surf.roleplay.protocol.screen.ReplaceNode
import dev.slne.surf.roleplay.protocol.screen.RowNode
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.ScrollListNode
import dev.slne.surf.roleplay.protocol.screen.SetEnabled
import dev.slne.surf.roleplay.protocol.screen.SetProgress
import dev.slne.surf.roleplay.protocol.screen.SetText
import dev.slne.surf.roleplay.protocol.screen.SetValue
import dev.slne.surf.roleplay.protocol.screen.SizeMode
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.TextInputNode
import dev.slne.surf.roleplay.api.client.common.screen.ScreenPresentation
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.api.client.common.screen.SheetSide
import dev.slne.surf.roleplay.protocol.screen.Presentation
import dev.slne.surf.roleplay.protocol.screen.SheetEdge
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import dev.slne.surf.roleplay.api.client.common.screen.TextElement
import dev.slne.surf.roleplay.api.client.common.screen.TextListElement
import dev.slne.surf.roleplay.api.client.common.screen.SeparatorElement
import dev.slne.surf.roleplay.api.client.common.screen.KbdElement
import dev.slne.surf.roleplay.api.client.common.screen.KbdGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.BadgeElement
import dev.slne.surf.roleplay.protocol.screen.TextNode
import dev.slne.surf.roleplay.protocol.screen.TextListNode
import dev.slne.surf.roleplay.protocol.screen.SeparatorNode
import dev.slne.surf.roleplay.protocol.screen.KbdNode
import dev.slne.surf.roleplay.protocol.screen.KbdGroupNode
import dev.slne.surf.roleplay.protocol.screen.BadgeNode
import dev.slne.surf.roleplay.api.client.common.screen.SkeletonElement
import dev.slne.surf.roleplay.api.client.common.screen.SpinnerElement
import dev.slne.surf.roleplay.api.client.common.screen.AspectRatioElement
import dev.slne.surf.roleplay.protocol.screen.SkeletonNode
import dev.slne.surf.roleplay.protocol.screen.SpinnerNode
import dev.slne.surf.roleplay.protocol.screen.AspectRatioNode
import dev.slne.surf.roleplay.api.client.common.screen.AvatarElement
import dev.slne.surf.roleplay.api.client.common.screen.AvatarGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.AvatarGroupCountElement
import dev.slne.surf.roleplay.api.client.common.screen.AvatarSource
import dev.slne.surf.roleplay.protocol.screen.AvatarNode
import dev.slne.surf.roleplay.protocol.screen.AvatarGroupNode
import dev.slne.surf.roleplay.protocol.screen.AvatarGroupCountNode
import dev.slne.surf.roleplay.api.client.common.screen.AlertElement
import dev.slne.surf.roleplay.api.client.common.screen.CardElement
import dev.slne.surf.roleplay.api.client.common.screen.CardHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.CardActionElement
import dev.slne.surf.roleplay.api.client.common.screen.CardContentElement
import dev.slne.surf.roleplay.api.client.common.screen.CardFooterElement
import dev.slne.surf.roleplay.protocol.screen.AlertNode
import dev.slne.surf.roleplay.protocol.screen.CardNode
import dev.slne.surf.roleplay.protocol.screen.CardHeaderNode
import dev.slne.surf.roleplay.protocol.screen.CardActionNode
import dev.slne.surf.roleplay.protocol.screen.CardContentNode
import dev.slne.surf.roleplay.protocol.screen.CardFooterNode
import dev.slne.surf.roleplay.api.client.common.screen.EmptyElement
import dev.slne.surf.roleplay.api.client.common.screen.EmptyHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.EmptyMediaElement
import dev.slne.surf.roleplay.api.client.common.screen.EmptyContentElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemMediaElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemContentElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemActionsElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemFooterElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemGroupElement
import dev.slne.surf.roleplay.protocol.screen.EmptyNode
import dev.slne.surf.roleplay.protocol.screen.EmptyHeaderNode
import dev.slne.surf.roleplay.protocol.screen.EmptyMediaNode
import dev.slne.surf.roleplay.protocol.screen.EmptyContentNode
import dev.slne.surf.roleplay.protocol.screen.ItemNode
import dev.slne.surf.roleplay.protocol.screen.ItemMediaNode
import dev.slne.surf.roleplay.protocol.screen.ItemContentNode
import dev.slne.surf.roleplay.protocol.screen.ItemActionsNode
import dev.slne.surf.roleplay.protocol.screen.ItemHeaderNode
import dev.slne.surf.roleplay.protocol.screen.ItemFooterNode
import dev.slne.surf.roleplay.protocol.screen.ItemGroupNode
import dev.slne.surf.roleplay.api.client.common.screen.PopoverElement
import dev.slne.surf.roleplay.api.client.common.screen.PopoverContentElement
import dev.slne.surf.roleplay.api.client.common.screen.PopoverHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.HoverCardElement
import dev.slne.surf.roleplay.api.client.common.screen.HoverCardContentElement
import dev.slne.surf.roleplay.api.client.common.screen.TooltipElement
import dev.slne.surf.roleplay.protocol.screen.PopoverNode
import dev.slne.surf.roleplay.protocol.screen.PopoverContentNode
import dev.slne.surf.roleplay.protocol.screen.PopoverHeaderNode
import dev.slne.surf.roleplay.protocol.screen.HoverCardNode
import dev.slne.surf.roleplay.protocol.screen.HoverCardContentNode
import dev.slne.surf.roleplay.protocol.screen.TooltipNode
import dev.slne.surf.roleplay.protocol.screen.SetOpen
import dev.slne.surf.roleplay.api.client.common.screen.DropdownMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuContentElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuRadioGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuSubElement
import dev.slne.surf.roleplay.api.client.common.screen.ContextMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.MenubarElement
import dev.slne.surf.roleplay.api.client.common.screen.MenubarMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuItemElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuCheckboxItemElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuRadioItemElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuLabelElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuSeparatorElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuSubTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.MenubarTriggerElement
import dev.slne.surf.roleplay.protocol.screen.DropdownMenuNode
import dev.slne.surf.roleplay.protocol.screen.MenuContentNode
import dev.slne.surf.roleplay.protocol.screen.MenuRadioGroupNode
import dev.slne.surf.roleplay.protocol.screen.MenuGroupNode
import dev.slne.surf.roleplay.protocol.screen.MenuSubNode
import dev.slne.surf.roleplay.protocol.screen.ContextMenuNode
import dev.slne.surf.roleplay.protocol.screen.MenubarNode
import dev.slne.surf.roleplay.protocol.screen.MenubarMenuNode
import dev.slne.surf.roleplay.protocol.screen.MenuItemNode
import dev.slne.surf.roleplay.protocol.screen.MenuCheckboxItemNode
import dev.slne.surf.roleplay.protocol.screen.MenuRadioItemNode
import dev.slne.surf.roleplay.protocol.screen.MenuLabelNode
import dev.slne.surf.roleplay.protocol.screen.MenuSeparatorNode
import dev.slne.surf.roleplay.protocol.screen.MenuSubTriggerNode
import dev.slne.surf.roleplay.protocol.screen.MenubarTriggerNode
import dev.slne.surf.roleplay.api.client.common.screen.CommandElement
import dev.slne.surf.roleplay.api.client.common.screen.CommandListElement
import dev.slne.surf.roleplay.api.client.common.screen.CommandGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.CommandInputElement
import dev.slne.surf.roleplay.api.client.common.screen.CommandEmptyElement
import dev.slne.surf.roleplay.api.client.common.screen.CommandItemElement
import dev.slne.surf.roleplay.api.client.common.screen.CommandSeparatorElement
import dev.slne.surf.roleplay.protocol.screen.CommandNode
import dev.slne.surf.roleplay.protocol.screen.CommandListNode
import dev.slne.surf.roleplay.protocol.screen.CommandGroupNode
import dev.slne.surf.roleplay.protocol.screen.CommandInputNode
import dev.slne.surf.roleplay.protocol.screen.CommandEmptyNode
import dev.slne.surf.roleplay.protocol.screen.CommandItemNode
import dev.slne.surf.roleplay.protocol.screen.CommandSeparatorNode
import dev.slne.surf.roleplay.api.client.common.screen.DialogElement
import dev.slne.surf.roleplay.api.client.common.screen.DialogContentElement
import dev.slne.surf.roleplay.api.client.common.screen.DialogHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.DialogFooterElement
import dev.slne.surf.roleplay.api.client.common.screen.DialogCloseElement
import dev.slne.surf.roleplay.api.client.common.screen.AlertDialogElement
import dev.slne.surf.roleplay.api.client.common.screen.AlertDialogContentElement
import dev.slne.surf.roleplay.api.client.common.screen.SheetElement
import dev.slne.surf.roleplay.api.client.common.screen.SheetContentElement
import dev.slne.surf.roleplay.api.client.common.screen.SheetHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.SheetFooterElement
import dev.slne.surf.roleplay.api.client.common.screen.AlertDialogMediaElement
import dev.slne.surf.roleplay.protocol.screen.DialogNode
import dev.slne.surf.roleplay.protocol.screen.DialogContentNode
import dev.slne.surf.roleplay.protocol.screen.DialogHeaderNode
import dev.slne.surf.roleplay.protocol.screen.DialogFooterNode
import dev.slne.surf.roleplay.protocol.screen.DialogCloseNode
import dev.slne.surf.roleplay.protocol.screen.AlertDialogNode
import dev.slne.surf.roleplay.protocol.screen.AlertDialogContentNode
import dev.slne.surf.roleplay.protocol.screen.SheetNode
import dev.slne.surf.roleplay.protocol.screen.SheetContentNode
import dev.slne.surf.roleplay.protocol.screen.SheetHeaderNode
import dev.slne.surf.roleplay.protocol.screen.SheetFooterNode
import dev.slne.surf.roleplay.protocol.screen.AlertDialogMediaNode
import dev.slne.surf.roleplay.api.client.common.screen.OverlayContainerElement
import dev.slne.surf.roleplay.protocol.screen.OverlayContainerNode
import dev.slne.surf.roleplay.api.client.common.screen.CollapsibleElement
import dev.slne.surf.roleplay.api.client.common.screen.CollapsibleTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.CollapsibleContentElement
import dev.slne.surf.roleplay.api.client.common.screen.AccordionElement
import dev.slne.surf.roleplay.api.client.common.screen.AccordionItemElement
import dev.slne.surf.roleplay.api.client.common.screen.AccordionTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.AccordionContentElement
import dev.slne.surf.roleplay.protocol.screen.CollapsibleNode
import dev.slne.surf.roleplay.protocol.screen.CollapsibleTriggerNode
import dev.slne.surf.roleplay.protocol.screen.CollapsibleContentNode
import dev.slne.surf.roleplay.protocol.screen.AccordionNode
import dev.slne.surf.roleplay.protocol.screen.AccordionItemNode
import dev.slne.surf.roleplay.protocol.screen.AccordionTriggerNode
import dev.slne.surf.roleplay.protocol.screen.AccordionContentNode
import dev.slne.surf.roleplay.api.client.common.screen.TabsElement
import dev.slne.surf.roleplay.api.client.common.screen.TabsListElement
import dev.slne.surf.roleplay.api.client.common.screen.TabsTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.TabsContentElement
import dev.slne.surf.roleplay.protocol.screen.TabsNode
import dev.slne.surf.roleplay.protocol.screen.TabsListNode
import dev.slne.surf.roleplay.protocol.screen.TabsTriggerNode
import dev.slne.surf.roleplay.protocol.screen.TabsContentNode
import dev.slne.surf.roleplay.api.client.common.screen.BreadcrumbElement
import dev.slne.surf.roleplay.api.client.common.screen.BreadcrumbListElement
import dev.slne.surf.roleplay.api.client.common.screen.BreadcrumbItemElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationContentElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationItemElement
import dev.slne.surf.roleplay.api.client.common.screen.BreadcrumbLinkElement
import dev.slne.surf.roleplay.api.client.common.screen.BreadcrumbPageElement
import dev.slne.surf.roleplay.api.client.common.screen.BreadcrumbSeparatorElement
import dev.slne.surf.roleplay.api.client.common.screen.BreadcrumbEllipsisElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationLinkElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationPreviousElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationNextElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationEllipsisElement
import dev.slne.surf.roleplay.protocol.screen.BreadcrumbNode
import dev.slne.surf.roleplay.protocol.screen.BreadcrumbListNode
import dev.slne.surf.roleplay.protocol.screen.BreadcrumbItemNode
import dev.slne.surf.roleplay.protocol.screen.PaginationNode
import dev.slne.surf.roleplay.protocol.screen.PaginationContentNode
import dev.slne.surf.roleplay.protocol.screen.PaginationItemNode
import dev.slne.surf.roleplay.protocol.screen.BreadcrumbLinkNode
import dev.slne.surf.roleplay.protocol.screen.BreadcrumbPageNode
import dev.slne.surf.roleplay.protocol.screen.BreadcrumbSeparatorNode
import dev.slne.surf.roleplay.protocol.screen.BreadcrumbEllipsisNode
import dev.slne.surf.roleplay.protocol.screen.PaginationLinkNode
import dev.slne.surf.roleplay.protocol.screen.PaginationPreviousNode
import dev.slne.surf.roleplay.protocol.screen.PaginationNextNode
import dev.slne.surf.roleplay.protocol.screen.PaginationEllipsisNode
import dev.slne.surf.roleplay.api.client.common.screen.ScrollAreaElement
import dev.slne.surf.roleplay.api.client.common.screen.DirectionElement
import dev.slne.surf.roleplay.api.client.common.screen.TableElement
import dev.slne.surf.roleplay.api.client.common.screen.TableCaptionElement
import dev.slne.surf.roleplay.api.client.common.screen.TableSectionElement
import dev.slne.surf.roleplay.api.client.common.screen.TableRowElement
import dev.slne.surf.roleplay.api.client.common.screen.TableCellElement
import dev.slne.surf.roleplay.api.client.common.screen.DataTableElement
import dev.slne.surf.roleplay.api.client.common.screen.DataTableColumnElement
import dev.slne.surf.roleplay.api.client.common.screen.DataTableRowElement
import dev.slne.surf.roleplay.api.client.common.screen.DataTableCellElement
import dev.slne.surf.roleplay.api.client.common.screen.ChartElement
import dev.slne.surf.roleplay.api.client.common.screen.ChatViewElement
import dev.slne.surf.roleplay.api.client.common.screen.ChatMessageElement
import dev.slne.surf.roleplay.protocol.screen.ScrollAreaNode
import dev.slne.surf.roleplay.protocol.screen.DirectionNode
import dev.slne.surf.roleplay.protocol.screen.TableNode
import dev.slne.surf.roleplay.protocol.screen.TableCaptionNode
import dev.slne.surf.roleplay.protocol.screen.TableSectionNode
import dev.slne.surf.roleplay.protocol.screen.TableRowNode
import dev.slne.surf.roleplay.protocol.screen.TableCellNode
import dev.slne.surf.roleplay.protocol.screen.DataTableNode
import dev.slne.surf.roleplay.protocol.screen.DataTableColumnNode
import dev.slne.surf.roleplay.protocol.screen.DataTableRowNode
import dev.slne.surf.roleplay.protocol.screen.DataTableCellNode
import dev.slne.surf.roleplay.protocol.screen.ChartNode
import dev.slne.surf.roleplay.protocol.screen.ChatViewNode
import dev.slne.surf.roleplay.protocol.screen.ChatMessageNode
import dev.slne.surf.roleplay.protocol.screen.ChartSeries as NodeChartSeries
import dev.slne.surf.roleplay.api.client.common.screen.ResizablePanelGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.ResizablePanelElement
import dev.slne.surf.roleplay.api.client.common.screen.ResizableHandleElement
import dev.slne.surf.roleplay.protocol.screen.ResizablePanelGroupNode
import dev.slne.surf.roleplay.protocol.screen.ResizablePanelNode
import dev.slne.surf.roleplay.protocol.screen.ResizableHandleNode
import dev.slne.surf.roleplay.api.client.common.screen.CarouselElement
import dev.slne.surf.roleplay.api.client.common.screen.CarouselContentElement
import dev.slne.surf.roleplay.api.client.common.screen.CarouselItemElement
import dev.slne.surf.roleplay.api.client.common.screen.CarouselPreviousElement
import dev.slne.surf.roleplay.api.client.common.screen.CarouselNextElement
import dev.slne.surf.roleplay.protocol.screen.CarouselNode
import dev.slne.surf.roleplay.protocol.screen.CarouselContentNode
import dev.slne.surf.roleplay.protocol.screen.CarouselItemNode
import dev.slne.surf.roleplay.protocol.screen.CarouselPreviousNode
import dev.slne.surf.roleplay.protocol.screen.CarouselNextNode
import dev.slne.surf.roleplay.api.client.common.screen.NavigationMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.NavigationMenuListElement
import dev.slne.surf.roleplay.api.client.common.screen.NavigationMenuItemElement
import dev.slne.surf.roleplay.api.client.common.screen.NavigationMenuTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.NavigationMenuContentElement
import dev.slne.surf.roleplay.api.client.common.screen.NavigationMenuLinkElement
import dev.slne.surf.roleplay.protocol.screen.NavigationMenuNode
import dev.slne.surf.roleplay.protocol.screen.NavigationMenuListNode
import dev.slne.surf.roleplay.protocol.screen.NavigationMenuItemNode
import dev.slne.surf.roleplay.protocol.screen.NavigationMenuTriggerNode
import dev.slne.surf.roleplay.protocol.screen.NavigationMenuContentNode
import dev.slne.surf.roleplay.protocol.screen.NavigationMenuLinkNode
import dev.slne.surf.roleplay.api.client.common.screen.SidebarProviderElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarInsetElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarFooterElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarContentElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarGroupContentElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuItemElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuSubElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuSubItemElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarGroupLabelElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarGroupActionElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuActionElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuBadgeElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuSkeletonElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuSubButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarRailElement
import dev.slne.surf.roleplay.protocol.screen.SidebarProviderNode
import dev.slne.surf.roleplay.protocol.screen.SidebarNode
import dev.slne.surf.roleplay.protocol.screen.SidebarInsetNode
import dev.slne.surf.roleplay.protocol.screen.SidebarHeaderNode
import dev.slne.surf.roleplay.protocol.screen.SidebarFooterNode
import dev.slne.surf.roleplay.protocol.screen.SidebarContentNode
import dev.slne.surf.roleplay.protocol.screen.SidebarGroupNode
import dev.slne.surf.roleplay.protocol.screen.SidebarGroupContentNode
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuNode
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuItemNode
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuSubNode
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuSubItemNode
import dev.slne.surf.roleplay.protocol.screen.SidebarGroupLabelNode
import dev.slne.surf.roleplay.protocol.screen.SidebarGroupActionNode
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuButtonNode
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuActionNode
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuBadgeNode
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuSkeletonNode
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuSubButtonNode
import dev.slne.surf.roleplay.protocol.screen.SidebarTriggerNode
import dev.slne.surf.roleplay.protocol.screen.SidebarRailNode
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

/**
 * Maps the public screen model to the protocol's screen nodes and patch operations.
 */
object ScreenMapper {

    /**
     * Serializes a text to the component JSON sent to the mod.
     *
     * @param component the text
     * @return the component JSON
     */
    fun text(component: Component): String = GsonComponentSerializer.gson().serialize(component)

    /**
     * Serializes a text that may be left out: an empty text becomes an empty string.
     *
     * @param component the text
     * @return the component JSON, or an empty string for an empty text
     */
    fun optionalText(component: Component): String =
        if (component is TextComponent && component.content().isEmpty() && component.children().isEmpty()) "" else text(component)

    /**
     * Maps an element, with its children, to a protocol node.
     *
     * @param element the element
     * @return the node
     */
    fun toNode(element: ScreenElement): ScreenNode {
        val width = size(element.width)
        val height = size(element.height)
        return when (element) {
            is RowElement -> RowNode(
                element.id, width, height, element.children.map(::toNode), element.gap, insets(element.padding),
                align(element.mainAlign), align(element.crossAlign),
            )

            is ColumnElement -> ColumnNode(
                element.id, width, height, element.children.map(::toNode), element.gap, insets(element.padding),
                align(element.mainAlign), align(element.crossAlign),
            )

            is ScrollListElement -> ScrollListNode(element.id, width, height, element.children.map(::toNode), element.gap)
            is LabelElement -> LabelNode(element.id, width, height, text(element.text), element.icon, element.forId)
            is ButtonElement -> ButtonNode(element.id, width, height, text(element.text), element.enabled, element.submitsInput, element.icon, enumOf(element.variant), enumOf(element.size))
            is TextInputElement -> TextInputNode(
                element.id, width, height, element.value, text(element.placeholder), element.maxLength, element.required, element.enabled, element.icon,
                element.onChange != null, enumOf(element.type),
            )

            is NumberInputElement -> NumberInputNode(
                element.id, width, height, element.value, element.min, element.max, element.required, element.enabled,
                element.onChange != null,
            )

            is CheckboxElement -> CheckboxNode(element.id, width, height, text(element.label), element.checked, element.enabled, element.onChange != null)
            is SelectElement -> SelectNode(
                element.id, width, height, groups(element.groups), element.selected, text(element.placeholder), enumOf(element.size), element.required,
                element.enabled, element.onChange != null,
            )

            is ComboboxElement -> ComboboxNode(
                element.id, width, height, groups(element.groups), element.selected, element.multiple, text(element.placeholder), text(element.emptyText),
                element.showClear, element.required, element.enabled, element.onChange != null, element.onSearch != null,
            )

            is ImageElement -> ImageNode(element.id, width, height, element.texture.asString())
            is ProgressElement -> ProgressNode(element.id, width, height, element.progress, element.label?.let(::text))
            is IconElement -> IconNode(element.id, width, height, element.icon, element.size, tint(element.tint))
            is ButtonGroupElement -> ButtonGroupNode(element.id, width, height, element.children.map(::toNode), enumOf(element.orientation))
            is ButtonGroupTextElement -> ButtonGroupTextNode(element.id, width, height, text(element.text), element.icon)
            is ButtonGroupSeparatorElement -> ButtonGroupSeparatorNode(element.id, width, height)
            is ToggleElement -> ToggleNode(
                element.id, width, height, text(element.text), element.icon, element.pressed, enumOf(element.variant), enumOf(element.size), element.enabled,
            )

            is ToggleGroupElement -> ToggleGroupNode(
                element.id, width, height,
                element.items.map { ToggleGroupItem(it.value, text(it.text), it.icon, it.enabled) },
                element.multiple, element.selected, enumOf(element.variant), enumOf(element.size), element.spacing,
                enumOf(element.orientation), element.enabled, element.required, element.onChange != null,
            )

            is TextareaElement -> TextareaNode(
                element.id, width, height, element.value, text(element.placeholder), element.rows, element.maxLength, element.required,
                element.enabled, element.onChange != null,
            )

            is FormElement -> FormNode(element.id, width, height, element.children.map(::toNode), element.submitId)
            is FieldSetElement -> FieldSetNode(element.id, width, height, element.children.map(::toNode))
            is FieldGroupElement -> FieldGroupNode(element.id, width, height, element.children.map(::toNode))
            is FieldElement -> FieldNode(element.id, width, height, element.children.map(::toNode), enumOf(element.orientation))
            is FieldContentElement -> FieldContentNode(element.id, width, height, element.children.map(::toNode))
            is FieldTextElement -> FieldTextNode(element.id, width, height, enumOf(element.kind), text(element.text), element.forId)
            is FieldSeparatorElement -> FieldSeparatorNode(element.id, width, height, element.text?.let(::text))
            is CalendarElement -> CalendarNode(
                element.id, width, height, enumOf(element.mode), CalendarValues.format(enumOf(element.mode), element.selected), element.month?.toString(),
                element.min?.toString(), element.max?.toString(), element.disabled.sorted().map { it.toString() }, element.showOutsideDays,
                enumOf(element.captionLayout), element.required, element.enabled, element.onChange != null,
            )

            is SwitchElement -> SwitchNode(element.id, width, height, element.checked, enumOf(element.size), element.enabled, element.onChange != null)
            is RadioGroupElement -> RadioGroupNode(
                element.id, width, height, element.options.map { RadioOption(it.value, text(it.label), it.enabled) }, element.selected,
                enumOf(element.orientation), element.required, element.enabled, element.onChange != null,
            )

            is SliderElement -> SliderNode(
                element.id, width, height, element.values, element.min, element.max, element.step, enumOf(element.orientation), element.enabled,
                element.onChange != null,
            )

            is InputGroupElement -> InputGroupNode(element.id, width, height, element.children.map(::toNode))
            is InputGroupAddonElement -> InputGroupAddonNode(element.id, width, height, element.children.map(::toNode), enumOf(element.align))
            is InputGroupTextElement -> InputGroupTextNode(element.id, width, height, text(element.text), element.icon)
            is InputOtpElement -> InputOtpNode(
                element.id, width, height, element.value, element.length, element.groups, enumOf(element.pattern), element.required,
                element.enabled, element.onChange != null,
            )
            is TextElement -> TextNode(element.id, width, height, enumOf(element.kind), text(element.text), element.maxLines, align(element.align))
            is TextListElement -> TextListNode(element.id, width, height, element.items.map(::text), element.ordered)
            is SeparatorElement -> SeparatorNode(element.id, width, height, enumOf(element.orientation))
            is KbdElement -> KbdNode(element.id, width, height, text(element.text), element.icon)
            is KbdGroupElement -> KbdGroupNode(element.id, width, height, element.children.map(::toNode))
            is BadgeElement -> BadgeNode(element.id, width, height, text(element.text), element.icon, enumOf(element.variant))
            is SkeletonElement -> SkeletonNode(element.id, width, height, element.round)
            is SpinnerElement -> SpinnerNode(element.id, width, height, element.size, tint(element.tint))
            is AspectRatioElement -> AspectRatioNode(element.id, width, height, element.children.map(::toNode), element.ratio)
            is AvatarElement -> AvatarNode(
                element.id, width, height, (element.source as? AvatarSource.Player)?.playerId?.toString(),
                (element.source as? AvatarSource.Texture)?.texture?.asString(), text(element.fallback), enumOf(element.size),
                element.badge, element.badgeIcon,
            )
            is AvatarGroupElement -> AvatarGroupNode(element.id, width, height, element.children.map(::toNode))
            is AvatarGroupCountElement -> AvatarGroupCountNode(element.id, width, height, text(element.text), element.icon)
            is AlertElement -> AlertNode(element.id, width, height, element.children.map(::toNode), enumOf(element.variant), element.icon)
            is CardElement -> CardNode(element.id, width, height, element.children.map(::toNode))
            is CardHeaderElement -> CardHeaderNode(element.id, width, height, element.children.map(::toNode))
            is CardActionElement -> CardActionNode(element.id, width, height, element.children.map(::toNode))
            is CardContentElement -> CardContentNode(element.id, width, height, element.children.map(::toNode))
            is CardFooterElement -> CardFooterNode(element.id, width, height, element.children.map(::toNode))
            is EmptyElement -> EmptyNode(element.id, width, height, element.children.map(::toNode), element.outline)
            is EmptyHeaderElement -> EmptyHeaderNode(element.id, width, height, element.children.map(::toNode))
            is EmptyMediaElement -> EmptyMediaNode(element.id, width, height, element.children.map(::toNode), enumOf(element.variant), element.icon)
            is EmptyContentElement -> EmptyContentNode(element.id, width, height, element.children.map(::toNode))
            is ItemElement -> ItemNode(element.id, width, height, element.children.map(::toNode), enumOf(element.variant), enumOf(element.size), element.onClick != null)
            is ItemMediaElement -> ItemMediaNode(element.id, width, height, element.children.map(::toNode), enumOf(element.variant), element.icon)
            is ItemContentElement -> ItemContentNode(element.id, width, height, element.children.map(::toNode))
            is ItemActionsElement -> ItemActionsNode(element.id, width, height, element.children.map(::toNode))
            is ItemHeaderElement -> ItemHeaderNode(element.id, width, height, element.children.map(::toNode))
            is ItemFooterElement -> ItemFooterNode(element.id, width, height, element.children.map(::toNode))
            is ItemGroupElement -> ItemGroupNode(element.id, width, height, element.children.map(::toNode))
            is PopoverElement -> PopoverNode(element.id, width, height, element.children.map(::toNode), element.open, enumOf(element.side), align(element.align), element.onChange != null)
            is PopoverContentElement -> PopoverContentNode(element.id, width, height, element.children.map(::toNode))
            is PopoverHeaderElement -> PopoverHeaderNode(element.id, width, height, element.children.map(::toNode))
            is HoverCardElement -> HoverCardNode(
                element.id, width, height, element.children.map(::toNode), element.open, enumOf(element.side), align(element.align),
                element.openDelay, element.closeDelay, element.onChange != null,
            )
            is HoverCardContentElement -> HoverCardContentNode(element.id, width, height, element.children.map(::toNode))
            is TooltipElement -> TooltipNode(element.id, width, height, element.children.map(::toNode), text(element.text), enumOf(element.side))
            is DropdownMenuElement -> DropdownMenuNode(element.id, width, height, element.children.map(::toNode), element.open, enumOf(element.side), align(element.align), element.onChange != null)
            is MenuContentElement -> MenuContentNode(element.id, width, height, element.children.map(::toNode))
            is MenuItemElement -> MenuItemNode(
                element.id, width, height, text(element.text), element.icon, element.shortcut?.let(::text), element.destructive, element.inset, element.enabled,
            )
            is MenuCheckboxItemElement -> MenuCheckboxItemNode(element.id, width, height, text(element.text), element.checked, element.enabled)
            is MenuRadioGroupElement -> MenuRadioGroupNode(element.id, width, height, element.children.map(::toNode), element.value)
            is MenuRadioItemElement -> MenuRadioItemNode(element.id, width, height, text(element.text), element.value, element.enabled)
            is MenuLabelElement -> MenuLabelNode(element.id, width, height, text(element.text), element.inset)
            is MenuSeparatorElement -> MenuSeparatorNode(element.id, width, height)
            is MenuGroupElement -> MenuGroupNode(element.id, width, height, element.children.map(::toNode))
            is MenuSubElement -> MenuSubNode(element.id, width, height, element.children.map(::toNode), element.open, element.onChange != null)
            is MenuSubTriggerElement -> MenuSubTriggerNode(element.id, width, height, text(element.text), element.icon, element.inset, element.enabled)
            is ContextMenuElement -> ContextMenuNode(element.id, width, height, element.children.map(::toNode), element.open, element.onChange != null)
            is MenubarElement -> MenubarNode(element.id, width, height, element.children.map(::toNode))
            is MenubarMenuElement -> MenubarMenuNode(element.id, width, height, element.children.map(::toNode), element.open, element.onChange != null)
            is MenubarTriggerElement -> MenubarTriggerNode(element.id, width, height, text(element.text))
            is CommandElement -> CommandNode(element.id, width, height, element.children.map(::toNode), element.onSearch != null)
            is CommandInputElement -> CommandInputNode(element.id, width, height, text(element.placeholder))
            is CommandListElement -> CommandListNode(element.id, width, height, element.children.map(::toNode))
            is CommandEmptyElement -> CommandEmptyNode(element.id, width, height, text(element.text))
            is CommandGroupElement -> CommandGroupNode(element.id, width, height, element.children.map(::toNode), element.heading?.let(::text))
            is CommandItemElement -> CommandItemNode(
                element.id, width, height, text(element.text), element.icon, element.shortcut?.let(::text), element.keywords, element.enabled,
            )
            is CommandSeparatorElement -> CommandSeparatorNode(element.id, width, height)
            is DialogElement -> DialogNode(element.id, width, height, element.children.map(::toNode), element.open, element.onChange != null)
            is DialogContentElement -> DialogContentNode(element.id, width, height, element.children.map(::toNode), element.showCloseButton)
            is DialogHeaderElement -> DialogHeaderNode(element.id, width, height, element.children.map(::toNode))
            is DialogFooterElement -> DialogFooterNode(element.id, width, height, element.children.map(::toNode))
            is DialogCloseElement -> DialogCloseNode(element.id, width, height, element.children.map(::toNode))
            is AlertDialogElement -> AlertDialogNode(element.id, width, height, element.children.map(::toNode), element.open, element.onChange != null)
            is AlertDialogContentElement -> AlertDialogContentNode(element.id, width, height, element.children.map(::toNode), enumOf(element.size))
            is AlertDialogMediaElement -> AlertDialogMediaNode(element.id, width, height, element.icon)
            is SheetElement -> SheetNode(element.id, width, height, element.children.map(::toNode), element.open, element.onChange != null)
            is SheetContentElement -> SheetContentNode(element.id, width, height, element.children.map(::toNode), enumOf(element.side), element.showCloseButton)
            is SheetHeaderElement -> SheetHeaderNode(element.id, width, height, element.children.map(::toNode))
            is SheetFooterElement -> SheetFooterNode(element.id, width, height, element.children.map(::toNode))
            is OverlayContainerElement -> OverlayContainerNode(element.id, width, height, element.children.map(::toNode))
            is CollapsibleElement -> CollapsibleNode(element.id, width, height, element.children.map(::toNode), element.open, element.onChange != null)
            is CollapsibleTriggerElement -> CollapsibleTriggerNode(element.id, width, height, element.children.map(::toNode))
            is CollapsibleContentElement -> CollapsibleContentNode(element.id, width, height, element.children.map(::toNode))
            is AccordionElement -> AccordionNode(element.id, width, height, element.children.map(::toNode), enumOf(element.type), element.collapsible, element.value, element.onChange != null)
            is AccordionItemElement -> AccordionItemNode(element.id, width, height, element.children.map(::toNode), element.value, element.enabled)
            is AccordionTriggerElement -> AccordionTriggerNode(element.id, width, height, text(element.text))
            is AccordionContentElement -> AccordionContentNode(element.id, width, height, element.children.map(::toNode))
            is TabsElement -> TabsNode(element.id, width, height, element.children.map(::toNode), element.value, enumOf(element.orientation), element.onChange != null)
            is TabsListElement -> TabsListNode(element.id, width, height, element.children.map(::toNode), enumOf(element.variant))
            is TabsTriggerElement -> TabsTriggerNode(element.id, width, height, element.value, text(element.text), element.icon, element.enabled)
            is TabsContentElement -> TabsContentNode(element.id, width, height, element.children.map(::toNode), element.value)
            is BreadcrumbElement -> BreadcrumbNode(element.id, width, height, element.children.map(::toNode))
            is BreadcrumbListElement -> BreadcrumbListNode(element.id, width, height, element.children.map(::toNode))
            is BreadcrumbItemElement -> BreadcrumbItemNode(element.id, width, height, element.children.map(::toNode))
            is PaginationElement -> PaginationNode(element.id, width, height, element.children.map(::toNode))
            is PaginationContentElement -> PaginationContentNode(element.id, width, height, element.children.map(::toNode))
            is PaginationItemElement -> PaginationItemNode(element.id, width, height, element.children.map(::toNode))
            is BreadcrumbLinkElement -> BreadcrumbLinkNode(element.id, width, height, text(element.text), element.enabled)
            is BreadcrumbPageElement -> BreadcrumbPageNode(element.id, width, height, text(element.text))
            is BreadcrumbSeparatorElement -> BreadcrumbSeparatorNode(element.id, width, height, element.icon)
            is BreadcrumbEllipsisElement -> BreadcrumbEllipsisNode(element.id, width, height)
            is PaginationLinkElement -> PaginationLinkNode(element.id, width, height, text(element.text), element.active, element.enabled)
            is PaginationPreviousElement -> PaginationPreviousNode(element.id, width, height, text(element.text), element.enabled)
            is PaginationNextElement -> PaginationNextNode(element.id, width, height, text(element.text), element.enabled)
            is PaginationEllipsisElement -> PaginationEllipsisNode(element.id, width, height)
            is ScrollAreaElement -> ScrollAreaNode(element.id, width, height, element.children.map(::toNode), enumOf(element.orientation))
            is DirectionElement -> DirectionNode(element.id, width, height, element.children.map(::toNode), enumOf(element.direction))
            is TableElement -> TableNode(element.id, width, height, element.children.map(::toNode))
            is TableCaptionElement -> TableCaptionNode(element.id, width, height, text(element.text))
            is TableSectionElement -> TableSectionNode(element.id, width, height, element.children.map(::toNode), enumOf(element.section))
            is TableRowElement -> TableRowNode(element.id, width, height, element.children.map(::toNode), element.selected)
            is TableCellElement -> TableCellNode(element.id, width, height, element.children.map(::toNode), element.head, align(element.align))
            is DataTableElement -> DataTableNode(
                element.id, width, height, element.children.map(::toNode), element.pageSize, element.selectable, element.filterColumn,
                text(element.filterPlaceholder), element.value.toJson(), element.onChange != null,
            )
            is DataTableColumnElement -> DataTableColumnNode(element.id, width, height, element.key, text(element.header), element.sortable, align(element.align))
            is DataTableRowElement -> DataTableRowNode(element.id, width, height, element.children.map(::toNode), element.selectable)
            is DataTableCellElement -> DataTableCellNode(element.id, width, height, element.children.map(::toNode), element.sortKey)
            is ChatViewElement -> ChatViewNode(element.id, width, height, element.children.map(::toNode))
            is ChatMessageElement -> ChatMessageNode(
                element.id, width, height, element.children.map(::toNode), element.own, element.playerId?.toString(), element.texture, element.fallback,
                optionalText(element.name), optionalText(element.time),
            )
            is ChartElement -> ChartNode(
                element.id, width, height, enumOf(element.kind), element.categories.map(::text),
                element.series.map { NodeChartSeries(it.key, text(it.label), it.color.coerceIn(1, 5), it.values.map { value -> if (value.isFinite()) value else 0.0 }) },
                element.categoryColors.map { it.coerceIn(1, 5) }, element.stacked, element.horizontal, enumOf(element.curve), element.dots, element.grid,
                element.categoryAxis, element.valueAxis, element.legend, element.tooltip, enumOf(element.indicator), element.donut, element.labels,
            )
            is ResizablePanelGroupElement -> ResizablePanelGroupNode(element.id, width, height, element.children.map(::toNode), enumOf(element.orientation), element.onChange != null)
            is ResizablePanelElement -> ResizablePanelNode(element.id, width, height, element.children.map(::toNode), element.defaultSize, element.minSize, element.maxSize)
            is ResizableHandleElement -> ResizableHandleNode(element.id, width, height, element.withHandle)
            is CarouselElement -> CarouselNode(element.id, width, height, element.children.map(::toNode), enumOf(element.orientation), element.loop, element.index, element.onChange != null)
            is CarouselContentElement -> CarouselContentNode(element.id, width, height, element.children.map(::toNode))
            is CarouselItemElement -> CarouselItemNode(element.id, width, height, element.children.map(::toNode), element.basis)
            is CarouselPreviousElement -> CarouselPreviousNode(element.id, width, height, element.enabled)
            is CarouselNextElement -> CarouselNextNode(element.id, width, height, element.enabled)
            is NavigationMenuElement -> NavigationMenuNode(element.id, width, height, element.children.map(::toNode))
            is NavigationMenuListElement -> NavigationMenuListNode(element.id, width, height, element.children.map(::toNode))
            is NavigationMenuItemElement -> NavigationMenuItemNode(element.id, width, height, element.children.map(::toNode), element.open, element.onChange != null)
            is NavigationMenuTriggerElement -> NavigationMenuTriggerNode(element.id, width, height, text(element.text), element.enabled)
            is NavigationMenuContentElement -> NavigationMenuContentNode(element.id, width, height, element.children.map(::toNode))
            is NavigationMenuLinkElement -> NavigationMenuLinkNode(element.id, width, height, element.children.map(::toNode), element.active, element.enabled)
            is SidebarProviderElement -> SidebarProviderNode(element.id, width, height, element.children.map(::toNode), element.open, element.onChange != null)
            is SidebarElement -> SidebarNode(element.id, width, height, element.children.map(::toNode), enumOf(element.side), enumOf(element.variant), enumOf(element.collapsible))
            is SidebarInsetElement -> SidebarInsetNode(element.id, width, height, element.children.map(::toNode))
            is SidebarHeaderElement -> SidebarHeaderNode(element.id, width, height, element.children.map(::toNode))
            is SidebarFooterElement -> SidebarFooterNode(element.id, width, height, element.children.map(::toNode))
            is SidebarContentElement -> SidebarContentNode(element.id, width, height, element.children.map(::toNode))
            is SidebarGroupElement -> SidebarGroupNode(element.id, width, height, element.children.map(::toNode))
            is SidebarGroupLabelElement -> SidebarGroupLabelNode(element.id, width, height, text(element.text))
            is SidebarGroupActionElement -> SidebarGroupActionNode(element.id, width, height, element.icon, element.enabled)
            is SidebarGroupContentElement -> SidebarGroupContentNode(element.id, width, height, element.children.map(::toNode))
            is SidebarMenuElement -> SidebarMenuNode(element.id, width, height, element.children.map(::toNode))
            is SidebarMenuItemElement -> SidebarMenuItemNode(element.id, width, height, element.children.map(::toNode))
            is SidebarMenuButtonElement -> SidebarMenuButtonNode(
                element.id, width, height, text(element.text), element.icon, enumOf(element.size), enumOf(element.variant), element.active,
                element.tooltip?.let { text(it) } ?: "", element.enabled,
            )
            is SidebarMenuActionElement -> SidebarMenuActionNode(element.id, width, height, element.icon, element.showOnHover, element.enabled)
            is SidebarMenuBadgeElement -> SidebarMenuBadgeNode(element.id, width, height, text(element.text))
            is SidebarMenuSkeletonElement -> SidebarMenuSkeletonNode(element.id, width, height, element.showIcon)
            is SidebarMenuSubElement -> SidebarMenuSubNode(element.id, width, height, element.children.map(::toNode))
            is SidebarMenuSubItemElement -> SidebarMenuSubItemNode(element.id, width, height, element.children.map(::toNode))
            is SidebarMenuSubButtonElement -> SidebarMenuSubButtonNode(element.id, width, height, text(element.text), element.icon, enumOf(element.size), element.active, element.enabled)
            is SidebarTriggerElement -> SidebarTriggerNode(element.id, width, height, element.enabled)
            is SidebarRailElement -> SidebarRailNode(element.id, width, height)
        }
    }

    /**
     * Maps a change to a patch operation.
     *
     * @param change the change
     * @return the operation
     */
    fun toOperation(change: ScreenChange): PatchOperation = when (change) {
        is ScreenChange.Replace -> ReplaceNode(change.targetId, toNode(change.element))
        is ScreenChange.Insert -> InsertNode(change.parentId, change.index, toNode(change.element))
        is ScreenChange.Remove -> RemoveNode(change.targetId)
        is ScreenChange.SetText -> SetText(change.targetId, text(change.text))
        is ScreenChange.SetValue -> SetValue(change.targetId, change.value)
        is ScreenChange.SetProgress -> SetProgress(change.targetId, change.progress)
        is ScreenChange.SetEnabled -> SetEnabled(change.targetId, change.enabled)
        is ScreenChange.SetOptions -> SetOptions(change.targetId, groups(change.groups))
        is ScreenChange.SetInvalid -> SetInvalid(change.targetId, change.invalid)
        is ScreenChange.SetOpen -> SetOpen(change.targetId, change.open)
    }

    /**
     * Maps option groups to their protocol form.
     *
     * @param groups the option groups
     * @return the protocol groups
     */
    fun groups(groups: List<SelectChoiceGroup>): List<SelectGroup> =
        groups.map { group -> SelectGroup(group.label?.let(::text), group.options.map { SelectOption(it.value, text(it.label), it.enabled) }) }

    /**
     * Maps an API enum constant to the protocol enum constant of the same name.
     *
     * @param T the protocol enum
     * @param value the API constant
     * @return the protocol constant
     */
    inline fun <reified T : Enum<T>> enumOf(value: Enum<*>): T = enumValueOf(value.name)

    /**
     * Maps an icon tint.
     *
     * @param tint the tint
     * @return the protocol icon colour
     */
    private fun tint(tint: IconTint): IconColor = when (tint) {
        IconTint.FOREGROUND -> IconColor.FOREGROUND
        IconTint.MUTED -> IconColor.MUTED
        IconTint.PRIMARY -> IconColor.PRIMARY
        IconTint.DESTRUCTIVE -> IconColor.DESTRUCTIVE
    }

    /**
     * Maps a theme variant.
     *
     * @param variant the variant
     * @return the protocol variant
     */
    fun variant(variant: ScreenVariant): ThemeVariant = when (variant) {
        ScreenVariant.DARK -> ThemeVariant.DARK
        ScreenVariant.LIGHT -> ThemeVariant.LIGHT
    }

    /**
     * Maps a presentation.
     *
     * @param presentation the presentation
     * @return the protocol presentation
     */
    fun presentation(presentation: ScreenPresentation): Presentation = when (presentation) {
        ScreenPresentation.SCREEN -> Presentation.SCREEN
        ScreenPresentation.DIALOG -> Presentation.DIALOG
        ScreenPresentation.SHEET -> Presentation.SHEET
    }

    /**
     * Maps a sheet side.
     *
     * @param side the side
     * @return the protocol sheet edge
     */
    fun sheetEdge(side: SheetSide): SheetEdge = when (side) {
        SheetSide.RIGHT -> SheetEdge.RIGHT
        SheetSide.LEFT -> SheetEdge.LEFT
        SheetSide.TOP -> SheetEdge.TOP
        SheetSide.BOTTOM -> SheetEdge.BOTTOM
    }

    /**
     * Maps an element size to a sizing.
     *
     * @param size the size
     * @return the sizing
     */
    private fun size(size: ElementSize): Sizing = when (size.mode) {
        ElementSizeMode.FIT -> Sizing.FIT
        ElementSizeMode.FIXED -> Sizing(SizeMode.FIXED, size.value)
        ElementSizeMode.GROW -> Sizing(SizeMode.GROW, size.value)
    }

    /**
     * Maps a spacing to insets.
     *
     * @param spacing the spacing
     * @return the insets
     */
    private fun insets(spacing: Spacing): Insets = Insets(spacing.top, spacing.right, spacing.bottom, spacing.left)

    /**
     * Maps an alignment.
     *
     * @param alignment the alignment
     * @return the protocol alignment
     */
    private fun align(alignment: Alignment): Align = when (alignment) {
        Alignment.START -> Align.START
        Alignment.CENTER -> Align.CENTER
        Alignment.END -> Align.END
        Alignment.STRETCH -> Align.STRETCH
    }
}
