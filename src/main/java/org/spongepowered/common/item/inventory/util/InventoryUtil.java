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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.CraftingGridInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.impl.comp.CraftingGridInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.slots.SlotLensImpl;

import java.util.Optional;

import javax.annotation.Nullable;

public final class InventoryUtil {

    private InventoryUtil() {}

    @SuppressWarnings("rawtypes")
    public static CraftingGridInventory toSpongeInventory(InventoryCrafting inv) {
        CraftingGridInventoryLensImpl lens = new CraftingGridInventoryLensImpl(0, inv.func_174922_i(), inv.func_174923_h(), inv.func_174922_i(), SlotLensImpl::new);

        return new CraftingGridInventoryAdapter((Fabric) inv, lens);
    }

    public static InventoryCrafting toNativeInventory(CraftingGridInventory inv) {
        Fabric fabric = ((CraftingGridInventoryAdapter) inv).bridge$getFabric();
        for (Object inventory : fabric.fabric$allInventories()) {
            if (inventory instanceof InventoryCrafting) {
                return ((InventoryCrafting) inventory);
            }
        }

        // Gather Debug Info...
        StringBuilder sb = new StringBuilder();
        sb.append("Invalid CraftingGridInventory. Could not find InventoryCrafting.\n")
          .append("Fabric was: ")
          .append(fabric.getClass().getSimpleName()).append(" Name: ")
          .append(fabric.fabric$getDisplayName() == null ? "unknown" : fabric.fabric$getDisplayName().get())
          .append("Viewed:");
        for (Object iInventory : fabric.fabric$allInventories()) {
            sb.append("\n").append(iInventory.getClass().getName());
        }

        throw new IllegalStateException(sb.toString());
    }

    public static Optional<Inventory> getDoubleChestInventory(TileEntityChest chest) {
        // BlockChest#getContainer(World, BlockPos, boolean) without isBlocked() check
        for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            BlockPos blockpos = chest.func_174877_v().func_177972_a(enumfacing);

            TileEntity tileentity1 = chest.func_145831_w().func_175625_s(blockpos);

            if (tileentity1 instanceof TileEntityChest && tileentity1.func_145838_q() == chest.func_145838_q()) {

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

    // Utility
    public static Inventory toInventory(IInventory inventory) {
        return toInventory(inventory, null);
    }

    public static Inventory toInventory(Object inventory, @Nullable Object forgeItemHandler) {
        if (forgeItemHandler == null) {
            if (inventory instanceof TileEntityChest) {
                inventory = getDoubleChestInventory(((TileEntityChest) inventory)).orElse(((Inventory) inventory));
            }
            if (inventory instanceof Inventory) {
                return ((Inventory) inventory);
            }
        }
        if (forgeItemHandler instanceof Inventory) {
            return ((Inventory) forgeItemHandler);
        }
        return SpongeImplHooks.toInventory(inventory, forgeItemHandler);
    }

    public static InventoryAdapter findAdapter(Object inventory) {
        if (inventory instanceof InventoryAdapter) {
            return ((InventoryAdapter) inventory);
        }
        return SpongeImplHooks.findInventoryAdapter(inventory);
    }


    public static TrackedInventoryBridge forCapture(Object toCapture) {
        if (toCapture instanceof TrackedInventoryBridge) {
            return ((TrackedInventoryBridge) toCapture);
        }
        return null;
    }

    public static PluginContainer getPluginContainer(Object inventory) {
        PluginContainer container;

        if (inventory instanceof CarriedInventory) {
            final Optional<?> carrier = ((CarriedInventory<?>) inventory).getCarrier();
            if (carrier.isPresent()) {
                inventory = carrier.get();
            }
        }

        final Object base = inventory;

        if (base instanceof org.spongepowered.api.block.tileentity.TileEntity) {
            final String id = ((org.spongepowered.api.block.tileentity.TileEntity) base).getBlock().getType().getId();
            final String pluginId = id.substring(0, id.indexOf(":"));
            container = Sponge.getPluginManager().getPlugin(pluginId)
                    .orElseThrow(() -> new AssertionError("Missing plugin " + pluginId + " for block " + id));
        } else if (base instanceof Entity) {
            final String id = ((Entity) base).getType().getId();
            final String pluginId = id.substring(0, id.indexOf(":"));
            container = Sponge.getPluginManager().getPlugin(pluginId).orElseGet(() -> {
                SpongeImpl.getLogger().debug("Unknown plugin for [{}]", base);
                return SpongeImpl.getMinecraftPlugin(); 
            });
        } else if (base instanceof SpongeUser) {
            container = SpongeImpl.getMinecraftPlugin();
        } else {
            container = Sponge.getPluginManager().getPlugin(SpongeImplHooks.getModIdFromClass(base.getClass())).orElseGet(() -> {
                SpongeImpl.getLogger().debug("Unknown plugin for [{}]", base);
                return SpongeImpl.getMinecraftPlugin();
            });
        }
        return container;
    }
}
