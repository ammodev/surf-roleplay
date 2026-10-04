package dev.slne.surf.roleplay.paper.screen.debug

import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ButtonGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ButtonGroupSeparator
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ButtonGroupText
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Calendar
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Checkbox
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Combobox
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MultiCombobox
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Field
import dev.slne.surf.roleplay.api.client.common.screen.dsl.FieldContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.FieldDescription
import dev.slne.surf.roleplay.api.client.common.screen.dsl.FieldError
import dev.slne.surf.roleplay.api.client.common.screen.dsl.FieldGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.FieldLabel
import dev.slne.surf.roleplay.api.client.common.screen.dsl.FieldLegend
import dev.slne.surf.roleplay.api.client.common.screen.dsl.FieldSeparator
import dev.slne.surf.roleplay.api.client.common.screen.dsl.FieldSet
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Form
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Icon
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Input
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputGroupAddon
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputGroupButton
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputGroupText
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputOtp
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Label
import dev.slne.surf.roleplay.api.client.common.screen.dsl.NativeSelect
import dev.slne.surf.roleplay.api.client.common.screen.dsl.RadioGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Row
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Select
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Slider
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Switch
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Textarea
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Toggle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ToggleGroup
import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.ButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.CalendarMode
import dev.slne.surf.roleplay.api.client.common.screen.CaptionLayout
import dev.slne.surf.roleplay.api.client.common.screen.ChangeHandler
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
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
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Screen
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
        return Screen(Component.text("Eingaben"), theme = theme, variant = variant) {
            Column(width = ElementSize.fixed(380), gap = 10, crossAlign = Alignment.STRETCH, id = "root") {
                Row(gap = 4, crossAlign = Alignment.CENTER, id = "theme_row") {
                    Select(THEMES, selected = theme, width = ElementSize.grow(), id = "theme")
                    Select(VARIANTS, selected = variant.name, width = ElementSize.fixed(90), id = "variant")
                    Button(Component.text("Anwenden"), submitsInput = false, icon = "palette", variant = ButtonVariant.OUTLINE, id = "apply_theme") { click ->
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
    private fun ComponentScope.section(id: String, title: String, content: ComponentScope.() -> Unit) {
        Column(gap = 4, crossAlign = Alignment.START, id = id) {
            Label(Component.text(title, NamedTextColor.GOLD, TextDecoration.BOLD), id = "${id}_title")
            content()
        }
    }

    /**
     * Adds the buttons, in every variant and size, and the button groups.
     *
     * @param clicked the handler that reports clicks
     */
    private fun ComponentScope.buttons(clicked: ButtonHandler) = section("buttons", "Schaltflächen") {
        Row(gap = 4, id = "button_variants") {
            ButtonVariant.entries.forEach { variant ->
                Button(Component.text(variant.name.lowercase().replaceFirstChar(Char::uppercase)), submitsInput = false, variant = variant, onClick = clicked, id = "button_${variant.name.lowercase()}")
            }
        }
        Row(gap = 4, crossAlign = Alignment.CENTER, id = "button_sizes") {
            listOf(ButtonSize.XS, ButtonSize.SM, ButtonSize.DEFAULT, ButtonSize.LG).forEach { size ->
                Button(Component.text(size.name), submitsInput = false, icon = "send", size = size, onClick = clicked, id = "button_size_${size.name.lowercase()}")
            }
            listOf(ButtonSize.ICON_XS, ButtonSize.ICON_SM, ButtonSize.ICON, ButtonSize.ICON_LG).forEach { size ->
                Button(Component.empty(), submitsInput = false, icon = "plus", variant = ButtonVariant.OUTLINE, size = size, onClick = clicked, id = "button_size_${size.name.lowercase()}")
            }
            Button(Component.text("Gesperrt"), enabled = false, icon = "lock", id = "button_disabled")
        }
        Row(gap = 8, id = "button_groups") {
            ButtonGroup(id = "group_horizontal") {
                ButtonGroupText(Component.text("Seite"), icon = "file", id = "group_text")
                Button(Component.empty(), submitsInput = false, icon = "chevron-left", variant = ButtonVariant.OUTLINE, size = ButtonSize.ICON, onClick = clicked, id = "group_back")
                ButtonGroupSeparator(id = "group_separator")
                Button(Component.empty(), submitsInput = false, icon = "chevron-right", variant = ButtonVariant.OUTLINE, size = ButtonSize.ICON, onClick = clicked, id = "group_next")
            }
            ButtonGroup(orientation = Orientation.VERTICAL, id = "group_vertical") {
                Button(Component.text("Hoch"), submitsInput = false, variant = ButtonVariant.SECONDARY, size = ButtonSize.SM, onClick = clicked, id = "group_up")
                Button(Component.text("Runter"), submitsInput = false, variant = ButtonVariant.SECONDARY, size = ButtonSize.SM, onClick = clicked, id = "group_down")
            }
        }
    }

    /**
     * Adds the toggles and toggle groups.
     *
     * @param clicked the handler that reports toggle presses
     * @param changed the handler that reports changes
     */
    private fun ComponentScope.toggles(clicked: ButtonHandler, changed: ChangeHandler) = section("toggles", "Umschalter") {
        Row(gap = 4, crossAlign = Alignment.CENTER, id = "toggle_row") {
            Toggle(icon = "bold", onToggle = clicked, id = "toggle_bold")
            Toggle(icon = "italic", variant = ToggleVariant.OUTLINE, pressed = true, onToggle = clicked, id = "toggle_italic")
            Toggle(Component.text("Klein"), size = ToggleSize.SM, onToggle = clicked, id = "toggle_sm")
            Toggle(Component.text("Groß"), size = ToggleSize.LG, variant = ToggleVariant.OUTLINE, onToggle = clicked, id = "toggle_lg")
            Toggle(Component.text("Aus"), enabled = false, id = "toggle_disabled")
        }
        Row(gap = 8, id = "toggle_groups") {
            ToggleGroup(listOf(ToggleGroupChoice("left", icon = "align-left"), ToggleGroupChoice("center", icon = "align-center"), ToggleGroupChoice("right", icon = "align-right")), selected = listOf("left"), variant = ToggleVariant.OUTLINE, onChange = changed, id = "align")
            ToggleGroup(listOf(ToggleGroupChoice("b", Component.text("F")), ToggleGroupChoice("i", Component.text("K")), ToggleGroupChoice("u", Component.text("U"), enabled = false)), multiple = true, spacing = 2, onChange = changed, id = "styles")
        }
    }

    /**
     * Adds the text inputs, textareas, input groups and one-time code inputs.
     *
     * @param clicked the handler that reports clicks
     * @param changed the handler that reports changes
     */
    private fun ComponentScope.texts(clicked: ButtonHandler, changed: ChangeHandler) = section("texts", "Texteingaben") {
        Row(gap = 4, width = ElementSize.grow(), id = "text_row") {
            Input(placeholder = Component.text("Text"), width = ElementSize.grow(), onChange = changed, id = "text_plain")
            Input(placeholder = Component.text("Passwort"), type = TextInputType.PASSWORD, width = ElementSize.grow(), id = "text_password")
            Input(placeholder = Component.text("E-Mail"), type = TextInputType.EMAIL, width = ElementSize.grow(), onChange = changed, id = "text_email")
        }
        Row(gap = 6, crossAlign = Alignment.CENTER, id = "label_row") {
            Label(Component.text("Rufname"), forId = "text_labelled", id = "text_label")
            Input(placeholder = Component.text("Klick auf das Label"), required = true, id = "text_labelled")
        }
        Textarea(placeholder = Component.text("Erzähl etwas über dich"), rows = 3, maxLength = 200, width = ElementSize.grow(), onChange = changed, id = "textarea")
        InputGroup(width = ElementSize.grow(), id = "search_group") {
            InputGroupAddon(id = "search_start") { Icon("search", size = 10, id = "search_icon") }
            Input(placeholder = Component.text("Suchen …"), id = "search_query")
            InputGroupAddon(InputGroupAlign.INLINE_END, id = "search_end") {
                InputGroupText(Component.text("12 Treffer"), id = "search_count")
                InputGroupButton(icon = "x", size = ButtonSize.ICON_XS, onClick = clicked, id = "search_clear")
            }
        }
        InputGroup(width = ElementSize.grow(), id = "message_group") {
            Textarea(placeholder = Component.text("Nachricht"), rows = 2, id = "message")
            InputGroupAddon(InputGroupAlign.BLOCK_END, id = "message_toolbar") {
                InputGroupText(Component.text("Markdown erlaubt"), id = "message_hint")
                InputGroupButton(Component.text("Senden"), icon = "send", variant = ButtonVariant.DEFAULT, onClick = clicked, id = "message_send")
            }
        }
        Row(gap = 8, crossAlign = Alignment.CENTER, id = "otp_row") {
            InputOtp(length = 6, groups = listOf(3, 3), onChange = changed, id = "otp_digits")
            InputOtp(length = 4, pattern = OtpPattern.ALPHANUMERIC, onChange = changed, id = "otp_letters")
        }
    }

    /**
     * Adds the switches, checkboxes, radio groups and sliders.
     *
     * @param changed the handler that reports changes
     */
    private fun ComponentScope.choices(changed: ChangeHandler) = section("choices", "Auswahl") {
        Row(gap = 6, crossAlign = Alignment.CENTER, id = "switch_row") {
            Switch(checked = true, onChange = changed, id = "switch_default")
            Label(Component.text("Benachrichtigungen"), forId = "switch_default", id = "switch_label")
            Switch(size = SwitchSize.SM, onChange = changed, id = "switch_small")
            Switch(enabled = false, id = "switch_disabled")
            Checkbox(Component.text("Regeln akzeptiert"), onChange = changed, id = "checkbox")
            Checkbox(Component.text("Gesperrt"), checked = true, enabled = false, id = "checkbox_disabled")
        }
        Row(gap = 16, id = "radio_row") {
            RadioGroup(listOf(RadioChoice("free", Component.text("Kostenlos")), RadioChoice("pro", Component.text("Pro")), RadioChoice("team", Component.text("Team"), enabled = false)), selected = "free", onChange = changed, id = "radio_vertical")
            RadioGroup(listOf(RadioChoice("small", Component.text("S")), RadioChoice("medium", Component.text("M")), RadioChoice("large", Component.text("L"))), orientation = Orientation.HORIZONTAL, required = true, onChange = changed, id = "radio_horizontal")
        }
        Row(gap = 12, crossAlign = Alignment.CENTER, width = ElementSize.grow(), id = "slider_row") {
            Column(gap = 8, width = ElementSize.grow(), crossAlign = Alignment.STRETCH, id = "slider_column") {
                Slider(listOf(40.0), step = 5.0, onChange = changed, id = "slider_single")
                Slider(listOf(20.0, 80.0), onChange = changed, id = "slider_range")
                Slider(listOf(0.5), min = 0.0, max = 1.0, step = 0.1, enabled = false, id = "slider_fine")
            }
            Slider(listOf(30.0), orientation = Orientation.VERTICAL, onChange = changed, id = "slider_vertical")
        }
    }

    /**
     * Adds the selects, native selects and comboboxes.
     *
     * @param report sends a report of a search to the player
     * @param changed the handler that reports changes
     */
    private fun ComponentScope.selections(report: (Component) -> Unit, changed: ChangeHandler) = section("selections", "Auswahllisten") {
        Row(gap = 4, id = "select_row") {
            Select(groups = FOOD, placeholder = Component.text("Essen wählen"), onChange = changed, id = "select_grouped")
            Select(CITIES, selected = "north", size = SelectSize.SM, onChange = changed, id = "select_small")
            NativeSelect(CITIES, selected = "south", onChange = changed, id = "native")
        }
        Row(gap = 4, id = "combobox_row") {
            Combobox(groups = FOOD, placeholder = Component.text("Suchen …"), showClear = true, onChange = changed, id = "combobox_single")
            MultiCombobox(CITIES, selected = listOf("north"), placeholder = Component.text("Städte"), onSearch = { search ->
                    report(Component.text("Suche: \"${search.query}\"", NamedTextColor.DARK_AQUA))
                }, onChange = changed, id = "combobox_multiple")
        }
    }

    /**
     * Adds the calendars.
     *
     * @param changed the handler that reports changes
     */
    private fun ComponentScope.calendars(changed: ChangeHandler) = section("calendars", "Kalender") {
        val today = LocalDate.now()
        Row(gap = 8, id = "calendar_row") {
            Calendar(selected = listOf(today), disabled = setOf(today.plusDays(2)), onChange = changed, id = "calendar_single")
            Calendar(CalendarMode.RANGE, selected = listOf(today.plusDays(3), today.plusDays(7)), min = today, max = today.plusMonths(6), showOutsideDays = false, captionLayout = CaptionLayout.DROPDOWN, onChange = changed, id = "calendar_range")
        }
    }

    /**
     * Adds a form whose submit rejects the name "Max" with a field error.
     *
     * @param report sends the result to the player
     */
    private fun ComponentScope.forms(report: (Component) -> Unit) = section("forms", "Formular") {
        Form(submitId = "account_send", width = ElementSize.grow(), id = "account_form") {
            FieldSet(id = "account_set") {
                FieldLegend(Component.text("Konto"), id = "account_legend")
                FieldGroup(id = "account_group") {
                    Field(id = "account_name_field") {
                        FieldLabel(Component.text("Name"), forId = "account_name", id = "account_name_label")
                        Input(placeholder = Component.text("Max"), required = true, width = ElementSize.grow(), id = "account_name")
                        FieldDescription(Component.text("\"Max\" ist schon vergeben."), id = "account_name_hint")
                        FieldError(id = "account_name_error")
                    }
                    FieldSeparator(Component.text("Optionen"), id = "account_separator")
                    Field(orientation = Orientation.HORIZONTAL, id = "account_news_field") {
                        FieldContent(id = "account_news_content") {
                            FieldLabel(Component.text("Newsletter"), forId = "account_news", id = "account_news_label")
                            FieldDescription(Component.text("Einmal pro Woche"), id = "account_news_hint")
                        }
                        Switch(id = "account_news")
                    }
                }
            }
            Row(gap = 4, mainAlign = Alignment.END, width = ElementSize.grow(), id = "account_actions") {
                Button(Component.text("Speichern"), submitsInput = false, icon = "check", id = "account_send") { click ->
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
    internal val THEMES = listOf(
        SelectChoice(ScreenThemes.DEFAULT, Component.text("Standard")),
        SelectChoice(ScreenThemes.SAR, Component.text("Rettungsdienst")),
        SelectChoice(ScreenThemes.POLICE, Component.text("Polizei")),
    )

    /**
     * The variants offered by the variant select.
     */
    internal val VARIANTS = listOf(
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
