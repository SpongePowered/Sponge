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
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.query.QueryStrategy;

import java.util.Set;

public class NameStrategy<TInventory, TStack> extends QueryStrategy<TInventory, TStack, Translation> {
    
    private Set<Translation> names;

    @Override
    public QueryStrategy<TInventory, TStack, Translation> with(ImmutableSet<Translation> args) {
        this.names = ImmutableSet.<Translation>copyOf(args);
        return this;
    }
    
    @Override
    public boolean matches(Lens<TInventory, TStack> lens, Lens<TInventory, TStack> parent, Fabric<TInventory> inventory) {
        if (this.names.isEmpty()) {
            return true;
        }
        for (Translation candidate : this.names) {
            if (candidate.toString().equals(lens.getName(inventory).toString())) {
                return true;
            }
        }
        
        return false;
    }

}
