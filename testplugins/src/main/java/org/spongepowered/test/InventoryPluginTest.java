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
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.api.item.inventory.InventoryKeys;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;

@Plugin(id = "inventoryplugintest", name = "Inventory Test", description = "A plugin to test the owner of an inventory.", version = "0.0.0")
public class InventoryPluginTest implements LoadableModule {

    @Inject private PluginContainer container;
    private final InventoryPluginListener listener = new InventoryPluginListener();

    @Override
    public void enable(MessageReceiver src) {
        Sponge.getEventManager().registerListeners(this.container, this.listener);
    }

    public static class InventoryPluginListener {

        @Listener
        public void onInventoryClose(InteractContainerEvent.Close event, @First Player player) {
            player.sendMessage(Text.of("This inventory was brought to you by ",
                    event.getContainer().get(InventoryKeys.PLUGIN).map(PluginContainer::getName).orElse("unknown")));
        }
    }
}
