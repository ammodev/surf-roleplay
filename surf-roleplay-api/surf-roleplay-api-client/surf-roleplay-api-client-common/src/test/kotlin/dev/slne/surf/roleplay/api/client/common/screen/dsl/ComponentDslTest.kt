package dev.slne.surf.roleplay.api.client.common.screen.dsl

import dev.slne.surf.roleplay.api.client.common.screen.ButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonGroupSeparatorElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonGroupTextElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.CalendarMode
import dev.slne.surf.roleplay.api.client.common.screen.ChangeHandler
import dev.slne.surf.roleplay.api.client.common.screen.ColumnElement
import dev.slne.surf.roleplay.api.client.common.screen.ComboboxElement
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.FieldContentElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldSeparatorElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldSetElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldTextElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldTextKind
import dev.slne.surf.roleplay.api.client.common.screen.FormElement
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupAddonElement
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupAlign
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupTextElement
import dev.slne.surf.roleplay.api.client.common.screen.LabelElement
import dev.slne.surf.roleplay.api.client.common.screen.OpenScreen
import dev.slne.surf.roleplay.api.client.common.screen.RadioChoice
import dev.slne.surf.roleplay.api.client.common.screen.RowElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenClick
import dev.slne.surf.roleplay.api.client.common.screen.ScreenInputChange
import dev.slne.surf.roleplay.api.client.common.screen.ScreenPatchBuilder
import dev.slne.surf.roleplay.api.client.common.screen.ScreenValues
import dev.slne.surf.roleplay.api.client.common.screen.ScrollListElement
import dev.slne.surf.roleplay.api.client.common.screen.SearchHandler
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoice
import dev.slne.surf.roleplay.api.client.common.screen.TextInputElement
import dev.slne.surf.roleplay.api.client.common.screen.ToggleElement
import dev.slne.surf.roleplay.api.client.common.screen.ToggleGroupChoice
import net.kyori.adventure.text.Component
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests for the component DSL: generated ids, typed input references and the components.
 */
class ComponentDslTest {

    /**
     * Adds a column whose children are built by a block, using only the scope primitives.
     *
     * @param id the explicit id, or `null` for a generated one
     * @param block the builder of the children
     * @return the column
     */
    private fun ComponentScope.column(id: String? = null, block: ComponentScope.() -> Unit): ColumnElement {
        val elementId = nextId(id)
        return add(ColumnElement(elementId, children(block)))
    }

    /**
     * Adds a label, using only the scope primitives.
     *
     * @param id the explicit id, or `null` for a generated one
     * @return the label
     */
    private fun ComponentScope.label(id: String? = null): LabelElement = add(LabelElement(nextId(id), Component.empty()))

    /**
     * Verifies that the scope generates ids from the position of every element.
     */
    @Test
    fun `the scope generates ids from the position`() {
        val root = assertIs<ColumnElement>(
            renderRoot {
                column {
                    label()
                    label(id = "named")
                    column { label() }
                }
            },
        )
        assertEquals("_0", root.id)
        assertEquals("_0.0", root.children[0].id)
        assertEquals("named", root.children[1].id)
        assertEquals("_0.2", root.children[2].id)
        assertEquals("_0.2.0", (root.children[2] as ColumnElement).children[0].id)
    }

    /**
     * Verifies that explicit ids starting with an underscore are rejected.
     */
    @Test
    fun `the scope rejects explicit ids with an underscore`() {
        assertFailsWith<IllegalArgumentException> { renderRoot { label(id = "_x") } }
    }

    /**
     * Verifies that rendering needs exactly one root element.
     */
    @Test
    fun `rendering needs exactly one root`() {
        assertFailsWith<IllegalStateException> { renderRoot { } }
        assertFailsWith<IllegalStateException> { renderRoot { label(); label() } }
    }

    /**
     * Verifies that a screen carries its options and the rendered root.
     */
    @Test
    fun `a screen carries its options`() {
        val definition = Screen(Component.text("T"), theme = "police", closable = false) { label(id = "root") }
        assertEquals(Component.text("T"), definition.title)
        assertEquals("police", definition.theme)
        assertEquals(false, definition.closable)
        assertEquals("root", definition.root.id)
    }

    /**
     * Verifies that the parsers of input references read every wire format.
     */
    @Test
    fun `input reference parsers read the wire formats`() {
        val values = ScreenValues(
            mapOf(
                "text" to "Ada",
                "number" to "42",
                "emptyNumber" to "",
                "checked" to "true",
                "unchecked" to "false",
                "selected" to "north",
                "noSelection" to "",
                "list" to "a, b",
                "emptyList" to "",
                "slider" to "10.0,20.5",
                "dates" to "2026-01-02,2026-01-05",
                "range" to "2026-01-02/2026-01-05",
            ),
        )
        assertEquals("Ada", values[InputRef("text", InputParsers.text)])
        assertEquals("", values[InputRef("missing", InputParsers.text)])
        assertEquals(42L, values[InputRef("number", InputParsers.number)])
        assertNull(values[InputRef("emptyNumber", InputParsers.number)])
        assertTrue(values[InputRef("checked", InputParsers.checked)])
        assertEquals(false, values[InputRef("unchecked", InputParsers.checked)])
        assertEquals(false, values[InputRef("missing", InputParsers.checked)])
        assertEquals("north", values[InputRef("selected", InputParsers.selected)])
        assertNull(values[InputRef("noSelection", InputParsers.selected)])
        assertEquals(listOf("a", "b"), values[InputRef("list", InputParsers.list)])
        assertEquals(emptyList(), values[InputRef("emptyList", InputParsers.list)])
        assertEquals(listOf(10.0, 20.5), values[InputRef("slider", InputParsers.numbers)])
        assertEquals(emptyList(), values[InputRef("missing", InputParsers.numbers)])
        val dates = listOf(LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 5))
        assertEquals(dates, values[InputRef("dates", InputParsers.dates)])
        assertEquals(dates, values[InputRef("range", InputParsers.dates)])
        assertEquals(emptyList(), values[InputRef("missing", InputParsers.dates)])
    }

    /**
     * Verifies that clicks and changes forward typed reads to their values.
     */
    @Test
    fun `clicks and changes read typed values`() {
        val screen = object : OpenScreen {
            /** A fixed session id. */
            override val sessionId: Int = 1

            /** A fixed viewer. */
            override val viewer: UUID = UUID(0, 0)

            /** Always open. */
            override val isOpen: Boolean = true

            /** Ignores the patch. */
            override fun patch(changes: ScreenPatchBuilder.() -> Unit) = Unit

            /** Ignores the errors. */
            override fun showErrors(errors: Map<String, Component>) = Unit

            /** Does nothing. */
            override fun close() = Unit
        }
        val values = ScreenValues(mapOf("age" to "7"))
        val age = InputRef("age", InputParsers.number)
        assertEquals(7L, ScreenClick(screen, "save", values)[age])
        assertEquals(7L, ScreenInputChange(screen, "age", "7", values)[age])
    }

    /**
     * Verifies that components get ids from their position.
     */
    @Test
    fun `ids are generated from the position`() {
        val definition = Screen(Component.text("T")) {
            Column { Button("A"); Row { Button("B") } }
        }
        val root = definition.root as ColumnElement
        assertEquals("_0", root.id)
        assertEquals("_0.0", root.children[0].id)
        assertEquals("_0.1.0", (root.children[1] as RowElement).children[0].id)
    }

    /**
     * Verifies that explicit ids are kept and that the generated prefix is reserved.
     */
    @Test
    fun `explicit ids are kept and may not start with an underscore`() {
        val definition = Screen(Component.text("T")) { Column(id = "root") { Button("A", id = "save") } }
        assertEquals("save", (definition.root as ColumnElement).children[0].id)
        assertFailsWith<IllegalArgumentException> { Screen(Component.text("T")) { Button("A", id = "_x") } }
    }

    /**
     * Verifies that two elements with the same explicit id fail the screen.
     */
    @Test
    fun `duplicate explicit ids fail`() {
        assertFailsWith<IllegalArgumentException> { Screen(Component.text("T")) { Column { Button("A", id = "x"); Button("B", id = "x") } } }
    }

    /**
     * Verifies that input references read typed values.
     */
    @Test
    fun `input references read typed values`() {
        lateinit var name: InputRef<String>
        lateinit var age: InputRef<Long?>
        lateinit var agree: InputRef<Boolean>
        Screen(Component.text("T")) { Column { name = Input(id = "name"); age = NumberInput(id = "age"); agree = Checkbox("Ja", id = "agree") } }
        val values = ScreenValues(mapOf("name" to "Ada", "age" to "", "agree" to "true"))
        assertEquals("Ada", values[name]); assertNull(values[age]); assertTrue(values[agree])
    }

    /**
     * Verifies that every handler passes through the binder with the id of its element.
     */
    @Test
    fun `the binder sees every handler with its element id`() {
        val seen = mutableListOf<String>()
        val binder = object : HandlerBinder {
            /** Records the id. */
            override fun button(elementId: String, handler: ButtonHandler) = handler.also { seen += elementId }

            /** Records the id. */
            override fun change(elementId: String, handler: ChangeHandler) = handler.also { seen += elementId }

            /** Records the id. */
            override fun search(elementId: String, handler: SearchHandler) = handler.also { seen += elementId }
        }
        Screen(Component.text("T"), binder = binder) { Column { Button("A") {}; Input(id = "q", onChange = {}) } }
        assertEquals(listOf("_0.0", "q"), seen)
    }

    /**
     * Verifies that a screen needs exactly one root component.
     */
    @Test
    fun `exactly one root is required`() {
        assertFailsWith<IllegalStateException> { Screen(Component.text("T")) { Button("A"); Button("B") } }
    }

    /**
     * Verifies that elements store the handler the binder returns, for every handler kind.
     */
    @Test
    fun `elements store the bound handlers`() {
        val bound = ButtonHandler { }
        val boundChange = ChangeHandler { }
        val boundSearch = SearchHandler { }
        val binder = object : HandlerBinder {
            /** Returns the bound button handler. */
            override fun button(elementId: String, handler: ButtonHandler) = bound

            /** Returns the bound change handler. */
            override fun change(elementId: String, handler: ChangeHandler) = boundChange

            /** Returns the bound search handler. */
            override fun search(elementId: String, handler: SearchHandler) = boundSearch
        }
        val root = assertIs<ColumnElement>(
            renderRoot(binder) {
                Column {
                    Button("A") { }
                    Toggle("B", onToggle = { })
                    Combobox(listOf(SelectChoice("a", Component.text("A"))), onSearch = { }, onChange = { })
                }
            },
        )
        assertSame(bound, assertIs<ButtonElement>(root.children[0]).onClick)
        assertSame(bound, assertIs<ToggleElement>(root.children[1]).onToggle)
        val combobox = assertIs<ComboboxElement>(root.children[2])
        assertSame(boundChange, combobox.onChange)
        assertSame(boundSearch, combobox.onSearch)
    }

    /**
     * Verifies that layout and action components build their elements with their settings.
     */
    @Test
    fun `layout and action components build their elements`() {
        val root = assertIs<ColumnElement>(
            renderRoot {
                Column(gap = 4) {
                    ScrollList(height = ElementSize.fixed(60), gap = 1) { Label("Hallo", icon = "user") }
                    ButtonGroup {
                        Button(Component.text("Eins"), variant = ButtonVariant.OUTLINE)
                        ButtonGroupSeparator()
                        ButtonGroupText("Zwei")
                    }
                }
            },
        )
        assertEquals(4, root.gap)
        val list = assertIs<ScrollListElement>(root.children[0])
        assertEquals(ElementSize.fixed(60), list.height)
        assertEquals("user", assertIs<LabelElement>(list.children[0]).icon)
        val group = assertIs<ButtonGroupElement>(root.children[1])
        assertEquals(ButtonVariant.OUTLINE, assertIs<ButtonElement>(group.children[0]).variant)
        assertIs<ButtonGroupSeparatorElement>(group.children[1])
        assertEquals(Component.text("Zwei"), assertIs<ButtonGroupTextElement>(group.children[2]).text)
        assertEquals("_0.1.2", group.children[2].id)
    }

    /**
     * Verifies that field and form components build their elements with the right text kinds.
     */
    @Test
    fun `field and form components build their elements`() {
        val form = assertIs<FormElement>(
            renderRoot {
                Form(submitId = "save") {
                    FieldSet {
                        FieldLegend("Person", asLabel = true)
                        FieldGroup {
                            Field {
                                FieldLabel("Name", forId = "name")
                                Input(id = "name")
                                FieldDescription("Der volle Name")
                                FieldError()
                            }
                            FieldSeparator("oder")
                            Field {
                                FieldContent { FieldTitle("Titel") }
                            }
                        }
                    }
                    InputGroup {
                        InputGroupAddon(align = InputGroupAlign.INLINE_END) {
                            InputGroupText("@")
                            InputGroupButton("Los")
                        }
                        Textarea()
                    }
                }
            },
        )
        assertEquals("save", form.submitId)
        val set = assertIs<FieldSetElement>(form.children[0])
        assertEquals(FieldTextKind.LEGEND_LABEL, assertIs<FieldTextElement>(set.children[0]).kind)
        val group = assertIs<FieldGroupElement>(set.children[1])
        val field = assertIs<FieldElement>(group.children[0])
        val label = assertIs<FieldTextElement>(field.children[0])
        assertEquals(FieldTextKind.LABEL, label.kind)
        assertEquals("name", label.forId)
        assertIs<TextInputElement>(field.children[1])
        assertEquals(FieldTextKind.DESCRIPTION, assertIs<FieldTextElement>(field.children[2]).kind)
        assertEquals(FieldTextKind.ERROR, assertIs<FieldTextElement>(field.children[3]).kind)
        val separator = assertIs<FieldSeparatorElement>(group.children[1])
        assertEquals(Component.text("oder"), separator.text)
        assertEquals(ElementSize.grow(), separator.width)
        val content = assertIs<FieldContentElement>(assertIs<FieldElement>(group.children[2]).children[0])
        assertEquals(ElementSize.grow(), content.width)
        assertEquals(FieldTextKind.TITLE, assertIs<FieldTextElement>(content.children[0]).kind)
        val inputGroup = assertIs<InputGroupElement>(form.children[1])
        val addon = assertIs<InputGroupAddonElement>(inputGroup.children[0])
        assertEquals(InputGroupAlign.INLINE_END, addon.align)
        assertIs<InputGroupTextElement>(addon.children[0])
        val button = assertIs<ButtonElement>(addon.children[1])
        assertEquals(ButtonVariant.GHOST, button.variant)
        assertEquals(false, button.submitsInput)
    }

    /**
     * Verifies that every input component returns a reference that reads its wire format.
     */
    @Test
    fun `every input reference reads its input type`() {
        val options = listOf(SelectChoice("a", Component.text("A")), SelectChoice("b", Component.text("B")))
        lateinit var input: InputRef<String>
        lateinit var number: InputRef<Long?>
        lateinit var textarea: InputRef<String>
        lateinit var otp: InputRef<String>
        lateinit var checkbox: InputRef<Boolean>
        lateinit var switch: InputRef<Boolean>
        lateinit var toggle: InputRef<Boolean>
        lateinit var toggleGroup: InputRef<List<String>>
        lateinit var radio: InputRef<String?>
        lateinit var slider: InputRef<List<Double>>
        lateinit var select: InputRef<String?>
        lateinit var nativeSelect: InputRef<String?>
        lateinit var combobox: InputRef<String?>
        lateinit var multiCombobox: InputRef<List<String>>
        lateinit var calendar: InputRef<List<LocalDate>>
        Screen(Component.text("T")) {
            Column {
                input = Input()
                number = NumberInput()
                textarea = Textarea()
                otp = InputOtp(length = 4)
                checkbox = Checkbox()
                switch = Switch()
                toggle = Toggle("Fett")
                toggleGroup = ToggleGroup(listOf(ToggleGroupChoice("a"), ToggleGroupChoice("b")), multiple = true)
                radio = RadioGroup(listOf(RadioChoice("a", Component.text("A"))))
                slider = Slider(listOf(10.0, 20.0))
                select = Select(options)
                nativeSelect = NativeSelect(options)
                combobox = Combobox(options)
                multiCombobox = MultiCombobox(options)
                calendar = Calendar(mode = CalendarMode.RANGE)
            }
        }
        val refs = listOf(input, number, textarea, otp, checkbox, switch, toggle, toggleGroup, radio, slider, select, nativeSelect, combobox, multiCombobox, calendar)
        assertEquals(refs.indices.map { "_0.$it" }, refs.map { it.id })
        val values = ScreenValues(
            mapOf(
                input.id to "Ada",
                number.id to "12",
                textarea.id to "a\nb",
                otp.id to "1234",
                checkbox.id to "true",
                switch.id to "false",
                toggle.id to "true",
                toggleGroup.id to "a,b",
                radio.id to "",
                slider.id to "10.0,20.0",
                select.id to "a",
                nativeSelect.id to "",
                combobox.id to "b",
                multiCombobox.id to "b,a",
                calendar.id to "2026-01-02/2026-01-05",
            ),
        )
        assertEquals("Ada", values[input])
        assertEquals(12L, values[number])
        assertEquals("a\nb", values[textarea])
        assertEquals("1234", values[otp])
        assertTrue(values[checkbox])
        assertEquals(false, values[switch])
        assertTrue(values[toggle])
        assertEquals(listOf("a", "b"), values[toggleGroup])
        assertNull(values[radio])
        assertEquals(listOf(10.0, 20.0), values[slider])
        assertEquals("a", values[select])
        assertNull(values[nativeSelect])
        assertEquals("b", values[combobox])
        assertEquals(listOf("b", "a"), values[multiCombobox])
        assertEquals(listOf(LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 5)), values[calendar])
    }

    /**
     * Verifies that a single combobox selects one value and a multiple combobox keeps its flag.
     */
    @Test
    fun `comboboxes keep their selection mode`() {
        val options = listOf(SelectChoice("a", Component.text("A")), SelectChoice("b", Component.text("B")))
        val root = assertIs<ColumnElement>(
            renderRoot { Column { Combobox(options, selected = "a"); MultiCombobox(options, selected = listOf("a", "b")) } },
        )
        val single = assertIs<ComboboxElement>(root.children[0])
        assertEquals(listOf("a"), single.selected)
        assertEquals(false, single.multiple)
        assertEquals(true, assertIs<ComboboxElement>(root.children[1]).multiple)
    }
}
