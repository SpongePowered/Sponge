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
package org.spongepowered.common.util.observer;

/**
 * Observer pattern for Sponge. Java's observer pattern classes
 * use a concrete class for Observer which means we can't mix in
 * to target classes. This implementation of Observer pattern uses
 * interfaces so we can mix in to targets, we also make the
 * notification payload generic to avoid some explicit casting in
 * observers.
 * 
 * @param <E> Event arg payload type
 */
public interface Observable<E extends EventArgs> {
    
    /**
     * Add an observer for this object.
     * 
     * @param observer Observer to add, the observer will not be added if it
     *      already exists in this observer
     * @return fluent
     */
    Observable<E> addObserver(Observer<E> observer);

    /**
     * Remove an observer from this object, removing an observer which
     * is not in the observer list is not an error.
     * 
     * @param observer Observer to remove
     * @return fluent
     */
    Observable<E> removeObserver(Observer<E> observer);
    
    /**
     * Remove all observers from this object.
     * 
     * @return fluent
     */
    Observable<E> clearObservers();
    
    /**
     * Get all the observers on this object.
     * 
     * @return all observers
     */
    Iterable<Observer<E>> getObservers();
    
    /**
     * Raise E against all observers.
     * 
     * @param eventArgs argument payload
     * @return fluent
     */
    Observable<E> raise(E eventArgs);

}
