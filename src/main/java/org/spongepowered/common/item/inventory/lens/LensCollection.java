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
package org.spongepowered.common.item.inventory.lens;

import org.spongepowered.api.item.inventory.InventoryProperty;

import java.util.Collection;


public interface LensCollection<TInventory, TStack> extends Iterable<Lens<TInventory, TStack>> {
    
    /**
     * Gets the lens at the specified index in this collection
     * 
     * @param index
     * @return
     */
    Lens<TInventory, TStack> getLens(int index);
    
    /**
     * Get all the properties for the specified target slot.
     * 
     * @param index the lens index to fetch
     * @return collection of properties for the specified slot when viewed
     *      through this lens
     */
    Collection<InventoryProperty<?, ?>> getProperties(int index);
    
    /**
     * Get all the properties for the specified lens (if contained in this
     * collection).
     * 
     * @param index the lens index to fetch
     * @return collection of properties for the specified slot when viewed
     *      through this lens
     */
    Collection<InventoryProperty<?, ?>> getProperties(Lens<TInventory, TStack> lens);

    /**
     * A strongly-typed {@link java.util.Collection#contains}.
     * 
     * @param lens
     * @return
     */
    boolean has(Lens<TInventory, TStack> lens);

    /**
     * Somewhat the inverse of {@link java.util.Collection#containsAll} in
     * that it checks that the <em>supplied</em> collection contains all of the
     * members of <em>this</em> lens (eg. that this lens collection is a subset
     * of the supplied collection).
     * 
     * @param c Collection to inspect
     * @return
     */
    boolean isSubsetOf(Collection<Lens<TInventory, TStack>> c);
    
}
