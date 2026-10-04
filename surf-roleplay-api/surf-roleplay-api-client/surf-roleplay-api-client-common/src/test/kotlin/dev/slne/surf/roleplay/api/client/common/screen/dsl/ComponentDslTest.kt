package dev.slne.surf.roleplay.api.client.common.screen.dsl

import dev.slne.surf.roleplay.api.client.common.screen.ALERT_DIALOG_SMALL_WIDTH
import dev.slne.surf.roleplay.api.client.common.screen.AlertDialogContentElement
import dev.slne.surf.roleplay.api.client.common.screen.AlertDialogElement
import dev.slne.surf.roleplay.api.client.common.screen.AlertDialogSize
import dev.slne.surf.roleplay.api.client.common.screen.CommandElement
import dev.slne.surf.roleplay.api.client.common.screen.DialogCloseElement
import dev.slne.surf.roleplay.api.client.common.screen.DropdownMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuContentElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuRadioGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.POPOVER_WIDTH
import dev.slne.surf.roleplay.api.client.common.screen.PaginationContentElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationItemElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationPreviousElement
import dev.slne.surf.roleplay.api.client.common.screen.ResizablePanelGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarProviderElement
import dev.slne.surf.roleplay.api.client.common.screen.TabsElement
import dev.slne.surf.roleplay.api.client.common.screen.PopoverContentElement
import dev.slne.surf.roleplay.api.client.common.screen.PopoverElement
import dev.slne.surf.roleplay.api.client.common.screen.AlertElement
import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.AvatarElement
import dev.slne.surf.roleplay.api.client.common.screen.CardContentElement
import dev.slne.surf.roleplay.api.client.common.screen.CardElement
import dev.slne.surf.roleplay.api.client.common.screen.CardFooterElement
import dev.slne.surf.roleplay.api.client.common.screen.CardHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemElement
import dev.slne.surf.roleplay.api.client.common.screen.Orientation
import dev.slne.surf.roleplay.api.client.common.screen.SeparatorElement
import dev.slne.surf.roleplay.api.client.common.screen.TextElement
import dev.slne.surf.roleplay.api.client.common.screen.TextKind
import dev.slne.surf.roleplay.api.client.common.screen.ButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonGroupSeparatorElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonGroupTextElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.CalendarMode
import dev.slne.surf.roleplay.api.client.common.screen.ChangeHandler
import dev.slne.surf.roleplay.api.client.common.screen.ChartElement
import dev.slne.surf.roleplay.api.client.common.screen.ChartKind
import dev.slne.surf.roleplay.api.client.common.screen.ChartSeries
import dev.slne.surf.roleplay.api.client.common.screen.ChatMessageElement
import dev.slne.surf.roleplay.api.client.common.screen.ChatViewElement
import dev.slne.surf.roleplay.api.client.common.screen.DataTableCellElement
import dev.slne.surf.roleplay.api.client.common.screen.DataTableElement
import dev.slne.surf.roleplay.api.client.common.screen.DataTableRowElement
import dev.slne.surf.roleplay.api.client.common.screen.DataTableView
import dev.slne.surf.roleplay.api.client.common.screen.TableCellElement
import dev.slne.surf.roleplay.api.client.common.screen.TableElement
import dev.slne.surf.roleplay.api.client.common.screen.TableRowElement
import dev.slne.surf.roleplay.api.client.common.screen.TableSection
import dev.slne.surf.roleplay.api.client.common.screen.TableSectionElement
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
        val values = ScreenValues(mapOf("age" to "7"))
        val age = InputRef("age", InputParsers.number)
        assertEquals(7L, ScreenClick(TestScreen, "save", values)[age])
        assertEquals(7L, ScreenInputChange(TestScreen, "age", "7", values)[age])
    }

    /**
     * An open screen that ignores every call, for building clicks and changes.
     */
    private object TestScreen : OpenScreen {
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

    /**
     * Verifies that rendering at a generated target id keeps that id for the root.
     */
    @Test
    fun `rendering at a generated id keeps the target id`() {
        val root = assertIs<ColumnElement>(renderRoot(at = "_0.3") { Column { Button("A") } })
        assertEquals("_0.3", root.id)
        assertEquals("_0.3.0", root.children[0].id)
    }

    /**
     * Verifies that rendering at an explicit target id prefixes the generated root id.
     */
    @Test
    fun `rendering at an explicit id prefixes the root id`() {
        val root = assertIs<ColumnElement>(renderRoot(at = "list") { Column { Button("A"); Row { Button("B") } } })
        assertEquals("_list", root.id)
        assertEquals("_list.0", root.children[0].id)
        assertEquals("_list.1.0", (root.children[1] as RowElement).children[0].id)
    }

    /**
     * Verifies that an explicit id on the root component wins over the target id.
     */
    @Test
    fun `an explicit root id wins over the target id`() {
        val root = assertIs<ColumnElement>(renderRoot(at = "_0.3") { Column(id = "box") { Button("A") } })
        assertEquals("box", root.id)
        assertEquals("_0.3.0", root.children[0].id)
    }

    /**
     * Verifies that a handler cannot add components to the scope its element was built in.
     */
    @Test
    fun `a built scope rejects new components`() {
        val button = assertIs<ButtonElement>(renderRoot { Button("A") { Button("B") } })
        val click = ScreenClick(TestScreen, button.id, ScreenValues(emptyMap()))
        assertFailsWith<IllegalStateException> { button.onClick!!.onClick(click) }
    }

    /**
     * Verifies that a handler cannot add components to the scope of a container's children.
     */
    @Test
    fun `a built child scope rejects new components`() {
        val column = assertIs<ColumnElement>(renderRoot { Column { Button("A") { Label("B") } } })
        val button = assertIs<ButtonElement>(column.children[0])
        val click = ScreenClick(TestScreen, button.id, ScreenValues(emptyMap()))
        assertFailsWith<IllegalStateException> { button.onClick!!.onClick(click) }
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

    /**
     * Verifies that typography components build texts of their style with generated or explicit
     * ids.
     */
    @Test
    fun `typography components build styled texts`() {
        val root = assertIs<ColumnElement>(
            renderRoot {
                Column {
                    H1("Eins"); H2("Zwei"); H3("Drei"); H4("Vier")
                    P("a", id = "a"); Lead("Lead"); Large("Groß"); Small("Klein"); Muted("Leise")
                    Blockquote("Zitat"); InlineCode("code")
                    H2(Component.text("Titel"), maxLines = 1, align = Alignment.CENTER)
                }
            },
        )
        val kinds = listOf(
            TextKind.H1, TextKind.H2, TextKind.H3, TextKind.H4, TextKind.P, TextKind.LEAD, TextKind.LARGE, TextKind.SMALL, TextKind.MUTED,
            TextKind.BLOCKQUOTE, TextKind.INLINE_CODE, TextKind.H2,
        )
        assertEquals(kinds, root.children.map { assertIs<TextElement>(it).kind })
        assertEquals("a", root.children[4].id)
        assertEquals(Component.text("a"), (root.children[4] as TextElement).text)
        assertEquals("_0.5", root.children[5].id)
        val title = root.children[11] as TextElement
        assertEquals(1, title.maxLines)
        assertEquals(Alignment.CENTER, title.align)
    }

    /**
     * Verifies that display containers nest their parts and keep the rules of their elements.
     */
    @Test
    fun `display components nest their parts`() {
        val card = assertIs<CardElement>(
            renderRoot {
                Card(width = ElementSize.fixed(100)) {
                    CardHeader {
                        CardTitle("Fall 12")
                        CardDescription("Offen")
                    }
                    CardContent { Separator(); Separator(Orientation.VERTICAL) }
                    CardFooter { Avatar("AB", badgeIcon = "check") }
                }
            },
        )
        assertEquals(ElementSize.fixed(100), card.width)
        val header = assertIs<CardHeaderElement>(card.children[0])
        assertEquals("_0.0.0", header.children[0].id)
        assertEquals(TextKind.CARD_TITLE, assertIs<TextElement>(header.children[0]).kind)
        assertEquals(TextKind.CARD_DESCRIPTION, assertIs<TextElement>(header.children[1]).kind)
        val content = assertIs<CardContentElement>(card.children[1])
        assertEquals(ElementSize.grow(), assertIs<SeparatorElement>(content.children[0]).width)
        assertEquals(ElementSize.grow(), assertIs<SeparatorElement>(content.children[1]).height)
        val footer = assertIs<CardFooterElement>(card.children[2])
        assertTrue(assertIs<AvatarElement>(footer.children[0]).badge)
        assertFailsWith<IllegalArgumentException> { renderRoot { AspectRatio(0f) { Label("x") } } }
    }

    /**
     * Verifies that a clickable item binds its handler with its id and grows across its container.
     */
    @Test
    fun `a clickable item binds its handler`() {
        val seen = mutableListOf<String>()
        val binder = recordingBinder(seen)
        val alert = assertIs<AlertElement>(
            renderRoot(binder) {
                Alert(icon = "info") {
                    AlertTitle("Achtung")
                    Item(onClick = { }, id = "item") { ItemContent { ItemTitle("Titel"); ItemDescription("Text") } }
                }
            },
        )
        val item = assertIs<ItemElement>(alert.children[1])
        assertEquals(listOf("item"), seen)
        assertEquals(ElementSize.grow(), item.width)
        assertEquals(TextKind.ALERT_TITLE, assertIs<TextElement>(alert.children[0]).kind)
    }

    /**
     * Verifies that overlays report their open state and bind every handler with its element id.
     */
    @Test
    fun `overlay and menu components bind their handlers`() {
        val seen = mutableListOf<String>()
        lateinit var open: InputRef<Boolean>
        lateinit var checked: InputRef<Boolean>
        lateinit var radio: InputRef<String?>
        val root = assertIs<ColumnElement>(
            renderRoot(recordingBinder(seen)) {
                Column {
                    open = Popover(open = true, onChange = { }, id = "pop") {
                        Button("Öffnen")
                        PopoverContent { PopoverHeader { PopoverTitle("Titel"); PopoverDescription("Text") } }
                    }
                    DropdownMenu(onChange = { }) {
                        Button("Menü")
                        MenuContent {
                            MenuItem("Löschen", destructive = true, id = "delete") { }
                            checked = MenuCheckboxItem("Fett", id = "bold") { }
                            radio = MenuRadioGroup(value = "a", onSelect = { }, id = "pick") { MenuRadioItem("A", "a") }
                        }
                    }
                    Command(onSearch = { }, id = "cmd") { CommandInput(); CommandList { CommandItem("Eins", id = "one") { } } }
                }
            },
        )
        assertEquals(listOf("pop", "delete", "bold", "pick", "_0.1", "one", "cmd"), seen)
        val popover = assertIs<PopoverElement>(root.children[0])
        assertTrue(popover.open)
        assertEquals(ElementSize.fixed(POPOVER_WIDTH), assertIs<PopoverContentElement>(popover.children[1]).width)
        val menu = assertIs<MenuContentElement>(assertIs<DropdownMenuElement>(root.children[1]).children[1])
        assertEquals("_0.1.1.2.0", assertIs<MenuRadioGroupElement>(menu.children[2]).children[0].id)
        assertEquals(ElementSize.grow(), assertIs<CommandElement>(root.children[2]).width)
        val values = ScreenValues(mapOf("pop" to "true", "bold" to "false", "pick" to "a"))
        assertTrue(values[open])
        assertEquals(false, values[checked])
        assertEquals("a", values[radio])
        assertFailsWith<IllegalArgumentException> { renderRoot { HoverCard(openDelay = -1) { Button("x") } } }
    }

    /**
     * Verifies that the action and cancel of an alert dialog wrap their buttons in a close part.
     */
    @Test
    fun `alert dialog buttons close the dialog`() {
        val seen = mutableListOf<String>()
        val dialog = assertIs<AlertDialogElement>(
            renderRoot(recordingBinder(seen)) {
                AlertDialog {
                    Button("Löschen")
                    AlertDialogContent(size = AlertDialogSize.SM) {
                        AlertDialogMedia("trash")
                        AlertDialogAction("Ja", id = "yes") { }
                        AlertDialogCancel("Nein") { }
                    }
                }
            },
        )
        val content = assertIs<AlertDialogContentElement>(dialog.children[1])
        assertEquals(ElementSize.fixed(ALERT_DIALOG_SMALL_WIDTH), content.width)
        val action = assertIs<DialogCloseElement>(content.children[1])
        assertEquals("yes_close", action.id)
        val yes = assertIs<ButtonElement>(action.children[0])
        assertEquals("yes", yes.id)
        assertEquals(false, yes.submitsInput)
        val cancel = assertIs<DialogCloseElement>(content.children[2])
        assertEquals("_0.1.2", cancel.id)
        assertEquals(ButtonVariant.OUTLINE, assertIs<ButtonElement>(cancel.children[0]).variant)
        assertEquals(listOf("yes", "_0.1.2.0"), seen)
    }

    /**
     * Verifies that navigation components report their state through typed references and bind
     * their change handlers.
     */
    @Test
    fun `navigation components report their state`() {
        val seen = mutableListOf<String>()
        lateinit var tab: InputRef<String?>
        lateinit var open: InputRef<List<String>>
        lateinit var shown: InputRef<Boolean>
        lateinit var slide: InputRef<Int?>
        lateinit var sizes: InputRef<List<Double>>
        val root = assertIs<ColumnElement>(
            renderRoot(recordingBinder(seen)) {
                Column {
                    tab = Tabs(value = "a", onChange = { }, id = "tabs") {
                        TabsList { TabsTrigger("a", "A"); TabsTrigger("b", "B") }
                        TabsContent("a") { Label("A") }
                    }
                    open = Accordion(id = "acc") { AccordionItem("x") { AccordionTrigger("X"); AccordionContent { } } }
                    shown = Collapsible(open = true, id = "col") { CollapsibleTrigger { Button("Auf") }; CollapsibleContent { } }
                    slide = Carousel(ElementSize.fixed(100), id = "car") { CarouselContent { CarouselItem { } }; CarouselPrevious(); CarouselNext() }
                    sizes = ResizablePanelGroup(onChange = { }) { ResizablePanel(50.0) { }; ResizableHandle(); ResizablePanel { } }
                }
            },
        )
        assertEquals(listOf("tabs", "_0.4"), seen)
        assertEquals("a", assertIs<TabsElement>(root.children[0]).value)
        assertEquals(ElementSize.grow(), assertIs<ResizablePanelGroupElement>(root.children[4]).width)
        val values = ScreenValues(mapOf("tabs" to "b", "acc" to "x", "col" to "false", "car" to "0", sizes.id to "40.0,60.0"))
        assertEquals("b", values[tab])
        assertEquals(listOf("x"), values[open])
        assertEquals(false, values[shown])
        assertEquals(0, values[slide])
        assertEquals(listOf(40.0, 60.0), values[sizes])
        assertFailsWith<IllegalArgumentException> { renderRoot { Accordion { AccordionItem("a,b") { } } } }
    }

    /**
     * Verifies that links, pagination and sidebar buttons bind their click handlers with their ids.
     */
    @Test
    fun `navigation links bind their handlers`() {
        val seen = mutableListOf<String>()
        lateinit var search: InputRef<String>
        val root = assertIs<ColumnElement>(
            renderRoot(recordingBinder(seen)) {
                Column {
                    Breadcrumb { BreadcrumbList { BreadcrumbItem { BreadcrumbLink("Start", id = "home") { } }; BreadcrumbSeparator(); BreadcrumbPage("Akte") } }
                    Pagination {
                        PaginationContent {
                            PaginationItem { PaginationPrevious(id = "prev") { } }
                            PaginationItem { PaginationLink("1", active = true, id = "one") { } }
                            PaginationItem { PaginationNext(id = "next") { } }
                        }
                    }
                    NavigationMenu { NavigationMenuList { NavigationMenuItem { NavigationMenuLink(onClick = { }, id = "nav") { Label("Link") } } } }
                    SidebarProvider(open = false, onChange = { }, id = "side") {
                        Sidebar {
                            SidebarHeader { search = SidebarInput(id = "q") }
                            SidebarContent { SidebarGroup { SidebarGroupAction(id = "add") { } } }
                        }
                        SidebarInset { SidebarMenuButton("Start", id = "menu") { } }
                    }
                }
            },
        )
        assertEquals(listOf("home", "prev", "one", "next", "nav", "add", "menu", "side"), seen)
        val pagination = assertIs<PaginationElement>(root.children[1])
        assertEquals(ElementSize.grow(), pagination.width)
        val previous = assertIs<PaginationItemElement>(assertIs<PaginationContentElement>(pagination.children[0]).children[0])
        assertEquals(Component.text("Zurück"), assertIs<PaginationPreviousElement>(previous.children[0]).text)
        assertEquals(false, assertIs<SidebarProviderElement>(root.children[3]).open)
        assertEquals("q", search.id)
        assertEquals("Ada", ScreenValues(mapOf("q" to "Ada"))[search])
    }

    /**
     * Verifies that a table builds its sections, and that text cells hold a label below the cell.
     */
    @Test
    fun `table components build sections and text cells`() {
        val table = assertIs<TableElement>(
            renderRoot {
                Table {
                    TableHeader { TableRow { TableHead("Name", align = Alignment.END) } }
                    TableBody { TableRow(selected = true) { TableCell("Ada", id = "ada"); TableCell { Button("x") } } }
                    TableFooter { }
                    TableCaption("Einheiten")
                }
            },
        )
        val sections = table.children.take(3).map { assertIs<TableSectionElement>(it).section }
        assertEquals(listOf(TableSection.HEADER, TableSection.BODY, TableSection.FOOTER), sections)
        val head = assertIs<TableCellElement>(assertIs<TableRowElement>(assertIs<TableSectionElement>(table.children[0]).children[0]).children[0])
        assertTrue(head.head)
        assertEquals(Alignment.END, head.align)
        assertEquals("_0.0.0.0.0", assertIs<LabelElement>(head.children[0]).id)
        val row = assertIs<TableRowElement>(assertIs<TableSectionElement>(table.children[1]).children[0])
        assertTrue(row.selected)
        val cell = assertIs<TableCellElement>(row.children[0])
        assertEquals("ada", cell.id)
        assertEquals(Component.text("Ada"), assertIs<LabelElement>(cell.children[0]).text)
        assertEquals("_0.1.0.0.0", cell.children[0].id)
    }

    /**
     * Verifies that a data table reports its view through a typed reference and binds its change
     * handler, and that charts and chat views keep their settings.
     */
    @Test
    fun `data, chart and chat components build their elements`() {
        val seen = mutableListOf<String>()
        lateinit var view: InputRef<DataTableView>
        val root = assertIs<ColumnElement>(
            renderRoot(recordingBinder(seen)) {
                Column {
                    view = DataTable(pageSize = 5, onChange = { }, id = "units") {
                        DataTableColumn("name", "Name", sortable = true)
                        DataTableRow(id = "r1") { DataTableCell("Ada", "ada") }
                    }
                    Chart(ChartKind.BAR, listOf(Component.text("Jan")), listOf(ChartSeries("a", Component.text("A"), 1, listOf(2.0))), stacked = true)
                    ChatView(ElementSize.fixed(80)) { ChatMessage(own = true, fallback = "AB") { P("Hallo") } }
                }
            },
        )
        assertEquals(listOf("units"), seen)
        val table = assertIs<DataTableElement>(root.children[0])
        assertEquals(5, table.pageSize)
        assertEquals(Component.text("Filtern..."), table.filterPlaceholder)
        val cell = assertIs<DataTableCellElement>(assertIs<DataTableRowElement>(table.children[1]).children[0])
        assertEquals("ada", cell.sortKey)
        assertIs<LabelElement>(cell.children[0])
        assertEquals(DataTableView(sort = "name", page = 1), ScreenValues(mapOf("units" to DataTableView(sort = "name", page = 1).toJson()))[view])
        assertEquals(DataTableView(), ScreenValues(emptyMap())[view])
        assertTrue(assertIs<ChartElement>(root.children[1]).stacked)
        val chat = assertIs<ChatViewElement>(root.children[2])
        assertEquals(ElementSize.fixed(80), chat.height)
        assertTrue(assertIs<ChatMessageElement>(chat.children[0]).own)
    }

    /**
     * Creates a binder that records the element id of every handler it binds.
     *
     * @param seen the list the ids are added to
     * @return the binder
     */
    private fun recordingBinder(seen: MutableList<String>): HandlerBinder = object : HandlerBinder {
        /** Records the id. */
        override fun button(elementId: String, handler: ButtonHandler) = handler.also { seen += elementId }

        /** Records the id. */
        override fun change(elementId: String, handler: ChangeHandler) = handler.also { seen += elementId }

        /** Records the id. */
        override fun search(elementId: String, handler: SearchHandler) = handler.also { seen += elementId }
    }
}
