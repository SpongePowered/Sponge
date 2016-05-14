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
package org.spongepowered.common.event.tracking.phase;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.EventConsumer;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.function.GeneralFunctions;
import org.spongepowered.common.event.tracking.phase.util.PhaseUtil;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeProxyBlockAccess;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public final class GeneralPhase extends TrackingPhase {

    public enum State implements IPhaseState {
        COMMAND {
            @Override
            public boolean canSwitchTo(IPhaseState state) {
                return state instanceof BlockPhase.State;
            }
        },
        COMPLETE {
            @Override
            public boolean canSwitchTo(IPhaseState state) {
                return true;
            }
        };

        @Override
        public GeneralPhase getPhase() {
            return TrackingPhases.GENERAL;
        }
    }

    public enum Post implements IPhaseState {
        /**
         * A specific state that is introduced for the sake of
         * preventing leaks into other phases as various phases
         * are unwound. This state is specifically to ignore any
         * transactions that may take place.
         */
        UNWINDING;


        @Override
        public TrackingPhase getPhase() {
            return TrackingPhases.GENERAL;
        }

        @Override
        public boolean canSwitchTo(IPhaseState state) {
            return false;
        }

        @Override
        public boolean tracksBlockRestores() {
            return true;
        }

    }

    GeneralPhase(@Nullable TrackingPhase parent) {
        super(parent);
    }

    @Override
    public GeneralPhase addChild(TrackingPhase child) {
        super.addChild(child);
        return this;
    }

    @Override
    public void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext phaseContext) {
        if (state == State.COMMAND) {
            final ICommand command = phaseContext.firstNamed(InternalNamedCauses.General.COMMAND, ICommand.class)
                    .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing a command, but none found!", phaseContext));
            final ICommandSender sender = phaseContext.firstNamed(NamedCause.SOURCE, ICommandSender.class)
                    .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing a Command Sender, but none found!", phaseContext));
            phaseContext.getCapturedBlockSupplier()
                    .ifPresentAndNotEmpty(list -> GeneralFunctions.processBlockCaptures(list, causeTracker, state, phaseContext));
            phaseContext.getCapturedEntitySupplier()
                    .ifPresentAndNotEmpty(entities -> {
                        // TODO the entity spawn causes are not likely valid, need to investigate further.
                        final Cause cause = Cause.source(SpawnCause.builder()
                                .type(InternalSpawnTypes.PLACEMENT)
                                .build())
                                .build();
                        EventConsumer.event(SpongeEventFactory.createSpawnEntityEvent(cause, entities, causeTracker.getWorld()))
                                .nonCancelled(event -> {
                                    for (Entity entity : event.getEntities()) {
                                        TrackingUtil.associateEntityCreator(phaseContext,
                                                EntityUtil.toNative(entity),
                                                causeTracker.getMinecraftWorld()
                                        );
                                        causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                    }
                                })
                                .process();
                    });
        } else if (state == Post.UNWINDING) {
            final IPhaseState unwindingState = phaseContext.firstNamed(InternalNamedCauses.Tracker.UNWINDING_STATE, IPhaseState.class)
                    .orElseThrow(PhaseUtil.throwWithContext("Expected to be unwinding a phase, but no phase found!", phaseContext));
            final PhaseContext unwindingContext = phaseContext.firstNamed(InternalNamedCauses.Tracker.UNWINDING_CONTEXT, PhaseContext.class)
                    .orElseThrow(PhaseUtil.throwWithContext("Expected to be unwinding a phase, but no context found!", phaseContext));
            state.getPhase().postDispatch(causeTracker, unwindingState, unwindingContext, phaseContext);
        }
    }

    @Override
    public void postDispatch(CauseTracker causeTracker, IPhaseState unwindingState, PhaseContext unwindingContext, PhaseContext postContext) {
        final List<BlockSnapshot> contextBlocks = postContext.getCapturedBlocks();
        final List<Entity> contextEntities = postContext.getCapturedEntities();
        final List<Entity> contextItems = postContext.getCapturedItems();
        final List<Transaction<BlockSnapshot>> invalidTransactions = postContext.getInvalidTransactions();
        if (contextBlocks.isEmpty() && contextEntities.isEmpty() && contextItems.isEmpty() && invalidTransactions.isEmpty()) {
            return;
        }
        if (!contextBlocks.isEmpty()) {
            final List<BlockSnapshot> blockSnapshots = new ArrayList<>(contextBlocks);
            contextBlocks.clear();
            processBlockTransactionListsPost(contextBlocks, blockSnapshots, causeTracker, unwindingState, unwindingContext);
        }
        if (!contextEntities.isEmpty()) {
            final ArrayList<Entity> entities = new ArrayList<>(contextEntities);
            contextEntities.clear();
            unwindingState.getPhase().processPostEntitySpawns(causeTracker, unwindingState, entities);
        }
        if (!contextItems.isEmpty()) {
            final ArrayList<Entity> items = new ArrayList<>(contextItems);
            contextItems.clear();
            unwindingState.getPhase().processPostItemSpawns(causeTracker, unwindingState, items);
        }

    }

    /**
     *
     * @param postContextList This list should be empty on initial unwinding since the blocks haven't been called
     * {@link Block#onBlockAdded(net.minecraft.world.World, BlockPos, IBlockState)}. However, after each time the
     * block is changed, and notifications are sent out, the list should be populated.
     * @param snapshots
     * @param causeTracker
     * @param unwindingState
     * @param unwinding
     */
    @SuppressWarnings("unchecked")
    public static void processBlockTransactionListsPost(List<BlockSnapshot> postContextList, List<BlockSnapshot> snapshots, CauseTracker causeTracker, IPhaseState unwindingState, PhaseContext unwinding) {
        ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays = new ImmutableList[GeneralFunctions.EVENT_COUNT];
        ImmutableList.Builder<Transaction<BlockSnapshot>>[] transactionBuilders = new ImmutableList.Builder[GeneralFunctions.EVENT_COUNT];
        for (int i = 0; i < GeneralFunctions.EVENT_COUNT; i++) {
            transactionBuilders[i] = new ImmutableList.Builder<>();
        }
        final List<ChangeBlockEvent> blockEvents = new ArrayList<>();
        final WorldServer minecraftWorld = causeTracker.getMinecraftWorld();

        for (BlockSnapshot snapshot : snapshots) {
            GeneralFunctions.TRANSACTION_PROCESSOR.apply(transactionBuilders).accept(GeneralFunctions.TRANSACTION_CREATION.apply(minecraftWorld, snapshot));
        }

        for (int i = 0; i < GeneralFunctions.EVENT_COUNT; i++) {
            transactionArrays[i] = transactionBuilders[i].build();
        }
        final ChangeBlockEvent[] mainEvents = new ChangeBlockEvent[4];
        final Cause.Builder builder = Cause.source(unwinding.firstNamed(NamedCause.SOURCE, Object.class).get());
        final World world = causeTracker.getWorld();
        GeneralFunctions.iterateChangeBlockEvents(transactionArrays, blockEvents, mainEvents, builder, world);
        if (GeneralFunctions.processMultiEvents(transactionArrays, blockEvents, mainEvents, builder, world)) {
            return;
        }

        if (!transactionArrays[BlockChange.DECAY.ordinal()].isEmpty()) {
            final ChangeBlockEvent event = BlockChange.DECAY.createEvent(builder.build(), world, transactionArrays[BlockChange.DECAY.ordinal()]);
            mainEvents[BlockChange.DECAY.ordinal()] = event;
            blockEvents.add(event);
        }

        for (ChangeBlockEvent blockEvent : blockEvents) {
            final BlockChange blockChange = BlockChange.forEvent(blockEvent);

            if (blockEvent.isCancelled()) {
                // Restore original blocks
                for (Transaction<BlockSnapshot> transaction : Lists.reverse(blockEvent.getTransactions())) {
                    transaction.getOriginal().restore(true, false);
                }
                return;
            } else {
                // Need to undo any invalid changes
                final List<Transaction<BlockSnapshot>> invalid = unwinding.getInvalidTransactions();
                for (Transaction<BlockSnapshot> snapshotTransaction : blockEvent.getTransactions()) {
                    if (!snapshotTransaction.isValid()) {
                        invalid.add(snapshotTransaction);
                    } else {
                        unwindingState.markPostNotificationChange(blockChange, minecraftWorld, unwinding, snapshotTransaction);
                    }
                }

                if (invalid.size() > 0) {
                    for (Transaction<BlockSnapshot> transaction : Lists.reverse(invalid)) {
                        transaction.getOriginal().restore(true, false);
                    }
                }

                performBlockAdditions(causeTracker, postContextList, blockEvent.getTransactions(), blockChange, builder, unwindingState, unwinding);
            }
        }

    }

    private static void performBlockAdditions(CauseTracker causeTracker, List<BlockSnapshot> postContext, List<Transaction<BlockSnapshot>> transactions, @Nullable BlockChange type, Cause.Builder builder, IPhaseState phaseState, PhaseContext phaseContext) {
        // We have to use a proxy so that our pending changes are notified such that any accessors from block
        // classes do not fail on getting the incorrect block state from the IBlockAccess
        final WorldServer minecraftWorld = causeTracker.getMinecraftWorld();
        final SpongeProxyBlockAccess proxyBlockAccess = new SpongeProxyBlockAccess(minecraftWorld, transactions);
        final PhaseContext.CapturedMultiMapSupplier<BlockPos, ItemStack> capturedBlockDrops = phaseContext.getBlockDropSupplier();
        for (Transaction<BlockSnapshot> transaction : transactions) {
            if (!transaction.isValid()) {
                continue; // Don't use invalidated block transactions during notifications, these only need to be restored
            }
            // Handle custom replacements
            if (transaction.getCustom().isPresent()) {
                transaction.getFinal().restore(true, false);
            }

            final SpongeBlockSnapshot oldBlockSnapshot = (SpongeBlockSnapshot) transaction.getOriginal();
            final SpongeBlockSnapshot newBlockSnapshot = (SpongeBlockSnapshot) transaction.getFinal();
            final Cause currentCause = builder.build();

            // Handle item drops captured
            if (capturedBlockDrops != null) {
                // These are not to be re-captured since we know the changes that took place
                GeneralFunctions.spawnItemStacksForBlockDrop(capturedBlockDrops.get().get(VecHelper.toBlockPos(oldBlockSnapshot.getPosition())), newBlockSnapshot,
                        causeTracker, phaseContext, phaseState);
            }

            SpongeHooks.logBlockAction(currentCause, minecraftWorld, type, transaction);
            int updateFlag = oldBlockSnapshot.getUpdateFlag();
            BlockPos pos = VecHelper.toBlockPos(oldBlockSnapshot.getPosition());
            IBlockState originalState = (IBlockState) oldBlockSnapshot.getState();
            IBlockState newState = (IBlockState) newBlockSnapshot.getState();
            // Containers get placed automatically
            if (!SpongeImplHooks.blockHasTileEntity(newState.getBlock(), newState)) {
                newState.getBlock().onBlockAdded(minecraftWorld, pos, newState);
            }

            proxyBlockAccess.proceed();
            causeTracker.getMixinWorld().markAndNotifyNeighbors(pos, null, originalState, newState, updateFlag);

            if (!postContext.isEmpty()) {
                final ArrayList<BlockSnapshot> snapshots = new ArrayList<>(postContext);
                postContext.clear();
                processBlockTransactionListsPost(postContext, snapshots, causeTracker, phaseState, phaseContext);
            }

            // Here is where we need to re-post process Depth First

        }
    }

    @Override
    public boolean ignoresBlockUpdateTick(PhaseData phaseData) {
        return phaseData.getState() == Post.UNWINDING;
    }

    @Override
    public boolean allowEntitySpawns(IPhaseState currentState) {
        return currentState != Post.UNWINDING;
    }

    @Override
    public boolean ignoresBlockEvent(IPhaseState phaseState) {
        return phaseState == Post.UNWINDING;
    }

    @Override
    public boolean alreadyCapturingBlockTicks(IPhaseState state, PhaseContext context) {
        return state == Post.UNWINDING;
    }

    @Override
    public boolean alreadyCapturingEntitySpawns(IPhaseState state) {
        return state == Post.UNWINDING;
    }

    @Override
    public boolean alreadyCapturingEntityTicks(IPhaseState state) {
        return state == Post.UNWINDING;
    }

    @Override
    public boolean alreadyCapturingTileTicks(IPhaseState state) {
        return state == Post.UNWINDING;
    }

    @Override
    public boolean alreadyCapturingItemSpawns(IPhaseState currentState) {
        return currentState == Post.UNWINDING;
    }

    @Override
    public boolean ignoresScheduledUpdates(IPhaseState phaseState) {
        return phaseState == Post.UNWINDING;
    }
}
