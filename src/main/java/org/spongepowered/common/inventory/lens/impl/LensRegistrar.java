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
import org.spongepowered.common.bridge.inventory.InventoryBridge;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.comp.CraftingInventoryLens;
import org.spongepowered.common.inventory.lens.impl.comp.GridInventoryLens;
import org.spongepowered.common.inventory.lens.impl.comp.PrimaryPlayerInventoryLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.BrewingStandInventoryLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.FurnaceInventoryLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.LargeChestInventoryLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.container.ContainerLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.container.ContainerPlayerInventoryLens;
import org.spongepowered.common.inventory.lens.impl.slot.BasicSlotLens;
import org.spongepowered.common.inventory.lens.impl.slot.CraftingOutputSlotLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.inventory.lens.slots.SlotLens;
import org.spongepowered.common.inventory.util.ContainerUtil;
import org.spongepowered.plugin.meta.util.NonnullByDefault;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Register known Lenses here
 */
@NonnullByDefault
public class LensRegistrar {

    // Class of Inventory -> Size -> Lens
    private static Map<Class, Int2ObjectMap<Lens>> lenses = new HashMap<>();

    private static Map<Class, LensFactory> lensFactories = new HashMap<>();


    static {

        register((inv, size, slp) -> lensGrid(inv, size, 9, 3, slp),
                ChestTileEntity.class,
                ShulkerBoxTileEntity.class,
                TrappedChestTileEntity.class,
                BarrelTileEntity.class,
                EnderChestInventory.class,
                ChestMinecartEntity.class
        );

        register((inv, size, slp) -> lensGrid(inv, size, 3,3, slp),
                DispenserTileEntity.class,
                DropperTileEntity.class,
                CraftingInventory.class);

        register((inv, size, slp) -> lensGrid(inv, size, 2, 2, slp),
                CraftingInventory.class);

        register((inv, size, slp) -> lensGrid(inv, size, 5, 1, slp),
                HopperTileEntity.class);

        register(restricted(LensRegistrar::lensFurnace, s -> s == 3),
                AbstractFurnaceTileEntity.class,
                SmokerTileEntity.class,
                FurnaceTileEntity.class,
                BlastFurnaceTileEntity.class
        );

        register(restricted(LensRegistrar::lensBrewingStandTileEntity, s -> s == 5), BrewingStandTileEntity.class);
        register(restricted(LensRegistrar::lensDoubleSided, s -> s == 2 * 9 * 3), DoubleSidedInventory.class);

        register(restricted(LensRegistrar::lensRepairContainer, s -> s == 2 + 1 + 9 * 3), RepairContainer.class);
        register(restricted(LensRegistrar::lensWorkbenchContainer,s -> s == 1 + 3 * 3 + 9 * 3), WorkbenchContainer.class);

        register(restricted(LensRegistrar::lensPlayerContainer, s -> s == 1+ 4 + 4 + 9*3 + 1), PlayerContainer.class);

        register(restricted(LensRegistrar::generateLens, s -> s == 8),
                AbstractVillagerEntity.class,
                VillagerEntity.class,
                WanderingTraderEntity.class);

        register(restricted(LensRegistrar::lensSlot, s -> s == 1), CraftResultInventory.class);
    }

    private static Lens lensSlot(Object inventory, int size, SlotLensProvider slotLensProvider) {
        return slotLensProvider.getSlotLens(0);
    }

    public static void register(LensFactory lensFactory, Class... classes) {
        for (Class clazz : classes) {
            lensFactories.put(clazz, lensFactory);
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
        return getLenses(inventory.getClass()).computeIfAbsent(size, k -> generateLens(inventory, size, slotLensProvider));
    }

    private static Int2ObjectMap<Lens> getLenses(Class inventory) {
        return lenses.computeIfAbsent(inventory, k -> new Int2ObjectOpenHashMap<>());
    }

    private interface LensFactory {
        @Nullable Lens apply(Object inventory, int size, SlotLensProvider slotLensProvider);
    }

    private static Lens generateLens(Object inventory, int size, SlotLensProvider slotLensProvider) {
        LensFactory lensFactory = lensFactories.get(inventory.getClass());
        if (size == 0) {
            return new DefaultEmptyLens(((InventoryBridge) inventory).bridge$getAdapter()); // TODO why do we need an adapter in an empty lens?
        }
        Lens lens = lensFactory.apply(inventory.getClass(), size, slotLensProvider);
        if (lens != null) {
            return lens;
        }
        if (inventory instanceof CraftingInventory) {
            lens = lensGrid(inventory, size, ((CraftingInventory) inventory).getWidth(), ((CraftingInventory) inventory).getHeight(), slotLensProvider);
        }
        else if (inventory instanceof Container) {
            lens = ContainerUtil.generateLens(((Container) inventory), slotLensProvider);
        } else if (size == 1) {
            return slotLensProvider.getSlotLens(0);
        }
        if (lens != null) {
            return lens;
        }
        return new DefaultIndexedLens(0, size, slotLensProvider);
    }

    @Nullable
    private static Lens lensGrid(Object inventory, int size, int width, int height, SlotLensProvider slotLensProvider) {
        if (size != width * height) {
            return null; // Wrong size
        }
        return new GridInventoryLens(0, width, height, inventory.getClass(), slotLensProvider);
    }

    private static Lens lensBrewingStandTileEntity(Object inventory, int size, SlotLensProvider slotLensProvider) {
        return new BrewingStandInventoryLens(size, inventory.getClass(), slotLensProvider);
    }

    private static Lens lensFurnace(Object inventory, int size, SlotLensProvider slotLensProvider) {
        return new FurnaceInventoryLens(size, inventory.getClass(), slotLensProvider);
    }


    private static Lens lensDoubleSided(Object inventory, int size, SlotLensProvider slotLensProvider) {
        return new LargeChestInventoryLens(size, inventory.getClass(), slotLensProvider);
    }

    private static Lens lensRepairContainer(Object inventory, int size, SlotLensProvider slotLensProvider) {
        final List<Lens> lenses = new ArrayList<>();
        lenses.add(new DefaultIndexedLens(0, 3, slotLensProvider));
        lenses.add(new PrimaryPlayerInventoryLens(3, slotLensProvider, true));
        return new ContainerLens(size, inventory.getClass(), slotLensProvider, lenses);
    }

    private static Lens lensWorkbenchContainer(Object inventory, int size, SlotLensProvider slotLensProvider) {
        final List<Lens> lenses = new ArrayList<>();
        lenses.add(new CraftingInventoryLens(0, 1, 3, 3, slotLensProvider));
        lenses.add(new PrimaryPlayerInventoryLens(3 * 3 + 1, slotLensProvider, true));
        return new ContainerLens(size, inventory.getClass(), slotLensProvider, lenses);
    }


    private static Lens lensPlayerContainer(Object inventory, int size, SlotLensProvider slotLensProvider) {
        return new ContainerPlayerInventoryLens(size, inventory.getClass(), slotLensProvider);
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
            return basicSlotLenses.computeIfAbsent(index, BasicSlotLens::new);
        }

        @Override
        public String toString() {
            return "SlotLensProvider[base=" + this.base + "][size=" + this.size + "]";
        }
    }


}
























