package dev.slne.surf.roleplay.paper.interaction.binding

import dev.slne.surf.roleplay.api.client.paper.interaction.InteractionBehaviour
import dev.slne.surf.roleplay.api.client.paper.interaction.TargetKind
import dev.slne.surf.roleplay.protocol.interaction.InteractionLimits
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConfiguration

/**
 * Reads and validates the bindings file.
 */
object BindingParser {

    /**
     * The pattern of entity binding keys.
     */
    val ENTITY_KEY_PATTERN: Regex = Regex("[a-z0-9_.-]{1,64}")

    /**
     * The parsed bindings with the errors of the skipped entries.
     *
     * @property bindings the valid bindings
     * @property errors one message per skipped entry
     */
    data class Result(val bindings: Bindings, val errors: List<String>)

    /**
     * Thrown when the file is not valid YAML.
     *
     * @param message what is wrong
     * @param cause the YAML error
     */
    class InvalidFileException(message: String, cause: Throwable) : Exception(message, cause)

    /**
     * Parses the bindings file. An entry with any error is skipped and reported; the other
     * entries load. A later entry for a position or key that is already bound is an error.
     *
     * @param yaml the file content
     * @param kindsOf returns the target kinds of a registered behaviour key, or `null` if unknown
     * @return the bindings and the errors
     * @throws InvalidFileException if the content is not valid YAML
     */
    fun parse(yaml: String, kindsOf: (String) -> Set<TargetKind>?): Result {
        val config = YamlConfiguration()
        try {
            config.loadFromString(yaml)
        } catch (exception: InvalidConfigurationException) {
            throw InvalidFileException("interactions.yml is not valid YAML", exception)
        }
        val errors = mutableListOf<String>()
        val blocks = linkedMapOf<BlockPosition, TargetBinding>()
        val entities = linkedMapOf<String, TargetBinding>()

        config.getMapList("blocks").forEachIndexed { index, entry ->
            val where = "blocks[$index]"
            val world = entry["world"] as? String
            val coords = listOf("x", "y", "z").map { (entry[it] as? Number)?.takeIf { n -> n.toDouble() % 1.0 == 0.0 && n.toDouble() in Int.MIN_VALUE.toDouble()..Int.MAX_VALUE.toDouble() }?.toInt() }
            val binding = target(where, entry, TargetKind.BLOCK, kindsOf, errors) ?: return@forEachIndexed
            when {
                world.isNullOrBlank() -> errors += "$where: missing world"
                coords.any { it == null } -> errors += "$where: x, y and z must be whole numbers"
                else -> {
                    val position = BlockPosition(world, coords[0]!!, coords[1]!!, coords[2]!!)
                    if (position in blocks) errors += "$where: $position is already bound" else blocks[position] = binding
                }
            }
        }

        config.getMapList("entities").forEachIndexed { index, entry ->
            val where = "entities[$index]"
            val key = entry["key"] as? String
            val binding = target(where, entry, TargetKind.ENTITY, kindsOf, errors) ?: return@forEachIndexed
            when {
                key == null || !ENTITY_KEY_PATTERN.matches(key) -> errors += "$where: key must match ${ENTITY_KEY_PATTERN.pattern}"
                key in entities -> errors += "$where: $key is already bound"
                else -> entities[key] = binding
            }
        }
        return Result(Bindings(blocks, entities), errors)
    }

    /**
     * Reads the behaviours and range of one entry.
     *
     * @param where the entry's location, for messages
     * @param entry the entry
     * @param kind the target kind of the entry
     * @param kindsOf returns the kinds of a behaviour key
     * @param errors receives the error of an invalid entry
     * @return the binding, or `null` if the entry is invalid
     */
    private fun target(where: String, entry: Map<*, *>, kind: TargetKind, kindsOf: (String) -> Set<TargetKind>?, errors: MutableList<String>): TargetBinding? {
        val range = entry["range"]?.let { (it as? Number)?.toDouble() ?: Double.NaN }
        if (range != null && !(range > 0.0 && range <= InteractionLimits.MAX_RANGE)) {
            errors += "$where: range must be above 0 and at most ${InteractionLimits.MAX_RANGE}"
            return null
        }
        val raw = entry["behaviours"] as? List<*>
        if (raw.isNullOrEmpty()) {
            errors += "$where: no behaviours"
            return null
        }
        if (raw.size > InteractionLimits.MAX_OPTIONS) {
            errors += "$where: at most ${InteractionLimits.MAX_OPTIONS} behaviours"
            return null
        }
        val behaviours = raw.map { item ->
            val (key, parameters) = when (item) {
                is String -> item to emptyMap<String, String>()
                is Map<*, *> -> (item["key"] as? String) to ((item["parameters"] as? Map<*, *>)?.entries?.associate { (k, v) -> k.toString() to v.toString() } ?: emptyMap())
                else -> null to emptyMap()
            }
            if (key == null || !InteractionBehaviour.KEY_PATTERN.matches(key)) {
                errors += "$where: malformed behaviour $item"
                return null
            }
            val kinds = kindsOf(key)
            if (kinds == null) {
                errors += "$where: unknown behaviour $key"
                return null
            }
            if (kind !in kinds) {
                errors += "$where: behaviour $key does not apply to ${kind.key} targets"
                return null
            }
            BehaviourBinding(key, parameters)
        }
        val duplicate = behaviours.groupBy { it.key }.entries.firstOrNull { it.value.size > 1 }?.key
        if (duplicate != null) {
            errors += "$where: duplicate behaviour $duplicate"
            return null
        }
        return TargetBinding(behaviours, range)
    }
}
