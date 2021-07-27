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
package org.spongepowered.common.world.generation.config;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.generation.config.NoiseGeneratorConfig;
import org.spongepowered.api.world.generation.config.noise.NoiseConfig;
import org.spongepowered.api.world.generation.config.structure.StructureGenerationConfig;
import org.spongepowered.common.accessor.world.level.levelgen.NoiseGeneratorSettingsAccessor;
import java.util.Objects;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.StructureSettings;

public final class SpongeNoiseGeneratorConfig {

    public static final class BuilderImpl implements NoiseGeneratorConfig.Builder {

        public StructureGenerationConfig structureConfig;
        public NoiseConfig noiseConfig;
        public BlockState defaultBlock, defaultFluid;
        public int bedrockRoofY, bedrockFloorY, seaLevel;

        public BuilderImpl() {
            this.reset();
        }

        @Override
        public NoiseGeneratorConfig.Builder structureConfig(final StructureGenerationConfig config) {
            this.structureConfig = Objects.requireNonNull(config, "config");
            return this;
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
        public NoiseGeneratorConfig.Builder bedrockRoofY(final int y) {
            this.bedrockRoofY = y;
            return this;
        }

        @Override
        public NoiseGeneratorConfig.Builder bedrockFloorY(final int y) {
            this.bedrockFloorY = y;
            return this;
        }

        @Override
        public NoiseGeneratorConfig.Builder seaLevel(final int y) {
            this.seaLevel = y;
            return this;
        }

        @Override
        public NoiseGeneratorConfig.Builder reset() {
            this.structureConfig = (StructureGenerationConfig) new StructureSettings(true);
            this.noiseConfig = NoiseConfig.overworld();
            this.defaultBlock = BlockTypes.STONE.get().defaultState();
            this.defaultFluid = BlockTypes.WATER.get().defaultState();
            this.bedrockRoofY = -10;
            this.bedrockFloorY = 0;
            this.seaLevel = 63;
            return this;
        }

        @Override
        public NoiseGeneratorConfig.Builder from(final NoiseGeneratorConfig value) {
            this.structureConfig = value.structureConfig();
            this.noiseConfig = value.noiseConfig();
            this.defaultBlock = value.defaultBlock();
            this.defaultFluid = value.defaultFluid();
            this.bedrockRoofY = value.bedrockRoofY();
            this.bedrockFloorY = value.bedrockFloorY();
            this.seaLevel = value.seaLevel();
            return this;
        }

        @Override
        public NoiseGeneratorConfig build() {
            final NoiseGeneratorSettings settings = NoiseGeneratorSettingsAccessor.invoker$new((StructureSettings) this.structureConfig,
                    (net.minecraft.world.level.levelgen.NoiseSettings) this.noiseConfig, (net.minecraft.world.level.block.state.BlockState) this.defaultBlock,
                    (net.minecraft.world.level.block.state.BlockState) this.defaultFluid, this.bedrockRoofY, this.bedrockFloorY, this.seaLevel, false);
            return (NoiseGeneratorConfig) (Object) settings;
        }
    }

    public static final class FactoryImpl implements NoiseGeneratorConfig.Factory {

        @Override
        public NoiseGeneratorConfig amplified() {
            return (NoiseGeneratorConfig) (Object) BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(NoiseGeneratorSettings.AMPLIFIED);
        }

        @Override
        public NoiseGeneratorConfig overworld() {
            return (NoiseGeneratorConfig) (Object) BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(NoiseGeneratorSettings.OVERWORLD);
        }

        @Override
        public NoiseGeneratorConfig nether() {
            return (NoiseGeneratorConfig) (Object) BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(NoiseGeneratorSettings.NETHER);
        }

        @Override
        public NoiseGeneratorConfig end() {
            return (NoiseGeneratorConfig) (Object) BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(NoiseGeneratorSettings.END);
        }

        @Override
        public NoiseGeneratorConfig caves() {
            return (NoiseGeneratorConfig) (Object) BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(NoiseGeneratorSettings.CAVES);
        }

        @Override
        public NoiseGeneratorConfig floatingIslands() {
            return (NoiseGeneratorConfig) (Object) BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(NoiseGeneratorSettings.FLOATING_ISLANDS);
        }
    }
}
