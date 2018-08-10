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

import com.google.common.base.Preconditions;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryTransformations;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.item.inventory.property.EquipmentSlotType;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

@Plugin(id = "inventoryquerytest", name = "Inventory Query Test", description = "A plugin for testing inventory queries", version = "0.0.0")
public class InventoryQueryTest {

    @Listener
    public void onInitialization(GameInitializationEvent e) {
        CommandSpec inventoryType = CommandSpec.builder()
                .executor((src, args) -> {
                    Inventory inventory = getPlayerInventory(src);
                    Inventory hotbar = inventory.query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class));
                    src.sendMessage(Text.of("You have ", hotbar.totalItems(), " items in your hotbar."));
                    return CommandResult.success();
                }).build();

        CommandSpec itemType = CommandSpec.builder()
                .executor((src, args) -> {
                    Inventory inventory = getPlayerInventory(src);
                    Inventory sticks = inventory.query(QueryOperationTypes.ITEM_TYPE.of(ItemTypes.STICK));
                    src.sendMessage(Text.of("You have ", sticks.totalItems(), " sticks in your inventory."));
                    return CommandResult.success();
                }).build();

        CommandSpec itemStackGeneral = CommandSpec.builder()
                .executor((src, args) -> {
                    Inventory inventory = getPlayerInventory(src);
                    ItemStack lapis = ItemStack.of(ItemTypes.DYE, 4);
                    lapis.offer(Keys.DYE_COLOR, DyeColors.BLUE);
                    Inventory lapisItems = inventory.query(QueryOperationTypes.ITEM_STACK_IGNORE_QUANTITY.of(lapis));
                    src.sendMessage(Text.of("You have ", lapisItems.totalItems(), " lapis lazuli in your inventory."));
                    return CommandResult.success();
                }).build();

        CommandSpec itemStackSpecific = CommandSpec.builder()
                .executor((src, args) -> {
                    Inventory inventory = getPlayerInventory(src);
                    ItemStack lapis = ItemStack.of(ItemTypes.DYE, 4);
                    lapis.offer(Keys.DYE_COLOR, DyeColors.BLUE);
                    Inventory lapisItems = inventory.query(QueryOperationTypes.ITEM_STACK_EXACT.of(lapis));
                    src.sendMessage(Text.of("You have ", lapisItems.size(), " stacks of 4 lapis lazuli in your inventory."));
                    return CommandResult.success();
                }).build();

        CommandSpec itemStackCustom = CommandSpec.builder()
                .executor((src, args) -> {
                    Inventory inventory = getPlayerInventory(src);
                    Inventory evenCountStacks = inventory.query(QueryOperationTypes.ITEM_STACK_CUSTOM.of(
                            x -> x.getQuantity() > 0 && x.getQuantity() % 2 == 0));
                    src.sendMessage(Text.of("You have ", evenCountStacks.size(), " stacks with an even number of items in your inventory."));
                    return CommandResult.success();
                }).build();

        CommandSpec inventoryProperty = CommandSpec.builder()
                .executor((src, args) -> {
                    Inventory inventory = getPlayerInventory(src);
                    Inventory slots = ((PlayerInventory) inventory).getHotbar()
                            .query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.builder().value(3).operator(Property.Operator.LESS).build()));
                    src.sendMessage(Text.of("You have ", slots.totalItems(), " items in the first 3 slots of your hotbar."));
                    return CommandResult.success();
                }).build();

        CommandSpec inventoryTranslation = CommandSpec.builder()
                .executor((src, args) -> {
                    Inventory inventory = getPlayerInventory(src);
                    Inventory slots = ((PlayerInventory) inventory).getHotbar()
                            .query(QueryOperationTypes.INVENTORY_TRANSLATION.of(Sponge.getRegistry().getTranslationById("slot.name").get()));
                    src.sendMessage(Text.of("You have ", slots.totalItems(), " items in your hotbar."));
                    return CommandResult.success();
                }).build();

        CommandSpec inventoryTransform = CommandSpec.builder()
                .executor((src, args) -> {
                    Inventory inventory = getPlayerInventory(src);
                    inventory.transform(InventoryTransformations.PLAYER_PRIMARY_HOTBAR_FIRST)
                             .transform(InventoryTransformations.REVERSE).offer(ItemStack.of(ItemTypes.PAPER, 46));

                    src.sendMessage(Text.of("Added paper to hotbar last."));
                    return CommandResult.success();
                }).build();

        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .child(inventoryType, "inventorytype")
                .child(itemType, "itemtype")
                .child(itemStackGeneral, "itemstackgeneral")
                .child(itemStackSpecific, "itemstackspecific")
                .child(itemStackCustom, "itemstackcustom")
                .child(inventoryProperty, "inventoryproperty")
                .child(inventoryTranslation, "inventorytranslation")
                .child(inventoryTransform, "inventorytransform")
                .build(), "invquery");
    }

    private Inventory getPlayerInventory(CommandSource source) throws CommandException {
        if (source instanceof Player) {
            return ((Player) source).getInventory();
        }
        throw new CommandException(Text.of("You must run this command as a player!"));
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Player joined = event.getTargetEntity();
        Inventory inventory = joined.getInventory();

        Inventory chestPlate = EquipmentSlotType.of(EquipmentTypes.CHESTPLATE).queryIn(inventory);
        Preconditions.checkState(chestPlate.capacity() == 1, "ChestPlate Slot should be 1 but is " + chestPlate.capacity());

        Inventory wornSlots = EquipmentSlotType.of(EquipmentTypes.WORN).queryIn(inventory);
        // 4 armor slots
        Preconditions.checkState(wornSlots.capacity() == 4, "Worn Slots should be 4 but is " + wornSlots.capacity());

        Inventory equipped = EquipmentSlotType.of(EquipmentTypes.EQUIPPED).queryIn(inventory);
        // 4 armor slots + OFF_HAND + MAIN_HAND
        Preconditions.checkState(equipped.capacity() == 6, "Equipped Slots should be 6 but is " + equipped.capacity());
    }

}
