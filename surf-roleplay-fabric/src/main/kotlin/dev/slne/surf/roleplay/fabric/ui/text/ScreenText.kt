package dev.slne.surf.roleplay.fabric.ui.text

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import dev.slne.surf.roleplay.fabric.RoleplayClient
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization

/**
 * Turns the component JSON of screen texts into vanilla components.
 *
 * Parsed texts are cached. Text that is not valid component JSON is shown as its raw string in
 * red, so that a broken text is visible instead of silently missing.
 */
object ScreenText {

    /**
     * The largest number of parsed texts kept in the cache.
     */
    private const val CACHE_SIZE = 512

    /**
     * The parsed texts keyed by their JSON, in access order.
     */
    private val cache = object : LinkedHashMap<String, Component>(CACHE_SIZE, 0.75f, true) {
        /**
         * Drops the least recently used text once the cache is full.
         *
         * @param eldest the least recently used entry
         * @return whether to drop it
         */
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Component>): Boolean = size > CACHE_SIZE
    }

    /**
     * Parses component JSON into a component.
     *
     * @param json the component JSON; an empty string is the empty component
     * @return the component, or the raw string in red if [json] is not valid component JSON
     */
    fun parse(json: String): Component {
        if (json.isEmpty()) return Component.empty()
        synchronized(cache) { cache[json]?.let { return it } }
        val component = decode(json)
        synchronized(cache) { cache[json] = component }
        return component
    }

    /**
     * Decodes component JSON with the registries of the current world, if any.
     *
     * @param json the component JSON
     * @return the component, or the raw string in red if decoding fails
     */
    private fun decode(json: String): Component = try {
        val element = JsonParser.parseString(json)
        val ops = Minecraft.getInstance().level?.registryAccess()?.createSerializationContext(JsonOps.INSTANCE) ?: JsonOps.INSTANCE
        ComponentSerialization.CODEC.parse(ops, element).result().orElseGet { fallback(json) }
    } catch (exception: RuntimeException) {
        RoleplayClient.log.debug("Invalid screen text {}", json, exception)
        fallback(json)
    }

    /**
     * Creates the component shown for text that cannot be parsed.
     *
     * @param json the raw text
     * @return the raw text in red
     */
    private fun fallback(json: String): Component = Component.literal(json).withStyle(ChatFormatting.RED)
}
