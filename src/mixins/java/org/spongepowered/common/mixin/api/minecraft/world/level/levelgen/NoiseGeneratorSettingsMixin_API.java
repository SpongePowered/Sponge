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

import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.generation.config.NoiseGeneratorConfig;
import org.spongepowered.api.world.generation.config.SurfaceRule;
import org.spongepowered.api.world.generation.config.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Interface.Remap;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NoiseGeneratorSettings.class)
@Implements(@Interface(iface = NoiseGeneratorConfig.class, prefix = "noiseGeneratorConfig$", remap = Remap.NONE))
public abstract class NoiseGeneratorSettingsMixin_API implements NoiseGeneratorConfig {

    // @formatter:off
    @Shadow public abstract net.minecraft.world.level.levelgen.NoiseSettings shadow$noiseSettings();
    @Shadow public abstract int shadow$seaLevel();
    @Shadow public abstract net.minecraft.world.level.block.state.BlockState shadow$defaultBlock();
    @Shadow public abstract net.minecraft.world.level.block.state.BlockState shadow$defaultFluid();
    @Shadow public abstract boolean shadow$isAquifersEnabled();
    @Shadow public abstract boolean shadow$oreVeinsEnabled();
    @Shadow @Deprecated protected abstract boolean shadow$disableMobGeneration();
    @Shadow public abstract boolean shadow$useLegacyRandomSource();
    @Shadow public abstract SurfaceRules.RuleSource shadow$surfaceRule();
    // @formatter:on

    @Override
    public NoiseConfig noiseConfig() {
        return (NoiseConfig) (Object) this.shadow$noiseSettings();
    }

    @Override
    public BlockState defaultBlock() {
        return (BlockState) this.shadow$defaultBlock();
    }

    @Override
    public BlockState defaultFluid() {
        return (BlockState) this.shadow$defaultFluid();
    }

    @Intrinsic
    public int noiseGeneratorConfig$seaLevel() {
        return this.shadow$seaLevel();
    }

    @Override
    public boolean aquifers() {
        return this.shadow$isAquifersEnabled();
    }

    @Override
    public boolean oreVeins() {
        return this.shadow$oreVeinsEnabled();
    }

    @Override
    public boolean legacyRandomSource() {
        return this.shadow$useLegacyRandomSource();
    }

    @Override
    public boolean mobGeneration() {
        return !this.shadow$disableMobGeneration();
    }

    @Intrinsic
    public SurfaceRule surfaceRule() {
        return (SurfaceRule) this.shadow$surfaceRule();
    }
}
