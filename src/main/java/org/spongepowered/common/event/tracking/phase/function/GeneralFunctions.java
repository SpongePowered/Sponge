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
package org.spongepowered.common.event.tracking.phase.function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.EventConsumer;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.phase.WorldPhase;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeProxyBlockAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GeneralFunctions {

    public static final int BREAK_BLOCK_INDEX = 0;
    public static final int PLACE_BLOCK_INDEX = 1;
    public static final int DECAY_BLOCK_INDEX = 2;
    public static final int CHANGE_BLOCK_INDEX = 3;
    public static final int MULTI_CHANGE_INDEX = 4;

    public static final int EVENT_COUNT = 5;

    public static void processUserBreakage(@Nullable BlockChange change, World minecraftWorld, Transaction<BlockSnapshot> transaction, @Nullable Entity tickingEntity) {
        if (change == BlockChange.BREAK) {
            final BlockPos blockPos = VecHelper.toBlockPos(transaction.getOriginal().getPosition());
            EntityUtil.findHangingEntities(minecraftWorld, blockPos).stream()
                    .filter(entity -> entity instanceof EntityItemFrame)
                    .forEach(hanging -> {
                        final EntityItemFrame itemFrame = (EntityItemFrame) hanging;
                        if (tickingEntity != null) {
                            itemFrame.dropItemOrSelf(EntityUtil.toNative(tickingEntity), true);
                        }
                        itemFrame.setDead();
                    });
        }
    }

    @SuppressWarnings("unchecked")
    public static void processBlockCaptures(List<BlockSnapshot> snapshots, CauseTracker causeTracker, IPhaseState state, PhaseContext context) {
        ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays = new ImmutableList[GeneralFunctions.EVENT_COUNT];
        ImmutableList.Builder<Transaction<BlockSnapshot>>[] transactionBuilders = new ImmutableList.Builder[GeneralFunctions.EVENT_COUNT];
        for (int i = 0; i < GeneralFunctions.EVENT_COUNT; i++) {
            transactionBuilders[i] = new ImmutableList.Builder<>();
        }
        final List<ChangeBlockEvent> blockEvents = new ArrayList<>();
        final WorldServer minecraftWorld = causeTracker.getMinecraftWorld();
        final IMixinWorldServer mixinWorld = causeTracker.getMixinWorld();

        snapshots.stream()
                .map(getBlockSnapshotTransactionFunction(minecraftWorld, mixinWorld))
                .forEach(getTransactionConsumer(transactionBuilders));

        for (int i = 0; i < GeneralFunctions.EVENT_COUNT; i++) {
            transactionArrays[i] = transactionBuilders[i].build();
        }
        final ChangeBlockEvent[] mainEvents = new ChangeBlockEvent[4];
        final Cause.Builder builder = Cause.source(context.firstNamed(NamedCause.SOURCE, Object.class).get());
        final org.spongepowered.api.world.World world = causeTracker.getWorld();
        iterateChangeBlockEvents(transactionArrays, blockEvents, mainEvents, builder, world);
        if (processMultiEvents(transactionArrays, blockEvents, mainEvents, builder, world)) {
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
                Lists.reverse(blockEvent.getTransactions()).forEach(transaction -> transaction.getOriginal().restore(true, false));
                return;
            } else {
                // Need to undo any invalid changes
                final List<Transaction<BlockSnapshot>> invalid = context.getInvalidTransactions().get();
                blockEvent.getTransactions().forEach(snapshotTransaction -> {
                    if (!snapshotTransaction.isValid()) {
                        invalid.add(snapshotTransaction);
                    } else {
                        state.handleBlockChangeWithUser(blockChange, minecraftWorld, snapshotTransaction, context);
                    }
                });

                if (invalid.size() > 0) {
                    for (Transaction<BlockSnapshot> transaction : Lists.reverse(invalid)) {
                        transaction.getOriginal().restore(true, false);
                    }
                }

                // TODO - Any additional changes to the world should take place
                // and get processed in the proceeding UNWINDING state.
                performBlockAdditions(causeTracker, blockEvent.getTransactions(), blockChange, builder, context);
            }
        }
    }

    public static void iterateChangeBlockEvents(ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays, List<ChangeBlockEvent> blockEvents,
            ChangeBlockEvent[] mainEvents, Cause.Builder builder, org.spongepowered.api.world.World world) {
        for (BlockChange blockChange : BlockChange.values()) {
            if (blockChange == BlockChange.DECAY) { // Decay takes place after.
                continue;
            }
            if (!transactionArrays[blockChange.ordinal()].isEmpty()) {
                final ChangeBlockEvent event = blockChange.createEvent(builder.build(), world, transactionArrays[blockChange.ordinal()]);
                mainEvents[blockChange.ordinal()] = event;
                if (event != null) {
                    blockEvents.add(event);
                }
            }
        }
    }

    @Nonnull
    private static Consumer<Transaction<BlockSnapshot>> getTransactionConsumer(ImmutableList.Builder<Transaction<BlockSnapshot>>[] transactionBuilders) {
        return transaction -> { // Assign the transactions as necessary
            final BlockChange blockChange = ((SpongeBlockSnapshot) transaction.getOriginal()).blockChange;
            transactionBuilders[blockChange.ordinal()].add(transaction);
            transactionBuilders[GeneralFunctions.MULTI_CHANGE_INDEX].add(transaction);
        };
    }

    private static boolean shouldChainCause(CauseTracker tracker, Cause cause) {
        final PhaseData currentPhase = tracker.getStack().peek();
        if (currentPhase != null) {
            final IPhaseState state = currentPhase.getState();
            final PhaseContext context = currentPhase.getContext();
            Optional<BlockSnapshot> currentTickingBlock = context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class);
            return state == WorldPhase.Tick.BLOCK && currentTickingBlock.isPresent()
                   && !context.first(PluginContainer.class).isPresent() && !cause.contains(currentTickingBlock);
        }
        return false;
    }

    private static void performBlockAdditions(CauseTracker causeTracker, List<Transaction<BlockSnapshot>> transactions, @Nullable BlockChange type,
            Cause.Builder builder, PhaseContext phaseContext) {
        // We have to use a proxy so that our pending changes are notified such that any accessors from block
        // classes do not fail on getting the incorrect block state from the IBlockAccess
        final WorldServer minecraftWorld = causeTracker.getMinecraftWorld();
        SpongeProxyBlockAccess proxyBlockAccess = new SpongeProxyBlockAccess(minecraftWorld, transactions);
        for (Transaction<BlockSnapshot> transaction : transactions) {
            if (!transaction.isValid()) {
                continue; // Don't use invalidated block transactions during notifications, these only need to be restored
            }
            // Handle custom replacements
            if (transaction.getCustom().isPresent()) {
                transaction.getFinal().restore(true, false);
            }

            SpongeBlockSnapshot oldBlockSnapshot = (SpongeBlockSnapshot) transaction.getOriginal();
            SpongeBlockSnapshot newBlockSnapshot = (SpongeBlockSnapshot) transaction.getFinal();
            final Cause currentCause = builder.build();
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

        }
    }

    public static void processPostBlockCaptures(List<BlockSnapshot> blockSnapshots, CauseTracker causeTracker, IPhaseState unwindingState, PhaseContext context) {
        ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays = new ImmutableList[GeneralFunctions.EVENT_COUNT];
        ImmutableList.Builder<Transaction<BlockSnapshot>>[] transactionBuilders = new ImmutableList.Builder[GeneralFunctions.EVENT_COUNT];
        for (int i = 0; i < GeneralFunctions.EVENT_COUNT; i++) {
            transactionBuilders[i] = new ImmutableList.Builder<>();
        }
        final List<ChangeBlockEvent> blockEvents = new ArrayList<>();
        final WorldServer minecraftWorld = causeTracker.getMinecraftWorld();
        final IMixinWorldServer mixinWorld = causeTracker.getMixinWorld();

        blockSnapshots.stream()
                .map(getBlockSnapshotTransactionFunction(minecraftWorld, mixinWorld))
                .forEach(getTransactionConsumer(transactionBuilders));

        for (int i = 0; i < GeneralFunctions.EVENT_COUNT; i++) {
            transactionArrays[i] = transactionBuilders[i].build();
        }
        final ChangeBlockEvent[] mainEvents = new ChangeBlockEvent[4];
        final Cause.Builder builder = Cause.source(context.firstNamed(NamedCause.SOURCE, Object.class).get());
        final org.spongepowered.api.world.World world = causeTracker.getWorld();
        iterateChangeBlockEvents(transactionArrays, blockEvents, mainEvents, builder, world);
        if (processMultiEvents(transactionArrays, blockEvents, mainEvents, builder, world)) {
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
                Lists.reverse(blockEvent.getTransactions()).forEach(transaction -> transaction.getOriginal().restore(true, false));
                return;
            } else {
                // Need to undo any invalid changes
                final List<Transaction<BlockSnapshot>> invalid = context.getInvalidTransactions().get();
                blockEvent.getTransactions().forEach(snapshotTransaction -> {
                    if (!snapshotTransaction.isValid()) {
                        invalid.add(snapshotTransaction);
                    } else {
                        unwindingState.markPostNotificationChange(blockChange, minecraftWorld, context, snapshotTransaction);
                    }
                });

                if (invalid.size() > 0) {
                    for (Transaction<BlockSnapshot> transaction : Lists.reverse(invalid)) {
                        transaction.getOriginal().restore(true, false);
                    }
                }

                performBlockAdditions(causeTracker, blockEvent.getTransactions(), blockChange, builder, context);
            }
        }

    }

    private static boolean processMultiEvents(ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays, List<ChangeBlockEvent> blockEvents,
            ChangeBlockEvent[] mainEvents, Cause.Builder builder, org.spongepowered.api.world.World world) {
        if (blockEvents.size() > 1) {
            for (BlockChange blockChange : BlockChange.values()) {
                final ChangeBlockEvent mainEvent = mainEvents[blockChange.ordinal()];
                if (mainEvent != null) {
                    blockChange.suggestNamed(builder, mainEvent);
                }
            }
            final ImmutableList<Transaction<BlockSnapshot>> transactions = transactionArrays[GeneralFunctions.MULTI_CHANGE_INDEX];
            final boolean cancelled = EventConsumer
                    .event(SpongeEventFactory.createChangeBlockEventPost(builder.build(), world, transactions))
                    .cancelled(event -> {
                        // Transactions must be restored in reverse order.
                        Lists.reverse(event.getTransactions()).forEach(transaction -> transaction.getOriginal().restore(true, false));
                    })
                    .process()
                    .isCancelled();
            if (cancelled) {
                return true;
            }
        }
        return false;
    }

    private static Function<BlockSnapshot, Transaction<BlockSnapshot>> getBlockSnapshotTransactionFunction(WorldServer minecraftWorld,
            IMixinWorldServer mixinWorld) {
        return originalSnapshot -> { // Create the transactions
            final BlockPos blockPos = VecHelper.toBlockPos(originalSnapshot.getPosition());
            final IBlockState newState = minecraftWorld.getBlockState(blockPos);
            final IBlockState newActualState = newState.getBlock().getActualState(newState, minecraftWorld, blockPos);
            final BlockSnapshot newSnapshot = mixinWorld.createSpongeBlockSnapshot(newState, newActualState, blockPos, 0);
            return new Transaction<>(originalSnapshot, newSnapshot);
        };
    }
}
