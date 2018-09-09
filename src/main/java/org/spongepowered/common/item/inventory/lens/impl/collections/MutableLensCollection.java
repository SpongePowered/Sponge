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
import org.spongepowered.common.item.inventory.lens.LensCollection;
import org.spongepowered.common.item.inventory.lens.impl.struct.LensHandle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MutableLensCollection implements LensCollection {

    protected final List<Lens> lenses = new ArrayList<>();
    protected Map<Lens, LensHandle> handles = new HashMap<>();

    /**
     * Adds a lens with properties.
     * If the lens already exists the properties are just added to the existing LensHandle
     *
     * @param lens The lens to add.
     * @param properties The properties to add.
     */
    public void add(Lens lens, InventoryProperty<?, ?>... properties) {
        if (!this.handles.containsKey(lens)) {
            this.lenses.add(lens);
        }
        LensHandle handle = this.handles.computeIfAbsent(lens, l -> new LensHandle(lens));
        Arrays.stream(properties).forEach(handle::setProperty);
    }

    protected final LensHandle getHandle(int index) {
        return this.handles.get(this.lenses.get(index));
    }

    public Lens get(int index) {
        return this.getLens(index);
    }

    @Override
    public Lens getLens(int index) {
        return this.lenses.get(index);
    }

    @Override
    public Collection<InventoryProperty<?, ?>> getProperties(int index) {
        this.checkIndex(index);
        return this.getHandle(index).getProperties();
    }
    
    @Override
    public Collection<InventoryProperty<?, ?>> getProperties(Lens child) {
        LensHandle handle = this.handles.get(child);
        return handle == null ? Collections.emptyList() : handle.getProperties();
    }

    @Override
    public boolean has(Lens lens) {
        return this.lenses.contains(lens);
    }

    @Override
    public boolean isSubsetOf(Collection<Lens> c) {
        return c.containsAll(this.lenses);
    }

    private void checkIndex(int index) {
        if (index >= this.lenses.size()) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    @Override
    public List<Lens> getChildren() {
        return this.lenses;
    }
}
