package dev.slne.surf.roleplay.fabric.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the parts of the GUI graphics that a custom GUI element needs to be submitted like a
 * built-in one: the render state it is added to and the stack of scissor areas.
 */
@Mixin(GuiGraphicsExtractor.class)
public interface GuiGraphicsExtractorAccessor {

    /**
     * Returns the render state the GUI elements of the current frame are added to.
     *
     * @return the render state
     */
    @Accessor("guiRenderState")
    GuiRenderState surfRoleplay$guiRenderState();

    /**
     * Returns the stack of scissor areas, typed as an object since its class is not accessible;
     * it implements {@link ScissorStackAccessor}.
     *
     * @return the scissor stack
     */
    @Accessor("scissorStack")
    Object surfRoleplay$scissorStack();
}
