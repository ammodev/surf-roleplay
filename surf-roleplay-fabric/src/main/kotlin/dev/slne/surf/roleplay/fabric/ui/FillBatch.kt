package dev.slne.surf.roleplay.fabric.ui

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
import org.joml.Matrix3x2f

/**
 * Many filled rectangles submitted to the GUI renderer as one element, which builds all of their
 * quads at once and is drawn exactly like the same rectangles filled one by one.
 *
 * @param data the rectangles, five numbers each: left, top, right, bottom and the ARGB colour
 * @param count the number of rectangles in [data]
 * @param dx the horizontal offset added to every rectangle
 * @param dy the vertical offset added to every rectangle
 * @param pose the transformation of the rectangles, copied when the batch is submitted
 * @param scissor the area the rectangles are clipped to, or `null` for none
 */
class FillBatchRenderState(
    private val data: IntArray,
    private val count: Int,
    private val dx: Int,
    private val dy: Int,
    private val pose: Matrix3x2f,
    private val scissor: ScreenRectangle?,
) : GuiElementRenderState {

    /**
     * The area the rectangles cover on screen within the scissor, or `null` if none of it shows.
     */
    private val area: ScreenRectangle? = run {
        var left = Int.MAX_VALUE
        var top = Int.MAX_VALUE
        var right = Int.MIN_VALUE
        var bottom = Int.MIN_VALUE
        for (index in 0 until count) {
            val at = index * FIELDS
            left = minOf(left, data[at])
            top = minOf(top, data[at + 1])
            right = maxOf(right, data[at + 2])
            bottom = maxOf(bottom, data[at + 3])
        }
        if (count == 0) {
            null
        } else {
            val covered = ScreenRectangle(left + dx, top + dy, right - left, bottom - top).transformMaxBounds(pose)
            if (scissor == null) covered else scissor.intersection(covered)
        }
    }

    /**
     * Adds four corners per rectangle, in the order a single filled rectangle uses.
     *
     * @param consumer the consumer of the vertices
     */
    override fun buildVertices(consumer: VertexConsumer) {
        for (index in 0 until count) {
            val at = index * FIELDS
            val x0 = (data[at] + dx).toFloat()
            val y0 = (data[at + 1] + dy).toFloat()
            val x1 = (data[at + 2] + dx).toFloat()
            val y1 = (data[at + 3] + dy).toFloat()
            val color = data[at + 4]
            consumer.addVertexWith2DPose(pose, x0, y0).setColor(color)
            consumer.addVertexWith2DPose(pose, x0, y1).setColor(color)
            consumer.addVertexWith2DPose(pose, x1, y1).setColor(color)
            consumer.addVertexWith2DPose(pose, x1, y0).setColor(color)
        }
    }

    /**
     * Returns the pipeline of plain coloured GUI shapes.
     *
     * @return the pipeline
     */
    override fun pipeline(): RenderPipeline = RenderPipelines.GUI

    /**
     * Returns that the rectangles use no texture.
     *
     * @return the empty texture setup
     */
    override fun textureSetup(): TextureSetup = TextureSetup.noTexture()

    /**
     * Returns the area the rectangles are clipped to.
     *
     * @return the scissor, or `null` for none
     */
    override fun scissorArea(): ScreenRectangle? = scissor

    /**
     * Returns the area the rectangles cover on screen within the scissor.
     *
     * @return the area, or `null` if none of it shows
     */
    override fun bounds(): ScreenRectangle? = area

    /**
     * Holds the storage layout of the rectangles.
     */
    companion object {
        /**
         * The numbers stored per rectangle.
         */
        const val FIELDS: Int = 5
    }
}
