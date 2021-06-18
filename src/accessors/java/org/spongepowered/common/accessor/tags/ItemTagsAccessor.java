package org.spongepowered.common.accessor.tags;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.StaticTagHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.common.UntransformedAccessorError;

@Mixin(ItemTags.class)
public interface ItemTagsAccessor {

    @Accessor("HELPER") static StaticTagHelper<Item> accessor$helper() { throw new UntransformedAccessorError(); }

}
