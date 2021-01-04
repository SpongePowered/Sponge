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
package org.spongepowered.common.mixin.core.world.gen.settings;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.NoiseChunkGenerator;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.gen.DimensionGeneratorSettingsBridge;
import org.spongepowered.common.server.BootstrapProperties;

import java.util.Objects;
import java.util.Random;

@Mixin(DimensionGeneratorSettings.class)
public abstract class DimensionGeneratorSettingsMixin implements DimensionGeneratorSettingsBridge {

    // @formatter:off
    @Shadow @Final private long seed;
    @Shadow @Final private boolean generateFeatures;
    @Shadow @Final private boolean generateBonusChest;
    @Shadow @Final private SimpleRegistry<Dimension> dimensions;

    @Shadow public static SimpleRegistry<Dimension> withOverworld(Registry<DimensionType> p_242749_0_,
            SimpleRegistry<Dimension> p_242749_1_, ChunkGenerator p_242749_2_) {
        return null;
    }
    @Shadow public static NoiseChunkGenerator makeDefaultOverworld(
            Registry<Biome> p_242750_0_, Registry<DimensionSettings> p_242750_1_, long p_242750_2_) {
        return null;
    }
    // @formatter:on

    @Override
    public DimensionGeneratorSettings bridge$copy() {
        return new DimensionGeneratorSettings(this.seed, this.generateFeatures, this.generateBonusChest, this.dimensions);
    }

    @Redirect(method = "guardExperimental", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/SimpleRegistry;get(Lnet/minecraft/util/RegistryKey;)Ljava/lang/Object;"))
    private Object impl$useBootstrapDimensionRegistryForGuard(SimpleRegistry registry, RegistryKey<Dimension> registryKey) {
        if (BootstrapProperties.dimensionGeneratorSettings == null) {
            BootstrapProperties.dimensionGeneratorSettings = (DimensionGeneratorSettings) (Object) this;
        }

        if (BootstrapProperties.dimensionGeneratorSettings == (Object) this) {
            return registry.get(registryKey);
        }

        return BootstrapProperties.dimensionGeneratorSettings.dimensions().get(registryKey);
    }

    @Redirect(method = "overworld", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/SimpleRegistry;get(Lnet/minecraft/util/RegistryKey;)Ljava/lang/Object;"))
    private Object impl$useBootstrapDimensionRegistryForGenerator(SimpleRegistry registry, RegistryKey<Dimension> registryKey) {
        if (BootstrapProperties.dimensionGeneratorSettings == (Object) this) {
            return registry.get(registryKey);
        }

        return BootstrapProperties.dimensionGeneratorSettings.dimensions().get(registryKey);
    }

    /**
     * @author zidane - January 3rd, 2021 - Minecraft 1.16.4
     * @reason Cache the default generator settings as early as possible if a defaulted one
     */
    @Overwrite
    public static DimensionGeneratorSettings makeDefault(Registry<DimensionType> p_242751_0_, Registry<Biome> p_242751_1_, Registry<DimensionSettings> p_242751_2_) {
        long i = (new Random()).nextLong();
        final DimensionGeneratorSettings dimensionGeneratorSettings =
                new DimensionGeneratorSettings(i, true, false, withOverworld(p_242751_0_, DimensionType.defaultDimensions(p_242751_0_, p_242751_1_,
                        p_242751_2_, i), makeDefaultOverworld(p_242751_1_, p_242751_2_, i)));
        BootstrapProperties.dimensionGeneratorSettings = dimensionGeneratorSettings;
        return dimensionGeneratorSettings;
    }
}
