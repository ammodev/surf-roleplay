---
name: surf-api-dialog
description: How to build Minecraft dialogs (Paper 1.21.6+ Dialog API) with the surf-api Kotlin DSL - dialog/base/body/input/type builders, action buttons and callbacks, reading input values, composable stateful dialogs, paginated and search dialogs, and resource-pack overlays inside dialogs. Use whenever a feature needs text/number/option input from a player, a confirmation, a notice, or a dialog-based screen such as the phone.
---

# surf-api dialog DSL

Source of truth: `S:\Workspaces\surf-api\surf-api-paper\surf-api-paper\src\main\kotlin\dev\slne\surf\api\paper\dialog\`
Working examples: `S:\Workspaces\surf-api\surf-api-paper\surf-api-paper-plugin-test\src\main\kotlin\dev\slne\surf\surfapi\bukkit\test\command\dialog\subcommands\`
(`PaginatedDialogTest.kt`, `SearchDialogTest.kt`). The inventory test view also uses `noticeDialog`.

The DSL is a thin Kotlin layer over Paper's `io.papermc.paper.dialog` / `io.papermc.paper.registry.data.dialog`
API (`@file:Suppress("UnstableApiUsage")` is normal in these files). A dialog is an immutable
`Dialog` value that you show with `player.showDialog(dialog)`. Every "update" means building
and showing a new dialog.

In this repository dialogs are **the input mechanism** (forms, amounts, names, confirmations).
Chest GUIs are built with the `surf-api-inventory` skill. Player-facing text is German (ADR-0008).

## Structure

```
dialog {                         // DialogRegistryEntry.Builder
    base {                       // DialogBaseBuilder - title REQUIRED
        title { text("...") }    // Component or SurfComponentBuilder block
        externalTitle { ... }    // shown on the button that opens this dialog from a list
        preventClosingWithEscape()        // or canCloseWithEscape = false
        afterAction(DialogBase.DialogAfterAction.CLOSE | NONE | WAIT_FOR_RESPONSE)
        body { ... }             // DialogBodyBuilder: messages and items, top to bottom
        input { ... }            // DialogInputBuilder: form fields, below the body
    }
    type { ... }                 // DialogTypeBuilder - exactly one type
}
```

Imports: `dev.slne.surf.api.paper.dialog.{dialog, base, type, noticeDialog, clearDialogs}`,
`dev.slne.surf.api.paper.dialog.builder.{actionButton, dialogAction}`.

### Body (`body { }`)

- `plainMessage(component, width?)` / `plainMessage { info("..."); variableValue(x) }` / `plain { message(...); width(300) }`
- `item { item(ItemType.PAPER) { ... }; simpleDescription(...); showTooltip(false); width(16); height(16) }`

### Inputs (`input { }`), each with a string **key**

| Builder | Response getter |
|---|---|
| `text("name") { label { text("Vorname") }; initial("Max"); maxLength(32); width(200); multiline(maxLines = 4, height = 80); labelVisible(true) }` | `response.getText("name")` |
| `simpleText("name", label)` / `simpleText("name") { text("…") }` | `getText` |
| `boolean("agb") { label(...); initial(false); onTrue("ja"); onFalse("nein") }` / `simpleBoolean("agb", label, default)` | `response.getBoolean("agb")` |
| `numberRange("amount", 1..10_000) { label(...); initial(100f); step(1f); labelFormat("%s: %s €"); width(300) }` / `simpleNumberRange(...)` | `response.getFloat("amount")` |
| `singleOption("gender") { label(...); option("m", selected = true) { text("Männlich") }; option("f") { text("Weiblich") } }` | `response.getText("gender")` returns the option key |
| `addInput(paperDialogInput)` | raw Paper input |

All getters return nullable values. Validate them on the server, because the client can send anything.

### Types (`type { }`)

- `notice()` / `notice { label { text("OK") }; action { ... } }` has a single button.
- `confirmation { yes { label{..}; action{..} }; no { ... } }` has two buttons. Both are required.
- `multiAction { columns(2); action { label{..}; tooltip{..}; width(150); action { ... } }; exitAction { ... } }`
  gives a grid of buttons. Also `multiAction(vararg actionButtons)`.
- `dialogList(dialogA, dialogB) { columns(1); buttonWidth(200); exitAction { } }` gives buttons that open
  other dialogs, labelled with their `externalTitle`.
- `serverLinks { columns(2); buttonWidth(150) }`

An action button is `actionButton { label(...); tooltip(...); width(...); action { ... } }`.

### Actions (`action { }` in a button)

| Call | What happens |
|---|---|
| `customPlayerClick { response, player -> }` | **Server callback with the form values**. The usual choice |
| `customClick(options) { response, audience -> }` | same, any Audience |
| `playerCallback { player -> }` / `callback { audience -> }` | server callback without form values |
| `showDialog(otherDialog)` / `showDialog { builder }` | client opens another dialog |
| `staticAction(ClickEvent.xxx)`, `openUrl`, `copyToClipboard`, `suggestCommand`, `commandTemplate("pay $(amount)")` | client-side actions |
| `customClick(key, nbt)` | custom payload with an id, handled by a `PlayerCustomClickEvent` listener |

## Example: form with validation

```kotlin
/** Callback options that stay valid for repeated clicks. */
val unlimitedOptions = ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).build()

/** Asks the player how much money to transfer and to whom. */
fun transferDialog(onSubmit: suspend (Player, String, BigDecimal) -> Unit) = dialog {
    base {
        title { text("Überweisung") }
        body { plainMessage { info("Empfänger und Betrag eingeben.") } }
        input {
            text("iban") { label { text("Kontonummer") }; maxLength(34) }
            numberRange("amount", 1..100_000) { label { text("Betrag") }; step(1f); initial(100f) }
        }
    }
    type {
        confirmation {
            yes {
                label { text("Senden") }
                action {
                    customPlayerClick(unlimitedOptions) { response, player ->
                        val iban = response.getText("iban")?.trim().orEmpty()
                        val amount = response.getFloat("amount")?.toBigDecimal() ?: return@customPlayerClick
                        plugin.launch { onSubmit(player, iban, amount) }
                    }
                }
            }
            no { label { text("Abbrechen") }; action { playerCallback { it.clearDialogs() } } }
        }
    }
}
```

## Callback lifetime: single use by default

Server callbacks (`customClick`, `customPlayerClick`, `callback`, `playerCallback`) are Adventure
`ClickCallback`s. When no options are passed, the builder uses `ClickCallback.Options.builder().build()`,
which is **1 use and a 12h lifetime**. For a dialog built once and stored in a `val`, the first click
from any player consumes the callback, and every later click does nothing. So:

- build dialogs **per show** (a function returning `Dialog`, as above), or
- pass `ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).lifetime(...).build()`.

Callbacks run on a thread that is not yours. For world or entity work, hop onto the player's
scheduler. For I/O, `plugin.launch { }` (mccoroutine-folia).

## Helpers

- `noticeDialog(title, message, width?)`, `noticeDialog { builder }`, `noticeDialogWithBuilder(title) { text }`.
- `player.clearDialogs(showEmptyDialogBefore = false)` closes dialogs. It is NMS-backed, so opt in with
  `@OptIn(NmsUseWithCaution::class)`.
- `searchDialog(title = { }, searchInput = { initialValue = ... }, onSearch = { p, q -> }, onClose = { p, q -> }, body = { })`
  is a ready-made text search with Search/Cancel buttons.
- Standalone builders: `dialogBase { }`, `dialogBody { }`, `dialogInput { }`, `dialogType { }`, `dialogAction { }`, `actionButton { }`.

## Stateful dialogs (composition)

`composableDialog` re-renders a dialog from immutable state, somewhat like a tiny Compose:

```kotlin
data class AtmState(val tab: Tab = Tab.BALANCE) : DialogState

plugin.launch {
    composableDialog(player, AtmState(), plugin.scope) {
        val s = state()
        val balance = remember(s.tab) { bank.balance(player) }   // suspend, cached per key list
        dialog {
            base { title { text("Geldautomat") }; body { plainMessage(text("$balance €")) } }
            type {
                multiAction {
                    Tab.entries.forEach { tab ->
                        action {
                            label { text(tab.label) }
                            action { playerCallback { plugin.launch { setState { copy(tab = tab) } } } }
                        }
                    }
                }
            }
        }
    }
}
```

- `state()` reads, and `setState { copy(...) }` (suspend) replaces the state and shows the re-rendered dialog.
- `remember(vararg keys) { suspend }` caches per key list for the lifetime of the store. The cache
  is never cleared, so include every input that the value depends on in the keys.
- The store is never unmounted. It keeps living for as long as its callbacks are referenced.

`paginatedDialog(player, query, itemBuilder = { item -> label to detailDialog }, searchable = true, scope = plugin.scope)`
builds on this. Implement `DialogQuery<PageState, T>` (`PageState(page, limit = 10, search)` in,
`PageResult(items, page, totalPages)` out). There is also `CursorDialogQuery` / `CursorState` / `CursorResult`
for cursor paging. Its button labels are German ("Zurück", "Weiter", "Suchen").

## Resource-pack overlays in dialogs (phone-style screens)

Dialog titles, body messages, and button labels are Adventure components, so they accept custom
fonts and glyphs just like inventory titles:

- Draw a frame or background: a `plainMessage` whose component is a glyph from a pack font
  (`text("\uE200").font(key("roleplay", "phone"))`). Its `ascent`/`height` in the font provider set
  its size and vertical position. Use negative-space shifts (`shift(px)` from the inventory
  framework's `view.util.GlyphShift`, font `surf:menu`) to position it horizontally or overlap it.
- Make icons clickable: use a `multiAction` whose button labels are app-icon glyphs, with
  `width` set to the icon size, `columns` set to the grid width, and a `playerCallback` / `showDialog` per app.
- Keep glyph dimensions in one place (a Kotlin object per pack font), because a wrong advance
  misaligns everything after it. The same rule applies to inventory `ViewContainerComponent`s.

Glyphs go in via Nexo, or via an external pack merged through Nexo's `external_packs`. Ask the
human which, and for any glyph codepoints, before inventing them.

## Gotchas

- `title` is required in `base`, and `yes` and `no` are both required in `confirmation`. A missing one
  fails at build time with `require` messages.
- Default callbacks are single use (see above).
- `response.getX(key)` is nullable, and keys are plain strings. Keep them as constants.
- The `input { }` block belongs to `base { }`. Calling it inside `body { }` still resolves to the
  outer `base` receiver, because there is no DSL marker.
- Width ranges: buttons and text are 1..1024, item bodies 1..256.
- Dialogs replace each other. There is no stack, so to "go back" you show the previous dialog again.
