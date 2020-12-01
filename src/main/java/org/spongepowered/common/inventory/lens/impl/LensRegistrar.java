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
package org.spongepowered.common.inventory.lens.impl;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.item.minecart.ChestMinecartEntity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.WanderingTraderEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.DoubleSidedInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.BarrelTileEntity;
import net.minecraft.tileentity.BlastFurnaceTileEntity;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.tileentity.DropperTileEntity;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.tileentity.ShulkerBoxTileEntity;
import net.minecraft.tileentity.SmokerTileEntity;
import net.minecraft.tileentity.TrappedChestTileEntity;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.comp.CraftingInventoryLens;
import org.spongepowered.common.inventory.lens.impl.comp.PrimaryPlayerInventoryLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.BrewingStandInventoryLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.FurnaceInventoryLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.LargeChestInventoryLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.SingleGridLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.SingleIndexedLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.container.ContainerLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.container.ContainerPlayerInventoryLens;
import org.spongepowered.common.inventory.lens.impl.slot.BasicSlotLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.inventory.lens.slots.SlotLens;
import org.spongepowered.common.inventory.util.ContainerUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Register known Lenses here
 */
public class LensRegistrar {

    // Class of Inventory -> Size -> Lens
    private static Map<Class<?>, Int2ObjectMap<Lens>> lenses = new HashMap<>();

    private static Map<Class<?>, LensFactory> lensFactories = new HashMap<>();


    static {

        LensRegistrar.register((inv, size, slp) -> LensRegistrar.lensGrid(inv, size, 9, 3, slp),
                ChestTileEntity.class,
                ShulkerBoxTileEntity.class,
                TrappedChestTileEntity.class,
                BarrelTileEntity.class,
                EnderChestInventory.class,
                ChestMinecartEntity.class
        );

        LensRegistrar.register((inv, size, slp) -> LensRegistrar.lensGrid(inv, size, 3,3, slp),
                DispenserTileEntity.class,
                DropperTileEntity.class,
                CraftingInventory.class);

        LensRegistrar.register((inv, size, slp) -> LensRegistrar.lensGrid(inv, size, 2, 2, slp),
                CraftingInventory.class);

        LensRegistrar.register((inv, size, slp) -> LensRegistrar.lensGrid(inv, size, 5, 1, slp),
                HopperTileEntity.class);

        LensRegistrar.register(
            LensRegistrar.restricted(LensRegistrar::lensFurnace, s -> s == 3),
                AbstractFurnaceTileEntity.class,
                SmokerTileEntity.class,
                FurnaceTileEntity.class,
                BlastFurnaceTileEntity.class
        );

        LensRegistrar.register(LensRegistrar.restricted(LensRegistrar::lensBrewingStandTileEntity, s -> s == 5), BrewingStandTileEntity.class);
        LensRegistrar.register(LensRegistrar.restricted(LensRegistrar::lensDoubleSided, s -> s == 2 * 9 * 3), DoubleSidedInventory.class);

        LensRegistrar.register(LensRegistrar.restricted(LensRegistrar::lensRepairContainer, s -> s == 2 + 1 + 9 * 3), RepairContainer.class);
        LensRegistrar.register(LensRegistrar.restricted(LensRegistrar::lensWorkbenchContainer,s -> s == 1 + 3 * 3 + 9 * 3), WorkbenchContainer.class);

        LensRegistrar.register(LensRegistrar.restricted(LensRegistrar::lensPlayerContainer, s -> s == 1+ 4 + 4 + 9*3 + 1), PlayerContainer.class);

        LensRegistrar.register(
            LensRegistrar.restricted(LensRegistrar::generateLens, s -> s == 8),
                AbstractVillagerEntity.class,
                VillagerEntity.class,
                WanderingTraderEntity.class);

        LensRegistrar.register(LensRegistrar.restricted(LensRegistrar::lensSlot, s -> s == 1), CraftResultInventory.class);
    }

    private static Lens lensSlot(Object inventory, int size, SlotLensProvider slotLensProvider) {
        return slotLensProvider.getSlotLens(0);
    }

    public static void register(LensFactory lensFactory, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            LensRegistrar.lensFactories.put(clazz, lensFactory);
        }
    }

    public static LensFactory restricted(LensFactory original, Predicate<Integer> sizeRestriction) {
        return (inventory, size, slotLensProvider) -> {
            if (sizeRestriction.test(size)) {
                return original.apply(inventory, size, slotLensProvider);
            }
            return null;
        };
    }

    public static Lens getLens(Object inventory, SlotLensProvider slotLensProvider, int size) {
        return LensRegistrar.getLenses(inventory.getClass()).computeIfAbsent(size, k -> LensRegistrar.generateLens(inventory, size, slotLensProvider));
    }

    private static Int2ObjectMap<Lens> getLenses(Class<?> inventory) {
        return LensRegistrar.lenses.computeIfAbsent(inventory, k -> new Int2ObjectOpenHashMap<>());
    }

    private interface LensFactory {
        @Nullable Lens apply(Object inventory, int size, SlotLensProvider slotLensProvider);
    }

    @SuppressWarnings("unchecked")
    private static Lens generateLens(Object inventory, int size, SlotLensProvider slotLensProvider) {

        if (size == 0) {
            return new DefaultEmptyLens();
        }

        LensFactory lensFactory = LensRegistrar.lensFactories.get(inventory.getClass());
        Lens lens = null;
        if (lensFactory != null) {
            lens = lensFactory.apply(inventory.getClass(), size, slotLensProvider);
            if (lens != null) {
                return lens;
            }
        }

        if (inventory instanceof CraftingInventory) {
            lens = LensRegistrar.lensGrid(inventory, size, ((CraftingInventory) inventory).getWidth(), ((CraftingInventory) inventory).getHeight(), slotLensProvider);
        }
        else if (inventory instanceof Container) {
            lens = ContainerUtil.generateLens(((Container) inventory), slotLensProvider);
        } else if (size == 1) {
            return slotLensProvider.getSlotLens(0);
        }
        if (lens != null) {
            return lens;
        }
        return new SingleIndexedLens(0, size, (Class<? extends Inventory>) inventory.getClass(), slotLensProvider);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static Lens lensGrid(Object inventory, int size, int width, int height, SlotLensProvider slotLensProvider) {
        if (size != width * height) {
            return null; // Wrong size
        }
        return new SingleGridLens(0, width, height, (Class<? extends Inventory>) inventory.getClass(), slotLensProvider);
    }

    @SuppressWarnings("unchecked")
    private static Lens lensBrewingStandTileEntity(Object inventory, int size, SlotLensProvider slotLensProvider) {
        return new BrewingStandInventoryLens(size, (Class<? extends Inventory>) inventory.getClass(), slotLensProvider);
    }

    @SuppressWarnings("unchecked")
    private static Lens lensFurnace(Object inventory, int size, SlotLensProvider slotLensProvider) {
        return new FurnaceInventoryLens(size, (Class<? extends Inventory>) inventory.getClass(), slotLensProvider);
    }
    @SuppressWarnings("unchecked")
    private static Lens lensDoubleSided(Object inventory, int size, SlotLensProvider slotLensProvider) {
        return new LargeChestInventoryLens(size, (Class<? extends Inventory>) inventory.getClass(), slotLensProvider);
    }

    @SuppressWarnings("unchecked")
    private static Lens lensRepairContainer(Object inventory, int size, SlotLensProvider slotLensProvider) {
        final List<Lens> lenses = new ArrayList<>();
        lenses.add(new DefaultIndexedLens(0, 3, slotLensProvider));
        lenses.add(new PrimaryPlayerInventoryLens(3, slotLensProvider, true));
        return new ContainerLens(size, (Class<? extends Inventory>) inventory.getClass(), slotLensProvider, lenses);
    }

    @SuppressWarnings("unchecked")
    private static Lens lensWorkbenchContainer(Object inventory, int size, SlotLensProvider slotLensProvider) {
        final List<Lens> lenses = new ArrayList<>();
        lenses.add(new CraftingInventoryLens(0, 1, 3, 3, slotLensProvider));
        lenses.add(new PrimaryPlayerInventoryLens(3 * 3 + 1, slotLensProvider, true));
        return new ContainerLens(size, (Class<? extends Inventory>)  inventory.getClass(), slotLensProvider, lenses);
    }

    @SuppressWarnings("unchecked")
    private static Lens lensPlayerContainer(Object inventory, int size, SlotLensProvider slotLensProvider) {
        return new ContainerPlayerInventoryLens(size, (Class<? extends Inventory>) inventory.getClass(), slotLensProvider);
    }

    public static class BasicSlotLensProvider implements SlotLensProvider {

        private static Map<Integer, BasicSlotLens> basicSlotLenses = new ConcurrentHashMap<>();

        public final int base;
        public final int size;

        public BasicSlotLensProvider(int size) {
            this(0, size);
        }

        public BasicSlotLensProvider(int base, int size) {
            this.base = base;
            this.size = size;
        }

        @Override
        public SlotLens getSlotLens(int index) {
            return BasicSlotLensProvider.basicSlotLenses.computeIfAbsent(index, BasicSlotLens::new);
        }

        @Override
        public String toString() {
            return "SlotLensProvider[base=" + this.base + "][size=" + this.size + "]";
        }
    }


}
























