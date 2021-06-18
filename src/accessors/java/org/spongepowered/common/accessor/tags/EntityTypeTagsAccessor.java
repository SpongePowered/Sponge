package org.spongepowered.common.accessor.tags;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.StaticTagHelper;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.common.UntransformedAccessorError;

@Mixin(EntityTypeTags.class)
public interface EntityTypeTagsAccessor {

    @Accessor("HELPER") static StaticTagHelper<EntityType> accessor$helper() { throw new UntransformedAccessorError(); }

}
