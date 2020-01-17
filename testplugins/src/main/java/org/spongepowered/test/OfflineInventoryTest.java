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
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.channel.MessageReceiver;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Test for User(Offline-Player) Inventory
 */
@Plugin(id = "offlineinventorytest", name = "Offline Inventory Test", description = "A plugin to test offline inventories", version = "0.0.0")
public class OfflineInventoryTest implements LoadableModule {

    @Inject private Logger logger;
    @Inject private PluginContainer container;

    private TestListener listener = new TestListener();

    @Override
    public void enable(MessageReceiver src) {
        Sponge.getEventManager().registerListeners(this.container, this.listener);
    }

    public class TestListener {

        @Listener
        public void onDisconnect(ClientConnectionEvent.Disconnect event, @Root Player player) {
            UUID uuid = player.getUniqueId();
            Sponge.getServer().getScheduler()
                    .createExecutor(OfflineInventoryTest.this.container)
                    .schedule(() -> this.run(uuid), 1, TimeUnit.SECONDS);
        }

        private void run(UUID uuid) {
            Logger logger = OfflineInventoryTest.this.logger;

            // Read offline inventory
            User user = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(uuid).get();
            logger.info(user.getName() + " has an Inventory with:");
            for (Slot slot : user.getInventory().slots()) {
                ItemStack stack = slot.peek();
                logger.info(stack.getType().getKey() + "x" + stack.getQuantity());
            }
            logger.info("Helmet: " + user.getHelmet().getType().getKey());
            logger.info("Chestplate: " + user.getChestplate().getType().getKey());
            logger.info("Leggings: " + user.getLeggings().getType().getKey());
            logger.info("Boots: " + user.getBoots().getType().getKey());

            // Modify offline inventory
            logger.info("and a hotbar full of diamonds!");
            for (Slot inv : user.getInventory().query(Hotbar.class).get().slots()) {
                inv.offer(ItemStack.of(ItemTypes.DIAMOND.get(), 1));
            }
        }

    }

}
