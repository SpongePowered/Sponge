package org.spongepowered.common.mixin.api.world;

import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;

@Mixin(LocatableBlock.class)
public class MixinLocatableBlock {

    /**
     * @author gabizou - August 17th, 2018
     * @reason Due to locatable blocks being created thousands of times per tick,
     * we need this to be stupid fast.
     */
    @Overwrite
    public static LocatableBlock.Builder builder() {
        return new SpongeLocatableBlockBuilder();
    }
}
