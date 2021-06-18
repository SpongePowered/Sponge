package org.spongepowered.common.accessor.tags;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.StaticTagHelper;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.common.UntransformedAccessorError;

@Mixin(BlockTags.class)
public interface BlockTagsAccessor {

    @Accessor("HELPER") static StaticTagHelper<Block> accessor$helper() { throw new UntransformedAccessorError(); }

}
