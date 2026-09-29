package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.protocol.screen.ChartCurve
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

/**
 * A rectangle in fractional GUI pixels.
 *
 * @property x the left edge
 * @property y the top edge
 * @property width the width
 * @property height the height
 */
data class RectF(val x: Float, val y: Float, val width: Float, val height: Float) {
    /**
     * The right edge.
     */
    val right: Float get() = x + width

    /**
     * The bottom edge.
     */
    val bottom: Float get() = y + height
}

/**
 * A point in fractional GUI pixels.
 *
 * @property x the horizontal position
 * @property y the vertical position
 */
data class PointF(val x: Float, val y: Float)

/**
 * A bar of a bar chart.
 *
 * @property series the index of its series
 * @property category the index of its category
 * @property rect its area
 * @property top whether it is the outermost bar of its stack, whose end is rounded
 */
data class ChartBar(val series: Int, val category: Int, val rect: RectF, val top: Boolean)

/**
 * The value range and the positions of a chart with a category axis and a value axis.
 *
 * @property plot the area the values are drawn in
 * @property count the number of categories
 * @property horizontal whether the categories run down the side and values from the left
 * @property min the value at the start of the value axis
 * @property max the value at the end of the value axis
 * @property banded whether each category takes a band, as for bars, rather than a point on an
 *           edge-to-edge axis, as for lines and areas
 */
class CartesianGeometry(
    val plot: Rect,
    val count: Int,
    val horizontal: Boolean,
    val min: Double,
    val max: Double,
    val banded: Boolean,
) {
    /**
     * The length of the category axis.
     */
    private val categoryLength: Float get() = if (horizontal) plot.height.toFloat() else plot.width.toFloat()

    /**
     * The length of one category's band.
     */
    val band: Float get() = if (count <= 0) 0f else categoryLength / count

    /**
     * Returns the position of a category along the category axis: the centre of its band, or
     * its point on an axis that runs from edge to edge.
     *
     * @param index the index of the category
     * @return the position in GUI pixels
     */
    fun categoryPosition(index: Int): Float {
        val start = if (horizontal) plot.y.toFloat() else plot.x.toFloat()
        return when {
            banded -> start + band * index + band / 2
            count <= 1 -> start + categoryLength / 2
            else -> start + categoryLength * index / (count - 1)
        }
    }

    /**
     * Returns the position of a value along the value axis.
     *
     * @param value the value
     * @return the position in GUI pixels, from the bottom up or from the left
     */
    fun valuePosition(value: Double): Float {
        val span = (max - min).takeIf { it > 0 } ?: 1.0
        val fraction = ((value - min) / span).toFloat()
        return if (horizontal) plot.x + fraction * plot.width else plot.bottom - fraction * plot.height
    }

    /**
     * Returns the category under a point of the plot: the band that contains it, or the nearest
     * point.
     *
     * @param x the horizontal position
     * @param y the vertical position
     * @return the index of the category, or `null` if the point is outside the plot
     */
    fun categoryAt(x: Double, y: Double): Int? {
        if (count <= 0 || !plot.contains(x, y)) return null
        val position = if (horizontal) y else x
        if (banded) {
            val start = if (horizontal) plot.y else plot.x
            return floor((position - start) / band).toInt().coerceIn(0, count - 1)
        }
        return (0 until count).minByOrNull { abs(categoryPosition(it) - position) }
    }
}

/**
 * The value computations and drawing geometry of charts.
 */
object ChartMath {

    /**
     * The share of a band left empty around the bars of a category.
     */
    const val BAND_GAP: Float = 0.2f

    /**
     * The space between two bars of a category that are not stacked.
     */
    const val BAR_GAP: Float = 1f

    /**
     * Returns the smallest round number at or above a value: 1, 2, 2.5 or 5 times a power of ten.
     *
     * @param value the value
     * @return the round number, or 1 for values of 0 or less
     */
    fun niceMax(value: Double): Double {
        if (value <= 0) return 1.0
        val magnitude = 10.0.pow(floor(log10(value)))
        val step = listOf(1.0, 2.0, 2.5, 5.0, 10.0).first { it * magnitude >= value - 1e-9 }
        return step * magnitude
    }

    /**
     * Returns evenly spaced values from a minimum to a maximum, both included.
     *
     * @param min the smallest value
     * @param max the largest value
     * @param steps the number of steps between them
     * @return the values
     */
    fun ticks(min: Double, max: Double, steps: Int = 4): List<Double> = (0..steps).map { min + (max - min) * it / steps }

    /**
     * Returns the running sums of series per category, for stacking: the value where each series
     * ends, counting every series below it.
     *
     * @param values the values of each series, per category
     * @return the running sums, in the same shape
     */
    fun stacked(values: List<List<Double>>): List<List<Double>> {
        val count = values.maxOfOrNull { it.size } ?: 0
        val sums = DoubleArray(count)
        return values.map { series ->
            List(count) { index ->
                sums[index] += series.getOrElse(index) { 0.0 }
                sums[index]
            }
        }
    }

    /**
     * Returns the value range of a chart: from zero or the lowest value up to a round number at
     * or above the highest value, counting stacks as their sums.
     *
     * @param values the values of each series, per category
     * @param stacked whether the series are stacked
     * @return the smallest and the largest value of the axis
     */
    fun range(values: List<List<Double>>, stacked: Boolean): Pair<Double, Double> {
        val shown = if (stacked) stacked(values) else values
        val all = shown.flatten()
        val low = minOf(0.0, all.minOrNull() ?: 0.0)
        val high = all.maxOrNull() ?: 0.0
        return (if (low < 0) -niceMax(-low) else 0.0) to niceMax(high)
    }

    /**
     * Returns the bars of a bar chart: side by side within each category's band, or stacked.
     *
     * @param geometry the chart geometry, banded
     * @param values the values of each series, per category
     * @param stacked whether the series are stacked
     * @return the bars, series by series
     */
    fun bars(geometry: CartesianGeometry, values: List<List<Double>>, stacked: Boolean): List<ChartBar> {
        val bars = mutableListOf<ChartBar>()
        val group = geometry.band * (1 - BAND_GAP)
        val seriesCount = values.size.coerceAtLeast(1)
        val width = if (stacked) group else ((group - BAR_GAP * (seriesCount - 1)) / seriesCount).coerceAtLeast(1f)
        val sums = if (stacked) stacked(values) else values
        for (category in 0 until geometry.count) {
            val bandStart = geometry.categoryPosition(category) - group / 2
            val lastShown = values.indices.lastOrNull { values[it].getOrElse(category) { 0.0 } != 0.0 } ?: -1
            values.indices.forEach { series ->
                val value = values[series].getOrElse(category) { 0.0 }
                val end = sums[series].getOrElse(category) { 0.0 }
                val start = if (stacked) end - value else 0.0
                val offset = if (stacked) 0f else series * (width + BAR_GAP)
                val a = geometry.valuePosition(start)
                val b = geometry.valuePosition(end)
                val rect = if (geometry.horizontal) {
                    RectF(minOf(a, b), bandStart + offset, abs(b - a), width)
                } else {
                    RectF(bandStart + offset, minOf(a, b), width, abs(b - a))
                }
                bars += ChartBar(series, category, rect, !stacked || series == lastShown)
            }
        }
        return bars
    }

    /**
     * Returns the points of a series on a line or area chart.
     *
     * @param geometry the chart geometry
     * @param values the values of the series, one per category
     * @return the points, one per category
     */
    fun points(geometry: CartesianGeometry, values: List<Double>): List<PointF> =
        (0 until geometry.count).map { index -> PointF(geometry.categoryPosition(index), geometry.valuePosition(values.getOrElse(index) { 0.0 })) }

    /**
     * Returns the height of a line through points at a horizontal position. Before the first and
     * after the last point it keeps their height.
     *
     * @param points the points, left to right
     * @param curve how the line runs between the points
     * @param x the horizontal position
     * @return the vertical position of the line
     */
    fun lineAt(points: List<PointF>, curve: ChartCurve, x: Float): Float {
        if (points.isEmpty()) return 0f
        if (x <= points.first().x) return points.first().y
        if (x >= points.last().x) return points.last().y
        val index = points.indexOfLast { it.x <= x }.coerceIn(0, points.size - 2)
        val a = points[index]
        val b = points[index + 1]
        val t = if (b.x == a.x) 0f else (x - a.x) / (b.x - a.x)
        return when (curve) {
            ChartCurve.LINEAR -> a.y + (b.y - a.y) * t
            ChartCurve.STEP -> if (t < 0.5f) a.y else b.y
            ChartCurve.NATURAL -> {
                val slopes = monotoneSlopes(points)
                val h = b.x - a.x
                val t2 = t * t
                val t3 = t2 * t
                (2 * t3 - 3 * t2 + 1) * a.y + (t3 - 2 * t2 + t) * h * slopes[index] + (-2 * t3 + 3 * t2) * b.y + (t3 - t2) * h * slopes[index + 1]
            }
        }
    }

    /**
     * Returns the tangents of a monotone cubic curve through points, so that the curve never
     * overshoots a point.
     *
     * @param points the points, left to right
     * @return the tangent at each point
     */
    private fun monotoneSlopes(points: List<PointF>): FloatArray {
        val n = points.size
        val secants = FloatArray(n - 1) { i ->
            val dx = points[i + 1].x - points[i].x
            if (dx == 0f) 0f else (points[i + 1].y - points[i].y) / dx
        }
        val slopes = FloatArray(n)
        slopes[0] = secants[0]
        slopes[n - 1] = secants[n - 2]
        for (i in 1 until n - 1) slopes[i] = if (secants[i - 1] * secants[i] <= 0f) 0f else (secants[i - 1] + secants[i]) / 2
        for (i in 0 until n - 1) {
            if (secants[i] == 0f) {
                slopes[i] = 0f
                slopes[i + 1] = 0f
                continue
            }
            val a = slopes[i] / secants[i]
            val b = slopes[i + 1] / secants[i]
            val sum = a * a + b * b
            if (sum > 9f) {
                val tau = 3f / kotlin.math.sqrt(sum)
                slopes[i] = tau * a * secants[i]
                slopes[i + 1] = tau * b * secants[i]
            }
        }
        return slopes
    }

    /**
     * Formats a value for tooltips, labels and axes in German: grouped thousands and at most
     * two decimals.
     *
     * @param value the value
     * @return the text
     */
    fun format(value: Double): String {
        val format = DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.GERMANY))
        return format.format(if (abs(value) < 1e-9) 0.0 else value)
    }

    /**
     * Returns how many categories apart axis labels must be so that labels of a width fit their
     * bands.
     *
     * @param band the length of one category along the axis
     * @param labelWidth the width of the widest label with its gap
     * @return the step between labelled categories, at least 1
     */
    fun labelStep(band: Float, labelWidth: Int): Int = if (band <= 0f) 1 else ceil(labelWidth / band).toInt().coerceAtLeast(1)
}
