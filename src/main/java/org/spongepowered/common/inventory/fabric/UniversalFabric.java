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
package org.spongepowered.common.inventory.fabric;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.common.bridge.world.inventory.InventoryBridge;

import java.util.Collection;

/**
 * A {@link Fabric} implementation that uses registered {@link InventoryTranslator}s.
 * This can be used for inventory interfaces like {@link Container}
 */
@SuppressWarnings("unchecked")
public interface UniversalFabric extends Fabric, InventoryBridge {

    @SuppressWarnings("rawtypes")
    default InventoryTranslator fabric$translator() {
        return InventoryTranslators.getTranslator(this.getClass());
    }

    @Override
    default Collection<InventoryBridge> fabric$allInventories() {
        return this.fabric$translator().allInventories(this);
    }

    @Override
    default InventoryBridge fabric$get(int index) {
        return this.fabric$translator().get(this, index);
    }

    @Override
    default ItemStack fabric$getStack(int index) {
        return this.fabric$translator().getStack(this, index);
    }

    @Override
    default void fabric$setStack(int index, ItemStack stack) {
        this.fabric$translator().setStack(this, index, stack);
    }

    @Override default int fabric$getMaxStackSize() {
        return this.fabric$translator().getMaxStackSize(this);
    }

    @Override default int fabric$getSize() {
        return this.fabric$translator().getSize(this);
    }

    @Override default void fabric$clear() {
        this.fabric$translator().clear(this);
        this.fabric$captureContainer();
    }

    @Override default void fabric$markDirty() {
        this.fabric$translator().markDirty(this);
        this.fabric$captureContainer();
    }
}
