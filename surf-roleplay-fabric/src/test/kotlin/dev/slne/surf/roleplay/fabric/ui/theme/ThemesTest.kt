package dev.slne.surf.roleplay.fabric.ui.theme

import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests for [Themes] and [ThemeColors].
 */
class ThemesTest {

    /**
     * Verifies that every theme exists in both variants and that the variants differ.
     */
    @Test
    fun `every theme has both variants`() {
        for (name in listOf("default", "sar", "police")) {
            val dark = Themes.resolve(name, ThemeVariant.DARK)
            val light = Themes.resolve(name, ThemeVariant.LIGHT)
            assertNotEquals(dark.background, light.background, name)
            assertNotEquals(dark.foreground, light.foreground, name)
        }
    }

    /**
     * Verifies that an unknown theme falls back to the default theme in the requested variant.
     */
    @Test
    fun `unknown themes fall back to default`() {
        assertSame(Themes.resolve("default", ThemeVariant.LIGHT), Themes.resolve("feuerwehr", ThemeVariant.LIGHT))
        assertSame(Themes.resolve("default", ThemeVariant.DARK), Themes.resolve("", ThemeVariant.DARK))
    }

    /**
     * Verifies that the organisation themes differ from the default theme in their primary
     * colour, and from each other.
     */
    @Test
    fun `organisation themes have their own primary`() {
        for (variant in ThemeVariant.entries) {
            val primaries = listOf("default", "sar", "police").map { Themes.resolve(it, variant).primary }
            assertEquals(3, primaries.toSet().size, variant.name)
        }
    }

    /**
     * Verifies that every token of every palette is opaque except the translucent border and
     * input tokens of dark variants, and that the radius is positive.
     */
    @Test
    fun `palettes define every token`() {
        for (tokens in Themes.all) {
            val opaque = listOf(
                tokens.background, tokens.foreground, tokens.card, tokens.cardForeground, tokens.popover,
                tokens.popoverForeground, tokens.primary, tokens.primaryForeground, tokens.secondary,
                tokens.secondaryForeground, tokens.muted, tokens.mutedForeground, tokens.accent,
                tokens.accentForeground, tokens.destructive, tokens.ring,
            )
            opaque.forEach { assertEquals(0xFF, it ushr 24, "token of $tokens") }
            assertTrue(tokens.border ushr 24 > 0 && tokens.input ushr 24 > 0)
            assertTrue(tokens.radius > 0)
        }
    }

    /**
     * Verifies alpha replacement and blending.
     */
    @Test
    fun `colour helpers blend and set alpha`() {
        assertEquals(0x80112233.toInt(), ThemeColors.withAlpha(0xFF112233.toInt(), 0.5f))
        assertEquals(0xFF808080.toInt(), ThemeColors.blend(0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 0.5f))
    }
}
