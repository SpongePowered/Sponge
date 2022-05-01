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

import static net.minecraft.world.level.levelgen.WorldGenSettings.makeDefaultOverworld;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
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
    // @formatter:on

    @Override
    public WorldGenSettings bridge$copy() {
        return new WorldGenSettings(this.seed, this.generateFeatures, this.generateBonusChest, this.dimensions);
    }

    @Redirect(method = "guardExperimental", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;get(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/Object;"))
    private Object impl$useBootstrapDimensionRegistryForGuard(Registry<LevelStem> registry, ResourceKey<LevelStem> registryKey) {
        if (BootstrapProperties.worldGenSettings == null) {
            BootstrapProperties.worldGenSettings = (WorldGenSettings) (Object) this;
        }

        if (BootstrapProperties.worldGenSettings == (Object) this) {
            return registry.get(registryKey);
        }

        return BootstrapProperties.worldGenSettings.dimensions().get(registryKey);
    }

    @Redirect(method = "overworld", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;get(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/Object;"))
    private Object impl$useBootstrapDimensionRegistryForGenerator(Registry<LevelStem> registry, ResourceKey<LevelStem> registryKey) {
        if (BootstrapProperties.worldGenSettings == null) {
            BootstrapProperties.worldGenSettings = (WorldGenSettings) (Object) this;
        }
        
        if (BootstrapProperties.worldGenSettings == (Object) this) {
            return registry.get(registryKey);
        }

        return BootstrapProperties.worldGenSettings.dimensions().get(registryKey);
    }

    /**
     * @author zidane
     * @reason Cache the default generator settings as early as possible if a defaulted one
     */
    @Overwrite
    public static WorldGenSettings makeDefault(RegistryAccess $$0) {
        long $$1 = (new Random()).nextLong();
        // Sponge Start - Cache the world gen settings as early as possible
        final WorldGenSettings worldGenSettings = new WorldGenSettings($$1, true, false,
                WorldGenSettings.withOverworld($$0.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY), DimensionType.defaultDimensions($$0, $$1),
                        makeDefaultOverworld($$0, $$1)));
        BootstrapProperties.worldGenSettings = worldGenSettings;
        // Sponge End
        return worldGenSettings;
    }
}
