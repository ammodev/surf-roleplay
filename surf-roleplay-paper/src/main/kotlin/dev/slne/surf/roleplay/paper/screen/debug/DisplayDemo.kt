package dev.slne.surf.roleplay.paper.screen.debug

import dev.slne.surf.roleplay.api.client.common.screen.AlertVariant
import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.AvatarSize
import dev.slne.surf.roleplay.api.client.common.screen.AvatarSource
import dev.slne.surf.roleplay.api.client.common.screen.BadgeVariant
import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.ButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.ElementsBuilder
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
import dev.slne.surf.roleplay.api.client.common.screen.alert
import dev.slne.surf.roleplay.api.client.common.screen.alertDescription
import dev.slne.surf.roleplay.api.client.common.screen.alertTitle
import dev.slne.surf.roleplay.api.client.common.screen.aspectRatio
import dev.slne.surf.roleplay.api.client.common.screen.avatar
import dev.slne.surf.roleplay.api.client.common.screen.avatarGroup
import dev.slne.surf.roleplay.api.client.common.screen.avatarGroupCount
import dev.slne.surf.roleplay.api.client.common.screen.badge
import dev.slne.surf.roleplay.api.client.common.screen.card
import dev.slne.surf.roleplay.api.client.common.screen.cardAction
import dev.slne.surf.roleplay.api.client.common.screen.cardContent
import dev.slne.surf.roleplay.api.client.common.screen.cardDescription
import dev.slne.surf.roleplay.api.client.common.screen.cardFooter
import dev.slne.surf.roleplay.api.client.common.screen.cardHeader
import dev.slne.surf.roleplay.api.client.common.screen.cardTitle
import dev.slne.surf.roleplay.api.client.common.screen.empty
import dev.slne.surf.roleplay.api.client.common.screen.emptyContent
import dev.slne.surf.roleplay.api.client.common.screen.emptyDescription
import dev.slne.surf.roleplay.api.client.common.screen.emptyHeader
import dev.slne.surf.roleplay.api.client.common.screen.emptyMedia
import dev.slne.surf.roleplay.api.client.common.screen.emptyTitle
import dev.slne.surf.roleplay.api.client.common.screen.item
import dev.slne.surf.roleplay.api.client.common.screen.itemActions
import dev.slne.surf.roleplay.api.client.common.screen.itemContent
import dev.slne.surf.roleplay.api.client.common.screen.itemDescription
import dev.slne.surf.roleplay.api.client.common.screen.itemFooter
import dev.slne.surf.roleplay.api.client.common.screen.itemGroup
import dev.slne.surf.roleplay.api.client.common.screen.itemHeader
import dev.slne.surf.roleplay.api.client.common.screen.itemMedia
import dev.slne.surf.roleplay.api.client.common.screen.itemSeparator
import dev.slne.surf.roleplay.api.client.common.screen.itemTitle
import dev.slne.surf.roleplay.api.client.common.screen.kbd
import dev.slne.surf.roleplay.api.client.common.screen.kbdGroup
import dev.slne.surf.roleplay.api.client.common.screen.screen
import dev.slne.surf.roleplay.api.client.common.screen.select
import dev.slne.surf.roleplay.api.client.common.screen.separator
import dev.slne.surf.roleplay.api.client.common.screen.skeleton
import dev.slne.surf.roleplay.api.client.common.screen.spinner
import dev.slne.surf.roleplay.api.client.common.screen.text
import dev.slne.surf.roleplay.api.client.common.screen.textList
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
        return screen(Component.text("Anzeige")) {
            this.theme = theme
            this.variant = variant
            column("root", width = ElementSize.fixed(380), gap = 12, crossAlign = Alignment.STRETCH) {
                row("theme_row", gap = 4, crossAlign = Alignment.CENTER) {
                    select("theme", InputsDemo.THEMES, selected = theme, width = ElementSize.grow())
                    select("variant", InputsDemo.VARIANTS, selected = variant.name, width = ElementSize.fixed(90))
                    button("apply_theme", Component.text("Anwenden"), submitsInput = false, icon = "palette", variant = ButtonVariant.OUTLINE) { click ->
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
    private fun ElementsBuilder.section(id: String, title: String, content: ElementsBuilder.() -> Unit) {
        column(id, gap = 6, crossAlign = Alignment.STRETCH) {
            text("${id}_title", Component.text(title), TextKind.H3)
            content()
        }
    }

    /**
     * Adds every typography style and both lists.
     */
    private fun ElementsBuilder.typography() = section("typography", "Typografie") {
        text("h1", Component.text("Überschrift 1"), TextKind.H1)
        text("h2", Component.text("Überschrift 2"), TextKind.H2)
        text("h3", Component.text("Überschrift 3"), TextKind.H3)
        text("h4", Component.text("Überschrift 4"), TextKind.H4)
        text("lead", Component.text("Ein einleitender Satz, der etwas größer und gedämpft erscheint."), TextKind.LEAD)
        text("p", Component.text(PARAGRAPH))
        text("large", Component.text("Großer Text"), TextKind.LARGE)
        text("small", Component.text("Kleiner Text"), TextKind.SMALL)
        text("muted", Component.text("Gedämpfter Text für Nebensächliches."), TextKind.MUTED)
        text("blockquote", Component.text("„In dieser Stadt gibt es keine Geheimnisse, nur Akten, die noch niemand gelesen hat.“"), TextKind.BLOCKQUOTE)
        row("code_row", gap = 4, crossAlign = Alignment.CENTER) {
            text("code_before", Component.text("Befehl:"))
            text("code", Component.text("/rpscreen display"), TextKind.INLINE_CODE)
        }
        text("clamped", Component.text("Dieser lange Text ist auf eine Zeile begrenzt und endet deshalb mit Auslassungspunkten, sobald er nicht mehr passt."), maxLines = 1)
        textList("bullets", listOf(Component.text("Führerschein beantragen"), Component.text("Fahrzeug anmelden, sobald der Führerschein ausgestellt wurde und die Versicherung bestätigt ist"), Component.text("Termin wahrnehmen")))
        textList("numbers", listOf(Component.text("Formular ausfüllen"), Component.text("Unterschreiben"), Component.text("Abgeben")), ordered = true)
    }

    /**
     * Adds alerts in both variants, with and without an icon.
     */
    private fun ElementsBuilder.alerts() = section("alerts", "Hinweise") {
        alert("alert_default", icon = "circle-check") {
            alertTitle("alert_default_title", Component.text("Erfolgreich gespeichert"))
            alertDescription("alert_default_description", Component.text("Deine Änderungen wurden übernommen und sind ab sofort für alle Beamten sichtbar."))
        }
        alert("alert_destructive", AlertVariant.DESTRUCTIVE, icon = "circle-alert") {
            alertTitle("alert_destructive_title", Component.text("Zahlung fehlgeschlagen"))
            alertDescription("alert_destructive_description", Component.text("Das Konto ist nicht gedeckt. Bitte prüfe den Kontostand und versuche es erneut."))
        }
        alert("alert_plain") {
            alertTitle("alert_plain_title", Component.text("Ein Hinweis ohne Symbol"))
        }
    }

    /**
     * Adds badges, keys and separators.
     */
    private fun ElementsBuilder.marks() = section("marks", "Abzeichen, Tasten und Trenner") {
        row("badges", gap = 4, crossAlign = Alignment.CENTER) {
            BadgeVariant.entries.forEach { variant ->
                badge("badge_${variant.name.lowercase()}", Component.text(variant.name.lowercase().replaceFirstChar(Char::uppercase)), variant = variant)
            }
        }
        row("badges_icons", gap = 4, crossAlign = Alignment.CENTER) {
            badge("badge_verified", Component.text("Geprüft"), icon = "badge-check", variant = BadgeVariant.SECONDARY)
            badge("badge_count", Component.text("8"), variant = BadgeVariant.DESTRUCTIVE)
            badge("badge_online", Component.text("Im Dienst"), icon = "radio", variant = BadgeVariant.OUTLINE)
        }
        row("keys", gap = 6, crossAlign = Alignment.CENTER) {
            text("keys_label", Component.text("Suche öffnen:"))
            kbdGroup("keys_search") {
                kbd("key_ctrl", Component.text("Strg"))
                text("key_plus", Component.text("+"))
                kbd("key_k", Component.text("K"))
            }
            kbd("key_enter", icon = "corner-down-left")
        }
        separator("separator_horizontal")
        row("separators", gap = 6, crossAlign = Alignment.CENTER) {
            text("sep_a", Component.text("Akten"))
            separator("separator_a", Orientation.VERTICAL)
            text("sep_b", Component.text("Fahrzeuge"))
            separator("separator_b", Orientation.VERTICAL)
            text("sep_c", Component.text("Personen"))
        }
    }

    /**
     * Adds skeletons, spinners, a progress bar and an aspect ratio box.
     */
    private fun ElementsBuilder.loading() = section("loading", "Laden") {
        row("skeleton_row", gap = 8, crossAlign = Alignment.CENTER) {
            skeleton("skeleton_avatar", ElementSize.fixed(24), ElementSize.fixed(24), round = true)
            column("skeleton_lines", width = ElementSize.grow(), gap = 4, crossAlign = Alignment.STRETCH) {
                skeleton("skeleton_line_a", ElementSize.grow(), ElementSize.fixed(6))
                skeleton("skeleton_line_b", ElementSize.fixed(120), ElementSize.fixed(6))
            }
        }
        row("spinners", gap = 8, crossAlign = Alignment.CENTER) {
            spinner("spinner_small", size = 8)
            spinner("spinner_default")
            spinner("spinner_large", size = 16, tint = IconTint.PRIMARY)
            spinner("spinner_muted", size = 12, tint = IconTint.MUTED)
            text("spinner_text", Component.text("Wird geladen …"), TextKind.MUTED)
        }
        progress("progress", 0.66f, width = ElementSize.grow())
        row("ratio_row", gap = 8) {
            aspectRatio("ratio", 16f / 9f, ElementSize.fixed(160)) {
                image("ratio_image", Key.key("minecraft", "textures/block/oak_planks.png"), ElementSize.grow(), ElementSize.grow())
            }
            text("ratio_text", Component.text("Das Bild füllt eine Fläche im Seitenverhältnis 16:9."), TextKind.MUTED, width = ElementSize.grow())
        }
    }

    /**
     * Adds avatars in every size and source, and an avatar group.
     *
     * @param playerId the UUID of the player whose face the player avatar shows
     */
    private fun ElementsBuilder.avatars(playerId: UUID) = section("avatars", "Avatare") {
        row("avatar_row", gap = 8, crossAlign = Alignment.CENTER) {
            avatar("avatar_player", Component.text("DU"), AvatarSource.Player(playerId), AvatarSize.LG, badgeIcon = "check")
            avatar("avatar_texture", Component.text("AP"), AvatarSource.Texture(Key.key("minecraft", "textures/item/apple.png")))
            avatar("avatar_fallback", Component.text("MS"), size = AvatarSize.SM, badge = true)
            avatarGroup("avatar_group") {
                avatar("group_a", Component.text("AB"), AvatarSource.Player(playerId))
                avatar("group_b", Component.text("CD"))
                avatar("group_c", Component.text("EF"), AvatarSource.Texture(Key.key("minecraft", "textures/item/emerald.png")))
                avatarGroupCount("group_count", Component.text("+5"))
            }
        }
    }

    /**
     * Adds a card with every part.
     *
     * @param clicked the handler that reports clicks
     */
    private fun ElementsBuilder.cards(clicked: ButtonHandler) = section("cards", "Karten") {
        card("card", width = ElementSize.grow()) {
            cardHeader("card_header") {
                cardTitle("card_title", Component.text("Bei deinem Konto anmelden"))
                cardDescription("card_description", Component.text("Gib deine Daten ein, um auf die Akten der Dienststelle zuzugreifen."))
                cardAction("card_action") {
                    button("card_signup", Component.text("Registrieren"), submitsInput = false, variant = ButtonVariant.LINK, size = ButtonSize.SM, onClick = clicked)
                }
            }
            cardContent("card_content") {
                text("card_body", Component.text("Der Zugang wird für jede Anmeldung protokolliert. Teile deine Zugangsdaten mit niemandem."))
            }
            cardFooter("card_footer") {
                button("card_login", Component.text("Anmelden"), submitsInput = false, width = ElementSize.grow(), onClick = clicked)
                button("card_cancel", Component.text("Abbrechen"), submitsInput = false, variant = ButtonVariant.OUTLINE, onClick = clicked)
            }
        }
    }

    /**
     * Adds an empty state with icon media and content.
     *
     * @param clicked the handler that reports clicks
     */
    private fun ElementsBuilder.empties(clicked: ButtonHandler) = section("empties", "Leerer Zustand") {
        empty("empty", outline = true) {
            emptyHeader("empty_header") {
                emptyMedia("empty_media", EmptyMediaVariant.ICON, icon = "folder-open")
                emptyTitle("empty_title", Component.text("Noch keine Akten"))
                emptyDescription("empty_description", Component.text("Du hast noch keine Akte angelegt. Lege deine erste Akte an, um loszulegen."))
            }
            emptyContent("empty_content") {
                row("empty_buttons", gap = 4) {
                    button("empty_create", Component.text("Akte anlegen"), submitsInput = false, onClick = clicked)
                    button("empty_import", Component.text("Importieren"), submitsInput = false, variant = ButtonVariant.OUTLINE, onClick = clicked)
                }
            }
        }
    }

    /**
     * Adds items in every variant and size, in a group with separators, one of them clickable.
     *
     * @param clicked the handler that reports clicks
     */
    private fun ElementsBuilder.items(clicked: ButtonHandler) = section("items", "Einträge") {
        itemGroup("item_group") {
            item("item_outline", ItemVariant.OUTLINE) {
                itemMedia("item_outline_media", ItemMediaVariant.ICON, icon = "shield-check")
                itemContent("item_outline_content") {
                    itemTitle("item_outline_title", Component.text("Sicherheitsprüfung"))
                    itemDescription("item_outline_description", Component.text("Alle Zugänge wurden in den letzten 24 Stunden geprüft. Es wurden keine Auffälligkeiten gefunden."))
                }
                itemActions("item_outline_actions") {
                    button("item_outline_open", Component.text("Ansehen"), submitsInput = false, variant = ButtonVariant.OUTLINE, size = ButtonSize.SM, onClick = clicked)
                }
            }
            itemSeparator("item_separator_a")
            item("item_click", ItemVariant.MUTED, ItemSize.SM, onClick = clicked) {
                itemHeader("item_click_header") {
                    badge("item_click_badge", Component.text("Neu"), variant = BadgeVariant.SECONDARY)
                    text("item_click_time", Component.text("vor 5 Minuten"), TextKind.MUTED)
                }
                itemMedia("item_click_media") { avatar("item_click_avatar", Component.text("JD")) }
                itemContent("item_click_content") {
                    itemTitle("item_click_title", Component.text("Anklickbarer Eintrag"))
                    itemDescription("item_click_description", Component.text("Ein Klick meldet sich im Chat."))
                }
                itemActions("item_click_actions") {
                    kbd("item_click_key", icon = "chevron-right")
                }
                itemFooter("item_click_footer") {
                    text("item_click_footer_text", Component.text("Streife 12"), TextKind.MUTED)
                }
            }
            itemSeparator("item_separator_b")
            item("item_default") {
                itemMedia("item_default_media", ItemMediaVariant.IMAGE) {
                    image("item_default_image", Key.key("minecraft", "textures/block/bricks.png"), ElementSize.grow(), ElementSize.grow())
                }
                itemContent("item_default_content") {
                    itemTitle("item_default_title", Component.text("Eintrag mit Bild"))
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
