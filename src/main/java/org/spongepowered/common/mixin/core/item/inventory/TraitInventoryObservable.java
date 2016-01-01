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
package org.spongepowered.common.mixin.core.item.inventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.interfaces.inventory.trait.IInventoryObservable;
import org.spongepowered.common.item.inventory.observer.InventoryEventArgs;
import org.spongepowered.common.util.observer.Observable;
import org.spongepowered.common.util.observer.Observer;

import java.util.ArrayList;
import java.util.List;

/**
 * This mixin is a trait for inventory components which participate in the
 * observer pattern internal to the inventory system's update logic. Writing
 * this as a trait means that we don't need to copy/paste this boiler plate into
 * all observables.
 */
@Mixin({
    net.minecraft.inventory.Slot.class,
    net.minecraft.item.ItemStack.class
})
public abstract class TraitInventoryObservable implements IInventoryObservable {

    private final List<Observer<InventoryEventArgs>> observers = new ArrayList<Observer<InventoryEventArgs>>();

    @Override
    public Observable<InventoryEventArgs> addObserver(Observer<InventoryEventArgs> observer) {
        if (!this.observers.contains(observer)) {
            this.observers.add(observer);
        }
        return this;
    }

    @Override
    public Observable<InventoryEventArgs> removeObserver(Observer<InventoryEventArgs> observer) {
        this.observers.remove(observer);
        return this;
    }
    
    @Override
    public Observable<InventoryEventArgs> clearObservers() {
        this.observers.clear();
        return this;
    }
    
    @Override
    public Observable<InventoryEventArgs> raise(InventoryEventArgs eventArgs) {
        for (Observer<InventoryEventArgs> observer : this.observers) {
            observer.notify(this, eventArgs);
        }
        return this;
    }
    
    @Override
    public Iterable<Observer<InventoryEventArgs>> getObservers() {
        return this.observers;
    }

}
