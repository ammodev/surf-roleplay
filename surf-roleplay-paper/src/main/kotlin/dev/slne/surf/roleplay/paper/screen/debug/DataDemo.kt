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
import dev.slne.surf.roleplay.api.client.common.screen.ElementsBuilder
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupAlign
import dev.slne.surf.roleplay.api.client.common.screen.OpenScreen
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.ScreenThemes
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.api.client.common.screen.TextKind
import dev.slne.surf.roleplay.api.client.common.screen.badge
import dev.slne.surf.roleplay.api.client.common.screen.card
import dev.slne.surf.roleplay.api.client.common.screen.cardContent
import dev.slne.surf.roleplay.api.client.common.screen.cardDescription
import dev.slne.surf.roleplay.api.client.common.screen.cardHeader
import dev.slne.surf.roleplay.api.client.common.screen.cardTitle
import dev.slne.surf.roleplay.api.client.common.screen.chart
import dev.slne.surf.roleplay.api.client.common.screen.chatMessage
import dev.slne.surf.roleplay.api.client.common.screen.chatView
import dev.slne.surf.roleplay.api.client.common.screen.dataTable
import dev.slne.surf.roleplay.api.client.common.screen.dataTableCell
import dev.slne.surf.roleplay.api.client.common.screen.dataTableColumn
import dev.slne.surf.roleplay.api.client.common.screen.dataTableRow
import dev.slne.surf.roleplay.api.client.common.screen.inputGroup
import dev.slne.surf.roleplay.api.client.common.screen.inputGroupAddon
import dev.slne.surf.roleplay.api.client.common.screen.inputGroupButton
import dev.slne.surf.roleplay.api.client.common.screen.screen
import dev.slne.surf.roleplay.api.client.common.screen.select
import dev.slne.surf.roleplay.api.client.common.screen.table
import dev.slne.surf.roleplay.api.client.common.screen.tableBody
import dev.slne.surf.roleplay.api.client.common.screen.tableCaption
import dev.slne.surf.roleplay.api.client.common.screen.tableCell
import dev.slne.surf.roleplay.api.client.common.screen.tableFooter
import dev.slne.surf.roleplay.api.client.common.screen.tableHead
import dev.slne.surf.roleplay.api.client.common.screen.tableHeader
import dev.slne.surf.roleplay.api.client.common.screen.tableRow
import dev.slne.surf.roleplay.api.client.common.screen.text
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
    fun definition(hooks: Hooks, theme: String, variant: ScreenVariant): ScreenDefinition = screen(Component.text("Daten")) {
        this.theme = theme
        this.variant = variant
        column("root", width = ElementSize.fixed(620), gap = 12, crossAlign = Alignment.STRETCH) {
            row("theme_row", gap = 4, crossAlign = Alignment.CENTER) {
                select("theme", InputsDemo.THEMES, selected = theme, width = ElementSize.grow(), onChange = { change -> hooks.reopen(change.value, variant) })
                select("variant", InputsDemo.VARIANTS, selected = variant.name, width = ElementSize.fixed(90), onChange = { change ->
                    hooks.reopen(theme, ScreenVariant.valueOf(change.value))
                })
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
    private fun ElementsBuilder.section(id: String, title: String, content: ElementsBuilder.() -> Unit) {
        column(id, gap = 6, crossAlign = Alignment.STRETCH) {
            text("${id}_title", Component.text(title), TextKind.H3)
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
    private fun ElementsBuilder.tables() = section("tables", "Tabelle") {
        table("missions") {
            tableHeader("missions_header") {
                tableRow("missions_head") {
                    tableHead("missions_head_number", Component.text("Einsatz"))
                    tableHead("missions_head_unit", Component.text("Einheit"))
                    tableHead("missions_head_status", Component.text("Status"))
                    tableHead("missions_head_amount", Component.text("Betrag"), Alignment.END)
                }
            }
            tableBody("missions_body") {
                MISSIONS.forEachIndexed { index, (number, unit, status, amount) ->
                    tableRow("mission_$index") {
                        tableCell("mission_${index}_number", Component.text(number))
                        tableCell("mission_${index}_unit", Component.text(unit))
                        tableCell("mission_${index}_status") {
                            badge("mission_${index}_badge", Component.text(status), variant = if (status == "Offen") BadgeVariant.DESTRUCTIVE else BadgeVariant.OUTLINE)
                        }
                        tableCell("mission_${index}_amount", Component.text("$amount €"), Alignment.END)
                    }
                }
            }
            tableFooter("missions_footer") {
                tableRow("missions_total") {
                    tableCell("missions_total_label", Component.text("Summe"))
                    tableCell("missions_total_unit", Component.empty())
                    tableCell("missions_total_status", Component.empty())
                    tableCell("missions_total_amount", Component.text("1.200,00 €"), Alignment.END)
                }
            }
            tableCaption("missions_caption", Component.text("Die letzten Einsätze der Leitstelle."))
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
    private fun ElementsBuilder.dataTables(hooks: Hooks) = section("data_tables", "Datentabelle") {
        dataTable(
            "units",
            pageSize = 5,
            selectable = true,
            filterColumn = "name",
            filterPlaceholder = Component.text("Einheiten filtern..."),
            onChange = { change ->
                val view = DataTableView.parse(change.value) ?: return@dataTable
                val sort = view.sort?.let { "${it} ${if (view.desc) "absteigend" else "aufsteigend"}" } ?: "unsortiert"
                hooks.report(Component.text("Tabelle: $sort, Filter \"${view.filter}\", Seite ${view.page + 1}, ${view.selected.size} ausgewählt", NamedTextColor.AQUA))
            },
        ) {
            dataTableColumn("units_column_status", "status", Component.text("Status"))
            dataTableColumn("units_column_name", "name", Component.text("Name"), sortable = true)
            dataTableColumn("units_column_calls", "calls", Component.text("Einsätze"), sortable = true, align = Alignment.END)
            UNITS.forEach { (id, name, status, calls) ->
                dataTableRow(id, selectable = status != "Außer Dienst") {
                    dataTableCell("${id}_status", status) {
                        badge("${id}_badge", Component.text(status), variant = if (status == "Frei") BadgeVariant.SECONDARY else BadgeVariant.OUTLINE)
                    }
                    dataTableCell("${id}_name", Component.text(name), name)
                    dataTableCell("${id}_calls", Component.text(calls), calls)
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
    private fun ElementsBuilder.charts() = section("charts", "Diagramme") {
        chartRow("charts_1") {
            chartCard("area_card", "Fläche – gestapelt", "Einsätze pro Monat") {
                chart("area", ChartKind.AREA, MONTHS, listOf(RESCUE, FIRE), width = ElementSize.grow(), stacked = true, legend = true)
            }
            chartCard("bar_card", "Balken – mehrere", "Mit Werteachse") {
                chart("bar", ChartKind.BAR, MONTHS, listOf(RESCUE, FIRE), width = ElementSize.grow(), valueAxis = true, indicator = ChartIndicator.DASHED)
            }
        }
        chartRow("charts_2") {
            chartCard("bar_horizontal_card", "Balken – horizontal", "Mit Werten") {
                chart("bar_horizontal", ChartKind.BAR, MONTHS, listOf(RESCUE), width = ElementSize.grow(), horizontal = true, labels = true, grid = false)
            }
            chartCard("bar_stacked_card", "Balken – gestapelt", "Mit Legende") {
                chart("bar_stacked", ChartKind.BAR, MONTHS, listOf(RESCUE, FIRE), width = ElementSize.grow(), stacked = true, legend = true, indicator = ChartIndicator.LINE)
            }
        }
        chartRow("charts_3") {
            chartCard("line_card", "Linie – weich", "Mit Punkten") {
                chart("line", ChartKind.LINE, MONTHS, listOf(RESCUE, FIRE), width = ElementSize.grow(), dots = true)
            }
            chartCard("line_step_card", "Linie – Stufen", "Linear und Stufen") {
                chart("line_step", ChartKind.LINE, MONTHS, listOf(RESCUE.copy(color = 3)), width = ElementSize.grow(), curve = ChartCurve.STEP)
            }
        }
        val browsers = listOf("Rettung", "Feuer", "Polizei", "THW", "Sonstige").map { Component.text(it) }
        val shares = ChartSeries("calls", Component.text("Einsätze"), 1, listOf(275.0, 200.0, 187.0, 173.0, 90.0))
        chartRow("charts_4") {
            chartCard("pie_card", "Kreis – Werte", "Einsätze nach Art") {
                chart("pie", ChartKind.PIE, browsers, listOf(shares), width = ElementSize.grow(), labels = true, legend = true)
            }
            chartCard("donut_card", "Kreis – Ring", "Mit Summe") {
                chart("donut", ChartKind.PIE, browsers, listOf(shares), width = ElementSize.grow(), donut = true)
            }
        }
        chartRow("charts_5") {
            chartCard("radar_card", "Netz", "Zwei Serien") {
                chart("radar", ChartKind.RADAR, MONTHS, listOf(RESCUE, FIRE), width = ElementSize.grow(), dots = true, legend = true)
            }
            chartCard("radial_card", "Radial", "Einsätze nach Art") {
                chart("radial", ChartKind.RADIAL, browsers, listOf(shares), width = ElementSize.grow(), legend = true)
            }
        }
    }

    /**
     * Adds a row of two chart cards of equal width.
     *
     * @param id the id of the row
     * @param content the builder of the cards
     */
    private fun ElementsBuilder.chartRow(id: String, content: ElementsBuilder.() -> Unit) {
        row(id, width = ElementSize.grow(), gap = 12, children = content)
    }

    /**
     * Adds a card with a title, a description and a chart.
     *
     * @param id the id of the card
     * @param title the title
     * @param description the description
     * @param content the builder of the chart
     */
    private fun ElementsBuilder.chartCard(id: String, title: String, description: String, content: ElementsBuilder.() -> Unit) {
        card(id, width = ElementSize.grow()) {
            cardHeader("${id}_header") {
                cardTitle("${id}_title", Component.text(title))
                cardDescription("${id}_description", Component.text(description))
            }
            cardContent("${id}_content", content)
        }
    }

    /**
     * Adds a chat view with a short conversation and a composer. Sending appends the message and
     * an answer of the dispatch centre, and reports the message in chat.
     *
     * @param hooks what the page needs from its surroundings
     */
    private fun ElementsBuilder.chat(hooks: Hooks) = section("chat", "Chat") {
        var count = 0
        chatView("chat_view", ElementSize.fixed(150), width = ElementSize.grow()) {
            chatMessage("chat_m1", name = Component.text("Leitstelle"), time = Component.text("12:30"), fallback = "LS") {
                text("chat_m1_text", Component.text("RTW 1, bitte melden."))
            }
            chatMessage("chat_m2", own = true, name = Component.text("RTW 1"), time = Component.text("12:31"), playerId = hooks.playerId, fallback = "R1") {
                text("chat_m2_text", Component.text("RTW 1 hört, Standort Wache Nord."))
            }
            chatMessage("chat_m3", name = Component.text("Leitstelle"), time = Component.text("12:31"), fallback = "LS") {
                text("chat_m3_text", Component.text("Einsatz: Verkehrsunfall B7, zwei Verletzte."))
            }
        }
        inputGroup("chat_composer", width = ElementSize.grow()) {
            textInput("chat_input", placeholder = Component.text("Nachricht schreiben..."), maxLength = 200, width = ElementSize.grow())
            inputGroupAddon("chat_actions", InputGroupAlign.INLINE_END) {
                inputGroupButton("chat_send", Component.text("Senden"), icon = "send", variant = ButtonVariant.DEFAULT, size = ButtonSize.XS, onClick = { click ->
                    val message = click.values.text("chat_input").orEmpty().trim()
                    if (message.isEmpty()) return@inputGroupButton
                    count++
                    send(click.screen, hooks, count, message)
                })
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
                chatMessage("chat_sent_$count", own = true, name = Component.text("RTW 1"), time = Component.text(time), playerId = hooks.playerId, fallback = "R1") {
                    text("chat_sent_${count}_text", Component.text(message))
                }
                chatMessage("chat_reply_$count", name = Component.text("Leitstelle"), time = Component.text(time), fallback = "LS") {
                    text("chat_reply_${count}_text", Component.text("Verstanden."))
                }
            }
            setValue("chat_input", "")
        }
        hooks.report(Component.text("Nachricht gesendet: $message", NamedTextColor.GREEN))
    }
}
