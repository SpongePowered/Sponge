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
package org.spongepowered.common.mixin.core.world.level.levelgen;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.level.levelgen.WorldGenSettingsBridge;
import org.spongepowered.common.server.BootstrapProperties;

import java.util.Random;

@Mixin(WorldGenSettings.class)
public abstract class WorldGenSettingsMixin implements WorldGenSettingsBridge {

    // @formatter:off
    @Shadow @Final private long seed;
    @Shadow @Final private boolean generateFeatures;
    @Shadow @Final private boolean generateBonusChest;
    @Shadow @Final private MappedRegistry<LevelStem> dimensions;

    @Shadow public static MappedRegistry<LevelStem> withOverworld(Registry<DimensionType> p_242749_0_,
            MappedRegistry<LevelStem> p_242749_1_, ChunkGenerator p_242749_2_) {
        return null;
    }
    @Shadow public static NoiseBasedChunkGenerator makeDefaultOverworld(
            Registry<Biome> p_242750_0_, Registry<NoiseGeneratorSettings> p_242750_1_, long p_242750_2_) {
        return null;
    }
    // @formatter:on

    @Override
    public WorldGenSettings bridge$copy() {
        return new WorldGenSettings(this.seed, this.generateFeatures, this.generateBonusChest, this.dimensions);
    }

    @Redirect(method = "guardExperimental", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/MappedRegistry;get(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/Object;"))
    private Object impl$useBootstrapDimensionRegistryForGuard(MappedRegistry registry, ResourceKey<LevelStem> registryKey) {
        if (BootstrapProperties.worldGenSettings == null) {
            BootstrapProperties.worldGenSettings = (WorldGenSettings) (Object) this;
        }

        if (BootstrapProperties.worldGenSettings == (Object) this) {
            return registry.get(registryKey);
        }

        return BootstrapProperties.worldGenSettings.dimensions().get(registryKey);
    }

    @Redirect(method = "overworld", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/MappedRegistry;get(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/Object;"))
    private Object impl$useBootstrapDimensionRegistryForGenerator(MappedRegistry registry, ResourceKey<LevelStem> registryKey) {
        if (BootstrapProperties.worldGenSettings == null) {
            BootstrapProperties.worldGenSettings = (WorldGenSettings) (Object) this;
        }
        
        if (BootstrapProperties.worldGenSettings == (Object) this) {
            return registry.get(registryKey);
        }

        return BootstrapProperties.worldGenSettings.dimensions().get(registryKey);
    }

    /**
     * @author zidane - January 3rd, 2021 - Minecraft 1.16.4
     * @reason Cache the default generator settings as early as possible if a defaulted one
     */
    @Overwrite
    public static WorldGenSettings makeDefault(Registry<DimensionType> p_242751_0_, Registry<Biome> p_242751_1_, Registry<NoiseGeneratorSettings> p_242751_2_) {
        long i = (new Random()).nextLong();
        final WorldGenSettings dimensionGeneratorSettings =
                new WorldGenSettings(i, true, false, withOverworld(p_242751_0_, DimensionType.defaultDimensions(p_242751_0_, p_242751_1_,
                        p_242751_2_, i), makeDefaultOverworld(p_242751_1_, p_242751_2_, i)));
        BootstrapProperties.worldGenSettings = dimensionGeneratorSettings;
        return dimensionGeneratorSettings;
    }
}
