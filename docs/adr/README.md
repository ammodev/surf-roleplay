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
| [0001](0001-each-identity-owns-a-dedicated-transaction-account.md) | Each identity owns a dedicated transaction account | Accepted | 2026-09-25 |
| [0002](0002-identity-accounts-are-created-on-the-paper-client.md) | Identity accounts are created on the Paper client | Accepted | 2026-09-25 |
| [0003](0003-user-licenses-reference-licenses-by-key.md) | User licenses reference licenses by key | Accepted | 2026-09-25 |
| [0004](0004-a-user-has-at-most-one-identity-per-type.md) | A user has at most one identity per type | Accepted | 2026-09-25 |
| [0005](0005-clients-cache-player-data-only-while-the-player-is-online.md) | Clients cache player data only while the player is online | Accepted | 2026-09-25 |
| [0006](0006-the-active-identity-is-not-persisted.md) | The active identity is not persisted | Accepted | 2026-09-25 |
| [0007](0007-ranks-and-qualifications-are-sealed-types-per-organisation.md) | Ranks and qualifications are sealed types per organisation | Accepted | 2026-09-25 |
| [0008](0008-player-facing-text-is-german.md) | Player-facing text is German | Accepted | 2026-09-25 |
| [0009](0009-license-grants-enforce-requirements-unless-forced.md) | License grants enforce requirements unless forced | Accepted | 2026-09-25 |
| [0010](0010-ranks-and-qualifications-use-english-identifiers.md) | Ranks and qualifications use English identifiers | Accepted | 2026-09-25 |
| [0011](0011-the-license-registry-lives-in-core-client-common.md) | The license registry lives in core-client-common | Accepted | 2026-09-25 |
