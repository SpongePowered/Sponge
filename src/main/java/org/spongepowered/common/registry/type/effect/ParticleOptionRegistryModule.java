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
package org.spongepowered.common.registry.type.effect;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.effect.particle.ParticleOption;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.effect.particle.SpongeParticleOption;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

public class ParticleOptionRegistryModule implements CatalogRegistryModule<ParticleOption<?>> {

    @RegisterCatalog(ParticleOptions.class)
    private final Map<String, ParticleOption<?>> particleOptionsMappings = new HashMap<>();
    private final Map<String, ParticleOption<?>> particleOptions = new HashMap<>();

    @Override
    public Optional<ParticleOption<?>> getById(String id) {
        return Optional.ofNullable(this.particleOptions.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<ParticleOption<?>> getAll() {
        return ImmutableList.copyOf(this.particleOptions.values());
    }

    @Override
    public void registerDefaults() {
        this.registerOption("block_state", BlockState.class);
        this.registerOption("color", Color.class);
        this.registerOption("direction", Direction.class);
        this.registerOption("firework_effects", List.class,
                value -> value.isEmpty() ? new IllegalArgumentException("The firework effects list may not be empty") : null);
        this.registerOption("quantity", Integer.class,
                value -> value < 1 ? new IllegalArgumentException("Quantity must be at least 1") : null);
        this.registerOption("item_stack_snapshot", ItemStackSnapshot.class);
        this.registerOption("note", NotePitch.class);
        this.registerOption("offset", Vector3d.class);
        this.registerOption("potion_effect_type", PotionEffectType.class);
        this.registerOption("scale", Double.class,
                value -> value < 0 ? new IllegalArgumentException("Scale may not be negative") : null);
        this.registerOption("velocity", Vector3d.class);
        this.registerOption("slow_horizontal_velocity", Boolean.class);
    }

    private <V> void registerOption(String id, Class<V> valueType) {
        this.registerOption(id, valueType, null);
    }

    private <V> void registerOption(String id, Class<V> valueType, @Nullable Function<V, IllegalArgumentException> valueValidator) {
        SpongeParticleOption<?> option = new SpongeParticleOption<>("minecraft:" + id, id, valueType, valueValidator);
        this.particleOptionsMappings.put(id, option);
        this.particleOptions.put(option.getId(), option);
    }
}
