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
package org.spongepowered.test.inventory;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.item.inventory.type.InventoryColumn;
import org.spongepowered.api.item.inventory.type.InventoryRow;
import org.spongepowered.api.plugin.Plugin;

import java.util.Optional;

import javax.inject.Inject;

/**
 * Tests intersect union on and containsInventory
 */
@Plugin(id = "inventorysetoperationstest", name = "Inventory Set Operations Test", description = InventorySetOpsTest.DESCRIPTION, version = "0.0.0")
public class InventorySetOpsTest {

    public static final String DESCRIPTION = "A plugin to test inventory set operations";
    @Inject private Logger logger;

    @Listener
    public void onStart(GameStartedServerEvent event) {
        testIntersect();
        testUnion();
        testSlotOps();
    }

    @SuppressWarnings("deprecation")
    @Listener
    public void onMidas(ChangeInventoryEvent.Held event, @Root Player player) {
        // Checks if Slots are contained in the hotbar then may transform iron to gold
        Inventory hotbar = event.getTargetInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class));
        boolean nugget = false;
        for (SlotTransaction transaction : event.getTransactions()) {
            if (hotbar.containsInventory(transaction.getSlot())) {
                if (ItemTypes.GOLD_NUGGET.equals(transaction.getOriginal().getType())) {
                    nugget = true;
                }
                if (nugget && ItemTypes.IRON_INGOT.equals(transaction.getOriginal().getType())) {
                    transaction.setCustom(ItemStack.of(ItemTypes.GOLD_INGOT, transaction.getOriginal().getQuantity()));
                }
            }
        }
    }

    private void testIntersect() {
        Inventory chest = Inventory.builder().build(this);
        Inventory firstSlots = chest.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(0)));
        Inventory firstRow = chest.query(QueryOperationTypes.INVENTORY_TYPE.of(InventoryRow.class)).children().get(0);
        Inventory firstCol = chest.query(QueryOperationTypes.INVENTORY_TYPE.of(InventoryColumn.class)).children().get(0);
        Inventory intersection = firstSlots.intersect(firstCol).intersect(firstRow);
        Preconditions.checkState(intersection.capacity() == 1, "This should be the first slot only!");
    }

    private void testUnion() {

        Inventory chest = Inventory.builder().build(this);
        Inventory firstSlots = chest.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(0)));
        Inventory firstRow = chest.query(QueryOperationTypes.INVENTORY_TYPE.of(InventoryRow.class)).children().get(0);
        GridInventory grid = (GridInventory) chest.query(QueryOperationTypes.INVENTORY_TYPE.of(GridInventory.class));
        InventoryColumn firstCol = grid.getColumn(0).get();
        InventoryColumn secondCol = grid.getColumn(1).get();
        Inventory union = firstSlots.union(firstCol).union(firstRow);
        Inventory union2 = firstCol.union(firstRow);
        Inventory union3 = firstCol.union(secondCol);
        Preconditions.checkState(union.capacity() == 11, "This should include all eleven slot of the first row and column!");
        Preconditions.checkState(union2.capacity() == 11, "This should include all eleven slot of the first row and column!");
        Preconditions.checkState(union3.capacity() == 6, "This should include all six slot of the first 2 columns!");
    }

    private void testSlotOps() {
        Inventory chest = Inventory.builder().build(this);
        chest.offer(ItemStack.of(ItemTypes.DIAMOND, 10));
        chest.offer(ItemStack.of(ItemTypes.DIRT, 1));
        chest.set(SlotIndex.of(2), ItemStack.of(ItemTypes.DIAMOND, 20));

        ItemStack stack10 = chest.peek();
        Preconditions.checkState(stack10.getQuantity() == 10, "Peeked quantity is not 10");

        Optional<ItemStack> stack20 = chest.peek(SlotIndex.of(2));
        Preconditions.checkState(stack20.get().getQuantity() == 20, "Peeked quantity is not 20");

        ItemStack stack16 = chest.peek(16);
        Preconditions.checkState(stack16.getQuantity() == 16, "Peeked quantity is not 16");

        stack16 = chest.poll(16);
        Preconditions.checkState(stack16.getQuantity() == 16, "Polled quantity is not 16");

        ItemStack stack14 = SlotIndex.of(2).queryIn(chest).peek();
        Preconditions.checkState(stack14.getQuantity() == 14, "Remaining Diamonds is not 14");

        chest.clear();
        Preconditions.checkState(!chest.peek(SlotIndex.of(35)).isPresent(), "Slot does exist");
        Preconditions.checkState(chest.peek().isEmpty(), "Item is not empty");

        chest.offer(ItemStack.of(ItemTypes.DIRT, 1));
        chest.offer(ItemStack.of(ItemTypes.DIAMOND, 10));

        chest.poll();
        stack10 = chest.peek();
        Preconditions.checkState(stack10.getQuantity() == 10, "Peeked quantity is not 10");

    }

}
