package dev.slne.surf.roleplay.paper.screen.debug

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.ButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.CalendarMode
import dev.slne.surf.roleplay.api.client.common.screen.CaptionLayout
import dev.slne.surf.roleplay.api.client.common.screen.ChangeHandler
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.ElementsBuilder
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupAlign
import dev.slne.surf.roleplay.api.client.common.screen.Orientation
import dev.slne.surf.roleplay.api.client.common.screen.OtpPattern
import dev.slne.surf.roleplay.api.client.common.screen.RadioChoice
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.ScreenThemes
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoice
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoiceGroup
import dev.slne.surf.roleplay.api.client.common.screen.SelectSize
import dev.slne.surf.roleplay.api.client.common.screen.SwitchSize
import dev.slne.surf.roleplay.api.client.common.screen.TextInputType
import dev.slne.surf.roleplay.api.client.common.screen.ToggleGroupChoice
import dev.slne.surf.roleplay.api.client.common.screen.ToggleSize
import dev.slne.surf.roleplay.api.client.common.screen.ToggleVariant
import dev.slne.surf.roleplay.api.client.common.screen.buttonGroup
import dev.slne.surf.roleplay.api.client.common.screen.buttonGroupSeparator
import dev.slne.surf.roleplay.api.client.common.screen.buttonGroupText
import dev.slne.surf.roleplay.api.client.common.screen.calendar
import dev.slne.surf.roleplay.api.client.common.screen.combobox
import dev.slne.surf.roleplay.api.client.common.screen.field
import dev.slne.surf.roleplay.api.client.common.screen.fieldContent
import dev.slne.surf.roleplay.api.client.common.screen.fieldDescription
import dev.slne.surf.roleplay.api.client.common.screen.fieldError
import dev.slne.surf.roleplay.api.client.common.screen.fieldGroup
import dev.slne.surf.roleplay.api.client.common.screen.fieldLabel
import dev.slne.surf.roleplay.api.client.common.screen.fieldLegend
import dev.slne.surf.roleplay.api.client.common.screen.fieldSeparator
import dev.slne.surf.roleplay.api.client.common.screen.fieldSet
import dev.slne.surf.roleplay.api.client.common.screen.form
import dev.slne.surf.roleplay.api.client.common.screen.inputGroup
import dev.slne.surf.roleplay.api.client.common.screen.inputGroupAddon
import dev.slne.surf.roleplay.api.client.common.screen.inputGroupButton
import dev.slne.surf.roleplay.api.client.common.screen.inputGroupText
import dev.slne.surf.roleplay.api.client.common.screen.inputOtp
import dev.slne.surf.roleplay.api.client.common.screen.nativeSelect
import dev.slne.surf.roleplay.api.client.common.screen.radioGroup
import dev.slne.surf.roleplay.api.client.common.screen.screen
import dev.slne.surf.roleplay.api.client.common.screen.select
import dev.slne.surf.roleplay.api.client.common.screen.slider
import dev.slne.surf.roleplay.api.client.common.screen.switch
import dev.slne.surf.roleplay.api.client.common.screen.textarea
import dev.slne.surf.roleplay.api.client.common.screen.toggle
import dev.slne.surf.roleplay.api.client.common.screen.toggleGroup
import dev.slne.surf.roleplay.api.client.paper.screen.ScreenService
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player
import java.time.LocalDate

/**
 * The page of the screen debug command that shows every input component in every variant, size
 * and state. Its actions and changes are reported in chat.
 */
object InputsDemo {

    /**
     * Opens the page.
     *
     * @param player the player
     * @param theme the name of the theme to draw the page with
     * @param variant the light or dark variant of the theme
     */
    fun open(player: Player, theme: String = ScreenThemes.DEFAULT, variant: ScreenVariant = ScreenVariant.DARK) {
        ScreenService.open(player, definition(player::sendMessage, { chosenTheme, chosenVariant -> open(player, chosenTheme, chosenVariant) }, theme, variant))
    }

    /**
     * Builds the page.
     *
     * @param report sends a report of an action or change to the player
     * @param reopen opens the page again in another theme and variant
     * @param theme the name of the theme
     * @param variant the variant of the theme
     * @return the page
     */
    fun definition(report: (Component) -> Unit, reopen: (String, ScreenVariant) -> Unit, theme: String, variant: ScreenVariant): ScreenDefinition {
        val changed = ChangeHandler { change ->
            report(Component.text("${change.inputId} geändert: \"${change.value}\"", NamedTextColor.GRAY))
        }
        val clicked = ButtonHandler { click ->
            report(Component.text("${click.buttonId} geklickt", NamedTextColor.GREEN))
        }
        return screen(Component.text("Eingaben")) {
            this.theme = theme
            this.variant = variant
            column("root", width = ElementSize.fixed(380), gap = 10, crossAlign = Alignment.STRETCH) {
                row("theme_row", gap = 4, crossAlign = Alignment.CENTER) {
                    select("theme", THEMES, selected = theme, width = ElementSize.grow())
                    select("variant", VARIANTS, selected = variant.name, width = ElementSize.fixed(90))
                    button("apply_theme", Component.text("Anwenden"), submitsInput = false, icon = "palette", variant = ButtonVariant.OUTLINE) { click ->
                        reopen(click.values.selected("theme") ?: ScreenThemes.DEFAULT, click.values.selected("variant")?.let(ScreenVariant::valueOf) ?: ScreenVariant.DARK)
                    }
                }
                buttons(clicked)
                toggles(clicked, changed)
                texts(clicked, changed)
                choices(changed)
                selections(report, changed)
                calendars(changed)
                forms(report)
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
        column(id, gap = 4, crossAlign = Alignment.START) {
            label("${id}_title", Component.text(title, NamedTextColor.GOLD, TextDecoration.BOLD))
            content()
        }
    }

    /**
     * Adds the buttons, in every variant and size, and the button groups.
     *
     * @param clicked the handler that reports clicks
     */
    private fun ElementsBuilder.buttons(clicked: ButtonHandler) = section("buttons", "Schaltflächen") {
        row("button_variants", gap = 4) {
            ButtonVariant.entries.forEach { variant ->
                button("button_${variant.name.lowercase()}", Component.text(variant.name.lowercase().replaceFirstChar(Char::uppercase)), submitsInput = false, variant = variant, onClick = clicked)
            }
        }
        row("button_sizes", gap = 4, crossAlign = Alignment.CENTER) {
            listOf(ButtonSize.XS, ButtonSize.SM, ButtonSize.DEFAULT, ButtonSize.LG).forEach { size ->
                button("button_size_${size.name.lowercase()}", Component.text(size.name), submitsInput = false, icon = "send", size = size, onClick = clicked)
            }
            listOf(ButtonSize.ICON_XS, ButtonSize.ICON_SM, ButtonSize.ICON, ButtonSize.ICON_LG).forEach { size ->
                button("button_size_${size.name.lowercase()}", Component.empty(), submitsInput = false, icon = "plus", variant = ButtonVariant.OUTLINE, size = size, onClick = clicked)
            }
            button("button_disabled", Component.text("Gesperrt"), enabled = false, icon = "lock")
        }
        row("button_groups", gap = 8) {
            buttonGroup("group_horizontal") {
                buttonGroupText("group_text", Component.text("Seite"), icon = "file")
                button("group_back", Component.empty(), submitsInput = false, icon = "chevron-left", variant = ButtonVariant.OUTLINE, size = ButtonSize.ICON, onClick = clicked)
                buttonGroupSeparator("group_separator")
                button("group_next", Component.empty(), submitsInput = false, icon = "chevron-right", variant = ButtonVariant.OUTLINE, size = ButtonSize.ICON, onClick = clicked)
            }
            buttonGroup("group_vertical", orientation = Orientation.VERTICAL) {
                button("group_up", Component.text("Hoch"), submitsInput = false, variant = ButtonVariant.SECONDARY, size = ButtonSize.SM, onClick = clicked)
                button("group_down", Component.text("Runter"), submitsInput = false, variant = ButtonVariant.SECONDARY, size = ButtonSize.SM, onClick = clicked)
            }
        }
    }

    /**
     * Adds the toggles and toggle groups.
     *
     * @param clicked the handler that reports toggle presses
     * @param changed the handler that reports changes
     */
    private fun ElementsBuilder.toggles(clicked: ButtonHandler, changed: ChangeHandler) = section("toggles", "Umschalter") {
        row("toggle_row", gap = 4, crossAlign = Alignment.CENTER) {
            toggle("toggle_bold", icon = "bold", onToggle = clicked)
            toggle("toggle_italic", icon = "italic", variant = ToggleVariant.OUTLINE, pressed = true, onToggle = clicked)
            toggle("toggle_sm", Component.text("Klein"), size = ToggleSize.SM, onToggle = clicked)
            toggle("toggle_lg", Component.text("Groß"), size = ToggleSize.LG, variant = ToggleVariant.OUTLINE, onToggle = clicked)
            toggle("toggle_disabled", Component.text("Aus"), enabled = false)
        }
        row("toggle_groups", gap = 8) {
            toggleGroup(
                "align",
                listOf(ToggleGroupChoice("left", icon = "align-left"), ToggleGroupChoice("center", icon = "align-center"), ToggleGroupChoice("right", icon = "align-right")),
                selected = listOf("left"),
                variant = ToggleVariant.OUTLINE,
                onChange = changed,
            )
            toggleGroup(
                "styles",
                listOf(ToggleGroupChoice("b", Component.text("F")), ToggleGroupChoice("i", Component.text("K")), ToggleGroupChoice("u", Component.text("U"), enabled = false)),
                multiple = true,
                spacing = 2,
                onChange = changed,
            )
        }
    }

    /**
     * Adds the text inputs, textareas, input groups and one-time code inputs.
     *
     * @param clicked the handler that reports clicks
     * @param changed the handler that reports changes
     */
    private fun ElementsBuilder.texts(clicked: ButtonHandler, changed: ChangeHandler) = section("texts", "Texteingaben") {
        row("text_row", gap = 4, width = ElementSize.grow()) {
            textInput("text_plain", placeholder = Component.text("Text"), width = ElementSize.grow(), onChange = changed)
            textInput("text_password", placeholder = Component.text("Passwort"), type = TextInputType.PASSWORD, width = ElementSize.grow())
            textInput("text_email", placeholder = Component.text("E-Mail"), type = TextInputType.EMAIL, width = ElementSize.grow(), onChange = changed)
        }
        row("label_row", gap = 6, crossAlign = Alignment.CENTER) {
            label("text_label", Component.text("Rufname"), forId = "text_labelled")
            textInput("text_labelled", placeholder = Component.text("Klick auf das Label"), required = true)
        }
        textarea("textarea", placeholder = Component.text("Erzähl etwas über dich"), rows = 3, maxLength = 200, width = ElementSize.grow(), onChange = changed)
        inputGroup("search_group", width = ElementSize.grow()) {
            inputGroupAddon("search_start") { icon("search_icon", "search", size = 10) }
            textInput("search_query", placeholder = Component.text("Suchen …"))
            inputGroupAddon("search_end", InputGroupAlign.INLINE_END) {
                inputGroupText("search_count", Component.text("12 Treffer"))
                inputGroupButton("search_clear", icon = "x", size = ButtonSize.ICON_XS, onClick = clicked)
            }
        }
        inputGroup("message_group", width = ElementSize.grow()) {
            textarea("message", placeholder = Component.text("Nachricht"), rows = 2)
            inputGroupAddon("message_toolbar", InputGroupAlign.BLOCK_END) {
                inputGroupText("message_hint", Component.text("Markdown erlaubt"))
                inputGroupButton("message_send", Component.text("Senden"), icon = "send", variant = ButtonVariant.DEFAULT, onClick = clicked)
            }
        }
        row("otp_row", gap = 8, crossAlign = Alignment.CENTER) {
            inputOtp("otp_digits", length = 6, groups = listOf(3, 3), onChange = changed)
            inputOtp("otp_letters", length = 4, pattern = OtpPattern.ALPHANUMERIC, onChange = changed)
        }
    }

    /**
     * Adds the switches, checkboxes, radio groups and sliders.
     *
     * @param changed the handler that reports changes
     */
    private fun ElementsBuilder.choices(changed: ChangeHandler) = section("choices", "Auswahl") {
        row("switch_row", gap = 6, crossAlign = Alignment.CENTER) {
            switch("switch_default", checked = true, onChange = changed)
            label("switch_label", Component.text("Benachrichtigungen"), forId = "switch_default")
            switch("switch_small", size = SwitchSize.SM, onChange = changed)
            switch("switch_disabled", enabled = false)
            checkbox("checkbox", Component.text("Regeln akzeptiert"), onChange = changed)
            checkbox("checkbox_disabled", Component.text("Gesperrt"), checked = true, enabled = false)
        }
        row("radio_row", gap = 16) {
            radioGroup(
                "radio_vertical",
                listOf(RadioChoice("free", Component.text("Kostenlos")), RadioChoice("pro", Component.text("Pro")), RadioChoice("team", Component.text("Team"), enabled = false)),
                selected = "free",
                onChange = changed,
            )
            radioGroup(
                "radio_horizontal",
                listOf(RadioChoice("small", Component.text("S")), RadioChoice("medium", Component.text("M")), RadioChoice("large", Component.text("L"))),
                orientation = Orientation.HORIZONTAL,
                required = true,
                onChange = changed,
            )
        }
        row("slider_row", gap = 12, crossAlign = Alignment.CENTER, width = ElementSize.grow()) {
            column("slider_column", gap = 8, width = ElementSize.grow(), crossAlign = Alignment.STRETCH) {
                slider("slider_single", listOf(40.0), step = 5.0, onChange = changed)
                slider("slider_range", listOf(20.0, 80.0), onChange = changed)
                slider("slider_fine", listOf(0.5), min = 0.0, max = 1.0, step = 0.1, enabled = false)
            }
            slider("slider_vertical", listOf(30.0), orientation = Orientation.VERTICAL, onChange = changed)
        }
    }

    /**
     * Adds the selects, native selects and comboboxes.
     *
     * @param report sends a report of a search to the player
     * @param changed the handler that reports changes
     */
    private fun ElementsBuilder.selections(report: (Component) -> Unit, changed: ChangeHandler) = section("selections", "Auswahllisten") {
        row("select_row", gap = 4) {
            select("select_grouped", groups = FOOD, placeholder = Component.text("Essen wählen"), onChange = changed)
            select("select_small", CITIES, selected = "north", size = SelectSize.SM, onChange = changed)
            nativeSelect("native", CITIES, selected = "south", onChange = changed)
        }
        row("combobox_row", gap = 4) {
            combobox("combobox_single", groups = FOOD, placeholder = Component.text("Suchen …"), showClear = true, onChange = changed)
            combobox(
                "combobox_multiple",
                CITIES,
                selected = listOf("north"),
                multiple = true,
                placeholder = Component.text("Städte"),
                onSearch = { search ->
                    report(Component.text("Suche: \"${search.query}\"", NamedTextColor.DARK_AQUA))
                },
                onChange = changed,
            )
        }
    }

    /**
     * Adds the calendars.
     *
     * @param changed the handler that reports changes
     */
    private fun ElementsBuilder.calendars(changed: ChangeHandler) = section("calendars", "Kalender") {
        val today = LocalDate.now()
        row("calendar_row", gap = 8) {
            calendar("calendar_single", selected = listOf(today), disabled = setOf(today.plusDays(2)), onChange = changed)
            calendar(
                "calendar_range",
                CalendarMode.RANGE,
                selected = listOf(today.plusDays(3), today.plusDays(7)),
                min = today,
                max = today.plusMonths(6),
                showOutsideDays = false,
                captionLayout = CaptionLayout.DROPDOWN,
                onChange = changed,
            )
        }
    }

    /**
     * Adds a form whose submit rejects the name "Max" with a field error.
     *
     * @param report sends the result to the player
     */
    private fun ElementsBuilder.forms(report: (Component) -> Unit) = section("forms", "Formular") {
        form("account_form", submitId = "account_send", width = ElementSize.grow()) {
            fieldSet("account_set") {
                fieldLegend("account_legend", Component.text("Konto"))
                fieldGroup("account_group") {
                    field("account_name_field") {
                        fieldLabel("account_name_label", Component.text("Name"), forId = "account_name")
                        textInput("account_name", placeholder = Component.text("Max"), required = true, width = ElementSize.grow())
                        fieldDescription("account_name_hint", Component.text("\"Max\" ist schon vergeben."))
                        fieldError("account_name_error")
                    }
                    fieldSeparator("account_separator", Component.text("Optionen"))
                    field("account_news_field", orientation = Orientation.HORIZONTAL) {
                        fieldContent("account_news_content") {
                            fieldLabel("account_news_label", Component.text("Newsletter"), forId = "account_news")
                            fieldDescription("account_news_hint", Component.text("Einmal pro Woche"))
                        }
                        switch("account_news")
                    }
                }
            }
            row("account_actions", gap = 4, mainAlign = Alignment.END, width = ElementSize.grow()) {
                button("account_send", Component.text("Speichern"), submitsInput = false, icon = "check") { click ->
                    if (click.values.text("account_name") == "Max") {
                        click.fail(mapOf("account_name" to Component.text("Dieser Name ist vergeben.")))
                    } else {
                        click.fail(emptyMap())
                        report(Component.text("Konto gespeichert: ${click.values.all}", NamedTextColor.GREEN))
                    }
                }
            }
        }
    }

    /**
     * The grouped options of the food selects.
     */
    private val FOOD = listOf(
        SelectChoiceGroup(
            Component.text("Obst"),
            listOf(SelectChoice("apple", Component.text("Apfel")), SelectChoice("pear", Component.text("Birne"), enabled = false), SelectChoice("plum", Component.text("Pflaume"))),
        ),
        SelectChoiceGroup(Component.text("Gemüse"), listOf(SelectChoice("carrot", Component.text("Möhre")), SelectChoice("pea", Component.text("Erbse")))),
    )

    /**
     * The themes offered by the theme select.
     */
    private val THEMES = listOf(
        SelectChoice(ScreenThemes.DEFAULT, Component.text("Standard")),
        SelectChoice(ScreenThemes.SAR, Component.text("Rettungsdienst")),
        SelectChoice(ScreenThemes.POLICE, Component.text("Polizei")),
    )

    /**
     * The variants offered by the variant select.
     */
    private val VARIANTS = listOf(
        SelectChoice(ScreenVariant.DARK.name, Component.text("Dunkel")),
        SelectChoice(ScreenVariant.LIGHT.name, Component.text("Hell")),
    )

    /**
     * The options of the city selects.
     */
    private val CITIES = listOf(
        SelectChoice("north", Component.text("Nordhafen")),
        SelectChoice("south", Component.text("Südstadt")),
        SelectChoice("old", Component.text("Altstadt")),
    )
}
