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
import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ContainerTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.slot.SlotMatchers;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.item.inventory.type.InventoryColumn;
import org.spongepowered.api.item.inventory.type.InventoryRow;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.channel.MessageReceiver;


/**
 * Tests intersect union on and containsInventory
 */
@Plugin(id = "inventorysetoperationstest", name = "Inventory Set Operations Test", description = InventorySetOpsTest.DESCRIPTION, version = "0.0.0")
public class InventorySetOpsTest implements LoadableModule {

    public static final String DESCRIPTION = "A plugin to test inventory set operations";
    @Inject private PluginContainer container;

    private final InventorySetOpsListener listener = new InventorySetOpsListener();

    @Listener
    public void onStart(GameStartedServerEvent event) {
        this.testIntersect();
        this.testUnion();
    }

    @SuppressWarnings("deprecation")
    private void testIntersect() {
        ViewableInventory chest = ViewableInventory.builder().type(ContainerTypes.GENERIC_9x3.get()).completeStructure().build();

        Inventory firstSlots = chest.query(SlotMatchers.index(0));
        Inventory firstRow = chest.query(InventoryRow.class).get().children().get(0);
        Inventory firstCol = chest.query(InventoryColumn.class).get().children().get(0);
        Inventory intersection = firstSlots.intersect(firstCol).intersect(firstRow);
        Preconditions.checkState(intersection.capacity() == 1, "This should be the first slot only!");
    }

    @SuppressWarnings("deprecation")
    private void testUnion() {

        ViewableInventory chest = ViewableInventory.builder().type(ContainerTypes.GENERIC_9x3.get()).completeStructure().build();

        Inventory firstSlots = chest.query(SlotMatchers.index(0));
        Inventory firstRow = chest.query(InventoryRow.class).get().children().get(0);
        GridInventory grid = chest.query(GridInventory.class).get();
        InventoryColumn firstCol = grid.getColumn(0).get();
        InventoryColumn secondCol = grid.getColumn(1).get();
        Inventory union = firstSlots.union(firstCol).union(firstRow);
        Inventory union2 = firstCol.union(firstRow);
        Inventory union3 = firstCol.union(secondCol);
        Preconditions.checkState(union.capacity() == 11, "This should include all eleven slot of the first row and column!");
        Preconditions.checkState(union2.capacity() == 11, "This should include all eleven slot of the first row and column!");
        Preconditions.checkState(union3.capacity() == 6, "This should include all six slot of the first 2 columns!");
    }

    @Override
    public void enable(MessageReceiver src) {
        Sponge.getEventManager().registerListeners(this.container, this.listener);
    }

    public static class InventorySetOpsListener {

        @SuppressWarnings("deprecation")
        @Listener
        public void onMidas(ChangeInventoryEvent.Held event, @Root Player player) {
            // Checks if Slots are contained in the hotbar then may transform iron to gold
            Inventory hotbar = event.getInventory().query(Hotbar.class).get();
            boolean nugget = false;
            for (SlotTransaction transaction : event.getTransactions()) {
                if (hotbar.containsInventory(transaction.getSlot())) {
                    if (ItemTypes.GOLD_NUGGET.equals(transaction.getOriginal().getType())) {
                        nugget = true;
                    }
                    if (nugget && ItemTypes.IRON_INGOT.equals(transaction.getOriginal().getType())) {
                        transaction.setCustom(ItemStack.of(ItemTypes.GOLD_INGOT.get(), transaction.getOriginal().getQuantity()));
                    }
                }
            }
        }
    }

}
