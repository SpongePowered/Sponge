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

import com.google.common.base.Preconditions;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.impl.MinecraftFabric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompoundFabric extends MinecraftFabric {

    private Translation displayName;
    private final List<MinecraftFabric> all;

    private CompoundFabric(List<MinecraftFabric> list) {
        Preconditions.checkArgument(!list.isEmpty());
        this.displayName = list.get(0).getDisplayName();
        this.all = new ArrayList<>(list);
    }

    @Override
    public Collection<IInventory> allInventories() {
        Set<IInventory> inv = new HashSet<>();
        for (MinecraftFabric fabric : this.all) {
            inv.addAll(fabric.allInventories());
        }
        return inv;
    }

    @Override
    public IInventory get(int index) {
        int offset = 0;
        for (MinecraftFabric fabric : this.all) {
            if (index < offset + fabric.getSize()) {
                return fabric.get(index - offset);
            } else {
                offset += fabric.getSize();
            }
        }

        return null; // TODO?
    }

    @Override
    public ItemStack getStack(int index) {
        int offset = 0;
        for (MinecraftFabric fabric : this.all) {
            if (index < offset + fabric.getSize()) {
                return fabric.getStack(index - offset);
            } else {
                offset += fabric.getSize();
            }
        }

        return null;
    }

    @Override
    public void setStack(int index, ItemStack stack) {

        int offset = 0;
        for (MinecraftFabric fabric : this.all) {
            if (index < offset + fabric.getSize()) {
                fabric.setStack(index - offset, stack);
                return;
            } else {
                offset += fabric.getSize();
            }
        }
    }

    @Override
    public int getMaxStackSize() {
        int max = 0;
        for (Fabric inv : this.all) {
            max = Math.max(max, inv.getMaxStackSize());
        }
        return max;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    @Override
    public int getSize() {
        return this.all.stream().mapToInt(Fabric::getSize).sum();
    }

    @Override
    public void clear() {
        this.all.forEach(Fabric::clear);
    }

    @Override
    public void markDirty() {
        this.all.forEach(Fabric::markDirty);
    }

    public static CompoundFabric.Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final List<Fabric> fabrics = new ArrayList<>();

        public Builder add(Fabric fabric) {
            this.fabrics.add(fabric);
            return this;
        }

        public CompoundFabric build() {
            return new CompoundFabric(((List) this.fabrics));
        }
    }

}
