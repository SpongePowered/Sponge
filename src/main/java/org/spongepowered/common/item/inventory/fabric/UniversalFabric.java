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
package org.spongepowered.common.item.inventory.fabric;

import net.minecraft.item.ItemStack;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.bridge.item.inventory.InventoryBridge;
import org.spongepowered.common.item.inventory.lens.Fabric;

import java.util.Collection;

@SuppressWarnings("unchecked")
public interface UniversalFabric extends Fabric, InventoryBridge {

    @Override
    default Collection<InventoryBridge> fabric$allInventories() {
        return InventoryTranslators.getTranslator(this.getClass()).allInventories(this);
    }

    @Override
    default InventoryBridge fabric$get(int index) {
        return InventoryTranslators.getTranslator(this.getClass()).get(this, index);
    }

    @Override
    default ItemStack fabric$getStack(int index) {
        return InventoryTranslators.getTranslator(this.getClass()).getStack(this, index);
    }

    @Override
    default void fabric$setStack(int index, ItemStack stack) {
        InventoryTranslators.getTranslator(this.getClass()).setStack(this, index, stack);
    }

    @Override default int fabric$getMaxStackSize() {
        return InventoryTranslators.getTranslator(this.getClass()).getMaxStackSize(this);
    }

    @Override default Translation fabric$getDisplayName() {
        return InventoryTranslators.getTranslator(this.getClass()).getDisplayName(this);
    }

    @Override default int fabric$getSize() {
        return InventoryTranslators.getTranslator(this.getClass()).getSize(this);
    }

    @Override default void fabric$clear() {
        InventoryTranslators.getTranslator(this.getClass()).clear(this);
    }

    @Override default void fabric$markDirty() {
        InventoryTranslators.getTranslator(this.getClass()).markDirty(this);
    }
}
