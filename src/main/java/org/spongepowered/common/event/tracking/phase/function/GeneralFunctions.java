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
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.EventConsumer;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;

import java.util.ArrayList;
import java.util.List;

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
                .map(originalSnapshot -> { // Create the transactions
                    final BlockPos blockPos = VecHelper.toBlockPos(originalSnapshot.getPosition());
                    final IBlockState newState = minecraftWorld.getBlockState(blockPos);
                    final IBlockState newActualState = newState.getBlock().getActualState(newState, minecraftWorld, blockPos);
                    final BlockSnapshot newSnapshot = mixinWorld.createSpongeBlockSnapshot(newState, newActualState, blockPos, 0);
                    return new Transaction<>(originalSnapshot, newSnapshot);
                })
                .forEach(transaction -> { // Assign the transactions as necessary
                    final BlockChange blockChange = ((SpongeBlockSnapshot) transaction.getOriginal()).blockChange;
                    transactionBuilders[blockChange.ordinal()].add(transaction);
                    transactionBuilders[GeneralFunctions.MULTI_CHANGE_INDEX].add(transaction);
                });

        for (int i = 0; i < GeneralFunctions.EVENT_COUNT; i++) {
            transactionArrays[i] = transactionBuilders[i].build();
        }
        final ChangeBlockEvent[] mainEvents = new ChangeBlockEvent[4];
        final Cause.Builder builder = Cause.source(context.firstNamed(NamedCause.SOURCE, Object.class).get());
        final org.spongepowered.api.world.World world = causeTracker.getWorld();
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
                return;
            }
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
                        context.first(User.class).ifPresent(user -> state.handleBlockChangeWithUser(blockChange, minecraftWorld, snapshotTransaction, context));
                    }
                });

                if (invalid.size() > 0) {
                    for (Transaction<BlockSnapshot> transaction : Lists.reverse(invalid)) {
                        transaction.getOriginal().restore(true, false);
                    }
                }

                // TODO - Any additional changes to the world should take place
                // and get processed in the proceeding UNWINDING state.
                TrackingUtil.performBlockNotifications(causeTracker, blockEvent.getTransactions(), blockChange, builder, context);
            }
        }
    }
}
