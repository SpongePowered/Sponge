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
import net.minecraft.world.WorldType;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.world.DimensionTypeBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.WorldProviderBridge;

@Mixin(net.minecraft.world.dimension.Dimension.class)
public abstract class WorldProviderMixin implements Dimension, WorldProviderBridge {

    @Shadow private WorldType terrainType;
    @Shadow protected World world;
    @Shadow public abstract net.minecraft.world.dimension.DimensionType getDimensionType();
    @Shadow public abstract boolean canRespawnHere();
    @Shadow public abstract int getAverageGroundLevel();
    @Shadow public abstract boolean doesWaterVaporize();
    @Shadow public abstract WorldBorder createWorldBorder();
    @Shadow public abstract boolean isNether();
    @Shadow private String generatorSettings;

    @SuppressWarnings("ConstantConditions")
    @Override
    public DimensionType getType() {
        return (DimensionType) (Object) this.getDimensionType();
    }

    @Override
    public GeneratorType getGeneratorType() {
        return (GeneratorType) this.terrainType;
    }

    @Override
    public boolean allowsPlayerRespawns() {
        return this.canRespawnHere();
    }

    @Override
    public int getMinimumSpawnHeight() {
        return this.getAverageGroundLevel();
    }

    @Override
    public boolean doesWaterEvaporate() {
        return this.doesWaterVaporize();
    }

    @Override
    public boolean hasSky() {
        return !isNether();
    }

    @Override
    public void accessor$setGeneratorSettings(final String generatorSettings) {
        this.generatorSettings = generatorSettings;
    }

    @Override
    public float bridge$getMovementFactor() {
        return 1.0f;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Context getContext() {
        return ((DimensionTypeBridge) (Object) getDimensionType()).bridge$getContext();
    }

    @Override
    public WorldBorder bridge$createServerWorldBorder() {
        return createWorldBorder();
    }

    /**
     * @author Zidane
     * @reason Check configs when dropping a chunk to see if we should keep the spawn loaded
     */
    @Overwrite
    public boolean canDropChunk(final int x, final int z) {
        final boolean isSpawnChunk = this.world.func_72916_c(x, z);

        return !isSpawnChunk || !SpongeImplHooks.shouldKeepSpawnLoaded(this.world.dimension.getType(), ((WorldServerBridge) this.world)
                .bridge$getDimensionId());
    }
}
