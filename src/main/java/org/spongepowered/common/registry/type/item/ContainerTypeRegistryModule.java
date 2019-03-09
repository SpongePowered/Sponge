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

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.item.inventory.custom.ContainerType;
import org.spongepowered.api.item.inventory.custom.ContainerTypes;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.type.SpongeContainerType;
import org.spongepowered.common.item.inventory.lens.LensCreator;
import org.spongepowered.common.item.inventory.lens.impl.DefaultIndexedLens;
import org.spongepowered.common.item.inventory.lens.impl.comp.CraftingInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.BrewingStandInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.FurnaceInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.slots.InputSlotLensImpl;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;

import javax.annotation.Nullable;

@RegisterCatalog(ContainerTypes.class)
public class ContainerTypeRegistryModule extends AbstractCatalogRegistryModule<ContainerType> implements AdditionalCatalogRegistryModule<ContainerType> {

    public static ContainerTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void registerDefaults() {
        this.register(CatalogKey.minecraft("chest"), 9, 3);
        this.register(CatalogKey.minecraft("double_chest"), 9, 6);
        this.register(CatalogKey.minecraft("furnace"), 3, FurnaceInventoryLens::new);
        this.register(CatalogKey.minecraft("dispenser"), 3, 3);
        this.register(CatalogKey.minecraft("crafting_table"), 10, sp -> new CraftingInventoryLensImpl(0, 1, 3, 3, sp));
        this.register(CatalogKey.minecraft("brewing_stand"), 5, BrewingStandInventoryLens::new);
        this.register(CatalogKey.minecraft("hopper"), 5, 1);
        this.register(CatalogKey.minecraft("beacon"), 1, sp -> new InputSlotLensImpl(0));
        this.register(CatalogKey.minecraft("enchanting_table"), 2);
        this.register(CatalogKey.minecraft("anvil"), 3);
        this.register(CatalogKey.minecraft("villager"), 3);
        this.register(CatalogKey.minecraft("horse"), 2, "EntityHorse"); // TODO different sizes
        this.register(CatalogKey.minecraft("shulker_box"), 9, 3);
    }

    private void register(final CatalogKey key, int size, int width, int height, LensCreator lensCreator, @Nullable String internalId) {
        this.map.put(key, new SpongeContainerType(key, size, width, height, lensCreator, internalId));
    }

    private void register(final CatalogKey key, int width, int height) {
        this.register(key, width * height, width, height, sp -> new GridInventoryLensImpl(width, height, sp), null);
    }

    private void register(final CatalogKey key, int size, LensCreator lensCreator) {
        this.register(key, size, 0, 0, lensCreator, null);
    }

    private void register(final CatalogKey key, int size) {
        this.register(key, size, 0, 0, sp -> new DefaultIndexedLens(0, size, sp), null);
    }

    private void register(final CatalogKey key, int size, final String internalId) {
        this.register(key, size, 0, 0, sp -> new DefaultIndexedLens(0, size, sp), internalId);
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
}
