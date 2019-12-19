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
package org.spongepowered.common.inventory.lens.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.Key;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.inventory.lens.slots.SlotLens;
import org.spongepowered.common.inventory.property.KeyValuePair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

public abstract class AbstractLens implements Lens {

    protected Class<? extends Inventory> adapterType;

    protected final int base;
    protected Lens parent;

    protected List<Lens> spanningChildren = new ArrayList<>();
    protected List<Lens> children = new ArrayList<>();
    protected Map<Lens, Map<Key, Object>> handleMap = new HashMap<>();

    protected int size;

    private int maxOrdinal = 0;

    public AbstractLens(final int base, final int size) {
        checkArgument(base >= 0, "Invalid offset: %s", base);
        checkArgument(size > 0, "Invalid size: %s", size);

        this.base = base;
        this.size = size;
    }

    public AbstractLens(final int base, final int size, final Class<? extends Inventory> adapterType) {
        checkArgument(base >= 0, "Invalid offset: %s", base);
        checkArgument(size > 0, "Invalid size: %s", size);
        checkNotNull(adapterType, "adapterType");

        this.base = base;
        this.size = size;
        this.adapterType = adapterType;
    }

    protected void addChild(final Lens lens, final KeyValuePair... keyValuePairs) {
        checkNotNull(lens, "Attempted to register a null lens");

        if (!this.children.contains(lens)) {
            this.children.add(lens);
        }
        this.addKeyValuePairs(lens, keyValuePairs);
    }

    protected void addSpanningChild(final Lens lens, final KeyValuePair... keyValuePairs) {
        this.addChild(lens, keyValuePairs);

        if (!this.spanningChildren.contains(lens)) {
            this.spanningChildren.add(lens);
            this.maxOrdinal += lens.slotCount();
        } else {
            // TODO check if this is fine
            if (lens instanceof AbstractLens) {
                ((AbstractLens) lens).setParent(this);
            }
        }
        this.addKeyValuePairs(lens, keyValuePairs);
    }

    private void addKeyValuePairs(Lens lens, KeyValuePair[] keyValuePairs) {
        Map<Key, Object> map = this.handleMap.computeIfAbsent(lens, l -> new HashMap<>());
        for (KeyValuePair pair : keyValuePairs) {
            map.put(pair.getKey(), pair.getValue());
        }
    }

    protected void setParent(final Lens parent) {
        this.parent = parent;
    }

    @Override
    public Lens getParent() {
        return this.parent;
    }

    @Override
    public Class<? extends Inventory> getAdapterType() {
        return this.adapterType;
    }

    @Nullable
    @Override
    public SlotLens getSlotLens(int ordinal) {
        if (ordinal < 0 || ordinal > this.maxOrdinal) {
            return null;
        }

        int offset = 0;
        for (final Lens child : this.spanningChildren) {
            int count = child.slotCount();
            if (ordinal < offset + count) {
                return child.getSlotLens(ordinal - offset);
            }
            offset += count;
        }
        return null;
    }

    private List<SlotLens> slotCache;

    @Override
    public List<SlotLens> getSlots() {
        if (this.slotCache == null) {
            this.slotCache = new ArrayList<>();
            if (this instanceof SlotLens) {
                this.slotCache.add((SlotLens) this);
            } else {
                for (Lens child : this.getSpanningChildren()) {
                    if (child instanceof SlotLens) {
                        this.slotCache.add((SlotLens) child);
                    } else {
                        this.slotCache.addAll(child.getSlots());
                    }
                }
            }
        }
        return this.slotCache;
    }

    @Override
    public List<Lens> getChildren() {
        return Collections.unmodifiableList(this.children);
    }

    @Override
    public List<Lens> getSpanningChildren() {
        return this.spanningChildren;
    }

    @Override
    public int slotCount() {
        return this.size;
    }

    @Override
    public Lens getLens(final int index) {
        return this.children.get(index);
    }

    @Override
    public Map<Key, Object> getDataAt(final int index) {
        return this.getDataFor(this.getLens(index));
    }

    @Override
    public Map<Key, Object> getDataFor(final Lens child) {
        if (!this.has(child)) {
            throw new NoSuchElementException("Specified child lens is not a direct descendant this lens");
        }
        return this.handleMap.get(child);
    }

    /**
     * Adds missing spanning slots for basic support
     *
     * @param base The last added slot
     * @param slots The slot provider
     */
    protected void addMissingSpanningSlots(int base, SlotLensProvider slots) {

        int additionalSlots = this.size - base;
        if (additionalSlots > 0) {
            this.addSpanningChild(new DefaultIndexedLens(base, additionalSlots, slots));
        }
    }

    @Override
    public boolean has(final Lens lens) {
        return this.children.contains(lens);
    }

    @Override
    public boolean isSubsetOf(final Collection<Lens> c) {
        return c.containsAll(this.children);
    }

    @Override
    public List<Lens> children() {
        return this.children;
    }

    @Override
    public int getMaxStackSize(final Fabric fabric) {
        return fabric.fabric$getMaxStackSize();
    }

    // Helpers for Debugging
    public static final int LIMIT = 2;

    public String toString(int deep) {
        String result = this.ident(deep) + this.getClass().getSimpleName() + ": (" + this.size + ") base:" + this.base + "\n";
        if (!this.children.isEmpty() && deep < LIMIT) {
            result += this.ident(deep) + "Children: ";
            for (Lens child : this.children) {
                if (this.spanningChildren.contains(child)) {
                    result += "\n" + this.ident(deep) + "(Spanning)";
                }
                result += child.toString(deep + 1);
            }
        }
        return result;
    }

    private String ident(int deep) {
        String ident = "";
        for (int i = 0; i < deep; i++) {
            ident += "-";
        }
        return ident;
    }

    @Override
    public String toString() {
        return this.toString(0);
    }
}
