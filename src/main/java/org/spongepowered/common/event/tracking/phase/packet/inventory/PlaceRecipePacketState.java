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

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.network.IPacket;
import org.spongepowered.api.Sponge;
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
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.bridge.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.item.util.ItemStackUtil;
import java.util.List;
import java.util.Optional;

public final class PlaceRecipePacketState extends BasicInventoryPacketState {

    @Override
    public void populateContext(final ServerPlayerEntity playerMP, final IPacket<?> packet, final InventoryPacketContext context) {
        ((TrackedInventoryBridge) playerMP.openContainer).bridge$setCaptureInventory(true);
        ((TrackedContainerBridge) playerMP.openContainer).bridge$setFirePreview(false);
    }

    @Override
    public void unwind(final InventoryPacketContext context) {
        final CPacketPlaceRecipe packet = context.getPacket();
        final boolean shift = packet.shouldPlaceAll();
        RecipeManager recipeManager = context.getPacketPlayer().server.getRecipeManager();
        final IRecipe recipe = recipeManager.getRecipe(packet.getRecipeId());

        final ServerPlayerEntity player = context.getPacketPlayer();
        ((TrackedContainerBridge)player.openContainer).bridge$detectAndSendChanges(true);
        ((TrackedInventoryBridge) player.openContainer).bridge$setCaptureInventory(false);
        ((TrackedContainerBridge) player.openContainer).bridge$setFirePreview(true);

        final Inventory craftInv = ((Inventory) player.openContainer).query(QueryTypes.INVENTORY_TYPE.of(CraftingInventory.class));
        if (!(craftInv instanceof CraftingInventory)) {
            SpongeImpl.getLogger().warn("Detected crafting without a InventoryCrafting!? Crafting Event will not fire.");
            return;
        }

        final List<SlotTransaction> previewTransactions = ((TrackedContainerBridge) player.openContainer).bridge$getPreviewTransactions();
        if (previewTransactions.isEmpty()) {
            final CraftingOutput slot = ((CraftingInventory) craftInv).getResult();
            final SlotTransaction st = new SlotTransaction(slot, ItemStackSnapshot.empty(), slot.peek().createSnapshot());
            previewTransactions.add(st);
        }
        InventoryEventFactory.callCraftEventPre(player, ((CraftingInventory) craftInv), previewTransactions.get(0),
                ((CraftingRecipe) recipe), player.openContainer, previewTransactions);
        previewTransactions.clear();

        final Entity spongePlayer = (Entity) player;
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(spongePlayer);
            frame.pushCause(player.openContainer);

            final List<SlotTransaction> transactions = ((TrackedInventoryBridge) player.openContainer).bridge$getCapturedSlotTransactions();
            final ItemStackSnapshot cursor = ItemStackUtil.snapshotOf(player.inventory.getItemStack());
            final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(cursor, cursor);
            final ClickContainerEvent event;

            if (shift) {
                event = SpongeEventFactory.createClickContainerEventRecipeAll(frame.getCurrentCause(),((Container) player.openContainer),
                        cursorTransaction, (Recipe) recipe, Optional.empty(), transactions);
            } else {
                event = SpongeEventFactory.createClickContainerEventRecipeSingle(frame.getCurrentCause(), ((Container) player.openContainer),
                        cursorTransaction, (Recipe) recipe, Optional.empty(), transactions);
            }
            SpongeImpl.postEvent(event);
            if (event.isCancelled() || !event.getCursorTransaction().isValid()) {
                PacketPhaseUtil.handleCustomCursor(player, event.getCursorTransaction().getOriginal());
            } else {
                PacketPhaseUtil.handleCustomCursor(player, event.getCursorTransaction().getFinal());
            }
            PacketPhaseUtil.handleSlotRestore(player, player.openContainer, event.getTransactions(), event.isCancelled());
            event.getTransactions().clear();
        }
    }
}
