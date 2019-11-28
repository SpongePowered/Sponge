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
package org.spongepowered.common.world.gen;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.world.World;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.common.bridge.world.gen.ChunkGeneratorOverworldBridge;
import org.spongepowered.common.mixin.core.world.WorldProviderAccessor;
import org.spongepowered.common.world.biome.SpongeBiomeGenerationSettings;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link WorldGenerator}.
 */
public final class SpongeWorldGenerator implements WorldGenerator {

    private final World world;
    /**
     * Holds the populators. May be mutable or immutable, but must be changed to
     * be mutable before the first call to {@link #getPopulators()}.
     */
    private List<Populator> populators;
    /**
     * Holds the generator populators. May be mutable or immutable, but must be
     * changed to be mutable before the first call to
     * {@link #getGenerationPopulators()}.
     */
    private List<GenerationPopulator> generationPopulators;
    private Map<BiomeType, BiomeGenerationSettings> biomeSettings;
    private BiomeGenerator biomeGenerator;
    private GenerationPopulator baseGenerator;

    public SpongeWorldGenerator(final World world, final BiomeGenerator biomeGenerator, final GenerationPopulator baseGenerator) {
        this.world = checkNotNull(world);
        this.biomeGenerator = checkNotNull(biomeGenerator);
        this.baseGenerator = checkNotNull(baseGenerator);
        this.populators = Lists.newArrayList();
        this.generationPopulators = Lists.newArrayList();
        this.biomeSettings = Maps.newHashMap();
        ((WorldProviderAccessor) this.world.field_73011_w).accessor$setBiomeProvider(CustomBiomeProvider.of(biomeGenerator));
        if (this.baseGenerator instanceof ChunkGeneratorOverworldBridge) {
            ((ChunkGeneratorOverworldBridge) this.baseGenerator).bridge$setBiomeGenerator(biomeGenerator);
        }
    }

    @Override
    public List<GenerationPopulator> getGenerationPopulators() {
        return this.generationPopulators;
    }

    @Override
    public List<GenerationPopulator> getGenerationPopulators(final Class<? extends GenerationPopulator> type) {
        return this.generationPopulators.stream().filter((p) -> type.isAssignableFrom(p.getClass())).collect(Collectors.toList());
    }

    @Override
    public List<Populator> getPopulators() {
        return this.populators;
    }

    @Override
    public List<Populator> getPopulators(final Class<? extends Populator> type) {
        return this.populators.stream().filter((p) -> type.isAssignableFrom(p.getClass())).collect(Collectors.toList());
    }

    @Override
    public BiomeGenerator getBiomeGenerator() {
        return this.biomeGenerator;
    }

    @Override
    public void setBiomeGenerator(final BiomeGenerator biomeGenerator) {
        this.biomeGenerator = checkNotNull(biomeGenerator, "biomeGenerator");
        // Replace biome generator with possible modified one
        ((WorldProviderAccessor) this.world.field_73011_w).accessor$setBiomeProvider(CustomBiomeProvider.of(biomeGenerator));
        if (this.baseGenerator instanceof ChunkGeneratorOverworldBridge) {
            ((ChunkGeneratorOverworldBridge) this.baseGenerator).bridge$setBiomeGenerator(biomeGenerator);
        }
    }

    @Override
    public GenerationPopulator getBaseGenerationPopulator() {
        return this.baseGenerator;
    }

    @Override
    public void setBaseGenerationPopulator(final GenerationPopulator generator) {
        this.baseGenerator = checkNotNull(generator, "generator");
        if (this.baseGenerator instanceof ChunkGeneratorOverworldBridge) {
            ((ChunkGeneratorOverworldBridge) this.baseGenerator).bridge$setBiomeGenerator(this.biomeGenerator);
        }
    }

    @Override
    public BiomeGenerationSettings getBiomeSettings(final BiomeType type) {
        checkNotNull(type);
        BiomeGenerationSettings settings = this.biomeSettings.get(type);
        if (settings == null) {
            if (SpongeGenerationPopulator.class.isInstance(this.baseGenerator)) {
                // If the base generator was mod provided then we assume that it will handle its own
                // generation so we don't add the base game's generation
                settings = new SpongeBiomeGenerationSettings();
            } else {
                settings = type.createDefaultGenerationSettings((org.spongepowered.api.world.World) this.world);
            }
            this.biomeSettings.put(type, settings);
        }
        return settings;
    }

    public Map<BiomeType, BiomeGenerationSettings> getBiomeSettings() {
        return ImmutableMap.copyOf(this.biomeSettings);
    }
}
