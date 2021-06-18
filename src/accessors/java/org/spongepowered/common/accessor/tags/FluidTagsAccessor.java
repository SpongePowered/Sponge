package org.spongepowered.common.accessor.tags;

import net.minecraft.tags.FluidTags;
import net.minecraft.tags.StaticTagHelper;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.common.UntransformedAccessorError;

@Mixin(FluidTags.class)
public interface FluidTagsAccessor {

    @Accessor("HELPER") static StaticTagHelper<Fluid> accessor$helper() { throw new UntransformedAccessorError(); }

}
