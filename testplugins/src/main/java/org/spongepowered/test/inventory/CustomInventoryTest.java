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
package org.spongepowered.test.inventory;

import com.flowpowered.math.vector.Vector2i;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
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
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryProperties;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.custom.ContainerTypes;
import org.spongepowered.api.item.inventory.equipment.EquipmentInventory;
import org.spongepowered.api.item.inventory.slot.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * When trying to break an Inventory TE while sneaking you open a custom Version of it instead.
 * Clicks in the opened Inventory are recorded with their SlotIndex in your Chat.
 * For detection this uses a very basic custom Carrier Implementation.
 */
@Plugin(id = "custominventorytest", name = "Custom Inventory Test", description = "A plugin to test custom inventories", version = "0.0.0")
public class CustomInventoryTest {

    @Listener
    public void onPunchBlock(InteractBlockEvent.Primary event, @Root Player player) {
        if (!player.get(Keys.IS_SNEAKING).orElse(false)) {
            return;
        }
        event.getBlock().getLocation().ifPresent(loc -> {
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
        if (event.getEntity() instanceof Horse) {
            ViewableInventory inventory = ViewableInventory.builder().type(ContainerTypes.HORSE)
                    .completeStructure()
                    .carrier(((Horse) event.getEntity()))
                    .build();

            int i = 1;
            for (Inventory slot : inventory.slots()) {
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
            ViewableInventory inventory = ViewableInventory.builder().type(ContainerTypes.DISPENSER)
                    .completeStructure()
                    .identity(UUID.randomUUID())
                    .build();

            ItemStack flard = ItemStack.of(ItemTypes.SLIME_BALL, 1);
            flard.offer(Keys.DISPLAY_NAME, Text.of("Flard?"));
            for (Inventory slot : inventory.slots()) {
                slot.set(flard);
            }
            
            player.openInventory(inventory, Text.of("Slime Content"));
            event.setCancelled(true);
        }
    }

    private void interactOtherBlock(InteractBlockEvent.Primary event, Player player, Location loc) {
        if (loc.getBlock().getType() == BlockTypes.CRAFTING_TABLE) {
            ViewableInventory inventory = ViewableInventory.builder().type(ContainerTypes.CRAFTING_TABLE).completeStructure().build();

            for (Inventory slot : inventory.slots()) {
                slot.set(ItemStack.of(ItemTypes.IRON_NUGGET, 1));
            }

            player.openInventory(inventory, Text.of("Custom Workbench"));
            event.setCancelled(true);
        }
    }

    private void interactCarrier(InteractBlockEvent.Primary event, Player player, Location loc) {
        loc.getTileEntity().ifPresent(te -> {
            if (te instanceof Carrier) {
                BasicCarrier myCarrier = new BasicCarrier();

                Optional<ViewableInventory> vi = ((Carrier) te).getInventory().asViewable();
                if (vi.isPresent()) {
                    ViewableInventory inventory = ViewableInventory.builder().typeFrom(vi.get()).completeStructure()
                            .carrier(myCarrier)
                            .build();

                    myCarrier.init(inventory);
                    player.openInventory(inventory, Text.of("Custom ", ((Carrier) te).getInventory().getName()));
                    event.setCancelled(true);
                }
            }
        });
    }

    private final AnnoyingListener listener = new AnnoyingListener();
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
                }).build(), "togglesuperannoyinginventorymessage");
    }

    public static class AnnoyingListener {

        @Listener
        public void onInventoryClick(ClickContainerEvent event, @First Player player, @Getter("getTargetInventory") CarriedInventory<?> container) {
            container.getProperty(InventoryProperties.UNIQUE_ID).ifPresent(uuid -> player.sendMessage(Text.of("Identifiable Inventory: ", uuid)));
            for (SlotTransaction trans : event.getTransactions()) {
                Slot slot = trans.getSlot();
                Slot realSlot = slot.viewedSlot();
                int slotClicked = slot.getProperty(InventoryProperties.SLOT_INDEX).map(SlotIndex::getIndex).orElse(-1);
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




    // TODO actually make it do smth.
    static void foobar()
    {
        Player player = null;
        Player player2 = null;

        Inventory inv1 = Inventory.builder().grid(3, 3).completeStructure().build();
        Inventory inv2 = Inventory.builder().grid(3, 3).completeStructure().build();
        Inventory inv3 = Inventory.builder().grid(9, 3).completeStructure().build();

        ViewableInventory inv = ViewableInventory.builder()
                .type(ContainerTypes.CHEST_3X9)
                .grid(inv1.slots(), new Vector2i(3,3), 0)
                .grid(inv2.slots(), new Vector2i(3,3), new Vector2i(3, 1))
                .grid(inv3.slots() /*TODO query for grid*/, new Vector2i(3, 3), new Vector2i(6, 3))
                .slots(Arrays.asList(inv3.getSlot(SlotIndex.of(0)).get()), 37)
                .dummySlots(1, 16)
                .fillDummy()
                .completeStructure()
                .identity(UUID.randomUUID())
                .build();

        ViewableInventory basicChest = ViewableInventory.builder()
                .type(ContainerTypes.CHEST_3X9)
                .completeStructure()
                .build();

        ItemStackSnapshot disabled = ItemStack.of(ItemTypes.LIGHT_GRAY_STAINED_GLASS_PANE, 1).createSnapshot();
        ItemStackSnapshot emerald = ItemStack.of(ItemTypes.EMERALD, 1).createSnapshot();


        EquipmentInventory armor = null;
        GridInventory mainGrid = null;
        Slot offhand = null;

        ViewableInventory.builder().type(ContainerTypes.CHEST_3X9)
                .slots(armor.slots(), 0)
                .slots(mainGrid.slots(), armor.slots().size())
                .slots(offhand.slots(), armor.slots().size() + mainGrid.slots().size())
                .completeStructure();

    }

}
