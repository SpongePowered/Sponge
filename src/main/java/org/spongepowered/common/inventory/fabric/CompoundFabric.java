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
package org.spongepowered.common.inventory.fabric;

import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.bridge.inventory.InventoryBridge;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class CompoundFabric implements Fabric {

    private final List<Fabric> fabrics;

    public CompoundFabric(Fabric... fabrics) {
        this.fabrics = Arrays.asList(fabrics);
    }

    public CompoundFabric(List<Inventory> children) {
        this.fabrics = children.stream().map(InventoryBridge.class::cast)
                .map(InventoryBridge::bridge$getAdapter)
                .map(InventoryAdapter::inventoryAdapter$getFabric)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<InventoryBridge> fabric$allInventories() {
        Set<InventoryBridge> inv = new HashSet<>();
        this.fabrics.forEach(fabric -> inv.addAll(fabric.fabric$allInventories()));
        return inv;
    }

    private <T> T getOnFabric(int index, BiFunction<Integer, Fabric, T> biFunction)
    {
        int offset = 0;
        for (Fabric fabric : this.fabrics) {
            if (index - offset < fabric.fabric$getSize()) {
                return biFunction.apply(index - offset, fabric);
            }
            offset += fabric.fabric$getSize();
        }
        return null;
    }

    private void runOnFabric(int index, BiConsumer<Integer, Fabric> biConsumer)
    {
        int offset = 0;
        for (Fabric fabric : this.fabrics) {
            if (index - offset < fabric.fabric$getSize()) {
                biConsumer.accept(index - offset, fabric);
                return;
            }
            offset += fabric.fabric$getSize();
        }
    }

    private <T> T collectOnFabric(BiFunction<Fabric, T, T> biFunction, T initial) {
        T value = initial;
        for (Fabric fabric : this.fabrics) {
            value = biFunction.apply(fabric, value);
        }
        return value;
    }

    @Override
    public InventoryBridge fabric$get(int index) {
        return this.getOnFabric(index, (idx,fabric)-> fabric.fabric$get(idx));
    }

    @Override
    public ItemStack fabric$getStack(int index) {
        return this.getOnFabric(index, (idx,fabric)-> fabric.fabric$getStack(idx));
    }

    @Override
    public void fabric$setStack(int index, ItemStack stack) {
        this.runOnFabric(index, (idx, fabric) -> fabric.fabric$setStack(idx, stack));
    }

    @Override
    public int fabric$getMaxStackSize() {
        return this.collectOnFabric((fabric, value) -> Math.max(value, fabric.fabric$getMaxStackSize()), 0);
    }

    @Override
    public int fabric$getSize() {
        return this.collectOnFabric((fabric, value) -> value + fabric.fabric$getSize(), 0);
    }

    @Override
    public void fabric$clear() {
        this.runOnFabric(0, (i, fabric) -> fabric.fabric$clear());
    }

    @Override
    public void fabric$markDirty() {
        this.runOnFabric(0, (i, fabric) -> fabric.fabric$markDirty());
    }

}
