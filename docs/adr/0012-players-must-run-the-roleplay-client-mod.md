# ADR-0012: Players must run the roleplay client mod

- **Status:** Accepted
- **Date:** 2026-09-26
- **Supersedes:** none
- **Superseded by:** none

## Context

The gamemode aims at GTA RP and Altis Life style play. That means drivable multi-seat
vehicles, a phone, an Einsatzleiter tablet, a minimap with GPS, target-eye
interaction, layered character appearance with 3D attachments, injury and
intoxication screen effects, and custom animations. A vanilla client can only
show server-side approximations of these:

- display entities for vehicles, with input lag
- resource-pack glyphs drawn into inventory titles and dialogs for UI
- 64x64 signed skins that an external service has to bake and sign

## Decision

Every player connects with the roleplay client mod, a Fabric mod built in this
repository. The server refuses clients that do not complete the mod handshake.
The mod implements all player-facing UI, vehicle rendering and input, player
appearance, furniture models, animations and screen effects.

## Alternatives considered

### Paper and resource pack only

Its advantage: players join with an unmodified client, there is nothing to
install or keep updated, and the whole gamemode stays in one runtime.

It was rejected because vehicles, the phone, the tablet, the minimap and layered
appearance would all be approximations with visible limits. Each would be a
fight against the client instead of something built for it.

### Optional mod with a vanilla fallback

Its advantage: players without the mod can still join, while modded players get
the full experience.

It was rejected because every feature would have to be built twice, once as a
mod screen and once as a glyph or dialog fallback, and the two would drift apart.

## Consequences

### What this gives us

Native screens, smooth vehicles, a real minimap, arbitrary player textures and
models, custom keybinds, and no dependency on skin-signing services.

### What this costs

- A second codebase (the Fabric client) that must follow every Minecraft update.
- A launcher to distribute it.
- A support burden for installation problems.
- A higher barrier to entry for new players.

### Follow-on work

A shared protocol between server and mod, a launcher, a mod whitelist, and a
build and release pipeline for the mod and its assets.

### What this forecloses

Players can no longer join with a plain client, and cross-play with other
launchers or with Bedrock is out. Reverting to a vanilla client would mean
rebuilding all UI, vehicles and appearance server-side.
