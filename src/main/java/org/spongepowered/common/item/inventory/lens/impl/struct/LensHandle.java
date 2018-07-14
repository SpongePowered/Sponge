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
package org.spongepowered.common.item.inventory.lens.impl.struct;

import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.common.item.inventory.lens.Lens;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * A relationship between a lens and properties for the lens when viewed through
 * another, primarily used as elements in lens collections.
 */
public final class LensHandle {

    /**
     * The lens
     */
    public Lens lens;
    
    /**
     * Ordinal, when required 
     */
    public int ordinal = -1;
    
    /**
     * Properties for the lens when viewed through the parent lens
     */
    private Collection<InventoryProperty<?, ?>> properties;
    
    /**
     * Create an "empty" lens handle
     */
    public LensHandle() {
        this(null);
    }
    
    /**
     * Create a lens handle containing the specified lens, and define the
     * supplied properties
     * 
     * @param lens
     * @param properties
     */
    public LensHandle(Lens lens, InventoryProperty<?, ?>... properties) {
        this.lens = lens;
        if (properties != null && properties.length > 0) {
            this.properties = new ArrayList<>();
            Collections.addAll(this.properties, properties);
        }
    }

    /**
     * Create a lens handle containing the specified lens, and define the
     * supplied properties
     * 
     * @param lens
     * @param properties
     */
    public LensHandle(Lens lens, Collection<InventoryProperty<?, ?>> properties) {
        this.lens = lens;
        if (properties != null && properties.size() > 0) {
            this.properties = new ArrayList<>(properties);
        }
    }
    
    public Collection<InventoryProperty<?, ?>> getProperties() {
        if (this.properties == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableCollection(this.properties);
    }

    public <T, K, V> InventoryProperty<K, V> getProperty(Class<T> property) {
        return this.getProperty(property, null);
    }
    
    @SuppressWarnings("unchecked")
    public <T, K, V> InventoryProperty<K, V> getProperty(Class<T> property, Object key) {
        if (this.properties != null) {
            for (InventoryProperty<?, ?> prop : this.properties) {
                if (prop.getClass().equals(property) && (key == null || key.equals(prop.getKey()))) {
                    return (InventoryProperty<K, V>) prop;
                }
            }
        }        
        return null;
    }

    public void setProperty(InventoryProperty<?, ?> property) {
        if (this.properties == null) {
            this.properties = new ArrayList<InventoryProperty<?, ?>>();
        } else {
            this.removeMatchingProperties( property);
        }
        this.properties.add(property);
    }

    public void removeProperty(InventoryProperty<?, ?> property) {
        if (this.properties != null) {
            this.removeMatchingProperties(property);
        }
    }

    private void removeMatchingProperties(InventoryProperty<?, ?> property) {
        for (Iterator<InventoryProperty<?, ?>> iter = this.properties.iterator(); iter.hasNext();) {
            InventoryProperty<?, ?> prop = iter.next();
            if (prop.getClass() == property.getClass() && prop.getKey().equals(property.getKey())) {
                iter.remove();
            }
        }
    }

}
