package dev.slne.surf.roleplay.paper.screen.debug

import dev.slne.surf.roleplay.api.client.common.screen.dsl.Alert
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AlertDescription
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AlertTitle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AspectRatio
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Avatar
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AvatarGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AvatarGroupCount
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Badge
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Blockquote
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Card
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CardAction
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CardContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CardDescription
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CardFooter
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CardHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CardTitle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Empty
import dev.slne.surf.roleplay.api.client.common.screen.dsl.EmptyContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.EmptyDescription
import dev.slne.surf.roleplay.api.client.common.screen.dsl.EmptyHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.EmptyMedia
import dev.slne.surf.roleplay.api.client.common.screen.dsl.EmptyTitle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.H1
import dev.slne.surf.roleplay.api.client.common.screen.dsl.H2
import dev.slne.surf.roleplay.api.client.common.screen.dsl.H3
import dev.slne.surf.roleplay.api.client.common.screen.dsl.H4
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Image
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InlineCode
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Item
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ItemActions
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ItemContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ItemDescription
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ItemFooter
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ItemGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ItemHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ItemMedia
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ItemSeparator
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ItemTitle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Kbd
import dev.slne.surf.roleplay.api.client.common.screen.dsl.KbdGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Large
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Lead
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Muted
import dev.slne.surf.roleplay.api.client.common.screen.dsl.P
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Progress
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Row
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Select
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Separator
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Skeleton
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Small
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Spinner
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TypographyList
import dev.slne.surf.roleplay.api.client.common.screen.AlertVariant
import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.AvatarSize
import dev.slne.surf.roleplay.api.client.common.screen.AvatarSource
import dev.slne.surf.roleplay.api.client.common.screen.BadgeVariant
import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.ButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
import dev.slne.surf.roleplay.api.client.common.screen.EmptyMediaVariant
import dev.slne.surf.roleplay.api.client.common.screen.IconTint
import dev.slne.surf.roleplay.api.client.common.screen.ItemMediaVariant
import dev.slne.surf.roleplay.api.client.common.screen.ItemSize
import dev.slne.surf.roleplay.api.client.common.screen.ItemVariant
import dev.slne.surf.roleplay.api.client.common.screen.Orientation
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.ScreenThemes
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.api.client.common.screen.TextKind
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Screen
import dev.slne.surf.roleplay.api.client.paper.screen.ScreenService
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import java.util.UUID

/**
 * The page of the screen debug command that shows every display component in every variant and
 * size. Clicks are reported in chat.
 */
object DisplayDemo {

    /**
     * Opens the page.
     *
     * @param player the player, whose face the player avatar shows
     * @param theme the name of the theme to draw the page with
     * @param variant the light or dark variant of the theme
     */
    fun open(player: Player, theme: String = ScreenThemes.DEFAULT, variant: ScreenVariant = ScreenVariant.DARK) {
        ScreenService.open(player, definition(player.uniqueId, player::sendMessage, { chosenTheme, chosenVariant -> open(player, chosenTheme, chosenVariant) }, theme, variant))
    }

    /**
     * Builds the page.
     *
     * @param playerId the UUID of the player whose face the player avatar shows
     * @param report sends a report of a click to the player
     * @param reopen opens the page again in another theme and variant
     * @param theme the name of the theme
     * @param variant the variant of the theme
     * @return the page
     */
    fun definition(playerId: UUID, report: (Component) -> Unit, reopen: (String, ScreenVariant) -> Unit, theme: String, variant: ScreenVariant): ScreenDefinition {
        val clicked = ButtonHandler { click -> report(Component.text("${click.buttonId} geklickt", NamedTextColor.GREEN)) }
        return Screen(Component.text("Anzeige"), theme = theme, variant = variant) {
            Column(width = ElementSize.fixed(380), gap = 12, crossAlign = Alignment.STRETCH, id = "root") {
                Row(gap = 4, crossAlign = Alignment.CENTER, id = "theme_row") {
                    Select(InputsDemo.THEMES, selected = theme, width = ElementSize.grow(), id = "theme")
                    Select(InputsDemo.VARIANTS, selected = variant.name, width = ElementSize.fixed(90), id = "variant")
                    Button(Component.text("Anwenden"), submitsInput = false, icon = "palette", variant = ButtonVariant.OUTLINE, id = "apply_theme") { click ->
                        reopen(click.values.selected("theme") ?: ScreenThemes.DEFAULT, click.values.selected("variant")?.let(ScreenVariant::valueOf) ?: ScreenVariant.DARK)
                    }
                }
                typography()
                alerts()
                marks()
                loading()
                avatars(playerId)
                cards(clicked)
                empties(clicked)
                items(clicked)
            }
        }
    }

    /**
     * Adds a section with a heading.
     *
     * @param id the id of the section
     * @param title the heading
     * @param content the builder of the section's content
     */
    private fun ComponentScope.section(id: String, title: String, content: ComponentScope.() -> Unit) {
        Column(gap = 6, crossAlign = Alignment.STRETCH, id = id) {
            H3(Component.text(title), id = "${id}_title")
            content()
        }
    }

    /**
     * Adds every typography style and both lists.
     */
    private fun ComponentScope.typography() = section("typography", "Typografie") {
        H1(Component.text("Überschrift 1"), id = "h1")
        H2(Component.text("Überschrift 2"), id = "h2")
        H3(Component.text("Überschrift 3"), id = "h3")
        H4(Component.text("Überschrift 4"), id = "h4")
        Lead(Component.text("Ein einleitender Satz, der etwas größer und gedämpft erscheint."), id = "lead")
        P(Component.text(PARAGRAPH), id = "p")
        Large(Component.text("Großer Text"), id = "large")
        Small(Component.text("Kleiner Text"), id = "small")
        Muted(Component.text("Gedämpfter Text für Nebensächliches."), id = "muted")
        Blockquote(Component.text("„In dieser Stadt gibt es keine Geheimnisse, nur Akten, die noch niemand gelesen hat.“"), id = "blockquote")
        Row(gap = 4, crossAlign = Alignment.CENTER, id = "code_row") {
            P(Component.text("Befehl:"), id = "code_before")
            InlineCode(Component.text("/rpscreen display"), id = "code")
        }
        P(Component.text("Dieser lange Text ist auf eine Zeile begrenzt und endet deshalb mit Auslassungspunkten, sobald er nicht mehr passt."), maxLines = 1, id = "clamped")
        TypographyList(listOf(Component.text("Führerschein beantragen"), Component.text("Fahrzeug anmelden, sobald der Führerschein ausgestellt wurde und die Versicherung bestätigt ist"), Component.text("Termin wahrnehmen")), id = "bullets")
        TypographyList(listOf(Component.text("Formular ausfüllen"), Component.text("Unterschreiben"), Component.text("Abgeben")), ordered = true, id = "numbers")
    }

    /**
     * Adds alerts in both variants, with and without an icon.
     */
    private fun ComponentScope.alerts() = section("alerts", "Hinweise") {
        Alert(icon = "circle-check", id = "alert_default") {
            AlertTitle(Component.text("Erfolgreich gespeichert"), id = "alert_default_title")
            AlertDescription(Component.text("Deine Änderungen wurden übernommen und sind ab sofort für alle Beamten sichtbar."), id = "alert_default_description")
        }
        Alert(AlertVariant.DESTRUCTIVE, icon = "circle-alert", id = "alert_destructive") {
            AlertTitle(Component.text("Zahlung fehlgeschlagen"), id = "alert_destructive_title")
            AlertDescription(Component.text("Das Konto ist nicht gedeckt. Bitte prüfe den Kontostand und versuche es erneut."), id = "alert_destructive_description")
        }
        Alert(id = "alert_plain") {
            AlertTitle(Component.text("Ein Hinweis ohne Symbol"), id = "alert_plain_title")
        }
    }

    /**
     * Adds badges, keys and separators.
     */
    private fun ComponentScope.marks() = section("marks", "Abzeichen, Tasten und Trenner") {
        Row(gap = 4, crossAlign = Alignment.CENTER, id = "badges") {
            BadgeVariant.entries.forEach { variant ->
                Badge(Component.text(variant.name.lowercase().replaceFirstChar(Char::uppercase)), variant = variant, id = "badge_${variant.name.lowercase()}")
            }
        }
        Row(gap = 4, crossAlign = Alignment.CENTER, id = "badges_icons") {
            Badge(Component.text("Geprüft"), icon = "badge-check", variant = BadgeVariant.SECONDARY, id = "badge_verified")
            Badge(Component.text("8"), variant = BadgeVariant.DESTRUCTIVE, id = "badge_count")
            Badge(Component.text("Im Dienst"), icon = "radio", variant = BadgeVariant.OUTLINE, id = "badge_online")
        }
        Row(gap = 6, crossAlign = Alignment.CENTER, id = "keys") {
            P(Component.text("Suche öffnen:"), id = "keys_label")
            KbdGroup(id = "keys_search") {
                Kbd(Component.text("Strg"), id = "key_ctrl")
                P(Component.text("+"), id = "key_plus")
                Kbd(Component.text("K"), id = "key_k")
            }
            Kbd(icon = "corner-down-left", id = "key_enter")
        }
        Separator(id = "separator_horizontal")
        Row(gap = 6, crossAlign = Alignment.CENTER, id = "separators") {
            P(Component.text("Akten"), id = "sep_a")
            Separator(Orientation.VERTICAL, id = "separator_a")
            P(Component.text("Fahrzeuge"), id = "sep_b")
            Separator(Orientation.VERTICAL, id = "separator_b")
            P(Component.text("Personen"), id = "sep_c")
        }
    }

    /**
     * Adds skeletons, spinners, a progress bar and an aspect ratio box.
     */
    private fun ComponentScope.loading() = section("loading", "Laden") {
        Row(gap = 8, crossAlign = Alignment.CENTER, id = "skeleton_row") {
            Skeleton(ElementSize.fixed(24), ElementSize.fixed(24), round = true, id = "skeleton_avatar")
            Column(width = ElementSize.grow(), gap = 4, crossAlign = Alignment.STRETCH, id = "skeleton_lines") {
                Skeleton(ElementSize.grow(), ElementSize.fixed(6), id = "skeleton_line_a")
                Skeleton(ElementSize.fixed(120), ElementSize.fixed(6), id = "skeleton_line_b")
            }
        }
        Row(gap = 8, crossAlign = Alignment.CENTER, id = "spinners") {
            Spinner(size = 8, id = "spinner_small")
            Spinner(id = "spinner_default")
            Spinner(size = 16, tint = IconTint.PRIMARY, id = "spinner_large")
            Spinner(size = 12, tint = IconTint.MUTED, id = "spinner_muted")
            Muted(Component.text("Wird geladen …"), id = "spinner_text")
        }
        Progress(0.66f, width = ElementSize.grow(), id = "progress")
        Row(gap = 8, id = "ratio_row") {
            AspectRatio(16f / 9f, ElementSize.fixed(160), id = "ratio") {
                Image(Key.key("minecraft", "textures/block/oak_planks.png"), ElementSize.grow(), ElementSize.grow(), id = "ratio_image")
            }
            Muted(Component.text("Das Bild füllt eine Fläche im Seitenverhältnis 16:9."), width = ElementSize.grow(), id = "ratio_text")
        }
    }

    /**
     * Adds avatars in every size and source, and an avatar group.
     *
     * @param playerId the UUID of the player whose face the player avatar shows
     */
    private fun ComponentScope.avatars(playerId: UUID) = section("avatars", "Avatare") {
        Row(gap = 8, crossAlign = Alignment.CENTER, id = "avatar_row") {
            Avatar(Component.text("DU"), AvatarSource.Player(playerId), AvatarSize.LG, badgeIcon = "check", id = "avatar_player")
            Avatar(Component.text("AP"), AvatarSource.Texture(Key.key("minecraft", "textures/item/apple.png")), id = "avatar_texture")
            Avatar(Component.text("MS"), size = AvatarSize.SM, badge = true, id = "avatar_fallback")
            AvatarGroup(id = "avatar_group") {
                Avatar(Component.text("AB"), AvatarSource.Player(playerId), id = "group_a")
                Avatar(Component.text("CD"), id = "group_b")
                Avatar(Component.text("EF"), AvatarSource.Texture(Key.key("minecraft", "textures/item/emerald.png")), id = "group_c")
                AvatarGroupCount(Component.text("+5"), id = "group_count")
            }
        }
    }

    /**
     * Adds a card with every part.
     *
     * @param clicked the handler that reports clicks
     */
    private fun ComponentScope.cards(clicked: ButtonHandler) = section("cards", "Karten") {
        Card(width = ElementSize.grow(), id = "card") {
            CardHeader(id = "card_header") {
                CardTitle(Component.text("Bei deinem Konto anmelden"), id = "card_title")
                CardDescription(Component.text("Gib deine Daten ein, um auf die Akten der Dienststelle zuzugreifen."), id = "card_description")
                CardAction(id = "card_action") {
                    Button(Component.text("Registrieren"), submitsInput = false, variant = ButtonVariant.LINK, size = ButtonSize.SM, onClick = clicked, id = "card_signup")
                }
            }
            CardContent(id = "card_content") {
                P(Component.text("Der Zugang wird für jede Anmeldung protokolliert. Teile deine Zugangsdaten mit niemandem."), id = "card_body")
            }
            CardFooter(id = "card_footer") {
                Button(Component.text("Anmelden"), submitsInput = false, width = ElementSize.grow(), onClick = clicked, id = "card_login")
                Button(Component.text("Abbrechen"), submitsInput = false, variant = ButtonVariant.OUTLINE, onClick = clicked, id = "card_cancel")
            }
        }
    }

    /**
     * Adds an empty state with icon media and content.
     *
     * @param clicked the handler that reports clicks
     */
    private fun ComponentScope.empties(clicked: ButtonHandler) = section("empties", "Leerer Zustand") {
        Empty(outline = true, id = "empty") {
            EmptyHeader(id = "empty_header") {
                EmptyMedia(EmptyMediaVariant.ICON, icon = "folder-open", id = "empty_media")
                EmptyTitle(Component.text("Noch keine Akten"), id = "empty_title")
                EmptyDescription(Component.text("Du hast noch keine Akte angelegt. Lege deine erste Akte an, um loszulegen."), id = "empty_description")
            }
            EmptyContent(id = "empty_content") {
                Row(gap = 4, id = "empty_buttons") {
                    Button(Component.text("Akte anlegen"), submitsInput = false, onClick = clicked, id = "empty_create")
                    Button(Component.text("Importieren"), submitsInput = false, variant = ButtonVariant.OUTLINE, onClick = clicked, id = "empty_import")
                }
            }
        }
    }

    /**
     * Adds items in every variant and size, in a group with separators, one of them clickable.
     *
     * @param clicked the handler that reports clicks
     */
    private fun ComponentScope.items(clicked: ButtonHandler) = section("items", "Einträge") {
        ItemGroup(id = "item_group") {
            Item(ItemVariant.OUTLINE, id = "item_outline") {
                ItemMedia(ItemMediaVariant.ICON, icon = "shield-check", id = "item_outline_media")
                ItemContent(id = "item_outline_content") {
                    ItemTitle(Component.text("Sicherheitsprüfung"), id = "item_outline_title")
                    ItemDescription(Component.text("Alle Zugänge wurden in den letzten 24 Stunden geprüft. Es wurden keine Auffälligkeiten gefunden."), id = "item_outline_description")
                }
                ItemActions(id = "item_outline_actions") {
                    Button(Component.text("Ansehen"), submitsInput = false, variant = ButtonVariant.OUTLINE, size = ButtonSize.SM, onClick = clicked, id = "item_outline_open")
                }
            }
            ItemSeparator(id = "item_separator_a")
            Item(ItemVariant.MUTED, ItemSize.SM, onClick = clicked, id = "item_click") {
                ItemHeader(id = "item_click_header") {
                    Badge(Component.text("Neu"), variant = BadgeVariant.SECONDARY, id = "item_click_badge")
                    Muted(Component.text("vor 5 Minuten"), id = "item_click_time")
                }
                ItemMedia(id = "item_click_media") { Avatar(Component.text("JD"), id = "item_click_avatar") }
                ItemContent(id = "item_click_content") {
                    ItemTitle(Component.text("Anklickbarer Eintrag"), id = "item_click_title")
                    ItemDescription(Component.text("Ein Klick meldet sich im Chat."), id = "item_click_description")
                }
                ItemActions(id = "item_click_actions") {
                    Kbd(icon = "chevron-right", id = "item_click_key")
                }
                ItemFooter(id = "item_click_footer") {
                    Muted(Component.text("Streife 12"), id = "item_click_footer_text")
                }
            }
            ItemSeparator(id = "item_separator_b")
            Item(id = "item_default") {
                ItemMedia(ItemMediaVariant.IMAGE, id = "item_default_media") {
                    Image(Key.key("minecraft", "textures/block/bricks.png"), ElementSize.grow(), ElementSize.grow(), id = "item_default_image")
                }
                ItemContent(id = "item_default_content") {
                    ItemTitle(Component.text("Eintrag mit Bild"), id = "item_default_title")
                }
            }
        }
    }

    /**
     * A paragraph long enough to wrap.
     */
    private const val PARAGRAPH: String = "Ein Absatz fließt über mehrere Zeilen, sobald er breiter wäre als die Fläche, die ihm " +
        "zur Verfügung steht. Die Zeilen brechen an Wortgrenzen um, und die Fläche wächst um genau die Zeilen, die dazukommen."
}
