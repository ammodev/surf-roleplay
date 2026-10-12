package dev.slne.surf.roleplay.paper.interaction

import dev.slne.surf.roleplay.api.client.common.toast.Toast
import dev.slne.surf.roleplay.api.client.common.toast.ToastType
import dev.slne.surf.roleplay.api.client.paper.interaction.RadialAction
import dev.slne.surf.roleplay.api.client.paper.interaction.RadialHandler
import dev.slne.surf.roleplay.api.client.paper.interaction.RadialSubmenu
import dev.slne.surf.roleplay.paper.toast.PaperToastService
import net.kyori.adventure.text.Component

/**
 * The demo entry of the radial menu, registered only when `interaction.demo-entries` is on.
 */
object DemoRadialEntries {
    /**
     * The submenu "Test" with the entry "Hallo", which shows a toast.
     */
    val entry: RadialSubmenu = RadialSubmenu(
        key = "demo",
        label = Component.text("Test"),
        icon = "flask-conical",
        children = listOf(
            RadialAction("demo.hello", Component.text("Hallo"), icon = "hand", handler = RadialHandler { player ->
                PaperToastService.INSTANCE.show(player, Toast(Component.text("Radialmenü funktioniert."), type = ToastType.INFO))
            }),
        ),
    )
}
