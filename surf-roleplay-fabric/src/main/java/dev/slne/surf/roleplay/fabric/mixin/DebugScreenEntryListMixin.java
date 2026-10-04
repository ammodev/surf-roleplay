package dev.slne.surf.roleplay.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.slne.surf.roleplay.fabric.vanilla.HiddenDebugEntries;
import dev.slne.surf.roleplay.fabric.vanilla.RoleplayUi;
import java.util.Collection;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Removes the debug screen entries that print coordinates from the enabled entries while the
 * roleplay server is active.
 */
@Mixin(DebugScreenEntryList.class)
public abstract class DebugScreenEntryListMixin {

    /**
     * Tells whether an entry is a hidden coordinate entry.
     *
     * @param id the identifier of the debug screen entry
     * @return true if the entry is hidden while the roleplay server is active
     */
    private static boolean surfRoleplay$isHidden(Identifier id) {
        return "minecraft".equals(id.getNamespace()) && HiddenDebugEntries.INSTANCE.getPATHS().contains(id.getPath());
    }

    /**
     * Filters the hidden entries out of the enabled entries.
     *
     * @param original the enabled entries
     * @return the enabled entries without the hidden ones while the roleplay server is active
     */
    @ModifyReturnValue(method = "getCurrentlyEnabled", at = @At("RETURN"))
    private Collection<Identifier> surfRoleplay$filterEnabled(Collection<Identifier> original) {
        return HiddenDebugEntries.INSTANCE.visible(original, DebugScreenEntryListMixin::surfRoleplay$isHidden, RoleplayUi.isActive());
    }

    /**
     * Reports hidden entries as disabled while the roleplay server is active.
     *
     * @param original whether the entry is enabled
     * @param id the identifier of the debug screen entry
     * @return false for hidden entries while the roleplay server is active, otherwise the original value
     */
    @ModifyReturnValue(method = "isCurrentlyEnabled", at = @At("RETURN"))
    private boolean surfRoleplay$filterEnabledEntry(boolean original, Identifier id) {
        return original && !(RoleplayUi.isActive() && surfRoleplay$isHidden(id));
    }
}
