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
package org.spongepowered.common.item.inventory.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.OrderedInventory;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.IMixinInventory;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.CraftingGridInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.CraftingInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.comp.CraftingGridInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.comp.CraftingGridInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.fabric.DefaultInventoryFabric;
import org.spongepowered.common.item.inventory.lens.impl.slots.SlotLensImpl;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public final class InventoryUtil {

    private InventoryUtil() {}

    public static CraftingGridInventory toSpongeInventory(InventoryCrafting inv) {
        DefaultInventoryFabric fabric = new DefaultInventoryFabric(inv);
        CraftingGridInventoryLens lens = new CraftingGridInventoryLensImpl(0, inv.getWidth(), inv.getHeight(), inv.getWidth(), SlotLensImpl::new);

        return new CraftingGridInventoryAdapter(fabric, lens);
    }

    public static InventoryCrafting toNativeInventory(CraftingGridInventory inv) {
        Fabric<IInventory> fabric = ((CraftingInventoryAdapter) inv).getInventory();
        Iterator<IInventory> inventories = fabric.allInventories().iterator();
        InventoryCrafting inventoryCrafting = (InventoryCrafting) inventories.next();

        if (inventories.hasNext()) {
            throw new IllegalStateException("Another inventory found: " + inventories.next());
        }

        return inventoryCrafting;
    }

    public static Optional<Inventory> getDoubleChestInventory(TileEntityChest chest) {
        // BlockChest#getContainer(World, BlockPos, boolean) without isBlocked() check
        for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            BlockPos blockpos = chest.getPos().offset(enumfacing);

            TileEntity tileentity1 = chest.getWorld().getTileEntity(blockpos);

            if (tileentity1 instanceof TileEntityChest && tileentity1.getBlockType() == chest.getBlockType()) {

                InventoryLargeChest inventory;

                if (enumfacing != EnumFacing.WEST && enumfacing != EnumFacing.NORTH) {
                    inventory = new InventoryLargeChest("container.chestDouble", chest, (TileEntityChest) tileentity1);
                } else {
                    inventory = new InventoryLargeChest("container.chestDouble", (TileEntityChest) tileentity1, chest);
                }

                return Optional.of((Inventory) inventory);
            }
        }
        return Optional.empty();
    }

}
