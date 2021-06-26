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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.carrier.chest.Chest;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.custom.CarriedWrapperInventory;
import org.spongepowered.common.inventory.custom.CustomInventory;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.plugin.PluginContainer;

import java.util.Optional;

import net.minecraft.world.CompoundContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;

public final class InventoryUtil {

    private InventoryUtil() {}

    public static CraftingGridInventory toSpongeInventory(final CraftingContainer inv) {
        return (CraftingGridInventory) inv;
    }

    @SuppressWarnings("unchecked")
    public static <C extends net.minecraft.world.Container> C toNativeInventory(final Inventory inv) {
        if (inv instanceof CraftingContainer) {
            return (C) inv;
        }
        if (inv instanceof Container) {
            for (final Object inventory : ((InventoryAdapter) inv).inventoryAdapter$getFabric().fabric$allInventories()) {
                if (inventory instanceof CraftingContainer) {
                    return (C) inventory;
                }
            }
        }

        // Gather Debug Info...
        throw new IllegalStateException("Invalid CraftingGridInventory. Could not find CraftingInventory.\nInventory was: " + inv.getClass().getSimpleName());
    }

    public static Optional<Inventory> getDoubleChestInventory(final ChestBlockEntity chest) {
        final Optional<Chest> connectedChestOptional = ((Chest) chest).connectedChest();
        if (!connectedChestOptional.isPresent()) {
            return Optional.empty();
        }

        final ChestType chestType = chest.getBlockState().getValue(ChestBlock.TYPE);
        final ChestBlockEntity connectedChest = (ChestBlockEntity) connectedChestOptional.get();
        // Logic in the instanceof check of ChestBlock.getChestInventory but with exploded ternary operators.
        if (chestType == ChestType.RIGHT) {
            return Optional.of((Inventory) new CompoundContainer(chest, connectedChest));
        } else {
            return Optional.of((Inventory) new CompoundContainer(connectedChest, chest));
        }
    }

    // Utility
    public static Inventory toInventory(final net.minecraft.world.Container inventory) {
        return InventoryUtil.toInventory(inventory, null);
    }

    public static Inventory toInventory(Object inventory, final @Nullable Object forgeItemHandler) {
        if (forgeItemHandler == null) {
            if (inventory instanceof ChestBlockEntity) {
                inventory = InventoryUtil.getDoubleChestInventory(((ChestBlockEntity) inventory)).orElse(((Inventory) inventory));
            }
            if (inventory instanceof Inventory) {
                return ((Inventory) inventory);
            }
        }
        if (forgeItemHandler instanceof Inventory) {
            return ((Inventory) forgeItemHandler);
        }
        return PlatformHooks.INSTANCE.getInventoryHooks().toInventory(inventory, forgeItemHandler);
    }

    public static InventoryAdapter findAdapter(final Object inventory) {
        if (inventory instanceof InventoryAdapter) {
            return ((InventoryAdapter) inventory);
        }
        return PlatformHooks.INSTANCE.getInventoryHooks().findInventoryAdapter(inventory);
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
            final Optional<?> carrier = ((CarriedInventory<?>) inventory).carrier();
            if (carrier.isPresent()) {
                inventory = carrier.get();
            }
        }

        final Object base = inventory;

        if (base instanceof BlockEntity) {
            final ResourceKey key = Sponge.game().registries().registry(RegistryTypes.BLOCK_ENTITY_TYPE).valueKey(((BlockEntity) base).type());
            final String pluginId = key.namespace();
            container = Sponge.pluginManager().plugin(pluginId)
                    .orElseThrow(() -> new AssertionError("Missing plugin " + pluginId + " for block " + key.namespace() + ":" + key.value()));
        } else if (base instanceof Entity) {
            final ResourceKey key = (ResourceKey) (Object) EntityType.getKey((EntityType<?>) ((Entity) base).type());
            final String pluginId = key.namespace();
            container = Sponge.pluginManager().plugin(pluginId).orElseGet(() -> {
                SpongeCommon.logger().debug("Unknown plugin for [{}]", base);
                return Launch.instance().minecraftPlugin();
            });
        } else if (base instanceof SpongeUser) {
            container = Launch.instance().minecraftPlugin();
        } else {
            container = Sponge.pluginManager().plugin(PlatformHooks.INSTANCE
                .getInventoryHooks()
                .getModIdFromInventory(base.getClass()))
                .orElseGet(() -> {
                    SpongeCommon.logger().debug("Unknown plugin for [{}]", base);
                    return Launch.instance().minecraftPlugin();
                });
        }
        return container;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Carrier> CarriedInventory<T> carriedWrapperInventory(
        final net.minecraft.world.Container inventory, final T carrier) {
        return (CarriedInventory<T>) new CarriedWrapperInventory(inventory, carrier);
    }
}
