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
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.InvalidLensDefinitionException;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.MutableLensCollection;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.collections.MutableLensCollectionImpl;
import org.spongepowered.common.item.inventory.lens.impl.struct.LensHandle;
import org.spongepowered.common.item.inventory.observer.InventoryEventArgs;
import org.spongepowered.common.item.inventory.observer.InventoryEventArgs.Type;
import org.spongepowered.common.util.observer.Observer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class AbstractLens<TInventory, TStack> extends ObservableLens<TInventory, TStack> implements Observer<InventoryEventArgs> {

    protected final Class<? extends Inventory> adapterType;
    
    protected final int base;
    
    protected final IntSet availableSlots = new IntOpenHashSet();
    
    protected Lens<TInventory, TStack> parent; 
    
    protected MutableLensCollection<TInventory, TStack> children;
    
    protected List<LensHandle<TInventory, TStack>> spanningChildren;
    
    protected int size;
    
    private int maxOrdinal = 0;
    
    public AbstractLens(int base, int size, InventoryAdapter<TInventory, TStack> adapter, SlotProvider<TInventory, TStack> slots) {
        this(base, size, adapter.getClass(), slots);
    }

    public AbstractLens(int base, int size, Class<? extends Inventory> adapterType, SlotProvider<TInventory, TStack> slots) {
        checkArgument(base >= 0, "Invalid offset: %s", base);
        checkArgument(size > 0, "Invalid size: %s", size);
        checkNotNull(adapterType, "adapterType");

        this.base = base;
        this.size = size;
        this.adapterType = adapterType;

        this.prepare();
    }

    protected void prepare() {
        this.children = new MutableLensCollectionImpl<>(0, false);
        this.spanningChildren = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    protected void init(InventoryAdapter<TInventory, TStack> adapter, SlotProvider<TInventory, TStack> slots) {
        try {
            if (slots != null) {
                this.init(slots);
            } else if (adapter instanceof SlotProvider) {
                this.init((SlotProvider<TInventory, TStack>) adapter);
            } else {
                this.init(index -> {
                    throw new NoSuchElementException("Attempted to fetch slot at index " + index + " but no provider was available instancing " + AbstractLens.this);
                });
            }
        } catch (NoSuchElementException ex) {
            throw new InvalidLensDefinitionException("Invalid lens definition, the lens referenced slots which do not exist.", ex);
        }
    }

    /**
     * Initialise children
     * 
     * @param slots
     */
    protected abstract void init(SlotProvider<TInventory, TStack> slots);
    
    protected void addChild(Lens<TInventory, TStack> lens, InventoryProperty<?, ?>... properties) {
        checkNotNull(lens, "Attempted to register a null lens");
        this.children.add(lens, properties);
        this.availableSlots.addAll(lens.getSlots());
        
        if (lens instanceof ObservableLens) {
            ((ObservableLens<TInventory, TStack>) lens).addObserver(this);
        }
        
        this.raise(new InventoryEventArgs(Type.LENS_ADDED, this));
    }
    
    protected void addSpanningChild(Lens<TInventory, TStack> lens, InventoryProperty<?, ?>... properties) {
        this.addChild(lens, properties);
        LensHandle<TInventory, TStack> child = new LensHandle<TInventory, TStack>(lens, properties);
        this.spanningChildren.add(child);
        child.ordinal = this.maxOrdinal;
        this.maxOrdinal += lens.getSlots().size();
        if (lens instanceof AbstractLens) {
            ((AbstractLens<TInventory, TStack>) lens).setParent(this);
        }
    }
    
    protected void setParent(Lens<TInventory, TStack> parent) {
        this.parent = parent;
    }
    
    @Override
    public Translation getName(Fabric<TInventory> inv) {
        return inv.getDisplayName();
    }
    
    @Override
    public Lens<TInventory, TStack> getParent() {
        return this.parent;
    }
    
    @Override
    public IntSet getSlots() {
        return IntSets.unmodifiable(this.availableSlots);
    }
    
    @Override
    public boolean hasSlot(int index) {
        return this.availableSlots.contains(index);
    }
    
    @Override
    public Class<? extends Inventory> getAdapterType() {
        return this.adapterType;
    }
    
    @Override
    public TStack getStack(Fabric<TInventory> inv, int ordinal) {
        LensHandle<TInventory, TStack> lens = this.getLensForOrdinal(ordinal);
        if (lens == null) {
            return null;
        }
        return lens.lens.getStack(inv, ordinal - lens.ordinal);
    }
    
    @Override
    public boolean setStack(Fabric<TInventory> inv, int ordinal, TStack stack) {
        LensHandle<TInventory, TStack> lens = this.getLensForOrdinal(ordinal);
        return lens != null && lens.lens.setStack(inv, ordinal - lens.ordinal, stack);
    }
    
    protected LensHandle<TInventory, TStack> getLensForOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal > this.maxOrdinal) {
            return null;
        }
        
        for (LensHandle<TInventory, TStack> child : this.spanningChildren) {
            if (child.ordinal <= ordinal && (ordinal - child.ordinal) < child.lens.slotCount()) {
                return child;
            }
        }
        
        return null;
    }
    
    @Override
    public List<Lens<TInventory, TStack>> getChildren() {
        return Collections.unmodifiableList(this.children);
    }

    @Override
    public List<Lens<TInventory, TStack>> getSpanningChildren() {
        Builder<Lens<TInventory, TStack>> listBuilder = ImmutableList.<Lens<TInventory, TStack>>builder();
        for (LensHandle<TInventory, TStack> child : this.spanningChildren) {
            listBuilder.add(child.lens);
        }
        return listBuilder.build();
    }

    @Override
    public int slotCount() {
        return this.size;
    }
    
    @Override
    public Lens<TInventory, TStack> getLens(int index) {
        return this.children.getLens(index);
    }

    @Override
    public Collection<InventoryProperty<?, ?>> getProperties(int index) {
        return this.children.getProperties(index);
    }
    
    @Override
    public Collection<InventoryProperty<?, ?>> getProperties(Lens<TInventory, TStack> child) {
        int index = this.children.indexOf(child);
        if (index < 0) {
            throw new NoSuchElementException("Specified child lens is not a direct descendant this lens");
        }
        return this.children.getProperties(index);
    }

    @Override
    public boolean has(Lens<TInventory, TStack> lens) {
        return this.children.contains(lens);
    }

    @Override
    public boolean isSubsetOf(Collection<Lens<TInventory, TStack>> c) {
        return this.children.isSubsetOf(c);
    }

    @Override
    public Iterator<Lens<TInventory, TStack>> iterator() {
        return this.children.iterator();
    }
    
    @Override
    public void notify(Object source, InventoryEventArgs e) {
        if (e.type == Type.LENS_INVALIDATED || e.type == Type.SLOT_CONTENT_CHANGED) {
            this.raise(e);
        }
        if (e.type == Type.LENS_ADDED && source instanceof Lens && this.children.contains(source)) {
            this.availableSlots.addAll(((Lens<?, ?>)source).getSlots());
        }
    }        

    @Override
    public void invalidate(Fabric<TInventory> inv) {
        this.raise(new InventoryEventArgs(Type.LENS_INVALIDATED, this));
    }

    @Override
    public int getRealIndex(Fabric<TInventory> inv, int ordinal) {
        LensHandle<TInventory, TStack> child = this.getLensForOrdinal(ordinal);
        return child.lens.getRealIndex(inv, ordinal - child.ordinal);
    }

    @Override
    public int getMaxStackSize(Fabric<TInventory> inv) {
        return inv.getMaxStackSize();
    }

    protected boolean checkOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < this.size;
    }

}
