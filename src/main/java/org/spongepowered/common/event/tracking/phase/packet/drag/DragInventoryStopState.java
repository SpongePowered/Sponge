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
package org.spongepowered.common.event.tracking.phase.packet.drag;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.world.World;
import org.spongepowered.common.bridge.inventory.ContainerBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.phase.packet.inventory.InventoryPacketContext;
import org.spongepowered.common.item.recipe.crafting.SpongeCraftingRecipeRegistry;
import org.spongepowered.common.util.Constants;

import java.util.List;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;

public abstract class DragInventoryStopState extends NamedInventoryState {

    public DragInventoryStopState(String name, int buttonId) {
        super(name, Constants.Networking.MODE_DRAG | buttonId | Constants.Networking.DRAG_STATUS_STOPPED | Constants.Networking.CLICK_OUTSIDE_WINDOW, Constants.Networking.MASK_DRAG);
    }

    @Override
    public void populateContext(ServerPlayerEntity playerMP, IPacket<?> packet, InventoryPacketContext context) {
        super.populateContext(playerMP, packet, context);
        ((ContainerBridge) playerMP.openContainer).bridge$setFirePreview(false);
    }

    @Override
    public void unwind(InventoryPacketContext context) {
        unwindCraftPreview(context);
        super.unwind(context);
    }

    public static void unwindCraftPreview(InventoryPacketContext context) {
        final ServerPlayerEntity player = context.getPacketPlayer();
        ((ContainerBridge) player.openContainer).bridge$setFirePreview(true);

        Inventory craftInv = ((Inventory) player.openContainer).query(QueryOperationTypes.INVENTORY_TYPE.of(CraftingInventory.class));
        if (craftInv instanceof CraftingInventory) {
            List<SlotTransaction> previewTransactions = ((ContainerBridge) player.openContainer).bridge$getPreviewTransactions();
            if (!previewTransactions.isEmpty()) {
                CraftingRecipe recipe = SpongeCraftingRecipeRegistry
                        .getInstance().findMatchingRecipe(((CraftingInventory) craftInv).getCraftingGrid(), ((World) player.world)).orElse(null);
                SpongeCommonEventFactory.callCraftEventPre(player, ((CraftingInventory) craftInv), previewTransactions.get(0),
                        recipe, player.openContainer, previewTransactions);
                previewTransactions.clear();
            }
        }
    }
}
