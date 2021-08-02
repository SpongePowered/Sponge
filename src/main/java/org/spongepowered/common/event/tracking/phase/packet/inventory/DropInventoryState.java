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
package org.spongepowered.common.event.tracking.phase.packet.inventory;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.bridge.world.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.transaction.GameTransaction;
import org.spongepowered.common.event.tracking.context.transaction.SpawnEntityTransaction;
import org.spongepowered.common.item.util.ItemStackUtil;

import com.google.common.collect.ImmutableList;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class DropInventoryState extends BasicInventoryPacketState {

    @Override
    public BiConsumer<CauseStackManager.StackFrame, InventoryPacketContext> getFrameModifier() {
        return super.getFrameModifier().andThen((frame, ctx) -> {
            final ItemStack usedStack = ctx.getItemUsed();
            final ItemStackSnapshot usedSnapshot = ItemStackUtil.snapshotOf(usedStack);
            if (!usedSnapshot.isEmpty()) {
                frame.addContext(EventContextKeys.USED_ITEM, usedSnapshot);
            }
        });
    }

    @Override
    public void unwind(final InventoryPacketContext context) {
        final ServerPlayer player = context.getPacketPlayer();

        TrackingUtil.processBlockCaptures(context);
        final TrackedInventoryBridge mixinContainer = (TrackedInventoryBridge) player.containerMenu;
        mixinContainer.bridge$setCaptureInventory(false);
        mixinContainer.bridge$getCapturedSlotTransactions().clear();
    }

    @Override
    public SpawnEntityEvent createSpawnEvent(
        final InventoryPacketContext context, final GameTransaction<@NonNull ?> parent,
        final ImmutableList<Tuple<net.minecraft.world.entity.Entity, SpawnEntityTransaction.DummySnapshot>> collect,
        final Cause currentCause
    ) {
        return SpongeEventFactory.createDropItemEventDispense(currentCause, collect.stream()
            .map(Tuple::first).map(entity -> (Entity) entity)
            .collect(ImmutableList.toImmutableList())
        );
    }

    @Override
    public Supplier<SpawnType> getSpawnTypeForTransaction(
        final InventoryPacketContext context, final net.minecraft.world.entity.Entity entityToSpawn
    ) {
        return SpawnTypes.DROPPED_ITEM;
    }
}
