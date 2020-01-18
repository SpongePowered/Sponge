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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.ContainerTypes;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;

import java.util.Optional;

@Plugin(id = InventoryOpenCloseTest.ID, name = "Inventory Open/Close Test", description = InventoryOpenCloseTest.DESCRIPTION, version = "0.0.0")
public class InventoryOpenCloseTest implements LoadableModule {

    @Inject private PluginContainer container;
    public static final String ID = "inventoryopenclosetest";
    public static final String DESCRIPTION = "A plugin to test open and close during inventory events.";
    private final InventoryOpenCloseListener listener = new InventoryOpenCloseListener();

    @Override
    public void enable(MessageReceiver src) {
        Sponge.getEventManager().registerListeners(this.container, this.listener);
    }

    public static class InventoryOpenCloseListener {

        @Listener
        public void onInventoryPrimary(ClickContainerEvent.Primary event, @First Player player) {
            ViewableInventory inv = ViewableInventory.builder().type(ContainerTypes.GENERIC_9x3.get()).completeStructure().build();
            // This will open the inventory the next tick
            player.openInventory(inv);
        }

        @Listener
        public void onInventorySecondary(ClickContainerEvent.Secondary event, @First Player player) {
            // This will close the inventory the next tick
            player.closeInventory();
        }

        @Listener
        public void onInventoryClose(ClickContainerEvent.Close event, @First Player player) {
            Optional<PluginContainer> pc = event.getCause().first(PluginContainer.class);
            player.sendMessage(Text.of("Inventory closed by " + pc.map(PluginContainer::getId).orElse(player.getName())));
        }

        @Listener
        public void onInventoryOpen(ClickContainerEvent.Open event, @First Player player) {
            Optional<PluginContainer> pc = event.getCause().first(PluginContainer.class);
            player.sendMessage(Text.of("Inventory opened by " + pc.map(PluginContainer::getId).orElse(player.getName())));
        }
    }
}
