package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.Corners
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.LayoutBox
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.protocol.screen.ChartCurve
import dev.slne.surf.roleplay.protocol.screen.ChartIndicator
import dev.slne.surf.roleplay.protocol.screen.ChartKind
import dev.slne.surf.roleplay.protocol.screen.ChartSeries
import dev.slne.surf.roleplay.protocol.screen.SizeMode
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * How a chart is drawn, apart from its data.
 *
 * @property stacked whether the series of area and bar charts are stacked
 * @property horizontal whether bars run from the left instead of from the bottom
 * @property curve how lines run between their points
 * @property dots whether line charts mark their points
 * @property grid whether the value grid is drawn
 * @property categoryAxis whether the categories are labelled
 * @property valueAxis whether the values are labelled
 * @property legend whether the series are listed below the chart
 * @property tooltip whether hovering shows a tooltip
 * @property indicator how the tooltip marks each series
 * @property donut whether a pie chart is drawn as a ring
 * @property labels whether pie slices and bars show their values
 */
data class ChartOptions(
    val stacked: Boolean = false,
    val horizontal: Boolean = false,
    val curve: ChartCurve = ChartCurve.NATURAL,
    val dots: Boolean = false,
    val grid: Boolean = true,
    val categoryAxis: Boolean = true,
    val valueAxis: Boolean = false,
    val legend: Boolean = false,
    val tooltip: Boolean = true,
    val indicator: ChartIndicator = ChartIndicator.DOT,
    val donut: Boolean = false,
    val labels: Boolean = false,
)

/**
 * A chart: the series of its categories drawn as areas, bars, lines, pie slices, radar polygons
 * or radial rings in the theme's chart colours, with an optional legend below. Hovering a
 * category shows a tooltip with its label and every series' value.
 *
 * @param id the id of the widget
 * @property kind the family of the chart
 * @property categories the labels of the categories as component JSON
 * @property series the series
 * @property categoryColors the chart colour numbers of the categories of pie and radial charts
 * @property options how the chart is drawn
 */
class ChartWidget(
    id: String,
    val kind: ChartKind,
    val categories: List<String>,
    val series: List<ChartSeries>,
    val categoryColors: List<Int>,
    val options: ChartOptions,
) : Widget(id) {

    /**
     * The category under the mouse at the last frame, or `null` for none.
     */
    var hovered: Int? = null
        private set

    /**
     * The values of each series, per category.
     */
    private val values: List<List<Double>> get() = series.map { it.values }

    /**
     * Returns the size a chart prefers without a width limit.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(DEFAULT_WIDTH, DEFAULT_WIDTH * 9 / 16)

    /**
     * Creates a layout box that is 16 by 9 when the chart's height fits its content.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox = LayoutBox(
        width = width,
        height = height,
        content = contentSize(measurer),
        aspectRatio = if (height.mode == SizeMode.FIT) ASPECT else 0f,
    ).also { layoutBox = it }

    /**
     * Returns the chart colour number of a category of a pie or radial chart.
     *
     * @param index the index of the category
     * @return the colour number
     */
    fun categoryColor(index: Int): Int = categoryColors.getOrNull(index) ?: (index + 1)

    /**
     * Returns the area the chart itself is drawn in: the widget without the legend below.
     *
     * @param measurer the text measurer
     * @return the area
     */
    fun chartArea(measurer: TextMeasurer): Rect {
        val legendHeight = if (options.legend) measurer.lineHeight + LEGEND_GAP * 2 else 0
        return Rect(bounds.x, bounds.y, bounds.width, (bounds.height - legendHeight).coerceAtLeast(0))
    }

    /**
     * Returns the geometry of an area, bar or line chart: its plot inside the axis labels and
     * its value range.
     *
     * @param measurer the text measurer
     * @return the geometry
     */
    fun geometry(measurer: TextMeasurer): CartesianGeometry {
        val area = chartArea(measurer)
        val (min, max) = ChartMath.range(values, options.stacked && kind != ChartKind.LINE)
        val horizontal = options.horizontal && kind == ChartKind.BAR
        val valueLabelWidth = ChartMath.ticks(min, max).maxOf { measurer.plainWidth(ChartMath.format(it)) }
        val categoryLabelWidth = categories.maxOfOrNull { measurer.width(it) } ?: 0
        val half = measurer.lineHeight / 2
        val banded = kind == ChartKind.BAR
        val edge = if (!banded && options.categoryAxis && !horizontal) {
            maxOf(measurer.width(categories.firstOrNull().orEmpty()), measurer.width(categories.lastOrNull().orEmpty())) / 2
        } else {
            0
        }
        val left: Int
        val bottom: Int
        if (horizontal) {
            left = if (options.categoryAxis) categoryLabelWidth + AXIS_GAP else 0
            bottom = if (options.valueAxis) measurer.lineHeight + AXIS_GAP else 0
        } else {
            left = maxOf(if (options.valueAxis) valueLabelWidth + AXIS_GAP else 0, edge)
            bottom = if (options.categoryAxis) measurer.lineHeight + AXIS_GAP else 0
        }
        val right = if (horizontal && options.valueAxis) valueLabelWidth / 2 else maxOf(edge, PLOT_INSET)
        val top = if (horizontal) PLOT_INSET else half
        val plot = Rect(area.x + left, area.y + top, (area.width - left - right).coerceAtLeast(0), (area.height - top - bottom).coerceAtLeast(0))
        return CartesianGeometry(plot, categories.size, horizontal, min, max, banded)
    }

    /**
     * Draws the chart for its family, its legend, and a tooltip for the hovered category.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        hovered = null
        when (kind) {
            ChartKind.AREA, ChartKind.BAR, ChartKind.LINE -> renderCartesian(ui, mouseX, mouseY)
            ChartKind.PIE, ChartKind.RADAR, ChartKind.RADIAL -> Unit
        }
        if (options.legend) renderLegend(ui)
        val category = hovered
        if (options.tooltip && category != null) context.drawOnTop { top -> renderTooltip(top, category, mouseX, mouseY) }
    }

    /**
     * Draws an area, bar or line chart: the grid, the axis labels, the hover cursor and the
     * series.
     *
     * @param ui the graphics to draw with
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    private fun renderCartesian(ui: UiGraphics, mouseX: Int, mouseY: Int) {
        val geometry = geometry(ui)
        val plot = geometry.plot
        if (plot.width <= 0 || plot.height <= 0) return
        val tokens = ui.tokens
        val ticks = ChartMath.ticks(geometry.min, geometry.max)
        ticks.forEach { tick ->
            val position = geometry.valuePosition(tick).roundToInt()
            if (options.grid) {
                if (geometry.horizontal) ui.fill(Rect(position.coerceAtMost(plot.right - 1), plot.y, 1, plot.height), tokens.border)
                else ui.fill(Rect(plot.x, position.coerceAtMost(plot.bottom - 1), plot.width, 1), tokens.border)
            }
            if (options.valueAxis) {
                val label = ChartMath.format(tick)
                val width = ui.plainWidth(label)
                if (geometry.horizontal) ui.plainText(label, position - width / 2, plot.bottom + AXIS_GAP, tokens.mutedForeground)
                else ui.plainText(label, plot.x - AXIS_GAP - width, position - ui.lineHeight / 2, tokens.mutedForeground)
            }
        }
        if (options.categoryAxis) renderCategoryLabels(ui, geometry)
        hovered = geometry.categoryAt(mouseX.toDouble(), mouseY.toDouble())
        hovered?.let { category ->
            if (!options.tooltip) return@let
            if (kind == ChartKind.BAR) {
                val start = (geometry.categoryPosition(category) - geometry.band / 2).roundToInt()
                val cursor = if (geometry.horizontal) Rect(plot.x, start, plot.width, geometry.band.roundToInt()) else Rect(start, plot.y, geometry.band.roundToInt(), plot.height)
                ui.fill(cursor, ThemeColors.withAlpha(tokens.muted, CURSOR_ALPHA))
            } else {
                ui.fill(Rect(geometry.categoryPosition(category).roundToInt(), plot.y, 1, plot.height), tokens.border)
            }
        }
        when (kind) {
            ChartKind.BAR -> renderBars(ui, geometry)
            ChartKind.AREA -> renderLines(ui, geometry, fill = true)
            else -> renderLines(ui, geometry, fill = false)
        }
    }

    /**
     * Draws the category labels along the category axis, skipping labels that would overlap.
     *
     * @param ui the graphics to draw with
     * @param geometry the chart geometry
     */
    private fun renderCategoryLabels(ui: UiGraphics, geometry: CartesianGeometry) {
        val plot = geometry.plot
        val spacing = if (geometry.horizontal) ui.lineHeight else (categories.maxOfOrNull { ui.width(it) } ?: 0) + LABEL_SPACING
        val along = if (geometry.banded) geometry.band else if (categories.size > 1) (if (geometry.horizontal) plot.height else plot.width).toFloat() / (categories.size - 1) else Float.MAX_VALUE
        val step = ChartMath.labelStep(along, spacing)
        categories.forEachIndexed { index, label ->
            if (index % step != 0) return@forEachIndexed
            val position = geometry.categoryPosition(index).roundToInt()
            val width = ui.width(label)
            if (geometry.horizontal) ui.text(label, plot.x - AXIS_GAP - width, position - ui.lineHeight / 2, ui.tokens.mutedForeground)
            else ui.text(label, position - width / 2, plot.bottom + AXIS_GAP, ui.tokens.mutedForeground)
        }
    }

    /**
     * Draws the bars with the outer end of each stack rounded, and their values when asked.
     *
     * @param ui the graphics to draw with
     * @param geometry the chart geometry
     */
    private fun renderBars(ui: UiGraphics, geometry: CartesianGeometry) {
        ChartMath.bars(geometry, values, options.stacked).forEach { bar ->
            val rect = pixels(bar.rect)
            if (rect.width <= 0 || rect.height <= 0) return@forEach
            val corners = when {
                !bar.top -> Corners.NONE
                geometry.horizontal -> Corners(false, true, false, true)
                else -> Corners.TOP
            }
            ui.fillRounded(rect, ui.tokens.chart(series[bar.series].color), BAR_RADIUS, corners)
            if (options.labels && bar.top) {
                val value = if (options.stacked) values.sumOf { it.getOrElse(bar.category) { 0.0 } } else values[bar.series].getOrElse(bar.category) { 0.0 }
                val text = ChartMath.format(value)
                val width = ui.plainWidth(text)
                if (geometry.horizontal) ui.plainText(text, rect.right + LABEL_GAP, rect.y + (rect.height - ui.lineHeight) / 2 + 1, ui.tokens.foreground)
                else ui.plainText(text, rect.x + (rect.width - width) / 2, rect.y - ui.lineHeight - 1, ui.tokens.foreground)
            }
        }
    }

    /**
     * Draws the series as lines, and for area charts the area below each line, at screen-pixel
     * resolution, then the dots.
     *
     * @param ui the graphics to draw with
     * @param geometry the chart geometry
     * @param fill whether the areas are filled
     */
    private fun renderLines(ui: UiGraphics, geometry: CartesianGeometry, fill: Boolean) {
        val stacked = options.stacked && fill
        val tops = if (stacked) ChartMath.stacked(values) else values
        val lines = tops.map { ChartMath.points(geometry, it) }
        val baseline = geometry.valuePosition(0.0.coerceIn(geometry.min, geometry.max))
        val plot = geometry.plot
        ui.fine { scale ->
            val start = plot.x * scale
            val end = plot.right * scale
            lines.indices.forEach { index ->
                val color = ui.tokens.chart(series[index].color)
                val line = lines[index]
                if (line.isEmpty()) return@forEach
                val from = maxOf(start, (line.first().x * scale).roundToInt())
                val to = minOf(end, (line.last().x * scale).roundToInt())
                if (fill) {
                    val below = if (stacked && index > 0) lines[index - 1] else null
                    val shade = ThemeColors.withAlpha(color, AREA_ALPHA)
                    for (px in from until to) {
                        val x = (px + 0.5f) / scale
                        val top = ChartMath.lineAt(line, options.curve, x)
                        val bottom = below?.let { ChartMath.lineAt(it, options.curve, x) } ?: baseline
                        ui.fineFill(px, (top * scale).roundToInt(), px + 1, (bottom * scale).roundToInt(), shade)
                    }
                }
                var previous = ChartMath.lineAt(line, options.curve, (from + 0.5f) / scale) * scale
                for (px in from until to) {
                    val y = ChartMath.lineAt(line, options.curve, (px + 0.5f) / scale) * scale
                    val low = floor(minOf(previous, y) - scale / 2f).toInt()
                    val high = floor(maxOf(previous, y) + scale / 2f).toInt().coerceAtLeast(low + 1)
                    ui.fineFill(px, low, px + 1, high, color)
                    previous = y
                }
            }
        }
        if (options.dots && !fill) {
            lines.forEachIndexed { index, line ->
                line.forEach { point ->
                    val dot = Rect((point.x - DOT / 2f).roundToInt(), (point.y - DOT / 2f).roundToInt(), DOT, DOT)
                    ui.fillRounded(dot, ui.tokens.chart(series[index].color), DOT / 2)
                }
            }
        }
    }

    /**
     * Draws the legend: a colour square and the label of each series, or of each category for pie
     * and radial charts, centred below the chart.
     *
     * @param ui the graphics to draw with
     */
    private fun renderLegend(ui: UiGraphics) {
        val entries = legendEntries()
        val widths = entries.map { SWATCH + SWATCH_GAP + ui.width(it.first) }
        val total = widths.sum() + LEGEND_SPACING * (entries.size - 1).coerceAtLeast(0)
        var x = bounds.x + (bounds.width - total) / 2
        val y = bounds.bottom - LEGEND_GAP - ui.lineHeight
        entries.forEachIndexed { index, (label, color) ->
            ui.fillRounded(Rect(x, y + (ui.lineHeight - SWATCH) / 2, SWATCH, SWATCH), ui.tokens.chart(color), 1)
            ui.text(label, x + SWATCH + SWATCH_GAP, y, ui.tokens.foreground)
            x += widths[index] + LEGEND_SPACING
        }
    }

    /**
     * Returns the labels and colour numbers the legend lists.
     *
     * @return the entries
     */
    fun legendEntries(): List<Pair<String, Int>> = when (kind) {
        ChartKind.PIE, ChartKind.RADIAL -> categories.mapIndexed { index, label -> label to categoryColor(index) }
        else -> series.map { it.label to it.color }
    }

    /**
     * Returns the rows of the tooltip of a category: a label, a colour number and a value each.
     *
     * @param category the index of the category
     * @return the rows
     */
    fun tooltipRows(category: Int): List<Triple<String, Int, Double>> = when (kind) {
        ChartKind.PIE, ChartKind.RADIAL -> series.map { Triple(it.label, categoryColor(category), it.values.getOrElse(category) { 0.0 }) }
        else -> series.map { Triple(it.label, it.color, it.values.getOrElse(category) { 0.0 }) }
    }

    /**
     * Draws the tooltip of a category beside the mouse, inside the chart: the category label,
     * then each series with its indicator, label and value.
     *
     * @param ui the graphics to draw with
     * @param category the index of the category
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    private fun renderTooltip(ui: UiGraphics, category: Int, mouseX: Int, mouseY: Int) {
        val tokens = ui.tokens
        val title = TextStyle.styled(categories.getOrElse(category) { "\"\"" }, bold = true, italic = false)
        val rows = tooltipRows(category)
        val rowHeight = ui.lineHeight + ROW_GAP
        val labelWidth = rows.maxOfOrNull { ui.width(it.first) } ?: 0
        val valueWidth = rows.maxOfOrNull { ui.plainWidth(ChartMath.format(it.third)) } ?: 0
        val width = maxOf(ui.width(title), INDICATOR_SPACE + labelWidth + VALUE_GAP + valueWidth) + 2 * TOOLTIP_PADDING
        val height = rowHeight * (rows.size + 1) + 2 * TOOLTIP_PADDING - ROW_GAP
        val x = if (mouseX + TOOLTIP_OFFSET + width <= bounds.right) mouseX + TOOLTIP_OFFSET else (mouseX - TOOLTIP_OFFSET - width).coerceAtLeast(bounds.x)
        val y = (mouseY - height / 2).coerceIn(bounds.y, (bounds.bottom - height).coerceAtLeast(bounds.y))
        val box = Rect(x, y, width, height)
        OverlaySurface.draw(ui, box)
        ui.fillRounded(box, tokens.background)
        ui.borderRounded(box, tokens.border)
        ui.text(title, x + TOOLTIP_PADDING, y + TOOLTIP_PADDING, tokens.foreground)
        rows.forEachIndexed { index, (label, color, value) ->
            val rowY = y + TOOLTIP_PADDING + rowHeight * (index + 1)
            val colour = tokens.chart(color)
            val left = x + TOOLTIP_PADDING
            when (options.indicator) {
                ChartIndicator.DOT -> ui.fillRounded(Rect(left, rowY + (ui.lineHeight - INDICATOR) / 2, INDICATOR, INDICATOR), colour, 1)
                ChartIndicator.LINE -> ui.fill(Rect(left + 1, rowY - 1, 2, ui.lineHeight + 1), colour)
                ChartIndicator.DASHED -> for (dash in 0 until ui.lineHeight + 1 step 3) ui.fill(Rect(left + 1, rowY - 1 + dash, 2, 2), colour)
            }
            ui.text(label, left + INDICATOR_SPACE, rowY, tokens.mutedForeground)
            val text = ChartMath.format(value)
            ui.plainText(text, box.right - TOOLTIP_PADDING - ui.plainWidth(text), rowY, tokens.foreground)
        }
    }

    /**
     * Rounds a fractional rectangle to whole GUI pixels.
     *
     * @param rect the rectangle
     * @return the rounded rectangle
     */
    private fun pixels(rect: RectF): Rect {
        val x = rect.x.roundToInt()
        val y = rect.y.roundToInt()
        return Rect(x, y, rect.right.roundToInt() - x, rect.bottom.roundToInt() - y)
    }

    /**
     * Holds the chart metrics.
     */
    companion object {
        /**
         * The width of a chart without a width limit.
         */
        const val DEFAULT_WIDTH: Int = 240

        /**
         * The width of a fitting chart divided by its height.
         */
        const val ASPECT: Float = 16f / 9f

        /**
         * The space between the plot and its axis labels.
         */
        const val AXIS_GAP: Int = 4

        /**
         * The smallest space around the plot on the sides without labels.
         */
        const val PLOT_INSET: Int = 2

        /**
         * The smallest space between two category labels.
         */
        const val LABEL_SPACING: Int = 6

        /**
         * The space between a bar and its value.
         */
        const val LABEL_GAP: Int = 2

        /**
         * The corner radius of bars.
         */
        const val BAR_RADIUS: Int = 2

        /**
         * The size of a line chart's dots.
         */
        const val DOT: Int = 4

        /**
         * The opacity of the shading of an area chart.
         */
        const val AREA_ALPHA: Float = 0.4f

        /**
         * The opacity of the band behind a hovered bar category.
         */
        const val CURSOR_ALPHA: Float = 0.6f

        /**
         * The space above and below the legend.
         */
        const val LEGEND_GAP: Int = 4

        /**
         * The space between two legend entries.
         */
        const val LEGEND_SPACING: Int = 10

        /**
         * The size of a legend colour square.
         */
        const val SWATCH: Int = 6

        /**
         * The space between a legend colour square and its label.
         */
        const val SWATCH_GAP: Int = 3

        /**
         * The space inside the tooltip.
         */
        const val TOOLTIP_PADDING: Int = 5

        /**
         * The distance between the mouse and the tooltip.
         */
        const val TOOLTIP_OFFSET: Int = 10

        /**
         * The space between two tooltip rows.
         */
        const val ROW_GAP: Int = 2

        /**
         * The size of a dot indicator.
         */
        const val INDICATOR: Int = 5

        /**
         * The space a row's indicator takes before its label.
         */
        const val INDICATOR_SPACE: Int = 9

        /**
         * The smallest space between a row's label and its value.
         */
        const val VALUE_GAP: Int = 12
    }
}
