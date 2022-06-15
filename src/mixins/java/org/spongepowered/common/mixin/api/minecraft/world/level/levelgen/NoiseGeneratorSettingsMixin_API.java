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
package org.spongepowered.common.mixin.api.minecraft.world.level.levelgen;

import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.biome.BiomeAttributes;
import org.spongepowered.api.world.generation.config.SurfaceRule;
import org.spongepowered.api.world.generation.config.noise.NoiseConfig;
import org.spongepowered.api.world.generation.config.noise.NoiseGeneratorConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Interface.Remap;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(NoiseGeneratorSettings.class)
@Implements(@Interface(iface = NoiseGeneratorConfig.class, prefix = "noiseGeneratorConfig$", remap = Remap.NONE))
public abstract class NoiseGeneratorSettingsMixin_API implements NoiseGeneratorConfig {

    // @formatter:off
    @Shadow @Final private NoiseSettings noiseSettings;
    @Shadow @Final private net.minecraft.world.level.block.state.BlockState defaultBlock;
    @Shadow @Final private net.minecraft.world.level.block.state.BlockState defaultFluid;
    @Shadow @Final private NoiseRouter noiseRouter;
    @Shadow @Final private SurfaceRules.RuleSource surfaceRule;
    @Shadow @Final private List<Climate.ParameterPoint> spawnTarget;
    @Shadow @Final private int seaLevel;
    @Shadow @Final private boolean disableMobGeneration;
    @Shadow @Final private boolean aquifersEnabled;
    @Shadow @Final private boolean oreVeinsEnabled;
    @Shadow @Final private boolean useLegacyRandomSource;
    // @formatter:on

    @Override
    public NoiseConfig noiseConfig() {
        return (NoiseConfig) (Object) this.noiseSettings;
    }

    @Intrinsic
    public org.spongepowered.api.world.generation.config.noise.NoiseRouter noiseGeneratorConfig$noiseRouter() {
        return (org.spongepowered.api.world.generation.config.noise.NoiseRouter) (Object) this.noiseRouter;
    }

    @Override
    public List<BiomeAttributes> spawnTargets() {
        return (List) this.spawnTarget;
    }

    @Intrinsic
    public BlockState noiseGeneratorConfig$defaultBlock() {
        return (BlockState) this.defaultBlock;
    }

    @Intrinsic
    public BlockState noiseGeneratorConfig$defaultFluid() {
        return (BlockState) this.defaultFluid;
    }

    @Intrinsic
    public int noiseGeneratorConfig$seaLevel() {
        return this.seaLevel;
    }

    @Override
    public boolean aquifers() {
        return this.aquifersEnabled;
    }

    @Override
    public boolean oreVeins() {
        return this.oreVeinsEnabled;
    }

    @Override
    public boolean legacyRandomSource() {
        return this.useLegacyRandomSource;
    }

    @Override
    public boolean mobGeneration() {
        return !this.disableMobGeneration;
    }

    @Intrinsic
    public SurfaceRule surfaceRule() {
        return (SurfaceRule) this.surfaceRule;
    }
}
