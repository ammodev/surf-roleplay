package dev.slne.surf.roleplay.fabric.ui

import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.navigation.ScreenRectangle
import org.joml.Matrix3x2f
import org.joml.Matrix3x2fc
import org.joml.Vector2f
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for drawing many filled rectangles as one GUI element.
 */
class FillBatchTest {

    /**
     * Returns a vertex consumer that records every vertex as its position after the pose and the
     * colour set on it.
     *
     * @param vertices the list the vertices are recorded in
     * @return the consumer
     */
    private fun recorder(vertices: MutableList<Triple<Float, Float, Int>>): VertexConsumer {
        var pending: Pair<Float, Float>? = null
        lateinit var proxy: VertexConsumer
        proxy = Proxy.newProxyInstance(VertexConsumer::class.java.classLoader, arrayOf(VertexConsumer::class.java)) { _, method, args ->
            when (method.name) {
                "addVertexWith2DPose" -> {
                    val point = (args[0] as Matrix3x2fc).transformPosition(Vector2f(args[1] as Float, args[2] as Float))
                    pending = point.x to point.y
                }
                "setColor" -> if (args.size == 1) {
                    val (x, y) = pending!!
                    vertices += Triple(x, y, args[0] as Int)
                }
            }
            proxy
        } as VertexConsumer
        return proxy
    }

    /**
     * Verifies that every rectangle becomes one quad, moved by the offset and placed by the pose,
     * in the corner order of a single filled rectangle.
     */
    @Test
    fun `every rectangle becomes one quad`() {
        val data = intArrayOf(0, 0, 2, 3, 7, 10, 10, 11, 12, 9)
        val batch = FillBatchRenderState(data, 2, 1, 1, Matrix3x2f().scale(0.5f), null)
        val vertices = mutableListOf<Triple<Float, Float, Int>>()

        batch.buildVertices(recorder(vertices))

        assertEquals(
            listOf(
                Triple(0.5f, 0.5f, 7), Triple(0.5f, 2f, 7), Triple(1.5f, 2f, 7), Triple(1.5f, 0.5f, 7),
                Triple(5.5f, 5.5f, 9), Triple(5.5f, 6.5f, 9), Triple(6f, 6.5f, 9), Triple(6f, 5.5f, 9),
            ),
            vertices,
        )
    }

    /**
     * Verifies that the batch covers the area of all its rectangles after the pose, cut to the
     * scissor, and nothing when the scissor leaves nothing of it.
     */
    @Test
    fun `the batch covers its rectangles within the scissor`() {
        val data = intArrayOf(0, 0, 4, 4, 1, 10, 6, 20, 8, 1)
        val pose = Matrix3x2f().scale(0.5f)

        assertEquals(ScreenRectangle(0, 0, 10, 4), FillBatchRenderState(data, 2, 0, 0, pose, null).bounds())
        assertEquals(ScreenRectangle(2, 0, 8, 4), FillBatchRenderState(data, 2, 0, 0, pose, ScreenRectangle(2, 0, 50, 50)).bounds())
        assertNull(FillBatchRenderState(data, 2, 0, 0, pose, ScreenRectangle(40, 40, 5, 5)).bounds())
    }
}
