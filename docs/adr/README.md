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
