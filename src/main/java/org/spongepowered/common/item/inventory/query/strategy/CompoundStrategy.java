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
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.query.Query;
import org.spongepowered.common.item.inventory.query.QueryStrategy;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

public final class CompoundStrategy<TInventory, TStack> extends QueryStrategy<TInventory, TStack, Object> {

    private Set<QueryStrategy<TInventory, TStack, ?>> strategies = new HashSet<>();

    @Override
    public QueryStrategy<TInventory, TStack, Object> with(ImmutableSet<Object> objects) {
        Multimap<Query.Type, Object> args = Multimaps.newMultimap(new EnumMap<>(Query.Type.class), HashSet::new);
        for (Object arg : objects) {
            args.put(Query.getType(arg), arg);
        }
        args.asMap().forEach(this::addStrategy);
        if (this.strategies.size() == 1) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            QueryStrategy<TInventory, TStack, Object> value = (QueryStrategy) this.strategies.iterator().next();
            return value;
        }
        return this;
    }

    private <E> void addStrategy(Query.Type type, Collection<E> args) {
        QueryStrategy<TInventory, TStack, E> strategy = Query.getStrategy(type);
        this.strategies.add(strategy.with(ImmutableSet.copyOf(args)));
    }

    @Override
    public boolean matches(Lens<TInventory, TStack> lens, Lens<TInventory, TStack> parent, Fabric<TInventory> inventory) {
        if (this.strategies.isEmpty()) {
            return true;
        }
        for (QueryStrategy<TInventory, TStack, ?> subStrategy : this.strategies) {
            if (subStrategy.matches(lens, parent, inventory)) {
                return true;
            }
        }
        return false;
    }
}
