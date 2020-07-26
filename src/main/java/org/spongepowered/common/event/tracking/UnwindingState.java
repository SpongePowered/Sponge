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
package org.spongepowered.common.event.tracking;

import com.google.common.collect.ListMultimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.context.BlockTransaction;
import org.spongepowered.common.event.tracking.context.MultiBlockCaptureSupplier;
import org.spongepowered.common.event.tracking.phase.general.ExplosionContext;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
public final class UnwindingState implements IPhaseState<UnwindingPhaseContext> {

    public static UnwindingState getInstance() {
        return Holder.INSTANCE;
    }

    private UnwindingState() { }

    private static final class Holder {
        static final UnwindingState INSTANCE = new UnwindingState();
    }

    @Override
    public UnwindingPhaseContext createPhaseContext(PhaseTracker server) {
        throw new UnsupportedOperationException("Use UnwindingPhaseContext#unwind(IPhaseState, PhaseContext)! Cannot create a context based on Post state!");
    }

    @Override
    public boolean requiresPost() {
        return false;
    }

    @Override
    public boolean ignoresBlockUpdateTick(final UnwindingPhaseContext context) {
        return true;
    }

    @Override
    public boolean ignoresScheduledUpdates() {
        return false;
    }

    @Override
    public boolean alreadyCapturingEntitySpawns() {
        return true;
    }

    @Override
    public boolean alreadyCapturingEntityTicks() {
        return true;
    }

    @Override
    public boolean alreadyCapturingTileTicks() {
        return true;
    }

    @Override
    public boolean alreadyProcessingBlockItemDrops() {
        return true;
    }

    @Override
    public boolean allowsGettingQueuedRemovedTiles() {
        return true;
    }

    @Override
    public boolean hasSpecificBlockProcess(final UnwindingPhaseContext context) {
        return !context.isPostingSpecialProcess();
    }

    @Override
    public boolean tracksTileEntityChanges(final UnwindingPhaseContext context) {
        return context.allowsBulkBlockCaptures() && !context.isPostingSpecialProcess() && context.usesMulti && context.tracksTiles;
    }

    @Override
    public boolean doesCaptureNeighborNotifications(final UnwindingPhaseContext context) {
        return context.allowsBulkBlockCaptures() && !context.isPostingSpecialProcess() && context.usesMulti && context.tracksNeighborNotifications;
    }

    @Override
    public boolean getShouldCancelAllTransactions(final UnwindingPhaseContext context, final List<ChangeBlockEvent> blockEvents, final ChangeBlockEvent.Post postEvent,
                                                  final ListMultimap<BlockPos, BlockEventData> scheduledEvents, final boolean noCancelledTransactions) {
        return context.allowsBulkBlockCaptures() && context.isPostingSpecialProcess() && context.getUnwindingState().getShouldCancelAllTransactions(context.getUnwindingContext(), blockEvents, postEvent, scheduledEvents, noCancelledTransactions);
    }

    @Override
    public void processCancelledTransaction(final UnwindingPhaseContext context, final Transaction<BlockSnapshot> transaction, final BlockSnapshot original) {
        if (context.isPostingSpecialProcess()) {
            context.getCapturedBlockSupplier().cancelTransaction(original);
        }
        IPhaseState.super.processCancelledTransaction(context, transaction, original);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void appendContextPreExplosion(final ExplosionContext explosionContext, final UnwindingPhaseContext context) {
        final IPhaseState<?> phaseState = context.getUnwindingState();
        final PhaseContext<?> unwinding = context.getUnwindingContext();
        ((IPhaseState) phaseState).appendContextPreExplosion(explosionContext, unwinding);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void associateNeighborStateNotifier(final UnwindingPhaseContext context, @Nullable final BlockPos sourcePos, final Block block, final BlockPos notifyPos,
                                               final ServerWorld minecraftWorld, final PlayerTracker.Type notifier) {
        final IPhaseState<?> unwindingState = context.getUnwindingState();
        final PhaseContext<?> unwindingContext = context.getUnwindingContext();
        ((IPhaseState) unwindingState).associateNeighborStateNotifier(unwindingContext, sourcePos, block, notifyPos, minecraftWorld, notifier);
    }

    @SuppressWarnings({"unchecked", "try"})
    @Override
    public void unwind(final UnwindingPhaseContext context) {
        final IPhaseState<?> unwindingState = context.getUnwindingState();
        final PhaseContext<?> unwindingContext = context.getUnwindingContext();
        try {
            final List<Entity> contextEntities = context.getCapturedEntitiesOrEmptyList();
            final List<Entity> contextItems = (List<Entity>) (List<?>) context.getCapturedItemsOrEmptyList();
            final MultiBlockCaptureSupplier originalSupplier = context.getCapturedBlockSupplier();
            if (context.usesMulti) {
                TrackingUtil.processBlockCaptures(context, 0, originalSupplier);
                do {
                    // The auto closeable is set up to only pop the pushed supplier after the fact.
                    // The pushed supplier is only provided in the case where a new set of block
                    // changes needs to be processed due to depth first processing.
                    try (final AutoCloseable ignored = context::popBlockSupplier) {
                        final MultiBlockCaptureSupplier recursiveSupplier = context.getCapturedBlockSupplier();
                        if (!recursiveSupplier.isEmpty()) {
                            // So, first we mark ourselves with a new capture supplier
                            context.pushNewCaptureSupplier();
                            TrackingUtil.processBlockCaptures(context, 0, recursiveSupplier);
                        }
                    }
                }
                while (context.blockSuppliers != null && !context.blockSuppliers.isEmpty());

            } else {
                TrackingUtil.processBlockCaptures(context);
            }
            if (contextEntities.isEmpty() && contextItems.isEmpty()) {
                return;
            }
            if (!contextEntities.isEmpty()) {
                final ArrayList<Entity> entities = new ArrayList<>(contextEntities);
                contextEntities.clear();
                ((IPhaseState) unwindingState).postProcessSpawns(unwindingContext, entities);
            }
            if (!contextItems.isEmpty()) {
                final ArrayList<Entity> items = new ArrayList<>(contextItems);
                contextItems.clear();
                SpongeCommonEventFactory.callSpawnEntity(items, unwindingContext);

            }
        } catch (final Exception e) {
            PhasePrinter.printExceptionFromPhase(PhaseTracker.getInstance().stack, e, context);
        }
    }

    @SuppressWarnings("try")
    @Override
    public void postProcessSpecificBlockChange(final UnwindingPhaseContext currentContext, final BlockTransaction.ChangeBlock changeBlock, final int depth) {
        if (PhasePrinter.checkMaxBlockProcessingDepth(this, currentContext, depth)) {
            return;
        }

        currentContext.getCapturedEntitySupplier().acceptAndClearIfNotEmpty(entities -> {
            final ArrayList<Entity> capturedEntities = new ArrayList<>(entities);
            currentContext.getUnwindingState().postProcessSpawns(currentContext.getUnwindingContext(), capturedEntities);
        });
        final MultiBlockCaptureSupplier original = currentContext.getCapturedBlockSupplier();
        if (!original.isEmpty()) {
            try {

                if (currentContext.usesMulti) {
                    TrackingUtil.processBlockCaptures(currentContext, depth + 1, original);
                    do {
                        // The auto closeable is set up to only pop the pushed supplier after the fact.
                        // The pushed supplier is only provided in the case where a new set of block
                        // changes needs to be processed due to depth first processing.
                        try (final AutoCloseable ignored = currentContext::popBlockSupplier) {
                            final MultiBlockCaptureSupplier recursiveSupplier = currentContext.getCapturedBlockSupplier();
                            if (!recursiveSupplier.isEmpty()) {
                                // So, first we mark ourselves with a new capture supplier
                                currentContext.pushNewCaptureSupplier();
                                TrackingUtil.processBlockCaptures(currentContext, depth + 1, recursiveSupplier);
                            }
                        }
                    }
                    while (currentContext.blockSuppliers != null && !currentContext.blockSuppliers.isEmpty());

                } else {
                    TrackingUtil.processBlockCaptures(currentContext, depth + 1, original);
                }
            } catch (final Exception e) {
                PhasePrinter.printExceptionFromPhase(PhaseTracker.getInstance().stack, e, currentContext);
            }
        }
    }

    @Override
    public boolean spawnEntityOrCapture(final UnwindingPhaseContext context, final Entity entity) {
        return context.getCapturedEntities().add(entity);
    }

    @Override
    public boolean doesCaptureEntitySpawns() {
        return false;
    }

    @Override
    public void performOnBlockAddedSpawns(final UnwindingPhaseContext context, final int depth) {
        if (PhasePrinter.checkMaxBlockProcessingDepth(this, context, depth)) {
            return;
        }

        context.getCapturedEntitySupplier().acceptAndClearIfNotEmpty(entities -> {
            final ArrayList<Entity> capturedEntities = new ArrayList<>(entities);
            context.getUnwindingState().postProcessSpawns(context.getUnwindingContext(), capturedEntities);
        });
        TrackingUtil.processBlockCaptures(context, depth, context.getCapturedBlockSupplier());
    }

    @Override
    public void performPostBlockNotificationsAndNeighborUpdates(final UnwindingPhaseContext context,
                                                                final BlockState newState, final SpongeBlockChangeFlag changeFlag, final int depth) {
        if (context.isPostingSpecialProcess()) {
            return; // it will keep on going internally.
        }
        if (PhasePrinter.checkMaxBlockProcessingDepth(this, context, depth)) {
            return;
        }
        context.setBulkBlockCaptures(false);
        TrackingUtil.processBlockCaptures(context, depth, context.getCapturedBlockSupplier());

    }

    @Override
    public boolean doesBulkBlockCapture(final UnwindingPhaseContext context) {
        return !context.isPostingSpecialProcess() && context.allowsBulkBlockCaptures();
    }

    /**
     * We want to allow the post state to do it's own thing and avoid entering extra states for block
     * ticking from unwinds.
     * @param context
     * @return
     */
    @Override
    public boolean alreadyCapturingBlockTicks(final UnwindingPhaseContext context) {
        return true;
    }

    /**
     * Specifically overridden to delegate to the unwinding state. Since the block physics processing is all handled in
     * {@link TrackingUtil#performBlockAdditions(List, PhaseContext, boolean, ListMultimap, int)}.
     *  @param blockChange change
     * @param snapshotTransaction the transaction
     * @param context the context
     */
    @SuppressWarnings("unchecked")
    @Override
    public void postBlockTransactionApplication(final BlockChange blockChange, final Transaction<? extends BlockSnapshot> snapshotTransaction,
                                                final UnwindingPhaseContext context) {
        final IPhaseState<?> unwindingState = context.getUnwindingState();
        final PhaseContext unwindingContext = context.getUnwindingContext();
        ((IPhaseState) unwindingState).postBlockTransactionApplication(blockChange, snapshotTransaction, unwindingContext);
    }

    private final String desc = TrackingUtil.phaseStateToString("General", this);

    @Override
    public String toString() {
        return this.desc;
    }

}
