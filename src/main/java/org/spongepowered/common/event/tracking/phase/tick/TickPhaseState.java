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
package org.spongepowered.common.event.tracking.phase.tick;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.event.tracking.phase.general.ExplosionContext;
import org.spongepowered.common.bridge.world.ServerWorldBridge;

import java.util.ArrayList;

import javax.annotation.Nullable;

abstract class TickPhaseState<C extends TickContext<C>> implements IPhaseState<C> {

    TickPhaseState() {
    }

    @Override
    public final TrackingPhase getPhase() {
        return TrackingPhases.TICK;
    }

    @Override
    public boolean doesCaptureEntityDrops(C context) {
        return true;
    }

    @Override
    public boolean tracksBlockSpecificDrops(C context) {
        return true;
    }

    @Override
    public boolean isTicking() {
        return true;
    }

    @Override
    public void unwind(C phaseContext) { }

    @Override
    public void associateNeighborStateNotifier(C context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
                                               WorldServer minecraftWorld, PlayerTracker.Type notifier) {

    }

    @Override
    public void appendNotifierPreBlockTick(ServerWorldBridge mixinWorld, BlockPos pos, C context, BlockTickContext phaseContext) {
        if (this == TickPhase.Tick.BLOCK || this == TickPhase.Tick.RANDOM_BLOCK) {

        }
    }

    @Override
    public void postProcessSpawns(C phaseContext, ArrayList<Entity> entities) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            if (!frame.getCurrentContext().get(EventContextKeys.SPAWN_TYPE).isPresent()) {
                frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.BLOCK_SPAWNING);
            }
            SpongeCommonEventFactory.callSpawnEntity(entities, phaseContext);
        }
    }

    @Override
    public void appendContextPreExplosion(ExplosionContext explosionContext, C context) {

    }

    @Override
    public abstract boolean spawnEntityOrCapture(C context, Entity entity, int chunkX, int chunkZ);

    @Override
    public boolean doesDenyChunkRequests() {
        return true;
    }

    private final String className = this.getClass().getSimpleName();

    @Override
    public String toString() {
        return this.getPhase() + "{" + this.className + "}";
    }
}
