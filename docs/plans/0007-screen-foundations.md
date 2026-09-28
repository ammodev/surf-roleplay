# Plan 0007: Screen foundations

- **Status:** Done
- **Date:** 2026-09-28
- **Accepted proposal:** Panel scrolling, browser-like keyboard navigation, dialog and sheet presentation with a confirmation helper, per-screen themes (default, sar, police in light and dark) and bundled Lucide icons, as the foundation for porting the shadcn component registry
- **Decision records:** ADR-0046, ADR-0047, ADR-0048, ADR-0049, ADR-0050, ADR-0051

## Goal

On the dev stack, `/rpscreen` shows the demo in any of the six theme and variant combinations,
chosen from a dropdown on the demo itself. Every widget is drawn from the theme's tokens, with
rounded corners, and the old constant roleplay palette is gone.

At GUI scale 2 in an 854x480 window, the demo is taller than the window. Its panel then fits the
window and scrolls with the mouse wheel and a scroll bar, and every button can be scrolled to and
clicked.

The whole demo form can be filled in and submitted with the keyboard alone:
- Tab and Shift+Tab move a visible focus ring through every enabled input, checkbox, dropdown
  and button, wrapping at the ends.
- Enter or Space activates, Space toggles, and the arrow keys choose dropdown options.
- The focused widget scrolls into view in scroll lists and in the panel.

A "Löschen" button on the demo opens a confirmation dialog on top of the demo, which stays
visible, dimmed and inert. Confirming or cancelling reports in chat, and Escape counts as cancel.
A "Details" button opens a sheet sliding in from the right over the demo. The server API offers
`presentation` on `open` and a `confirm` helper.

Buttons, labels and text inputs can show a Lucide icon by name, and a standalone `icon` node
exists. The icons come from an atlas that a Gradle task rasterises from a pinned Lucide release
with Apache Batik at build time, and the mod jar contains the atlas and the Lucide licence
notice.

`./gradlew build` passes, and every new function, class and public property has a KDoc comment.

## Out of scope

- The shadcn components themselves (plans 0008 to 0012), beyond icons on existing widgets.
- Enter in a text field submitting the form, text selection and clipboard copy.
- Horizontal overflow of screens wider than the window.
- Animations other than the sheet slide-in.
- Palettes defined outside the mod, and per-player theme settings.
- Coloured or multi-tone icons.

## Steps

### Step 1: Add theme and presentation fields to the protocol

**Does:**
- `ScreenOpen` gains:
  - `theme` (string, default `default`)
  - `variant` (`LIGHT` or `DARK`, default `DARK`)
  - `presentation` (`SCREEN`, `DIALOG` or `SHEET`, default `SCREEN`)
  - `sheetEdge` (`LEFT`, `RIGHT`, `TOP` or `BOTTOM`, default `RIGHT`)
- `ButtonNode`, `LabelNode` and `TextInputNode` gain an optional `icon` name.
- A new `icon` node has a name, a size, and a colour token (`foreground`, `muted`, `primary` or
  `destructive`).
- Every new field and enum constant carries `@ProtoNumber`, and the new node carries `@SerialName`.
- `docs/protocol-versioning.md` notes that icon names come from the pinned Lucide release, so
  renaming an icon is a protocol change.

**Ends in:** The protocol carries theme, variant, presentation and icons.

**Verified by:** `./gradlew :surf-roleplay-protocol:test` passes, with round trips for every new
field and the icon node, and a test that an open without the new fields decodes to the defaults.

**Pushes:** no

### Step 2: Draw the toolkit from theme tokens

**Does:**
- Adds a token model and the six palettes to the mod:
  - `default` uses shadcn's neutral palette
  - `police` uses a blue primary
  - `sar` uses a red-orange primary
  - each theme comes in light and dark
- Adds a rounded-rectangle fill and a rounded border to `UiGraphics`.
- Restyles every widget and the panel onto tokens: background, card, border, input, primary,
  muted, destructive and ring.
- Replaces `RoleplayTheme`'s colours with a token lookup from the screen's theme and variant, and
  keeps the pure sizes as layout metrics.

**Ends in:** No widget draws a fixed colour, and the screen's theme and variant decide every
colour.

**Verified by:** `./gradlew :surf-roleplay-fabric:test` passes, with tests for palette lookup,
fallback to `default` for an unknown theme, and a check that every palette defines every token.
Nothing in `ui/` outside the palettes contains a colour literal (checked with `grep`).

**Pushes:** no

### Step 3: Scroll panels whose content is taller than the window

**Does:**
- The host lays the root out at its full preferred height inside a viewport, and computes a
  panel scroll offset.
- It draws the content clipped to the viewport, with a scroll bar.
- Wheel events go to a scroll list under the mouse first, and to the panel when the list cannot
  scroll further.
- Hit-testing and dropdown list positions follow the offset.
- The scroll math lives in a pure class.

**Ends in:** Content taller than the window scrolls inside the panel.

**Verified by:** `./gradlew :surf-roleplay-fabric:test` passes, with unit tests for:
- clamping the offset to the content
- keeping the offset across a resize
- handing wheel steps from a list to the panel
- converting a point from screen to content coordinates

**Pushes:** no

### Step 4: Add browser-like keyboard navigation

**Does:**
- Adds a pure focus traversal over the widget tree. It collects every enabled interactive widget
  in tree order, moves forward or backward, and wraps at the ends.
- The host handles Tab and Shift+Tab and draws a focus ring with the `ring` token.
- Buttons activate on Enter and Space, and checkboxes toggle on Space.
- Dropdowns open on Enter, Space or Down. In the open list, Up and Down move the highlight and
  Enter selects.
- Escape closes an open list before it closes the screen.
- Focus changes scroll the focused widget into view, in its scroll list and in the panel.
- Clicking an interactive widget focuses it.

**Ends in:** The demo is usable from the keyboard alone.

**Verified by:** `./gradlew :surf-roleplay-fabric:test` passes, with unit tests for:
- traversal order
- wrap-around in both directions
- skipping disabled and removed widgets
- focus after a patch
- the scroll-into-view offsets

**Pushes:** no

### Step 5: Present dialogs and sheets over their parent

**Does:**
- **Mod:** splits the screen host into panels and one Minecraft screen that draws a layer list:
  - every stacked screen down to the nearest `SCREEN` presentation
  - a dimmed backdrop under each dialog or sheet
  - sheets slide in from their edge
  - only the top panel receives input, and a click on the backdrop closes a closable dialog or
    sheet
  - typed screens become panels too
- **Server and API:**
  - `ScreenService.open` gains `presentation` and `sheetEdge` parameters, and screen definitions
    gain `theme` and `variant`
  - the mapper sends all four
  - `ScreenService` gains `confirm(player, parent, title, text, confirmLabel, cancelLabel,
    destructive, onConfirm, onCancel)`, which opens a dialog, runs exactly one of the two
    handlers, and treats Escape, a backdrop click or a closed parent as cancel

**Ends in:** Child screens can appear on top of their parent, and the server can ask for a
confirmation with one call.

**Verified by:** `./gradlew build` passes, with unit tests for:
- layer selection (down to the nearest `SCREEN`)
- input going only to the top layer
- the confirm helper running exactly one handler on confirm, cancel, Escape and parent close

**Pushes:** no

### Step 6: Rasterise the Lucide icon set at build time

**Does:**
- Adds a Gradle task to the mod build that downloads a pinned Lucide release and rasterises every
  SVG with Apache Batik (build-script classpath only) into white 64x64 images.
- Packs the images into one atlas PNG with a name-to-cell index.
- Writes a contact sheet of every icon for inspection.
- The outputs go under `build/`, and `processResources` puts the atlas, the index and the Lucide
  licence notice into the mod jar.

**Ends in:** The mod jar contains the icon atlas, its index and the licence notice.

**Verified by:** `./gradlew :surf-roleplay-fabric:build` passes, the jar listing shows the three
files, and the contact sheet is inspected visually. Icons that render wrongly are listed. If any
exist, the agent stops and asks before continuing.

**Pushes:** no

### Step 7: Draw icons in the mod and expose them in the API

**Does:**
- **Mod:** loads the atlas and index as a GUI texture and draws tinted icons for:
  - the `icon` node
  - the leading icon of buttons, labels and text inputs
  - a placeholder for unknown names
- **API:** adds an `icon` element and `icon` parameters to the DSL.
- **Paper:** maps them.

**Ends in:** Screens can show any Lucide icon by name.

**Verified by:** `./gradlew build` passes, with tests for the icon index lookup and placeholder
fallback, API and mapping tests for the icon element and parameters, and a dev-client screenshot
of icons in buttons and labels.

**Pushes:** no

### Step 8: Extend the debug demo

**Does:** Changes `/rpscreen`:
- It gets a theme dropdown and a light or dark switch that reopen the demo in the chosen theme.
- It gets icons on its buttons.
- A "Löschen" button opens a confirmation dialog that reports its outcome in German in chat.
- A "Details" button opens a sheet from the right with a "Schließen" button.
- It is tall enough to overflow at GUI scale 2 in an 854x480 window.

**Ends in:** The demo exercises every feature of this plan.

**Verified by:** `./gradlew build` passes and the demo opens on the dev stack.

**Pushes:** no

### Step 9: Verification

**Does:** Checks the whole goal on the dev stack after `./gradlew build` and a restart of
`paper`, `velocity` and `ms-roleplay`.

**Ends in:** Every statement of the goal has been observed.

**Verified by:**
- `./gradlew build` passes.
- Every new declaration has a KDoc comment with no ADR, plan or history reference.
- In the dev client:
  - screenshots of the demo in all six theme and variant combinations
  - at 854x480 and GUI scale 2: the panel fits the window, scrolls, and "Absenden" can be reached
    and clicked
  - at 1920x1080 and GUI scale 4: the demo fits without a scroll bar
  - the full form filled in and submitted with the keyboard only, with the focus ring visible and
    focused widgets scrolled into view
  - "Löschen" opens the dialog over the dimmed demo, the demo ignores clicks, and confirm, cancel,
    Escape and a backdrop click each report correctly
  - "Details" slides the sheet in from the right
  - icons render in the buttons
- The roadmap tasks 307 to 311 are Done.

**Pushes:** no

## Push points

none

## Risk

Step 6 is the most likely to go wrong. Batik may misrender some of Lucide's stroke-based SVGs,
for example those that use `currentColor` or unusual path features, and the pinned release's
archive layout has to match the task. The contact sheet makes misrendered icons visible. If any
are wrong, the agent lists them and asks the human how to proceed. It does not skip them
silently or switch rasterisers on its own.

## If reality contradicts this plan

The agent stops and asks. It does not silently rewrite this plan to match what actually
happened, and it does not continue past a step whose stated end state was not reached.
