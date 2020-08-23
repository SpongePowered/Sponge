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

import com.google.common.collect.ListMultimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.block.BlockEventDataBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.world.BlockChange;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

class BlockEventTickPhaseState extends TickPhaseState<BlockEventTickContext> {

    private final BiConsumer<CauseStackManager.StackFrame, BlockEventTickContext> FRAME_MODIFIER =
            super.getFrameModifier().andThen((frame, context) -> {
                final BlockEventDataBridge blockEventData = context.getSource(BlockEventDataBridge.class).orElse(null);
                if (blockEventData != null) {
                    if (blockEventData.bridge$getTileEntity() != null) {
                        frame.pushCause(blockEventData.bridge$getTileEntity());
                    } else if (blockEventData.bridge$getTickingLocatable() != null) {
                        frame.pushCause(blockEventData.bridge$getTickingLocatable());
                        frame.addContext(EventContextKeys.BLOCK_EVENT_PROCESS, blockEventData.bridge$getTickingLocatable());
                    }
                }
            });

    @Override
    public BlockEventTickContext createNewContext(final PhaseTracker tracker) {
        return new BlockEventTickContext(tracker)
                .addBlockCaptures()
                .addEntityCaptures();
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, BlockEventTickContext> getFrameModifier() {
        return this.FRAME_MODIFIER;
    }


    @Override
    public void associateNeighborStateNotifier(
        final BlockEventTickContext context, @Nullable final BlockPos sourcePos, final Block block, final BlockPos notifyPos,
                                               final ServerWorld minecraftWorld, final PlayerTracker.Type notifier) {
        // If we do not have a notifier at this point then there is no need to attempt to retrieve one from the chunk
        context.applyNotifierIfAvailable(user -> {
            final ChunkBridge mixinChunk = (ChunkBridge) minecraftWorld.getChunkAt(notifyPos);
            mixinChunk.bridge$addTrackedBlockPosition(block, notifyPos, user, PlayerTracker.Type.NOTIFIER);
        });
    }

    @Override
    public boolean spawnEntityOrCapture(final BlockEventTickContext context, final Entity entity) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.CUSTOM);

            final List<Entity> entities = new ArrayList<>(1);
            entities.add(entity);
            return SpongeCommonEventFactory.callSpawnEntity(entities, context);
        }
    }

    @Override
    public void postBlockTransactionApplication(final BlockChange blockChange,
        final Transaction<? extends BlockSnapshot> snapshotTransaction, final BlockEventTickContext context) {
        final Block block = (Block) snapshotTransaction.getOriginal().getState().getType();
        final SpongeBlockSnapshot original = (SpongeBlockSnapshot) snapshotTransaction.getOriginal();
        final BlockPos changedBlockPos = original.getBlockPos();
        original.getServerWorld().ifPresent(worldServer -> {
            final ChunkBridge changedMixinChunk = (ChunkBridge) worldServer.getChunkAt(changedBlockPos);
            changedMixinChunk.bridge$getBlockCreator(changedBlockPos)
                .ifPresent(owner -> changedMixinChunk.bridge$addTrackedBlockPosition(block, changedBlockPos, owner, PlayerTracker.Type.CREATOR));
            changedMixinChunk.bridge$getBlockNotifier(changedBlockPos)
                .ifPresent(user -> changedMixinChunk.bridge$addTrackedBlockPosition(block, changedBlockPos, user, PlayerTracker.Type.NOTIFIER));
        });
    }

    @Override
    public void unwind(final BlockEventTickContext context) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.CUSTOM);
            TrackingUtil.processBlockCaptures(context);
        }
    }

    @Override
    public boolean getShouldCancelAllTransactions(
        final BlockEventTickContext context, final List<ChangeBlockEvent> blockEvents, final ChangeBlockEvent.Post postEvent,
        final ListMultimap<BlockPos, BlockEventData> scheduledEvents, final boolean noCancelledTransactions) {
        if (!(context.getSource() instanceof TileEntity)) {
            // we have a LocatableBlock.
            final LocatableBlock source = (LocatableBlock) context.getSource();
            // Basically, if the source was a tile entity, and during the block event, it changed?
            // and if any of the transaction cancelled, the whole thing should be cancelled.
            if (SpongeImplHooks.hasBlockTileEntity((net.minecraft.block.BlockState) source.getBlockState())) {
                context.setWasNotCancelled(noCancelledTransactions);
                return !noCancelledTransactions;
            }
            context.setWasNotCancelled(noCancelledTransactions);
            return !noCancelledTransactions;
        }
        if (!postEvent.getTransactions().isEmpty()) {
            return postEvent.getTransactions().stream().anyMatch(transaction -> {
                final BlockState state = transaction.getOriginal().getState();
                final BlockType type = state.getType();
                final boolean hasTile = SpongeImplHooks.hasBlockTileEntity((net.minecraft.block.BlockState) state);
                if (!hasTile && !transaction.getIntermediary().isEmpty()) { // Check intermediary
                    return transaction.getIntermediary().stream().anyMatch(inter -> {
                        final BlockState iterState = inter.getState();
                        final BlockType interType = state.getType();
                        final boolean interMediaryHasTile = SpongeImplHooks.hasBlockTileEntity((net.minecraft.block.BlockState) iterState);
                        context.setWasNotCancelled(!interMediaryHasTile);
                        return interMediaryHasTile;
                    });
                }
                context.setWasNotCancelled(!hasTile);
                return hasTile;
            });
        }
        context.setWasNotCancelled(noCancelledTransactions);
        return !noCancelledTransactions;
    }

    @Override
    public boolean doesCaptureNeighborNotifications(final BlockEventTickContext context) {
        return true;
    }
}
