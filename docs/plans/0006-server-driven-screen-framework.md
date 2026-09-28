# Plan 0006: Server-driven screen framework

- **Status:** In progress
- **Date:** 2026-09-28
- **Accepted proposal:** A hybrid server-driven screen framework (generic widget trees and typed screens) with a public server API, an own mod UI toolkit, and roleplay-server scoping of the mod
- **Decision records:** ADR-0032, ADR-0033, ADR-0034, ADR-0035, ADR-0036, ADR-0037, ADR-0038, ADR-0039, ADR-0040 (superseded during step 1 by ADR-0041), ADR-0041

## Goal

A staff member on the dev stack runs `/rpscreen` and the mod opens a generic demo screen. The
screen is laid out by flex containers and holds every widget kind: `label`, `button`,
`text_input`, `number_input`, `checkbox`, `dropdown`, `scroll_list`, `image` and `progress`.
On that screen:
- Buttons run server-side handlers that receive the validated input values.
- The server patches the screen live, and focus, scroll and typed text survive the patch.
- A child screen opens on top, and Escape returns to the parent with its state intact.
- `/rpscreen counter` opens a typed demo counter screen whose state the server updates and
  whose typed actions reach the server.

The server rejects and logs actions for unknown sessions, missing or disabled widgets, and
values that break an input's constraints. It also rate-limits actions per player. The mod
enforces the same constraints while the player types.

The mod keeps sending its hello to every server. It enables screens only after the play-phase
`welcome` packet, and resets on disconnect.

The API for generic screens and for closing screens is public in `surf-roleplay-api`
(model and DSL in `surf-roleplay-api-client-common`, the player-facing service in
`surf-roleplay-api-client-paper`), and no protocol class appears in it. Typed screens are
opened through feature-specific API added with each feature. The only typed screen in this
plan is the debug counter, which is opened only by the Paper plugin.

`PROTOCOL_VERSION` is 2. `./gradlew build` passes, and every new function, class and public
property has a KDoc comment.

## Out of scope

- Real feature screens: inventory, phone, ID card, tablet and the settings screen.
- client-mod tasks 5 to 7: keybind settings, hiding F3 coordinates and the Tab list, and
  disabling vanilla crafting. Only the roleplay-active state that they will consult is built.
- Tooltips, drag and drop, animations, sounds, and keyboard navigation beyond focus and text
  entry.
- Configurable themes or a resource-pack-driven look.
- Public API for typed screens.
- Moving existing UI (surf-api dialogs and inventories) onto the framework.
- Automated tests against a running Minecraft client or server. The in-game checks are manual.

## Steps

### Step 1: Spike the configuration-phase channel announcement

**Does:** Adds temporary logging to the mod that records, during the configuration phase on
the dev stack (Paper behind Velocity):
- whether `ClientConfigurationNetworking.canSend` is true for the hello payload type
- at which point that becomes true: at `ClientConfigurationConnectionEvents.START` or later,
  after the server's channel registration arrives

The finding is reported in the chat. Nothing from this step is committed.

**Ends in:** A named, observed point in the configuration phase at which the mod knows that
the server receives `roleplay:hello`, or the observation that it never learns this.

**Verified by:** The dev client log from a join through `localhost:25565`, with the Paper and
Velocity logs of the same join.

**Pushes:** no

### Step 2: Scope the mod to the roleplay server

**Does:**
- **Protocol:** adds the clientbound play packet `welcome` and bumps `PROTOCOL_VERSION` to 2.
  The later packet changes in this plan belong to the same unreleased version 2.
- **Fabric:** keeps sending the hello at the start of every configuration phase (step 1 found
  that the server never announces the channel, and ADR-0041 records the outcome), and adds a
  `RoleplayServerState` that becomes active when `welcome` arrives and resets on disconnect.
- **Paper:** sends `welcome` to every player who joins after a passed handshake.

**Ends in:** On the dev stack, the dev client log shows the roleplay state turning active
after join. It shows the state resetting after disconnect.

**Verified by:** `./gradlew build` passes, the protocol tests cover the `welcome` round trip,
and a live join and leave on the dev stack show the log lines above.

**Pushes:** no

### Step 3: Add the screen model and packets to the protocol

**Does:** Adds to `surf-roleplay-protocol`, with `@ProtoNumber` on every property:
- the node model: `row` and `column` containers (gap, padding, alignment, fixed or grow
  sizing per child), and every widget kind from the goal, each with a node id. Input widgets
  carry their constraints. Texts are component JSON strings.
- patch operations: replace, insert, remove, set text, set value, set enabled.
- `ScreenType(key, stateSerializer, actionSerializer)` and a `ScreenTypes` list with the
  debug counter type.
- the packets `screen_open`, `screen_patch`, `screen_typed_update` and `screen_close`
  (clientbound), and `screen_widget_action`, `screen_typed_action` and `screen_closed`
  (serverbound), all play phase only.
- `docs/protocol-versioning.md` gains the rule that a typed screen's state and action schemas
  follow the same bump rules as packets.

**Ends in:** The protocol module holds the full screen wire format.

**Verified by:** `./gradlew :surf-roleplay-protocol:test` passes, with round-trip tests for
every packet, a nested tree with every widget kind, every patch operation, and the counter
state and action. A further test checks that screen type keys are unique.

**Pushes:** no

### Step 4: Build the flex layout engine

**Does:** Adds a pure-Kotlin flex layout engine to the mod, with no Minecraft imports. It
lays out rows and columns with gap, padding, alignment, and fixed, content or grow sizes,
given the available width and height and the measured sizes of the leaves.

**Ends in:** A layout engine that computes a position and size for every node of a tree.

**Verified by:** `./gradlew :surf-roleplay-fabric:test` passes, with unit tests for nested
rows and columns, grow distribution, alignment, padding and gap, and trees wider than the
available space.

**Pushes:** no

### Step 5: Build the toolkit widgets and the theme

**Does:** Adds the mod's component tree on top of vanilla rendering:
- one component per widget kind
- focus handling and text entry, with constraint enforcement while typing
- scrolling in `scroll_list`
- a roleplay theme as constants
- component JSON parsing with a visible fallback for text that fails to parse
- a `RoleplayScreenHost`, a vanilla `Screen` that lays out and renders one component tree

It also adds a dev-only client command that opens a hardcoded tree, to check the rendering
without the server.

**Ends in:** The dev client shows a hardcoded tree with every widget kind, laid out correctly
at two different GUI scales. Text inputs refuse input beyond their constraints.

**Verified by:** `./gradlew :surf-roleplay-fabric:build` passes, and the tree is inspected in
the dev client at GUI scale 2 and 4.

**Pushes:** no

### Step 6: Add screen sessions to the mod

**Does:** Adds the client-side screen stack. It handles `screen_open`, `screen_patch`,
`screen_typed_update` and `screen_close`, and only while `RoleplayServerState` is active:
- Buttons send `screen_widget_action` with the current values of the screen's inputs.
- Escape sends `screen_closed` and shows the parent, unless the screen is not closable.
- Patches keep focus, scroll and typed text on the widgets that survive.
- A typed screen registry maps screen type keys to coded screens and holds the debug counter
  screen, which sends `screen_typed_action`.
- Closing a screen also closes the screens above it.
- Everything is cleared on disconnect.

The dev-only client command from step 5 is removed.

**Ends in:** The mod reacts to every screen packet and sends every serverbound screen packet.

**Verified by:** `./gradlew :surf-roleplay-fabric:test` passes, with unit tests for stack
behaviour, including close propagation and parent restore, and for patch application on the
component tree.

**Pushes:** no

### Step 7: Add the public screen API

**Does:**
- **`surf-roleplay-api-client-common`:** adds the generic screen model (Adventure components
  for texts), a Kotlin builder DSL for trees with action handlers bound to buttons, and
  handle types for open screens that expose patch operations.
- **`surf-roleplay-api-client-paper`:** adds a `ScreenService` to open a screen for a player,
  optionally on top of a parent, and to close one screen or the whole stack.

No protocol class appears in either module.

**Ends in:** The API modules compile with the full generic screen API, documented.

**Verified by:** `./gradlew build` passes, with unit tests for the DSL and for the tree
operations the handles expose.

**Pushes:** no

### Step 8: Implement screens on Paper

**Does:** Implements `ScreenService` in `surf-roleplay-paper`:
- the per-player stack and the server-held tree of every session
- mapping between API trees and protocol nodes, with texts serialized as component JSON
- patches, applied to the server-held tree and sent in the same operation
- action validation: session in stack, widget present, enabled and clickable, and values
  within constraints, with rejected actions logged
- a per-player action rate limit of 20 actions per second, read from the plugin config
- routing of typed actions to the handler of the typed session
- cleanup on quit

**Ends in:** Paper plugins can open, patch and close generic screens, and the server enforces
every rule of ADR-0037.

**Verified by:** `./gradlew :surf-roleplay-paper:test` passes, with unit tests for mapping,
patch application, every validation rule, the rate limit, and stack cleanup.

**Pushes:** no

### Step 9: Add the debug command and the typed counter

**Does:** Adds the command `/rpscreen`, which requires the permission
`surf.roleplay.command.rpscreen`:
- `/rpscreen` opens the generic demo screen: every widget kind, a button that patches the
  screen, a submit button that validates and echoes the input values in chat, a live
  progress bar, and a button that opens a child screen.
- `/rpscreen counter` opens the typed counter. Its typed actions increment and decrement a
  server-side value, and the server answers with `screen_typed_update`.

All player-visible text is German.

**Ends in:** The dev stack serves both demo screens to a player with the permission.

**Verified by:** `./gradlew build` passes, and both commands open their screens in the dev
client on the dev stack.

**Pushes:** no

### Step 10: Verification

**Does:** Checks the whole goal end to end on the dev stack, after `./gradlew build` and
`docker compose restart paper velocity ms-roleplay`.

**Ends in:** Every statement in the goal has been observed to hold.

**Verified by:**
- `./gradlew build` passes.
- Every new declaration has a KDoc comment with no ADR, plan or history reference.
- In the dev client:
  - `/rpscreen` shows every widget kind laid out correctly at GUI scale 2 and 4.
  - Submit echoes the entered values.
  - The patch button changes the screen without losing typed text, focus or scroll position.
  - The child screen opens, and Escape returns to the parent with its state intact.
  - The progress bar updates live.
  - `/rpscreen counter` counts up and down through typed actions.
- A player without the permission cannot run `/rpscreen`.
- Forged actions are sent with a temporary debug hook in the dev client, which is not
  committed. The Paper log shows each one rejected:
  - an unknown session
  - a disabled button
  - an out-of-range number
  - a flood beyond the rate limit
- The dev client log shows that the hello and the roleplay state behave as in step 2.
- The roadmap shows client-mod task 8 as Done, and the system as Review.

**Pushes:** no

## Push points

none

## Risk

Step 1 is the most likely to go wrong. Paper drops clientbound payloads in the configuration
phase, and the server's channel registration is itself a clientbound payload. It may also not
pass through Velocity in that phase. If the mod can never tell that the server receives
`roleplay:hello`, the agent stops before step 2:
- It sets the client-mod system to Blocked and adds a roadmap question.
- It asks the human how to gate the hello. That may supersede ADR-0040.

It does not fall back to an ungated hello or an address list on its own.

## If reality contradicts this plan

The agent stops and asks. It does not silently rewrite this plan to match what actually
happened, and it does not continue past a step whose stated end state was not reached.
