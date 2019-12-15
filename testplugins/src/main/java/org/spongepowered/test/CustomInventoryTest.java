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
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.animal.horse.Horse;
import org.spongepowered.api.entity.living.animal.horse.Mule;
import org.spongepowered.api.entity.living.animal.horse.llama.Llama;
import org.spongepowered.api.entity.living.monster.slime.Slime;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ContainerTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryKeys;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.equipment.EquipmentInventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.math.vector.Vector2i;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * When trying to break an Inventory TE while sneaking you open a custom Version of it instead.
 * Clicks in the opened Inventory are recorded with their SlotIndex in your Chat.
 * For detection this uses a very basic custom Carrier Implementation.
 */
@Plugin(id = "custominventorytest", name = "Custom Inventory Test", description = "A plugin to test custom inventories", version = "0.0.0")
public class CustomInventoryTest implements LoadableModule {

    private final AnnoyingListener listener = new AnnoyingListener();
    private final CustomInventoryListener interactListener = new CustomInventoryListener();

    @Inject private PluginContainer container;

    @Override
    public void enable(MessageReceiver src) {
        Sponge.getEventManager().registerListeners(this.container, this.listener);
        Sponge.getEventManager().registerListeners(this.container, this.interactListener);
    }


    public static class CustomInventoryListener {


        @Listener
        public void onPunchBlock(InteractBlockEvent.Primary event, @Root Player player) {
            if (!player.get(Keys.IS_SNEAKING).orElse(false)) {
                return;
            }
            event.getBlock().getLocation().ifPresent(loc -> {
                this.interactCarrier(event, player, loc);
                this.interactOtherBlock(event, player, loc);
            });
        }

        @Listener
        public void onPunchEntity(InteractEntityEvent.Primary event, @Root Player player) {
            if (!player.get(Keys.IS_SNEAKING).orElse(false)) {
                return;
            }
            if (event.getEntity() instanceof Horse) {

                World world = event.getEntity().getWorld();

                Entity copyEntity = world.createEntity(event.getEntity().getType(), event.getEntity().getBlockPosition());
                CarriedInventory<? extends Carrier> inventory = ((Horse) copyEntity).getInventory();

                int i = 1;
                for (Slot slot : inventory.slots()) {
                    slot.set(ItemStack.of(ItemTypes.APPLE, i++));
                }
                Text text = Text.of("Custom Horse");
                if (event.getEntity() instanceof Mule) {
                    text = Text.of("Custom Mule");
                } else if (event.getEntity() instanceof Llama) {
                    text = Text.of("Custom Llama");
                }

                player.openInventory(inventory, text);
                event.setCancelled(true);
            }

            if (event.getEntity() instanceof Slime) {
                ViewableInventory inventory = ViewableInventory.builder().type(ContainerTypes.GENERIC_3x3)
                        .completeStructure()
                        .identity(UUID.randomUUID())
                        .build();
                ItemStack flard = ItemStack.of(ItemTypes.SLIME_BLOCK, 1);
                flard.offer(Keys.DISPLAY_NAME, Text.of("Flard?"));
                for (Slot slot : inventory.slots()) {
                    slot.set(flard);
                }

                player.openInventory(inventory, Text.of("Slime Content"));
                event.setCancelled(true);
            }
        }

        private void interactOtherBlock(InteractBlockEvent.Primary event, Player player, Location loc) {
            if (loc.getBlock().getType() == BlockTypes.CRAFTING_TABLE) {
                ViewableInventory inventory = ViewableInventory.builder().type(ContainerTypes.CRAFTING).completeStructure()
                        .build();
                for (Slot slot : inventory.slots()) {
                    slot.set(ItemStack.of(ItemTypes.IRON_NUGGET, 1));
                }

                player.openInventory(inventory, Text.of("Custom Workbench"));

                event.setCancelled(true);
            }
        }

        private void interactCarrier(InteractBlockEvent.Primary event, Player player, Location loc) {
            loc.getBlockEntity().ifPresent(te -> {
                if (te instanceof Carrier) {
                    BasicCarrier myCarrier = new BasicCarrier();
                    Optional<ViewableInventory> vi = ((Carrier) te).getInventory().asViewable();
                    if (vi.isPresent()) {
                        ViewableInventory inventory = ViewableInventory.builder().typeFrom(vi.get()).completeStructure()
                                .carrier(myCarrier)
                                .build();

                        myCarrier.init(inventory);
                        player.openInventory(inventory, Text.of("Custom ", te.getType().getKey()));
                        event.setCancelled(true);
                    }
                }
            });
        }
    }

    public static class AnnoyingListener {

        @Listener
        public void onInventoryClick(ClickContainerEvent event, @First Player player, @Getter("getInventory") Container container) {

            container.get(InventoryKeys.UNIQUE_ID).ifPresent(i -> player.sendMessage(Text.of("Identifiable Inventory: ", i)));
            for (SlotTransaction trans : event.getTransactions()) {
                Slot slot = trans.getSlot();
                Slot realSlot = slot.viewedSlot();
                Integer slotClicked = slot.get(InventoryKeys.SLOT_INDEX).orElse(-1);
                player.sendMessage(Text.of("You clicked Slot ", slotClicked, " in ", container.getClass().getSimpleName(), "/", realSlot.parent().getClass().getSimpleName()));
            }
        }
    }

    private static class BasicCarrier implements Carrier {

        private Inventory inventory;

        @Override
        public CarriedInventory<? extends Carrier> getInventory() {
            return ((CarriedInventory<?>) this.inventory);
        }

        public void init(Inventory inventory) {
            this.inventory = inventory;
        }
    }



    // TODO actually make it do smth.
    static void foobar()
    {
        Player player = null;
        Player player2 = null;

        Inventory inv1 = Inventory.builder().grid(3, 3).completeStructure().build();
        Inventory inv2 = Inventory.builder().grid(3, 3).completeStructure().build();
        Inventory inv3 = Inventory.builder().grid(9, 3).completeStructure().build();

        ViewableInventory inv = ViewableInventory.builder()
                .type(ContainerTypes.GENERIC_9x3)
                .grid(inv1.slots(), new Vector2i(3,3), 0)
                .grid(inv2.slots(), new Vector2i(3,3), new Vector2i(3, 1))
                .grid(inv3.slots() /*TODO query for grid*/, new Vector2i(3, 3), new Vector2i(6, 3))
                .slots(Arrays.asList(inv3.getSlot(0).get()), 37)
                .dummySlots(1, 16)
                .fillDummy()
                .completeStructure()
                .identity(UUID.randomUUID())
                .build();

        ViewableInventory basicChest = ViewableInventory.builder()
                .type(ContainerTypes.GENERIC_9x3)
                .completeStructure()
                .build();

        ItemStackSnapshot disabled = ItemStack.of(ItemTypes.LIGHT_GRAY_STAINED_GLASS_PANE, 1).createSnapshot();
        ItemStackSnapshot emerald = ItemStack.of(ItemTypes.EMERALD, 1).createSnapshot();


        EquipmentInventory armor = null;
        GridInventory mainGrid = null;
        Slot offhand = null;

        ViewableInventory.builder().type(ContainerTypes.GENERIC_9x3)
                .slots(armor.slots(), 0)
                .slots(mainGrid.slots(), armor.slots().size())
                .slots(offhand.slots(), armor.slots().size() + mainGrid.slots().size())
                .completeStructure();

    }

}
