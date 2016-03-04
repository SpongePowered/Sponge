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
package org.spongepowered.common.mixin.core.world;

import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkGenerator;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.interfaces.world.IMixinWorldProvider;

@NonnullByDefault
@Mixin(WorldProvider.class)
@Implements(@Interface(iface = IMixinWorldProvider.class, prefix = "mixinworldprovider$"))
public abstract class MixinWorldProvider implements Dimension {

    private boolean allowPlayerRespawns;
    private SpongeConfig<SpongeConfig.DimensionConfig> dimensionConfig;
    private volatile Context dimContext;

    @Shadow private String generatorSettings;
    @Shadow public WorldType terrainType;
    @Shadow protected boolean hasNoSky;
    @Shadow protected World worldObj;
    @Shadow public abstract IChunkGenerator createChunkGenerator();
    @Shadow public abstract net.minecraft.world.DimensionType getDimensionType();
    @Shadow public abstract boolean canRespawnHere();
    @Shadow public abstract boolean doesWaterVaporize();

    @Override
    public String getName() {
        return getDimensionType().getName();
    }

    @Override
    public GeneratorType getGeneratorType() {
        return (GeneratorType) this.terrainType;
    }

    @Override
    public DimensionType getType() {
        return (DimensionType) (Object) getDimensionType();
    }

    public IChunkGenerator mixinworldprovider$createChunkGenerator(String settings) {
        this.generatorSettings = settings;
        return this.createChunkGenerator();
    }

    @Override
    public boolean allowsPlayerRespawns() {
        return this.canRespawnHere();
    }

    @Override
    public int getMinimumSpawnHeight() {
        return this.getAverageGroundLevel();
    }

    @Overwrite
    public int getAverageGroundLevel() {
        int spawnHeight = worldObj.getSeaLevel() + 1;

        if (worldObj.getWorldType() == WorldType.FLAT) {
            spawnHeight = 4;
        } else if (worldObj.getWorldType() == GeneratorTypes.THE_END) {
            spawnHeight = 50;
        }
        return spawnHeight;
    }

    @Override
    public boolean doesWaterEvaporate() {
        return this.doesWaterVaporize();
    }

    @Override
    public boolean hasSky() {
        return !getHasNoSky();
    }

    @Overwrite
    public boolean getHasNoSky() {
        return this.terrainType.equals(GeneratorTypes.NETHER) || hasNoSky;
    }

    public String mixinworldprovider$getSaveFolder() {
        final IMixinWorldProvider this$ = (IMixinWorldProvider) this;
        return (this$.getDimensionId() == 0 ? null : "DIM" + this$.getDimensionId());
    }

    public void mixinworldprovider$setDimensionConfig(SpongeConfig<SpongeConfig.DimensionConfig> config) {
        this.dimensionConfig = config;
    }

    public SpongeConfig<SpongeConfig.DimensionConfig> mixinworldprovider$getDimensionConfig() {
        return this.dimensionConfig;
    }

    @Override
    public Context getContext() {
        if (this.dimContext == null) {
            this.dimContext = new Context(Context.DIMENSION_KEY, getName());
        }
        return this.dimContext;
    }
}
