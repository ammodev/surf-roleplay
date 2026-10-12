package dev.slne.surf.roleplay.paper.interaction

import dev.slne.surf.roleplay.api.client.paper.interaction.BlockTarget
import dev.slne.surf.roleplay.api.client.paper.interaction.InteractionAction
import dev.slne.surf.roleplay.api.client.paper.interaction.InteractionBehaviour
import dev.slne.surf.roleplay.api.client.paper.interaction.InteractionCondition
import dev.slne.surf.roleplay.api.client.paper.interaction.InteractionContext
import dev.slne.surf.roleplay.api.client.paper.interaction.TargetKind
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.Openable
import org.bukkit.block.data.type.Door

/**
 * The behaviour `door.toggle`: opens or closes a door, trapdoor or gate.
 */
object DoorToggle {

    /**
     * Returns the openable data of the context's block.
     *
     * @param context the context
     * @return the data, or `null` if the target is not an openable block
     */
    private fun openable(context: InteractionContext): Openable? =
        ((context.target as? BlockTarget)?.block?.blockData) as? Openable

    /**
     * Returns the German caption for a door state.
     *
     * @param open whether the door is open
     * @return "Schließen" for an open door, "Öffnen" otherwise
     */
    fun label(open: Boolean): String = if (open) "Schließen" else "Öffnen"

    /**
     * Applies an open state to the other half of a door, so both halves agree without waiting for
     * a neighbour update. Single-block openables such as trapdoors and gates have no other half.
     *
     * @param block the block that was toggled
     * @param data the block data of the toggled block
     * @param open the open state to apply
     */
    private fun syncOtherHalf(block: Block, data: Openable, open: Boolean) {
        if (data !is Door) return
        val face = if (data.half == Bisected.Half.BOTTOM) BlockFace.UP else BlockFace.DOWN
        val other = block.getRelative(face)
        val otherData = other.blockData as? Door ?: return
        if (other.type != block.type || otherData.half == data.half) return
        otherData.isOpen = open
        other.blockData = otherData
    }

    /**
     * The behaviour: toggles the open state of doors, trapdoors and gates (both halves of a door),
     * plays the matching door sound once, and is labelled "Öffnen" or "Schließen" by the current
     * state.
     */
    val behaviour: InteractionBehaviour = InteractionBehaviour(
        key = "door.toggle",
        kinds = setOf(TargetKind.BLOCK),
        label = { Component.text(label(openable(it)?.isOpen == true)) },
        icon = "door-open",
        conditions = listOf(InteractionCondition { openable(it) != null }),
        action = InteractionAction { context ->
            val block = (context.target as BlockTarget).block
            val data = block.blockData as Openable
            val open = !data.isOpen
            data.isOpen = open
            block.blockData = data
            syncOtherHalf(block, data, open)
            val iron = block.type == Material.IRON_DOOR || block.type == Material.IRON_TRAPDOOR
            val sound = when {
                iron && open -> Sound.BLOCK_IRON_DOOR_OPEN
                iron -> Sound.BLOCK_IRON_DOOR_CLOSE
                open -> Sound.BLOCK_WOODEN_DOOR_OPEN
                else -> Sound.BLOCK_WOODEN_DOOR_CLOSE
            }
            block.world.playSound(block.location.toCenterLocation(), sound, 1f, 1f)
        },
    )
}
