package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.layout.FlexLayout
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.protocol.screen.AspectRatioNode
import dev.slne.surf.roleplay.protocol.screen.ImageNode
import dev.slne.surf.roleplay.protocol.screen.ProgressNode
import dev.slne.surf.roleplay.protocol.screen.SkeletonNode
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.SpinnerNode
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for skeletons, spinners, progress bars and aspect ratio boxes in the mod.
 */
class LoadingWidgetsTest {

    /**
     * A measurer in which every character is 5 pixels wide.
     */
    private val measurer = object : TextMeasurer {
        override val lineHeight: Int = 9
        override fun width(json: String): Int = json.length * 5
        override fun plainWidth(text: String): Int = text.length * 5
    }

    /**
     * Verifies that a skeleton pulses from full to half opacity and back every two seconds.
     */
    @Test
    fun `skeleton pulses`() {
        assertEquals(1f, SkeletonWidget.opacity(0), 0.001f)
        assertEquals(0.5f, SkeletonWidget.opacity(1000), 0.001f)
        assertEquals(1f, SkeletonWidget.opacity(2000), 0.001f)
    }

    /**
     * Verifies that a skeleton takes the size its node gives it.
     */
    @Test
    fun `skeleton takes its node size`() {
        val skeleton = WidgetFactory.create(SkeletonNode("s", width = Sizing.fixed(40), height = Sizing.fixed(8), round = true))

        assertEquals(Size(40, 8), FlexLayout.measure(skeleton.createLayout(measurer)))
    }

    /**
     * Verifies that a spinner turns once a second and is as large as its size.
     */
    @Test
    fun `spinner turns once a second`() {
        assertEquals(0f, SpinnerWidget.angle(0), 0.001f)
        assertEquals(90f, SpinnerWidget.angle(250), 0.001f)
        assertEquals(0f, SpinnerWidget.angle(1000), 0.001f)
        assertEquals(Size(14, 14), FlexLayout.measure(WidgetFactory.create(SpinnerNode("spin", size = 14)).createLayout(measurer)))
    }

    /**
     * Verifies that a progress bar without a label is a thin bar.
     */
    @Test
    fun `progress without label is thin`() {
        val bar = WidgetFactory.create(ProgressNode("p", progress = 0.3f))

        assertEquals(ProgressWidget.BAR_HEIGHT, FlexLayout.measure(bar.createLayout(measurer)).height)
    }

    /**
     * Verifies that an aspect ratio box is as wide as it may be, as tall as its ratio says, and
     * that its child fills it.
     */
    @Test
    fun `aspect ratio follows its width`() {
        val box = WidgetFactory.create(AspectRatioNode("ratio", ratio = 2f, children = listOf(ImageNode("image", texture = "minecraft:textures/block/stone.png"))))
        val layout = box.createLayout(measurer)

        assertEquals(Size(100, 50), FlexLayout.measure(layout, 100))
        assertEquals(Size(160, 80), FlexLayout.measure(layout, 160))
        FlexLayout.layout(layout, Rect(0, 0, 100, 50))
        box.applyLayout()

        assertEquals(Rect(0, 0, 100, 50), WidgetTree.find(box, "image")!!.bounds)
    }
}
