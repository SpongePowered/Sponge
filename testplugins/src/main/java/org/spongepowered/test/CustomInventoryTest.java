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
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.animal.Horse;
import org.spongepowered.api.entity.living.animal.Llama;
import org.spongepowered.api.entity.living.animal.Mule;
import org.spongepowered.api.entity.living.monster.Slime;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.GuiIdProperty;
import org.spongepowered.api.item.inventory.property.GuiIds;
import org.spongepowered.api.item.inventory.property.Identifiable;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.function.Consumer;

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
    public void enable(CommandSource src) {
        Sponge.getEventManager().registerListeners(this.container, this.listener);
        Sponge.getEventManager().registerListeners(this.container, this.interactListener);
    }


    public static class CustomInventoryListener {


        @Listener
        public void onPunchBlock(InteractBlockEvent.Primary event, @Root Player player) {
            if (!player.get(Keys.IS_SNEAKING).orElse(false)) {
                return;
            }
            event.getTargetBlock().getLocation().ifPresent(loc -> {
                        interactCarrier(event, player, loc);
                        interactOtherBlock(event, player, loc);
                    }
            );
        }

        @Listener
        public void onPunchEntity(InteractEntityEvent.Primary event, @Root Player player) {
            if (!player.get(Keys.IS_SNEAKING).orElse(false)) {
                return;
            }
            if (event.getTargetEntity() instanceof Mule) {
                Inventory.Builder builder;
                if (((Mule) event.getTargetEntity()).getInventory().capacity() <= 2) {
                    builder = Inventory.builder().of(InventoryArchetypes.HORSE);
                } else {
                    builder = Inventory.builder().of(InventoryArchetypes.HORSE_WITH_CHEST);
                }
                Inventory inventory = builder.property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of("Custom Mule")))
                        .withCarrier(((Horse) event.getTargetEntity()))
                        .build(this);
                int i = 1;
                for (Inventory slot : inventory.slots()) {
                    slot.set(ItemStack.of(ItemTypes.APPLE, i++));
                }
                Sponge.getCauseStackManager().pushCause(player);
                player.openInventory(inventory);
                Sponge.getCauseStackManager().popCause();
                event.setCancelled(true);
            } else if (event.getTargetEntity() instanceof Llama) {
                Inventory.Builder builder;
                if (((Llama) event.getTargetEntity()).getInventory().capacity() <= 2) {
                    builder = Inventory.builder().of(InventoryArchetypes.HORSE);
                } else {
                    builder = Inventory.builder().of(InventoryArchetypes.HORSE_WITH_CHEST);
                }
                Inventory inventory = builder.property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of("Custom Llama")))
                        .withCarrier(((Horse) event.getTargetEntity()))
                        .build(this);
                int i = 1;
                for (Inventory slot : inventory.slots()) {
                    slot.set(ItemStack.of(ItemTypes.APPLE, i++));
                }
                Sponge.getCauseStackManager().pushCause(player);
                player.openInventory(inventory);
                Sponge.getCauseStackManager().popCause();
                event.setCancelled(true);
            } else if (event.getTargetEntity() instanceof Horse) {
                Inventory inventory = Inventory.builder().of(InventoryArchetypes.HORSE)
                        .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of("Custom Horse")))
                        .withCarrier(((Horse) event.getTargetEntity()))
                        .build(this);
                int i = 1;
                for (Inventory slot : inventory.slots()) {
                    slot.set(ItemStack.of(ItemTypes.APPLE, i++));
                }
                Sponge.getCauseStackManager().pushCause(player);
                player.openInventory(inventory);
                Sponge.getCauseStackManager().popCause();
                event.setCancelled(true);
            }
            if (event.getTargetEntity() instanceof Slime) {
                Inventory inventory = Inventory.builder().of(InventoryArchetypes.MENU_GRID)
                        .property(InventoryDimension.of(1, 9))
                        .property(InventoryTitle.of(Text.of("Slime Content")))
                        .property(new Identifiable())
                        .property(new GuiIdProperty(GuiIds.DISPENSER))
                        .build(this);
                ItemStack flard = ItemStack.of(ItemTypes.SLIME, 1);
                flard.offer(Keys.DISPLAY_NAME, Text.of("Flard?"));
                for (Inventory slot : inventory.slots()) {
                    slot.set(flard);
                }
                Sponge.getCauseStackManager().pushCause(player);
                player.openInventory(inventory);
                Sponge.getCauseStackManager().popCause();
            }
        }

        private void interactOtherBlock(InteractBlockEvent.Primary event, Player player, Location<World> loc) {
            if (loc.getBlockType() == BlockTypes.CRAFTING_TABLE) {
                Inventory inventory = Inventory.builder().of(InventoryArchetypes.WORKBENCH)
                        .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of("Custom Workbench")))
                        .listener(ClickInventoryEvent.Shift.class, new Consumer<ClickInventoryEvent.Shift>() {

                            @Override
                            public void accept(ClickInventoryEvent.Shift shift) {
                                shift.getCause().first(Player.class).get().sendMessage(Text.of(TextColors.GREEN, "Shift click!"));
                            }
                        })
                        .build(this);
                for (Inventory slot : inventory.slots()) {
                    slot.set(ItemStack.of(ItemTypes.IRON_NUGGET, 1));
                }
                Sponge.getCauseStackManager().pushCause(player);
                player.openInventory(inventory);
                Sponge.getCauseStackManager().popCause();

                event.setCancelled(true);
            }
        }

        private void interactCarrier(InteractBlockEvent.Primary event, Player player, Location<World> loc) {
            loc.getTileEntity().ifPresent(te -> {
                if (te instanceof Carrier) {
                    BasicCarrier myCarrier = new BasicCarrier();
                    Inventory custom = Inventory.builder().from(((Carrier) te).getInventory())
                            .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of("Custom ", ((Carrier) te).getInventory().getName())))
                            .listener(ClickInventoryEvent.Shift.class, new Consumer<ClickInventoryEvent.Shift>() {

                                @Override
                                public void accept(ClickInventoryEvent.Shift shift) {
                                    player.sendMessage(Text.of(TextColors.YELLOW, "Carrier shift click!"));
                                }
                            })
                            .withCarrier(myCarrier)
                            .build(this);
                    myCarrier.init(custom);
                    Sponge.getCauseStackManager().pushCause(player);
                    player.openInventory(custom);
                    player.closeInventory();
                    player.openInventory(custom);
                    Sponge.getCauseStackManager().popCause();
                    event.setCancelled(true);
                }
            });
        }
    }

    public static class AnnoyingListener {

        @Listener
        public void onInventoryClick(ClickInventoryEvent event, @First Player player, @Getter("getTargetInventory") CarriedInventory<?> container) {
            container.getInventoryProperty(Identifiable.class).ifPresent(i -> player.sendMessage(Text.of("Identifiable Inventory: ", i.getValue())));
            for (SlotTransaction trans : event.getTransactions()) {
                Slot slot = trans.getSlot();
                Slot realSlot = slot.transform();
                Integer slotClicked = slot.getProperty(SlotIndex.class, "slotindex").map(SlotIndex::getValue).orElse(-1);
                player.sendMessage(Text.of("You clicked Slot ", slotClicked, " in ", container.getName(), "/", realSlot.parent().getName()));
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

}
