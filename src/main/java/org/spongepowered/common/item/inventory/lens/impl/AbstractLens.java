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
package org.spongepowered.common.item.inventory.lens.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.MutableLensCollection;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.collections.MutableLensCollectionImpl;
import org.spongepowered.common.item.inventory.lens.impl.struct.LensHandle;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class AbstractLens implements Lens {

    protected final Class<? extends Inventory> adapterType;
    
    protected final int base;
    
    protected final IntSet availableSlots = new IntOpenHashSet();
    
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

        this.children = new MutableLensCollectionImpl(0, false);
        this.spanningChildren = new ArrayList<>();
    }

    protected void addChild(final Lens lens, final InventoryProperty<?, ?>... properties) {
        checkNotNull(lens, "Attempted to register a null lens");
        this.children.add(lens, properties);
        this.availableSlots.addAll(lens.getSlots());
    }
    
    protected void addSpanningChild(final Lens lens, final InventoryProperty<?, ?>... properties) {
        this.addChild(lens, properties);
        final LensHandle child = new LensHandle(lens, properties);
        this.spanningChildren.add(child);
        child.ordinal = this.maxOrdinal;
        this.maxOrdinal += lens.getSlots().size();
        if (lens instanceof AbstractLens) {
            ((AbstractLens) lens).setParent(this);
        }
    }
    
    protected void setParent(final Lens parent) {
        this.parent = parent;
    }
    
    @Override
    public Translation getName(final Fabric inv) {
        return inv.fabric$getDisplayName();
    }
    
    @Override
    public Lens getParent() {
        return this.parent;
    }
    
    @Override
    public IntSet getSlots() {
        return IntSets.unmodifiable(this.availableSlots);
    }
    
    @Override
    public boolean hasSlot(final int index) {
        return this.availableSlots.contains(index);
    }
    
    @Override
    public Class<? extends Inventory> getAdapterType() {
        return this.adapterType;
    }
    
    @Override
    public ItemStack getStack(final Fabric inv, final int ordinal) {
        final LensHandle lens = this.getLensForOrdinal(ordinal);
        if (lens == null) {
            return ItemStack.EMPTY;
        }
        return lens.lens.getStack(inv, ordinal - lens.ordinal);
    }
    
    @Override
    public boolean setStack(final Fabric inv, final int ordinal, final ItemStack stack) {
        final LensHandle lens = this.getLensForOrdinal(ordinal);
        return lens != null && lens.lens.setStack(inv, ordinal - lens.ordinal, stack);
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

    @Override
    public SlotLens getSlotLens(final int ordinal) {
        final LensHandle handle = this.getLensForOrdinal(ordinal);
        if (handle == null) {
                return null;
        }
        return handle.lens.getSlotLens(ordinal - handle.ordinal);
    }
    
    @Override
    public List<Lens> getChildren() {
        return Collections.unmodifiableList(this.children);
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
    public Collection<InventoryProperty<?, ?>> getProperties(final int index) {
        return this.children.getProperties(index);
    }
    
    @Override
    public Collection<InventoryProperty<?, ?>> getProperties(final Lens child) {
        final int index = this.children.indexOf(child);
        if (index < 0) {
            throw new NoSuchElementException("Specified child lens is not a direct descendant this lens");
        }
        return this.children.getProperties(index);
    }

    @Override
    public boolean has(final Lens lens) {
        return this.children.contains(lens);
    }

    @Override
    public boolean isSubsetOf(final Collection<Lens> c) {
        return this.children.isSubsetOf(c);
    }

    @Override
    public Iterator<Lens> iterator() {
        return this.children.iterator();
    }
    
    @Override
    public int getRealIndex(final Fabric inv, final int ordinal) {
        final LensHandle child = this.getLensForOrdinal(ordinal);
        return child.lens.getRealIndex(inv, ordinal - child.ordinal);
    }

    @Override
    public int getMaxStackSize(final Fabric inv) {
        return inv.fabric$getMaxStackSize();
    }

    protected boolean checkOrdinal(final int ordinal) {
        return ordinal >= 0 && ordinal < this.size;
    }

}
