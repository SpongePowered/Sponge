/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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