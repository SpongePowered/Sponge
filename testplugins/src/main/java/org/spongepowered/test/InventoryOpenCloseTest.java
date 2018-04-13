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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.util.Optional;

@Plugin(id = "inventoryopenclosetest", name = "Inventory Open/Close Test", description = "A plugin to test open and close during inventory events.")
public class InventoryOpenCloseTest {

    private final InventoryListener listener = new InventoryListener();
    private boolean registered = false;

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getCommandManager().register(this,
                CommandSpec.builder().executor((source, context) -> {
                    if (this.registered) {
                        this.registered = false;
                        Sponge.getEventManager().unregisterListeners(this.listener);
                    } else {
                        this.registered = true;
                        Sponge.getEventManager().registerListeners(this, this.listener);
                    }
                    return CommandResult.success();
                }).build(), "toggleinventoryopenclosetest");
    }

    public class InventoryListener {

        @Listener
        public void onInventoryPrimary(ClickInventoryEvent.Primary event, @First Player player) {
            Inventory inv = Inventory.builder().build(InventoryOpenCloseTest.this);
            // This will open the inventory the next tick
            player.openInventory(inv);
        }

        @Listener
        public void onInventorySecondary(ClickInventoryEvent.Secondary event, @First Player player) {
            // This will close the inventory the next tick
            player.closeInventory();
        }

        @Listener
        public void onInventoryClose(InteractInventoryEvent.Close event, @First Player player) {
            Optional<PluginContainer> pc = event.getCause().first(PluginContainer.class);
            player.sendMessage(Text.of("Inventory closed by " + pc.map(PluginContainer::getId).orElse(player.getName())));
        }

        @Listener
        public void onInventoryOpen(InteractInventoryEvent.Open event, @First Player player) {
            Optional<PluginContainer> pc = event.getCause().first(PluginContainer.class);
            player.sendMessage(Text.of("Inventory opened by " + pc.map(PluginContainer::getId).orElse(player.getName())));
        }
    }
}
