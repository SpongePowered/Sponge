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
import net.minecraft.world.level.block.Block;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.transaction.BlockTransactionReceipt;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.bridge.world.level.TrackableBlockEventDataBridge;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.general.ExplosionContext;
import org.spongepowered.common.world.BlockChange;

import java.util.function.BiConsumer;

abstract class LocationBasedTickPhaseState<T extends LocationBasedTickContext<T>> extends TickPhaseState<T> {

    private final BiConsumer<CauseStackManager.StackFrame, T> LOCATION_MODIFIER =
        super.getFrameModifier().andThen((frame, context) ->
            context.getSource(LocatableBlock.class)
                .ifPresent(frame::pushCause)
        );

    LocatableBlock getLocatableBlockSourceFromContext(final PhaseContext<?> context) {
        return context.getSource(LocatableBlock.class)
            .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking over at a location!", context));
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, T> getFrameModifier() {
        return this.LOCATION_MODIFIER;
    }


    @Override
    public void unwind(final T context) {
        TrackingUtil.processBlockCaptures(context);
    }

    @Override
    public void associateNeighborStateNotifier(final T context, final @Nullable BlockPos sourcePos, final Block block, final BlockPos notifyPos,
                                               final ServerLevel minecraftWorld, final PlayerTracker.Type notifier) {
        // If we do not have a notifier at this point then there is no need to attempt to retrieve one from the chunk
        context.applyNotifierIfAvailable(user -> {
            final LevelChunkBridge mixinChunk = (LevelChunkBridge) minecraftWorld.getChunkAt(notifyPos);
            mixinChunk.bridge$addTrackedBlockPosition(block, notifyPos, user, PlayerTracker.Type.NOTIFIER);
        });
    }

    /**
     * Specifically overridden here because some states have defaults and don't check the context.
     * @param context The context
     * @return True if block events are to be tracked by the specific type of entity (default is true)
     */
    @Override
    public boolean doesBlockEventTracking(final T context) {
        return context.allowsBlockEvents();
    }


    @Override
    public void appendNotifierToBlockEvent(
        T context, TrackedWorldBridge mixinWorldServer, BlockPos pos,
        TrackableBlockEventDataBridge blockEvent
    ) {
        final LocatableBlock source = this.getLocatableBlockSourceFromContext(context);
        blockEvent.bridge$setTickingLocatable(source);
    }


    @Override
    public void appendContextPreExplosion(final ExplosionContext explosionContext, final T context) {
        context.applyOwnerIfAvailable(explosionContext::creator);
        context.applyNotifierIfAvailable(explosionContext::notifier);
        final LocatableBlock locatableBlock = this.getLocatableBlockSourceFromContext(context);
        explosionContext.source(locatableBlock);
    }

    @Override
    public void postBlockTransactionApplication(
        final T context, final BlockChange blockChange,
        final BlockTransactionReceipt receipt
    ) {
        // If we do not have a notifier at this point then there is no need to attempt to retrieve one from the chunk
        context.applyNotifierIfAvailable(user -> {
            final SpongeBlockSnapshot original = (SpongeBlockSnapshot) receipt.originalBlock();
            final Block block = (Block) original.state().type();
            final BlockPos changedBlockPos = original.getBlockPos();
            original.getServerWorld().ifPresent(worldServer -> {
                final LevelChunkBridge changedMixinChunk = (LevelChunkBridge) worldServer.getChunkAt(changedBlockPos);
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
                        changedMixinChunk.bridge$addTrackedBlockPosition(block, changedBlockPos, owner, PlayerTracker.Type.CREATOR);
                    });
                }
            });

        });
    }


}
