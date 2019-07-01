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
package org.spongepowered.common.item.inventory.lens.impl.fabric;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.item.inventory.lens.impl.MinecraftFabric;
import org.spongepowered.common.item.inventory.lens.impl.slots.SlotLensImpl;

import java.util.Collection;
import java.util.Collections;

@SuppressWarnings("unchecked")
public class SlotFabric extends MinecraftFabric {

    private final Slot slot;

    public SlotFabric(final Slot inventory) {
        this.slot = inventory;
    }

    @Override
    public Collection<?> allInventories() {
        return Collections.emptyList();
    }

    @Override
    public IInventory get(final int index) {
        if (this.slot.inventory != null) {
            return this.slot.inventory;
        }

        throw new UnsupportedOperationException("Unable to access slot at " + index + " for delegating fabric of " + this.slot.getClass());
    }

    @Override
    public ItemStack getStack(final int index) {
        return this.slot.getStack();
    }

    @Override
    public void setStack(final int index, final ItemStack stack) {
        this.slot.putStack(stack);
    }

    @Override
    public int getMaxStackSize() {
        return this.slot.getSlotStackLimit();
    }

    @Override
    public Translation getDisplayName() {
        return SlotLensImpl.SLOT_NAME;
    }

    @Override
    public int getSize() {
        return this.slot.getStack().getCount();
    }

    @Override
    public void clear() {
        this.slot.putStack(ItemStack.EMPTY);
    }

    @Override
    public void markDirty() {
        this.slot.onSlotChanged();
    }

    public Slot getDelegate() {
        return this.slot;
    }
}
