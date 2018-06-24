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
package org.spongepowered.common.item.inventory.lens.impl;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("rawtypes")
public class ReusableLens<T extends Lens<IInventory, ItemStack>> {

    // InventoryAdapterClass -> LensClass -> Size -> ReusableLens
    private static Map<Class<? extends InventoryAdapter>, Map<Class<? extends Lens<IInventory, ItemStack>>, Int2ObjectMap<ReusableLens>>>
            reusableLenses = new HashMap<>();

    private final SlotProvider<IInventory, ItemStack> slots;
    private final T lens;

    public ReusableLens(SlotProvider<IInventory, ItemStack> slots, T lens) {
        this.slots = slots;
        this.lens = lens;
    }

    public ReusableLens(SlotProvider<IInventory, ItemStack> slots, Function<SlotProvider<IInventory, ItemStack>, T> lensProvider) {
        this.slots = slots;
        this.lens = lensProvider.apply(slots);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Lens<IInventory, ItemStack>> ReusableLens<T> getLens(Class<T> lensType, InventoryAdapter<IInventory, ItemStack> adapter,
            Supplier<SlotProvider<IInventory, ItemStack>> slots, Function<SlotProvider<IInventory, ItemStack>, T> lens) {
        Map<Class<? extends Lens<IInventory, ItemStack>>, Int2ObjectMap<ReusableLens>>
                adapterLenses = reusableLenses.computeIfAbsent(adapter.getClass(), k -> new HashMap<>());
        Int2ObjectMap<ReusableLens> lenses = adapterLenses.computeIfAbsent(lensType, k -> new Int2ObjectOpenHashMap<>());
        return lenses.computeIfAbsent(adapter.getFabric().getSize(), k -> {
            SlotProvider<IInventory, ItemStack> sl = slots.get();
            return new ReusableLens(sl, lens);
        });
    }

    public SlotProvider<IInventory, ItemStack> getSlots() {
        return this.slots;
    }

    public T getLens() {
        return this.lens;
    }
}
