package dev.slne.surf.roleplay.api.common.license

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.api.core.util.mutableObject2BooleanMapOf
import dev.slne.surf.roleplay.api.common.identity.RoleplayIdentity
import dev.slne.surf.roleplay.api.common.license.requirement.LicenseRequirement
import it.unimi.dsi.fastutil.objects.Object2BooleanMap
import it.unimi.dsi.fastutil.objects.ObjectList
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

abstract class License(
    val key: Key,
    displayName: SurfComponentBuilder.() -> Unit,
    val requirements: ObjectList<LicenseRequirement>
) : ComponentLike {
    val displayName: Component = SurfComponentBuilder.builder().apply(displayName).build()

    override fun asComponent(): Component {
        return displayName
    }

    /**
     * Evaluates every requirement of this license against [identity].
     *
     * @param identity the identity the license would be granted to
     * @return the overall outcome together with the result of each individual requirement
     */
    fun calculateRequirements(identity: RoleplayIdentity): LicenseRequirementsCalculationResult {
        val results = mutableObject2BooleanMapOf<LicenseRequirement>()

        for (requirement in requirements) {
            val result = requirement.isMet(identity)

            results[requirement] = result
        }

        return LicenseRequirementsCalculationResult(
            isMet = results.values.all { it },
            results = results,
        )
    }

    class LicenseRequirementsCalculationResult(
        val isMet: Boolean,
        val results: Object2BooleanMap<LicenseRequirement>,
    ) : ComponentLike {
        private val component = buildText {
            if (isMet) {
                success("✅ Alle Anforderungen erfüllt")
            } else {
                error("❌ Nicht alle Anforderungen erfüllt")
            }
            appendNewline(2)

            val met = results.filterValues { it }
            val notMet = results.filterValues { !it }

            if (met.isNotEmpty()) {
                success("✅ Erfüllte Anforderungen:")

                for ((requirement, _) in met) {
                    appendNewline()
                    append(requirement)
                }
            }

            if (met.isNotEmpty() && notMet.isNotEmpty()) {
                appendNewline(2)
            }

            if (notMet.isNotEmpty()) {
                error("❌ Nicht erfüllte Anforderungen:")

                for ((requirement, _) in notMet) {
                    appendNewline()
                    append(requirement)
                }
            }
        }

        override fun asComponent(): Component = component
    }
}