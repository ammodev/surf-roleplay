package dev.slne.surf.roleplay.paper.interaction

import dev.slne.surf.roleplay.api.client.paper.interaction.BlockTarget
import dev.slne.surf.roleplay.api.client.paper.interaction.InteractionAction
import dev.slne.surf.roleplay.api.client.paper.interaction.InteractionBehaviour
import dev.slne.surf.roleplay.api.client.paper.interaction.InteractionCondition
import dev.slne.surf.roleplay.api.client.paper.interaction.TargetKind
import dev.slne.surf.roleplay.protocol.interaction.InteractionLimits
import io.mockk.mockk
import net.kyori.adventure.text.Component
import org.bukkit.block.Block
import org.bukkit.entity.Player
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for choosing the options a target offers.
 */
class OptionEvaluatorTest {

    /** The interacting player. */
    private val player = mockk<Player>(relaxed = true)

    /** The targeted block. */
    private val target = BlockTarget(mockk<Block>(relaxed = true))

    /**
     * Creates a bound behaviour with conditions.
     *
     * @param key the behaviour key
     * @param kinds the target kinds
     * @param conditions the conditions
     * @return the bound behaviour
     */
    private fun bound(key: String, kinds: Set<TargetKind> = setOf(TargetKind.BLOCK), conditions: List<InteractionCondition> = emptyList()) =
        BoundBehaviour(InteractionBehaviour(key, kinds, { Component.text(key) }, conditions = conditions, action = InteractionAction { }), emptyMap())

    /**
     * Verifies that exactly the behaviours whose conditions all pass are offered, in order.
     */
    @Test
    fun `only passing behaviours are offered`() {
        val result = OptionEvaluator.passing(
            listOf(bound("a.open"), bound("b.locked", conditions = listOf(InteractionCondition { false })), bound("c.knock", conditions = listOf(InteractionCondition { true }))),
            player, target,
        ) { _, _ -> }

        assertEquals(listOf("a.open", "c.knock"), result.map { it.behaviour.key })
    }

    /**
     * Verifies that a behaviour for another target kind is never offered.
     */
    @Test
    fun `other target kinds are skipped`() {
        val result = OptionEvaluator.passing(listOf(bound("e.talk", setOf(TargetKind.ENTITY))), player, target) { _, _ -> }

        assertEquals(emptyList(), result)
    }

    /**
     * Verifies that a throwing condition counts as failing, is reported, and does not affect the
     * other behaviours.
     */
    @Test
    fun `throwing condition fails only its behaviour`() {
        val errors = mutableListOf<String>()
        val result = OptionEvaluator.passing(
            listOf(bound("a.broken", conditions = listOf(InteractionCondition { error("boom") })), bound("b.fine")),
            player, target,
        ) { key, _ -> errors += key }

        assertEquals(listOf("b.fine"), result.map { it.behaviour.key })
        assertEquals(listOf("a.broken"), errors)
    }

    /**
     * Verifies that at most the option limit is offered.
     */
    @Test
    fun `options are capped`() {
        val many = (1..InteractionLimits.MAX_OPTIONS + 4).map { bound("b.option_$it") }

        assertEquals(InteractionLimits.MAX_OPTIONS, OptionEvaluator.passing(many, player, target) { _, _ -> }.size)
    }
}
