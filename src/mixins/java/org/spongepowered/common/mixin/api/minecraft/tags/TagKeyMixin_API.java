package org.spongepowered.common.mixin.api.minecraft.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TagKey.class)
public class TagKeyMixin_API<T> implements Tag<T> {

    @Shadow @Final private ResourceLocation location;

    @Override
    public ResourceKey key() {
        return (ResourceKey) (Object) this.location;
    }
}
