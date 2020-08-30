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
import net.minecraft.block.Blocks;
import net.minecraft.block.IGrowable;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.general.ExplosionContext;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;

import java.util.List;
import java.util.function.BiConsumer;

class BlockTickPhaseState extends LocationBasedTickPhaseState<BlockTickContext> {
    private final BiConsumer<CauseStackManager.StackFrame, BlockTickContext> LOCATION_MODIFIER =
        super.getFrameModifier().andThen((frame, context) ->
            {
                frame.pushCause(this.getLocatableBlockSourceFromContext(context));
                context.tickingBlock.bridge$getTickFrameModifier().accept(frame, (ServerWorldBridge) context.world);
            }
        );
    private final String desc;

    BlockTickPhaseState(final String name) {
        this.desc = TrackingUtil.phaseStateToString("Tick", name, this);
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, BlockTickContext> getFrameModifier() {
        return this.LOCATION_MODIFIER;
    }

    @Override
    public BlockTickContext createNewContext(final PhaseTracker tracker) {
        return new BlockTickContext(this, tracker)
                .addCaptures();
    }


    @Override
    public boolean shouldProvideModifiers(final BlockTickContext phaseContext) {
        return phaseContext.providesModifier;
    }

    @Override
    public boolean getShouldCancelAllTransactions(final BlockTickContext context, final List<ChangeBlockEvent> blockEvents, final ChangeBlockEvent.Post postEvent,
                                                  final ListMultimap<BlockPos, BlockEventData> scheduledEvents, final boolean noCancelledTransactions) {
        if (!postEvent.getTransactions().isEmpty()) {
            return postEvent.getTransactions().stream().anyMatch(transaction -> {
                final BlockState state = transaction.getOriginal().getState();
                final BlockType type = state.getType();
                final boolean hasTile = SpongeImplHooks.hasBlockTileEntity((net.minecraft.block.BlockState) state);
                final BlockPos pos = VecHelper.toBlockPos(context.getSource(LocatableBlock.class).get().getBlockPosition());
                final BlockPos blockPos = ((SpongeBlockSnapshot) transaction.getOriginal()).getBlockPos();
                if (pos.equals(blockPos) && !transaction.isValid()) {
                    return true;
                }
                if (!hasTile && !transaction.getIntermediary().isEmpty()) { // Check intermediary
                    return transaction.getIntermediary().stream().anyMatch(inter -> {
                        final BlockState iterState = inter.getState();
                        final BlockType interType = state.getType();
                        return SpongeImplHooks.hasBlockTileEntity((net.minecraft.block.BlockState) iterState);
                    });
                }
                return hasTile;
            });
        }
        return false;
    }

    @Override
    public boolean doesCaptureNeighborNotifications(final BlockTickContext context) {
        return context.allowsBulkBlockCaptures();
    }

    @Override
    LocatableBlock getLocatableBlockSourceFromContext(final PhaseContext<?> context) {
        return context.getSource(LocatableBlock.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking over at a location!", context));
    }

    @Override
    public void unwind(final BlockTickContext context) {
        TrackingUtil.processBlockCaptures(context);
    }

    @Override
    public void appendContextPreExplosion(final ExplosionContext explosionContext, final BlockTickContext context) {
        context.applyOwnerIfAvailable(explosionContext::creator);
        context.applyNotifierIfAvailable(explosionContext::notifier);
        final LocatableBlock locatableBlock = this.getLocatableBlockSourceFromContext(context);
        explosionContext.source(locatableBlock);
    }


    /**
     * Specifically overridden here because some states have defaults and don't check the context.
     * @param context The context
     * @return True if bulk block captures are usable for this entity type (default true)
     */
    @Override
    public boolean doesBulkBlockCapture(final BlockTickContext context) {
        return context.allowsBulkBlockCaptures();
    }

    /**
     * Specifically overridden here because some states have defaults and don't check the context.
     * @param context The context
     * @return True if block events are to be tracked by the specific type of entity (default is true)
     */
    @Override
    public boolean doesBlockEventTracking(final BlockTickContext context) {
        return context.allowsBlockEvents();
    }

    @Override
    public boolean doesCaptureEntityDrops(final BlockTickContext context) {
        return true; // Maybe make this configurable as well.
    }

    @Override
    public BlockChange associateBlockChangeWithSnapshot(final BlockTickContext phaseContext, final net.minecraft.block.BlockState newState, final Block newBlock,
                                                        final net.minecraft.block.BlockState currentState, final SpongeBlockSnapshot snapshot, final Block originalBlock) {
        if (phaseContext.tickingBlock instanceof IGrowable) {
            if (newBlock == Blocks.AIR) {
                return BlockChange.BREAK;
            }
            if (newBlock instanceof IGrowable || newState.getMaterial().isFlammable()) {
                return BlockChange.GROW;
            }
        }
        return super.associateBlockChangeWithSnapshot(phaseContext, newState, newBlock, currentState, snapshot, originalBlock);
    }

    @Override
    public String toString() {
        return this.desc;
    }

}
