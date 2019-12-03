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
package org.spongepowered.common.inventory.query.type;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.query.Query;
import org.spongepowered.common.inventory.EmptyInventoryImpl;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.adapter.impl.BasicInventoryAdapter;
import org.spongepowered.common.inventory.query.SpongeQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AppendQuery extends SpongeQuery {

    private final List<Query> queryList;

    public AppendQuery(List<Query> queryList) {
        this.queryList = Collections.unmodifiableList(queryList);
    }

    public static Query of(Query query, Query[] queries) {
        List<Query> newQueries = new ArrayList<>();
        if (query instanceof AppendQuery) {
            newQueries.addAll(((AppendQuery) query).queryList);
        } else {
            newQueries.add(query);
        }
        newQueries.addAll(Arrays.asList(queries));
        return new AppendQuery(newQueries);
    }

    @Override
    public Inventory execute(Inventory inventory, InventoryAdapter adapter) {
        Inventory result = new EmptyInventoryImpl(inventory);
        if (this.queryList.isEmpty()) {
            return result;
        }

        List<Inventory> results = this.queryList.stream().map(q -> q.execute(inventory)).collect(Collectors.toList());
        return new BasicInventoryAdapter(adapter.bridge$getFabric(), results, inventory);
    }


}