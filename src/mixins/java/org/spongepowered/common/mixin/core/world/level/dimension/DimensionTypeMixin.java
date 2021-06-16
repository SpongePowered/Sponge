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
package org.spongepowered.common.mixin.core.world.level.dimension;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeZoomer;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.api.world.biome.BiomeSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.level.dimension.DimensionTypeBridge;
import org.spongepowered.common.registry.provider.BiomeSamplerProvider;
import org.spongepowered.common.world.server.SpongeWorldTypeTemplate;

import java.io.File;

@Mixin(DimensionType.class)
public abstract class DimensionTypeMixin implements DimensionTypeBridge {

    // @formatter:off

    @Shadow @Final @Mutable private boolean createDragonFight;
    @Shadow @Final @Mutable private BiomeZoomer biomeZoomer;

    // @formatter:on

    /**
     * @author zidane - - January 1st, 2021 - Minecraft 1.16.4
     * @reason Compensate for our per-world level save adapters
     */
    @Overwrite
    public static File getStorageFolder(ResourceKey<Level> worldKey, File defaultLevelDirectory) {
        // Sponge Start - The directory is already set to be at this location
        return defaultLevelDirectory;
    }

    @Override
    public DimensionType bridge$decorateData(final SpongeWorldTypeTemplate.SpongeDataSection data) {
        this.createDragonFight = data.createDragonFight;
        this.biomeZoomer = (BiomeZoomer) BiomeSamplerProvider.INSTANCE.get((org.spongepowered.api.ResourceKey) (Object) data.biomeSampler);

        return (DimensionType) (Object) this;
    }

    @Override
    public SpongeWorldTypeTemplate.SpongeDataSection bridge$createData() {
        return new SpongeWorldTypeTemplate.SpongeDataSection(
            (ResourceLocation) (Object) BiomeSamplerProvider.INSTANCE.get((BiomeSampler) this.biomeZoomer),
            this.createDragonFight);
    }
}
