package dev.slne.surf.roleplay.fabric.interaction

import dev.slne.surf.roleplay.protocol.interaction.BlockTargetId
import dev.slne.surf.roleplay.protocol.interaction.InteractionLimits
import dev.slne.surf.roleplay.protocol.interaction.TargetKinds
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult

/**
 * Finds the block or entity under the crosshair, up to the longest binding range.
 */
object TargetPicker {
    /**
     * Raycasts from the camera along the view direction.
     *
     * @param mc the Minecraft client
     * @return the target, or `null` if the crosshair is on nothing within reach
     */
    fun pick(mc: Minecraft): TargetRef? {
        val camera = mc.cameraEntity ?: return null
        val partial = mc.deltaTracker.getGameTimeDeltaPartialTick(true)
        val eye = camera.getEyePosition(partial)
        val look = camera.getViewVector(partial)
        val end = eye.add(look.scale(InteractionLimits.MAX_RANGE))
        val block = camera.level().clip(ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, camera))
        val reach = if (block.type == HitResult.Type.MISS) InteractionLimits.MAX_RANGE else block.location.distanceTo(eye)
        val box = camera.boundingBox.expandTowards(look.scale(reach)).inflate(1.0)
        val entity = ProjectileUtil.getEntityHitResult(camera, eye, eye.add(look.scale(reach)), box, { !it.isSpectator && it.isPickable }, reach * reach)
        if (entity != null) return TargetRef(TargetKinds.ENTITY, entity.entity.uuid.toString())
        if (block.type != HitResult.Type.BLOCK) return null
        val pos = block.blockPos
        return TargetRef(TargetKinds.BLOCK, BlockTargetId.format(pos.x, pos.y, pos.z))
    }
}
