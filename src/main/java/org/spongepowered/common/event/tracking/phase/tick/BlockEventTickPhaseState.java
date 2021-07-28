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

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.transaction.BlockTransactionReceipt;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.world.level.TrackableBlockEventDataBridge;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.world.BlockChange;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

class BlockEventTickPhaseState extends TickPhaseState<BlockEventTickContext> {

    private final BiConsumer<CauseStackManager.StackFrame, BlockEventTickContext> FRAME_MODIFIER =
            super.getFrameModifier().andThen((frame, context) -> {
                final TrackableBlockEventDataBridge blockEventData = context.getSource(TrackableBlockEventDataBridge.class).orElse(null);
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
        return new BlockEventTickContext(tracker);
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, BlockEventTickContext> getFrameModifier() {
        return this.FRAME_MODIFIER;
    }


    @Override
    public void associateNeighborStateNotifier(
        final BlockEventTickContext context, final @Nullable BlockPos sourcePos, final Block block, final BlockPos notifyPos,
                                               final ServerLevel minecraftWorld, final PlayerTracker.Type notifier) {
        // If we do not have a notifier at this point then there is no need to attempt to retrieve one from the chunk
        context.applyNotifierIfAvailable(user -> {
            final LevelChunkBridge mixinChunk = (LevelChunkBridge) minecraftWorld.getChunkAt(notifyPos);
            mixinChunk.bridge$addTrackedBlockPosition(block, notifyPos, user, PlayerTracker.Type.NOTIFIER);
        });
    }

    @Override
    public void postBlockTransactionApplication(
        final BlockEventTickContext context, final BlockChange blockChange,
        final BlockTransactionReceipt receipt
    ) {
        final Block block = (Block) receipt.originalBlock().state().type();
        final SpongeBlockSnapshot original = (SpongeBlockSnapshot) receipt.originalBlock();
        final BlockPos changedBlockPos = original.getBlockPos();
        original.getServerWorld().ifPresent(worldServer -> {
            final LevelChunkBridge changedMixinChunk = (LevelChunkBridge) worldServer.getChunkAt(changedBlockPos);
            changedMixinChunk.bridge$getBlockCreatorUUID(changedBlockPos)
                .ifPresent(owner -> changedMixinChunk.bridge$addTrackedBlockPosition(block, changedBlockPos, owner, PlayerTracker.Type.CREATOR));
            changedMixinChunk.bridge$getBlockNotifierUUID(changedBlockPos)
                .ifPresent(user -> changedMixinChunk.bridge$addTrackedBlockPosition(block, changedBlockPos, user, PlayerTracker.Type.NOTIFIER));
        });
    }

    @Override
    public void unwind(final BlockEventTickContext context) {
        TrackingUtil.processBlockCaptures(context);
    }

    @Override
    public Supplier<SpawnType> getSpawnTypeForTransaction(
        final BlockEventTickContext context, final Entity entityToSpawn
    ) {
        return SpawnTypes.CUSTOM;
    }

}
