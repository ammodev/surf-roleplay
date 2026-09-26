---
name: surf-api-inventory
description: How to build chest GUIs with the surf-api inventory framework (surfView / paginatedSurfView, a Kotlin DSL on top of devnatan's inventory-framework) - lifecycle hooks, slots, state, stateful buttons, pagination, and the resource-pack title container (header text, row text, blocked cells, custom glyph overlays). Use whenever creating, changing, or debugging any inventory menu or custom GUI in this repository.
---

# surf-api inventory framework

Source of truth: `S:\Workspaces\surf-api\surf-api-paper\surf-api-paper\src\main\kotlin\dev\slne\surf\api\paper\inventory\framework\`
Working examples: `S:\Workspaces\surf-api\surf-api-paper\surf-api-paper-plugin-test\src\main\kotlin\dev\slne\surf\surfapi\bukkit\test\command\subcommands\inventory\`
(`TestInventoryView.kt`, `TestButtonView.kt`, `TestPaginatedView.kt`). If something below seems off,
read those. They are the maintained reference.

The framework wraps **devnatan inventory-framework (IF)** (`me.devnatan.inventoryframework.*`,
forked as `dev.slne.forks.inventoryframework`). surf-api adds a DSL, typed state handles,
stateful buttons, pagination, and a **title container** that draws resource-pack
textures and text into the inventory title. Project rules still apply to everything you
write: English identifiers, German player-facing text (ADR-0008), and a doc comment on every
declaration.

## Mental model

```
surfView("Header") {           // DSL block runs ONCE at construction, not per player
    val count = mutableState(0)          // declare state + buttons here (deferred, per viewer)
    settings { rows(ViewRows.FIVE) }     // size, cancel behaviour, fonts, geometry
    containerDefaults { ... }            // title-bar composition: header, rowText, blockCell, custom glyphs
    onOpen { ... }                       // per open, can cancel / modifyConfig
    onFirstRender { slot(...) { ... } }  // place items
    onClick { ... }  onUpdate { ... }  onClose { ... }
}
```

- `surfView` / `paginatedSurfView` return an `AbstractSurfView` (an IF `View`). The block is
  configuration. Anything that must differ per player goes into state or a render callback,
  never into a plain local variable.
- **Register every view once in `onLoad`** (`view.register()`), then `view.open(player)` or
  `view.open(player, data)`. An unregistered view cannot be opened.
- Hold views as top-level `val`s or `object` properties. They are singletons shared by all viewers.

## Imports you will need

```kotlin
import dev.slne.surf.api.paper.inventory.framework.view.*            // surfView, settings, onFirstRender, containerDefaults, ...
import dev.slne.surf.api.paper.inventory.framework.dsl.slot           // RenderContext.slot { }
import dev.slne.surf.api.paper.inventory.framework.dsl.withItem       // BukkitItemComponentBuilder.withItem
import dev.slne.surf.api.paper.inventory.framework.dsl.renderWith
import dev.slne.surf.api.paper.inventory.framework.dsl.onItemClick
import dev.slne.surf.api.paper.inventory.framework.register
import dev.slne.surf.api.paper.inventory.framework.open
import dev.slne.surf.api.paper.inventory.framework.view.state.*       // mutableState, get, set, increment
import dev.slne.surf.api.paper.inventory.framework.view.button.*      // toggleButton, statefulButton, button(handle)
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.* // header, rowText, blockRow, blockAllSlots, ...
import dev.slne.surf.api.paper.inventory.framework.view.pagination.pagination
import dev.slne.surf.api.paper.inventory.framework.view.settings.*    // ViewRows, PaginationViewRows, PaginationEmptyRows
import dev.slne.surf.api.paper.inventory.framework.view.settings.align.TextAlignment
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.displayName
```

## Minimal view

```kotlin
val bankView = surfView("Bank") {
    val clicks = mutableState(0)                    // MutableIntState handle

    settings { rows(ViewRows.THREE) }

    containerDefaults {
        header { darkSpacer("Bank") }               // SurfComponentBuilder block
        rowText(3, "Guthaben: 1.250 €")             // one-based row
    }

    onFirstRender {
        slot(2, 5) {                                // row 2, column 5 (ONE-based, see gotchas)
            withItem(ItemType.GOLD_INGOT) { displayName { text("Einzahlen") } }
            onClick(clicks::increment)              // or: onItemClick { ... }
        }
    }

    onClose { player.sendMessage(text("Klicks: ${clicks[this]}")) }
}

// PaperMain.onLoadAsync(): bankView.register()
// anywhere:                bankView.open(player)
```

Lifecycle hooks and their receivers (from `SurfViewLifecycleDsl.kt`):

| Hook | Receiver | Typical use |
|---|---|---|
| `onInit { }` | `ViewConfigBuilder` | IF config: `layout { +"XXXXXXXXX" }`, extra config |
| `onOpen { }` | `OpenContext` | permission check → `cancel()`, read `initialData`, `modifyConfig { }` |
| `onFirstRender { }` | `RenderContext` | place items with `slot`, `layoutSlot`, `firstSlot`, `availableSlot` |
| `onClick { }` | `SlotClickContext` | view-wide click handler |
| `onUpdate { }` | `Context` | reacting to `update()` calls |
| `onClose { }` | `CloseContext` | persist, `cancel()` to refuse closing |
| `containerDefaults { }` | `ViewContainerModificationContext` | title-bar composition (see below) |

Inside every hook a `view` property is available (the built view). Only one callback per hook.
A second `onClick { }` replaces the first.

## Slots and items

- `slot(index) { }` uses a zero-based linear index (0 to rows*9-1).
- `slot(row, column) { }` is **ONE-based** (row 1..6, column 1..9). IF's `SlotConverter` does
  `(row-1)*9 + (column-1)` and clamps 0 to 1. The surf-api KDoc says "zero-based", and the KDoc is wrong.
  `slot(1, 1)` is the top-left slot.
- On the item builder: `withItem(ItemType|Material) { itemStack DSL }` for a static item,
  `renderWith(...)` for a render-time item, `onItemRender { }`, `onItemClick { }`, `onItemUpdate { }`.
  IF-native: `withItem(itemStack)`, `renderWith { itemStack }`, `onClick { click -> }` /
  `onClick { }` (Runnable) are available on the same builder.
- Custom-model items: build them with `buildItem(ItemType.X) { ... }` and set the item model or
  custom model data yourself. The framework does not care what the item is.
- Clicks are cancelled by default (`cancelOnClick/Drag/Drop/Pickup` all default `true`). For slots
  players should put items into (stashes, trunks), turn cancelling off in `settings { }` and
  control it per slot.
- Navigation: `openForPlayer(otherView)` / `openForPlayer(otherView, data)` inside a context.
  History is tracked automatically. An **outside click navigates back** to the previous view
  (or closes if there is none) because `navigateBackOnOutsideClick` defaults to `true`.

## State (per viewer)

Declare state in the DSL block. It resolves to one IF state per viewer context.

```kotlin
val counter  = mutableState(0)                    // StateHandle<MutableIntState>
val selected = mutableState<Vehicle?>(null)       // StateHandle<MutableState<T>>
val balance  = computedState { ctx -> lookup(ctx.player) }   // recomputed on each read
val profile  = lazyState { ctx -> load(ctx.player) }         // computed once per viewer
val target   = initialState<UUID>()               // value passed as open(player, data)
```

Read and write with the context: `counter[ctx]`, `counter[ctx] = 5`, `counter.increment(ctx)`.
Inside a hook the receiver itself is the context, so write `counter[this]`. Writing a
`MutableState` re-renders the slots that read it.

**Suspending data.** The render hooks are not suspend functions. Load data before opening
(`plugin.launch { val d = service.load(); view.open(player, d) }` plus `initialState`), or use
a suspend pagination source. Never block the main or region thread with `runBlocking`.

## Stateful buttons

Declared in the DSL block and placed with `button(handle)` inside a slot. See `TestButtonView.kt`.

```kotlin
val lights = toggleButton(initial = false) {
    whenOn  { item(ItemType.LIME_DYE) { displayName { text("Licht: an") } } }
    whenOff { item(ItemType.GRAY_DYE) { displayName { text("Licht: aus") } } }
    onToggle { on -> /* SlotClickContext */ }
    onCloseChanged { initial, current -> /* persist only if changed */ }
}
onFirstRender { slot(1, 5) { button(lights) } }
// read anywhere: lights[ctx], lights.hasChanged(ctx), lights.initial(ctx)
```

Also `computedToggleButton({ ctx -> loadInitial(ctx) }) { }`, `tripleButton(initial) { state(v) { } }`,
`statefulButton(initial) { state(...) ...; reverseOnRightClick(false); onChange { from, to -> } }`,
and their `computed*` variants. `onCloseChanged` fires only if the view actually closed (not cancelled).

## Pagination

```kotlin
val garageView = paginatedSurfView("Garage") {
    settings {
        paginationViewRows(PaginationViewRows.THREE)   // content rows; inventory = content + 1 button row
        paginationEmptyRows(PaginationEmptyRows.NONE)
        paginationButtonsAtBottom()                   // or paginationButtonsAtTop()
    }
    layoutTarget('I')                                  // REQUIRED, any char
    pagination<OwnedVehicle> {                         // REQUIRED
        suspendSource(plugin) { ctx -> vehicleService.list(ctx.player.uniqueId) }
        itemFactory { vehicle ->
            withItem(ItemType.MINECART) { displayName { text(vehicle.plate) } }
            onItemClick { /* spawn */ }
        }
    }
}
```

- The framework **generates the layout itself**: content rows get `" IIIIIII "`, so items use
  columns 2 to 8 only (7 per row). Every other cell gets a block overlay, and the navigation arrows and page
  indicator are drawn into the title.
- Sources: `source(list)`, `source { }` (evaluated once), `computedSource { ctx -> }` (every
  page load), `lazySource`, `asyncSource` / `lazyAsyncSource` (CompletableFuture),
  `suspendSource(plugin|scope) { ctx -> }`, `lazySuspendSource(...)`.
- `elementFactory { ctx, builder, index, value -> }` for full control, `onPageSwitch { ctx, p -> }`,
  `horizontal()` / `vertical()`.
- Options: `paginationPageIndicator { cur, total -> component }` (or `null`), `paginationSwitchSound(sound|null)`.
- Extra container components: `containerDefaults { }` works in paginated views too. The border
  blocking is `final`, and your block runs after it.

## The title container (the resource-pack part)

Minecraft only lets a server draw custom visuals through the inventory **title**. surf-api
composes the title from `ViewContainerComponent`s. Each component shifts the cursor with
negative-space glyphs, renders its glyph or text, and shifts back. Everything is rendered in
font `surf:menu` unless the component sets its own.

In `containerDefaults { }` (or later via `modifyContainer(ctx) { }` inside a hook):

| Call | Effect |
|---|---|
| `header("Text")`, `header(component)`, `header { darkSpacer("...") }` | title text. Alignment defaults to CENTER |
| `rowText(row, text, TextAlignment.LEFT, columns = 0..2)` | text vertically centred in a slot row (row one-based 1..6, columns zero-based 0..8) |
| `rowText(row) { builder }`, `rowTextAt(row, column, text)` | builder / left-edge anchored variants |
| `clearRowText(row)`, `clearRowTexts()` | remove row texts |
| `blockCell(column, row)` | grey "blocked" overlay on one cell (column zero-based, row one-based) |
| `blockSlot(slot)`, `blockRow(row, exempt)`, `blockColumn(col, exempt)`, `blockAllSlots(rows, exemptSlots)` | bulk blocking |
| `unblockCell`, `unblockSlot`, `unblockAllSlots()` | remove overlays |
| `addChild(component)`, `removeChild`, `removeChildrenOfType<T>()`, `hasComponentOfType<T>()` | custom components |

Blocking is **purely cosmetic**. It draws a texture and does not stop clicks. Use the cancel
settings or a click handler for that.

To change the title after opening, use `modifyContainer(ctx) { header(...) }`, or `updateHeader(ctx, ...)`
in a subclass. Never call `modifyConfig { title(...) }` expecting it to replace the
container. It gets adopted as the header text.

### Custom full-screen GUIs (phone, bank terminal, shop panels)

Set up a custom inventory texture like this:

1. Put a glyph in a resource-pack font (Nexo glyph or an external pack merged via Nexo
   `external_packs`). Its `ascent`/`height` fix its vertical position. Horizontal position
   comes from shifts.
2. Implement `ViewContainerComponent`:
   ```kotlin
   /** Draws the phone frame texture over the whole inventory area. */
   data class PhoneFrameComponent(val glyph: Char) : ViewContainerComponent {
       override val positionalShift = -48      // move to the texture's left edge (see ViewContainerGlyphComponent)
       override val textureWidth = 222         // MUST equal the glyph's real advance in pixels
       override fun SurfComponentBuilder.renderComponent() {
           text(glyph); color(Colors.WHITE)    // white = untinted texture
           // font(key("roleplay", "phone"))   // if the glyph lives in its own font
       }
   }
   ```
   `data class` gives you the `equals`/`hashCode` the container needs (duplicates are dropped).
   Components that render arbitrary text must return `hasExactWidth = false`. They are drawn last.
3. `containerDefaults { addChild(PhoneFrameComponent('\uE100')) }`. Optionally
   `settings { backgroundGlyph(true) }` to use surf's per-row background glyphs, and re-measure
   `headerGeometry { copy(...) }` when you swap the inventory texture.
4. Clickable areas are still slots. Put transparent or custom-model items in the slots under
   your painted "buttons" and give them `onItemClick`.

`shift(px)` (`view.util.GlyphShift`) returns the negative/positive-space string for manual
offsets. A wrong `textureWidth` shifts **every component after it**. It's the most common cause
of "everything is off by N pixels".

### Settings reference (`settings { }`)

`rows(ViewRows.X | 1..6)` (simple views, default FIVE), `cancelOnClick/Drag/Drop/Pickup(bool)`,
`cancelAllInteractions()`, `navigateBackOnOutsideClick(bool)`, `font(key)` (header font, default
`minecraft:default`, `SurfViewSettingsDefaults.ASCENDED_HEADER_FONT` puts it one line lower),
`headerTextAlignment`, `headerTextColor` (default `#404040`), `headerFontMetrics`,
`rowFont(row, key)` / `rowFonts(map)` (defaults `surf:menu_font_row_1..6`), `rowFontMetrics`,
`backgroundGlyph(bool)` (default false), `headerGeometry(...)`.

The `surf:*` fonts come from the surf resource pack. Without it, the title shows placeholder boxes.

## Gotchas

- `slot(row, column)` is ONE-based. `rowText` rows are one-based, `blockCell` columns are zero-based.
- The DSL block runs once. `val x = player...` there does not compile or is wrong. Use state.
- Forgot `register()` in `onLoad`: the view fails to open.
- The whole view defaults to cancelling interactions. Plan item-transfer GUIs explicitly.
- Outside click = back navigation, not close.
- Button close handlers are skipped if `onClose` cancelled the close.
- The Paper server runs mccoroutine-folia. Launch with `plugin.launch { }`, and re-enter the
  player's region/entity scheduler before touching the world.

## Alternative: subclassing

For views with complex logic you can subclass `AbstractSurfView(header)` (or
`AbstractPaginatedSurfView`) and override `onViewInit/Open/Render/Click/Close/Update`,
`containerDefaults()`, and `settings`. Use the protected `modifyContainer` and `updateHeader`. The IF
`final` overrides apply settings and the container before your hooks run. Plain IF `View`
subclasses also work (see `TestInventoryView` object), but they lose the container features.
