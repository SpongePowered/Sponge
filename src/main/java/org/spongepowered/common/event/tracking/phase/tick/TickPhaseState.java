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
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.teleport.TeleportCause;
import org.spongepowered.api.event.cause.entity.teleport.TeleportTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.event.tracking.phase.block.BlockPhaseState;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhaseState;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.ArrayList;

import javax.annotation.Nullable;

abstract class TickPhaseState implements IPhaseState {

    TickPhaseState() {
    }

    @Override
    public final TrackingPhase getPhase() {
        return TrackingPhases.TICK;
    }

    @Override
    public boolean canSwitchTo(IPhaseState state) {
        return state instanceof BlockPhaseState || state instanceof EntityPhaseState || state == GenerationPhase.State.TERRAIN_GENERATION;
    }

    @Override
    public boolean tracksBlockSpecificDrops() {
        return true;
    }

    public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) { }

    public abstract void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker);

    public void associateBlockEventNotifier(PhaseContext context, CauseTracker causeTracker, BlockPos pos, IMixinBlockEventData blockEvent) {

    }

    public void associateNeighborBlockNotifier(PhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
            WorldServer minecraftWorld, PlayerTracker.Type notifier) {

    }

    public Cause generateTeleportCause(PhaseContext context) {
        return Cause.of(NamedCause.source(TeleportCause.builder().type(TeleportTypes.UNKNOWN).build()));
    }

    public void processPostSpawns(CauseTracker causeTracker, PhaseContext phaseContext, ArrayList<Entity> entities) {
        final SpawnEntityEvent
                event =
                SpongeEventFactory.createSpawnEntityEvent(InternalSpawnTypes.UNKNOWN_CAUSE, entities, causeTracker.getWorld());
        SpongeImpl.postEvent(event);
        if (!event.isCancelled()) {
            for (Entity entity : event.getEntities()) {
                causeTracker.getMixinWorld().forceSpawnEntity(entity);
            }
        }
    }

    public void appendExplosionContext(PhaseContext explosionContext, PhaseContext context) {

    }
}
