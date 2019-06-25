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
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.bridge.block.BlockEventDataBridge;
import org.spongepowered.common.world.BlockChange;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

class BlockEventTickPhaseState extends TickPhaseState<BlockEventTickContext> {

    private final BiConsumer<CauseStackManager.StackFrame, BlockEventTickContext> FRAME_MODIFIER =
            super.getFrameModifier().andThen((frame, context) -> {
                final BlockEventDataBridge blockEventData = context.getSource(BlockEventDataBridge.class).orElse(null);
                if (blockEventData != null) {
                    if (blockEventData.getBridge$TileEntity() != null) {
                        frame.pushCause(blockEventData.getBridge$TileEntity());
                    } else if (blockEventData.getBridge$TickingLocatable() != null) {
                        frame.pushCause(blockEventData.getBridge$TickingLocatable());
                        frame.addContext(EventContextKeys.BLOCK_EVENT_PROCESS, blockEventData.getBridge$TickingLocatable());
                    }
                }
            });

    BlockEventTickPhaseState() {
    }

    @Override
    public BlockEventTickContext createPhaseContext() {
        return new BlockEventTickContext()
                .addBlockCaptures()
                .addEntityCaptures();
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, BlockEventTickContext> getFrameModifier() {
        return this.FRAME_MODIFIER;
    }


    @Override
    public void associateNeighborStateNotifier(BlockEventTickContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
                                               WorldServer minecraftWorld, PlayerTracker.Type notifier) {
        // If we do not have a notifier at this point then there is no need to attempt to retrieve one from the chunk
        context.applyNotifierIfAvailable(user -> {
            final ChunkBridge mixinChunk = (ChunkBridge) minecraftWorld.getChunk(notifyPos);
            mixinChunk.addTrackedBlockPosition(block, notifyPos, user, PlayerTracker.Type.NOTIFIER);
        });
    }

    @Override
    public boolean spawnEntityOrCapture(BlockEventTickContext context, Entity entity, int chunkX, int chunkZ) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.CUSTOM);

            final List<Entity> entities = new ArrayList<>(1);
            entities.add(entity);
            return SpongeCommonEventFactory.callSpawnEntity(entities, context);
        }
    }

    @Override
    public boolean doesBulkBlockCapture(BlockEventTickContext context) {
        return true;
    }

    @Override
    public boolean doesCaptureEntitySpawns() {
        return false;
    }

    @Override
    public void postBlockTransactionApplication(BlockChange blockChange,
        Transaction<BlockSnapshot> snapshotTransaction, BlockEventTickContext context) {
        final Block block = (Block) snapshotTransaction.getOriginal().getState().getType();
        final SpongeBlockSnapshot original = (SpongeBlockSnapshot) snapshotTransaction.getOriginal();
        final BlockPos changedBlockPos = original.getBlockPos();
        original.getWorldServer().ifPresent(worldServer -> {
            final ChunkBridge changedMixinChunk = (ChunkBridge) worldServer.getChunk(changedBlockPos);
            changedMixinChunk.getBlockOwner(changedBlockPos)
                .ifPresent(owner -> changedMixinChunk.addTrackedBlockPosition(block, changedBlockPos, owner, PlayerTracker.Type.OWNER));
            changedMixinChunk.getBlockNotifier(changedBlockPos)
                .ifPresent(user -> changedMixinChunk.addTrackedBlockPosition(block, changedBlockPos, user, PlayerTracker.Type.NOTIFIER));
        });
    }

    @Override
    public void unwind(BlockEventTickContext context) {
        try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.CUSTOM);
            TrackingUtil.processBlockCaptures(this, context);
            context.getCapturedItemsSupplier()
                    .acceptAndClearIfNotEmpty(items -> {
                        final ArrayList<Entity> capturedEntities = new ArrayList<>();
                        for (EntityItem entity : items) {
                            capturedEntities.add((Entity) entity);
                        }
                        SpongeCommonEventFactory.callSpawnEntity(capturedEntities, context);
                    });
        }
    }

    @Override
    public boolean getShouldCancelAllTransactions(BlockEventTickContext context, List<ChangeBlockEvent> blockEvents, ChangeBlockEvent.Post postEvent,
        ListMultimap<BlockPos, BlockEventData> scheduledEvents, boolean noCancelledTransactions) {
        if (!(context.getSource() instanceof TileEntity)) {
            // we have a LocatableBlock.
            final LocatableBlock source = (LocatableBlock) context.getSource();
            // Basically, if the source was a tile entity, and during the block event, it changed?
            // and if any of the transaction cancelled, the whole thing should be cancelled.
            if (SpongeImplHooks.hasBlockTileEntity(((IBlockState) source.getBlockState()).getBlock(), (IBlockState) source.getBlockState())) {
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
                final boolean hasTile = SpongeImplHooks.hasBlockTileEntity((Block) type, (IBlockState) state);
                if (!hasTile && !transaction.getIntermediary().isEmpty()) { // Check intermediary
                    return transaction.getIntermediary().stream().anyMatch(inter -> {
                        final BlockState iterState = inter.getState();
                        final BlockType interType = state.getType();
                        final boolean interMediaryHasTile = SpongeImplHooks.hasBlockTileEntity((Block) interType, (IBlockState) iterState);
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
    public boolean tracksTileEntityChanges(BlockEventTickContext currentContext) {
        return true;
    }

    @Override
    public boolean hasSpecificBlockProcess(BlockEventTickContext context) {
        return true;
    }

    @Override
    public boolean doesCaptureNeighborNotifications(BlockEventTickContext context) {
        return true;
    }
}
