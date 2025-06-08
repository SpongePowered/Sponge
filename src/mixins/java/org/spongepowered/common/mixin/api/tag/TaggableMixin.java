package org.spongepowered.common.mixin.api.tag;

import net.minecraft.core.MappedRegistry;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.tag.Taggable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.stream.Stream;

@Mixin(value = Taggable.class, remap = false)
public interface TaggableMixin<T extends Taggable<T>> {

    @SuppressWarnings("unchecked")
    default Stream<Tag<T>> tags(final DefaultedRegistryType<T> registryType) {
        return (Stream<Tag<T>>) (Object) ((MappedRegistry<T>) registryType.get()).wrapAsHolder((T) this).tags();
    }
}
