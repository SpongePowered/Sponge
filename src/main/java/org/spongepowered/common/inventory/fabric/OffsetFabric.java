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

import org.spongepowered.common.bridge.inventory.InventoryBridge;

import java.util.Collection;
import net.minecraft.world.item.ItemStack;

public class OffsetFabric implements Fabric {

    private final Fabric fabric;
    private final int offset;

    private OffsetFabric(Fabric fabric, int offset) {
        this.fabric = fabric;
        this.offset = offset;
    }

    public static Fabric of(Fabric fabric, int by) {
        if (by == 0) {
            return fabric;
        }
        if (fabric instanceof OffsetFabric) {
            by = ((OffsetFabric) fabric).offset + by;
            fabric = ((OffsetFabric) fabric).fabric;
        }
        return new OffsetFabric(fabric, by);
    }

    @Override
    public Collection<InventoryBridge> fabric$allInventories() {
        return this.fabric.fabric$allInventories();
    }

    @Override
    public InventoryBridge fabric$get(int index) {
        return this.fabric.fabric$get(index + this.offset);
    }

    @Override
    public ItemStack fabric$getStack(int index) {
        return this.fabric.fabric$getStack(index + this.offset);
    }

    @Override
    public void fabric$setStack(int index, ItemStack stack) {
        this.fabric.fabric$setStack(index + this.offset, stack);
    }

    @Override
    public int fabric$getMaxStackSize() {
        return this.fabric.fabric$getMaxStackSize();
    }

    @Override public int fabric$getSize() {
        return this.fabric.fabric$getSize();
    }

    @Override
    public void fabric$clear() {
        this.fabric.fabric$clear();
    }

    @Override
    public void fabric$markDirty() {
        this.fabric.fabric$markDirty();
    }

    @Override
    public String toString() {
        return this.fabric.toString() + " offset: " + this.offset;
    }
}
