package org.spongepowered.common.mixin.api.mcp.particles;

import com.google.common.collect.ImmutableMap;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.effect.particle.ParticleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.effect.particle.ParticleOptionDefaults;
import org.spongepowered.common.effect.particle.ParticleTypeExtra;

import java.util.Map;
import java.util.Optional;

@Mixin(net.minecraft.particles.ParticleType.class)
public abstract class ParticleTypeMixin_API implements ParticleTypeExtra {

    private ResourceKey impl$key = null;
    private ImmutableMap<ParticleOption<?>, Object> impl$defaultOptions = null;

    @Override
    public Map<ParticleOption<?>, Object> getDefaultOptionMap() {
        return this.impl$defaultOptions;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> Optional<V> getDefaultOption(ParticleOption<V> option) {
        if (this.impl$defaultOptions == null) {
            this.impl$defaultOptions = ParticleOptionDefaults.generateDefaultsForNamed((ParticleType<?>) (Object) this);
        }
        return Optional.ofNullable((V) this.impl$defaultOptions.get(option));
    }

    @Override
    public Map<ParticleOption<?>, Object> getDefaultOptions() {
        if (this.impl$defaultOptions == null) {
            this.impl$defaultOptions = ParticleOptionDefaults.generateDefaultsForNamed((ParticleType<?>) (Object) this);
        }
        return this.impl$defaultOptions;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public ResourceKey getKey() {
        if (this.impl$key == null) {
            final ResourceLocation location = Registry.PARTICLE_TYPE.getKey((net.minecraft.particles.ParticleType<?>) (Object) this);
            this.impl$key = (ResourceKey) (Object) location;
        }
        return this.impl$key;
    }
}