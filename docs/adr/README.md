# Architecture Decision Records

Every decision that required a human is recorded here. A decision required a human
when it constrained future work, was expensive to reverse, traded one desirable
property against another, affected the security model, changed a public API or a
wire or on-disk format, or added a runtime dependency.

Records are numbered sequentially, zero-padded to four digits, and **immutable
once accepted**. To change an accepted decision, add a new record and set the old
one's `Superseded by` field. The superseded record's body is never edited.

Create the next record with `/surf:new-adr`.

| ADR | Title | Status | Date |
| --- | ----- | ------ | ---- |
| [0003](0003-user-licenses-reference-licenses-by-key.md) | User licenses reference licenses by key | Accepted | 2026-09-25 |
| [0004](0004-a-user-has-at-most-one-identity-per-type.md) | A user has at most one identity per type | Accepted | 2026-09-25 |
| [0006](0006-the-active-identity-is-not-persisted.md) | The active identity is not persisted | Accepted | 2026-09-25 |
| [0008](0008-player-facing-text-is-german.md) | Player-facing text is German | Accepted | 2026-09-25 |
| [0009](0009-license-grants-enforce-requirements-unless-forced.md) | License grants enforce requirements unless forced | Accepted | 2026-09-25 |
| [0012](0012-players-must-run-the-roleplay-client-mod.md) | Players must run the roleplay client mod | Accepted | 2026-09-26 |
| [0013](0013-server-and-mod-talk-over-typed-payload-channels.md) | Server and mod talk over typed payload channels | Accepted | 2026-09-26 |
| [0014](0014-the-mod-renders-models-from-blockbench-files.md) | The mod renders models from Blockbench files | Accepted | 2026-09-26 |
| [0015](0015-game-content-is-defined-in-config-or-the-database.md) | Game content is defined in config or the database | Accepted | 2026-09-26 |
| [0016](0016-bank-accounts-are-opened-at-a-bank.md) | Bank accounts are opened at a bank | Accepted | 2026-09-26 |
| [0017](0017-the-microservice-owns-all-persistent-game-data.md) | The microservice owns all persistent game data | Accepted | 2026-09-26 |
| [0018](0018-the-dashboard-writes-the-database-and-publishes-redis-events.md) | The dashboard writes the database and publishes Redis events | Accepted | 2026-09-26 |
| [0019](0019-an-own-zone-system-replaces-worldguard.md) | An own zone system replaces WorldGuard | Accepted | 2026-09-26 |
| [0020](0020-the-server-is-authoritative.md) | The server is authoritative | Accepted | 2026-09-26 |
| [0021](0021-vehicles-use-client-prediction-with-server-reconciliation.md) | Vehicles use client prediction with server reconciliation | Accepted | 2026-09-26 |
| [0022](0022-the-launcher-is-an-electron-app.md) | The launcher is an Electron app | Accepted | 2026-09-26 |
| [0023](0023-the-roadmap-app-is-a-next-js-app-with-sqlite.md) | The roadmap app is a Next.js app with SQLite | Accepted | 2026-09-26 |
| [0024](0024-the-roadmap-app-uses-one-shared-login-token.md) | The roadmap app uses one shared login token | Accepted | 2026-09-26 |
| [0025](0025-agents-use-the-roadmap-app-through-a-remote-mcp-endpoint.md) | Agents use the roadmap app through a remote MCP endpoint | Accepted | 2026-09-27 |
| [0026](0026-agents-authenticate-with-the-shared-login-token.md) | Agents authenticate with the shared login token | Accepted | 2026-09-27 |
| [0027](0027-packets-are-encoded-with-kotlinx-serialization-protobuf.md) | Packets are encoded with kotlinx.serialization ProtoBuf | Accepted | 2026-09-27 |
| [0028](0028-every-packet-has-its-own-payload-channel.md) | Every packet has its own payload channel | Accepted | 2026-09-27 |
| [0029](0029-the-mod-handshake-runs-in-the-configuration-phase.md) | The mod handshake runs in the configuration phase | Accepted | 2026-09-27 |
| [0030](0030-the-client-mod-depends-on-fabric-language-kotlin.md) | The client mod depends on Fabric Language Kotlin | Accepted | 2026-09-27 |
| [0031](0031-the-local-dev-stack-runs-fully-in-docker-compose.md) | The local dev stack runs fully in Docker Compose | Accepted | 2026-09-28 |
| [0032](0032-server-driven-screens-combine-widget-trees-and-typed-screens.md) | Server-driven screens combine widget trees and typed screens | Accepted | 2026-09-28 |
| [0033](0033-generic-screens-are-trees-of-flex-containers-and-fixed-widgets.md) | Generic screens are trees of flex containers and fixed widgets | Superseded by ADR-0051 | 2026-09-28 |
| [0034](0034-screen-texts-travel-as-text-component-json.md) | Screen texts travel as text component JSON | Accepted | 2026-09-28 |
| [0035](0035-server-driven-screens-stack-per-player.md) | Server-driven screens stack per player | Superseded by ADR-0048 | 2026-09-28 |
| [0036](0036-open-screens-change-through-patches-by-widget-id.md) | Open screens change through patches by widget id | Accepted | 2026-09-28 |
| [0037](0037-screen-actions-are-validated-against-the-server-held-screen.md) | Screen actions are validated against the server-held screen | Superseded by ADR-0042 | 2026-09-28 |
| [0038](0038-the-screen-api-is-public-in-surf-roleplay-api.md) | The screen API is public in surf-roleplay-api | Accepted | 2026-09-28 |
| [0039](0039-the-mod-ui-is-built-on-an-own-toolkit.md) | The mod UI is built on an own toolkit | Accepted | 2026-09-28 |
| [0040](0040-the-mod-is-active-only-on-servers-that-announce-the-roleplay-protocol.md) | The mod is active only on servers that announce the roleplay protocol | Superseded by ADR-0041 | 2026-09-28 |
| [0041](0041-the-mod-is-active-only-after-the-roleplay-server-welcomes-it.md) | The mod is active only after the roleplay server welcomes it | Accepted | 2026-09-28 |
| [0042](0042-screen-actions-are-accepted-only-for-the-top-screen.md) | Screen actions are accepted only for the top screen | Accepted | 2026-09-28 |
| [0043](0043-only-submitting-buttons-require-valid-input.md) | Only submitting buttons require valid input | Accepted | 2026-09-28 |
| [0044](0044-the-screen-api-is-called-on-the-players-region-thread.md) | The screen API is called on the player's region thread | Accepted | 2026-09-28 |
| [0045](0045-the-mod-drops-screen-opens-for-a-parent-it-no-longer-has.md) | The mod drops screen opens for a parent it no longer has | Accepted | 2026-09-28 |
| [0046](0046-screens-taller-than-the-window-scroll-inside-their-panel.md) | Screens taller than the window scroll inside their panel | Accepted | 2026-09-28 |
| [0047](0047-screens-use-browser-like-keyboard-navigation.md) | Screens use browser-like keyboard navigation | Accepted | 2026-09-28 |
| [0048](0048-screens-are-presented-as-full-screen-dialog-or-sheet.md) | Screens are presented as full screen, dialog or sheet | Accepted | 2026-09-28 |
| [0049](0049-the-server-picks-a-named-theme-and-variant-per-screen.md) | The server picks a named theme and variant per screen | Accepted | 2026-09-28 |
| [0050](0050-the-mod-bundles-lucide-icons-rasterised-at-build-time.md) | The mod bundles Lucide icons rasterised at build time | Accepted | 2026-09-28 |
| [0051](0051-the-screen-framework-covers-the-shadcn-component-registry.md) | The screen framework covers the shadcn component registry | Accepted | 2026-09-28 |
| [0052](0052-screen-inputs-submit-with-actions-and-can-send-change-events.md) | Screen inputs submit with actions and can send change events | Accepted | 2026-09-28 |
| [0053](0053-dates-are-shown-in-german-and-sent-as-iso-dates.md) | Dates are shown in German and sent as ISO dates | Accepted | 2026-09-28 |
| [0054](0054-comboboxes-filter-on-the-client-with-an-optional-search-event.md) | Comboboxes filter on the client with an optional search event | Accepted | 2026-09-28 |
| [0055](0055-screen-texts-wrap-to-the-width-their-container-gives-them.md) | Screen texts wrap to the width their container gives them | Accepted | 2026-09-28 |
| [0056](0056-avatars-show-player-heads-or-resource-pack-textures.md) | Avatars show player heads or resource-pack textures | Accepted | 2026-09-28 |
