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

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.EventConsumer;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.util.PhaseUtil;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeProxyBlockAccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GeneralFunctions {

    public static final int BREAK_BLOCK_INDEX = 0;
    public static final int PLACE_BLOCK_INDEX = 1;
    public static final int DECAY_BLOCK_INDEX = 2;
    public static final int CHANGE_BLOCK_INDEX = 3;
    public static final int MULTI_CHANGE_INDEX = 4;

    public static final int EVENT_COUNT = 5;

    public static final BiFunction<WorldServer, BlockSnapshot, Transaction<BlockSnapshot>> TRANSACTION_CREATION = ((worldServer, blockSnapshot) -> {
        final BlockPos blockPos = VecHelper.toBlockPos(blockSnapshot.getPosition());
        final IBlockState newState = worldServer.getBlockState(blockPos);
        final IBlockState newActualState = newState.getBlock().getActualState(newState, worldServer, blockPos);
        final BlockSnapshot newSnapshot = ((IMixinWorldServer) worldServer).createSpongeBlockSnapshot(newState, newActualState, blockPos, 0);
        return new Transaction<>(blockSnapshot, newSnapshot);
    });

    public static final Function<ImmutableList.Builder<Transaction<BlockSnapshot>>[], Consumer<Transaction<BlockSnapshot>>> TRANSACTION_PROCESSOR =
            builders ->
                    transaction -> {
                        final BlockChange blockChange = ((SpongeBlockSnapshot) transaction.getOriginal()).blockChange;
                        builders[blockChange.ordinal()].add(transaction);
                        builders[GeneralFunctions.MULTI_CHANGE_INDEX].add(transaction);
                    }
            ;

    public static void processUserBreakage(@Nullable BlockChange change, net.minecraft.world.World minecraftWorld,
            Transaction<BlockSnapshot> transaction, @Nullable Entity tickingEntity) {
        if (change == BlockChange.BREAK) {
            final BlockPos blockPos = VecHelper.toBlockPos(transaction.getOriginal().getPosition());
            for (EntityHanging entityHanging : EntityUtil.findHangingEntities(minecraftWorld, blockPos)) {
                if (entityHanging instanceof EntityItemFrame) {
                    final EntityItemFrame frame = (EntityItemFrame) entityHanging;
                    if (tickingEntity != null) {
                        frame.dropItemOrSelf(EntityUtil.toNative(tickingEntity), true);
                    }
                    frame.setDead();
                }
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void processBlockCaptures(List<BlockSnapshot> snapshots, CauseTracker causeTracker, IPhaseState state, PhaseContext context) {
        if (snapshots.isEmpty()) {
            return;
        }
        ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays = new ImmutableList[GeneralFunctions.EVENT_COUNT];
        ImmutableList.Builder<Transaction<BlockSnapshot>>[] transactionBuilders = new ImmutableList.Builder[GeneralFunctions.EVENT_COUNT];
        for (int i = 0; i < GeneralFunctions.EVENT_COUNT; i++) {
            transactionBuilders[i] = new ImmutableList.Builder<>();
        }
        final List<ChangeBlockEvent> blockEvents = new ArrayList<>();
        final WorldServer minecraftWorld = causeTracker.getMinecraftWorld();

        for (BlockSnapshot snapshot : snapshots) {
            TRANSACTION_PROCESSOR.apply(transactionBuilders).accept(TRANSACTION_CREATION.apply(minecraftWorld, snapshot));
        }

        for (int i = 0; i < GeneralFunctions.EVENT_COUNT; i++) {
            transactionArrays[i] = transactionBuilders[i].build();
        }
        final ChangeBlockEvent[] mainEvents = new ChangeBlockEvent[BlockChange.values().length];
        final Cause.Builder builder = Cause.source(context.firstNamed(NamedCause.SOURCE, Object.class).orElseThrow(PhaseUtil.throwWithContext("There was no root sEmerource object for this phase!", context)));
        state.getPhase().associateAdditionalCauses(state, context, builder, causeTracker);
        final World world = causeTracker.getWorld();
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
                for (Transaction<BlockSnapshot> transaction : Lists.reverse(blockEvent.getTransactions())) {
                    transaction.getOriginal().restore(true, false);
                }
                return;
            } else {
                // Need to undo any invalid changes
                final List<Transaction<BlockSnapshot>> invalid = context.getInvalidTransactions()
                        .orElseThrow(PhaseUtil.throwWithContext("Not capturing invalid transactions!", context));
                for (Transaction<BlockSnapshot> transaction : blockEvent.getTransactions()) {
                    if (!transaction.isValid()) {
                        invalid.add(transaction);
                    } else {
                        state.handleBlockChangeWithUser(blockChange, minecraftWorld, transaction, context);
                    }
                }

                if (!invalid.isEmpty()) {
                    for (Transaction<BlockSnapshot> transaction : Lists.reverse(invalid)) {
                        transaction.getOriginal().restore(true, false);
                        if (state.tracksBlockSpecificDrops()) {
                            final BlockPos position = VecHelper.toBlockPos(transaction.getOriginal().getPosition());
                            final Multimap<BlockPos, ItemStack> multiMap = context.getBlockDropSupplier()
                                    .orElseThrow(PhaseUtil.throwWithContext("State is not allowing block specific item drop capturing!", context))
                                    .get();
                            multiMap.get(position).clear();
                        }
                    }
                }

                performBlockAdditions(causeTracker, blockEvent.getTransactions(), blockChange, builder, state, context);
            }
        }
    }

    public static void iterateChangeBlockEvents(ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays, List<ChangeBlockEvent> blockEvents,
            ChangeBlockEvent[] mainEvents, Cause.Builder builder, World world) {
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

    private static void performBlockAdditions(CauseTracker causeTracker, List<Transaction<BlockSnapshot>> transactions, @Nullable BlockChange type, Cause.Builder builder, IPhaseState phaseState, PhaseContext phaseContext) {
        // We have to use a proxy so that our pending changes are notified such that any accessors from block
        // classes do not fail on getting the incorrect block state from the IBlockAccess
        final WorldServer minecraftWorld = causeTracker.getMinecraftWorld();
        final SpongeProxyBlockAccess proxyBlockAccess = new SpongeProxyBlockAccess(minecraftWorld, transactions);
        final PhaseContext.CapturedMultiMapSupplier<BlockPos, ItemStack> capturedBlockDrops = phaseContext.getBlockDropSupplier().orElse(null);
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
                spawnItemStacksForBlockDrop(capturedBlockDrops.get().get(VecHelper.toBlockPos(oldBlockSnapshot.getPosition())), newBlockSnapshot,
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

            // Here is where we need to re-post process Depth First

        }
    }

    public static void spawnItemStacksForBlockDrop(Collection<ItemStack> itemStacks, SpongeBlockSnapshot oldBlockSnapshot, CauseTracker causeTracker,
            PhaseContext phaseContext, IPhaseState state) {
        final Vector3i position = oldBlockSnapshot.getPosition();
        final List<ItemStackSnapshot> itemSnapshots = itemStacks.stream().map(ItemStack::createSnapshot).collect(Collectors.toList());
        final ImmutableList<ItemStackSnapshot> originalSnapshots = ImmutableList.copyOf(itemSnapshots);
        final Cause cause = Cause.source(oldBlockSnapshot).build();
        EventConsumer.event(SpongeEventFactory.createDropItemEventPre(cause, originalSnapshots, itemSnapshots))
                .cancelled(event -> itemStacks.clear())
                .process();

        if (itemStacks.isEmpty()) {
            return;
        }
        // Now we can spawn the entity items appropriately
        final List<Entity> itemDrops = itemStacks.stream().map(itemStack -> {
                    final net.minecraft.item.ItemStack minecraftStack = ItemStackUtil.toNative(itemStack);
                    final WorldServer minecraftWorld = causeTracker.getMinecraftWorld();
                    float f = 0.5F;
                    double offsetX = (double) (minecraftWorld.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    double offsetY = (double) (minecraftWorld.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    double offsetZ = (double) (minecraftWorld.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    final double x = (double) position.getX() + offsetX;
                    final double y = (double) position.getY() + offsetY;
                    final double z = (double) position.getZ() + offsetZ;
                    EntityItem entityitem = new EntityItem(minecraftWorld, x, y, z, minecraftStack);
                    entityitem.setDefaultPickupDelay();
                    return entityitem;
                })
                .map(EntityUtil::fromNative)
                .collect(Collectors.toList());
        final Cause.Builder builder = Cause.source(BlockSpawnCause.builder()
                .block(oldBlockSnapshot)
                .type(InternalSpawnTypes.DROPPED_ITEM)
                .build());
        phaseContext.firstNamed(NamedCause.NOTIFIER, User.class).map(NamedCause::notifier).ifPresent(builder::named);
        final Cause spawnCauses = builder.build();
        EventConsumer.event(SpongeEventFactory.createDropItemEventDestruct(spawnCauses, itemDrops, causeTracker.getWorld()))
                .nonCancelled(event -> {
                            for (Entity entity : event.getEntities()) {
                                TrackingUtil.associateEntityCreator(phaseContext, EntityUtil.toNative(entity), causeTracker.getMinecraftWorld());
                                causeTracker.getMixinWorld().forceSpawnEntity(entity);
                            }
                        }
                )
                .process();

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void processPostBlockCaptures(List<BlockSnapshot> blockSnapshots, CauseTracker causeTracker, IPhaseState unwindingState,
            PhaseContext context) {
        ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays = new ImmutableList[GeneralFunctions.EVENT_COUNT];
        ImmutableList.Builder<Transaction<BlockSnapshot>>[] transactionBuilders = new ImmutableList.Builder[GeneralFunctions.EVENT_COUNT];
        for (int i = 0; i < GeneralFunctions.EVENT_COUNT; i++) {
            transactionBuilders[i] = new ImmutableList.Builder<>();
        }
        final List<ChangeBlockEvent> blockEvents = new ArrayList<>();
        final WorldServer minecraftWorld = causeTracker.getMinecraftWorld();

        for (BlockSnapshot snapshot : blockSnapshots) {
            TRANSACTION_PROCESSOR.apply(transactionBuilders).accept(TRANSACTION_CREATION.apply(minecraftWorld, snapshot));
        }

        for (int i = 0; i < GeneralFunctions.EVENT_COUNT; i++) {
            transactionArrays[i] = transactionBuilders[i].build();
        }
        final ChangeBlockEvent[] mainEvents = new ChangeBlockEvent[4];
        final Cause.Builder builder = Cause.source(context.firstNamed(NamedCause.SOURCE, Object.class).get());
        final World world = causeTracker.getWorld();
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
                for (Transaction<BlockSnapshot> transaction : Lists.reverse(blockEvent.getTransactions())) {
                    transaction.getOriginal().restore(true, false);
                }
                return;
            } else {
                // Need to undo any invalid changes
                final List<Transaction<BlockSnapshot>> invalid = context.getInvalidTransactions()
                        .orElseThrow(PhaseUtil.throwWithContext("Expected to have invalid transactions on an event, but had none!", context));
                for (Transaction<BlockSnapshot> snapshotTransaction : blockEvent.getTransactions()) {
                    if (!snapshotTransaction.isValid()) {
                        invalid.add(snapshotTransaction);
                    } else {
                        unwindingState.markPostNotificationChange(blockChange, minecraftWorld, context, snapshotTransaction);
                    }
                }

                if (invalid.size() > 0) {
                    for (Transaction<BlockSnapshot> transaction : Lists.reverse(invalid)) {
                        transaction.getOriginal().restore(true, false);
                    }
                }

                performBlockAdditions(causeTracker, blockEvent.getTransactions(), blockChange, builder, unwindingState, context);
            }
        }

    }

    public static boolean processMultiEvents(ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays, List<ChangeBlockEvent> blockEvents,
            ChangeBlockEvent[] mainEvents, Cause.Builder builder, World world) {
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
                        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
                            transaction.getOriginal().restore(true, false);
                        }
                    })
                    .process()
                    .isCancelled();
            if (cancelled) {
                return true;
            }
        }
        return false;
    }

}
