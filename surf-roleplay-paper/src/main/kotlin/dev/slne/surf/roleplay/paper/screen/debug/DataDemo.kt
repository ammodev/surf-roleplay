package dev.slne.surf.roleplay.paper.screen.debug

import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.BadgeVariant
import dev.slne.surf.roleplay.api.client.common.screen.ButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.ButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.ChartCurve
import dev.slne.surf.roleplay.api.client.common.screen.ChartIndicator
import dev.slne.surf.roleplay.api.client.common.screen.ChartKind
import dev.slne.surf.roleplay.api.client.common.screen.ChartSeries
import dev.slne.surf.roleplay.api.client.common.screen.DataTableView
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupAlign
import dev.slne.surf.roleplay.api.client.common.screen.OpenScreen
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.ScreenThemes
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Badge
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Card
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CardContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CardDescription
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CardHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.CardTitle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Chart
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ChatMessage
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ChatView
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DataTable
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DataTableCell
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DataTableColumn
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DataTableRow
import dev.slne.surf.roleplay.api.client.common.screen.dsl.H3
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Input
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputGroupAddon
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputGroupButton
import dev.slne.surf.roleplay.api.client.common.screen.dsl.P
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Row
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Screen
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Select
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Table
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TableBody
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TableCaption
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TableCell
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TableFooter
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TableHead
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TableHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.TableRow
import dev.slne.surf.roleplay.api.client.paper.screen.ScreenService
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * The page of the screen debug command that shows the data, chat and chart components. Data
 * table changes and sent chat messages are reported in chat, and a sent message is answered.
 */
object DataDemo {

    /**
     * What the page needs from its surroundings.
     *
     * @property report sends a report of a change to the player
     * @property reopen opens the page again with another theme and variant
     * @property playerId the player whose head is the avatar of the player's own messages
     * @property now returns the time shown on new chat messages
     */
    class Hooks(
        val report: (Component) -> Unit,
        val reopen: (String, ScreenVariant) -> Unit,
        val playerId: UUID? = null,
        val now: () -> String = { LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) },
    )

    /**
     * Opens the page.
     *
     * @param player the player
     * @param theme the name of the theme to draw the page with
     * @param variant the light or dark variant of the theme
     */
    fun open(player: Player, theme: String = ScreenThemes.DEFAULT, variant: ScreenVariant = ScreenVariant.DARK) {
        val hooks = Hooks(
            report = player::sendMessage,
            reopen = { chosenTheme, chosenVariant -> open(player, chosenTheme, chosenVariant) },
            playerId = player.uniqueId,
        )
        ScreenService.open(player, definition(hooks, theme, variant))
    }

    /**
     * Builds the page.
     *
     * @param hooks what the page needs from its surroundings
     * @param theme the name of the theme
     * @param variant the variant of the theme
     * @return the page
     */
    fun definition(hooks: Hooks, theme: String, variant: ScreenVariant): ScreenDefinition = Screen(Component.text("Daten"), theme = theme, variant = variant) {
        Column(width = ElementSize.fixed(620), gap = 12, crossAlign = Alignment.STRETCH, id = "root") {
            Row(gap = 4, crossAlign = Alignment.CENTER, id = "theme_row") {
                Select(InputsDemo.THEMES, selected = theme, width = ElementSize.grow(), onChange = { change -> hooks.reopen(change.value, variant) }, id = "theme")
                Select(InputsDemo.VARIANTS, selected = variant.name, width = ElementSize.fixed(90), onChange = { change ->
                    hooks.reopen(theme, ScreenVariant.valueOf(change.value))
                }, id = "variant")
            }
            tables()
            dataTables(hooks)
            charts()
            chat(hooks)
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
     * The missions of the table section: number, unit, status, amount.
     */
    private val MISSIONS = listOf(
        listOf("E-1001", "RTW 1", "Abgeschlossen", "250,00"),
        listOf("E-1002", "NEF 2", "Unterwegs", "150,00"),
        listOf("E-1003", "KTW 3", "Offen", "350,00"),
        listOf("E-1004", "RTW 4", "Abgeschlossen", "450,00"),
    )

    /**
     * Adds a table of missions with a status badge per row, a total in the footer and a caption.
     */
    private fun ComponentScope.tables() = section("tables", "Tabelle") {
        Table(id = "missions") {
            TableHeader(id = "missions_header") {
                TableRow(id = "missions_head") {
                    TableHead(Component.text("Einsatz"), id = "missions_head_number")
                    TableHead(Component.text("Einheit"), id = "missions_head_unit")
                    TableHead(Component.text("Status"), id = "missions_head_status")
                    TableHead(Component.text("Betrag"), Alignment.END, id = "missions_head_amount")
                }
            }
            TableBody(id = "missions_body") {
                MISSIONS.forEachIndexed { index, (number, unit, status, amount) ->
                    TableRow(id = "mission_$index") {
                        TableCell(Component.text(number), id = "mission_${index}_number")
                        TableCell(Component.text(unit), id = "mission_${index}_unit")
                        TableCell(id = "mission_${index}_status") {
                            Badge(Component.text(status), variant = if (status == "Offen") BadgeVariant.DESTRUCTIVE else BadgeVariant.OUTLINE, id = "mission_${index}_badge")
                        }
                        TableCell(Component.text("$amount €"), Alignment.END, id = "mission_${index}_amount")
                    }
                }
            }
            TableFooter(id = "missions_footer") {
                TableRow(id = "missions_total") {
                    TableCell(Component.text("Summe"), id = "missions_total_label")
                    TableCell(Component.empty(), id = "missions_total_unit")
                    TableCell(Component.empty(), id = "missions_total_status")
                    TableCell(Component.text("1.200,00 €"), Alignment.END, id = "missions_total_amount")
                }
            }
            TableCaption(Component.text("Die letzten Einsätze der Leitstelle."), id = "missions_caption")
        }
    }

    /**
     * The units of the data table section: id, name, status, calls.
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
     * Adds a selectable data table of units, five per page, filtered by name, whose changes are
     * reported in chat. Units out of service cannot be selected.
     *
     * @param hooks what the page needs from its surroundings
     */
    private fun ComponentScope.dataTables(hooks: Hooks) = section("data_tables", "Datentabelle") {
        DataTable(pageSize = 5, selectable = true, filterColumn = "name", filterPlaceholder = Component.text("Einheiten filtern..."), onChange = { change ->
                val view = DataTableView.parse(change.value) ?: return@DataTable
                val sort = view.sort?.let { "${it} ${if (view.desc) "absteigend" else "aufsteigend"}" } ?: "unsortiert"
                hooks.report(Component.text("Tabelle: $sort, Filter \"${view.filter}\", Seite ${view.page + 1}, ${view.selected.size} ausgewählt", NamedTextColor.AQUA))
            }, id = "units") {
            DataTableColumn("status", Component.text("Status"), id = "units_column_status")
            DataTableColumn("name", Component.text("Name"), sortable = true, id = "units_column_name")
            DataTableColumn("calls", Component.text("Einsätze"), sortable = true, align = Alignment.END, id = "units_column_calls")
            UNITS.forEach { (id, name, status, calls) ->
                DataTableRow(selectable = status != "Außer Dienst", id = id) {
                    DataTableCell(status, id = "${id}_status") {
                        Badge(Component.text(status), variant = if (status == "Frei") BadgeVariant.SECONDARY else BadgeVariant.OUTLINE, id = "${id}_badge")
                    }
                    DataTableCell(Component.text(name), name, id = "${id}_name")
                    DataTableCell(Component.text(calls), calls, id = "${id}_calls")
                }
            }
        }
    }

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
     * Adds charts of every family, two per row, each in a card.
     */
    private fun ComponentScope.charts() = section("charts", "Diagramme") {
        chartRow("charts_1") {
            chartCard("area_card", "Fläche – gestapelt", "Einsätze pro Monat") {
                Chart(ChartKind.AREA, MONTHS, listOf(RESCUE, FIRE), width = ElementSize.grow(), stacked = true, legend = true, id = "area")
            }
            chartCard("bar_card", "Balken – mehrere", "Mit Werteachse") {
                Chart(ChartKind.BAR, MONTHS, listOf(RESCUE, FIRE), width = ElementSize.grow(), valueAxis = true, indicator = ChartIndicator.DASHED, id = "bar")
            }
        }
        chartRow("charts_2") {
            chartCard("bar_horizontal_card", "Balken – horizontal", "Mit Werten") {
                Chart(ChartKind.BAR, MONTHS, listOf(RESCUE), width = ElementSize.grow(), horizontal = true, labels = true, grid = false, id = "bar_horizontal")
            }
            chartCard("bar_stacked_card", "Balken – gestapelt", "Mit Legende") {
                Chart(ChartKind.BAR, MONTHS, listOf(RESCUE, FIRE), width = ElementSize.grow(), stacked = true, legend = true, indicator = ChartIndicator.LINE, id = "bar_stacked")
            }
        }
        chartRow("charts_3") {
            chartCard("line_card", "Linie – weich", "Mit Punkten") {
                Chart(ChartKind.LINE, MONTHS, listOf(RESCUE, FIRE), width = ElementSize.grow(), dots = true, id = "line")
            }
            chartCard("line_step_card", "Linie – Stufen", "Linear und Stufen") {
                Chart(ChartKind.LINE, MONTHS, listOf(RESCUE.copy(color = 3)), width = ElementSize.grow(), curve = ChartCurve.STEP, id = "line_step")
            }
        }
        val browsers = listOf("Rettung", "Feuer", "Polizei", "THW", "Sonstige").map { Component.text(it) }
        val shares = ChartSeries("calls", Component.text("Einsätze"), 1, listOf(275.0, 200.0, 187.0, 173.0, 90.0))
        chartRow("charts_4") {
            chartCard("pie_card", "Kreis – Werte", "Einsätze nach Art") {
                Chart(ChartKind.PIE, browsers, listOf(shares), width = ElementSize.grow(), labels = true, legend = true, id = "pie")
            }
            chartCard("donut_card", "Kreis – Ring", "Mit Summe") {
                Chart(ChartKind.PIE, browsers, listOf(shares), width = ElementSize.grow(), donut = true, id = "donut")
            }
        }
        chartRow("charts_5") {
            chartCard("radar_card", "Netz", "Zwei Serien") {
                Chart(ChartKind.RADAR, MONTHS, listOf(RESCUE, FIRE), width = ElementSize.grow(), dots = true, legend = true, id = "radar")
            }
            chartCard("radial_card", "Radial", "Einsätze nach Art") {
                Chart(ChartKind.RADIAL, browsers, listOf(shares), width = ElementSize.grow(), legend = true, id = "radial")
            }
        }
    }

    /**
     * Adds a row of two chart cards of equal width.
     *
     * @param id the id of the row
     * @param content the builder of the cards
     */
    private fun ComponentScope.chartRow(id: String, content: ComponentScope.() -> Unit) {
        Row(width = ElementSize.grow(), gap = 12, id = id, children = content)
    }

    /**
     * Adds a card with a title, a description and a chart.
     *
     * @param id the id of the card
     * @param title the title
     * @param description the description
     * @param content the builder of the chart
     */
    private fun ComponentScope.chartCard(id: String, title: String, description: String, content: ComponentScope.() -> Unit) {
        Card(width = ElementSize.grow(), id = id) {
            CardHeader(id = "${id}_header") {
                CardTitle(Component.text(title), id = "${id}_title")
                CardDescription(Component.text(description), id = "${id}_description")
            }
            CardContent(id = "${id}_content", children = content)
        }
    }

    /**
     * Adds a chat view with a short conversation and a composer. Sending appends the message and
     * an answer of the dispatch centre, and reports the message in chat.
     *
     * @param hooks what the page needs from its surroundings
     */
    private fun ComponentScope.chat(hooks: Hooks) = section("chat", "Chat") {
        var count = 0
        ChatView(ElementSize.fixed(150), width = ElementSize.grow(), id = "chat_view") {
            ChatMessage(name = Component.text("Leitstelle"), time = Component.text("12:30"), fallback = "LS", id = "chat_m1") {
                P(Component.text("RTW 1, bitte melden."), id = "chat_m1_text")
            }
            ChatMessage(own = true, name = Component.text("RTW 1"), time = Component.text("12:31"), playerId = hooks.playerId, fallback = "R1", id = "chat_m2") {
                P(Component.text("RTW 1 hört, Standort Wache Nord."), id = "chat_m2_text")
            }
            ChatMessage(name = Component.text("Leitstelle"), time = Component.text("12:31"), fallback = "LS", id = "chat_m3") {
                P(Component.text("Einsatz: Verkehrsunfall B7, zwei Verletzte."), id = "chat_m3_text")
            }
        }
        InputGroup(width = ElementSize.grow(), id = "chat_composer") {
            Input(placeholder = Component.text("Nachricht schreiben..."), maxLength = 200, width = ElementSize.grow(), id = "chat_input")
            InputGroupAddon(InputGroupAlign.INLINE_END, id = "chat_actions") {
                InputGroupButton(Component.text("Senden"), icon = "send", variant = ButtonVariant.DEFAULT, size = ButtonSize.XS, onClick = { click ->
                    val message = click.values.text("chat_input").orEmpty().trim()
                    if (message.isEmpty()) return@InputGroupButton
                    count++
                    send(click.screen, hooks, count, message)
                }, id = "chat_send")
            }
        }
    }

    /**
     * Appends a sent message and the answer of the dispatch centre to the chat view, clears the
     * composer and reports the message.
     *
     * @param screen the open page
     * @param hooks what the page needs from its surroundings
     * @param count the number of the sent message
     * @param message the text of the message
     */
    private fun send(screen: OpenScreen, hooks: Hooks, count: Int, message: String) {
        val time = hooks.now()
        screen.patch {
            append("chat_view") {
                ChatMessage(own = true, name = Component.text("RTW 1"), time = Component.text(time), playerId = hooks.playerId, fallback = "R1", id = "chat_sent_$count") {
                    P(Component.text(message), id = "chat_sent_${count}_text")
                }
                ChatMessage(name = Component.text("Leitstelle"), time = Component.text(time), fallback = "LS", id = "chat_reply_$count") {
                    P(Component.text("Verstanden."), id = "chat_reply_${count}_text")
                }
            }
            setValue("chat_input", "")
        }
        hooks.report(Component.text("Nachricht gesendet: $message", NamedTextColor.GREEN))
    }
}
