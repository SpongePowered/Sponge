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
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.world.BlockChange;

import java.util.function.BiConsumer;

import javax.annotation.Nullable;

abstract class LocationBasedTickPhaseState<T extends LocationBasedTickContext<T>> extends TickPhaseState<T> {

    private final BiConsumer<CauseStackManager.StackFrame, T> LOCATION_MODIFIER =
        super.getFrameModifier().andThen((frame, context) ->
            context.getSource(LocatableBlock.class)
                .ifPresent(frame::pushCause)
        );

    abstract LocatableBlock getLocatableBlockSourceFromContext(PhaseContext<?> context);

    @Override
    public BiConsumer<CauseStackManager.StackFrame, T> getFrameModifier() {
        return this.LOCATION_MODIFIER;
    }

    @Override
    public void associateNeighborStateNotifier(final T context, @Nullable final BlockPos sourcePos, final Block block, final BlockPos notifyPos,
                                               final ServerWorld minecraftWorld, final PlayerTracker.Type notifier) {
        // If we do not have a notifier at this point then there is no need to attempt to retrieve one from the chunk
        context.applyNotifierIfAvailable(user -> {
            final ChunkBridge mixinChunk = (ChunkBridge) minecraftWorld.func_175726_f(notifyPos);
            mixinChunk.bridge$addTrackedBlockPosition(block, notifyPos, user, PlayerTracker.Type.NOTIFIER);
        });
    }

    @Override
    public void postBlockTransactionApplication(final BlockChange blockChange,
        final Transaction<? extends BlockSnapshot> snapshotTransaction, final T context) {
        // If we do not have a notifier at this point then there is no need to attempt to retrieve one from the chunk
        context.applyNotifierIfAvailable(user -> {
            final SpongeBlockSnapshot original = (SpongeBlockSnapshot) snapshotTransaction.getOriginal();
            final Block block = (Block) original.getState().getType();
            final BlockPos changedBlockPos = original.getBlockPos();
            original.getWorldServer().ifPresent(worldServer -> {
                final ChunkBridge changedMixinChunk = (ChunkBridge) worldServer.func_175726_f(changedBlockPos);
                changedMixinChunk.bridge$addTrackedBlockPosition(block, changedBlockPos, user, PlayerTracker.Type.NOTIFIER);
                // and check for owner, if it's available, only if the block change was placement
                // We don't want to set owners on modify because that would mean the current context owner
                // would be setting itself onto potentially other owner's blocks, which since it is a modification, it's considered a
                // notification for a block change so to speak (this is how you can avoid blocks changing colors and being re-branded for
                // ownership to whoever triggered some redstone devices that caused a color change). Only block placements will be considered
                // to have new owners, which we can gather from the context.
                if (blockChange == BlockChange.PLACE) {
                    context.applyOwnerIfAvailable(owner -> {
                        // We can do this when we check for notifiers because owners will always have a notifier set
                        // if not, well, file a bug report and find out the corner case that owners are set but not notifiers with block changes.
                        changedMixinChunk.bridge$addTrackedBlockPosition(block, changedBlockPos, owner, PlayerTracker.Type.OWNER);
                    });
                }
            });

        });
    }


}
