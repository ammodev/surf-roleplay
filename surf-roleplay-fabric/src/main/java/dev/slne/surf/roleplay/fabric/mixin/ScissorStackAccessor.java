package dev.slne.surf.roleplay.fabric.mixin;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Exposes the current scissor area of the GUI graphics' scissor stack.
 */
@Mixin(targets = "net.minecraft.client.gui.GuiGraphicsExtractor$ScissorStack")
public interface ScissorStackAccessor {

    /**
     * Returns the scissor area in effect, the overlap of every pushed area.
     *
     * @return the area, or {@code null} while nothing is clipped
     */
    @Invoker("peek")
    ScreenRectangle surfRoleplay$peek();
}
