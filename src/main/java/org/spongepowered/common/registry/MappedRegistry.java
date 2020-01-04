package org.spongepowered.common.registry;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.registry.SimpleRegistry;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;

public final class MappedRegistry<T, U> extends SimpleRegistry<T> {

    private final Map<T, U> mappings;
    private final Map<U, T> reverseMappings;

    MappedRegistry() {
        this.mappings = new Object2ObjectOpenHashMap<>();
        this.reverseMappings = new Object2ObjectOpenHashMap<>();
    }

    void registerMapping(T value, U mapping) {
        this.mappings.put(value, mapping);
        this.reverseMappings.put(mapping, value);
    }

    @Nullable
    public U getMapping(T value) {
        return this.mappings.get(value);
    }

    @Nullable
    public T getReverseMapping(U value) {
        return this.reverseMappings.get(value);
    }
}
