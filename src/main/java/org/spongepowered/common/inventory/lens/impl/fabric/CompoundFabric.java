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
package org.spongepowered.common.inventory.lens.impl.fabric;

import net.minecraft.item.ItemStack;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.bridge.inventory.InventoryBridge;
import org.spongepowered.common.inventory.fabric.Fabric;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unchecked")
public class CompoundFabric implements Fabric {

    private final Fabric fabric1;
    private final Fabric fabric2;

    public CompoundFabric(Fabric fabric1, Fabric fabric2) {
        this.fabric1 = fabric1;
        this.fabric2 = fabric2;
    }

    @Override
    public Collection<InventoryBridge> fabric$allInventories() {
        Set<InventoryBridge> inv = new HashSet<>();
        inv.addAll(this.fabric1.fabric$allInventories());
        inv.addAll(this.fabric2.fabric$allInventories());
        return inv;
    }

    @Override
    public InventoryBridge fabric$get(int index) {

        if (index < this.fabric1.fabric$getSize()) {
            return this.fabric1.fabric$get(index);
        }
        return this.fabric2.fabric$get(index - this.fabric1.fabric$getSize());
    }

    @Override
    public ItemStack fabric$getStack(int index) {
        if (index < this.fabric1.fabric$getSize()) {
            return this.fabric1.fabric$getStack(index);
        }
        return this.fabric2.fabric$getStack(index - this.fabric1.fabric$getSize());
    }

    @Override
    public void fabric$setStack(int index, ItemStack stack) {
        if (index < this.fabric1.fabric$getSize()) {
            this.fabric1.fabric$setStack(index, stack);
        } else {
            this.fabric2.fabric$setStack(index - this.fabric1.fabric$getSize(), stack);
        }
    }

    @Override
    public int fabric$getMaxStackSize() {
        return Math.max(this.fabric1.fabric$getMaxStackSize(), this.fabric2.fabric$getMaxStackSize());
    }

    @Override
    public int fabric$getSize() {
        return this.fabric1.fabric$getSize() + this.fabric2.fabric$getSize();
    }

    @Override
    public void fabric$clear() {
        this.fabric1.fabric$clear();
        this.fabric2.fabric$clear();
    }

    @Override
    public void fabric$markDirty() {
        this.fabric1.fabric$markDirty();
        this.fabric2.fabric$markDirty();
    }

}
