package dev.slne.surf.roleplay.fabric.mixin;

import dev.slne.surf.roleplay.fabric.vanilla.RoleplayUi;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides the Tab player list while the roleplay server is active.
 */
@Mixin(Hud.class)
public abstract class HudMixin {

    /**
     * Skips drawing the player list.
     *
     * @param graphics the graphics the HUD is drawn with
     * @param deltaTracker the frame timing
     * @param ci the callback of the injection
     */
    @Inject(method = "extractTabList", at = @At("HEAD"), cancellable = true)
    private void surfRoleplay$hidePlayerList(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (RoleplayUi.isActive()) {
            ci.cancel();
        }
    }
}
