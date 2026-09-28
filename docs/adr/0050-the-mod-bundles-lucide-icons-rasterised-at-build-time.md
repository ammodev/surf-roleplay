# ADR-0050: The mod bundles Lucide icons rasterised at build time

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

The shadcn components the toolkit adopts use Lucide icons throughout: in buttons, alerts, menus,
inputs and navigation. Minecraft's GUI draws textures, not SVGs, and hand-drawing every needed
icon would slow every screen that needs a new one.

## Decision

A Gradle task downloads a pinned Lucide release and rasterises every icon with Apache Batik into
white 64x64 images packed into one texture atlas, which ships in the mod jar together with the
Lucide ISC licence notice. Batik is a build-time dependency and is not shipped. The mod tints
icons at draw time. Screens reference icons by their Lucide name; an unknown name draws a
placeholder. The atlas is a build output and is not committed.

## Alternatives considered

### Committed PNG files

Its advantage: no build-time rasterisation and no Batik dependency.

It was rejected because it puts about 1,600 binary files into the repository, and updating
Lucide means regenerating and committing all of them.

### A curated subset

Its advantage: a smaller jar and a smaller build step.

It was rejected because every new screen that needs another icon would first need a list edit
and a mod release.

### Resource-pack textures only

Its advantage: no icon pipeline at all; any texture can be an icon.

It was rejected because every icon would have to be drawn and shipped by hand.

## Consequences

### What this gives us

The whole Lucide set is available to every screen by name.

### What this costs

- A build step that downloads and rasterises icons, and a build-time dependency on Batik.
- About 1 to 2 MB more in the mod jar.
- Icon names become part of the screen format: renaming an icon in a newer Lucide release
  breaks screens that use the old name.

### Follow-on work

The Gradle task, the atlas loader and icon drawing in the mod, the icon node, and a licence
notice.

### What this forecloses

Coloured, multi-tone icons from this set; those need their own textures.
