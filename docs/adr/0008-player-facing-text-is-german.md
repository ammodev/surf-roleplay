# ADR-0008: Player-facing text is German

- **Status:** Accepted
- **Date:** 2026-09-25
- **Supersedes:** none
- **Superseded by:** none

## Context

The repository rule is that all output is English, covering code, identifiers,
comments, documentation, and commit messages. The server's players are
German-speaking, and the existing display names and requirement messages are
German. The rule does not state whether it covers text shown to players.

## Decision

Text shown to players in-game (display names of licenses, ranks,
qualifications, and messages) is German. Code, identifiers, comments,
documentation, and commits stay English.

## Alternatives considered

### Translate player-facing text to English

Its advantage: a single language across the whole repository, with nothing left
to interpret about the rule.

It was rejected because the players are German-speaking and roleplay content
such as German police ranks is inherently German.

## Consequences

### What this gives us

Natural in-game text for the actual audience.

### What this costs

The source code mixes two languages: English code with German string literals.

### Follow-on work

None beyond writing new display names in German.

### What this forecloses

Serving non-German players without later introducing translations. Adding
localisation afterwards means extracting every hard-coded string.
