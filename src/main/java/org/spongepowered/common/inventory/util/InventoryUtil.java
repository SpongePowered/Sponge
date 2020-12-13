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
package org.spongepowered.common.inventory.util;

import net.minecraft.block.ChestBlock;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.DoubleSidedInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.ChestTileEntity;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.carrier.chest.Chest;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.adapter.impl.comp.CraftingGridInventoryAdapter;
import org.spongepowered.common.inventory.custom.CarriedWrapperInventory;
import org.spongepowered.common.inventory.custom.CustomInventory;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.impl.comp.CraftingGridInventoryLens;
import org.spongepowered.common.inventory.lens.impl.slot.BasicSlotLens;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.plugin.PluginContainer;

import javax.annotation.Nullable;
import java.util.Optional;

public final class InventoryUtil {

    private InventoryUtil() {}

    public static CraftingGridInventory toSpongeInventory(final CraftingInventory inv) {
        final CraftingGridInventoryLens lens = new CraftingGridInventoryLens(0, inv.getWidth(), inv.getHeight(), BasicSlotLens::new);

        return new CraftingGridInventoryAdapter((Fabric) inv, lens);
    }

    @SuppressWarnings("unchecked")
    public static <C extends IInventory> C toNativeInventory(final Inventory inv) {
        final Fabric fabric = ((CraftingGridInventoryAdapter) inv).inventoryAdapter$getFabric();
        for (final Object inventory : fabric.fabric$allInventories()) {
            if (inventory instanceof CraftingInventory) {
                return (C) inventory;
            }
        }

        // Gather Debug Info...
        final StringBuilder sb = new StringBuilder();
        sb.append("Invalid CraftingGridInventory. Could not find InventoryCrafting.\n")
          .append("Fabric was: ")
          .append(fabric.getClass().getSimpleName()).append(" Name: ")
          .append("Viewed:");
        for (final Object iInventory : fabric.fabric$allInventories()) {
            sb.append("\n").append(iInventory.getClass().getName());
        }

        throw new IllegalStateException(sb.toString());
    }

    public static Optional<Inventory> getDoubleChestInventory(final ChestTileEntity chest) {
        final Optional<Chest> connectedChestOptional = ((Chest) chest).getConnectedChest();
        if (!connectedChestOptional.isPresent()) {
            return Optional.empty();
        }

        final ChestType chestType = chest.getBlockState().getValue(ChestBlock.TYPE);
        final ChestTileEntity connectedChest = (ChestTileEntity) connectedChestOptional.get();
        // Logic in the instanceof check of ChestBlock.getChestInventory but with exploded ternary operators.
        if (chestType == ChestType.RIGHT) {
            return Optional.of((Inventory) new DoubleSidedInventory(chest, connectedChest));
        } else {
            return Optional.of((Inventory) new DoubleSidedInventory(connectedChest, chest));
        }
    }

    // Utility
    public static Inventory toInventory(final IInventory inventory) {
        return InventoryUtil.toInventory(inventory, null);
    }

    public static Inventory toInventory(Object inventory, @Nullable final Object forgeItemHandler) {
        if (forgeItemHandler == null) {
            if (inventory instanceof ChestTileEntity) {
                inventory = InventoryUtil.getDoubleChestInventory(((ChestTileEntity) inventory)).orElse(((Inventory) inventory));
            }
            if (inventory instanceof Inventory) {
                return ((Inventory) inventory);
            }
        }
        if (forgeItemHandler instanceof Inventory) {
            return ((Inventory) forgeItemHandler);
        }
        return PlatformHooks.getInstance().getInventoryHooks().toInventory(inventory, forgeItemHandler);
    }

    public static InventoryAdapter findAdapter(final Object inventory) {
        if (inventory instanceof InventoryAdapter) {
            return ((InventoryAdapter) inventory);
        }
        return PlatformHooks.getInstance().getInventoryHooks().findInventoryAdapter(inventory);
    }

    public static TrackedInventoryBridge forCapture(final Object toCapture) {
        if (toCapture instanceof TrackedInventoryBridge) {
            return ((TrackedInventoryBridge) toCapture);
        }
        return null;
    }

    public static PluginContainer getPluginContainer(Object inventory) {
        // TODO maybe caching?
        final PluginContainer container;

        if (inventory instanceof CustomInventory) {
            return ((CustomInventory)inventory).getPlugin();
        }

        if (inventory instanceof CarriedInventory) {
            final Optional<?> carrier = ((CarriedInventory<?>) inventory).getCarrier();
            if (carrier.isPresent()) {
                inventory = carrier.get();
            }
        }

        final Object base = inventory;

        if (base instanceof BlockEntity) {
            final ResourceKey key = ((BlockEntity) base).getBlock().getType().getKey();
            final String pluginId = key.getNamespace();
            container = Sponge.getPluginManager().getPlugin(pluginId)
                    .orElseThrow(() -> new AssertionError("Missing plugin " + pluginId + " for block " + key.getNamespace() + ":" + key.getValue()));
        } else if (base instanceof Entity) {
            final ResourceKey key = ((Entity) base).getType().getKey();
            final String pluginId = key.getNamespace();
            container = Sponge.getPluginManager().getPlugin(pluginId).orElseGet(() -> {
                SpongeCommon.getLogger().debug("Unknown plugin for [{}]", base);
                return Launch.getInstance().getMinecraftPlugin();
            });
        } else if (base instanceof SpongeUser) {
            container = Launch.getInstance().getMinecraftPlugin();
        } else {
            container = Sponge.getPluginManager().getPlugin(PlatformHooks.getInstance()
                .getInventoryHooks()
                .getModIdFromInventory(base.getClass()))
                .orElseGet(() -> {
                    SpongeCommon.getLogger().debug("Unknown plugin for [{}]", base);
                    return Launch.getInstance().getMinecraftPlugin();
                });
        }
        return container;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Carrier> CarriedInventory<T> carriedWrapperInventory(
        final net.minecraft.inventory.IInventory inventory, final T carrier) {
        return (CarriedInventory<T>) new CarriedWrapperInventory(inventory, carrier);
    }
}
