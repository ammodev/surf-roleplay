package dev.slne.surf.roleplay.api.client.common.screen

import net.kyori.adventure.text.Component

/**
 * How many items of an accordion can be open at once.
 */
enum class AccordionType {
    /**
     * At most one item; opening an item closes the open one.
     */
    SINGLE,

    /**
     * Any number of items.
     */
    MULTIPLE,
}

/**
 * A collapsible: its triggers, and a content shown or hidden when a trigger fires.
 *
 * @property id the id of this element
 * @property children the triggers and the collapsible contents
 * @property open whether the content is shown
 * @property onChange whether the mod reports every change of the state at once
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CollapsibleElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val open: Boolean = false,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The trigger part of a collapsible: an action of a widget inside it shows or hides the content
 * instead of reaching the server.
 *
 * @property id the id of this element
 * @property children the widgets that toggle the content, usually one button
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CollapsibleTriggerElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The content of a collapsible, shown while the collapsible is open.
 *
 * @property id the id of this element
 * @property children the content, stacked
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CollapsibleContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * An accordion: stacked items, each with a trigger that shows or hides its content.
 *
 * @property id the id of this element
 * @property children the accordion items
 * @property type how many items can be open at once
 * @property collapsible whether the open item of a single accordion can be closed
 * @property value the values of the open items
 * @property onChange whether the mod reports every change of the state at once
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class AccordionElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val type: AccordionType = AccordionType.SINGLE,
    val collapsible: Boolean = false,
    val value: List<String> = emptyList(),
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * An item of an accordion: its trigger and its content, with a border below.
 *
 * @property id the id of this element
 * @property children the trigger and the content
 * @property value the value that identifies the item in the accordion
 * @property enabled whether the item can be opened and closed
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class AccordionItemElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val value: String = "",
    val enabled: Boolean = true,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The trigger of an accordion item: its text and a chevron that turns while the item is open.
 *
 * @property id the id of this element
 * @property text the text
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class AccordionTriggerElement(
    override val id: String,
    val text: Component = Component.empty(),
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * The content of an accordion item, shown while the item is open.
 *
 * @property id the id of this element
 * @property children the content, stacked
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class AccordionContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * Builds the children of a container element.
 *
 * @param children the builder of the children
 * @return the built children
 */
private fun build(children: ElementsBuilder.() -> Unit): List<ScreenElement> = ElementsBuilder().apply(children).elements.toList()

/**
 * Adds a collapsible. Its children are [collapsibleTrigger]s and [collapsibleContent]s.
 *
 * @param id the id of the collapsible
 * @param open whether the content is shown at first
 * @param onChange the handler run whenever the player shows or hides the content, or `null` for
 *        none
 * @param children the builder of the triggers and contents
 */
fun ElementsBuilder.collapsible(id: String, open: Boolean = false, onChange: ChangeHandler? = null, children: ElementsBuilder.() -> Unit) {
    elements += CollapsibleElement(id, build(children), open, onChange)
}

/**
 * Adds the trigger part of a collapsible: an action of a widget inside it shows or hides the
 * content instead of reaching the server.
 *
 * @param id the id of the trigger
 * @param children the builder of the widgets that toggle the content, usually one button
 */
fun ElementsBuilder.collapsibleTrigger(id: String, children: ElementsBuilder.() -> Unit) {
    elements += CollapsibleTriggerElement(id, build(children))
}

/**
 * Adds the content of a collapsible, shown while the collapsible is open.
 *
 * @param id the id of the content
 * @param children the builder of the content
 */
fun ElementsBuilder.collapsibleContent(id: String, children: ElementsBuilder.() -> Unit) {
    elements += CollapsibleContentElement(id, build(children))
}

/**
 * Adds an accordion. Its children are [accordionItem]s.
 *
 * @param id the id of the accordion
 * @param type how many items can be open at once
 * @param collapsible whether the open item of a single accordion can be closed
 * @param value the values of the items open at first
 * @param onChange the handler run whenever the player opens or closes an item, with the values of
 *        the open items, comma separated, or `null` for none
 * @param children the builder of the items
 */
fun ElementsBuilder.accordion(
    id: String,
    type: AccordionType = AccordionType.SINGLE,
    collapsible: Boolean = false,
    value: List<String> = emptyList(),
    onChange: ChangeHandler? = null,
    children: ElementsBuilder.() -> Unit,
) {
    elements += AccordionElement(id, build(children), type, collapsible, value, onChange)
}

/**
 * Adds an item of an accordion. Its children are an [accordionTrigger] and an
 * [accordionContent].
 *
 * @param id the id of the item
 * @param value the value that identifies the item in the accordion; it must not contain a comma
 * @param enabled whether the item can be opened and closed
 * @param children the builder of the trigger and the content
 */
fun ElementsBuilder.accordionItem(id: String, value: String, enabled: Boolean = true, children: ElementsBuilder.() -> Unit) {
    require(',' !in value) { "Accordion item values must not contain a comma: $value" }
    elements += AccordionItemElement(id, build(children), value, enabled)
}

/**
 * Adds the trigger of an accordion item: its text and a chevron that turns while the item is
 * open.
 *
 * @param id the id of the trigger
 * @param text the text
 */
fun ElementsBuilder.accordionTrigger(id: String, text: Component) {
    elements += AccordionTriggerElement(id, text)
}

/**
 * Adds the content of an accordion item, shown while the item is open.
 *
 * @param id the id of the content
 * @param children the builder of the content
 */
fun ElementsBuilder.accordionContent(id: String, children: ElementsBuilder.() -> Unit) {
    elements += AccordionContentElement(id, build(children))
}

/**
 * How a tab list is drawn.
 */
enum class TabsVariant {
    /**
     * A muted pill in which the active trigger is raised.
     */
    DEFAULT,

    /**
     * Plain triggers with a line under the active one.
     */
    LINE,
}

/**
 * Tabs: a tab list and contents, of which only the content of the selected tab is shown.
 *
 * @property id the id of this element
 * @property children one tab list and the tab contents
 * @property value the value of the selected tab
 * @property orientation whether the triggers are in a row above the contents or in a column
 *           beside them
 * @property onChange whether the mod reports every change of the state at once
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class TabsElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val value: String = "",
    val orientation: Orientation = Orientation.HORIZONTAL,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The list of the triggers of tabs.
 *
 * @property id the id of this element
 * @property children the tab triggers
 * @property variant how the list is drawn
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class TabsListElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val variant: TabsVariant = TabsVariant.DEFAULT,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A trigger of tabs: selecting it shows the content with the same value.
 *
 * @property id the id of this element
 * @property value the value of the tab
 * @property text the text
 * @property icon the Lucide name of an icon before the text, or null for none
 * @property enabled whether the tab can be selected
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class TabsTriggerElement(
    override val id: String,
    val value: String = "",
    val text: Component = Component.empty(),
    val icon: String? = null,
    val enabled: Boolean = true,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * The content of a tab, shown while its tab is selected.
 *
 * @property id the id of this element
 * @property children the content, stacked
 * @property value the value of the tab it belongs to
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class TabsContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val value: String = "",
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * Adds tabs. Their children are one [tabsList] and a [tabsContent] per tab.
 *
 * @param id the id of the tabs
 * @param value the value of the tab selected at first; empty selects the first enabled tab
 * @param orientation whether the triggers are in a row above the contents or in a column beside
 *        them
 * @param onChange the handler run whenever the player selects another tab, with its value, or
 *        `null` for none
 * @param children the builder of the list and the contents
 */
fun ElementsBuilder.tabs(
    id: String,
    value: String = "",
    orientation: Orientation = Orientation.HORIZONTAL,
    onChange: ChangeHandler? = null,
    children: ElementsBuilder.() -> Unit,
) {
    elements += TabsElement(id, build(children), value, orientation, onChange)
}

/**
 * Adds the list of the triggers of tabs. Its children are [tabsTrigger]s.
 *
 * @param id the id of the list
 * @param variant how the list is drawn
 * @param children the builder of the triggers
 */
fun ElementsBuilder.tabsList(id: String, variant: TabsVariant = TabsVariant.DEFAULT, children: ElementsBuilder.() -> Unit) {
    elements += TabsListElement(id, build(children), variant)
}

/**
 * Adds a trigger of tabs.
 *
 * @param id the id of the trigger
 * @param value the value of the tab
 * @param text the text
 * @param icon the Lucide name of an icon before the text, or `null` for none
 * @param enabled whether the tab can be selected
 */
fun ElementsBuilder.tabsTrigger(id: String, value: String, text: Component, icon: String? = null, enabled: Boolean = true) {
    elements += TabsTriggerElement(id, value, text, icon, enabled)
}

/**
 * Adds the content of a tab, shown while its tab is selected.
 *
 * @param id the id of the content
 * @param value the value of the tab it belongs to
 * @param children the builder of the content
 */
fun ElementsBuilder.tabsContent(id: String, value: String, children: ElementsBuilder.() -> Unit) {
    elements += TabsContentElement(id, build(children), value)
}

