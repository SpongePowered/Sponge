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
package org.spongepowered.common.world.generation.config.noise;

import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.biome.BiomeAttributes;
import org.spongepowered.api.world.generation.config.SurfaceRule;
import org.spongepowered.api.world.generation.config.noise.NoiseConfig;
import org.spongepowered.api.world.generation.config.noise.NoiseConfigs;
import org.spongepowered.api.world.generation.config.noise.NoiseGeneratorConfig;
import org.spongepowered.api.world.generation.config.noise.NoiseRouter;

import java.util.List;
import java.util.Objects;

public final class SpongeNoiseGeneratorConfigBuilder implements NoiseGeneratorConfig.Builder {

    public NoiseConfig noiseConfig;
    public BlockState defaultBlock, defaultFluid;
    public int seaLevel;
    public boolean aquifers, oreVeins, legacyRandomSource, disableMobGeneration;
    public SurfaceRule surfaceRule;
    private NoiseRouter router;
    private List<BiomeAttributes> spawnTargets;

    public SpongeNoiseGeneratorConfigBuilder() {
        this.reset();
    }

    @Override
    public NoiseGeneratorConfig.Builder noiseConfig(final NoiseConfig config) {
        this.noiseConfig = Objects.requireNonNull(config, "config");
        return this;
    }

    @Override
    public NoiseGeneratorConfig.Builder defaultBlock(final BlockState block) {
        this.defaultBlock = Objects.requireNonNull(block, "block");
        return this;
    }

    @Override
    public NoiseGeneratorConfig.Builder defaultFluid(final BlockState fluid) {
        this.defaultFluid = Objects.requireNonNull(fluid, "fluid");
        return this;
    }

    @Override
    public NoiseGeneratorConfig.Builder surfaceRule(SurfaceRule rule) {
        this.surfaceRule = rule;
        return this;
    }


    @Override
    public NoiseGeneratorConfig.Builder seaLevel(final int y) {
        this.seaLevel = y;
        return this;
    }

    @Override
    public NoiseGeneratorConfig.Builder mobGeneration(boolean mobGeneration) {
        this.disableMobGeneration = !mobGeneration;
        return this;
    }

    @Override
    public NoiseGeneratorConfig.Builder aquifers(final boolean enableAquifers) {
        this.aquifers = enableAquifers;
        return this;
    }

    @Override
    public NoiseGeneratorConfig.Builder oreVeins(final boolean enableOreVeins) {
        this.oreVeins = enableOreVeins;
        return this;
    }

    @Override
    public NoiseGeneratorConfig.Builder randomSource(boolean useLegacyRandomSource) {
        this.legacyRandomSource = useLegacyRandomSource;
        return this;
    }

    @Override
    public NoiseGeneratorConfig.Builder noiseRouter(final NoiseRouter router) {
        this.router = router;
        return this;
    }

    @Override
    public NoiseGeneratorConfig.Builder spawnTargets(final List<BiomeAttributes> spawnTargets) {
        this.spawnTargets = spawnTargets;
        return this;
    }

    @Override
    public NoiseGeneratorConfig.Builder reset() {
        this.noiseConfig = NoiseConfigs.OVERWORLD.get();
        this.defaultBlock = BlockTypes.STONE.get().defaultState();
        this.defaultFluid = BlockTypes.WATER.get().defaultState();
        this.surfaceRule = SurfaceRule.overworld();
        this.seaLevel = 63;
        this.aquifers = false;
        this.oreVeins = false;
        this.legacyRandomSource = false;
        this.router = null;
        this.spawnTargets = (List) new OverworldBiomeBuilder().spawnTarget();
        return this;
    }

    @Override
    public NoiseGeneratorConfig.Builder from(final NoiseGeneratorConfig value) {
        this.noiseConfig = value.noiseConfig();
        this.defaultBlock = value.defaultBlock();
        this.defaultFluid = value.defaultFluid();
        this.surfaceRule = value.surfaceRule();
        this.seaLevel = value.seaLevel();
        this.aquifers = value.aquifers();
        this.oreVeins = value.oreVeins();
        this.legacyRandomSource = value.legacyRandomSource();
        this.router = value.noiseRouter();
        this.spawnTargets = value.spawnTargets();
        return this;
    }

    @Override
    public NoiseGeneratorConfig build() {
        final NoiseGeneratorSettings settings = new NoiseGeneratorSettings(
            (net.minecraft.world.level.levelgen.NoiseSettings) (Object) this.noiseConfig,
            (net.minecraft.world.level.block.state.BlockState) this.defaultBlock,
            (net.minecraft.world.level.block.state.BlockState) this.defaultFluid,
            (net.minecraft.world.level.levelgen.NoiseRouter) (Object) Objects.requireNonNull(this.router, "router"),
            (net.minecraft.world.level.levelgen.SurfaceRules.RuleSource) this.surfaceRule,
            (List) this.spawnTargets,
            this.seaLevel,
            this.disableMobGeneration,
            this.aquifers,
            this.oreVeins,
            this.legacyRandomSource
        );
        return (NoiseGeneratorConfig) (Object) settings;
    }
}
