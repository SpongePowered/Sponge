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
package org.spongepowered.common.item.inventory.lens.impl.collections;

import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.MutableLensCollection;
import org.spongepowered.common.item.inventory.lens.impl.struct.LensHandle;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MutableLensCollectionImpl extends AbstractList<Lens> implements MutableLensCollection {

    protected final List<LensHandle> lenses;
    private Map<Lens, LensHandle> handleMap = new HashMap<>();
    
    private final boolean allowRemove;
    
    private boolean resizeSemaphore = false;

    public MutableLensCollectionImpl(int initialCapacity, boolean allowRemove) {
        this.lenses = new ArrayList<LensHandle>(initialCapacity);
        this.allowRemove = allowRemove;
        this.resize(initialCapacity);
    }
    
    @Override
    public void add(Lens lens, InventoryProperty<?, ?>... properties) {
        LensHandle handle = handleMap.get(lens);
        if (handle == null) {
            handle = new LensHandle(lens, properties);
            this.lenses.add(handle);
            handleMap.put(lens, handle);
        } else {
            for (InventoryProperty<?, ?> property : properties) {
                handle.setProperty(property);
            }
        }
    }
    
    @Override
    public void add(int index, Lens lens, InventoryProperty<?, ?>... properties) {
        if (index >= this.size()) {
            this.resize(index - 1);
        }
        this.lenses.add(index, new LensHandle(lens, properties));
    }
    
    @Override
    public void add(int index, Lens lens) {
        this.add(index, lens, (InventoryProperty<?, ?>) null);
    }
    
    private void resize(int size) {
        assert !this.resizeSemaphore;
        this.resizeSemaphore = true;
        for (int index = this.lenses.size(); index < size; index++) {
            this.lenses.add(index, new LensHandle());
        }
        this.resizeSemaphore = false;
    }
    
    protected final LensHandle getHandle(int index) {
        return this.getHandle(index, false);
    }
    
    protected final LensHandle getHandle(int index, boolean autoResize) {
        if (index >= this.lenses.size()) {
            if (!autoResize) {
                throw new IndexOutOfBoundsException("Index: " + index);
            }
            this.resize(index + 1);
        }
        return this.lenses.get(index);
    }

    @Override
    public void setProperty(Lens lens, InventoryProperty<?, ?> property) {
        this.setProperty(this.indexOf(lens), property);
    }

    @Override
    public void setProperty(int index, InventoryProperty<?, ?> property) {
        this.checkIndex(index);
        this.getHandle(index).setProperty(property);
    }
    
    @Override
    public void removeProperty(Lens lens, InventoryProperty<?, ?> property) {
        this.removeProperty(this.indexOf(lens), property);
    }
    
    @Override
    public void removeProperty(int index, InventoryProperty<?, ?> property) {
        this.checkIndex(index);
        this.getHandle(index).removeProperty(property);
    }
    
    @Override
    public Lens get(int index) {
        return this.getLens(index);
    }

    @Override
    public Lens getLens(int index) {
        return this.lenses.get(index).lens;
    }
    
    @Override
    public Lens remove(int index) {
        if (!this.allowRemove) {
            throw new UnsupportedOperationException();
        }
        
        LensHandle old = this.lenses.remove(index);
        return (old != null) ? old.lens : null;
    }
    
    @Override
    public boolean remove(Object child) {
        if (!this.allowRemove) {
            throw new UnsupportedOperationException();
        }

        int index = this.indexOf(child);
        return (index > -1) ? this.remove(index) != null : false;
    }
    
    @Override
    public int size() {
        return this.lenses.size();
    }
    
    @Override
    public boolean contains(Object o) {
        return this.indexOf(o) >= 0;
    }

    @Override
    public int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < this.lenses.size(); i++) {
                if (this.lenses.get(i).lens == null) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < this.lenses.size(); i++) {
                if (o.equals(this.lenses.get(i).lens)) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public Collection<InventoryProperty<?, ?>> getProperties(int index) {
        this.checkIndex(index);
        return this.getHandle(index).getProperties();
    }
    
    @Override
    public Collection<InventoryProperty<?, ?>> getProperties(Lens child) {
        int index = this.indexOf(child);
        if (index < 0) {
            return Collections.<InventoryProperty<?, ?>>emptyList();
        }
        return this.getProperties(index);
    }

    @Override
    public boolean has(Lens lens) {
        return this.contains(lens);
    }

    @Override
    public boolean isSubsetOf(Collection<Lens> c) {
        for (Iterator<Lens> iter = this.iterator(); iter.hasNext();) {
            if (!c.contains(iter.next())) {
                return false;
            }
        }
            
        return true;
    }

    private void checkIndex(int index) {
        if (index >= this.lenses.size()) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

}
