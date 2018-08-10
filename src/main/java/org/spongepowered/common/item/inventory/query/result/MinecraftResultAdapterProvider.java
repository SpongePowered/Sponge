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
package org.spongepowered.common.item.inventory.query.result;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.AbstractInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.MutableLensSet;
import org.spongepowered.common.item.inventory.query.Query.ResultAdapterProvider;

public class MinecraftResultAdapterProvider implements ResultAdapterProvider {
    
    public class MinecraftQueryResultAdapter extends AbstractInventoryAdapter implements QueryResult {
        
        public class MinecraftQueryLens extends QueryLens {

            public MinecraftQueryLens(int size, MutableLensSet resultSet) {
                super(size, resultSet);
            }
            
            @Override
            public InventoryAdapter getAdapter(Fabric inv, Inventory parent) {
                return MinecraftQueryResultAdapter.this;
            }
        }

        public MinecraftQueryResultAdapter(Fabric inventory, Inventory parent) {
            super(inventory, null, parent); // null root lens calls initRootLens();
        }
        
        @Override
        protected Lens initRootLens() {
            return new MinecraftQueryLens(MinecraftResultAdapterProvider.this.getResultSize(), MinecraftResultAdapterProvider.this.getResultSet());
        }
    }

    private MutableLensSet resultSet;

    @Override
    public QueryResult getResultAdapter(Fabric inventory, MutableLensSet matches, Inventory parent) {
        this.resultSet = matches;
        return new MinecraftQueryResultAdapter(inventory, parent);
    }
    
    protected MutableLensSet getResultSet() {
        return this.resultSet;
    }

    protected int getResultSize() {
        IntSet slots = new IntOpenHashSet();
        
        for (Lens lens : this.getResultSet()) {
            slots.addAll(lens.getSlots());
        }
        
        return slots.size();
    }

}
