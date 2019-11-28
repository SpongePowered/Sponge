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
package org.spongepowered.common.mixin.core.item.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.item.inventory.InventoryBridge;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.impl.slots.SlotLensImpl;

import java.util.Collection;
import java.util.Collections;

@Mixin(Slot.class)
public abstract class SlotFabricMixin implements Fabric, InventoryBridge {
    @Shadow @Final public IInventory inventory;
    @Shadow public abstract ItemStack getStack();
    @Shadow public abstract void putStack(ItemStack stack);
    @Shadow public abstract int getSlotStackLimit();
    @Shadow public abstract void onSlotChanged();

    @Override
    public Collection<InventoryBridge> fabric$allInventories() {
        return Collections.emptyList();
    }

    @Override
    public InventoryBridge fabric$get(final int index) {
        if (this.inventory != null) {
            return (InventoryBridge) this.inventory;
        }

        throw new UnsupportedOperationException("Unable to access slot at " + index + " for delegating fabric of " + this.getClass());
    }

    @Override
    public ItemStack fabric$getStack(final int index) {
        return this.getStack();
    }

    @Override
    public void fabric$setStack(final int index, final ItemStack stack) {
        this.putStack(stack);
    }

    @Override
    public int fabric$getMaxStackSize() {
        return this.getSlotStackLimit();
    }

    @Override
    public Translation fabric$getDisplayName() {
        return SlotLensImpl.SLOT_NAME;
    }

    @Override
    public int fabric$getSize() {
        return 1;
    }

    @Override
    public void fabric$clear() {
        this.putStack(ItemStack.EMPTY);
    }

    @Override
    public void fabric$markDirty() {
        this.onSlotChanged();
    }
}
