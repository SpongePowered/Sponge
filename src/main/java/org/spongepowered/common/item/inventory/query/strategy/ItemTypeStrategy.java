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
package org.spongepowered.common.item.inventory.query.strategy;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;
import org.spongepowered.common.item.inventory.query.QueryStrategy;

import java.util.Set;

public class ItemTypeStrategy<TInventory> extends QueryStrategy<TInventory, ItemStack, ItemType> {
    
    private Set<ItemType> types;

    @Override
    public QueryStrategy<TInventory, ItemStack, ItemType> with(ImmutableSet<ItemType> types) {
        this.types = types;
        return this;
    }
    
    @Override
    public boolean matches(Lens<TInventory, ItemStack> lens, Lens<TInventory, ItemStack> parent, Fabric<TInventory> inventory) {
        if (this.types.isEmpty()) {
            return true;
        }
        
        if (lens instanceof SlotLens) {
            ItemStack stack = ((SlotLens<TInventory, ItemStack>)lens).getStack(inventory);
            if (stack == null) {
                return false;
            }
            for (ItemType type : this.types) {
                if (stack.getType().equals(type)) {
                    return true;
                }
            }
        }
        
        return false;
    }

}
