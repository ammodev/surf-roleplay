# ADR-0043: Only submitting buttons require valid input

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

A click on a generic screen carries the values of every input of the screen, and the server
validates each against its constraints (ADR-0042). The mod lets the player type intermediate
values that break a constraint, such as a number below its minimum, and marks them as invalid.
With every click validating every input, a form with an empty required field blocks all of its
buttons, including "Zurück" and "Öffnen", and the player gets no feedback.

## Decision

Every button declares whether it submits the screen's input. A submitting button, the default,
is accepted only if every input value passes its constraints. A button that does not submit is
accepted whatever the inputs hold; its handler receives only the values that pass their
constraints, and inputs whose submitted value breaks a constraint keep their last valid value on
the server. The flag travels with the button node to the mod.

## Alternatives considered

### The mod disables every button while an input is invalid

Its advantage: no protocol or API change, and the player sees at once that the form is not
ready.

It was rejected because it also blocks back and cancel buttons, which is the problem being
solved.

### The mod omits invalid values and the server keeps the last valid value

Its advantage: every click goes through with no flag to set.

It was rejected because a submit handler could then act on stale values without knowing that the
player's input was invalid.

## Consequences

### What this gives us

Forms can always be left or navigated, while submits still receive only valid input.

### What this costs

- Every button has one more setting that feature code must choose correctly.
- A new field on the button node.

### Follow-on work

The field on the protocol node and the API element, validation that depends on it, and the
split between valid and invalid values on the server.

### What this forecloses

Nothing permanent: the flag defaults to submitting, so existing buttons keep strict validation.
