package dev.slne.surf.roleplay.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.slne.surf.roleplay.fabric.vanilla.RoleplayUi;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Leaves the recipe book button out of every screen with a recipe book while the roleplay server is
 * active.
 */
@Mixin(AbstractRecipeBookScreen.class)
public abstract class RecipeBookScreenMixin {

    /**
     * Adds the recipe book button to the screen only while the roleplay server is not active.
     *
     * @param screen the screen the button is added to
     * @param widget the recipe book button
     * @param original the call that adds the button
     * @return the button
     */
    @WrapOperation(
            method = "initButton",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractRecipeBookScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"
            )
    )
    private GuiEventListener surfRoleplay$skipRecipeBookButton(
            AbstractRecipeBookScreen<?> screen,
            GuiEventListener widget,
            Operation<GuiEventListener> original
    ) {
        if (RoleplayUi.isActive()) {
            return widget;
        }
        return original.call(screen, widget);
    }
}
