/*
 * This file is part of testplugins, licensed under the MIT License (MIT).
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

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.util.Optional;

/**
 * When trying to break an Inventory TE while sneaking you open a custom Version of it instead.
 * Clicks in the opened Inventory are recorded with their SlotIndex in your Chat.
 * For detection this uses a very basic custom Carrier Implementation.
 */
@Plugin(id = "custominventorytest", name = "Custom Inventory Test", description = "A plugin to test custom inventories")
public class CustomInventoryTest {

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Primary event, @Root Player player) {
        if (!player.get(Keys.IS_SNEAKING).orElse(false)) {
            return;
        }
        event.getTargetBlock().getLocation().ifPresent(l ->
                l.getTileEntity().ifPresent(te -> {
                    if (te instanceof Carrier) {
                        BasicCarrier myCarrier = new BasicCarrier();
                        Inventory custom = Inventory.builder().from(((Carrier) te).getInventory())
                                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of("Custom ", ((Carrier) te).getInventory().getName())))
                                .withCarrier(myCarrier)
                                .build(this);
                        myCarrier.init(custom);
                        player.openInventory(custom, Cause.source(player).build());
                        event.setCancelled(true);
                    }
                })
        );
    }

    @Listener
    public void onInventoryClick(ClickInventoryEvent event, @Root Player player, @Getter("getTargetInventory") CarriedInventory container) {
        Optional<Carrier> carrier = container.getCarrier();
        if (carrier.isPresent() && carrier.get() instanceof BasicCarrier) {
            for (SlotTransaction trans : event.getTransactions()) {
                Integer slotClicked = trans.getSlot().getProperty(SlotIndex.class, "slotindex").map(SlotIndex::getValue).orElse(-1);
                player.sendMessage(Text.of("You clicked Slot ", slotClicked, " in ", container.getName()));
            }
        }
    }

    private static class BasicCarrier implements Carrier {

        private Inventory inventory;

        @Override
        public CarriedInventory<? extends Carrier> getInventory() {
            return ((CarriedInventory) this.inventory);
        }

        public void init(Inventory inventory) {
            this.inventory = inventory;
        }
    }

}
