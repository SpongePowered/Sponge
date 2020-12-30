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
package org.spongepowered.common.mixin.api.mcp.world.gen;

import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.generation.settings.NoiseGeneratorSettings;
import org.spongepowered.api.world.generation.settings.noise.NoiseSettings;
import org.spongepowered.api.world.generation.settings.structure.StructureGenerationSettings;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DimensionSettings.class)
@Implements(@Interface(iface = NoiseGeneratorSettings.class, prefix = "noiseGeneratorSettings$"))
public abstract class DimensionSettingsMixin_API implements NoiseGeneratorSettings {

    // @formatter:off
    @Shadow public abstract DimensionStructuresSettings shadow$structureSettings();
    @Shadow public abstract net.minecraft.world.gen.settings.NoiseSettings shadow$noiseSettings();
    @Shadow public abstract int shadow$getBedrockRoofPosition();
    @Shadow public abstract int shadow$getBedrockFloorPosition();
    @Shadow public abstract int shadow$seaLevel();
    // @formatter:on

    @Intrinsic
    public StructureGenerationSettings noiseGeneratorSettings$structureSettings() {
        return (StructureGenerationSettings) this.shadow$structureSettings();
    }

    @Intrinsic
    public NoiseSettings noiseGeneratorSettings$noiseSettings() {
        return (NoiseSettings) this.shadow$noiseSettings();
    }

    @Intrinsic
    public BlockState noiseGeneratorSettings$defaultBlock() {
        return this.defaultBlock();
    }

    @Intrinsic
    public BlockState noiseGeneratorSettings$defaultFluid() {
        return this.defaultFluid();
    }

    @Override
    public int bedrockRoofY() {
        return this.shadow$getBedrockRoofPosition();
    }

    @Override
    public int bedrockFloorY() {
        return this.shadow$getBedrockFloorPosition();
    }

    @Intrinsic
    public int noiseGeneratorSettings$seaLevel() {
        return this.shadow$seaLevel();
    }
}
