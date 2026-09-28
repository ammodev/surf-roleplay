# Plan 0010: shadcn overlays and menus

- **Status:** In progress
- **Date:** 2026-09-28
- **Accepted proposal:** Port the overlay and menu components of the shadcn registry (alert-dialog, dialog, drawer, sheet, popover, hover-card, tooltip, dropdown-menu, context-menu, menubar, command, sonner) as native screen nodes opened on the client, plus HUD toasts with a hold-key cursor; pre-accepted under the standing autonomy for shadcn-faithful components
- **Decision records:** ADR-0051, ADR-0052, ADR-0054, ADR-0057, ADR-0058, ADR-0059

## Goal

`/rpscreen overlays` opens a screen on the dev stack that shows every overlay and menu component
of the shadcn registry, each working by mouse and keyboard:
- popover with header, title and description, and a date picker built from a popover and a
  calendar
- hover card, opened after hovering its trigger
- tooltip, on every side, shown while its trigger is hovered or focused
- dropdown menu with items (icon, shortcut, destructive, inset, disabled), checkbox items, a radio
  group, labels, separators, groups and a sub-menu
- context menu, opened by a right click on its area
- menubar with several menus that switch on hover while one is open
- command with an input, groups, items with shortcuts, separators and an empty text, filtered on
  the client, and a command dialog
- dialog with header, title, description, footer and close button
- alert dialog in the sizes default and sm, with media, action and cancel
- sheet from every side, and drawer from the bottom and the top
- toasts of every type, with description, action and cancel, stacked bottom-right, over the HUD
  and over screens, updated and dismissed by id

Overlays open without a round trip, their content belongs to the screen, and their open state is
an input value with optional change events that the server can set by patch (ADR-0057). Overlays
stack, a click outside closes them from the top, and modal overlays dim what is below and keep the
focus inside. Menu, command and toast actions reach the server and are validated there. Holding
the HUD cursor key (left Alt by default, rebindable in the controls) shows a cursor over the HUD
with which toast buttons can be clicked while the player keeps walking (ADR-0059).
`./gradlew build` passes, and every new function, class and public property has a KDoc comment.

## Out of scope

- The navigation, layout and data component groups (plans 0011 and 0012).
- Dragging a drawer to dismiss it; drawers close by their buttons, Escape or the backdrop.
- Animations beyond the existing sheet slide and a fade of toasts.
- Typeahead in menus (jumping to an item by typing its first letter).
- Toast positions other than bottom right, and toasts with custom content.
- Other clickable HUD elements besides toasts.
- The component DSL, the GUI DSL and the storybook.

## Steps

### Step 1: Build the overlay stack

**Does:**
- **Mod:**
  - the panel's single popover becomes a stack
    - opening an overlay from inside another keeps the lower one
    - a click outside closes the overlays above the one it hits
    - Escape closes the top overlay
  - widget overlays lay out and draw a widget subtree next to a trigger, on a side with an
    alignment, flipping when there is no room
  - modal overlays dim the window below and trap Tab inside their content
  - overlay content is hidden from layout and Tab order while closed, but stays in the tree for
    patches and input values
  - triggers toggle their overlay instead of firing actions
  - a tooltip layer draws one tooltip per frame above everything
- **Protocol:** a `set_open` patch.
- **Server:**
  - open state input rules for overlay elements
  - `ScreenPatchBuilder.setOpen`

**Ends in:** A nested stack of overlays opens, closes and routes mouse and keyboard input as
described, in the mod's tests.

**Verified by:** `./gradlew build` passes, with tests for stacking, outside clicks, Escape, focus
trapping, placement and flipping, hidden content in Tab order and layout, trigger interception, and
the `set_open` patch.

**Pushes:** no

### Step 2: Add popover, hover card and tooltip

**Does:**
- **New nodes:** `popover`, `popover_content` and `popover_header` (with title and description
  text kinds), `hover_card` with `hover_card_content`, and `tooltip`.
- **Behaviour:**
  - The popover toggles on its trigger.
  - The hover card opens after hovering its trigger and closes shortly after the mouse leaves
    trigger and content.
  - The tooltip shows its text on a side of its trigger, with an arrow, while the trigger is
    hovered or focused.
- **Server and API:** elements, builders, mapper cases and open state rules.

**Ends in:** The three components exist in the protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with tests for the hover timing, tooltip placement,
inputs inside a popover being submitted, and mapping.

**Pushes:** no

### Step 3: Add dropdown menu, context menu and menubar

**Does:**
- **New nodes:**
  - containers: `dropdown_menu` with `menu_content`, `context_menu` and `menubar` with
    `menubar_menu`
  - the shared menu parts:
    - `menu_item` (text, icon, shortcut, destructive, inset, disabled)
    - `menu_checkbox_item`
    - `menu_radio_group` with `menu_radio_item`
    - `menu_label`, `menu_separator` and `menu_group`
    - `menu_sub` with `menu_sub_trigger`
- **Behaviour:**
  - Items fire actions and close the menus.
  - Checkbox items are inputs that fire an action with their new state.
  - Radio groups are inputs whose items fire the group's action with the chosen value.
  - The keyboard works throughout: Up and Down move the highlight, Enter and Space activate,
    Right opens and Left closes a sub-menu, and Escape closes the menu.
  - A context menu opens at the mouse on a right click in its area.
  - A menubar switches between its menus on hover while one of them is open.
- **Server and API:** elements, builders, mapper cases, action and input rules.

**Ends in:** All menus exist in the protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with tests for keyboard navigation and sub-menus,
checkbox and radio values on the server, context menu placement, menubar switching, and action
validation.

**Pushes:** no

### Step 4: Add command

**Does:**
- **New nodes:** `command` with `command_input`, `command_list`, `command_group`,
  `command_item` (text, icon, shortcut, keywords, disabled), `command_separator` and
  `command_empty`.
- **Behaviour:**
  - The input filters the items on the client by text and keywords.
  - Groups without matches are hidden, and the empty text shows when nothing matches.
  - Up and Down in the input move the highlight, and Enter fires the highlighted item.
  - An optional search event reports the query (ADR-0054).
- **Server and API:** elements, builders, mapper cases, action rules, and the search event.

**Ends in:** The command menu exists in the protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with tests for filtering, highlighting, empty text,
activation and mapping.

**Pushes:** no

### Step 5: Add dialog, alert dialog, sheet and drawer

**Does:**
- **New nodes:**
  - `dialog` with `dialog_content`, `dialog_header`, `dialog_footer` and `dialog_close`, plus
    title and description text kinds
  - `alert_dialog` with `alert_dialog_content` (size), `alert_dialog_media`, and the header and
    footer parts
  - `sheet` (side) and `drawer` (direction), with content, header, footer and close parts
- **Behaviour:**
  - These are modal: a backdrop, focus trapping, and a close button where shadcn has one.
  - A click on the backdrop closes all of them except alert dialogs.
  - Actions inside a close part close the overlay after firing.
- **Server and API:** elements, builders, mapper cases and open state rules.

**Ends in:** All modal overlays exist in the protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with tests for backdrop behaviour, focus trapping,
close parts, sheet sides, drawer directions, and mapping.

**Pushes:** no

### Step 6: Add toasts

**Does:**
- **Protocol:** clientbound `toast_show` and `toast_dismiss`, and serverbound `toast_action`.
- **Server:**
  - a toast service in the API
  - a per-player registry of active toasts with their handlers, which accepts actions only for
    active toasts with that button and counts them against the rate limit
- **Mod:**
  - a toast stack drawn bottom-right over the HUD and over screens
  - types with icons, and loading with the spinner
  - expiry, paused while hovered
  - close, action and cancel buttons
- **Cursor:** a key binding (left Alt by default) that releases the mouse while held, so that
  clicks reach the toasts without stopping movement.

**Ends in:** The server can show, update and dismiss toasts, and their buttons reach it.

**Verified by:** `./gradlew build` passes, with tests for the packets, the server registry
(unknown toast, expired toast, missing button, accepted), toast expiry and stacking, and the
cursor mode state.

**Pushes:** no

### Step 7: Add the overlays demo page

**Does:** Adds `/rpscreen overlays`. It shows every component of this plan, with buttons that send
toasts of every type, a toast whose action reports in chat, and a loading toast that turns into a
success. Actions report in German in chat.

**Ends in:** One screen exercises the whole overlay group.

**Verified by:** `./gradlew build` passes and the page opens on the dev stack.

**Pushes:** no

### Step 8: Verification

**Does:** Checks the whole goal on the dev stack, then runs a fresh whole-branch review and fixes
its findings.

**Ends in:** Every statement of the goal has been observed, and the review findings are fixed or
listed.

**Verified by:**
- `./gradlew build` passes, and every new declaration has a KDoc comment.
- Screenshots saved to `screenshots/0010/` of:
  - every overlay open, in the dark default theme and in one light theme
  - toasts over the HUD and over a screen
- Live checks:
  - a menu item, a checkbox item, a command item and a toast action reach the server
  - a toast button is clicked with the HUD cursor
- A reviewer's findings are fixed with tests.

**Pushes:** no

## Push points

none

## Risk

Step 1 is the most likely to go wrong. Every existing select, combobox and calendar popover moves
onto the new stack, and a slip in the click or key routing breaks inputs that work today. The
existing popover tests must pass unchanged. If they cannot pass without changing what they
assert, the agent stops and reports instead of adjusting the tests.

## If reality contradicts this plan

The agent stops and asks. It does not silently rewrite this plan to match what actually
happened, and it does not continue past a step whose stated end state was not reached.
