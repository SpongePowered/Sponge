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

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.AbstractInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.comp.OrderedInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;
import org.spongepowered.common.mixin.core.inventory.SlotAccessor;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.inventory.container.Slot;

/**
 * A delegating Lens used in Containers. Provides ordered inventory access.
 */
@SuppressWarnings("rawtypes")
public class DelegatingLens extends AbstractLens {

    private Lens delegate;

    public DelegatingLens(final int base, final Lens lens, final SlotProvider slots) {
        super(base, lens.slotCount(), AbstractInventoryAdapter.class);
        this.delegate = lens;
        this.init(slots);
    }

    public DelegatingLens(final int base, final List<Slot> containerSlots, final Lens lens, final SlotProvider slots) {
        super(base, containerSlots.size(), AbstractInventoryAdapter.class);
        this.delegate = lens;
        final CustomSlotProvider slotProvider = new CustomSlotProvider();
        for (final Slot slot : containerSlots) {
            // Get slots from original slot provider and add them to custom slot provider in order of actual containerSlots.
            slotProvider.add(slots.getSlot(((SlotAccessor) slot).accessor$getSlotNumber()));
        }
        // Provide indexed access over the Container to the slots in the base inventory
        this.addSpanningChild(new OrderedInventoryLensImpl(0, containerSlots.size(), 1, slotProvider));
        this.addChild(this.delegate);
    }

    protected void init(final SlotProvider slots) {
        this.addSpanningChild(new OrderedInventoryLensImpl(this.base, this.size, 1, slots));
        this.addChild(this.delegate);
    }

    @SuppressWarnings("unchecked")
    @Override
    public InventoryAdapter getAdapter(final Fabric inv, final Inventory parent) {
        return new AbstractInventoryAdapter(inv, this, parent);
    }

    public static class CustomSlotProvider implements SlotProvider {

        private List<SlotLens> lenses = new ArrayList<>();

        public void add(final SlotLens toAdd) {
            this.lenses.add(toAdd);
        }

        @Override
        public SlotLens getSlot(final int index) {
            return this.lenses.get(index);
        }
    }
}
