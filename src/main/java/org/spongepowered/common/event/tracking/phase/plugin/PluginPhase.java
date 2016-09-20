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
package org.spongepowered.common.event.tracking.phase.plugin;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;

import javax.annotation.Nullable;

public final class PluginPhase extends TrackingPhase {

    public static final class State {
        public static final IPhaseState BLOCK_WORKER = new BlockWorkerPhaseState();
        public static final IPhaseState CUSTOM_SPAWN = new PluginPhaseState();
        public static final IPhaseState CUSTOM_EXPLOSION = new CustomExplosionState();

        private State() {
        }
    }

    public static final class Listener {
        public static final IPhaseState PRE_WORLD_TICK_LISTENER = new PreWorldTickListenerState();
        public static final IPhaseState POST_WORLD_TICK_LISTENER = new PostWorldTickListenerState();
        public static final IPhaseState PRE_SERVER_TICK_LISTENER = new PreServerTickListenerState();
        public static final IPhaseState POST_SERVER_TICK_LISTENER = new PostServerTickListenerState();

        private Listener() {
        }

    }

    public static PluginPhase getInstance() {
        return Holder.INSTANCE;
    }

    private PluginPhase() {
    }

    private static final class Holder {
        static final PluginPhase INSTANCE = new PluginPhase();
    }

    @Override
    public void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext phaseContext) {
        ((PluginPhaseState) state).processPostTick(causeTracker, phaseContext);
    }

    @Override
    public void associateAdditionalCauses(IPhaseState state, PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
        if (state instanceof ListenerPhaseState) {
            ((ListenerPhaseState) state).associateAdditionalBlockChangeCauses(context, builder, causeTracker);
        }
    }

    @Override
    public void addNotifierToBlockEvent(IPhaseState phaseState, PhaseContext context, CauseTracker causeTracker, BlockPos pos,
            IMixinBlockEventData blockEvent) {
        if (phaseState instanceof ListenerPhaseState) {
            ((ListenerPhaseState) phaseState).associateBlockEventNotifier(context, causeTracker, pos, blockEvent);
        }
    }


    @Override
    public void associateNeighborStateNotifier(IPhaseState state, PhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
            WorldServer minecraftWorld, PlayerTracker.Type notifier) {
        if (state instanceof ListenerPhaseState) {
            ((ListenerPhaseState) state).associateNeighborBlockNotifier(context, sourcePos, block, notifyPos, minecraftWorld, notifier);
        }
    }

    @Override
    public void capturePlayerUsingStackToBreakBlock(@Nullable ItemStack itemStack, EntityPlayerMP playerMP, IPhaseState state, PhaseContext context,
            CauseTracker causeTracker) {
        if (state instanceof ListenerPhaseState) {
            ((ListenerPhaseState) state).capturePlayerUsingStackToBreakBlocks(context, playerMP, itemStack);
        }
    }

    @Override
    public boolean handlesOwnPhaseCompletion(IPhaseState state) {
        return state == State.BLOCK_WORKER;
    }
}
