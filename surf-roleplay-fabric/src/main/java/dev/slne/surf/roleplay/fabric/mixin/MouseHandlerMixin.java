package dev.slne.surf.roleplay.fabric.mixin;

import dev.slne.surf.roleplay.fabric.toast.HudCursor;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Routes mouse buttons to the HUD cursor while it is shown, so that clicks reach the toasts
 * instead of the world and do not capture the mouse again.
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    /**
     * Passes a mouse button to the HUD cursor and drops it if the cursor takes it.
     *
     * @param window the window handle
     * @param info the mouse button
     * @param action the GLFW action
     * @param ci the callback of the injection
     */
    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void surfRoleplay$routeToHudCursor(long window, MouseButtonInfo info, int action, CallbackInfo ci) {
        if (HudCursor.handleButton(info.button(), action)) {
            ci.cancel();
        }
    }
}
