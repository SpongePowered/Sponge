package org.spongepowered.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.tags.StaticTagHelper;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.tag.Tag;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface SpongeRegistryHolder extends RegistryHolder {

    void setRootMinecraftRegistry(Registry<Registry<?>> registry);

    <T> org.spongepowered.api.registry.Registry<T> createRegistry(final RegistryType<T> type, final @Nullable InitialRegistryData<T> defaultValues,
        final boolean isDynamic, final @Nullable BiConsumer<net.minecraft.resources.ResourceKey<T>, T> callback);

    default <T> org.spongepowered.api.registry.Registry<T> createRegistry(final RegistryType<T> type, final @Nullable Map<ResourceKey, T> defaultValues) {
        return this.createRegistry(type, defaultValues != null ? () -> defaultValues : null, false);
    }

    default <T> org.spongepowered.api.registry.Registry<T> createRegistry(final RegistryType<T> type, final @Nullable Supplier<Map<ResourceKey, T>> defaultValues) {
        return this.createRegistry(type, defaultValues, false);
    }

    default <T> org.spongepowered.api.registry.Registry<T> createRegistry(final RegistryType<T> type, final @Nullable Supplier<Map<ResourceKey, T>> defaultValues,
        final boolean isDynamic) {
        return this.createRegistry(type, InitialRegistryData.noIds(defaultValues), isDynamic, null);
    }

    default <T> org.spongepowered.api.registry.Registry<T> createRegistry(final RegistryType<T> type, final RegistryLoader<T> loader) {
        return this.createRegistry(type, loader, false);
    }

    default <T> org.spongepowered.api.registry.Registry<T> createRegistry(final RegistryType<T> type, final RegistryLoader<T> loader,
        final boolean isDynamic) {
        return this.createRegistry(type, loader, isDynamic, null);
    }

    <T> void wrapTagHelperAsRegistry(RegistryType<Tag<T>> type, StaticTagHelper<T> helper);
}
