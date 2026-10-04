package dev.slne.surf.roleplay.api.client.common.screen.dsl

import dev.slne.surf.roleplay.api.client.common.screen.ContainerElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests that the component DSL has a component for every kind of screen element.
 */
class ComponentCoverageTest {

    /**
     * Verifies that the kitchen sink screen, which uses every component, contains every concrete
     * element class.
     */
    @Test
    fun `every element kind has a component`() {
        val built = mutableSetOf<Class<*>>()
        /** Adds the class of [e] and of every element below it to the built classes. */
        fun walk(e: ScreenElement) {
            built += e.javaClass
            (e as? ContainerElement)?.children?.forEach(::walk)
        }
        walk(KitchenSink.definition().root)
        val expected = concreteElementClasses()
        assertEquals(emptyList(), (expected - built).map { it.simpleName }.sorted(), "Element classes without a component")
        assertEquals(expected, built)
    }

    /**
     * Verifies that the element classes are found through the sealed hierarchy, so that the
     * coverage check cannot pass on an empty set.
     */
    @Test
    fun `the sealed hierarchy lists the element classes`() {
        assertTrue(concreteElementClasses().size > 100, "Expected the sealed hierarchy to list the element classes")
    }

    /**
     * Collects every concrete class below [ScreenElement] by following the permitted subclasses of
     * the sealed interfaces.
     *
     * @return the concrete element classes
     */
    private fun concreteElementClasses(): Set<Class<*>> {
        val result = mutableSetOf<Class<*>>()
        /** Adds [type] if it is concrete, or the concrete classes below it if it is sealed. */
        fun collect(type: Class<*>) {
            val permitted = type.permittedSubclasses
            if (permitted == null) {
                result += type
            } else {
                permitted.forEach(::collect)
            }
        }
        collect(ScreenElement::class.java)
        return result
    }
}
