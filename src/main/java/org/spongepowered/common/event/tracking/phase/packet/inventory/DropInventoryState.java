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

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.ArrayList;

public final class DropInventoryState extends BasicInventoryPacketState {

    @Override
    public void unwind(InventoryPacketContext context) {
        final EntityPlayerMP player = context.getPacketPlayer();
        final ItemStack usedStack = context.getItemUsed();
        final ItemStackSnapshot usedSnapshot = ItemStackUtil.snapshotOf(usedStack);
        final Entity spongePlayer = EntityUtil.fromNative(player);
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(spongePlayer);
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
            // TODO - Determine if we need to pass the supplier or perform some parameterized
            //  process if not empty method on the capture object.
            TrackingUtil.processBlockCaptures(context.getCapturedBlockSupplier(), this, context);
            context.getCapturedItemsSupplier()
                .acceptAndClearIfNotEmpty(items -> {

                    final ArrayList<Entity> entities = new ArrayList<>();
                    for (EntityItem item : items) {
                        entities.add(EntityUtil.fromNative(item));
                    }
                    final DropItemEvent.Dispense dropItemEvent =
                        SpongeEventFactory.createDropItemEventDispense(Sponge.getCauseStackManager().getCurrentCause(), entities);
                    SpongeImpl.postEvent(dropItemEvent);
                    if (!dropItemEvent.isCancelled()) {
                        processSpawnedEntities(player, dropItemEvent);
                    }
                });

            final IMixinContainer mixinContainer = ContainerUtil.toMixin(player.openContainer);
            mixinContainer.setCaptureInventory(false);
            mixinContainer.getCapturedTransactions().clear();
        }

    }
}
