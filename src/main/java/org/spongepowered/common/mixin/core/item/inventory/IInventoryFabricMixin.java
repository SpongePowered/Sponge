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

import com.google.common.collect.ImmutableSet;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.text.translation.FixedTranslation;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.item.inventory.InventoryBridge;
import org.spongepowered.common.item.inventory.lens.Fabric;

import java.util.Collection;

@Mixin(IInventory.class)
public interface IInventoryFabricMixin extends Fabric, IInventory, InventoryBridge {

    @Override
    default Collection<InventoryBridge> fabric$allInventories() {
        return ImmutableSet.of(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    default InventoryBridge fabric$get(int index) {
        return this;
    }

    @Override
    default ItemStack fabric$getStack(int index) {
        return this.getStackInSlot(index);
    }

    @Override
    default void fabric$setStack(int index, ItemStack stack) {
        this.setInventorySlotContents(index, stack);
    }

    @Override
    default int fabric$getMaxStackSize() {
        return this.getInventoryStackLimit();
    }

    @Override
    default Translation fabric$getDisplayName() {
        return new FixedTranslation(this.getDisplayName().getUnformattedText());
    }

    @Override
    default int fabric$getSize() {
        return this.getSizeInventory();
    }

    @Override
    default void fabric$clear() {
        this.clear();
    }

    @Override
    default void fabric$markDirty() {
        this.markDirty();
    }

}
