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
package org.spongepowered.common.world.generation.config.structure;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.gen.settings.StructureSpreadSettings;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.generation.Structure;
import org.spongepowered.api.world.generation.config.structure.SeparatedStructureConfig;
import org.spongepowered.api.world.generation.config.structure.SpacedStructureConfig;
import org.spongepowered.api.world.generation.config.structure.StructureGenerationConfig;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class SpongeStructureGenerationConfigBuilder implements StructureGenerationConfig.Builder {

    @Nullable public SpacedStructureConfig stronghold;
    public final Map<RegistryReference<Structure>, SeparatedStructureConfig> structures = new Object2ObjectOpenHashMap<>();

    public SpongeStructureGenerationConfigBuilder() {
        this.reset();
    }

    @Override
    public StructureGenerationConfig.Builder stronghold(@Nullable SpacedStructureConfig config) {
        this.stronghold = config;
        return this;
    }

    @Override
    public StructureGenerationConfig.Builder addStructure(final RegistryReference<Structure> structure, final SeparatedStructureConfig config) {
        this.structures.put(Objects.requireNonNull(structure, "structure"), Objects.requireNonNull(config, "config"));
        return this;
    }

    @Override
    public StructureGenerationConfig.Builder addStructures(final Map<RegistryReference<Structure>, SeparatedStructureConfig> structures) {
        this.structures.putAll(Objects.requireNonNull(structures, "structures"));
        return this;
    }

    @Override
    public StructureGenerationConfig.Builder removeStructure(final RegistryReference<Structure> structure) {
        this.structures.remove(Objects.requireNonNull(structure, "structure"));
        return this;
    }

    @Override
    public StructureGenerationConfig.Builder reset() {
        this.stronghold = (SpacedStructureConfig) DimensionStructuresSettings.DEFAULT_STRONGHOLD;
        this.structures.clear();
        for (final Map.Entry<net.minecraft.world.gen.feature.structure.Structure<?>, StructureSeparationSettings> entry : DimensionStructuresSettings.DEFAULTS.entrySet()) {
            final net.minecraft.world.gen.feature.structure.Structure<?> structure = entry.getKey();
            final StructureSeparationSettings settings = entry.getValue();
            this.structures.put(RegistryKey.of(RegistryTypes.STRUCTURE, ResourceKey.sponge(structure.getFeatureName())).asReference(),
                    (SeparatedStructureConfig) settings);
        }
        return this;
    }

    @Override
    public StructureGenerationConfig.Builder from(final StructureGenerationConfig value) {
        this.stronghold = value.stronghold().orElse(null);
        this.structures.clear();
        this.structures.putAll(value.structures());
        return this;
    }

    @Override
    public @NonNull StructureGenerationConfig build() {
        return (StructureGenerationConfig) new DimensionStructuresSettings(Optional.ofNullable((StructureSpreadSettings) this.stronghold),
                (Map<net.minecraft.world.gen.feature.structure.Structure<?>, StructureSeparationSettings>) (Object) this.structures);
    }
}
