package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.ScreenPanel
import dev.slne.surf.roleplay.fabric.ui.ScreenPanelListener
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.protocol.screen.ChartCurve
import dev.slne.surf.roleplay.protocol.screen.ChartKind
import dev.slne.surf.roleplay.protocol.screen.ChartNode
import dev.slne.surf.roleplay.protocol.screen.ChartSeries
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for charts in the mod.
 */
class ChartWidgetsTest {

    /**
     * A measurer in which every character is 5 pixels wide.
     */
    private val measurer = object : TextMeasurer {
        override val lineHeight: Int = 9
        override fun width(json: String): Int = json.length * 5
        override fun plainWidth(text: String): Int = text.length * 5
    }

    /**
     * A listener that ignores everything.
     */
    private val listener = object : ScreenPanelListener {
        override fun actionTriggered(panel: ScreenPanel, widget: Widget) = Unit
        override fun closeRequested(panel: ScreenPanel) = Unit
    }

    /**
     * Returns whether two numbers are within a small distance.
     *
     * @param expected the expected number
     * @param actual the actual number
     */
    private fun assertNear(expected: Float, actual: Float) = assertTrue(abs(expected - actual) < 0.01f, "expected $expected but was $actual")

    /**
     * Verifies that value axes end at round numbers and include negative values.
     */
    @Test
    fun `value ranges end at round numbers`() {
        assertEquals(200.0, ChartMath.niceMax(186.0))
        assertEquals(500.0, ChartMath.niceMax(305.0))
        assertEquals(25.0, ChartMath.niceMax(25.0))
        assertEquals(0.5, ChartMath.niceMax(0.3))
        assertEquals(1.0, ChartMath.niceMax(0.0))
        assertEquals(0.0 to 10.0, ChartMath.range(listOf(listOf(1.0, 2.0), listOf(3.0, 4.0)), stacked = true))
        assertEquals(0.0 to 5.0, ChartMath.range(listOf(listOf(1.0, 2.0), listOf(3.0, 4.0)), stacked = false))
        assertEquals(-5.0 to 5.0, ChartMath.range(listOf(listOf(-3.0, 5.0)), stacked = false))
        assertEquals(listOf(0.0, 2.5, 5.0, 7.5, 10.0), ChartMath.ticks(0.0, 10.0))
    }

    /**
     * Verifies that bars of two series sit side by side within their category's band, scaled to
     * the value range.
     */
    @Test
    fun `bars sit side by side`() {
        val geometry = CartesianGeometry(Rect(0, 0, 100, 50), 2, horizontal = false, min = 0.0, max = 10.0, banded = true)
        val bars = ChartMath.bars(geometry, listOf(listOf(10.0, 5.0), listOf(2.0, 0.0)), stacked = false)

        val first = bars.first { it.series == 0 && it.category == 0 }.rect
        assertNear(5f, first.x)
        assertNear(19.5f, first.width)
        assertNear(0f, first.y)
        assertNear(50f, first.height)
        val second = bars.first { it.series == 1 && it.category == 0 }.rect
        assertNear(first.right + ChartMath.BAR_GAP, second.x)
        assertNear(10f, second.height)
        assertNear(25f, bars.first { it.series == 0 && it.category == 1 }.rect.height)
    }

    /**
     * Verifies that stacked bars start where the series below ends, and that only the outermost
     * bar of each stack is rounded.
     */
    @Test
    fun `stacked bars pile up`() {
        val geometry = CartesianGeometry(Rect(0, 0, 100, 100), 1, horizontal = false, min = 0.0, max = 10.0, banded = true)
        val bars = ChartMath.bars(geometry, listOf(listOf(4.0), listOf(6.0)), stacked = true)

        assertNear(60f, bars[0].rect.y)
        assertNear(40f, bars[0].rect.height)
        assertNear(0f, bars[1].rect.y)
        assertNear(bars[0].rect.y, bars[1].rect.bottom)
        assertEquals(listOf(false, true), bars.map { it.top })
    }

    /**
     * Verifies that horizontal bars run from the left edge and stack their categories downwards.
     */
    @Test
    fun `horizontal bars run from the left`() {
        val geometry = CartesianGeometry(Rect(10, 0, 100, 40), 2, horizontal = true, min = 0.0, max = 10.0, banded = true)
        val bars = ChartMath.bars(geometry, listOf(listOf(5.0, 10.0)), stacked = false)

        assertNear(10f, bars[0].rect.x)
        assertNear(50f, bars[0].rect.width)
        assertTrue(bars[1].rect.y > bars[0].rect.bottom)
        assertNear(100f, bars[1].rect.width)
    }

    /**
     * Verifies that lines run straight, in steps or in a smooth curve through their points, and
     * that the smooth curve does not overshoot between equal points.
     */
    @Test
    fun `lines follow their curve`() {
        val points = listOf(PointF(0f, 10f), PointF(10f, 0f), PointF(20f, 0f), PointF(30f, 20f))

        assertNear(5f, ChartMath.lineAt(points, ChartCurve.LINEAR, 5f))
        assertNear(10f, ChartMath.lineAt(points, ChartCurve.STEP, 4f))
        assertNear(0f, ChartMath.lineAt(points, ChartCurve.STEP, 6f))
        points.forEach { assertNear(it.y, ChartMath.lineAt(points, ChartCurve.NATURAL, it.x)) }
        assertNear(0f, ChartMath.lineAt(points, ChartCurve.NATURAL, 15f))
        (1 until 10).forEach { x -> assertTrue(ChartMath.lineAt(points, ChartCurve.NATURAL, x.toFloat()) in 0f..10f) }
    }

    /**
     * Verifies that the category under a point is its band for bars and the nearest point for
     * lines, and that points outside the plot have none.
     */
    @Test
    fun `hovering finds the category`() {
        val bands = CartesianGeometry(Rect(0, 0, 90, 30), 3, horizontal = false, min = 0.0, max = 1.0, banded = true)
        val edges = CartesianGeometry(Rect(0, 0, 90, 30), 3, horizontal = false, min = 0.0, max = 1.0, banded = false)

        assertEquals(1, bands.categoryAt(40.0, 10.0))
        assertEquals(2, bands.categoryAt(89.0, 10.0))
        assertEquals(0, edges.categoryAt(20.0, 10.0))
        assertEquals(1, edges.categoryAt(30.0, 10.0))
        assertEquals(null, bands.categoryAt(40.0, 31.0))
        assertNear(0f, edges.categoryPosition(0))
        assertNear(90f, edges.categoryPosition(2))
    }

    /**
     * Verifies German number formatting.
     */
    @Test
    fun `values are formatted in German`() {
        assertEquals("1.234,5", ChartMath.format(1234.5))
        assertEquals("12", ChartMath.format(12.0))
        assertEquals("0,33", ChartMath.format(1.0 / 3))
        assertEquals("0", ChartMath.format(-0.0000000001))
    }

    /**
     * Verifies that a fitting chart is 16 by 9, that its legend is taken off the chart area, and
     * that its plot sits above the category labels.
     */
    @Test
    fun `charts keep their aspect and leave room for the legend`() {
        val node = ChartNode(
            "chart",
            width = Sizing.fixed(320),
            kind = ChartKind.BAR,
            categories = listOf("\"Jan\"", "\"Feb\""),
            series = listOf(ChartSeries("calls", "\"Einsätze\"", 1, listOf(3.0, 4.0))),
            legend = true,
        )
        val panel = ScreenPanel("T", WidgetFactory.create(ColumnNode("root", children = listOf(node))), true, listener, PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)))
            .also { it.layoutIfNeeded(measurer, 500, 400) }
        val chart = assertIs<ChartWidget>(WidgetTree.find(panel.root, "chart"))

        assertEquals(180, chart.bounds.height)
        val area = chart.chartArea(measurer)
        assertEquals(180 - measurer.lineHeight - 2 * ChartWidget.LEGEND_GAP, area.height)
        val plot = chart.geometry(measurer).plot
        assertEquals(area.bottom - measurer.lineHeight - ChartWidget.AXIS_GAP, plot.bottom)
        assertEquals(listOf("\"Einsätze\"" to 1), chart.legendEntries())
        assertEquals(listOf(Triple("\"Einsätze\"", 1, 4.0)), chart.tooltipRows(1))
    }
}
