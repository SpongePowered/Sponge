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
package org.spongepowered.common.mixin.optimization.world.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.level.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.mixin.optimization.world.level.LevelReaderMixin_Optimization_Collision;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

@Mixin(value = ServerLevel.class, priority = 1500)
public abstract class ServerWorldMixin_Optimization_Collision implements LevelReaderMixin_Optimization_Collision {

    @Override
    public Stream<BlockState> getBlockStatesIfLoaded(final AABB aabb) {
        if (((WorldBridge) this).bridge$isFake()) {
            return LevelReaderMixin_Optimization_Collision.super.getBlockStatesIfLoaded(aabb);
        }
        final Optional<ActiveChunkReferantBridge> source = PhaseTracker.getInstance().getPhaseContext().getSource(Entity.class)
            .map(entity -> (ActiveChunkReferantBridge) entity);
        if (source.isPresent()) {
            final LevelChunkBridge activeChunk = source.get().bridge$getActiveChunk();
            if (activeChunk == null || activeChunk.bridge$isQueuedForUnload() || !activeChunk.bridge$areNeighborsLoaded()) {
                return Stream.empty();
            }
        }
        return LevelReaderMixin_Optimization_Collision.super.getBlockStatesIfLoaded(aabb);
    }
}
