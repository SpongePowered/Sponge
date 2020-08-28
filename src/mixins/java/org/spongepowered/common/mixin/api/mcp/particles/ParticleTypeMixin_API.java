package org.spongepowered.common.mixin.api.mcp.particles;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.effect.particle.ParticleOption;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Mixin(net.minecraft.particles.ParticleType.class)
public abstract class ParticleTypeMixin_API implements ParticleType {

    private ResourceKey impl$key;

    @Override
    public <V> Optional<V> getDefaultOption(ParticleOption<V> option) {
        // TODO
        return Optional.empty();
    }

    @Override
    public Map<ParticleOption<?>, Object> getDefaultOptions() {
        // TODO
        return Collections.emptyMap();
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