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
package org.spongepowered.common.event.tracking.phase;

import com.google.common.base.MoreObjects;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import javax.annotation.Nullable;

public abstract class TrackingPhase {

    // Default methods that are basic qualifiers, leaving up to the phase and state to decide
    // whether they perform capturing.

    public boolean requiresBlockCapturing(IPhaseState<?> currentState) {
        return true;
    }

    public boolean alreadyCapturingEntitySpawns(IPhaseState<?> state) {
        return false;
    }

    public boolean alreadyCapturingEntityTicks(IPhaseState<?> state) {
        return false;
    }

    public boolean alreadyCapturingTileTicks(IPhaseState<?> state) {
        return false;
    }

    public boolean alreadyCapturingItemSpawns(IPhaseState<?> currentState) {
        return false;
    }

    public boolean ignoresItemPreMerging(IPhaseState<?> currentState) {
        return false;
    }

    public boolean isWorldGeneration(IPhaseState<?> state) {
        return false;
    }


    public void capturePlayerUsingStackToBreakBlock(@Nullable ItemStack itemStack, EntityPlayerMP playerMP, IPhaseState<?> state, PhaseContext<?> context,
            PhaseTracker phaseTracker) {

    }


    /**
     * Associates any notifiers and owners for tracking as to what caused
     * the next {@link TickPhase.Tick} to enter for a block to be updated.
     * The interesting thing is that since the current state and context
     * are already known, we can associate the notifiers/owners appropriately.
     * This may have the side effect of a long winded "bubble down" from
     * a single lever pull to blocks getting updated hundreds of blocks
     * away.
     *
     * @param mixinWorld
     * @param pos
     * @param currentState
     * @param context
     * @param newContext
     */
    public void appendNotifierPreBlockTick(IMixinWorldServer mixinWorld, BlockPos pos, IPhaseState<?> currentState, PhaseContext<?> context, PhaseContext<?> newContext) {
        final Chunk chunk = mixinWorld.asMinecraftWorld().getChunkFromBlockCoords(pos);
        final IMixinChunk mixinChunk = (IMixinChunk) chunk;
        if (chunk != null && !chunk.isEmpty()) {
            mixinChunk.getBlockOwner(pos).ifPresent(newContext::owner);
            mixinChunk.getBlockNotifier(pos).ifPresent(newContext::notifier);
        }
    }

    // Actual capture methods

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }

    public void addNotifierToBlockEvent(IPhaseState<?> phaseState, PhaseContext<?> context, IMixinWorldServer mixinWorld, BlockPos pos, IMixinBlockEventData blockEvent) {

    }

    public void associateNeighborStateNotifier(IPhaseState<?> state, PhaseContext<?> context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
                                               WorldServer minecraftWorld, PlayerTracker.Type notifier) {

    }

    public void appendContextPreExplosion(PhaseContext<?> phaseContext, PhaseData currentPhaseData) {

    }

}
