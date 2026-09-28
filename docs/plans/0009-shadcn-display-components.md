# Plan 0009: shadcn display components

- **Status:** In progress
- **Date:** 2026-09-28
- **Accepted proposal:** Port the display components of the shadcn registry (alert, aspect-ratio, avatar, badge, card, empty, item, kbd, progress, separator, skeleton, spinner, typography) as native screen nodes, and make screen texts wrap; pre-accepted under the standing autonomy for shadcn-faithful components
- **Decision records:** ADR-0051, ADR-0055, ADR-0056

## Goal

`/rpscreen display` opens a screen on the dev stack that shows every display component of the
shadcn registry in every variant and size:
- typography: the headings h1 to h4, paragraph, lead, large, small, muted, blockquote, inline code,
  and bulleted and numbered lists
- alert, in the variants default and destructive, with and without an icon
- badge, in the variants default, secondary, destructive, outline, ghost and link, with and
  without an icon
- kbd and kbd group
- separator, horizontal and vertical
- skeleton, as a rectangle and as a circle, pulsing
- spinner, rotating, in several sizes
- progress, in shadcn's look
- aspect ratio, holding an image
- avatar, in the sizes sm, default and lg, showing a player head, a resource-pack texture and a
  fallback text, with a badge; avatar group with a count
- card, with header, title, description, action, content and footer
- empty, with header, media in the variants default and icon, title, description and content
- item, in the variants default, outline and muted and the sizes default and sm, with media,
  content, title, description, actions, header and footer, in an item group with separators, and
  a clickable item that fires an action

Texts wrap to the width their container gives them (ADR-0055): long descriptions in alerts, cards,
items, empties and form fields break at word boundaries instead of overflowing, and a text with a
line limit ends in an ellipsis. Buttons, badges and kbd never wrap. Avatars show a player head by
UUID or a resource-pack texture, and their fallback text otherwise (ADR-0056). `./gradlew build`
passes, and every new function, class and public property has a KDoc comment.

## Out of scope

- The overlay, navigation and data component groups (plans 0010 to 0012), including tooltips on
  badges or kbd.
- Remote image URLs in avatars or images (ADR-0056).
- A monospace font for inline code; it is drawn with the regular font on a muted background.
- Hyphenation and breaking inside words, except for a single word wider than its container.
- The component DSL, the GUI DSL and the storybook.
- Feedback for rejected submits (deferred from plan 0008).

## Steps

### Step 1: Make the layout height-for-width and texts wrap

**Does:**
- **Layout:** `FlexLayout.measure` takes the width available to a box. Columns pass their inner
  width to their children; rows share their width among their children and shrink wrapping
  children towards their longest word before overflowing. A leaf may compute its size from the
  width it gets. Measurements are cached per width.
- **Text:** `TextMeasurer` gains line wrapping, and `UiGraphics` draws wrapped text with an
  optional line limit and an ellipsis on the last shown line.
- **Widgets:** labels and field texts wrap.

**Ends in:** A long label or field description in a narrow column is drawn on several lines, and
the column grows by exactly those lines.

**Verified by:** `./gradlew build` passes, with layout tests for wrapping in columns, rows with a
fixed sibling, growing children and scroll lists, a test that texts that fit keep one line, and
tests for the line limit.

**Pushes:** no

### Step 2: Add typography, separator, kbd and badge

**Does:**
- **New nodes:**
  - `text` (kind, text, line limit, alignment). Its kinds are the typography styles h1 to h4,
    paragraph, lead, large, small, muted, blockquote and inline code, and the slot texts: alert,
    card, empty and item titles and descriptions. Headings are drawn with a scaled font.
  - `text_list` (items, ordered)
  - `separator` (orientation)
  - `kbd` (text, icon) and `kbd_group`
  - `badge` (text, icon, variant)
- **Server and API:** each node gets its API element, builder, mapper case and element rule
  (texts can be patched).

**Ends in:** Every text style, separators, keys and badges exist in the protocol, the mod and the
API.

**Verified by:** `./gradlew build` passes, with round-trip, mapping and widget size tests for each
node, including that badges and kbd never wrap and that `SetText` changes their text.

**Pushes:** no

### Step 3: Add skeleton, spinner, progress and aspect ratio

**Does:**
- **New nodes:** `skeleton` (round), `spinner` (size), and `aspect_ratio` (ratio, one child), whose
  height follows its width.
- **Progress:** restyled to shadcn's thin rounded track.
- **Mod:** the skeleton pulses and the spinner rotates, both driven by the frame time.

**Ends in:** Loading placeholders, spinners and ratio boxes exist in the protocol, the mod and the
API.

**Verified by:** `./gradlew build` passes, with tests for the aspect ratio's height at several
widths, the pulse and rotation as functions of time, and mapping.

**Pushes:** no

### Step 4: Add avatars

**Does:**
- **New nodes:** `avatar` (source, fallback text, size, badge with an optional icon),
  `avatar_group` and `avatar_group_count`. The source is a player UUID or a texture identifier
  (ADR-0056).
- **Mod:** draws the face of a player's skin, loaded through Minecraft's skin manager and cached,
  or a texture clipped to a circle, or the fallback text while no picture is available. Avatars in
  a group overlap, with a ring in the background colour.
- **Server and API:** elements, builders and mapper cases, and a check that a UUID source is a
  UUID.

**Ends in:** Avatars show player heads, textures and fallbacks in three sizes.

**Verified by:** `./gradlew build` passes, with tests for source parsing, the fallback while
loading, the group overlap layout, and mapping.

**Pushes:** no

### Step 5: Add alert and card

**Does:**
- **New nodes:** `alert` (variant, icon), and `card`, `card_header`, `card_action`, `card_content`
  and `card_footer`. Alert and card titles and descriptions are `text` kinds.
- **Mod:** the alert draws its icon beside a column of title and description, and the destructive
  variant tints its texts. The card header places an action at the top right, beside its title and
  description.

**Ends in:** Alerts and cards exist in the protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with layout tests for the alert with and without an
icon, the card header with and without an action, and mapping.

**Pushes:** no

### Step 6: Add empty and item

**Does:**
- **New nodes:** `empty`, `empty_header`, `empty_media` (variant, icon) and `empty_content`; and
  `item` (variant, size, clickable), `item_media` (variant), `item_content`, `item_actions`,
  `item_header`, `item_footer`, `item_group` and `item_separator`.
- **Behaviour:** a clickable item highlights under the mouse, can take the focus, and fires a
  widget action on a click, Enter or Space. Its element rule makes it an action, like a button
  that does not submit input.
- **Server and API:** elements, builders, mapper cases and rules.

**Ends in:** Empty states and items exist in the protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with tests for the item's header and footer layout, the
clickable item's action by mouse and by keyboard, the server's action validation, and mapping.

**Pushes:** no

### Step 7: Add the display demo page

**Does:** Adds `/rpscreen display`, which shows every component of this plan in every variant and
size, grouped into sections, with a theme and variant switcher. Clicks on items and buttons report
in German in chat.

**Ends in:** One screen exercises the whole display group.

**Verified by:** `./gradlew build` passes and the page opens on the dev stack.

**Pushes:** no

### Step 8: Verification

**Does:** Checks the whole goal on the dev stack, then runs a fresh whole-branch review and fixes
its findings.

**Ends in:** Every statement of the goal has been observed, and the review findings are fixed or
listed.

**Verified by:**
- `./gradlew build` passes, and every new declaration has a KDoc comment.
- Screenshots saved to `screenshots/0009/`: the demo page in the default dark theme and in one
  light theme, and wrapped texts at two window widths.
- The skeleton pulses, the spinner turns, an avatar shows the logged-in player's head, and a click
  on the clickable item reaches the server.
- A reviewer's findings are fixed with tests.

**Pushes:** no

## Push points

none

## Risk

Step 1 is the most likely to go wrong. Every existing screen goes through the changed layout, and a
slip in the width passing or the row shrink step moves or resizes widgets on screens that do not
use wrapping at all. The existing layout and widget tests must pass unchanged. If they cannot pass
without changing what they assert, the agent stops and reports instead of adjusting the tests.

## If reality contradicts this plan

The agent stops and asks. It does not silently rewrite this plan to match what actually
happened, and it does not continue past a step whose stated end state was not reached.
