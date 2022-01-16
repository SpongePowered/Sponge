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
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.biome.provider.CheckerboardBiomeConfig;
import org.spongepowered.api.world.biome.provider.ConfigurableBiomeProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.server.BootstrapProperties;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Mixin(CheckerboardColumnBiomeSource.class)
public abstract class CheckerboardColumnBiomeSourceMixin_API extends BiomeSourceMixin_API implements ConfigurableBiomeProvider<CheckerboardBiomeConfig> {

    // @formatter:off
    @Shadow @Final private List<Supplier<Biome>> allowedBiomes;
    // @formatter:on

    @Nullable private CheckerboardBiomeConfig api$config;

    @Override
    public CheckerboardBiomeConfig config() {
        if (this.api$config == null) {

            var biomeRegistry = BootstrapProperties.registries.registryOrThrow(Registry.BIOME_REGISTRY);
            var biomes = this.allowedBiomes.stream().map(Supplier::get)
                    .map(biome -> RegistryTypes.BIOME.referenced((ResourceKey) (Object) biomeRegistry.getKey(biome)))
                    .collect(Collectors.toList());
            this.api$config = CheckerboardBiomeConfig.builder()
                    .addBiomes(biomes)
                    .build();
        }
        return this.api$config;
    }
}
