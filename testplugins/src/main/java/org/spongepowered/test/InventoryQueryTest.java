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

import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.KeyValueMatcher;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.item.inventory.slot.SlotMatchers;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;

@Plugin(id = "inventoryquerytest", name = "Inventory Query Test", description = "A plugin for testing inventory queries", version = "0.0.0")
public class InventoryQueryTest implements LoadableModule {

    @Inject private PluginContainer container;

    private final TestListener listener = new TestListener();

    @Override public void enable(MessageReceiver src) {
        Sponge.getEventManager().registerListeners(this.container, this.listener);
    }

    public static class TestListener {
        @Listener
        public void onCloseInventory(ClickContainerEvent.Close event, @Root Player player) {
            Inventory inventory = player.getInventory();
            Inventory hotbar = inventory.query(QueryTypes.INVENTORY_TYPE.of(Hotbar.class));
            player.sendMessage(Text.of("You have ", hotbar.totalQuantity(), " items in your hotbar."));

            Inventory sticks = inventory.query(QueryTypes.ITEM_TYPE.of(ItemTypes.STICK));
            player.sendMessage(Text.of("You have ", sticks.totalQuantity(), " sticks in your inventory."));

            ItemStack lapis = ItemStack.of(ItemTypes.LAPIS_LAZULI, 4);
            Inventory lapisItems = inventory.query(QueryTypes.ITEM_STACK_IGNORE_QUANTITY.of(lapis));
            player.sendMessage(Text.of("You have ", lapisItems.totalQuantity(), " lapis lazuli in your inventory."));

            Inventory lapisItemsExact = inventory.query(QueryTypes.ITEM_STACK_EXACT.of(lapis));
            player.sendMessage(Text.of("You have ", lapisItemsExact.capacity(), " stacks of 4 lapis lazuli in your inventory."));

            Inventory evenCountStacks = inventory.query(QueryTypes.ITEM_STACK_CUSTOM.of(
                    x -> x.getQuantity() > 0 && x.getQuantity() % 2 == 0));
            player.sendMessage(Text.of("You have ", evenCountStacks.capacity(), " stacks with an even number of items in your inventory."));

            Inventory slots = ((PlayerInventory) inventory).getHotbar()
                    .query(SlotMatchers.index(3, KeyValueMatcher.Operator.LESS));
            player.sendMessage(Text.of("You have ", slots.totalQuantity(), " items in the first 3 slots of your hotbar."));

            Inventory slots2 = ((PlayerInventory) inventory).getHotbar()
                    .query(QueryTypes.INVENTORY_TRANSLATION.of(Sponge.getRegistry().getTranslationById("slot.name").get()));
            player.sendMessage(Text.of("You have ", slots2.totalQuantity(), " items in your hotbar."));

            inventory.query(QueryTypes.PLAYER_PRIMARY_HOTBAR_FIRST.toQuery())
                    .query(QueryTypes.REVERSE.toQuery())
                    .offer(ItemStack.of(ItemTypes.PAPER, 46));
            player.sendMessage(Text.of("Added paper to hotbar last."));
        }
    }

}
