package dev.slne.surf.roleplay.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.slne.surf.roleplay.fabric.vanilla.RoleplayUi;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Keeps the recipe book closed while the roleplay server is active, whatever the player's saved
 * recipe book settings say.
 */
@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin {

    /**
     * Reports the recipe book as closed in the player's settings while the roleplay server is active.
     *
     * @param original whether the player's settings keep the recipe book open
     * @return false while the roleplay server is active, otherwise the original value
     */
    @ModifyReturnValue(method = "isVisibleAccordingToBookData", at = @At("RETURN"))
    private boolean surfRoleplay$keepClosed(boolean original) {
        return original && !RoleplayUi.isActive();
    }
}
