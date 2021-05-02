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
package org.spongepowered.common.inventory.lens.impl.slot;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.impl.AbstractLens;
import org.spongepowered.common.inventory.lens.slots.SlotLens;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public abstract class DelegatingSlotLens extends AbstractLens implements SlotLens {

    private final SlotLens delegate;

    public DelegatingSlotLens(SlotLens lens, Class<? extends Inventory> adapter) {
        super(0, 1, adapter);
        this.addSpanningChild(lens);
        this.delegate = lens;
    }

    @Override
    public int getOrdinal(Fabric fabric) {
        return this.delegate.getOrdinal(fabric);
    }

    @Override
    public ItemStack getStack(Fabric fabric) {
        return this.delegate.getStack(fabric);
    }

    @Override
    public boolean setStack(Fabric fabric, ItemStack stack) {
        return this.delegate.setStack(fabric, stack);
    }

    @Override
    public String toString(int deep) {
        return "[Delegate-" + this.delegate + "]";
    }

    @Override
    public List<SlotLens> getSlots(Fabric fabric) {
        return this.delegate.getSlots(fabric);
    }

    @Override
    public @Nullable SlotLens getSlotLens(Fabric fabric, int ordinal) {
        return this.delegate.getSlotLens(fabric, ordinal);
    }
}
