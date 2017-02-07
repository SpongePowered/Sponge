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
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.query.QueryStrategy;

import java.util.Set;

public class ClassStrategy<TInventory, TStack> extends QueryStrategy<TInventory, TStack, Class<?>> {
    
    private Set<Class<?>> classes;

    @Override
    public QueryStrategy<TInventory, TStack, Class<?>> with(Class<?>[] args) {
        this.classes = ImmutableSet.<Class<?>>copyOf(args);
        return this;
    }
    
    @Override
    public boolean matches(Lens<TInventory, TStack> lens, Lens<TInventory, TStack> parent, Fabric<TInventory> inventory) {
        if (this.classes.isEmpty()) {
            return true;
        }
        for (Class<?> candidate : this.classes) {
            // Check for null first because there are inventories without lens (e.g. InventoryCrafting in EntitySheep)
            if (candidate != null && candidate.isAssignableFrom(lens.getAdapterType())) {
                return true;
            }
        }
        
        return false;
    }

}
