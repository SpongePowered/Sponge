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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.slots.SlotLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.inventory.lens.impl.struct.LensHandle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public abstract class AbstractLens implements Lens {

    protected final Class<? extends Inventory> adapterType;

    protected final int base;
    protected Lens parent;

    protected MutableLensCollection children;

    protected List<LensHandle> spanningChildren;

    protected int size;

    private int maxOrdinal = 0;

    public AbstractLens(final int base, final int size, final Class<? extends Inventory> adapterType) {
        checkArgument(base >= 0, "Invalid offset: %s", base);
        checkArgument(size > 0, "Invalid size: %s", size);
        checkNotNull(adapterType, "adapterType");

        this.base = base;
        this.size = size;
        this.adapterType = adapterType;

        this.children = new MutableLensCollection( );
        this.spanningChildren = new ArrayList<>();
    }

    protected void addChild(final Lens lens, final PropertyEntry... properties) {
        checkNotNull(lens, "Attempted to register a null lens");
        this.children.add(lens, properties);
    }

    protected void addSpanningChild(final Lens lens, final PropertyEntry... properties) {
        this.addChild(lens, properties);

        for (LensHandle spanningChild : this.spanningChildren) {
            if (spanningChild.lens == lens) { // Spanning Child already exists
                for (PropertyEntry property : properties) {
                    // Just add properties
                    spanningChild.setProperty(property);
                }
                return;
            }
        }

        final LensHandle child = new LensHandle(lens, properties);
        this.spanningChildren.add(child);
        child.ordinal = this.maxOrdinal;
        this.maxOrdinal += lens.slotCount();
        if (lens instanceof AbstractLens) {
            ((AbstractLens) lens).setParent(this);
        }
    }

    protected void setParent(final Lens parent) {
        this.parent = parent;
    }

    @Override
    public Translation getName(final Fabric fabric) {
        return fabric.fabric$getDisplayName();
    }

    @Override
    public Lens getParent() {
        return this.parent;
    }
    @Override
    public Class<? extends Inventory> getAdapterType() {
        return this.adapterType;
    }
    protected LensHandle getLensForOrdinal(final int ordinal) {
        if (ordinal < 0 || ordinal > this.maxOrdinal) {
            return null;
        }

        for (final LensHandle child : this.spanningChildren) {
            if (child.ordinal <= ordinal && (ordinal - child.ordinal) < child.lens.slotCount()) {
                return child;
            }
        }

        return null;
    }

    @Nullable
    @Override
    public SlotLens getSlotLens(int ordinal) {
        LensHandle handle = this.getLensForOrdinal(ordinal);
        if (handle == null) {
            return null;
        }
        return handle.lens.getSlotLens(ordinal - handle.ordinal);
    }

    @Override
    public List<SlotLens> getSlots() {
        this.cacheSlots(this);
        return this.slotCache.stream().map(lh -> lh.lens).map(SlotLens.class::cast).collect(Collectors.toList());
    }

    protected List<LensHandle> slotCache;

    private void cacheSlots(Lens lens) {
        if (this.slotCache != null) {
            return;
        }
        this.slotCache = new ArrayList<>();
        if (lens instanceof SlotLens) {
            this.slotCache.add(new LensHandle(lens, lens.getProperties(0)));
            return;
        }
        for (Lens child : lens.getSpanningChildren()) {
            if (child instanceof SlotLens) {
                this.slotCache.add(new LensHandle(child, lens.getProperties(child)));
            } else {
                this.cacheSlots(child);
            }
        }
    }
    @Override
    public List<Lens> getChildren() {
        return Collections.unmodifiableList(this.children.children());
    }

    @Override
    public List<Lens> getSpanningChildren() {
        final Builder<Lens> listBuilder = ImmutableList.<Lens>builder();
        for (final LensHandle child : this.spanningChildren) {
            listBuilder.add(child.lens);
        }
        return listBuilder.build();
    }

    @Override
    public int slotCount() {
        return this.size;
    }

    @Override
    public Lens getLens(final int index) {
        return this.children.getLens(index);
    }

    @Override
    public Map<Property<?>, Object> getProperties(final int index) {
        return this.children.getProperties(index);
    }

    @Override
    public Map<Property<?>, Object> getProperties(final Lens child) {
        if (!this.children.has(child)) {
            throw new NoSuchElementException("Specified child lens is not a direct descendant this lens");
        }
        return this.children.getProperties(child);
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
        return this.children.has(lens);
    }

    @Override
    public boolean isSubsetOf(final Collection<Lens> c) {
        return this.children.isSubsetOf(c);
    }

    @Override
    public List<Lens> children() {
        return this.children.children();
    }

    @Override
    public int getMaxStackSize(final Fabric fabric) {
        return fabric.fabric$getMaxStackSize();
    }

    protected boolean checkOrdinal(final int ordinal) {
        return ordinal >= 0 && ordinal < this.size;
    }

    // Helpers for Debugging
    public static final int LIMIT = 2;

    public String toString(int deep) {
        String result = this.ident(deep) + this.getClass().getSimpleName() + ": (" + this.size + ") base:" + this.base + "\n";
        if (!this.children.children().isEmpty() && deep < LIMIT) {
            result += this.ident(deep) + "Children: ";
            for (Lens child : this.children.children()) {
                for (LensHandle spanningChild : this.spanningChildren) {
                    if (spanningChild.lens == child) {
                        result += "\n" + ident(deep) + "(Spanning)";
                    }
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
        return toString(0);
    }
}
