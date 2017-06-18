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
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

@Plugin(id = "inventoryquerytest", name = "Inventory Query Test", description = "A plugin for testing inventory queries")
public class InventoryQueryTest {

    @Listener
    public void onInitialization(GameInitializationEvent e) {
        Command inventoryType = Command.builder()
                .targetedExecutor((cause, player, args) -> {
                    Inventory inventory = player.getInventory();
                    Inventory hotbar = inventory.query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class));
                    player.sendMessage(Text.of("You have ", hotbar.totalItems(), " items in your hotbar."));
                    return CommandResult.success();
                }, Player.class).build();

        Command itemType = Command.builder()
                .targetedExecutor((cause, player, args) -> {
                    Inventory inventory = player.getInventory();
                    Inventory sticks = inventory.query(QueryOperationTypes.ITEM_TYPE.of(ItemTypes.STICK));
                    player.sendMessage(Text.of("You have ", sticks.totalItems(), " sticks in your inventory."));
                    return CommandResult.success();
                }, Player.class).build();

        Command itemStackGeneral = Command.builder()
                .targetedExecutor((cause, player, args) -> {
                    Inventory inventory = player.getInventory();
                    ItemStack lapis = ItemStack.of(ItemTypes.DYE, 4);
                    lapis.offer(Keys.DYE_COLOR, DyeColors.BLUE);
                    Inventory lapisItems = inventory.query(QueryOperationTypes.ITEM_STACK_IGNORE_QUANTITY.of(lapis));
                    player.sendMessage(Text.of("You have ", lapisItems.totalItems(), " lapis lazuli in your inventory."));
                    return CommandResult.success();
                }, Player.class).build();

        Command itemStackSpecific = Command.builder()
                .targetedExecutor((cause, player, args) -> {
                    Inventory inventory = player.getInventory();
                    ItemStack lapis = ItemStack.of(ItemTypes.DYE, 4);
                    lapis.offer(Keys.DYE_COLOR, DyeColors.BLUE);
                    Inventory lapisItems = inventory.query(QueryOperationTypes.ITEM_STACK_EXACT.of(lapis));
                    player.sendMessage(Text.of("You have ", lapisItems.size(), " stacks of 4 lapis lazuli in your inventory."));
                    return CommandResult.success();
                }, Player.class).build();

        Command itemStackCustom = Command.builder()
                .targetedExecutor((cause, player, args) -> {
                    Inventory inventory = player.getInventory();
                    Inventory evenCountStacks = inventory.query(QueryOperationTypes.ITEM_STACK_CUSTOM.of(
                            x -> x.getQuantity() > 0 && x.getQuantity() % 2 == 0));
                    player.sendMessage(Text.of("You have ", evenCountStacks.size(), " stacks with an even number of items in your inventory."));
                    return CommandResult.success();
                }, Player.class).build();

        Command inventoryProperty = Command.builder()
                .targetedExecutor((cause, player, args) -> {
                    Inventory inventory = player.getInventory();
                    Inventory slots = ((PlayerInventory) inventory).getHotbar()
                            .query(QueryOperationTypes.INVENTORY_PROPERTY.of(new SlotIndex(3, Property.Operator.LESS)));
                    player.sendMessage(Text.of("You have ", slots.totalItems(), " items in the first 3 slots of your hotbar."));
                    return CommandResult.success();
                }, Player.class).build();

        Command inventoryTranslation = Command.builder()
                .targetedExecutor((cause, player, args) -> {
                    Inventory inventory = player.getInventory();
                    Inventory slots = ((PlayerInventory) inventory).getHotbar()
                            .query(QueryOperationTypes.INVENTORY_TRANSLATION.of(Sponge.getRegistry().getTranslationById("slot.name").get()));
                    player.sendMessage(Text.of("You have ", slots.totalItems(), " items in your hotbar."));
                    return CommandResult.success();
                }, Player.class).build();

        Sponge.getCommandManager().register(this, Command.builder()
                .child(inventoryType, "inventorytype")
                .child(itemType, "itemtype")
                .child(itemStackGeneral, "itemstackgeneral")
                .child(itemStackSpecific, "itemstackspecific")
                .child(itemStackCustom, "itemstackcustom")
                .child(inventoryProperty, "inventoryproperty")
                .child(inventoryTranslation, "inventorytranslation")
                .build(), "invquery");
    }

}
