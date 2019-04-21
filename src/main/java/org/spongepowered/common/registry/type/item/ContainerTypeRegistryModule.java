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
package org.spongepowered.common.registry.type.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerDispenser;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.ContainerShulkerBox;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IInventory;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.item.inventory.custom.ContainerType;
import org.spongepowered.api.item.inventory.custom.ContainerTypes;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.type.SpongeContainerType;
import org.spongepowered.common.data.type.SpongeContainerTypeEmpty;
import org.spongepowered.common.data.type.SpongeContainerTypeEntity;
import org.spongepowered.common.item.inventory.lens.LensCreator;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.BrewingStandInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.FurnaceInventoryLens;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;

@RegisterCatalog(ContainerTypes.class)
public class ContainerTypeRegistryModule extends AbstractCatalogRegistryModule<ContainerType>
        implements AdditionalCatalogRegistryModule<ContainerType> {

    public static ContainerTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void registerDefaults() {
        this.register(CatalogKey.minecraft("chest"),        (i, p) -> new ContainerChest(p.inventory, i, p), 9, 3);
        this.register(CatalogKey.minecraft("double_chest"), (i, p) -> new ContainerChest(p.inventory, i, p), 9, 6);
        this.register(CatalogKey.minecraft("furnace"),      (i, p) -> new ContainerFurnace(p.inventory, i), FurnaceInventoryLens::new, 3);
        this.register(CatalogKey.minecraft("dispenser"),    (i, p) -> new ContainerDispenser(p.inventory, i), 3, 3);
        this.register(CatalogKey.minecraft("brewing_stand"),(i, p) -> new ContainerBrewingStand(p.inventory, i), BrewingStandInventoryLens::new, 5);
        this.register(CatalogKey.minecraft("hopper"),       (i, p) -> new ContainerHopper(p.inventory, i, p), 5, 1);
        this.register(CatalogKey.minecraft("shulker_box"),  (i, p) -> new ContainerShulkerBox(p.inventory, i, p), 9, 3);

        this.registerEmpty(CatalogKey.minecraft("crafting_table"),  (i, p) -> new ContainerWorkbench(p.inventory, p.getEntityWorld(), p.getPosition()));
        this.registerEmpty(CatalogKey.minecraft("enchanting_table"),(i, p) -> new ContainerEnchantment(p.inventory, p.getEntityWorld(), p.getPosition()));
        this.registerEmpty(CatalogKey.minecraft("anvil"),           (i, p) -> new ContainerRepair(p.inventory, p.getEntityWorld(), p.getPosition(), p));
        this.registerEmpty(CatalogKey.minecraft("beacon"),          (i, p) -> new ContainerBeacon(p.inventory, i));

        this.registerEntity(CatalogKey.minecraft("villager"));
        this.registerEntity(CatalogKey.minecraft("horse")); // "EntityHorse"
    }

    private void registerEntity(CatalogKey key) {
        this.map.put(key, new SpongeContainerTypeEntity(key));
    }

    private void registerEmpty(final CatalogKey key, ContainerProvider provider) {
        this.map.put(key, new SpongeContainerTypeEmpty(key, provider));
    }

    private void register(final CatalogKey key, ContainerProvider provider, LensCreator lensCreator, int size, int width, int height) {
        this.map.put(key, new SpongeContainerType(key, size, width, height, lensCreator, provider));
    }

    private void register(final CatalogKey key, ContainerProvider provider, int width, int height) {
        this.register(key, provider, sp -> new GridInventoryLensImpl(width, height, sp), width * height, width, height);
    }

    private void register(final CatalogKey key, ContainerProvider provider, LensCreator lensCreator, int size) {
        this.register(key, provider, lensCreator, size, 0, 0);
    }

    @Override
    public void registerAdditionalCatalog(ContainerType guiId) {
        if (this.map.containsKey(guiId.getKey())) {
            throw new IllegalArgumentException("GuiId is already registered");
        }
        this.map.put(guiId.getKey(), guiId);
    }

    private ContainerTypeRegistryModule() {
    }

    private static final class Holder {

        static final ContainerTypeRegistryModule INSTANCE = new ContainerTypeRegistryModule();
    }


    /**
     * Provides a {@link Container} for a {@link EntityPlayer} viewing an {@link IInventory}
     */
    public static interface ContainerProvider {
        Container provide(IInventory viewed, EntityPlayer viewing);
    }
}
