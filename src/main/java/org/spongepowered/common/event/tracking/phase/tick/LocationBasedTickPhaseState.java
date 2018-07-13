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
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;

import java.util.function.BiConsumer;

import javax.annotation.Nullable;

abstract class LocationBasedTickPhaseState<T extends LocationBasedTickContext<T>> extends TickPhaseState<T> {

    private final BiConsumer<CauseStackManager.StackFrame, T> LOCATION_MODIFIER =
        super.getFrameModifier().andThen((frame, context) ->
            context.getSource(LocatableBlock.class)
                .ifPresent(frame::pushCause)
        );

    LocationBasedTickPhaseState() {
    }

    abstract LocatableBlock getLocatableBlockSourceFromContext(PhaseContext<?> context);

    @Override
    public BiConsumer<CauseStackManager.StackFrame, T> getFrameModifier() {
        return this.LOCATION_MODIFIER;
    }

    @Override
    public void associateNeighborStateNotifier(T context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
                                               WorldServer minecraftWorld, PlayerTracker.Type notifier) {
        // If we do not have a notifier at this point then there is no need to attempt to retrieve one from the chunk
        context.getNotifier().ifPresent(user -> {
            final IMixinChunk mixinChunk = (IMixinChunk) minecraftWorld.getChunkFromBlockCoords(notifyPos);
            mixinChunk.addTrackedBlockPosition(block, notifyPos, user, PlayerTracker.Type.NOTIFIER);
        });
    }

    @Override
    public void postBlockTransactionApplication(BlockChange blockChange,
        Transaction<BlockSnapshot> snapshotTransaction, T context) {
        // If we do not have a notifier at this point then there is no need to attempt to retrieve one from the chunk
        final User user = context.getNotifier().orElse(null);
        if (user != null) {
            final Block block = (Block) snapshotTransaction.getOriginal().getState().getType();
            final Location<World> changedLocation = snapshotTransaction.getOriginal().getLocation().get();
            final BlockPos changedBlockPos = VecHelper.toBlockPos(changedLocation);
            final IMixinChunk changedMixinChunk = (IMixinChunk) ((WorldServer) changedLocation.getExtent()).getChunkFromBlockCoords(changedBlockPos);
            changedMixinChunk.addTrackedBlockPosition(block, changedBlockPos, user, PlayerTracker.Type.NOTIFIER);
        }
    }


    @Override
    public boolean canSwitchTo(IPhaseState<?> state) {
        return super.canSwitchTo(state) || state == GenerationPhase.State.CHUNK_LOADING;
    }

}
