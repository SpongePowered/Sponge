package org.spongepowered.common.registry.builtin.sponge;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.effect.particle.ParticleOption;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.effect.particle.SpongeParticleOption;
import org.spongepowered.math.vector.Vector3d;

import java.util.stream.Stream;

public final class ParticleOptionStreamGenerator {

    private ParticleOptionStreamGenerator() {
    }

    public static Stream<ParticleOption<?>> stream() {
        return Stream.of(
                new SpongeParticleOption<>(ResourceKey.sponge("block_state"), BlockState.class),
                new SpongeParticleOption<>(ResourceKey.sponge("color"), Color.class),
                new SpongeParticleOption<>(ResourceKey.sponge("direction"), Direction.class),
                new SpongeParticleOption<>(ResourceKey.sponge("item_stack_snapshot"), ItemStackSnapshot.class),
                new SpongeParticleOption<>(ResourceKey.sponge("offset"), Vector3d.class),
                new SpongeParticleOption<>(ResourceKey.sponge("potion_effect_type"), PotionEffectType.class),
                new SpongeParticleOption<>(ResourceKey.sponge("quantity"), Integer.class,
                        value -> value < 1 ? new IllegalArgumentException("Quantity must be at least one") : null),
                new SpongeParticleOption<>(ResourceKey.sponge("velocity"), Vector3d.class)
        );
    }
}