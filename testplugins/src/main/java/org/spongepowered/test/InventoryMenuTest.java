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

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ContainerTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.menu.InventoryMenu;
import org.spongepowered.api.item.inventory.menu.handler.InventoryCallbackHandler;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.math.vector.Vector2i;

import java.util.Arrays;

@Plugin(id = "inventorymenutest", name = "Inventory Menu Test", description = "A plugin to test inventory menus", version = "0.0.0")
public class InventoryMenuTest implements LoadableModule {

    @Override
    public void enable(MessageReceiver src) {
        // TODO actually make it do smth.
    }

    public static void foobar2() {

        Player player = null;
        Player player2 = null;

        Inventory inv = Inventory.builder().grid(3, 3).completeStructure().build();

        ItemStackSnapshot disabled = ItemStack.of(ItemTypes.LIGHT_GRAY_STAINED_GLASS_PANE, 1).createSnapshot();
        ItemStackSnapshot emerald = ItemStack.of(ItemTypes.EMERALD, 1).createSnapshot();

        ViewableInventory display = ViewableInventory.builder().type(ContainerTypes.GENERIC_3x3)
                .fillDummy().item(disabled)
                .slots(Arrays.asList(inv.query(GridInventory.class).get().getSlot(1,1).get()), new Vector2i(1, 1))
                .completeStructure().build();
        display.query(GridInventory.class).get().set(1,1, ItemStack.of(ItemTypes.DIAMOND, 1));

        ViewableInventory display2 = ViewableInventory.builder().type(ContainerTypes.GENERIC_3x3)
                .fillDummy()
                .dummySlots(1, new Vector2i(1,1)).item(emerald)
                .completeStructure().build();
        display.query(GridInventory.class).get().set(1,1, ItemStack.of(ItemTypes.DIAMOND, 1));


        ViewableInventory basicChest = ViewableInventory.builder()
                .type(ContainerTypes.GENERIC_9x3)
                .completeStructure()
                .build();


        InventoryMenu menu = InventoryMenu.of(display);
        menu.open(player);
        menu.open(player2);

        menu.setCurrentInventory(display2); // matching ContainerType so the inventory is silently swapped
        menu.setTitle(Text.of("This reopens containers"));
        menu.registerSlotClick((cause, container, slot, slotIndex, clickType) -> checkClick());

        menu.setReadOnly(false);
        MyHandler handler = new MyHandler();
        menu.registerHandler(handler);
        menu.registerChange((cause, container, slot, slotIndex, oldStack, newStack) -> checkAllChange());

        menu.setReadOnly(true);
        menu.setCurrentInventory(basicChest);
        menu.unregisterAll(); // already done as changing the ContainerType clears all callbacks
    }


    static boolean checkClick() { return true; }
    static boolean checkAllChange() { return false; }


    static class MyHandler implements InventoryCallbackHandler {}

}