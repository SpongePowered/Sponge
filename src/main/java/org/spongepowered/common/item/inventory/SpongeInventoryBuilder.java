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
package org.spongepowered.common.item.inventory;

import net.minecraft.inventory.InventoryBasic;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.custom.CustomInventory;
import org.spongepowered.common.item.inventory.lens.CompoundSlotProvider;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.impl.CompoundLens;
import org.spongepowered.common.item.inventory.lens.impl.DefaultIndexedLens;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

public class SpongeInventoryBuilder implements Inventory.Builder, Inventory.Builder.BuildingStep, Inventory.Builder.EndStep {

    private List<Lens> lenses = new ArrayList<>();
    private List<Inventory> inventories = new ArrayList<>();
    private int size = 0;

    private Lens finalLens; // always set before build
    @Nullable private UUID identity;
    @Nullable private Carrier carrier;
    private CompoundSlotProvider finalProvider;

    @Override
    public BuildingStep slots(int amount) {
        this.size += amount;
        InventoryAdapter adapter = (InventoryAdapter) new InventoryBasic(null, amount);
        this.inventories.add(adapter);
        this.lenses.add(new DefaultIndexedLens(0, amount, adapter.getSlotProvider()));
        return this;
    }

    @Override
    public BuildingStep grid(int sizeX, int sizeY) {
        this.size += sizeX * sizeY;
        InventoryAdapter adapter = (InventoryAdapter) new InventoryBasic(null, sizeX * sizeY);
        this.lenses.add(new GridInventoryLensImpl(0, sizeX, sizeY, adapter.getSlotProvider()));
        this.inventories.add(adapter);
        return this;
    }

    @Override
    public BuildingStep inventory(Inventory inventory) {
        InventoryAdapter adapter = (InventoryAdapter) inventory;
        this.size += inventory.capacity();
        this.lenses.add(adapter.getRootLens());
        this.inventories.add(inventory);
        return this;
    }

    @Override
    public EndStep completeStructure() {
        CompoundLens.Builder lensBuilder = CompoundLens.builder();
        for (Lens lens : this.lenses) {
            lensBuilder.add(lens);
        }
        CompoundSlotProvider provider = new CompoundSlotProvider();
        for (Inventory inventory : this.inventories) {
            provider.add(((InventoryAdapter) inventory));
        }
        this.finalProvider = provider;
        this.finalLens = lensBuilder.build(provider);
        return this;
    }

    @Override
    public EndStep identity(UUID uuid) {
        this.identity = uuid;
        return this;
    }

    @Override
    public EndStep carrier(Carrier carrier) {
        this.carrier = carrier;
        return this;
    }

    @Override
    public Inventory build() {
        return ((Inventory) new CustomInventory(this.size, this.finalLens, this.finalProvider, this.inventories, this.identity, this.carrier));
    }

    @Override
    public Inventory.Builder reset() {

        this.lenses = new ArrayList<>();
        this.inventories = new ArrayList<>();
        this.size = 0;

        this.finalLens = null;
        this.finalProvider = null;
        this.identity = null;
        this.carrier = null;

        return this;
    }

}
