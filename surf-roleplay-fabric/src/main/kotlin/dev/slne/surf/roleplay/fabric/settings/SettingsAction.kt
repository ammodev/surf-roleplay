package dev.slne.surf.roleplay.fabric.settings

/**
 * What a button of the settings screen asks for.
 */
sealed interface SettingsAction {

    /**
     * Wait for a new key for one binding.
     *
     * @property id the id of the binding
     */
    data class StartCapture(val id: String) : SettingsAction

    /**
     * Restore the default key of one binding.
     *
     * @property id the id of the binding
     */
    data class Reset(val id: String) : SettingsAction

    /**
     * Restore the default key of every binding.
     */
    data object ResetAll : SettingsAction

    /**
     * Reads widget ids as actions.
     */
    companion object {
        /**
         * Returns the action of a settings widget.
         *
         * @param widgetId the id of the widget that was triggered
         * @return the action, or `null` if the widget triggers none
         */
        fun of(widgetId: String): SettingsAction? = when {
            widgetId == SettingsView.RESET_ALL_ID -> ResetAll
            widgetId.startsWith(SettingsView.BINDING_PREFIX) ->
                widgetId.removePrefix(SettingsView.BINDING_PREFIX).takeIf { it.isNotEmpty() }?.let(::StartCapture)
            widgetId.startsWith(SettingsView.RESET_PREFIX) ->
                widgetId.removePrefix(SettingsView.RESET_PREFIX).takeIf { it.isNotEmpty() }?.let(::Reset)
            else -> null
        }
    }
}
