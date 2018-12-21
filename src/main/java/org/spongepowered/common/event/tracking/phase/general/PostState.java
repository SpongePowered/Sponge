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
package org.spongepowered.common.event.tracking.phase.general;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.BlockPosMultiMap;
import org.spongepowered.common.world.BlockChange;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
public final class PostState extends GeneralState<UnwindingPhaseContext> {

    @SuppressWarnings("unchecked")
    private static void postBlockAddedSpawns(UnwindingPhaseContext postContext, IPhaseState<?> unwindingState, PhaseContext<?> unwindingPhaseContext,
        BlockPosMultiMap capturedBlockSupplier, int depth) {
        if (PhaseTracker.checkMaxBlockProcessingDepth(GeneralPhase.Post.UNWINDING, postContext, depth)) {
            return;
        }

        postContext.getCapturedEntitySupplier().acceptAndClearIfNotEmpty(entities -> {
            final ArrayList<Entity> capturedEntities = new ArrayList<>(entities);
            ((IPhaseState) unwindingState).postProcessSpawns(unwindingPhaseContext, capturedEntities);
        });
        TrackingUtil.processBlockCaptures(capturedBlockSupplier, GeneralPhase.Post.UNWINDING, postContext, depth);
    }

    @Override
    public UnwindingPhaseContext createPhaseContext() {
        throw new UnsupportedOperationException("Use UnwindingPhaseContext#unwind(IPhaseState, PhaseContext)! Cannot create a context based on Post state!");
    }

    @Override
    public boolean canSwitchTo(IPhaseState<?> state) {
        return state != this; // Basically, we don't want to cause issues if we're entering another state due to plugin listeners or anything else.
    }

    @Override
    public boolean requiresPost() {
        return false;
    }

    @Override
    public boolean ignoresBlockUpdateTick(UnwindingPhaseContext context) {
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

    @SuppressWarnings("unchecked")
    @Override
    public void appendContextPreExplosion(ExplosionContext explosionContext, UnwindingPhaseContext context) {
        final IPhaseState<?> phaseState = context.getUnwindingState();
        final PhaseContext<?> unwinding = context.getUnwindingContext();
        ((IPhaseState) phaseState).appendContextPreExplosion(explosionContext, unwinding);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void associateNeighborStateNotifier(UnwindingPhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
        WorldServer minecraftWorld, PlayerTracker.Type notifier) {
        final IPhaseState<?> unwindingState = context.getUnwindingState();
        final PhaseContext<?> unwindingContext = context.getUnwindingContext();
        ((IPhaseState) unwindingState).associateNeighborStateNotifier(unwindingContext, sourcePos, block, notifyPos, minecraftWorld, notifier);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void unwind(UnwindingPhaseContext context) {
        final IPhaseState<?> unwindingState = context.getUnwindingState();
        final PhaseContext<?> unwindingContext = context.getUnwindingContext();
        this.postDispatch(unwindingState, unwindingContext, context);
    }

    /**
     * This is the post dispatch method that is automatically handled for
     * states that deem it necessary to have some post processing for
     * advanced game mechanics. This is always performed when capturing
     * has been turned on during a phases's
     * {@link IPhaseState#unwind(PhaseContext)} is
     * dispatched. The rules of post dispatch are as follows:
     * - Entering extra phases is not allowed: This is to avoid
     *  potential recursion in various corner cases.
     * - The unwinding phase context is provided solely as a root
     *  cause tracking for any nested notifications that require
     *  association of causes
     * - The unwinding phase is used with the unwinding state to
     *  further exemplify during what state that was unwinding
     *  caused notifications. This narrows down to the exact cause
     *  of the notifications.
     * - post dispatch may loop several times until no more notifications
     *  are required to be dispatched. This may include block physics for
     *  neighbor notification events.
     *
     * @param unwindingState
     * @param unwindingContext The context of the state that was unwinding,
     *     contains the root cause for the state
     * @param postContext The post dispatch context captures containing any
     */
    @SuppressWarnings("unchecked")
    private void postDispatch(IPhaseState<?> unwindingState, PhaseContext<?> unwindingContext, UnwindingPhaseContext postContext) {
        final List<Entity> contextEntities = postContext.getCapturedEntitiesOrEmptyList();
        final List<Entity> contextItems = (List<Entity>) (List<?>) postContext.getCapturedItemsOrEmptyList();
        if (postContext.getCapturedBlockSupplier().isEmpty() && contextEntities.isEmpty() && contextItems.isEmpty()) {
            return;
        }
        if (!postContext.getCapturedBlockSupplier().isEmpty()) {
            TrackingUtil.processBlockCaptures(postContext.getCapturedBlockSupplier(), this, postContext);
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
    }

    @Override
    public boolean spawnEntityOrCapture(UnwindingPhaseContext context, Entity entity, int chunkX, int chunkZ) {
        return context.getCapturedEntities().add(entity);
    }

    @Override
    public boolean doesCaptureEntitySpawns() {
        return false;
    }

    @Override
    public void performOnBlockAddedSpawns(UnwindingPhaseContext context, int depth) {
        postBlockAddedSpawns(context, context.getUnwindingState(), context.getUnwindingContext(), context.getCapturedBlockSupplier(), depth);
    }

    @Override
    public void performPostBlockNotificationsAndNeighborUpdates(UnwindingPhaseContext context, int depth) {
        if (PhaseTracker.checkMaxBlockProcessingDepth(this, context, depth)) {
            return;
        }
        context.setBulkBlockCaptures(false);
        TrackingUtil.processBlockCaptures(context.getCapturedBlockSupplier(), this, context, depth);

    }

    @Override
    public boolean doesBulkBlockCapture(UnwindingPhaseContext context) {
        return context.allowsBulkBlockCaptures();
    }

    /**
     * We want to allow the post state to do it's own thing and avoid entering extra states for block
     * ticking from unwinds.
     * @param context
     * @return
     */
    @Override
    public boolean alreadyCapturingBlockTicks(UnwindingPhaseContext context) {
        return true;
    }

    /**
     * Specifically overridden to delegate to the unwinding state. Since the block physics processing is all handled in
     * {@link TrackingUtil#performBlockAdditions(List, IPhaseState, PhaseContext, boolean, int)}.
     *
     * @param blockChange change
     * @param snapshotTransaction the transaction
     * @param context the context
     */
    @SuppressWarnings("unchecked")
    @Override
    public void postBlockTransactionApplication(BlockChange blockChange, Transaction<BlockSnapshot> snapshotTransaction,
        UnwindingPhaseContext context) {
        final IPhaseState<?> unwindingState = context.getUnwindingState();
        final PhaseContext unwindingContext = context.getUnwindingContext();
        ((IPhaseState) unwindingState).postBlockTransactionApplication(blockChange, snapshotTransaction, unwindingContext);
    }

}
