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
import org.spongepowered.common.bridge.inventory.LensProviderBridge;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.ReusableLensInventoryAdapaterBridge;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.ReusableLensProvider;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.OrderedInventoryLensImpl;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("rawtypes")
public class ReusableLens<T extends Lens> {

    // InventoryAdapterClass -> LensClass -> Size -> ReusableLens
    private static Map<Class<? extends InventoryAdapter>, Map<Class<? extends Lens>, Int2ObjectMap<ReusableLens>>>
            reusableLenses = new IdentityHashMap<>();

    private final SlotProvider slots;
    private final T lens;

    private ReusableLens(SlotProvider slots, T lens) {
        this.slots = slots;
        this.lens = lens;
    }

    private ReusableLens(SlotProvider slots, Function<SlotProvider, T> lensProvider) {
        this.slots = slots;
        this.lens = lensProvider.apply(slots);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Lens> ReusableLens<T> getLens(Class<T> lensType, InventoryAdapter adapter,
            Supplier<SlotProvider> slots, Function<SlotProvider, T> lens) {
        Map<Class<? extends Lens>, Int2ObjectMap<ReusableLens>>
                adapterLenses = reusableLenses.computeIfAbsent(adapter.getClass(), k -> new IdentityHashMap<>());
        Int2ObjectMap<ReusableLens> lenses = adapterLenses.computeIfAbsent(lensType, k -> new Int2ObjectOpenHashMap<>());
        return lenses.computeIfAbsent(adapter.bridge$getFabric().fabric$getSize(), k -> {
            SlotProvider sl = slots.get();
            return new ReusableLens(sl, lens);
        });
    }

    public static ReusableLens getLens(ReusableLensInventoryAdapaterBridge adapter)
    {
        if (adapter instanceof ReusableLensProvider) {
            // TODO is this interface needed? Instead use LensProviderBridge?
            return ((ReusableLensProvider) adapter).bridge$generateReusableLens(adapter.bridge$getFabric(), adapter);
        }
        if (adapter instanceof LensProviderBridge) {
            // TODO this is not actually creating lenses that will be reused
            SlotProvider slotProvider = ((LensProviderBridge) adapter).bridge$slotProvider(adapter.bridge$getFabric(), adapter);
            adapter.bridge$setSlotProvider(slotProvider);
            final Lens lens = ((LensProviderBridge) adapter).bridge$rootLens(adapter.bridge$getFabric(), adapter);
            adapter.bridge$setLens(lens);
            return new ReusableLens<>(slotProvider, lens);
        }

        return getLens(Lens.class, adapter,
                () -> defaultSlots(adapter),
                (slots) -> defaultLens(adapter, slots));
    }

    private static SlotProvider defaultSlots(InventoryAdapter adapter) {
        return new SlotCollection.Builder().add(adapter.bridge$getFabric().fabric$getSize()).build();
    }

    private static Lens defaultLens(InventoryAdapter adapter, SlotProvider slots) {
        int slotCount = adapter.bridge$getFabric().fabric$getSize();
        if (slotCount == 0) {
            return new DefaultEmptyLens(adapter);
        } else {
            return new OrderedInventoryLensImpl(0, slotCount, slots);
        }
    }

    public static ReusableLens<Lens> defaultReusableLens(InventoryAdapter adapter) {
        return getLens(Lens.class, adapter,
                () -> defaultSlots(adapter),
                (slots) -> defaultLens(adapter, slots));
    }

    public SlotProvider getSlots() {
        return this.slots;
    }

    public T getLens() {
        return this.lens;
    }
}
