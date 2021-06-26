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

import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.api.item.inventory.crafting.CraftingOutput;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.recipe.Recipe;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.bridge.world.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeManager;

public final class PlaceRecipePacketState extends BasicInventoryPacketState {

    @Override
    public void populateContext(final ServerPlayer playerMP, final Packet<?> packet, final InventoryPacketContext context) {
        ((TrackedInventoryBridge) playerMP.containerMenu).bridge$setCaptureInventory(true);
        ((TrackedContainerBridge) playerMP.containerMenu).bridge$setFirePreview(false);
    }

    @Override
    public void unwind(final InventoryPacketContext context) {
        final ServerboundPlaceRecipePacket packet = context.getPacket();
        final boolean shift = packet.isShiftDown();
        RecipeManager recipeManager = context.getPacketPlayer().server.getRecipeManager();
        final net.minecraft.world.item.crafting.Recipe recipe = recipeManager.byKey(packet.getRecipe()).orElse(null);

        final ServerPlayer player = context.getPacketPlayer();
        ((TrackedContainerBridge)player.containerMenu).bridge$detectAndSendChanges(true);
        ((TrackedInventoryBridge) player.containerMenu).bridge$setCaptureInventory(false);
        ((TrackedContainerBridge) player.containerMenu).bridge$setFirePreview(true);

        final Inventory craftInv = ((Inventory) player.containerMenu).query(QueryTypes.INVENTORY_TYPE.get().of(CraftingInventory.class));
        if (!(craftInv instanceof CraftingInventory)) {
            SpongeCommon.logger().warn("Detected crafting without a InventoryCrafting!? Crafting Event will not fire.");
            return;
        }

        final List<SlotTransaction> previewTransactions = ((TrackedContainerBridge) player.containerMenu).bridge$getPreviewTransactions();
        if (previewTransactions.isEmpty()) {
            final CraftingOutput slot = ((CraftingInventory) craftInv).result();
            final SlotTransaction st = new SlotTransaction(slot, ItemStackSnapshot.empty(), slot.peek().createSnapshot());
            previewTransactions.add(st);
        }
        InventoryEventFactory.callCraftEventPre(player, ((CraftingInventory) craftInv), previewTransactions.get(0),
                ((CraftingRecipe) recipe), player.containerMenu, previewTransactions);
        previewTransactions.clear();

        final Entity spongePlayer = (Entity) player;
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(spongePlayer);
            frame.pushCause(player.containerMenu);

            final List<SlotTransaction> transactions = ((TrackedInventoryBridge) player.containerMenu).bridge$getCapturedSlotTransactions();
            final ItemStackSnapshot cursor = ItemStackUtil.snapshotOf(player.inventory.getCarried());
            final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(cursor, cursor);
            final ClickContainerEvent event;

            if (shift) {
                event = SpongeEventFactory.createClickContainerEventRecipeAll(frame.currentCause(),((Container) player.containerMenu),
                        cursorTransaction, (Recipe) recipe, Optional.empty(), transactions);
            } else {
                event = SpongeEventFactory.createClickContainerEventRecipeSingle(frame.currentCause(), ((Container) player.containerMenu),
                        cursorTransaction, (Recipe) recipe, Optional.empty(), transactions);
            }
            SpongeCommon.post(event);
            if (event.isCancelled() || !event.cursorTransaction().isValid()) {
                PacketPhaseUtil.handleCustomCursor(player, event.cursorTransaction().original());
            } else {
                PacketPhaseUtil.handleCustomCursor(player, event.cursorTransaction().finalReplacement());
            }
            PacketPhaseUtil.handleSlotRestore(player, player.containerMenu, event.transactions(), event.isCancelled());
            event.transactions().clear();
        }
    }
}
