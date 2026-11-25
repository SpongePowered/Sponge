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
package org.spongepowered.common.event.tracking.phase.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.transaction.BlockTransactionReceipt;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.PooledPhaseState;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.world.BlockChange;

import java.util.function.BiConsumer;

@SuppressWarnings({"unchecked", "rawTypes"})
public class GrowablePhaseState extends PooledPhaseState<GrowablePhaseContext> implements IPhaseState<GrowablePhaseContext> {

    private final BiConsumer<CauseStackManager.StackFrame, GrowablePhaseContext> FRAME_MODIFIER = super.getFrameModifier()
        .andThen((stackFrame, growablePhaseContext) -> {
            if (!growablePhaseContext.usedItem.isEmpty()) {
                stackFrame.addContext(EventContextKeys.USED_ITEM, growablePhaseContext.usedItem);
            }
            stackFrame.addContext(EventContextKeys.GROWTH_ORIGIN, growablePhaseContext.snapshot);

        });

    @Override
    public GrowablePhaseContext createNewContext(final PhaseTracker tracker) {
        return new GrowablePhaseContext(this, tracker);
    }

    @Override
    public void unwind(final GrowablePhaseContext phaseContext) {
        TrackingUtil.processBlockCaptures(phaseContext);
    }

    @Override
    public BlockChange associateBlockChangeWithSnapshot(
        final GrowablePhaseContext phaseContext,
        final BlockState newState, final BlockState currentState
    ) {
        return BlockChange.GROW;
    }

    // Grown blocks belong to whoever the growth is attributed to, as its GROWTH_ORIGIN does: the player using bone
    // meal, the dispenser's creator, or, when it grows by itself, whoever placed the sapling. A block someone else
    // already created stays theirs, with the growth as its notifier, so bone meal can't take another player's crops.
    @Override
    public void postBlockTransactionApplication(
        final GrowablePhaseContext context, final BlockChange blockChange, final BlockTransactionReceipt receipt
    ) {
        context.getCreator().ifPresent(uuid -> {
            final SpongeBlockSnapshot grown = (SpongeBlockSnapshot) receipt.finalBlock();
            final Block block = ((BlockState) grown.state()).getBlock();
            grown.getServerWorld().ifPresent(world -> {
                final LevelChunkBridge chunk = (LevelChunkBridge) world.getChunkAt(grown.getBlockPos());
                final PlayerTracker.Type type = chunk.bridge$getBlockCreatorUUID(grown.getBlockPos()).isPresent()
                    ? PlayerTracker.Type.NOTIFIER : PlayerTracker.Type.CREATOR;
                chunk.bridge$addTrackedBlockPosition(block, grown.getBlockPos(), uuid, type);
            });
        });
    }

    @Override
    public void associateNeighborStateNotifier(
        final GrowablePhaseContext context, final @Nullable BlockPos sourcePos, final Block block, final BlockPos notifyPos,
        final ServerLevel minecraftWorld, final PlayerTracker.Type notifier
    ) {
        context.getNotifier()
            .ifPresent(uuid -> ((LevelChunkBridge) minecraftWorld.getChunkAt(notifyPos)).bridge$setBlockNotifier(notifyPos, uuid));
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, GrowablePhaseContext> getFrameModifier() {
        return this.FRAME_MODIFIER;
    }

    private final String desc = TrackingUtil.phaseStateToString("Growable", this);

    @Override
    public String toString() {
        return this.desc;
    }
}
