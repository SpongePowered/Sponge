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
package org.spongepowered.common.bridge.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.common.bridge.TimingHolder;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.bridge.world.ChunkBridge;
import org.spongepowered.common.bridge.TrackableBridge;

import javax.annotation.Nullable;

public interface TileEntityBridge extends TrackableBridge, TimingHolder {

    /**
     * Gets a {@link NBTTagCompound} that can be used to store custom data for
     * this tile entity. It will be written, and read from disc, so it persists
     * over world saves.
     *
     * @return A compound tag for custom data
     */
    NBTTagCompound getTileData();

    default boolean hasTileDataCompound() {
        return true;
    }

    /**
     * Gets the included {@link NBTTagCompound} for a tile entity. With Vanilla, this is
     * created by vanilla. With Forge, this is included.
     *
     * @return Gets the tag compound containing various tile data
     */
    default NBTTagCompound getSpongeData() {
        NBTTagCompound data = this.getTileData();
        if (!data.hasKey(NbtDataUtil.SPONGE_DATA, NbtDataUtil.TAG_COMPOUND)) {
            data.setTag(NbtDataUtil.SPONGE_DATA, new NBTTagCompound());
        }
        return data.getCompoundTag(NbtDataUtil.SPONGE_DATA);
    }


    void markDirty();

    boolean isVanilla();

    // Tracking
    default SpawnType getTickedSpawnType() {
        return SpawnTypes.BLOCK_SPAWNING;
    }

    void setSpongeOwner(@Nullable User owner);

    void setSpongeNotifier(@Nullable User notifier);

    @Nullable User getSpongeOwner();

    @Nullable User getSpongeNotifier();

    @Nullable
    ChunkBridge getActiveChunk();

    void setActiveChunk(@Nullable ChunkBridge chunk);

    boolean shouldTick();

    boolean isTicking();

    void setIsTicking(boolean ticking);

    boolean isCaptured();

    void setCaptured(boolean captured);
    default String getPrettyPrinterString() {
        return  this.toString();
    }
}
