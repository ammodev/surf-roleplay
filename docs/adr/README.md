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
