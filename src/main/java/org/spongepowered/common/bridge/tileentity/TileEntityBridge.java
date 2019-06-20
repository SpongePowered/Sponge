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

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.common.bridge.TimingBridge;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.bridge.world.ChunkBridge;

import javax.annotation.Nullable;

public interface TileEntityBridge extends TrackableBridge {

    void bridge$markDirty();

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
