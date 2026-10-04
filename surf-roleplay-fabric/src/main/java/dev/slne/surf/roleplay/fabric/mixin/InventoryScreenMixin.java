package dev.slne.surf.roleplay.fabric.mixin;

import dev.slne.surf.roleplay.fabric.vanilla.CraftingUi;
import dev.slne.surf.roleplay.fabric.vanilla.RoleplayUi;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Removes the crafting area of the player inventory screen while the roleplay server is active:
 * the 2x2 grid, the arrow and the result slot are painted over with the panel colour, and the
 * crafting title above them is not drawn.
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractRecipeBookScreen<InventoryMenu> {

    /**
     * Satisfies the compiler; the constructor of a mixin class is never called.
     *
     * @param menu the inventory menu
     * @param recipeBook the recipe book component
     * @param inventory the player's inventory
     * @param title the screen title
     */
    private InventoryScreenMixin(InventoryMenu menu, RecipeBookComponent<?> recipeBook, Inventory inventory, Component title) {
        super(menu, recipeBook, inventory, title);
    }

    /**
     * Paints the crafting area over with the panel colour right after the inventory texture is
     * drawn, so the player model is still drawn on top.
     *
     * @param graphics the graphics the screen is drawn with
     * @param mouseX the horizontal mouse position
     * @param mouseY the vertical mouse position
     * @param partialTick the frame time
     * @param ci the callback of the injection
     */
    @Inject(
            method = "extractBackground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V",
                    shift = At.Shift.AFTER
            )
    )
    private void surfRoleplay$coverCraftingArea(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!RoleplayUi.isActive()) {
            return;
        }
        int x = leftPos + CraftingUi.GRID_X;
        int y = topPos + CraftingUi.GRID_Y;
        graphics.fill(x, y, x + CraftingUi.GRID_WIDTH, y + CraftingUi.GRID_HEIGHT, CraftingUi.PANEL_COLOR);
    }

    /**
     * Skips drawing the crafting title above the crafting area, the only label of the screen.
     *
     * @param graphics the graphics the screen is drawn with
     * @param mouseX the horizontal mouse position
     * @param mouseY the vertical mouse position
     * @param ci the callback of the injection
     */
    @Inject(method = "extractLabels", at = @At("HEAD"), cancellable = true)
    private void surfRoleplay$hideCraftingTitle(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (RoleplayUi.isActive()) {
            ci.cancel();
        }
    }
}
