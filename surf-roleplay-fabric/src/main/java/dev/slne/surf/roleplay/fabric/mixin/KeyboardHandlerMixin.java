package dev.slne.surf.roleplay.fabric.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.slne.surf.roleplay.fabric.vanilla.RoleplayUi;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stops the debug key actions that copy locations to the clipboard while the roleplay server is
 * active, without disabling the other debug keys.
 */
@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

    /**
     * Skips writing the teleport command of the F3+C action to the clipboard while the roleplay
     * server is active.
     *
     * @param handler the keyboard handler
     * @param text the command that would be copied
     * @return true if the text may be copied
     */
    @WrapWithCondition(
        method = "handleDebugKeys",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyboardHandler;setClipboard(Ljava/lang/String;)V")
    )
    private boolean surfRoleplay$blockCopyLocation(KeyboardHandler handler, String text) {
        return !RoleplayUi.isActive();
    }

    /**
     * Cancels the F3+I action, which copies the command that recreates the looked-at block or
     * entity including its position, while the roleplay server is active.
     *
     * @param includeData whether the data of the target is copied as well
     * @param self whether the player itself is the target
     * @param ci the callback of the injection
     */
    @Inject(method = "copyRecreateCommand", at = @At("HEAD"), cancellable = true)
    private void surfRoleplay$blockCopyRecreate(boolean includeData, boolean self, CallbackInfo ci) {
        if (RoleplayUi.isActive()) {
            ci.cancel();
        }
    }
}
