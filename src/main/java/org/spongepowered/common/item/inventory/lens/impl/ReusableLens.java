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
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("rawtypes")
public class ReusableLens<T extends Lens> {

    // InventoryAdapterClass -> LensClass -> Size -> ReusableLens
    private static Map<Class<? extends InventoryAdapter>, Map<Class<? extends Lens>, Int2ObjectMap<ReusableLens>>>
            reusableLenses = new HashMap<>();

    private final SlotProvider slots;
    private final T lens;

    public ReusableLens(SlotProvider slots, T lens) {
        this.slots = slots;
        this.lens = lens;
    }

    public ReusableLens(SlotProvider slots, Function<SlotProvider, T> lensProvider) {
        this.slots = slots;
        this.lens = lensProvider.apply(slots);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Lens> ReusableLens<T> getLens(Class<T> lensType, InventoryAdapter adapter,
            Supplier<SlotProvider> slots, Function<SlotProvider, T> lens) {
        Map<Class<? extends Lens>, Int2ObjectMap<ReusableLens>>
                adapterLenses = reusableLenses.computeIfAbsent(adapter.getClass(), k -> new HashMap<>());
        Int2ObjectMap<ReusableLens> lenses = adapterLenses.computeIfAbsent(lensType, k -> new Int2ObjectOpenHashMap<>());
        return lenses.computeIfAbsent(adapter.bridge$getFabric().getSize(), k -> {
            SlotProvider sl = slots.get();
            return new ReusableLens(sl, lens);
        });
    }

    public SlotProvider getSlots() {
        return this.slots;
    }

    public T getLens() {
        return this.lens;
    }
}
