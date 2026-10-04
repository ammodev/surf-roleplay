package dev.slne.surf.roleplay.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.slne.surf.roleplay.fabric.vanilla.CraftingUi;
import dev.slne.surf.roleplay.fabric.vanilla.RoleplayUi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Deactivates the crafting grid and crafting result slots of the player's own inventory while the
 * roleplay server is active, so they are neither drawn nor clickable and items are never moved
 * into them.
 */
@Mixin(Slot.class)
public abstract class SlotMixin {

    /**
     * The container the slot shows an item of.
     */
    @Shadow
    @Final
    public Container container;

    /**
     * Reports hidden crafting slots as inactive.
     *
     * @param original whether the slot is active
     * @return false for a hidden crafting slot, otherwise the original value
     */
    @ModifyReturnValue(method = "isActive", at = @At("RETURN"))
    private boolean surfRoleplay$hideCraftingSlot(boolean original) {
        boolean active = RoleplayUi.isActive();
        if (!original || !active) {
            return original;
        }
        boolean isCraftingSlot = container instanceof CraftingContainer;
        boolean isResultSlot = (Object) this instanceof ResultSlot;
        boolean inPlayerInventoryMenu = (isCraftingSlot || isResultSlot) && surfRoleplay$inPlayerInventoryMenu();
        return !CraftingUi.INSTANCE.hidesSlot(isCraftingSlot, isResultSlot, inPlayerInventoryMenu, active);
    }

    /**
     * Tells whether this slot is a crafting grid or crafting result slot of the local player's own
     * inventory menu.
     *
     * @return true if the slot shows the local player's crafting grid or its result
     */
    private boolean surfRoleplay$inPlayerInventoryMenu() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }
        InventoryMenu menu = player.inventoryMenu;
        return container == menu.getCraftSlots() || (Object) this == menu.getResultSlot();
    }
}
