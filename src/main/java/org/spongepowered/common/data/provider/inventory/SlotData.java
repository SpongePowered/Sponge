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
package org.spongepowered.common.data.provider.inventory;

import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.common.bridge.inventory.InventoryBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.inventory.lens.Lens;

import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public final class SlotData {

    private SlotData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asImmutable(Slot.class)
                    .create(Keys.EQUIPMENT_TYPE)
                        .get(h -> SlotData.getter(h, Keys.EQUIPMENT_TYPE))
                        .set((h, v) -> null)
                    .create(Keys.SLOT_INDEX)
                        .get(h -> SlotData.getter(h, Keys.SLOT_INDEX))
                        .set((h, v) -> null)
                    .create(Keys.SLOT_POSITION)
                        .get(h -> SlotData.getter(h, Keys.SLOT_POSITION))
                        .set((h, v) -> null)
                    .create(Keys.SLOT_SIDE)
                        .get(h -> SlotData.getter(h, Keys.SLOT_SIDE))
                        .set((h, v) -> null);
    }
    // @formatter:on

    private static <D> D getter(final Slot holder, final Supplier<Key<Value<D>>> suppliedKey) {
        final Lens parentLens = ((InventoryBridge) holder.parent()).bridge$getAdapter().inventoryAdapter$getRootLens();
        final Lens childLens = ((InventoryBridge) holder).bridge$getAdapter().inventoryAdapter$getRootLens();
        final Map<Key<?>, Object> dataMap = parentLens.getDataFor(childLens);
        return (D) dataMap.get(suppliedKey.get());
    }
}
