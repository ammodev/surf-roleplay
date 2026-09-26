# ADR-0024: The roadmap app uses one shared login token

- **Status:** Accepted
- **Date:** 2026-09-26
- **Supersedes:** none
- **Superseded by:** none

## Context

The roadmap app (ADR-0023) holds internal planning data and must not be public.
It is used by a small team of developers, builders and staff. It needs a login
that is cheap to set up and cheap to operate.

## Decision

The app has one login token, read from the `ROADMAP_TOKEN` environment variable.
Visitors enter it on a login page and receive an httpOnly session cookie, and
middleware rejects every other request without that cookie. Each visitor also
enters a display name, which the browser keeps in local storage and sends with
every change so that the change log names the author. The display name is not
verified.

## Alternatives considered

### Individual user accounts

Its advantage: real authentication per person, revocable access, and trustworthy
authorship in the change log.

It was rejected because account management is out of proportion for a small
internal tool.

### HTTP Basic Auth

Its advantage: no login page and no cookie handling, just the browser's built-in
prompt.

It was rejected in favour of a login page that also asks for the display name.

## Consequences

### What this gives us

Access control with a single secret and no user management.

### What this costs

Everyone shares one secret. Revoking one person means rotating the token for
everyone, and authorship in the change log is self-declared.

### Follow-on work

The token check, the cookie session, the middleware, and token rotation
instructions in the app's documentation.

### What this forecloses

Per-person permissions. Adding them later means introducing accounts and
migrating the change log's author field.
