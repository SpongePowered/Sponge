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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.generation.structure.Structure;
import org.spongepowered.api.world.generation.config.structure.SeparatedStructureConfig;
import org.spongepowered.api.world.generation.config.structure.SpacedStructureConfig;
import org.spongepowered.api.world.generation.config.structure.StructureGenerationConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.configurations.StrongholdConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

public final class SpongeStructureGenerationConfig {

    public static final class BuilderImpl implements StructureGenerationConfig.Builder {
        @Nullable public SpacedStructureConfig stronghold;
        public final Map<Structure, SeparatedStructureConfig> structures = new Object2ObjectOpenHashMap<>();

        public BuilderImpl() {
            this.reset();
        }

        @Override
        public StructureGenerationConfig.Builder stronghold(final @Nullable SpacedStructureConfig config) {
            this.stronghold = config;
            return this;
        }

        @Override
        public StructureGenerationConfig.Builder addStructure(final Structure structure, final SeparatedStructureConfig config) {
            this.structures.put(Objects.requireNonNull(structure, "structure"), Objects.requireNonNull(config, "config"));
            return this;
        }

        @Override
        public StructureGenerationConfig.Builder addStructures(final Map<Structure, SeparatedStructureConfig> structures) {
            this.structures.putAll(Objects.requireNonNull(structures, "structures"));
            return this;
        }

        @Override
        public StructureGenerationConfig.Builder removeStructure(final Structure structure) {
            this.structures.remove(Objects.requireNonNull(structure, "structure"));
            return this;
        }

        @Override
        public StructureGenerationConfig.Builder reset() {
            this.stronghold = null;
            this.structures.clear();
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
            return (StructureGenerationConfig) new StructureSettings(Optional.ofNullable((StrongholdConfiguration) this.stronghold),
                    (Map<net.minecraft.world.level.levelgen.feature.StructureFeature<?>, StructureFeatureConfiguration>) (Object) this.structures);
        }
    }

    public static final class FactoryImpl implements StructureGenerationConfig.Factory {

        @Override
        public StructureGenerationConfig standard() {
            return (StructureGenerationConfig) new StructureSettings(true);
        }

        @Override
        public StructureGenerationConfig flat() {
            return (StructureGenerationConfig) new StructureSettings(Optional.of(StructureSettings.DEFAULT_STRONGHOLD), Maps.newHashMap(
                    ImmutableMap.of(StructureFeature.VILLAGE, StructureSettings.DEFAULTS.get(StructureFeature.VILLAGE))));
        }

        @Override
        public StructureGenerationConfig none() {
            return (StructureGenerationConfig) new StructureSettings(Optional.empty(), new HashMap<>());
        }
    }
}
