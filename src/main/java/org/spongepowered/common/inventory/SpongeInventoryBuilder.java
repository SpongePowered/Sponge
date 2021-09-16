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
package org.spongepowered.common.inventory;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.custom.CustomInventory;
import org.spongepowered.common.inventory.lens.CompoundSlotLensProvider;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.CompoundLens;
import org.spongepowered.common.inventory.lens.impl.DefaultIndexedLens;
import org.spongepowered.common.inventory.lens.impl.DelegatingLens;
import org.spongepowered.common.inventory.lens.impl.LensRegistrar;
import org.spongepowered.common.inventory.lens.impl.comp.GridInventoryLens;
import org.spongepowered.plugin.PluginContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class SpongeInventoryBuilder implements Inventory.Builder, Inventory.Builder.BuildingStep, Inventory.Builder.EndStep {

    private final List<Lens> lenses = new ArrayList<>();
    private final List<Inventory> inventories = new ArrayList<>();

    private PluginContainer plugin;
    private int size = 0;

    private Lens finalLens; // always set before build
    private @Nullable UUID identity;
    private @Nullable Carrier carrier;
    private CompoundSlotLensProvider finalProvider;

    public BuildingStep slots(int amount) {
        this.size += amount;
        net.minecraft.world.SimpleContainer adapter = new net.minecraft.world.SimpleContainer(amount);
        this.inventories.add((Inventory) adapter);
        this.lenses.add(new DefaultIndexedLens(0, amount, ((InventoryAdapter) adapter).inventoryAdapter$getSlotLensProvider()));
        return this;
    }

    public BuildingStep grid(int sizeX, int sizeY) {
        this.size += sizeX * sizeY;
        net.minecraft.world.SimpleContainer adapter = new net.minecraft.world.SimpleContainer(sizeX * sizeY);
        this.lenses.add(new GridInventoryLens(0, sizeX, sizeY, ((InventoryAdapter) adapter).inventoryAdapter$getSlotLensProvider()));
        this.inventories.add((Inventory) adapter);
        return this;
    }

    public BuildingStep inventory(Inventory inventory) {
        InventoryAdapter adapter = (InventoryAdapter) inventory;
        this.size += inventory.capacity();
        this.lenses.add(adapter.inventoryAdapter$getRootLens());
        this.inventories.add(inventory);
        return this;
    }

    public EndStep completeStructure() {
        CompoundLens.Builder lensBuilder = CompoundLens.builder();
        int size = 0;
        for (Lens lens : this.lenses) {
            size += lens.slotCount();
        }
        final LensRegistrar.BasicSlotLensProvider lensProvider = new LensRegistrar.BasicSlotLensProvider(size);
        int offset = 0;
        for (Lens lens : this.lenses) {
            lensBuilder.add(new DelegatingLens(offset, lens, lensProvider));
            offset += lens.slotCount();
        }
        this.finalLens = lensBuilder.build(lensProvider);
        return this;
    }

    @Override
    public EndStep plugin(final PluginContainer plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        return this;
    }

    public EndStep identity(UUID uuid) {
        this.identity = uuid;
        return this;
    }

    public EndStep carrier(Carrier carrier) {
        this.carrier = carrier;
        return this;
    }

    public Inventory build() {
        if (this.plugin == null) {
            throw new IllegalStateException("Plugin has not been set in this builder!");
        }
        return ((Inventory) new CustomInventory(this.plugin, this.size, this.finalLens, this.finalProvider,
                this.inventories, this.identity, this.carrier));
    }

    @Override
    public Inventory.Builder reset() {
        this.plugin = null;

        this.lenses.clear();
        this.inventories.clear();
        this.size = 0;

        this.finalLens = null;
        this.finalProvider = null;
        this.identity = null;
        this.carrier = null;

        return this;
    }

}
