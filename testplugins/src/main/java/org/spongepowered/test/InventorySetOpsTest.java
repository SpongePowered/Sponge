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
package org.spongepowered.test;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.item.inventory.type.InventoryColumn;
import org.spongepowered.api.item.inventory.type.InventoryRow;
import org.spongepowered.api.plugin.Plugin;

import javax.inject.Inject;

/**
 * Tests intersect union and containsInventory
 */
@Plugin(id = "inventorysetoperationstest", name = "Inventory Set Operations Test", description = "A plugin to test inventory set operations")
public class InventorySetOpsTest {

    @Inject private Logger logger;

    @Listener
    public void onStart(GameStartedServerEvent event) {
        testIntersect();

    }

    @Listener
    public void onCmd(SendCommandEvent event)
    {
        testIntersect(); // TODO remove me once this is all working
    }

    private void testIntersect() {
        Inventory chest = Inventory.builder().build(this);
        Inventory firstSlots = chest.query(SlotIndex.of(0));
        Inventory firstRow = chest.query(InventoryRow.class).first(); // TODO is the query supposed to return the entire grid?
        Inventory firstCol = chest.query(InventoryColumn.class).first();
        GridInventory grid = chest.query(GridInventory.class);
        //InventoryColumn firstCol = grid.getColumn(0).get();
        //InventoryRow firstRow = grid.getRow(0).get();
        Inventory intersection = firstSlots.intersect(firstCol).intersect(firstRow);
        Preconditions.checkArgument(intersection.capacity() == 1, "This should be the first slot only!");
        logger.info("Intersect works!");
    }


}
