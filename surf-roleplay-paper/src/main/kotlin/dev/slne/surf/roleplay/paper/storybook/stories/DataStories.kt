package dev.slne.surf.roleplay.paper.storybook.stories

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.BadgeVariant
import dev.slne.surf.roleplay.api.client.common.screen.ButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.ButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.ChartCurve
import dev.slne.surf.roleplay.api.client.common.screen.ChartIndicator
import dev.slne.surf.roleplay.api.client.common.screen.ChartKind
import dev.slne.surf.roleplay.api.client.common.screen.ChartSeries
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupAlign
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Badge
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Card
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CardContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CardHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CardTitle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Chart
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ChatMessage
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ChatView
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DataTable
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DataTableCell
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DataTableColumn
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DataTableRow
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Input
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputGroupAddon
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputGroupButton
import dev.slne.surf.roleplay.api.client.common.screen.dsl.P
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Row
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Table
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TableBody
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TableCaption
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TableCell
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TableFooter
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TableHead
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TableHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TableRow
import dev.slne.surf.roleplay.paper.storybook.Story
import dev.slne.surf.roleplay.paper.storybook.StoryCategory
import dev.slne.surf.roleplay.paper.storybook.StoryContext
import dev.slne.surf.roleplay.paper.storybook.slug
import dev.slne.surf.roleplay.paper.storybook.storySection
import net.kyori.adventure.text.Component

/**
 * The stories of the table, chart and chat components, in sidebar order.
 */
internal val DATA_STORIES: List<Story> = listOf(
    Story("table", "Tabelle", StoryCategory.DATA) { tableStory() },
    Story("data-table", "Datentabelle", StoryCategory.DATA) { dataTableStory(it) },
    Story("chart", "Diagramm", StoryCategory.DATA) { chartStory() },
    Story("chat", "Chat", StoryCategory.DATA) { chatStory(it) },
)

/**
 * The missions of the table story: number, unit, status, amount.
 */
private val MISSIONS = listOf(
    listOf("E-1001", "RTW 1", "Abgeschlossen", "250,00"),
    listOf("E-1002", "NEF 2", "Unterwegs", "150,00"),
    listOf("E-1003", "KTW 3", "Offen", "350,00"),
    listOf("E-1004", "RTW 4", "Abgeschlossen", "450,00"),
)

/**
 * The units of the data table story: id, name, status, calls.
 */
private val UNITS = listOf(
    listOf("u1", "RTW 1", "Frei", "12"),
    listOf("u2", "NEF 2", "Im Einsatz", "7"),
    listOf("u3", "KTW 3", "Frei", "21"),
    listOf("u4", "RTW 4", "Außer Dienst", "3"),
    listOf("u5", "RTW Nord", "Im Einsatz", "15"),
    listOf("u6", "Leitstelle", "Frei", "40"),
    listOf("u7", "RTH Christoph", "Frei", "9"),
)

/**
 * The months the charts show.
 */
private val MONTHS = listOf("Jan", "Feb", "Mär", "Apr", "Mai", "Jun").map { Component.text(it) }

/**
 * The rescue calls per month.
 */
private val RESCUE = ChartSeries("rescue", Component.text("Rettung"), 1, listOf(186.0, 305.0, 237.0, 73.0, 209.0, 214.0))

/**
 * The fire calls per month.
 */
private val FIRE = ChartSeries("fire", Component.text("Feuer"), 2, listOf(80.0, 200.0, 120.0, 190.0, 130.0, 140.0))

/**
 * The kinds of calls the pie, radial and radar charts show.
 */
private val CALL_KINDS = listOf("Rettung", "Feuer", "Polizei", "THW", "Sonstige").map { Component.text(it) }

/**
 * The number of calls per kind.
 */
private val CALL_SHARES = ChartSeries("calls", Component.text("Einsätze"), 1, listOf(275.0, 200.0, 187.0, 173.0, 90.0))

/**
 * Shows a table of missions with a status badge per row, a total in the footer and a caption.
 */
private fun ComponentScope.tableStory() {
    storySection("Beispiel") {
        Table {
            TableHeader {
                TableRow {
                    TableHead(Component.text("Einsatz"))
                    TableHead(Component.text("Einheit"))
                    TableHead(Component.text("Status"))
                    TableHead(Component.text("Betrag"), Alignment.END)
                }
            }
            TableBody {
                MISSIONS.forEach { (number, unit, status, amount) ->
                    TableRow {
                        TableCell(Component.text(number))
                        TableCell(Component.text(unit))
                        TableCell { Badge(status, variant = if (status == "Offen") BadgeVariant.DESTRUCTIVE else BadgeVariant.OUTLINE) }
                        TableCell(Component.text("$amount €"), Alignment.END)
                    }
                }
            }
            TableFooter {
                TableRow {
                    TableCell(Component.text("Summe"))
                    TableCell(Component.empty())
                    TableCell(Component.empty())
                    TableCell(Component.text("1.200,00 €"), Alignment.END)
                }
            }
            TableCaption(Component.text("Die letzten Einsätze der Leitstelle."))
        }
    }
}

/**
 * Shows a selectable, sortable and filterable data table of units, five per page, in which units
 * out of service cannot be selected.
 *
 * @param context the story context
 */
private fun ComponentScope.dataTableStory(context: StoryContext) {
    storySection("Beispiel") {
        DataTable(pageSize = 5, selectable = true, filterColumn = "name", filterPlaceholder = Component.text("Einheiten filtern …"), id = "units", onChange = context.changed) {
            DataTableColumn("status", Component.text("Status"))
            DataTableColumn("name", Component.text("Name"), sortable = true)
            DataTableColumn("calls", Component.text("Einsätze"), sortable = true, align = Alignment.END)
            UNITS.forEach { (id, name, status, calls) ->
                DataTableRow(selectable = status != "Außer Dienst", id = id) {
                    DataTableCell(status) {
                        Badge(status, variant = if (status == "Frei") BadgeVariant.SECONDARY else BadgeVariant.OUTLINE)
                    }
                    DataTableCell(Component.text(name), name)
                    DataTableCell(Component.text(calls), calls)
                }
            }
        }
    }
}

/**
 * Shows every chart kind, every line curve, every tooltip indicator and the chart options.
 */
private fun ComponentScope.chartStory() {
    storySection("Arten") {
        ChartKind.entries.chunked(2).forEach { kinds ->
            Row(width = ElementSize.grow(), gap = 12) {
                kinds.forEach { kind ->
                    chartCard(kind.slug()) {
                        when (kind) {
                            ChartKind.PIE, ChartKind.RADIAL -> Chart(kind, CALL_KINDS, listOf(CALL_SHARES), width = ElementSize.grow(), legend = true)
                            else -> Chart(kind, MONTHS, listOf(RESCUE, FIRE), width = ElementSize.grow(), legend = true)
                        }
                    }
                }
            }
        }
    }
    storySection("Kurven und Anzeiger") {
        Row(width = ElementSize.grow(), gap = 12) {
            ChartCurve.entries.forEach { curve ->
                chartCard("Linie ${curve.slug()}") { Chart(ChartKind.LINE, MONTHS, listOf(RESCUE), width = ElementSize.grow(), curve = curve, dots = true) }
            }
        }
        Row(width = ElementSize.grow(), gap = 12) {
            ChartIndicator.entries.forEach { indicator ->
                chartCard("Anzeiger ${indicator.slug()}") { Chart(ChartKind.BAR, MONTHS, listOf(RESCUE, FIRE), width = ElementSize.grow(), indicator = indicator) }
            }
        }
    }
    storySection("Optionen") {
        Row(width = ElementSize.grow(), gap = 12) {
            chartCard("Gestapelt, Werteachse") { Chart(ChartKind.BAR, MONTHS, listOf(RESCUE, FIRE), width = ElementSize.grow(), stacked = true, valueAxis = true) }
            chartCard("Horizontal, Werte") { Chart(ChartKind.BAR, MONTHS, listOf(RESCUE), width = ElementSize.grow(), horizontal = true, labels = true, grid = false) }
            chartCard("Ring") { Chart(ChartKind.PIE, CALL_KINDS, listOf(CALL_SHARES), width = ElementSize.grow(), donut = true, labels = true) }
        }
    }
}

/**
 * Adds a card with a title and a chart that shares the row's width.
 *
 * @param title the title
 * @param content the builder of the chart
 */
private fun ComponentScope.chartCard(title: String, content: ComponentScope.() -> Unit) {
    Card(width = ElementSize.grow()) {
        CardHeader { CardTitle(Component.text(title)) }
        CardContent(children = content)
    }
}

/**
 * Shows a chat view with own and other messages, a player head, and a composer whose send button
 * reports.
 *
 * @param context the story context
 */
private fun ComponentScope.chatStory(context: StoryContext) {
    storySection("Beispiel") {
        ChatView(ElementSize.fixed(150), width = ElementSize.grow()) {
            ChatMessage(name = Component.text("Leitstelle"), time = Component.text("12:30"), fallback = "LS") { P("RTW 1, bitte melden.") }
            ChatMessage(own = true, name = Component.text("RTW 1"), time = Component.text("12:31"), playerId = context.playerId, fallback = "R1") {
                P("RTW 1 hört, Standort Wache Nord.")
            }
            ChatMessage(name = Component.text("Leitstelle"), time = Component.text("12:31"), fallback = "LS") { P("Einsatz: Verkehrsunfall B7, zwei Verletzte.") }
        }
        InputGroup(width = ElementSize.grow()) {
            Input(placeholder = Component.text("Nachricht schreiben …"), maxLength = 200, width = ElementSize.grow(), id = "chat_input")
            InputGroupAddon(InputGroupAlign.INLINE_END) {
                InputGroupButton("Senden", icon = "send", variant = ButtonVariant.DEFAULT, size = ButtonSize.XS, id = "chat_send", onClick = context.clicked)
            }
        }
    }
}
