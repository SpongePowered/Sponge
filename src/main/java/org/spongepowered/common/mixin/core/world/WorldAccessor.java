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

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

import javax.annotation.Nullable;

@Mixin(World.class)
public interface WorldAccessor {

    @Invoker("getPendingTileEntityAt")
    @Nullable
    TileEntity accessPendingTileEntityAt(BlockPos pos);

    @Invoker("isValid") boolean accessor$isValid(BlockPos pos);

    @Accessor("unloadedEntityList") List<Entity> accessor$getUnloadedEntityList();

    /**
     * Tile Entity additions that were deferred because the World was still iterating existing Tile Entities; will be
     * added to the world at the end of the tick.
     */
    @Accessor("addedTileEntityList") List<TileEntity> accessor$getAddedTileEntityList();

    /**
     * Tile Entity removals that were deferred because the World was still iterating existing Tile Entities; will be
     * removed from the world at the end of the tick.
     */
    @Accessor("tileEntitiesToBeRemoved") List<TileEntity> accessor$getTileEntitiesToBeRemoved();

    /**
     * True while the World is ticking {@link #accessor$getTickableTileEntities()}, to prevent CME's if any of those ticks create more
     * tile entities.
     */
    @Accessor("processingLoadedTiles") boolean accessor$getProcessingLoadedTiles();

    @Invoker("isOutsideBuildHeight") boolean accessor$getIsOutsideBuildHeight(BlockPos pos);

}
