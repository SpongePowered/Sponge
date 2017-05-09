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

import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.observer.InventoryEventArgs;
import org.spongepowered.common.util.observer.Observable;
import org.spongepowered.common.util.observer.Observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete lens class which implements observable.
 * 
 * @param <TInventory>
 * @param <TStack>
 */
public abstract class ObservableLens<TInventory, TStack> implements Lens<TInventory, TStack>, Observable<InventoryEventArgs> {
    
    /**
     * Observers.
     */
    private final List<Observer<InventoryEventArgs>> observers = new ArrayList<Observer<InventoryEventArgs>>();
    
    /* (non-Javadoc)
     * @see org.spongepowered.common.util.observer.Observable
     *      #addObserver(org.spongepowered.common.util.observer.Observer)
     */
    @Override
    public Observable<InventoryEventArgs> addObserver(Observer<InventoryEventArgs> observer) {
        if (!this.observers.contains(observer)) {
            this.observers.add(observer);
        }
        return this;
    }

    /* (non-Javadoc)
     * @see org.spongepowered.common.util.observer.Observable
     *      #removeObserver(org.spongepowered.common.util.observer.Observer)
     */
    @Override
    public Observable<InventoryEventArgs> removeObserver(Observer<InventoryEventArgs> observer) {
        this.observers.remove(observer);
        return this;
    }
    
    /* (non-Javadoc)
     * @see org.spongepowered.common.util.observer.Observable#clearObservers()
     */
    @Override
    public Observable<InventoryEventArgs> clearObservers() {
        this.observers.clear();
        return this;
    }
    
    /* (non-Javadoc)
     * @see org.spongepowered.common.util.observer.Observable
     *      #raise(org.spongepowered.common.util.observer.EventArgs)
     */
    @Override
    public Observable<InventoryEventArgs> raise(InventoryEventArgs eventArgs) {
        for (Observer<InventoryEventArgs> observer : this.observers) {
            observer.notify(this, eventArgs);
        }
        return this;
    }
    
    /* (non-Javadoc)
     * @see org.spongepowered.common.util.observer.Observable#getObservers()
     */
    @Override
    public Iterable<Observer<InventoryEventArgs>> getObservers() {
        return this.observers;
    }

}
