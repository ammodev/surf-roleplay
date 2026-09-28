# ADR-0047: Screens use browser-like keyboard navigation

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

The mod's screens are forms and menus (ADR-0032), but so far only text entry works from the
keyboard; every other widget needs the mouse. Players expect to move through a form with Tab as
in a web browser.

## Decision

Tab and Shift+Tab move the keyboard focus through every enabled interactive widget of the shown
screen in tree order, wrapping at the ends. Nothing is focused until the first Tab or click, and
clicking an interactive widget also focuses it. The focused widget shows a focus ring. Enter or
Space activates a focused button, Space toggles a focused checkbox, and Enter, Space or Down
opens a focused dropdown, whose options are chosen with Up, Down and Enter. Escape closes an
open dropdown list first and otherwise keeps its meaning of closing the screen. A focused widget
outside the visible area is scrolled into view.

## Alternatives considered

### Tab between text and number inputs only

Its advantage: much less to build, and it covers the most common case of filling in text fields.

It was rejected because buttons, checkboxes and dropdowns would still need the mouse, so a form
could not be completed from the keyboard.

### Enter in a text field submits the form

Its advantage: fast form entry, as on web pages.

It was not chosen now because a screen has no notion of a default button yet, and it can be
added later without changing the rules above.

## Consequences

### What this gives us

Every screen can be used with the keyboard alone, with focus behaviour players know from web
forms.

### What this costs

- Every interactive widget needs focus and key handling.
- Future widgets must define how they behave when focused.

### Follow-on work

Focus traversal and a focus ring in the toolkit, key handling for each interactive widget, and
scrolling focused widgets into view.

### What this forecloses

Using Tab, Enter or Space for other purposes inside screens.
