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

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.event.tracking.phase.BlockPhase;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.CaptureType;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

public class TrackingHelper {

    public static boolean doInvalidTransactionsExist(List<Transaction<BlockSnapshot>> invalidTransactions, Iterator<Entity> iter, Entity currentEntity) {
        if (!invalidTransactions.isEmpty()) {
            // check to see if this drop is invalid and if so, remove
            boolean invalid = false;
            for (Transaction<BlockSnapshot> blockSnapshot : invalidTransactions) {
                if (blockSnapshot.getOriginal().getLocation().get().getBlockPosition().equals(currentEntity.getLocation().getBlockPosition())) {
                    invalid = true;
                    iter.remove();
                    break;
                }
            }
            if (invalid) {
                return true;
            }
        }
        return false;
    }

    public static Cause identifyCauses(Cause cause, List<BlockSnapshot> capturedSnapshots, World world) {
        if (!cause.first(User.class).isPresent() && !(capturedSnapshots.size() > 0
                                                      && ((SpongeBlockSnapshot) capturedSnapshots.get(0)).captureType
                                                         == CaptureType.DECAY)) {
            if ((cause.first(BlockSnapshot.class).isPresent() || cause.first(TileEntity.class).isPresent())) {
                // Check for player at pos of first transaction
                Optional<BlockSnapshot> snapshot = cause.first(BlockSnapshot.class);
                Optional<TileEntity> te = cause.first(TileEntity.class);
                BlockPos pos;
                if (snapshot.isPresent()) {
                    pos = VecHelper.toBlockPos(snapshot.get().getPosition());
                } else {
                    pos = ((net.minecraft.tileentity.TileEntity) te.get()).getPos();
                }
                net.minecraft.world.chunk.Chunk chunk = world.getChunkFromBlockCoords(pos);
                if (chunk != null) {
                    IMixinChunk spongeChunk = (IMixinChunk) chunk;

                    Optional<User> owner = spongeChunk.getBlockOwner(pos);
                    Optional<User> notifier = spongeChunk.getBlockNotifier(pos);
                    if (notifier.isPresent() && !cause.containsNamed(NamedCause.NOTIFIER)) {
                        cause = cause.with(NamedCause.notifier(notifier.get()));
                    }
                    if (owner.isPresent() && !cause.containsNamed(NamedCause.OWNER)) {
                        cause = cause.with(NamedCause.owner(owner.get()));
                    }
                }
            } else if (cause.first(Entity.class).isPresent()) {
                Entity entity = cause.first(Entity.class).get();
                if (entity instanceof EntityTameable) {
                    EntityTameable tameable = (EntityTameable) entity;
                    if (tameable.getOwner() != null && !cause.containsNamed(NamedCause.OWNER)) {
                        cause = cause.with(NamedCause.owner(tameable.getOwner()));
                    }
                } else {
                    Optional<User> owner = ((IMixinEntity) entity).getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
                    if (owner.isPresent() && !cause.contains(NamedCause.OWNER)) {
                        cause = cause.with(NamedCause.owner(owner.get()));
                    }
                }
            }
        }
        return cause;
    }

    static void sendItemChangeToPlayer(EntityPlayerMP player) {
        if (StaticMixinHelper.prePacketProcessItem == null) {
            return;
        }

        // handle revert
        player.isChangingQuantityOnly = true;
        player.inventory.mainInventory[player.inventory.currentItem] = StaticMixinHelper.prePacketProcessItem;
        Slot slot = player.openContainer.getSlotFromInventory(player.inventory, player.inventory.currentItem);
        player.openContainer.detectAndSendChanges();
        player.isChangingQuantityOnly = false;
        // force client itemstack update if place event was cancelled
        player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(player.openContainer.windowId, slot.slotNumber,
            StaticMixinHelper.prePacketProcessItem));
    }

    static void processList(CauseTracker causeTracker, ListIterator<Transaction<BlockSnapshot>> listIterator) {
        while (listIterator.hasPrevious()) {
            Transaction<BlockSnapshot> transaction = listIterator.previous();
            causeTracker.push(BlockPhase.State.RESTORING_BLOCKS);
            transaction.getOriginal().restore(true, false);
            causeTracker.pop();
        }
    }

    static boolean shouldChainCause(CauseTracker tracker, Cause cause) {
        return !tracker.isCapturingTerrainGen() && !tracker.isWorldSpawnerRunning() && !tracker.isChunkSpawnerRunning()
               && !tracker.isProcessingBlockRandomTicks() && !tracker.isCaptureCommand() && tracker.hasTickingBlock() && tracker.hasPluginCause()
               && !cause.contains(tracker.getCurrentTickBlock().get());

    }

    public static Tuple<List<EntitySnapshot>, Cause> processSnapshotsForSpawning(Cause cause, org.spongepowered.api.world.World world, List<Entity> capturedEntities, List<Transaction<BlockSnapshot>> invalidTransactions) {
        Iterator<Entity> iter = capturedEntities.iterator();
        ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<>();
        while (iter.hasNext()) {
            Entity currentEntity = iter.next();
            if (TrackingHelper.doInvalidTransactionsExist(invalidTransactions, iter, currentEntity)) {
                continue;
            }
            if (cause.first(User.class).isPresent()) {
                // store user UUID with entity to track later
                User user = cause.first(User.class).get();
                ((IMixinEntity) currentEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, user.getUniqueId());
            } else if (cause.first(Entity.class).isPresent()) {
                IMixinEntity spongeEntity = (IMixinEntity) cause.first(Entity.class).get();
                Optional<User> owner = spongeEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
                if (owner.isPresent() && !cause.containsNamed(NamedCause.OWNER)) {
                    cause = cause.with(NamedCause.of(NamedCause.OWNER, owner.get()));
                    ((IMixinEntity) currentEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, owner.get().getUniqueId());
                }
            }
            entitySnapshotBuilder.add(currentEntity.createSnapshot());
        }

        List<EntitySnapshot> entitySnapshots = entitySnapshotBuilder.build();
        return new Tuple<>(entitySnapshots, cause);
    }
}
