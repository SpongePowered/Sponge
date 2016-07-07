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
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.CapturedMultiMapSupplier;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.GeneralPhase;
import org.spongepowered.common.event.tracking.phase.ItemDropData;
import org.spongepowered.common.event.tracking.phase.util.PhaseUtil;
import org.spongepowered.common.interfaces.world.IMixinLocation;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeProxyBlockAccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class GeneralFunctions {

    public static final int BREAK_BLOCK_INDEX = 0;
    public static final int PLACE_BLOCK_INDEX = 1;
    public static final int DECAY_BLOCK_INDEX = 2;
    public static final int CHANGE_BLOCK_INDEX = 3;
    public static final int MULTI_CHANGE_INDEX = 4;

    public static final int EVENT_COUNT = 5;

    public static final BiFunction<WorldServer, BlockSnapshot, Transaction<BlockSnapshot>> TRANSACTION_CREATION = ((worldServer, blockSnapshot) -> {
        final BlockPos blockPos = ((IMixinLocation) (Object) blockSnapshot.getLocation().get()).getBlockPos();
        final IBlockState newState = worldServer.getBlockState(blockPos);
        final IBlockState newActualState = newState.getActualState(worldServer, blockPos);
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

    public static void processUserBreakage(@Nullable BlockChange change, WorldServer minecraftWorld,
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

    /**
     * Processes the given list of {@link BlockSnapshot}s and creates and throws and processes
     * the {@link ChangeBlockEvent}s as appropriately determined based on the {@link BlockChange}
     * for each snapshot. If any transactions are invalid or events cancelled, this event
     * returns {@code false} to signify a transaction was cancelled. This return value
     * is used for portal creation.
     *
     * @param snapshots The snapshots to process
     * @param causeTracker The cause tracker for the world
     * @param state The phase state that is being processed, used to handle marking notifiers
     *  and block owners
     * @param context The phase context, only used by the phase for handling processes.
     * @return True if no events or transactions were cancelled
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean processBlockCaptures(List<BlockSnapshot> snapshots, CauseTracker causeTracker, IPhaseState state, PhaseContext context) {
        if (snapshots.isEmpty()) {
            return false;
        }
        ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays = new ImmutableList[GeneralFunctions.EVENT_COUNT];
        ImmutableList.Builder<Transaction<BlockSnapshot>>[] transactionBuilders = new ImmutableList.Builder[GeneralFunctions.EVENT_COUNT];
        for (int i = 0; i < GeneralFunctions.EVENT_COUNT; i++) {
            transactionBuilders[i] = new ImmutableList.Builder<>();
        }
        final List<ChangeBlockEvent> blockEvents = new ArrayList<>();
        final WorldServer minecraftWorld = causeTracker.getMinecraftWorld();

        for (BlockSnapshot snapshot : snapshots) {
            // This processes each snapshot to assign them to the correct event in the next area, with the
            // correct builder array entry.
            TRANSACTION_PROCESSOR.apply(transactionBuilders).accept(TRANSACTION_CREATION.apply(minecraftWorld, snapshot));
        }

        for (int i = 0; i < GeneralFunctions.EVENT_COUNT; i++) {
            // Build each event array
            transactionArrays[i] = transactionBuilders[i].build();
        }
        final ChangeBlockEvent[] mainEvents = new ChangeBlockEvent[BlockChange.values().length];
        // This likely needs to delegate to the phase in the event we don't use the source object as the main object causing the block changes
        // case in point for WorldTick event listeners since the players are captured non-deterministically
        final Cause.Builder builder = Cause.source(context.firstNamed(NamedCause.SOURCE, Object.class).orElseThrow(PhaseUtil.throwWithContext("There was no root sEmerource object for this phase!", context)));
        state.getPhase().associateAdditionalCauses(state, context, builder, causeTracker);
        final World world = causeTracker.getWorld();
        // Creates the block events accordingly to the transaction arrays
        iterateChangeBlockEvents(transactionArrays, blockEvents, mainEvents, builder, world); // Needs to throw events
        // We create the post event and of course post it in the method, regardless whether any transactions are invalidated or not
        final ChangeBlockEvent.Post postEvent = throwMultiEventsAndCreatePost(transactionArrays, blockEvents, mainEvents, builder, world);

        if (postEvent == null) { // Means that we have had no actual block changes apparently?
            return false;
        }

        final List<Transaction<BlockSnapshot>> invalid = new ArrayList<>();

        boolean noCancelledTransactions = true;

        // Iterate through the block events to mark any transactions as invalid to accumilate after (since the post event contains all
        // transactions of the preceeding block events)
        for (ChangeBlockEvent blockEvent : blockEvents) { // Need to only check if the event is cancelled, If it is, restore
            if (blockEvent.isCancelled()) {
                noCancelledTransactions = false;
                // Don't restore the transactions just yet, since we're just marking them as invalid for now
                for (Transaction<BlockSnapshot> transaction : Lists.reverse(blockEvent.getTransactions())) {
                    transaction.setValid(false);
                }
            }
        }

        // Now we can gather the invalid transactions that either were marked as invalid from an event listener - OR - cancelled.
        // Because after, we will restore all the invalid transactions in reverse order.
        for (Transaction<BlockSnapshot> transaction : postEvent.getTransactions()) {
            if (!transaction.isValid()) {
                invalid.add(transaction);
                // Cancel any block drops performed, avoids any item drops, regardless
                context.getBlockItemDropSupplier().ifPresentAndNotEmpty(map -> {
                    final BlockPos blockPos = ((IMixinLocation) (Object) transaction.getOriginal().getLocation().get()).getBlockPos();
                    map.get(blockPos).clear();
                });
            }
        }

        if (!invalid.isEmpty()) {
            // We need to set this value and return it to signify that some transactions were cancelled
            noCancelledTransactions = false;
            // NOW we restore the invalid transactions (remember invalid transactions are from either plugins marking them as invalid
            // or the events were cancelled), again in reverse order of which they were received.
            for (Transaction<BlockSnapshot> transaction : Lists.reverse(invalid)) {
                transaction.getOriginal().restore(true, BlockChangeFlag.NONE);
                if (state.tracksBlockSpecificDrops()) {
                    // Cancel any block drops or harvests for the block change.
                    // This prevents unnecessary spawns.
                    final BlockPos position = ((IMixinLocation) (Object) transaction.getOriginal().getLocation().get()).getBlockPos();
                    context.getBlockDropSupplier().ifPresentAndNotEmpty(map -> {
                        // Check if the mapping actually has the position to avoid unnecessary
                        // collection creation
                        if (map.containsKey(position)) {
                            map.get(position).clear();
                        }
                    });
                }
            }
        }
        return performBlockAdditions(causeTracker, postEvent.getTransactions(), builder, state, context, noCancelledTransactions);
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
                    SpongeImpl.postEvent(event);
                    blockEvents.add(event);
                }
            }
        }
        if (!transactionArrays[BlockChange.DECAY.ordinal()].isEmpty()) { // Needs to be placed into iterateChangeBlockEvents
            final ChangeBlockEvent event = BlockChange.DECAY.createEvent(builder.build(), world, transactionArrays[BlockChange.DECAY.ordinal()]);
            mainEvents[BlockChange.DECAY.ordinal()] = event;
            if (event != null) {
                SpongeImpl.postEvent(event);
                blockEvents.add(event);
            }
        }
    }

    private static boolean performBlockAdditions(CauseTracker causeTracker, List<Transaction<BlockSnapshot>> transactions, Cause.Builder builder, IPhaseState phaseState, PhaseContext phaseContext, boolean noCancelledTransactions) {
        // We have to use a proxy so that our pending changes are notified such that any accessors from block
        // classes do not fail on getting the incorrect block state from the IBlockAccess
        final WorldServer minecraftWorld = causeTracker.getMinecraftWorld();
        final SpongeProxyBlockAccess proxyBlockAccess = new SpongeProxyBlockAccess(minecraftWorld, transactions);
        final CapturedMultiMapSupplier<BlockPos, ItemDropData> capturedBlockDrops = phaseContext.getBlockDropSupplier();
        final CapturedMultiMapSupplier<BlockPos, EntityItem> capturedBlockItemEntityDrops = phaseContext.getBlockItemDropSupplier();
        for (Transaction<BlockSnapshot> transaction : transactions) {
            if (!transaction.isValid()) {
                // Rememver that this value needs to be set to false to return because of the fact that
                // a transaction was marked as invalid or cancelled. This is used primarily for
                // things like portal creation, and if false, removes the portal from the cache
                noCancelledTransactions = false;
                continue; // Don't use invalidated block transactions during notifications, these only need to be restored
            }
            // Handle custom replacements
            if (transaction.getCustom().isPresent()) {
                transaction.getFinal().restore(true, BlockChangeFlag.NONE);
            }

            final SpongeBlockSnapshot oldBlockSnapshot = (SpongeBlockSnapshot) transaction.getOriginal();
            final SpongeBlockSnapshot newBlockSnapshot = (SpongeBlockSnapshot) transaction.getFinal();
            final Cause currentCause = builder.build();

            // Handle item drops captured
            final BlockPos pos = ((IMixinLocation) (Object) oldBlockSnapshot.getLocation().get()).getBlockPos();
            // This is for pre-merged items
            capturedBlockDrops.ifPresentAndNotEmpty(map -> spawnItemDataForBlockDrops(map.containsKey(pos) ? map.get(pos) : Collections.emptyList(), newBlockSnapshot, causeTracker, phaseContext, phaseState));
            // And this is for un-pre-merged items, these will be EntityItems, not ItemDropDatas.
            capturedBlockItemEntityDrops.ifPresentAndNotEmpty(map -> spawnItemEntitiesForBlockDrops(map.containsKey(pos) ? map.get(pos) : Collections.emptyList(), newBlockSnapshot, causeTracker, phaseContext, phaseState));

            SpongeHooks.logBlockAction(currentCause, minecraftWorld, oldBlockSnapshot.blockChange, transaction);
            BlockChangeFlag changeFlag = oldBlockSnapshot.getChangeFlag();
            IBlockState originalState = (IBlockState) oldBlockSnapshot.getState();
            IBlockState newState = (IBlockState) newBlockSnapshot.getState();
            // Containers get placed automatically
            if (changeFlag.performBlockPhysics() && originalState.getBlock() != newState.getBlock() && !SpongeImplHooks.blockHasTileEntity(newState.getBlock(), newState)) {
                newState.getBlock().onBlockAdded(minecraftWorld, pos, newState);
                final PhaseData peek = causeTracker.getStack().peek();
                if (peek.getState() == GeneralPhase.Post.UNWINDING) {
                    peek.getState().getPhase().unwind(causeTracker, peek.getState(), peek.getContext());
                }
            }

            proxyBlockAccess.proceed();
            phaseState.handleBlockChangeWithUser(oldBlockSnapshot.blockChange, minecraftWorld, transaction, phaseContext);

            if (changeFlag.updateNeighbors()) {
                causeTracker.getMixinWorld().markAndNotifyNeighbors(pos, minecraftWorld.getChunkFromBlockCoords(pos), originalState, newState, oldBlockSnapshot.getUpdateFlag());
            }


            final PhaseData peek = causeTracker.getStack().peek();
            if (peek.getState() == GeneralPhase.Post.UNWINDING) {
                peek.getState().getPhase().unwind(causeTracker, peek.getState(), peek.getContext());
            }
        }
        return noCancelledTransactions;
    }

    public static void spawnItemEntitiesForBlockDrops(Collection<EntityItem> entityItems, SpongeBlockSnapshot newBlockSnapshot,
            CauseTracker causeTracker, PhaseContext phaseContext, IPhaseState phaseState) {
        final Vector3i position = newBlockSnapshot.getPosition();

        // Now we can spawn the entity items appropriately
        final List<Entity> itemDrops = entityItems.stream()
                .map(EntityUtil::fromNative)
                .collect(Collectors.toList());
        final Cause.Builder builder = Cause.source(BlockSpawnCause.builder()
                .block(newBlockSnapshot)
                .type(InternalSpawnTypes.DROPPED_ITEM)
                .build());
        phaseContext.firstNamed(NamedCause.NOTIFIER, User.class).map(NamedCause::notifier).ifPresent(builder::named);
        final Cause spawnCauses = builder.build();
        final DropItemEvent.Destruct
                destruct =
                SpongeEventFactory.createDropItemEventDestruct(spawnCauses, itemDrops, causeTracker.getWorld());
        SpongeImpl.postEvent(destruct);
        if (!destruct.isCancelled()) {
            for (Entity entity : destruct.getEntities()) {
                TrackingUtil.associateEntityCreator(phaseContext, EntityUtil.toNative(entity), causeTracker.getMinecraftWorld());
                causeTracker.getMixinWorld().forceSpawnEntity(entity);
            }
        }
    }

    public static void spawnItemDataForBlockDrops(Collection<ItemDropData> itemStacks, SpongeBlockSnapshot oldBlockSnapshot, CauseTracker causeTracker,
            PhaseContext phaseContext, IPhaseState state) {
        final Vector3i position = oldBlockSnapshot.getPosition();
        final List<ItemStackSnapshot> itemSnapshots = itemStacks.stream()
                .map(ItemDropData::getStack)
                .map(ItemStackUtil::createSnapshot)
                .collect(Collectors.toList());
        final ImmutableList<ItemStackSnapshot> originalSnapshots = ImmutableList.copyOf(itemSnapshots);
        final Cause cause = Cause.source(oldBlockSnapshot).build();
        final DropItemEvent.Pre dropItemEventPre = SpongeEventFactory.createDropItemEventPre(cause, originalSnapshots, itemSnapshots);
        SpongeImpl.postEvent(dropItemEventPre);
        if (dropItemEventPre.isCancelled()) {
            itemStacks.clear();
        }
        if (itemStacks.isEmpty()) {
            return;
        }
        // Now we can spawn the entity items appropriately
        final List<Entity> itemDrops = itemStacks.stream().map(itemStack -> {
                    final net.minecraft.item.ItemStack minecraftStack = itemStack.getStack();
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
        final DropItemEvent.Destruct
                destruct =
                SpongeEventFactory.createDropItemEventDestruct(spawnCauses, itemDrops, causeTracker.getWorld());
        SpongeImpl.postEvent(destruct);
        if (!destruct.isCancelled()) {
            for (Entity entity : destruct.getEntities()) {
                TrackingUtil.associateEntityCreator(phaseContext, EntityUtil.toNative(entity), causeTracker.getMinecraftWorld());
                causeTracker.getMixinWorld().forceSpawnEntity(entity);
            }
        }
    }

    public static ChangeBlockEvent.Post throwMultiEventsAndCreatePost(ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays, List<ChangeBlockEvent> blockEvents,
            ChangeBlockEvent[] mainEvents, Cause.Builder builder, World world) {
        if (!blockEvents.isEmpty()) {
            for (BlockChange blockChange : BlockChange.values()) {
                final ChangeBlockEvent mainEvent = mainEvents[blockChange.ordinal()];
                if (mainEvent != null) {
                    blockChange.suggestNamed(builder, mainEvent);
                }
            }
            final ImmutableList<Transaction<BlockSnapshot>> transactions = transactionArrays[GeneralFunctions.MULTI_CHANGE_INDEX];
            final ChangeBlockEvent.Post post = SpongeEventFactory.createChangeBlockEventPost(builder.build(), world, transactions);
            SpongeImpl.postEvent(post);
            return post;
        }
        return null;
    }

}
