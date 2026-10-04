package dev.slne.surf.roleplay.paper.storybook.stories

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.ButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.CalendarMode
import dev.slne.surf.roleplay.api.client.common.screen.CaptionLayout
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupAlign
import dev.slne.surf.roleplay.api.client.common.screen.Orientation
import dev.slne.surf.roleplay.api.client.common.screen.OtpPattern
import dev.slne.surf.roleplay.api.client.common.screen.RadioChoice
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoice
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoiceGroup
import dev.slne.surf.roleplay.api.client.common.screen.SelectSize
import dev.slne.surf.roleplay.api.client.common.screen.SwitchSize
import dev.slne.surf.roleplay.api.client.common.screen.TextInputType
import dev.slne.surf.roleplay.api.client.common.screen.ToggleGroupChoice
import dev.slne.surf.roleplay.api.client.common.screen.ToggleSize
import dev.slne.surf.roleplay.api.client.common.screen.ToggleVariant
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ButtonGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ButtonGroupSeparator
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ButtonGroupText
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Calendar
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Checkbox
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Combobox
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Field
import dev.slne.surf.roleplay.api.client.common.screen.dsl.FieldContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.FieldDescription
import dev.slne.surf.roleplay.api.client.common.screen.dsl.FieldError
import dev.slne.surf.roleplay.api.client.common.screen.dsl.FieldGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.FieldLabel
import dev.slne.surf.roleplay.api.client.common.screen.dsl.FieldLegend
import dev.slne.surf.roleplay.api.client.common.screen.dsl.FieldSeparator
import dev.slne.surf.roleplay.api.client.common.screen.dsl.FieldSet
import dev.slne.surf.roleplay.api.client.common.screen.dsl.FieldTitle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Form
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Icon
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Input
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputGroupAddon
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputGroupButton
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputGroupText
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputOtp
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Label
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MultiCombobox
import dev.slne.surf.roleplay.api.client.common.screen.dsl.NativeSelect
import dev.slne.surf.roleplay.api.client.common.screen.dsl.NumberInput
import dev.slne.surf.roleplay.api.client.common.screen.dsl.RadioGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Row
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Select
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Slider
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Switch
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Textarea
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Toggle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ToggleGroup
import dev.slne.surf.roleplay.paper.storybook.Story
import dev.slne.surf.roleplay.paper.storybook.StoryCategory
import dev.slne.surf.roleplay.paper.storybook.StoryContext
import dev.slne.surf.roleplay.paper.storybook.slug
import dev.slne.surf.roleplay.paper.storybook.storySection
import net.kyori.adventure.text.Component
import java.time.LocalDate

/**
 * The stories of the input components, in sidebar order.
 */
internal val INPUT_STORIES: List<Story> = listOf(
    Story("button", "Schaltfläche", StoryCategory.INPUTS) { buttonStory(it) },
    Story("button-group", "Schaltflächengruppe", StoryCategory.INPUTS) { buttonGroupStory(it) },
    Story("toggle", "Umschalter", StoryCategory.INPUTS) { toggleStory(it) },
    Story("toggle-group", "Umschaltgruppe", StoryCategory.INPUTS) { toggleGroupStory(it) },
    Story("input", "Eingabefeld", StoryCategory.INPUTS) { inputStory(it) },
    Story("textarea", "Textbereich", StoryCategory.INPUTS) { textareaStory(it) },
    Story("input-group", "Eingabegruppe", StoryCategory.INPUTS) { inputGroupStory(it) },
    Story("input-otp", "Einmalcode", StoryCategory.INPUTS) { inputOtpStory(it) },
    Story("label", "Beschriftung", StoryCategory.INPUTS) { labelStory(it) },
    Story("checkbox", "Kontrollkästchen", StoryCategory.INPUTS) { checkboxStory(it) },
    Story("switch", "Schalter", StoryCategory.INPUTS) { switchStory(it) },
    Story("radio-group", "Optionsgruppe", StoryCategory.INPUTS) { radioGroupStory(it) },
    Story("slider", "Schieberegler", StoryCategory.INPUTS) { sliderStory(it) },
    Story("select", "Auswahl", StoryCategory.INPUTS) { selectStory(it) },
    Story("native-select", "Native Auswahl", StoryCategory.INPUTS) { nativeSelectStory(it) },
    Story("combobox", "Combobox", StoryCategory.INPUTS) { comboboxStory(it) },
    Story("calendar", "Kalender", StoryCategory.INPUTS) { calendarStory(it) },
    Story("field", "Feld", StoryCategory.INPUTS) { fieldStory(it) },
    Story("form", "Formular", StoryCategory.INPUTS) { formStory(it) },
)

/**
 * The cities offered by the select stories.
 */
private val CITIES = listOf(
    SelectChoice("north", Component.text("Nordhafen")),
    SelectChoice("south", Component.text("Südstadt")),
    SelectChoice("old", Component.text("Altstadt")),
    SelectChoice("harbour", Component.text("Hafenviertel (gesperrt)"), enabled = false),
)

/**
 * The grouped units offered by the select and combobox stories.
 */
private val UNITS = listOf(
    SelectChoiceGroup(
        Component.text("Rettungsdienst"),
        listOf(SelectChoice("rtw", Component.text("Rettungswagen")), SelectChoice("nef", Component.text("Notarzt")), SelectChoice("ktw", Component.text("Krankentransport"), enabled = false)),
    ),
    SelectChoiceGroup(Component.text("Polizei"), listOf(SelectChoice("patrol", Component.text("Streife")), SelectChoice("k9", Component.text("Hundestaffel")))),
)

/**
 * Shows buttons in every variant and every size, disabled, and a realistic action row.
 *
 * @param context the story context
 */
private fun ComponentScope.buttonStory(context: StoryContext) {
    storySection("Varianten") {
        Row(gap = 4, crossAlign = Alignment.CENTER) {
            ButtonVariant.entries.forEach { variant ->
                Button(variant.slug(), submitsInput = false, variant = variant, id = "button_${variant.slug()}", onClick = context.clicked)
            }
        }
    }
    storySection("Größen je Variante") {
        ButtonVariant.entries.forEach { variant ->
            Row(gap = 4, crossAlign = Alignment.CENTER) {
                ButtonSize.entries.forEach { size ->
                    val iconOnly = size.name.startsWith("ICON")
                    Button(
                        if (iconOnly) Component.empty() else Component.text(size.slug()),
                        submitsInput = false,
                        icon = "plus",
                        variant = variant,
                        size = size,
                        id = "button_${variant.slug()}_${size.slug()}",
                        onClick = context.clicked,
                    )
                }
            }
        }
    }
    storySection("Gesperrt") {
        Row(gap = 4) {
            ButtonVariant.entries.forEach { variant ->
                Button(variant.slug(), enabled = false, variant = variant, id = "button_disabled_${variant.slug()}")
            }
        }
    }
    storySection("Beispiel") {
        Row(gap = 4) {
            Button("Akte speichern", submitsInput = false, icon = "save", id = "button_save", onClick = context.clicked)
            Button("Verwerfen", submitsInput = false, variant = ButtonVariant.OUTLINE, id = "button_discard", onClick = context.clicked)
            Button("Akte löschen", submitsInput = false, icon = "trash", variant = ButtonVariant.DESTRUCTIVE, id = "button_delete", onClick = context.clicked)
        }
    }
}

/**
 * Shows button groups in both orientations, with text and separators.
 *
 * @param context the story context
 */
private fun ComponentScope.buttonGroupStory(context: StoryContext) {
    storySection("Ausrichtungen") {
        Row(gap = 12) {
            Orientation.entries.forEach { orientation ->
                ButtonGroup(orientation = orientation, id = "group_${orientation.slug()}") {
                    Button("Eins", submitsInput = false, variant = ButtonVariant.OUTLINE, id = "group_${orientation.slug()}_1", onClick = context.clicked)
                    Button("Zwei", submitsInput = false, variant = ButtonVariant.OUTLINE, id = "group_${orientation.slug()}_2", onClick = context.clicked)
                    Button("Drei", submitsInput = false, variant = ButtonVariant.OUTLINE, id = "group_${orientation.slug()}_3", onClick = context.clicked)
                }
            }
        }
    }
    storySection("Mit Text und Trenner") {
        ButtonGroup(id = "group_pager") {
            ButtonGroupText("Seite 3 von 12", icon = "file", id = "group_pager_text")
            Button(Component.empty(), submitsInput = false, icon = "chevron-left", variant = ButtonVariant.OUTLINE, size = ButtonSize.ICON, id = "group_pager_back", onClick = context.clicked)
            ButtonGroupSeparator()
            Button(Component.empty(), submitsInput = false, icon = "chevron-right", variant = ButtonVariant.OUTLINE, size = ButtonSize.ICON, id = "group_pager_next", onClick = context.clicked)
        }
    }
    storySection("Beispiel") {
        ButtonGroup(id = "group_radio") {
            Button("Funk", submitsInput = false, icon = "radio", variant = ButtonVariant.SECONDARY, id = "group_radio_talk", onClick = context.clicked)
            Button("Stumm", submitsInput = false, icon = "volume-x", variant = ButtonVariant.SECONDARY, id = "group_radio_mute", onClick = context.clicked)
            Button("Notruf", submitsInput = false, icon = "siren", variant = ButtonVariant.DESTRUCTIVE, id = "group_radio_alarm", onClick = context.clicked)
        }
    }
}

/**
 * Shows toggles in every variant and size, pressed and disabled.
 *
 * @param context the story context
 */
private fun ComponentScope.toggleStory(context: StoryContext) {
    storySection("Varianten und Größen") {
        ToggleVariant.entries.forEach { variant ->
            Row(gap = 4, crossAlign = Alignment.CENTER) {
                ToggleSize.entries.forEach { size ->
                    Toggle(size.slug(), icon = "bookmark", variant = variant, size = size, id = "toggle_${variant.slug()}_${size.slug()}", onToggle = context.clicked)
                }
                Toggle(icon = "bold", variant = variant, pressed = true, id = "toggle_${variant.slug()}_pressed", onToggle = context.clicked)
                Toggle("Gesperrt", variant = variant, enabled = false, id = "toggle_${variant.slug()}_disabled")
            }
        }
    }
    storySection("Beispiel") {
        Row(gap = 4) {
            Toggle(icon = "bold", id = "toggle_bold", onToggle = context.clicked)
            Toggle(icon = "italic", id = "toggle_italic", onToggle = context.clicked)
            Toggle(icon = "underline", id = "toggle_underline", onToggle = context.clicked)
        }
    }
}

/**
 * Shows toggle groups in every variant and size, single and multiple, vertical and disabled.
 *
 * @param context the story context
 */
private fun ComponentScope.toggleGroupStory(context: StoryContext) {
    val alignments = listOf(ToggleGroupChoice("left", icon = "align-left"), ToggleGroupChoice("center", icon = "align-center"), ToggleGroupChoice("right", icon = "align-right"))
    storySection("Varianten und Größen") {
        ToggleVariant.entries.forEach { variant ->
            Row(gap = 8, crossAlign = Alignment.CENTER) {
                ToggleSize.entries.forEach { size ->
                    ToggleGroup(alignments, selected = listOf("left"), variant = variant, size = size, id = "toggle_group_${variant.slug()}_${size.slug()}", onChange = context.changed)
                }
            }
        }
    }
    storySection("Mehrfach, mit Abstand, senkrecht und gesperrt") {
        Row(gap = 8) {
            ToggleGroup(
                listOf(ToggleGroupChoice("b", Component.text("F")), ToggleGroupChoice("i", Component.text("K")), ToggleGroupChoice("u", Component.text("U"), enabled = false)),
                multiple = true,
                spacing = 2,
                id = "toggle_group_multiple",
                onChange = context.changed,
            )
            ToggleGroup(alignments, orientation = Orientation.VERTICAL, variant = ToggleVariant.OUTLINE, id = "toggle_group_vertical", onChange = context.changed)
            ToggleGroup(alignments, selected = listOf("center"), enabled = false, id = "toggle_group_disabled")
        }
    }
    storySection("Beispiel") {
        ToggleGroup(
            listOf(ToggleGroupChoice("day", Component.text("Tag")), ToggleGroupChoice("week", Component.text("Woche")), ToggleGroupChoice("month", Component.text("Monat"))),
            selected = listOf("week"),
            variant = ToggleVariant.OUTLINE,
            required = true,
            id = "toggle_group_range",
            onChange = context.changed,
        )
    }
}

/**
 * Shows text inputs of every type, with an icon, required, disabled, and number inputs.
 *
 * @param context the story context
 */
private fun ComponentScope.inputStory(context: StoryContext) {
    storySection("Typen") {
        TextInputType.entries.forEach { type ->
            Input(placeholder = Component.text(type.slug()), type = type, width = ElementSize.fixed(200), id = "input_${type.slug()}", onChange = context.changed)
        }
    }
    storySection("Zustände") {
        Input(placeholder = Component.text("Mit Symbol"), icon = "search", width = ElementSize.fixed(200), id = "input_icon", onChange = context.changed)
        Input(placeholder = Component.text("Pflichtfeld"), required = true, width = ElementSize.fixed(200), id = "input_required", onChange = context.changed)
        Input(value = "Gesperrt", enabled = false, width = ElementSize.fixed(200), id = "input_disabled")
        NumberInput(value = 25, min = 18, max = 99, width = ElementSize.fixed(200), id = "input_number", onChange = context.changed)
        NumberInput(value = 3, enabled = false, width = ElementSize.fixed(200), id = "input_number_disabled")
    }
    storySection("Beispiel") {
        Input(placeholder = Component.text("Kennzeichen, z. B. NH-RP 112"), maxLength = 12, icon = "car", width = ElementSize.fixed(240), id = "input_plate", onChange = context.changed)
    }
}

/**
 * Shows textareas with different row counts, required and disabled.
 *
 * @param context the story context
 */
private fun ComponentScope.textareaStory(context: StoryContext) {
    storySection("Zeilen") {
        listOf(2, 4).forEach { rows ->
            Textarea(placeholder = Component.text("$rows Zeilen"), rows = rows, width = ElementSize.grow(), id = "textarea_$rows", onChange = context.changed)
        }
    }
    storySection("Zustände") {
        Textarea(placeholder = Component.text("Pflichtfeld"), required = true, width = ElementSize.grow(), id = "textarea_required", onChange = context.changed)
        Textarea(value = "Gesperrter Text", enabled = false, width = ElementSize.grow(), id = "textarea_disabled")
    }
    storySection("Beispiel") {
        Textarea(placeholder = Component.text("Beschreibe den Einsatzverlauf …"), rows = 4, maxLength = 500, width = ElementSize.grow(), id = "textarea_report", onChange = context.changed)
    }
}

/**
 * Shows input groups with an addon on every side, texts and buttons.
 *
 * @param context the story context
 */
private fun ComponentScope.inputGroupStory(context: StoryContext) {
    storySection("Ausrichtungen der Zusätze") {
        InputGroupAlign.entries.forEach { align ->
            InputGroup(width = ElementSize.grow(), id = "input_group_${align.slug()}") {
                if (align == InputGroupAlign.BLOCK_START || align == InputGroupAlign.BLOCK_END) {
                    Textarea(placeholder = Component.text(align.slug()), rows = 2, id = "input_group_${align.slug()}_field")
                } else {
                    Input(placeholder = Component.text(align.slug()), id = "input_group_${align.slug()}_field")
                }
                InputGroupAddon(align) {
                    InputGroupText(align.slug())
                    InputGroupButton(icon = "x", size = ButtonSize.ICON_XS, id = "input_group_${align.slug()}_button", onClick = context.clicked)
                }
            }
        }
    }
    storySection("Beispiel") {
        InputGroup(width = ElementSize.grow(), id = "input_group_search") {
            InputGroupAddon { Icon("search", size = 10) }
            Input(placeholder = Component.text("Personen suchen …"), id = "input_group_search_query", onChange = context.changed)
            InputGroupAddon(InputGroupAlign.INLINE_END) {
                InputGroupText("12 Treffer")
                InputGroupButton("Suchen", variant = ButtonVariant.DEFAULT, id = "input_group_search_send", onClick = context.clicked)
            }
        }
    }
}

/**
 * Shows one-time code inputs with every pattern, groups, and disabled.
 *
 * @param context the story context
 */
private fun ComponentScope.inputOtpStory(context: StoryContext) {
    storySection("Muster") {
        OtpPattern.entries.forEach { pattern ->
            InputOtp(length = 6, pattern = pattern, id = "otp_${pattern.slug()}", onChange = context.changed)
        }
    }
    storySection("Gruppen und gesperrt") {
        InputOtp(length = 6, groups = listOf(3, 3), id = "otp_groups", onChange = context.changed)
        InputOtp(length = 4, value = "1234", enabled = false, id = "otp_disabled")
    }
    storySection("Beispiel") {
        Label("Bestätigungscode aus der SMS", forId = "otp_sms")
        InputOtp(length = 6, groups = listOf(3, 3), required = true, id = "otp_sms", onChange = context.changed)
    }
}

/**
 * Shows labels with an icon and labels bound to inputs.
 *
 * @param context the story context
 */
private fun ComponentScope.labelStory(context: StoryContext) {
    storySection("Varianten") {
        Label("Nur Text")
        Label("Mit Symbol", icon = "info")
    }
    storySection("Beispiel") {
        Row(gap = 6, crossAlign = Alignment.CENTER) {
            Checkbox(id = "label_terms", onChange = context.changed)
            Label("Ich akzeptiere die Dienstvorschriften", forId = "label_terms")
        }
    }
}

/**
 * Shows checkboxes unchecked, checked and disabled.
 *
 * @param context the story context
 */
private fun ComponentScope.checkboxStory(context: StoryContext) {
    storySection("Zustände") {
        Checkbox("Nicht angehakt", id = "checkbox_off", onChange = context.changed)
        Checkbox("Angehakt", checked = true, id = "checkbox_on", onChange = context.changed)
        Checkbox("Gesperrt", enabled = false, id = "checkbox_disabled")
        Checkbox("Gesperrt und angehakt", checked = true, enabled = false, id = "checkbox_disabled_on")
    }
    storySection("Beispiel") {
        Checkbox("Benachrichtigungen bei neuen Einsätzen", checked = true, id = "checkbox_notify", onChange = context.changed)
        Checkbox("Funkverkehr aufzeichnen", id = "checkbox_record", onChange = context.changed)
    }
}

/**
 * Shows switches in every size, on, off and disabled.
 *
 * @param context the story context
 */
private fun ComponentScope.switchStory(context: StoryContext) {
    storySection("Größen und Zustände") {
        SwitchSize.entries.forEach { size ->
            Row(gap = 8, crossAlign = Alignment.CENTER) {
                Switch(size = size, id = "switch_${size.slug()}_off", onChange = context.changed)
                Switch(checked = true, size = size, id = "switch_${size.slug()}_on", onChange = context.changed)
                Switch(size = size, enabled = false, id = "switch_${size.slug()}_disabled")
                Label(size.slug())
            }
        }
    }
    storySection("Beispiel") {
        Row(gap = 6, crossAlign = Alignment.CENTER) {
            Switch(checked = true, id = "switch_duty", onChange = context.changed)
            Label("Im Dienst", forId = "switch_duty")
        }
    }
}

/**
 * Shows radio groups in both orientations, required and with a disabled option.
 *
 * @param context the story context
 */
private fun ComponentScope.radioGroupStory(context: StoryContext) {
    val sizes = listOf(RadioChoice("small", Component.text("S")), RadioChoice("medium", Component.text("M")), RadioChoice("large", Component.text("L")))
    storySection("Ausrichtungen") {
        Row(gap = 16) {
            Orientation.entries.forEach { orientation ->
                RadioGroup(sizes, selected = "medium", orientation = orientation, id = "radio_${orientation.slug()}", onChange = context.changed)
            }
        }
    }
    storySection("Pflicht und gesperrt") {
        Row(gap = 16) {
            RadioGroup(sizes, required = true, id = "radio_required", onChange = context.changed)
            RadioGroup(sizes, selected = "small", enabled = false, id = "radio_disabled")
        }
    }
    storySection("Beispiel") {
        RadioGroup(
            listOf(RadioChoice("free", Component.text("Kostenlos")), RadioChoice("pro", Component.text("Pro")), RadioChoice("team", Component.text("Team"), enabled = false)),
            selected = "free",
            id = "radio_plan",
            onChange = context.changed,
        )
    }
}

/**
 * Shows sliders with one and two thumbs, steps, both orientations and disabled.
 *
 * @param context the story context
 */
private fun ComponentScope.sliderStory(context: StoryContext) {
    storySection("Varianten") {
        Row(gap = 12, width = ElementSize.grow()) {
            Column(gap = 8, width = ElementSize.grow(), crossAlign = Alignment.STRETCH) {
                Slider(listOf(40.0), step = 5.0, id = "slider_single", onChange = context.changed)
                Slider(listOf(20.0, 80.0), id = "slider_range", onChange = context.changed)
                Slider(listOf(0.5), min = 0.0, max = 1.0, step = 0.1, enabled = false, id = "slider_disabled")
            }
            Slider(listOf(30.0), orientation = Orientation.VERTICAL, height = ElementSize.fixed(80), id = "slider_vertical", onChange = context.changed)
        }
    }
    storySection("Beispiel") {
        Label("Lautstärke des Funkgeräts")
        Slider(listOf(70.0), step = 10.0, width = ElementSize.grow(), id = "slider_volume", onChange = context.changed)
    }
}

/**
 * Shows selects in every size, with groups, required and disabled.
 *
 * @param context the story context
 */
private fun ComponentScope.selectStory(context: StoryContext) {
    storySection("Größen") {
        Row(gap = 4) {
            SelectSize.entries.forEach { size ->
                Select(CITIES, selected = "north", size = size, id = "select_${size.slug()}", onChange = context.changed)
            }
        }
    }
    storySection("Gruppen, Pflicht und gesperrt") {
        Row(gap = 4) {
            Select(groups = UNITS, placeholder = Component.text("Einheit wählen"), id = "select_grouped", onChange = context.changed)
            Select(CITIES, placeholder = Component.text("Pflichtfeld"), required = true, id = "select_required", onChange = context.changed)
            Select(CITIES, selected = "south", enabled = false, id = "select_disabled")
        }
    }
    storySection("Beispiel") {
        Select(CITIES, placeholder = Component.text("Wohnort wählen"), width = ElementSize.fixed(200), id = "select_home", onChange = context.changed)
    }
}

/**
 * Shows native selects in every size, with groups and disabled.
 *
 * @param context the story context
 */
private fun ComponentScope.nativeSelectStory(context: StoryContext) {
    storySection("Größen") {
        Row(gap = 4) {
            SelectSize.entries.forEach { size ->
                NativeSelect(CITIES, selected = "south", size = size, id = "native_${size.slug()}", onChange = context.changed)
            }
        }
    }
    storySection("Gruppen und gesperrt") {
        Row(gap = 4) {
            NativeSelect(groups = UNITS, selected = "rtw", id = "native_grouped", onChange = context.changed)
            NativeSelect(CITIES, selected = "old", enabled = false, id = "native_disabled")
        }
    }
    storySection("Beispiel") {
        NativeSelect(groups = UNITS, selected = "patrol", required = true, id = "native_unit", onChange = context.changed)
    }
}

/**
 * Shows single and multiple comboboxes, with a clear button, required and disabled.
 *
 * @param context the story context
 */
private fun ComponentScope.comboboxStory(context: StoryContext) {
    storySection("Einzeln und mehrfach") {
        Row(gap = 4) {
            Combobox(groups = UNITS, placeholder = Component.text("Einheit suchen …"), showClear = true, id = "combobox_single", onChange = context.changed)
            MultiCombobox(CITIES, selected = listOf("north"), placeholder = Component.text("Städte"), id = "combobox_multiple", onChange = context.changed)
        }
    }
    storySection("Pflicht und gesperrt") {
        Row(gap = 4) {
            Combobox(CITIES, placeholder = Component.text("Pflichtfeld"), required = true, id = "combobox_required", onChange = context.changed)
            Combobox(CITIES, selected = "old", enabled = false, id = "combobox_disabled")
        }
    }
    storySection("Beispiel") {
        Combobox(
            groups = UNITS,
            placeholder = Component.text("Einheit alarmieren …"),
            emptyText = Component.text("Keine Einheit gefunden."),
            id = "combobox_alarm",
            onSearch = { search -> context.reportChange(search.inputId) },
            onChange = context.changed,
        )
    }
}

/**
 * Shows calendars in every mode and caption layout, with limits and disabled days.
 *
 * @param context the story context
 */
private fun ComponentScope.calendarStory(context: StoryContext) {
    val today = LocalDate.now()
    storySection("Modi") {
        Row(gap = 8) {
            CalendarMode.entries.forEach { mode ->
                val selected = when (mode) {
                    CalendarMode.SINGLE -> listOf(today)
                    CalendarMode.MULTIPLE -> listOf(today, today.plusDays(2), today.plusDays(5))
                    CalendarMode.RANGE -> listOf(today.plusDays(3), today.plusDays(7))
                }
                Calendar(mode, selected = selected, id = "calendar_${mode.slug()}", onChange = context.changed)
            }
        }
    }
    storySection("Kopfzeilen, Grenzen und gesperrt") {
        Row(gap = 8) {
            CaptionLayout.entries.forEach { layout ->
                Calendar(captionLayout = layout, min = today, max = today.plusMonths(6), disabled = setOf(today.plusDays(1)), showOutsideDays = false, id = "calendar_${layout.slug()}", onChange = context.changed)
            }
            Calendar(selected = listOf(today), enabled = false, id = "calendar_disabled")
        }
    }
}

/**
 * Shows fields in both orientations, with title, description, error and separator.
 *
 * @param context the story context
 */
private fun ComponentScope.fieldStory(context: StoryContext) {
    storySection("Ausrichtungen") {
        Orientation.entries.forEach { orientation ->
            Field(orientation = orientation, width = ElementSize.grow()) {
                FieldLabel("Rufname (${orientation.slug()})", forId = "field_${orientation.slug()}")
                Input(placeholder = Component.text("Adler 1"), width = ElementSize.grow(), id = "field_${orientation.slug()}", onChange = context.changed)
            }
        }
    }
    storySection("Ungültig") {
        Field(width = ElementSize.grow()) {
            FieldLabel("Dienstnummer", forId = "field_invalid")
            Input(value = "abc", required = true, width = ElementSize.grow(), id = "field_invalid", onChange = context.changed)
            FieldError("Die Dienstnummer besteht nur aus Ziffern.")
        }
    }
    storySection("Beispiel") {
        FieldSet(width = ElementSize.grow()) {
            FieldLegend("Benachrichtigungen")
            FieldGroup {
                Field(orientation = Orientation.HORIZONTAL) {
                    FieldContent {
                        FieldTitle("Neue Einsätze")
                        FieldDescription("Ein Ton, sobald die Leitstelle dich alarmiert.")
                    }
                    Switch(checked = true, id = "field_calls", onChange = context.changed)
                }
                FieldSeparator(Component.text("oder"))
                Field(orientation = Orientation.HORIZONTAL) {
                    FieldContent {
                        FieldTitle("Funkmeldungen")
                        FieldDescription("Nur Meldungen deiner Wache.")
                    }
                    Switch(id = "field_radio", onChange = context.changed)
                }
            }
        }
    }
}

/**
 * Shows a form whose submit rejects the name "Max" with a field error and reports a valid one.
 *
 * @param context the story context
 */
private fun ComponentScope.formStory(context: StoryContext) {
    storySection("Beispiel") {
        Form(submitId = "form_send", width = ElementSize.grow(), id = "form_account") {
            FieldSet {
                FieldLegend("Konto")
                FieldGroup {
                    Field {
                        FieldLabel("Name", forId = "form_name")
                        Input(placeholder = Component.text("Max"), required = true, width = ElementSize.grow(), id = "form_name")
                        FieldDescription("\"Max\" ist schon vergeben.")
                        FieldError(id = "form_name_error")
                    }
                }
            }
            Row(gap = 4, mainAlign = Alignment.END, width = ElementSize.grow()) {
                Button("Speichern", submitsInput = false, icon = "check", id = "form_send") { click ->
                    if (click.values.text("form_name") == "Max") {
                        click.fail(mapOf("form_name" to Component.text("Dieser Name ist vergeben.")))
                    } else {
                        click.fail(emptyMap())
                        context.report(click.buttonId)
                    }
                }
            }
        }
    }
}
