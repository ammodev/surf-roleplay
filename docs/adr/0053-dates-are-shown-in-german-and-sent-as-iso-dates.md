# ADR-0053: Dates are shown in German and sent as ISO dates

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

The calendar component and date inputs show months, weekdays and dates to players, whose text is
German (ADR-0008), and send the chosen dates to the server. The mod and the server need one
unambiguous format for dates on the wire.

## Decision

The mod shows dates in German: German month and weekday names, weeks starting on Monday, and dates
written as `TT.MM.JJJJ`. On the wire, every date is an ISO 8601 calendar date, `yyyy-MM-dd`,
without a time or time zone. Several dates are sent as a comma-separated list, and a range as
two dates joined by `/`.

## Alternatives considered

### The server sends month and weekday labels

Its advantage: the server fully controls wording, and other languages would need no mod change.

It was rejected because every calendar would carry the same labels again, and the gamemode's
player-facing language is fixed to German.

## Consequences

### What this gives us

Dates that read naturally for players and parse without ambiguity on the server.

### What this costs

German wording is built into the mod; another language would need a mod release.

### Follow-on work

German date formatting in the mod, and ISO parsing and validation on the server.

### What this forecloses

Times of day and time zones in calendar values; those need their own inputs.
