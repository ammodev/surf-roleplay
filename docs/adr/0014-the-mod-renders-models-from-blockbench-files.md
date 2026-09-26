# ADR-0014: The mod renders models from Blockbench files

- **Status:** Accepted
- **Date:** 2026-09-26
- **Supersedes:** none
- **Superseded by:** none

## Context

Vehicles, player attachments (hair, hats, vests, holsters, backpacks), furniture
and custom items all need 3D models. The earlier server-side plan was:

- Nexo for items, furniture and glyphs
- BetterHud for the HUD
- ModelEngine for animated models

With the required client mod (ADR-0012), the mod can render models and the HUD
itself.

## Decision

Nexo, BetterHud and ModelEngine are not used. Models are authored in Blockbench
and exported into the project's own model format. The mod loads and renders that
format, including named bones for animated parts such as wheels, doors and
steering. The mod also draws the HUD.

## Alternatives considered

### Keep Nexo, BetterHud and ModelEngine alongside the mod

Its advantage: proven tools with existing configs and workflows, and less
renderer code to write.

It was rejected because each would duplicate what the mod already controls. Their
server-side rendering (display entities, glyph fonts) is exactly the limitation
the mod exists to remove.

### GLB or OBJ models from general 3D tools

Its advantage: higher-detail models from mainstream tools and artists.

It was rejected because it breaks the blocky visual style, and the team's
builders already work in Blockbench.

## Consequences

### What this gives us

One model pipeline for vehicles, attachments and furniture, animations driven by
the mod, and no licensing or update dependency on three plugins.

### What this costs

A model loader, renderer and animation system in the mod, plus an export step and
asset delivery through the launcher.

### Follow-on work

The model format and exporter, an asset registry synced from the server, and an
in-game model preview for builders.

### What this forecloses

Existing Nexo or ModelEngine assets cannot be used directly. Returning to those
plugins would mean re-authoring models and rebuilding the HUD.
