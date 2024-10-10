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
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.KeyValueMatcher;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.type.HorseColors;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.animal.horse.Horse;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.entity.CookingEvent;
import org.spongepowered.api.event.entity.ChangeEntityEquipmentEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.EnchantItemEvent;
import org.spongepowered.api.event.item.inventory.TransferInventoryEvent;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ContainerTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.PrimaryPlayerInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.menu.ClickType;
import org.spongepowered.api.item.inventory.menu.InventoryMenu;
import org.spongepowered.api.item.inventory.menu.handler.SlotClickHandler;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Plugin("inventorytest")
public final class InventoryTest implements LoadableModule {

    private final PluginContainer plugin;

    @Inject
    public InventoryTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        final Command.Builder builder = Command.builder();
        builder.addChild(Command.builder().executor(this::enderchest).build(), "ender");
        builder.addChild(Command.builder().executor(this::villager).build(), "villager");
        builder.addChild(Command.builder().executor(this::horse).build(), "horse");
        event.register(this.plugin, builder.build(), "inventorytest");
    }

    private CommandResult villager(final CommandContext commandContext) {
        final ServerPlayer player = commandContext.cause().first(ServerPlayer.class).orElse(null);
        if (player == null) {
            return CommandResult.error(Component.text("Must be run ingame by a player"));
        }
        final ViewableInventory.Custom merchant = ViewableInventory.builder().type(ContainerTypes.MERCHANT).completeStructure().plugin(this.plugin).build();
        merchant.offer(Keys.TRADE_OFFERS, List.of(TradeOffer.builder()
                .firstBuyingItem(ItemStack.of(ItemTypes.DIAMOND, 1))
                .sellingItem(ItemStack.of(ItemTypes.EMERALD)).build()));
        player.openInventory(merchant);
        return CommandResult.success();
    }

    private CommandResult horse(final CommandContext commandContext) {
        final ServerPlayer player = commandContext.cause().first(ServerPlayer.class).orElse(null);
        if (player == null) {
            return CommandResult.error(Component.text("Must be run ingame by a player"));
        }
        final Horse horse = player.world().createEntity(EntityTypes.HORSE.get(), player.position());
        horse.offer(Keys.HORSE_COLOR, HorseColors.BLACK.get());
        horse.offer(Keys.IS_TAMED, true);
        player.world().spawnEntity(horse); // TODO can this work without spawning the horse?
        player.openInventory(horse.inventory());
        return CommandResult.success();
    }

    private CommandResult enderchest(CommandContext commandContext) {
        final ServerPlayer player = commandContext.cause().first(ServerPlayer.class).orElse(null);
        if (player == null) {
            return CommandResult.error(Component.text("Must be run ingame by a player"));
        }
        player.openInventory(player.enderChestInventory());
        return CommandResult.success();
    }

    public static class InventoryTestListener {

        private final PluginContainer plugin;

        public InventoryTestListener(final PluginContainer plugin) {
            this.plugin = plugin;
        }

        @Listener
        private void onInteractContainerOpen(final InteractContainerEvent.Open event) {
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

            final String title = component.map(c -> PlainTextComponentSerializer.plainText().serialize(c)).orElse("No Title");
            this.plugin.logger().info("{} [{}]", event.getClass().getSimpleName(), title);
        }

        @Listener
        private void onCooking(final CookingEvent event) {
            final String recipe = event.recipeKey().isPresent() ? event.recipeKey().get().toString() : "no recipe";
            this.plugin.logger().info("{} in {} using {}", event.getClass().getSimpleName(), event.blockEntity().getClass().getSimpleName(), recipe);
        }

        @Listener
        private void onInteractContainer(final InteractContainerEvent event) {
            if (event instanceof EnchantItemEvent) {
                this.plugin.logger().info("{} [{}] S:{}", event.getClass().getSimpleName(), ((EnchantItemEvent) event).option(),
                        ((EnchantItemEvent) event).seed());
            }
            final Optional<Component> component = event.container().get(Keys.DISPLAY_NAME);
            final String title = component.map(c -> PlainTextComponentSerializer.plainText().serialize(c)).orElse("No Title");
            if (title.equals("Foobar")) {
                InventoryTest.doFancyStuff(this.plugin, event.cause().first(Player.class).get());
            }
        }

        @Listener
        private void beforePickup(final ChangeInventoryEvent.Pickup.Pre event) {
            if (event.originalStack().type().isAnyOf(ItemTypes.BEDROCK)) {
                final ItemStackSnapshot stack = ItemStack.of(ItemTypes.COBBLESTONE, 64).asImmutable();
                final ArrayList<ItemStackSnapshot> items = new ArrayList<>();
                event.setCustom(items);
                for (int i = 0; i < 100; i++) {
                    items.add(stack);
                }
            }
        }

        @Listener
        public void onInteract(final ChangeInventoryEvent event) {
            this.plugin.logger().info("{} {} {}", event.getClass().getSimpleName(), event.inventory().getClass().getSimpleName(), event.cause());
            if (event instanceof ClickContainerEvent) {
                final Transaction<ItemStackSnapshot> cursor = ((ClickContainerEvent) event).cursorTransaction();
                ((ClickContainerEvent) event).slot().ifPresent(clicked -> {
                    this.plugin.logger().info("  Clicked: {}", InventoryTest.slotName(clicked));
                });
                this.plugin.logger().info("  Cursor: {}x{}->{}x{}", cursor.original().type().key(RegistryTypes.ITEM_TYPE), cursor.original().quantity(),
                        cursor.finalReplacement().type().key(RegistryTypes.ITEM_TYPE), cursor.finalReplacement().quantity());
                if (event instanceof CraftItemEvent.Preview) {
                    final SlotTransaction preview = ((CraftItemEvent.Preview) event).preview();
                    this.plugin.logger().info("  Preview: {}x{}->{}x{}", preview.original().type().key(RegistryTypes.ITEM_TYPE), preview.original().quantity(),
                            preview.finalReplacement().type().key(RegistryTypes.ITEM_TYPE), preview.finalReplacement().quantity());
                }
                if (event instanceof CraftItemEvent.Craft) {
                    final ItemStackSnapshot craft = ((CraftItemEvent.Craft) event).crafted();
                    this.plugin.logger().info("  Craft: {}x{}", craft.type().key(RegistryTypes.ITEM_TYPE), craft.quantity());
                }
            }
            if (event instanceof ChangeInventoryEvent.Drop) {
                final Slot hand = ((ChangeInventoryEvent.Drop) event).slot();
                this.plugin.logger().info("  Hand: {}", InventoryTest.slotName(hand));
            }
            if (event instanceof ClickContainerEvent.Creative.Drop) {
                final ItemStackSnapshot stack = ((ClickContainerEvent.Creative.Drop) event).droppedStack();
                this.plugin.logger().info("  Creative Drop: {}x{}", stack.type().key(RegistryTypes.ITEM_TYPE), stack.quantity());
            }
            if (event instanceof DropItemEvent.Dispense) {
                this.plugin.logger().info("  Dropping: {} entities", ((DropItemEvent.Dispense) event).entities().size());
            }

            for (final SlotTransaction slotTrans : event.transactions()) {
                this.plugin.logger().info("  SlotTr: {}x{}->{}x{}[{}]", slotTrans.original().type().key(RegistryTypes.ITEM_TYPE), slotTrans.original().quantity(),
                        slotTrans.finalReplacement().type().key(RegistryTypes.ITEM_TYPE), slotTrans.finalReplacement().quantity(), InventoryTest.slotName(slotTrans.slot()));
            }
        }

        @Listener
        public void onChangeEquipment(final ChangeEntityEquipmentEvent event, @Getter("transaction") final Transaction<@NonNull ItemStackSnapshot> transaction) {
            final Slot slot = event.slot();
            this.plugin.logger().info("Equipment: {}: {} {}->{}",
                    event.entity().type().key(RegistryTypes.ENTITY_TYPE),
                    slot.get(Keys.EQUIPMENT_TYPE).get().key(RegistryTypes.EQUIPMENT_TYPE),
                    transaction.original().type().key(RegistryTypes.ITEM_TYPE),
                    transaction.finalReplacement().type().key(RegistryTypes.ITEM_TYPE));
        }

        @Listener
        private void onTransfer(final TransferInventoryEvent event) {
            if (event instanceof TransferInventoryEvent.Post) {
                this.plugin.logger().info("{} {}=>{}", event.getClass().getSimpleName(), event.sourceInventory().getClass().getSimpleName(), event.targetInventory()
                        .getClass().getSimpleName());
                final Slot source = ((TransferInventoryEvent.Post) event).sourceSlot();
                final Integer sourceIdx = source.get(Keys.SLOT_INDEX).get();
                final Slot target = ((TransferInventoryEvent.Post) event).targetSlot();
                final Integer targetIdx = target.get(Keys.SLOT_INDEX).get();
                final ItemStackSnapshot item = ((TransferInventoryEvent.Post) event).transferredItem();
                this.plugin.logger().info("{}[{}] -> {}[{}] {}x{}", source.parent().getClass().getSimpleName(), sourceIdx,
                        target.parent().getClass().getSimpleName(), targetIdx, ItemTypes.registry().valueKey(item.type()), item.quantity());
            }
        }

        @Listener
        private void onCraft(final CraftItemEvent event) {
//            this.plugin.logger().info("{} size: {} recipe: {} ",
//                    event.getClass().getSimpleName(),
//                    event.craftingInventory().capacity(),
//                    event.recipe().map(Recipe::getKey).map(ResourceKey::asString).orElse("no recipe"));
        }
    }

    private static String slotName(Slot clicked) {
        final Optional<Integer> idx = clicked.get(Keys.SLOT_INDEX);
        final Optional<EquipmentType> equipmentType = clicked.get(Keys.EQUIPMENT_TYPE);
        return idx.map(String::valueOf).orElse(equipmentType.map(t -> t.key(RegistryTypes.EQUIPMENT_TYPE).asString()).orElse("?"));
    }

    @Override
    public void enable(final CommandContext ctx) {
        Sponge.eventManager().registerListeners(this.plugin, new InventoryTestListener(this.plugin));
    }

    private static void doFancyStuff(final PluginContainer plugin, final Player player) {

        final GridInventory inv27Grid = player.inventory().query(PrimaryPlayerInventory.class).get().storage();
        final Inventory inv27Slots = Inventory.builder().slots(27).completeStructure().plugin(plugin).build();
        final Inventory inv27Slots2 = Inventory.builder().slots(27).completeStructure().plugin(plugin).build();
        final ViewableInventory doubleMyInventory = ViewableInventory.builder().type(ContainerTypes.GENERIC_9X6.get())
                .grid(inv27Slots.slots(), Vector2i.from(9, 3), Vector2i.from(0, 0))
                .grid(inv27Slots2.slots(), Vector2i.from(9, 3), Vector2i.from(0, 3))
                .completeStructure()
                .carrier(player)
                .plugin(plugin)
                .build();
        final InventoryMenu menu = doubleMyInventory.asMenu();
        menu.setReadOnly(true);
        doubleMyInventory.set(0, ItemStack.of(ItemTypes.IRON_INGOT));
        doubleMyInventory.set(8, ItemStack.of(ItemTypes.GOLD_INGOT));
        doubleMyInventory.set(45, ItemStack.of(ItemTypes.EMERALD));
        doubleMyInventory.set(53, ItemStack.of(ItemTypes.DIAMOND));
        menu.registerSlotClick(new MySlotClickHandler(plugin, menu, doubleMyInventory));
        Sponge.server().scheduler().submit(Task.builder().plugin(plugin).execute(() -> {
            menu.open((ServerPlayer) player);
        }).build());
    }

    private static class MySlotClickHandler implements SlotClickHandler {

        private final InventoryMenu menu;
        private PluginContainer plugin;
        private final ViewableInventory primary;
        private ViewableInventory last;

        public MySlotClickHandler(PluginContainer plugin, final InventoryMenu menu, final ViewableInventory primary) {
            this.plugin = plugin;
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
                                .fillDummy().item(slot.peek().asImmutable())
                                .completeStructure().plugin(this.plugin).build();
                        Sponge.server().scheduler().submit(Task.builder().execute(() -> this.menu.setCurrentInventory(this.last)).plugin(this.plugin).build());
                        break;
                    default:
                        slot.set(ItemStack.of(ItemTypes.BEDROCK));
                }
                return false;
            } else if (slot.viewedSlot().parent() == this.last) {
                Sponge.server().scheduler().submit(Task.builder().execute(() -> this.menu.setCurrentInventory(this.primary)).plugin(this.plugin).build());
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
