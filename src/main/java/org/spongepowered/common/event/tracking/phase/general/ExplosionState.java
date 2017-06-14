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

import static org.spongepowered.common.event.tracking.TrackingUtil.iterateChangeBlockEvents;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.world.IMixinExplosion;
import org.spongepowered.common.interfaces.world.IMixinLocation;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

final class ExplosionState extends GeneralState {
    @Override
    public boolean canSwitchTo(IPhaseState state) {
        return true;
    }

    @Override
    public boolean tracksEntitySpecificDrops() {
        return true;
    }

    @Override
    public boolean tracksBlockSpecificDrops() {
        return true;
    }

    @Override
    public boolean requiresBlockPosTracking() {
        return true;
    }

    @Override
    void unwind(PhaseContext context) {
        final Optional<Explosion> explosion = context.getCaptureExplosion().getExplosion();
        if (!explosion.isPresent()) { // More than likely never will happen
            return;
        }
        final Cause cause = ((IMixinExplosion) explosion.get()).getCreatedCause();
        context.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(blocks -> processBlockCaptures(blocks, explosion.get(), cause, context));
        context.getCapturedEntitySupplier()
                .ifPresentAndNotEmpty(entities -> {
                    final Cause.Builder builder = Cause.builder();
                    final Object root = cause.root();
                    if (root instanceof Entity) {
                        builder.named(NamedCause.source(EntitySpawnCause
                                .builder()
                                .entity((Entity) root)
                                .type(InternalSpawnTypes.TNT_IGNITE)
                                .build()
                                )
                        );
                    } else if (root instanceof BlockSnapshot) {
                        builder.named(NamedCause.source(BlockSpawnCause
                                .builder()
                                .block((BlockSnapshot) root)
                                .type(InternalSpawnTypes.TNT_IGNITE)
                                .build()
                                )
                        );
                    } else {
                        builder.named(NamedCause.source(SpawnCause
                                .builder()
                                .type(InternalSpawnTypes.TNT_IGNITE)
                                .build()
                                )
                        );
                    }

                    context.getNotifier().ifPresent(builder::notifier);
                    context.getOwner().ifPresent(builder::owner);
                    builder.named(NamedCause.of("Explosion", explosion.get()));
                    final User user = context.getNotifier().orElseGet(() -> context.getOwner().orElse(null));
                    final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(builder.build(), entities);
                    SpongeImpl.postEvent(event);
                    if (!event.isCancelled()) {
                        for (Entity entity : event.getEntities()) {
                            if (user != null) {
                                EntityUtil.toMixin(entity).setCreator(user.getUniqueId());
                            }
                            EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);

                        }
                    }


                });

    }

    @SuppressWarnings({"unchecked"})
    private void processBlockCaptures(List<BlockSnapshot> snapshots, Explosion explosion, Cause cause, PhaseContext context) {
        if (snapshots.isEmpty()) {
            return;
        }
        ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays = new ImmutableList[TrackingUtil.EVENT_COUNT];
        ImmutableList.Builder<Transaction<BlockSnapshot>>[] transactionBuilders = new ImmutableList.Builder[TrackingUtil.EVENT_COUNT];
        for (int i = 0; i < TrackingUtil.EVENT_COUNT; i++) {
            transactionBuilders[i] = new ImmutableList.Builder<>();
        }
        final List<ChangeBlockEvent> blockEvents = new ArrayList<>();

        for (BlockSnapshot snapshot : snapshots) {
            // This processes each snapshot to assign them to the correct event in the next area, with the
            // correct builder array entry.
            TrackingUtil.TRANSACTION_PROCESSOR.apply(transactionBuilders).accept(TrackingUtil.TRANSACTION_CREATION.apply(snapshot));
        }
        for (int i = 0; i < TrackingUtil.EVENT_COUNT; i++) {
            // Build each event array
            transactionArrays[i] = transactionBuilders[i].build();
        }
        final ChangeBlockEvent[] mainEvents = new ChangeBlockEvent[BlockChange.values().length];
        // This likely needs to delegate to the phase in the event we don't use the source object as the main object causing the block changes
        // case in point for WorldTick event listeners since the players are captured non-deterministically
        final Cause.Builder builder = Cause.source(context.getSource(Object.class)
                .orElseThrow(TrackingUtil.throwWithContext("There was no root source object for this phase!", context))
        );
        context.getNotifier().ifPresent(builder::notifier);
        context.getOwner().ifPresent(builder::owner);
        try {
            this.getPhase().associateAdditionalCauses(this, context, builder);
        } catch (Exception e) {
            // TODO - this should be a thing to associate additional objects in the cause, or context, but for now it's just a simple
            // try catch to avoid bombing on performing block changes.
        }
        // Creates the block events accordingly to the transaction arrays
        iterateChangeBlockEvents(transactionArrays, blockEvents, mainEvents, builder); // Needs to throw events
        // We create the post event and of course post it in the method, regardless whether any transactions are invalidated or not

        // Copied from TrackingUtil#throwMultiEventsAndCreatePost
        for (BlockChange blockChange : BlockChange.values()) {
            final ChangeBlockEvent mainEvent = mainEvents[blockChange.ordinal()];
            if (mainEvent != null) {
                blockChange.suggestNamed(builder, mainEvent);
            }
        }
        final ImmutableList<Transaction<BlockSnapshot>> transactions = transactionArrays[TrackingUtil.MULTI_CHANGE_INDEX];

        final ExplosionEvent.Post postEvent = SpongeEventFactory.createExplosionEventPost(cause, explosion, transactions);
        if (postEvent == null) { // Means that we have had no actual block changes apparently?
            return;
        }
        SpongeImpl.postEvent(postEvent);

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

        // Finally check the post event
        if (postEvent.isCancelled()) {
            // Of course, if post is cancelled, just mark all transactions as invalid.
            noCancelledTransactions = false;
            for (Transaction<BlockSnapshot> transaction : postEvent.getTransactions()) {
                transaction.setValid(false);
            }
        }

        // Now we can gather the invalid transactions that either were marked as invalid from an event listener - OR - cancelled.
        // Because after, we will restore all the invalid transactions in reverse order.
        for (Transaction<BlockSnapshot> transaction : postEvent.getTransactions()) {
            if (!transaction.isValid()) {
                invalid.add(transaction);
                final BlockPos blockPos = ((IMixinLocation) (Object) transaction.getOriginal().getLocation().get()).getBlockPos();

                // Cancel any block drops performed, avoids any item drops, regardless
                context.getBlockItemDropSupplier().ifPresentAndNotEmpty(map -> {
                    if (map.containsKey(blockPos)) {
                        map.get(blockPos).clear();
                    }
                });
                context.getBlockEntitySpawnSupplier().ifPresentAndNotEmpty(map -> {
                    if (map.containsKey(blockPos)) {
                        map.get(blockPos).clear();
                    }
                });
                context.getBlockEntitySpawnSupplier().ifPresentAndNotEmpty(blockPosEntityMultimap -> {
                    if (blockPosEntityMultimap.containsKey(blockPos)) {
                        blockPosEntityMultimap.get(blockPos).clear();
                    }
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
                if (this.tracksBlockSpecificDrops()) {
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
        TrackingUtil.performBlockAdditions(postEvent.getTransactions(), builder, this, context, noCancelledTransactions);
    }

    @Override
    public boolean shouldCaptureBlockChangeOrSkip(PhaseContext phaseContext,
        BlockPos pos) {
        boolean match = false;
        final Vector3i blockPos = VecHelper.toVector3i(pos);
        for (final Iterator<BlockSnapshot> iterator = phaseContext.getCapturedBlocks().iterator(); iterator.hasNext(); ) {
            final BlockSnapshot capturedSnapshot = iterator.next();
            if (capturedSnapshot.getPosition().equals(blockPos)) {
                match = true;
            }
        }
        return !match;
    }

    @Override
    public boolean spawnEntityOrCapture(PhaseContext context, Entity entity, int chunkX, int chunkZ) {
        final BlockPos blockPos = context.getBlockPosition()
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be capturing a block position during an explosion!", context));
        final Multimap<BlockPos, net.minecraft.entity.Entity> blockPosEntityMultimap = context.getBlockEntitySpawnSupplier().get();
        final Multimap<BlockPos, EntityItem> blockPosEntityItemMultimap = context.getBlockItemDropSupplier().get();
        if (entity instanceof EntityItem) {
            blockPosEntityItemMultimap.put(blockPos, (EntityItem) entity);
        } else {
            blockPosEntityMultimap.put(blockPos, (net.minecraft.entity.Entity) entity);
        }
        return true;
    }
}
