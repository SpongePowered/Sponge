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

import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.WorldAccessor;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.dimension.DimensionTypeBridge;
import org.spongepowered.common.world.dimension.SpongeDimensionType;

import java.util.Random;

@Mixin(net.minecraft.world.World.class)
public abstract class WorldMixin implements WorldBridge, IWorld {

    // @formatter: off
    @Shadow @Final public boolean isRemote;
    @Shadow @Final public Dimension dimension;
    @Shadow @Final public Random rand;
    @Shadow @Final protected WorldInfo worldInfo;
    @Shadow @Final protected AbstractChunkProvider chunkProvider;

    @Shadow public abstract Dimension shadow$getDimension();
    @Shadow public abstract WorldInfo shadow$getWorldInfo();
    @Shadow public abstract void shadow$calculateInitialSkylight();

    @Shadow public abstract boolean shadow$isThundering();

    @Shadow public abstract boolean shadow$isRaining();

    // @formatter on
    private boolean impl$isDefinitelyFake = false;
    private boolean impl$hasChecked = false;

    @Override
    public boolean bridge$isFake() {
        if (this.impl$hasChecked) {
            return this.impl$isDefinitelyFake;
        }
        this.impl$isDefinitelyFake = this.isRemote || this.shadow$getWorldInfo() == null || this.shadow$getWorldInfo().getWorldName() == null || !(this instanceof TrackedWorldBridge);
        this.impl$hasChecked = true;
        return this.impl$isDefinitelyFake;
    }

    @Override
    public void bridge$clearFakeCheck() {
        this.impl$hasChecked = false;
    }

    @Override
    public void bridge$adjustDimensionLogic(final SpongeDimensionType dimensionType) {
        ((DimensionTypeBridge) this.dimension.getType()).bridge$setSpongeDimensionType(dimensionType);
        ((WorldAccessor) this).accessor$setDimension(this.dimension.getType().create((World) (Object) this));
    }

}
