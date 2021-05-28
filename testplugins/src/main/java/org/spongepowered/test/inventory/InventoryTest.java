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

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.KeyValueMatcher;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.ChangeEntityEquipmentEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.event.item.inventory.EnchantItemEvent;
import org.spongepowered.api.event.item.inventory.TransferInventoryEvent;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ContainerTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.PrimaryPlayerInventory;
import org.spongepowered.api.item.inventory.menu.ClickType;
import org.spongepowered.api.item.inventory.menu.InventoryMenu;
import org.spongepowered.api.item.inventory.menu.handler.SlotClickHandler;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.util.Optional;

@Plugin("inventorytest")
public final class InventoryTest implements LoadableModule {

    private final PluginContainer plugin;

    @Inject
    public InventoryTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    public static class InventoryTestListener {

        private final PluginContainer plugin;

        public InventoryTestListener(final PluginContainer plugin) {
            this.plugin = plugin;
        }

        @Listener
        public void onInteractContainerOpen(final InteractContainerEvent.Open event) {
            final Container container = event.container();
            final Hotbar hotbarFromContain = container.query(Hotbar.class).orElse(null);
            final Hotbar hotbarFromPrimary = container.query(PrimaryPlayerInventory.class).get().query(Hotbar.class).orElse(null);
            final Inventory stoneFromContain = hotbarFromContain.query(QueryTypes.ITEM_TYPE, ItemTypes.STONE.get());
            final Inventory stoneFromPrimary = hotbarFromPrimary.query(QueryTypes.ITEM_TYPE, ItemTypes.STONE.get());

            final Inventory slotIndex0 = container.query(KeyValueMatcher.of(Keys.SLOT_INDEX, 0));
            this.plugin.logger().info("{} slots: {}", "SlotIndex 0 ", slotIndex0.capacity());
            final Inventory slotPos1_1 = container.query(KeyValueMatcher.of(Keys.SLOT_POSITION, Vector2i.from(1,1)));
            this.plugin.logger().info("{} slots: {}", "SlotPos 1 1", slotPos1_1.capacity());
            final Inventory slotPos0_6 = container.query(PrimaryPlayerInventory.class).get().query(KeyValueMatcher.of(Keys.SLOT_POSITION, Vector2i.from(0, 6)));
            this.plugin.logger().info("{} slots: {}", "SlotPos 0 6", slotPos0_6.capacity());

            // TODO equality check fails with the default TextComponent
            final Inventory foobarInv = container.query(KeyValueMatcher.of(Keys.DISPLAY_NAME, Component.text("Foobar")));
            this.plugin.logger().info("{} slots: {}", "Foobar Title", foobarInv.capacity());
            final Inventory max1Quantity = container.query(KeyValueMatcher.of(Keys.MAX_STACK_SIZE, 1));
            this.plugin.logger().info("{} slots: {}", "Max quantity 1", max1Quantity.capacity());
            final Inventory grids = container.query(QueryTypes.INVENTORY_TYPE, GridInventory.class);
            this.plugin.logger().info("{} count: {}", "grids ", grids.children().size()); // contains duplicate slots
            final Optional<Component> component = container.get(Keys.DISPLAY_NAME);

            final String title = component.map(c -> PlainComponentSerializer.plain().serialize(c)).orElse("No Title");
            this.plugin.logger().info("{} [{}]", event.getClass().getSimpleName(), title);
        }

        @Listener
        public void onInteractContainer(final InteractContainerEvent event) {
            if (event instanceof EnchantItemEvent) {
                this.plugin.logger().info("{} [{}] S:{}", event.getClass().getSimpleName(), ((EnchantItemEvent) event).option(),
                        ((EnchantItemEvent) event).seed());
            }
            final Optional<Component> component = event.container().get(Keys.DISPLAY_NAME);
            final String title = component.map(c -> PlainComponentSerializer.plain().serialize(c)).orElse("No Title");
            if (title.equals("Foobar")) {
                InventoryTest.doFancyStuff(event.cause().first(Player.class).get());
            }
        }

        @Listener
        public void onInteract(final ChangeInventoryEvent event) {

            if (event instanceof ClickContainerEvent) {
                this.plugin.logger().info("{} {}", event.getClass().getSimpleName(), ((ClickContainerEvent) event).container().getClass().getSimpleName());
                final Transaction<ItemStackSnapshot> cursor = ((ClickContainerEvent) event).cursorTransaction();
                this.plugin.logger().info("  Cursor: {}x{}->{}x{}", cursor.original().type(), cursor.original().quantity(),
                        cursor.finalReplacement().type(), cursor.finalReplacement().quantity());
            } else {
                this.plugin.logger().info("{} {}", event.getClass().getSimpleName(), event.inventory().getClass().getSimpleName());
            }
            for (final SlotTransaction slotTrans : event.transactions()) {
                final Optional<Integer> integer = slotTrans.slot().get(Keys.SLOT_INDEX);
                this.plugin.logger().info("  SlotTr: {}x{}->{}x{}[{}]", slotTrans.original().type(), slotTrans.original().quantity(),
                        slotTrans.finalReplacement().type(), slotTrans.finalReplacement().quantity(), integer.get());
            }
        }

        @Listener
        public void onChangeEquipment(final ChangeEntityEquipmentEvent event) {
            final Slot slot = event.slot();
            final Transaction<ItemStackSnapshot> transaction = event.transaction();
            this.plugin.logger().info("{}: {} {}->{}",
                    event.entity().type().key(RegistryTypes.ENTITY_TYPE),
                    slot.get(Keys.EQUIPMENT_TYPE).get().key(RegistryTypes.EQUIPMENT_TYPE),
                    transaction.original().type().key(RegistryTypes.ITEM_TYPE),
                    transaction.finalReplacement().type().key(RegistryTypes.ITEM_TYPE));
        }

        @Listener
        public void onTransfer(final TransferInventoryEvent event) {
            if (event instanceof TransferInventoryEvent.Post) {
                this.plugin.logger().info("{} {}=>{}", event.getClass().getSimpleName(), event.sourceInventory().getClass().getSimpleName(), event.targetInventory()
                        .getClass().getSimpleName());
                final Integer sourceIdx = ((TransferInventoryEvent.Post) event).sourceSlot().get(Keys.SLOT_INDEX).get();
                final Integer targetIdx = ((TransferInventoryEvent.Post) event).targetSlot().get(Keys.SLOT_INDEX).get();
                final ItemStackSnapshot item = ((TransferInventoryEvent.Post) event).transferredItem();
                this.plugin.logger().info("[{}] -> [{}] {}x{}", sourceIdx, targetIdx, item.type(), item.quantity());
            }
        }

        @Listener
        public void onCraft(CraftItemEvent event) {
//            this.plugin.logger().info("{} size: {} recipe: {} ",
//                    event.getClass().getSimpleName(),
//                    event.craftingInventory().capacity(),
//                    event.recipe().map(Recipe::getKey).map(ResourceKey::asString).orElse("no recipe"));
        }
    }

    @Override
    public void enable(final CommandContext ctx) {
        Sponge.eventManager().registerListeners(this.plugin, new InventoryTestListener(this.plugin));
    }

    private static void doFancyStuff(final Player player) {

        final GridInventory inv27Grid = player.inventory().query(PrimaryPlayerInventory.class).get().storage();
        final Inventory inv27Slots = Inventory.builder().slots(27).completeStructure().build();
        final Inventory inv27Slots2 = Inventory.builder().slots(27).completeStructure().build();
        final ViewableInventory doubleMyInventory = ViewableInventory.builder().type(ContainerTypes.GENERIC_9X6.get())
                .grid(inv27Slots.slots(), Vector2i.from(9, 3), Vector2i.from(0, 0))
                .grid(inv27Slots2.slots(), Vector2i.from(9, 3), Vector2i.from(0, 3))
                .completeStructure()
                .carrier(player)
                .build();
        final InventoryMenu menu = doubleMyInventory.asMenu();
        menu.setReadOnly(true);
        doubleMyInventory.set(0, ItemStack.of(ItemTypes.IRON_INGOT));
        doubleMyInventory.set(8, ItemStack.of(ItemTypes.GOLD_INGOT));
        doubleMyInventory.set(45, ItemStack.of(ItemTypes.EMERALD));
        doubleMyInventory.set(53, ItemStack.of(ItemTypes.DIAMOND));
        menu.registerSlotClick(new MySlotClickHandler(menu, doubleMyInventory));
        final Optional<Container> open = menu.open((ServerPlayer) player);
    }

    private static class MySlotClickHandler implements SlotClickHandler {

        private final InventoryMenu menu;
        private final ViewableInventory primary;
        private ViewableInventory last;

        public MySlotClickHandler(final InventoryMenu menu, final ViewableInventory primary) {
            this.primary = primary;
            this.menu = menu;
        }

        @Override
        public boolean handle(final Cause cause, final Container container, final Slot slot, final int slotIndex, final ClickType<?> clickType) {

            if (slot.viewedSlot().parent() == this.primary) {
                switch (slotIndex) {
                    case 0:
                    case 8:
                    case 45:
                    case 53:
                        this.last = ViewableInventory.builder().type(ContainerTypes.GENERIC_9X6.get())
                                .fillDummy().item(slot.peek().createSnapshot())
                                .completeStructure().build();
                        this.menu.setCurrentInventory(this.last);
                        break;
                    default:
                        slot.set(ItemStack.of(ItemTypes.BEDROCK));
                }
                return false;
            } else if (slot.viewedSlot().parent() == this.last) {
                this.menu.setCurrentInventory(this.primary);
                return false;
            }
            return true;
        }
    }

    //
    //    public static net.minecraft.inventory.container.Container doStuff(net.minecraft.inventory.container.Container mcContainer, PlayerEntity
    //    player) {
    //        Container container = ((Container) mcContainer);
    //        InventoryAdapter adapter = (InventoryAdapter) container;
    //
    //        if (container instanceof ChestContainer) {
    //            int i = 1;
    //            ItemStack stick = ItemStack.of(ItemTypes.STICK);
    //            for (org.spongepowered.api.item.inventory.Slot slot : container.slots()) {
    //                stick.setQuantity(i++);
    //                slot.set(stick.copy());
    //            }
    //            stick.setQuantity(1);
    //            Inventory queriedGrid = container.query(PrimaryPlayerInventory.class).get().asGrid().query(QueryTypes.GRID, Vector2i.from(1, 1),
    //                    Vector2i.from(2, 2));
    //            queriedGrid.slots().forEach(slot -> {
    //                slot.set(stick.copy());
    //            });
    //            Inventory grids = container.query(QueryTypes.INVENTORY_TYPE, GridInventory.class);
    //            container.query(Hotbar.class).get().set(0, ItemStack.of(ItemTypes.CHEST));
    //
    //            Inventory inv5slots = Inventory.builder().slots(5).completeStructure().build();
    //            Inventory inv4GridSlots = Inventory.builder().grid(2, 2).completeStructure().build();
    //            inv4GridSlots.offer(ItemStack.of(ItemTypes.DIAMOND), ItemStack.of(ItemTypes.EMERALD), ItemStack.of(ItemTypes.IRON_INGOT),
    //            ItemStack.of(ItemTypes.GOLD_INGOT));
    //            Inventory inv10Composite = Inventory.builder()
    //                    .inventory(inv5slots)
    //                    .inventory(inv4GridSlots)
    //                    .slots(1)
    //                    .completeStructure().build();
    //            Inventory inv4GridAgain = inv10Composite.query(GridInventory.class).get();
    //
    //
    //            Optional<ItemStack> itemStack = inv10Composite.peekAt(5);
    //            inv4GridAgain.peekAt(0);
    //            inv4GridAgain.slots().forEach(slot -> System.out.println(slot.peek()));
    //
    //            Inventory mixedComposite = Inventory.builder().inventory(grids).slots(1).inventory(container).completeStructure().build();
    //        }
    //        if (container instanceof DispenserContainer) {
    //            final GridInventory inv27Grid = ((PlayerInventory)player.inventory).query(PrimaryPlayerInventory.class).get().storage();
    //            final Inventory inv27Slots = Inventory.builder().slots(27).completeStructure().build();
    //            final Inventory inv27Slots2 = Inventory.builder().slots(27).completeStructure().build();
    //            final ViewableInventory doubleMyInventory = ViewableInventory.builder().type(ContainerTypes.GENERIC_9x6.get())
    //                    .grid(inv27Slots.slots(), Vector2i.from(9, 3), Vector2i.from(0, 0))
    //                    .grid(inv27Slots2.slots(), Vector2i.from(9, 3), Vector2i.from(0, 3))
    //                    .completeStructure()
    //                    .carrier((Carrier)player)
    //                    .build();
    //            final Optional<Container> open = doubleMyInventory.asMenu().open((ServerPlayer) player);
    //            doubleMyInventory.offer(ItemStack.of(ItemTypes.GOLD_INGOT));
    //            doubleMyInventory.offer(ItemStack.of(ItemTypes.IRON_INGOT));
    //            return null;
    //        }
    //
    //        return mcContainer;
    //
    //    }
}
