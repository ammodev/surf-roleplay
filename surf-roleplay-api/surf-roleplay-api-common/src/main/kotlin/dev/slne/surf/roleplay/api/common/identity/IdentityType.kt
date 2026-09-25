package dev.slne.surf.roleplay.api.common.identity

/**
 * The organisation a [RoleplayIdentity] belongs to.
 */
enum class IdentityType {
    /**
     * An identity without membership in a uniformed organisation.
     */
    CIVILIAN,

    /**
     * An identity that belongs to the police force.
     */
    POLICE,

    /**
     * An identity that belongs to the search-and-rescue service.
     */
    SAR,
}
