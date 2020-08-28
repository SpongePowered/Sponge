package org.spongepowered.common.effect.particle;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.effect.particle.ParticleOption;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.common.SpongeCatalogType;

import java.util.Map;
import java.util.Optional;

public final class NumericalParticleType extends SpongeCatalogType implements ParticleType {

    private final int id;
    private final Map<ParticleOption<?>, Object> defaultOptions;

    public NumericalParticleType(int id, ResourceKey key, Map<ParticleOption<?>, Object> defaultOptions) {
        super(key);
        this.id = id;
        this.defaultOptions = defaultOptions;
    }

    public int getId() {
        return this.id;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> Optional<V> getDefaultOption(final ParticleOption<V> option) {
        return Optional.ofNullable((V) this.defaultOptions.get(option));
    }

    @Override
    public Map<ParticleOption<?>, Object> getDefaultOptions() {
        return this.defaultOptions;
    }
}