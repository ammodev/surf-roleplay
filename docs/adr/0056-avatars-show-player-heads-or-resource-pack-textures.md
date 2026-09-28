# ADR-0056: Avatars show player heads or resource-pack textures

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

The avatar component (ADR-0051) shows a small round picture of a person, with a fallback text
while the picture is missing. In the browser the picture is an arbitrary image URL. In the game,
the people shown are mostly players and characters, and the mod can already draw player skins and
resource-pack textures.

## Decision

An avatar's picture is either the face of a Minecraft player's skin, named by the player's UUID,
or a texture from a resource pack, named by its identifier. The mod never downloads an image from
a URL the server sends. While a skin is loading, or if it cannot be loaded, the avatar shows its
fallback text.

## Alternatives considered

### Remote image URLs

Its advantage: any picture could be shown, such as uploaded character portraits, without a
resource pack release.

It was rejected because every avatar would make the client connect to an address chosen by the
server, which leaks the player's IP address to third parties, and the mod would need an image
decoder and a cache for untrusted data.

### Player heads only

Its advantage: one source, and no dependency on the resource pack.

It was rejected because characters, organisations and non-player characters need pictures that
are not player skins.

## Consequences

### What this gives us

Avatars for players and characters with no new network traffic besides Minecraft's own skin
loading, and no untrusted image data in the mod.

### What this costs

- Pictures that are neither skins nor pack textures need a resource pack release.
- Skins of players who are not online are fetched through Minecraft's profile service, which can
  be slow or rate-limited, so the fallback text shows longer.

### Follow-on work

Skin lookup by UUID with caching in the mod, and an avatar source type in the protocol and the
server API.

### What this forecloses

Remote images in avatars. Allowing them later needs a new ADR, an image decoder and a privacy
review.
