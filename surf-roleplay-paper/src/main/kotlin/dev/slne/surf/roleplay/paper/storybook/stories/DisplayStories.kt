package dev.slne.surf.roleplay.paper.storybook.stories

import dev.slne.surf.roleplay.api.client.common.screen.AlertVariant
import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.AvatarSize
import dev.slne.surf.roleplay.api.client.common.screen.AvatarSource
import dev.slne.surf.roleplay.api.client.common.screen.BadgeVariant
import dev.slne.surf.roleplay.api.client.common.screen.ButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.ButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.EmptyMediaVariant
import dev.slne.surf.roleplay.api.client.common.screen.IconTint
import dev.slne.surf.roleplay.api.client.common.screen.ItemMediaVariant
import dev.slne.surf.roleplay.api.client.common.screen.ItemSize
import dev.slne.surf.roleplay.api.client.common.screen.ItemVariant
import dev.slne.surf.roleplay.api.client.common.screen.Orientation
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
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
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
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Separator
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Skeleton
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Small
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Spinner
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TypographyList
import dev.slne.surf.roleplay.paper.storybook.Story
import dev.slne.surf.roleplay.paper.storybook.StoryCategory
import dev.slne.surf.roleplay.paper.storybook.StoryContext
import dev.slne.surf.roleplay.paper.storybook.slug
import dev.slne.surf.roleplay.paper.storybook.storySection
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

/**
 * The stories of the display components, in sidebar order.
 */
internal val DISPLAY_STORIES: List<Story> = listOf(
    Story("typography", "Typografie", StoryCategory.DISPLAY) { typographyStory() },
    Story("alert", "Hinweis", StoryCategory.DISPLAY) { alertStory() },
    Story("badge", "Abzeichen", StoryCategory.DISPLAY) { badgeStory() },
    Story("kbd", "Taste", StoryCategory.DISPLAY) { kbdStory() },
    Story("separator", "Trenner", StoryCategory.DISPLAY) { separatorStory() },
    Story("skeleton", "Platzhalter", StoryCategory.DISPLAY) { skeletonStory() },
    Story("spinner", "Ladeanzeige", StoryCategory.DISPLAY) { spinnerStory() },
    Story("progress", "Fortschritt", StoryCategory.DISPLAY) { progressStory() },
    Story("aspect-ratio", "Seitenverhältnis", StoryCategory.DISPLAY) { aspectRatioStory() },
    Story("avatar", "Avatar", StoryCategory.DISPLAY) { avatarStory(it) },
    Story("card", "Karte", StoryCategory.DISPLAY) { cardStory(it) },
    Story("empty", "Leerer Zustand", StoryCategory.DISPLAY) { emptyStory(it) },
    Story("item", "Eintrag", StoryCategory.DISPLAY) { itemStory(it) },
)

/**
 * Shows every heading and text style, inline code, a clamped paragraph and both lists.
 */
private fun ComponentScope.typographyStory() {
    storySection("Überschriften") {
        H1("Überschrift 1")
        H2("Überschrift 2")
        H3("Überschrift 3")
        H4("Überschrift 4")
    }
    storySection("Text") {
        Lead("Ein einleitender Satz, der etwas größer und gedämpft erscheint.")
        P("Ein Absatz fließt über mehrere Zeilen, sobald er breiter wäre als die Fläche, die ihm zur Verfügung steht.", width = ElementSize.grow())
        Large("Großer Text")
        Small("Kleiner Text")
        Muted("Gedämpfter Text für Nebensächliches.")
        Row(gap = 4, crossAlign = Alignment.CENTER) {
            P("Befehl:")
            InlineCode(Component.text("/rpstorybook"))
        }
        P("Dieser lange Text ist auf eine Zeile begrenzt und endet deshalb mit Auslassungspunkten, sobald er nicht mehr passt.", maxLines = 1, width = ElementSize.grow())
    }
    storySection("Beispiel") {
        Blockquote(Component.text("„In dieser Stadt gibt es keine Geheimnisse, nur Akten, die noch niemand gelesen hat.“"))
        TypographyList(listOf(Component.text("Führerschein beantragen"), Component.text("Fahrzeug anmelden"), Component.text("Termin wahrnehmen")))
        TypographyList(listOf(Component.text("Formular ausfüllen"), Component.text("Unterschreiben"), Component.text("Abgeben")), ordered = true)
    }
}

/**
 * Shows alerts in every variant, with and without an icon and description.
 */
private fun ComponentScope.alertStory() {
    storySection("Varianten") {
        AlertVariant.entries.forEach { variant ->
            Alert(variant, icon = "info") {
                AlertTitle(Component.text("Variante ${variant.slug()}"))
                AlertDescription(Component.text("Ein Hinweis mit Symbol, Titel und Beschreibung."))
            }
            Alert(variant) {
                AlertTitle(Component.text("Ohne Symbol und Beschreibung"))
            }
        }
    }
    storySection("Beispiel") {
        Alert(AlertVariant.DESTRUCTIVE, icon = "circle-alert") {
            AlertTitle(Component.text("Zahlung fehlgeschlagen"))
            AlertDescription(Component.text("Das Konto ist nicht gedeckt. Bitte prüfe den Kontostand und versuche es erneut."))
        }
    }
}

/**
 * Shows badges in every variant, with and without an icon.
 */
private fun ComponentScope.badgeStory() {
    storySection("Varianten") {
        Row(gap = 4, crossAlign = Alignment.CENTER) {
            BadgeVariant.entries.forEach { variant -> Badge(variant.slug(), variant = variant) }
        }
        Row(gap = 4, crossAlign = Alignment.CENTER) {
            BadgeVariant.entries.forEach { variant -> Badge(variant.slug(), icon = "star", variant = variant) }
        }
    }
    storySection("Beispiel") {
        Row(gap = 4, crossAlign = Alignment.CENTER) {
            Badge("Geprüft", icon = "badge-check", variant = BadgeVariant.SECONDARY)
            Badge("8", variant = BadgeVariant.DESTRUCTIVE)
            Badge("Im Dienst", icon = "radio", variant = BadgeVariant.OUTLINE)
        }
    }
}

/**
 * Shows keys with text and icons, and a key combination.
 */
private fun ComponentScope.kbdStory() {
    storySection("Varianten") {
        Row(gap = 6, crossAlign = Alignment.CENTER) {
            Kbd("Esc")
            Kbd(icon = "corner-down-left")
            Kbd("Strg", icon = "command")
        }
    }
    storySection("Beispiel") {
        Row(gap = 6, crossAlign = Alignment.CENTER) {
            P("Suche öffnen:")
            KbdGroup {
                Kbd("Strg")
                P("+")
                Kbd("K")
            }
        }
    }
}

/**
 * Shows separators in both orientations.
 */
private fun ComponentScope.separatorStory() {
    storySection("Ausrichtungen") {
        Orientation.entries.forEach { orientation ->
            if (orientation == Orientation.HORIZONTAL) {
                Separator(orientation)
            } else {
                Row(gap = 6, crossAlign = Alignment.CENTER, height = ElementSize.fixed(16)) {
                    P("Akten")
                    Separator(orientation)
                    P("Fahrzeuge")
                    Separator(orientation)
                    P("Personen")
                }
            }
        }
    }
}

/**
 * Shows square, round and line skeletons as a loading profile.
 */
private fun ComponentScope.skeletonStory() {
    storySection("Formen") {
        Row(gap = 8, crossAlign = Alignment.CENTER) {
            Skeleton(ElementSize.fixed(24), ElementSize.fixed(24))
            Skeleton(ElementSize.fixed(24), ElementSize.fixed(24), round = true)
            Skeleton(ElementSize.fixed(120), ElementSize.fixed(6))
        }
    }
    storySection("Beispiel") {
        Row(gap = 8, crossAlign = Alignment.CENTER, width = ElementSize.grow()) {
            Skeleton(ElementSize.fixed(24), ElementSize.fixed(24), round = true)
            Column(width = ElementSize.grow(), gap = 4, crossAlign = Alignment.STRETCH) {
                Skeleton(ElementSize.grow(), ElementSize.fixed(6))
                Skeleton(ElementSize.fixed(120), ElementSize.fixed(6))
            }
        }
    }
}

/**
 * Shows spinners in several sizes and every tint.
 */
private fun ComponentScope.spinnerStory() {
    storySection("Größen") {
        Row(gap = 8, crossAlign = Alignment.CENTER) {
            listOf(8, 10, 12, 16).forEach { size -> Spinner(size = size) }
        }
    }
    storySection("Farben") {
        Row(gap = 8, crossAlign = Alignment.CENTER) {
            IconTint.entries.forEach { tint -> Spinner(size = 12, tint = tint) }
        }
    }
    storySection("Beispiel") {
        Row(gap = 6, crossAlign = Alignment.CENTER) {
            Spinner(tint = IconTint.MUTED)
            Muted("Akte wird geladen …")
        }
    }
}

/**
 * Shows progress bars at several values, with and without a label.
 */
private fun ComponentScope.progressStory() {
    storySection("Werte") {
        listOf(0f, 0.33f, 0.66f, 1f).forEach { value -> Progress(value, width = ElementSize.grow()) }
    }
    storySection("Beispiel") {
        Progress(0.4f, Component.text("Upload: 40 %"), width = ElementSize.grow())
    }
}

/**
 * Shows images in several aspect ratios.
 */
private fun ComponentScope.aspectRatioStory() {
    storySection("Seitenverhältnisse") {
        Row(gap = 8) {
            listOf(1f to "1:1", 4f / 3f to "4:3", 16f / 9f to "16:9").forEach { (ratio, label) ->
                Column(gap = 2) {
                    AspectRatio(ratio, ElementSize.fixed(120)) {
                        Image(Key.key("minecraft", "textures/block/oak_planks.png"), ElementSize.grow(), ElementSize.grow())
                    }
                    Muted(label)
                }
            }
        }
    }
}

/**
 * Shows avatars in every size and source, with badges, and an avatar group.
 *
 * @param context the story context
 */
private fun ComponentScope.avatarStory(context: StoryContext) {
    storySection("Größen") {
        Row(gap = 8, crossAlign = Alignment.CENTER) {
            AvatarSize.entries.forEach { size -> Avatar("MS", size = size) }
        }
    }
    storySection("Quellen und Abzeichen") {
        Row(gap = 8, crossAlign = Alignment.CENTER) {
            Avatar("DU", AvatarSource.Player(context.playerId), AvatarSize.LG, badgeIcon = "check")
            Avatar("AP", AvatarSource.Texture(Key.key("minecraft", "textures/item/apple.png")))
            Avatar("MS", badge = true)
        }
    }
    storySection("Beispiel") {
        AvatarGroup {
            Avatar("AB", AvatarSource.Player(context.playerId))
            Avatar("CD")
            Avatar("EF", AvatarSource.Texture(Key.key("minecraft", "textures/item/emerald.png")))
            AvatarGroupCount(Component.text("+5"))
        }
    }
}

/**
 * Shows a card with every part and a minimal card.
 *
 * @param context the story context
 */
private fun ComponentScope.cardStory(context: StoryContext) {
    storySection("Nur Inhalt") {
        Card(width = ElementSize.grow()) {
            CardContent { P("Eine Karte mit nichts als Inhalt.") }
        }
    }
    storySection("Beispiel") {
        Card(width = ElementSize.grow()) {
            CardHeader {
                CardTitle(Component.text("Bei deinem Konto anmelden"))
                CardDescription(Component.text("Gib deine Daten ein, um auf die Akten der Dienststelle zuzugreifen."))
                CardAction {
                    Button("Registrieren", submitsInput = false, variant = ButtonVariant.LINK, size = ButtonSize.SM, id = "card_signup", onClick = context.clicked)
                }
            }
            CardContent {
                P("Der Zugang wird für jede Anmeldung protokolliert. Teile deine Zugangsdaten mit niemandem.", width = ElementSize.grow())
            }
            CardFooter {
                Button("Anmelden", submitsInput = false, width = ElementSize.grow(), id = "card_login", onClick = context.clicked)
                Button("Abbrechen", submitsInput = false, variant = ButtonVariant.OUTLINE, id = "card_cancel", onClick = context.clicked)
            }
        }
    }
}

/**
 * Shows empty states with every media variant, with and without an outline.
 *
 * @param context the story context
 */
private fun ComponentScope.emptyStory(context: StoryContext) {
    storySection("Medien und Rahmen") {
        Row(gap = 8, width = ElementSize.grow()) {
            EmptyMediaVariant.entries.forEach { variant ->
                Empty(outline = variant == EmptyMediaVariant.ICON) {
                    EmptyHeader {
                        EmptyMedia(variant, icon = "inbox")
                        EmptyTitle("Variante ${variant.slug()}")
                    }
                }
            }
        }
    }
    storySection("Beispiel") {
        Empty(outline = true) {
            EmptyHeader {
                EmptyMedia(EmptyMediaVariant.ICON, icon = "folder-open")
                EmptyTitle("Noch keine Akten")
                EmptyDescription("Du hast noch keine Akte angelegt. Lege deine erste Akte an, um loszulegen.")
            }
            EmptyContent {
                Row(gap = 4) {
                    Button("Akte anlegen", submitsInput = false, id = "empty_create", onClick = context.clicked)
                    Button("Importieren", submitsInput = false, variant = ButtonVariant.OUTLINE, id = "empty_import", onClick = context.clicked)
                }
            }
        }
    }
}

/**
 * Shows items in every variant and size, every media variant, and a clickable item group.
 *
 * @param context the story context
 */
private fun ComponentScope.itemStory(context: StoryContext) {
    storySection("Varianten und Größen") {
        ItemVariant.entries.forEach { variant ->
            ItemSize.entries.forEach { size ->
                Item(variant, size) {
                    ItemContent {
                        ItemTitle(Component.text("Variante ${variant.slug()}, Größe ${size.slug()}"))
                    }
                }
            }
        }
    }
    storySection("Medien") {
        ItemMediaVariant.entries.forEach { variant ->
            Item(ItemVariant.OUTLINE) {
                if (variant == ItemMediaVariant.IMAGE) {
                    ItemMedia(variant) { Image(Key.key("minecraft", "textures/block/bricks.png"), ElementSize.grow(), ElementSize.grow()) }
                } else {
                    ItemMedia(variant, icon = "shield-check") { if (variant == ItemMediaVariant.DEFAULT) Avatar("JD") }
                }
                ItemContent { ItemTitle(Component.text("Medium ${variant.slug()}")) }
            }
        }
    }
    storySection("Beispiel") {
        ItemGroup {
            Item(ItemVariant.MUTED, onClick = context.clicked, id = "item_call") {
                ItemHeader {
                    Badge("Neu", variant = BadgeVariant.SECONDARY)
                    Muted("vor 5 Minuten")
                }
                ItemMedia(ItemMediaVariant.ICON, icon = "siren")
                ItemContent {
                    ItemTitle(Component.text("Verkehrsunfall B7"))
                    ItemDescription(Component.text("Zwei Verletzte, Rettungswagen angefordert."))
                }
                ItemActions { Kbd(icon = "chevron-right") }
                ItemFooter { Muted("Streife 12") }
            }
            ItemSeparator()
            Item(ItemVariant.OUTLINE) {
                ItemMedia(ItemMediaVariant.ICON, icon = "shield-check")
                ItemContent {
                    ItemTitle(Component.text("Sicherheitsprüfung"))
                    ItemDescription(Component.text("Alle Zugänge wurden in den letzten 24 Stunden geprüft."))
                }
                ItemActions {
                    Button("Ansehen", submitsInput = false, variant = ButtonVariant.OUTLINE, size = ButtonSize.SM, id = "item_open", onClick = context.clicked)
                }
            }
        }
    }
}
