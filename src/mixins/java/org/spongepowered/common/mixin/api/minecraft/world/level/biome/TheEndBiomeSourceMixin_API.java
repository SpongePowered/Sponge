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
package org.spongepowered.common.mixin.api.minecraft.world.level.biome;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.biome.provider.ConfigurableBiomeProvider;
import org.spongepowered.api.world.biome.provider.EndStyleBiomeConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.server.BootstrapProperties;

import javax.annotation.Nullable;

@Mixin(TheEndBiomeSource.class)
public abstract class TheEndBiomeSourceMixin_API extends BiomeSourceMixin_API implements ConfigurableBiomeProvider<EndStyleBiomeConfig> {

    // @formatter:off
    @Shadow @Final private Biome end;
    @Shadow @Final private Biome highlands;
    @Shadow @Final private Biome midlands;
    @Shadow @Final private Biome islands;
    @Shadow @Final private Biome barrens;
    @Shadow @Final private long seed;
    // @formatter:on

    @Nullable private EndStyleBiomeConfig api$config;

    @Override
    public EndStyleBiomeConfig config() {
        if (this.api$config == null) {

            var biomeRegistry = BootstrapProperties.registries.registryOrThrow(Registry.BIOME_REGISTRY);
            this.api$config = EndStyleBiomeConfig.builder()
                    .endBiome(RegistryTypes.BIOME.referenced((ResourceKey) (Object) biomeRegistry.getKey(this.end)))
                    .highlandsBiome(RegistryTypes.BIOME.referenced((ResourceKey) (Object) biomeRegistry.getKey(this.highlands)))
                    .midlandsBiome(RegistryTypes.BIOME.referenced((ResourceKey) (Object) biomeRegistry.getKey(this.midlands)))
                    .islandsBiome(RegistryTypes.BIOME.referenced((ResourceKey) (Object) biomeRegistry.getKey(this.islands)))
                    .barrensBiome(RegistryTypes.BIOME.referenced((ResourceKey) (Object) biomeRegistry.getKey(this.barrens)))
                    .seed(this.seed).build();
        }
        return this.api$config;
    }
}
