package org.spongepowered.common.mixin.accessor.tileentity;

import net.minecraft.tileentity.BannerPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BannerPattern.class)
public interface BannerPatternAccessor {

    @Accessor("fileName") String accessor$getFileName();
}
