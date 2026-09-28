# Plan 0011: shadcn navigation and layout

- **Status:** In progress
- **Date:** 2026-09-28
- **Accepted proposal:** Port the navigation and layout components of the shadcn registry (accordion, breadcrumb, carousel, collapsible, direction, navigation-menu, pagination, resizable, scroll-area, sidebar, tabs) as native screen nodes; pre-accepted under the standing autonomy for shadcn-faithful components
- **Decision records:** ADR-0051, ADR-0052, ADR-0057, ADR-0062, ADR-0063

## Goal

`/rpscreen navigation` opens a screen on the dev stack that shows every navigation and layout
component of the shadcn registry, each working by mouse and keyboard:
- collapsible with a trigger and content
- accordion of type single (optionally collapsible) and multiple, with a chevron that turns when
  an item is open
- tabs in the variants default and line, horizontal and vertical, switched by click and arrow keys
- breadcrumb with links, the current page, separators (default and custom icon) and an ellipsis
  that opens a dropdown menu
- pagination with previous and next ("Zurück", "Weiter"), page links with the active one marked,
  and an ellipsis
- scroll area scrolling vertically, horizontally and both, with draggable bars
- resizable panel groups, horizontal and vertical, with and without a grip on the handle, dragged
  by mouse and moved by arrow keys, keeping each panel inside its limits
- carousel, horizontal and vertical, with previous and next buttons, arrow keys, item basis and
  loop
- navigation menu with triggers that open content below the list and switch on hover, and links
- sidebar on the left and on the right in the variants sidebar, floating and inset, collapsible
  offcanvas, to icons, or not at all, with header, footer, groups, menus (buttons in three sizes,
  active state, actions, badges, skeletons, sub-menus), input, separator, trigger, rail and Ctrl+B
- a direction node that shows a subtree mirrored right-to-left

The state of tabs, accordions, collapsibles, carousels, sidebars and resizable groups changes in
the mod without a round trip. It is an input value with optional change events that the server
validates and can set by patch (ADR-0062). Breadcrumb links, pagination links and navigation menu
links fire actions that reach the server and are validated there. A right-to-left subtree mirrors
rows, alignment and component sides, while text is still drawn left to right (ADR-0063).
`./gradlew build` passes, and every new function, class and public property has a KDoc comment.

## Out of scope

- The data, chat and chart components (plan 0012).
- The sidebar's mobile behaviour, where it turns into a sheet on narrow screens, and its
  persistence cookie.
- Carousel autoplay, plugins and swipe gestures; slides change by buttons, keys and the API.
- Animations: accordions, collapsibles and sidebars open and close without a transition.
- Collapsing a resizable panel to zero by dragging past its minimum.
- Real right-to-left text rendering.
- The component DSL, the GUI DSL and the storybook.

## Steps

### Step 1: Add collapsible and accordion

**Does:**
- **New nodes:** `collapsible` with `collapsible_trigger` and `collapsible_content`, and
  `accordion` (type single or multiple, collapsible) with `accordion_item` (value, disabled),
  `accordion_trigger` and `accordion_content`.
- **Behaviour:**
  - A trigger toggles its content; closed content is hidden from layout and Tab order but stays
    in the tree.
  - In a single accordion, opening an item closes the other; without `collapsible` the open item
    cannot be closed.
  - Accordion triggers draw a chevron that turns when open, and items a border below all but the
    last.
- **State:** the open state (collapsible) and the open item values (accordion) are input values
  with optional change events and a patch that sets them.
- **Server and API:** elements, builders, mapper cases and input rules (known item values, at most
  one for single).

**Ends in:** Both components exist in the protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with tests for toggling, single and multiple
accordions, the collapsible rule, hidden content, value validation and the patch.

**Pushes:** no

### Step 2: Add tabs

**Does:**
- **New nodes:** `tabs` (value, orientation) with `tabs_list` (variant default or line),
  `tabs_trigger` (value, icon, disabled) and `tabs_content` (value).
- **Behaviour:**
  - A click on a trigger selects it and shows its content only.
  - Left and Right (Up and Down when vertical) move between enabled triggers and select them.
  - The default list is a muted pill with the active trigger raised; the line list underlines the
    active trigger (on its right side when vertical).
- **Server and API:** elements, builders, mapper cases and the input rule (a known, enabled value).

**Ends in:** Tabs exist in the protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with tests for switching by click and keys, both
orientations, disabled triggers, and validation.

**Pushes:** no

### Step 3: Add breadcrumb and pagination

**Does:**
- **New nodes:** `breadcrumb` with `breadcrumb_list`, `breadcrumb_item`, `breadcrumb_link`,
  `breadcrumb_page`, `breadcrumb_separator` (optional icon) and `breadcrumb_ellipsis`; and
  `pagination` with `pagination_content`, `pagination_item`, `pagination_link` (active, disabled),
  `pagination_previous`, `pagination_next` and `pagination_ellipsis`.
- **Behaviour:** links fire actions; the current page is not clickable; the ellipsis can be the
  trigger of a dropdown menu.
- **Server and API:** elements, builders, mapper cases and action rules.

**Ends in:** Both components exist in the protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with tests for link actions, the current page, the
ellipsis menu, and action validation.

**Pushes:** no

### Step 4: Add scroll area

**Does:**
- **New node:** `scroll_area` (orientation vertical, horizontal or both) with a fixed or growing
  size.
- **Behaviour:** its content scrolls by wheel (Shift+wheel horizontally), by dragging a bar, and to
  keep the focused widget in view; content outside is clipped and does not receive the mouse.
- **Server and API:** element, builder and mapper case.

**Ends in:** The scroll area exists in the protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with tests for both axes, bar dragging, clipping and
scrolling to the focus.

**Pushes:** no

### Step 5: Add resizable

**Does:**
- **New nodes:** `resizable_panel_group` (orientation) with `resizable_panel` (default, minimum and
  maximum size in percent) and `resizable_handle` (with a grip or without).
- **Behaviour:** a handle is dragged with the mouse or moved by arrow keys when focused, resizing
  the panels on both sides within their limits.
- **State:** the panel sizes are an input value with optional change events and a patch.
- **Server and API:** elements, builders, mapper cases and the input rule (one size per panel,
  inside its limits, summing to 100).

**Ends in:** Resizable groups exist in the protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with tests for dragging, keys, limits, both orientations
and validation.

**Pushes:** no

### Step 6: Add carousel

**Does:**
- **New nodes:** `carousel` (orientation, loop) with `carousel_content`, `carousel_item` (basis in
  percent), `carousel_previous` and `carousel_next`.
- **Behaviour:** the content shows the items from the current index; the buttons and the arrow keys
  move it; without loop the buttons are disabled at the ends.
- **State:** the index is an input value with optional change events and a patch.
- **Server and API:** elements, builders, mapper cases and the input rule (an index in range).

**Ends in:** The carousel exists in the protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with tests for moving, looping, the ends, basis and
validation.

**Pushes:** no

### Step 7: Add navigation menu

**Does:**
- **New nodes:** `navigation_menu` with `navigation_menu_list`, `navigation_menu_item`,
  `navigation_menu_trigger`, `navigation_menu_content` and `navigation_menu_link` (active).
- **Behaviour:** a trigger opens its content below the list as a non-modal overlay; while one is
  open, hovering another trigger switches to it; links fire actions and close the content; a link
  directly in the list uses the trigger style.
- **Server and API:** elements, builders, mapper cases, open state and action rules.

**Ends in:** The navigation menu exists in the protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with tests for opening, hover switching, link actions
and closing.

**Pushes:** no

### Step 8: Add sidebar

**Does:**
- **New nodes:** `sidebar_provider` (open) with `sidebar` (side, variant, collapsible) and
  `sidebar_inset`; the parts `sidebar_header`, `sidebar_footer`, `sidebar_content`,
  `sidebar_group`, `sidebar_group_label`, `sidebar_group_action`, `sidebar_group_content`,
  `sidebar_menu`, `sidebar_menu_item`, `sidebar_menu_button` (size, active, icon, tooltip),
  `sidebar_menu_action`, `sidebar_menu_badge`, `sidebar_menu_skeleton`, `sidebar_menu_sub`,
  `sidebar_menu_sub_item`, `sidebar_menu_sub_button`, `sidebar_input`, `sidebar_separator`,
  `sidebar_trigger` and `sidebar_rail`.
- **Behaviour:**
  - The trigger, the rail and Ctrl+B toggle the sidebar.
  - Collapsed offcanvas hides it; collapsed to icons shows only the icons of menu buttons, with
    their tooltip on hover; `none` cannot collapse.
  - Floating and inset draw it as a card with a margin; inset draws the inset as a card.
  - Menu buttons fire actions and show the active one.
- **State:** the open state is an input value with optional change events and a patch.
- **Server and API:** elements, builders, mapper cases, open state and action rules.

**Ends in:** The sidebar exists in the protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with tests for toggling by trigger, rail and Ctrl+B,
the three collapsible modes, both sides, icon tooltips and action validation.

**Pushes:** no

### Step 9: Add direction

**Does:**
- **New node:** `direction` (ltr or rtl).
- **Behaviour:** inside rtl, rows lay out from the right, start and end alignment swap, and
  components with a side mirror it: sheets, sidebars, carousel buttons, menu chevrons and
  breadcrumb separators.
- **Server and API:** element, builder and mapper case.

**Ends in:** The direction node exists in the protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with tests for mirrored rows, alignment, nested
directions and mirrored sides.

**Pushes:** no

### Step 10: Add the navigation demo page

**Does:** Adds `/rpscreen navigation`. It shows every component of this plan, with actions
reporting in German in chat, and change events of tabs and accordions reported in chat.

**Ends in:** One screen exercises the whole navigation and layout group.

**Verified by:** `./gradlew build` passes and the page opens on the dev stack.

**Pushes:** no

### Step 11: Verification

**Does:** Checks the whole goal on the dev stack, then runs a fresh whole-branch review and fixes
its findings.

**Ends in:** Every statement of the goal has been observed, and the review findings are fixed or
listed.

**Verified by:**
- `./gradlew build` passes, and every new declaration has a KDoc comment.
- Screenshots saved to `screenshots/0011/` of every component, in the dark default theme and in
  one light theme, and of the mirrored direction.
- Live checks: a breadcrumb link, a pagination link, a navigation menu link and a sidebar menu
  button reach the server; a tab switch and an accordion change report through change events.
- A reviewer's findings are fixed with tests.

**Pushes:** no

## Push points

none

## Risk

Step 8 is the most likely to go wrong. The sidebar is the largest component of the group and
changes the layout of the whole screen around it: collapsing it to icons must resize its menus,
hide their texts and show tooltips, while the inset reflows. If the existing flex layout cannot
express the collapsed widths without special cases in every part, the agent stops and reports
instead of working around it.

## If reality contradicts this plan

The agent stops and asks. It does not silently rewrite this plan to match what actually
happened, and it does not continue past a step whose stated end state was not reached.
