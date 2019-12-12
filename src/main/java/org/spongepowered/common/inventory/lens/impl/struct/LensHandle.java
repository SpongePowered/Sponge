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
package org.spongepowered.common.inventory.lens.impl.struct;

import org.spongepowered.api.data.property.Property;
import org.spongepowered.common.inventory.property.PropertyEntry;
import org.spongepowered.common.inventory.lens.Lens;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

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
    private Map<Property<?>, Object> properties;
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
    public LensHandle(Lens lens, PropertyEntry... properties) {
        this.lens = lens;
        if (properties != null && properties.length > 0) {
            this.properties = new HashMap<>();
            for (PropertyEntry entry : properties) {
                this.properties.put(entry.getProperty(), entry.getValue());
            }
        }
    }

    /**
     * Create a lens handle containing the specified lens, and define the
     * supplied properties
     *
     * @param lens
     * @param properties
     */
    public LensHandle(Lens lens, Map<Property<?>, Object> properties) {
        this.lens = lens;
        if (properties != null && properties.size() > 0) {
            this.properties = new HashMap<>(properties);
        }
    }

    public Map<Property<?>, Object> getProperties() {
        if (this.properties == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(this.properties);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <V> V getProperty(Property<V> property) {
        if (this.properties != null) {
            return (V) this.properties.get(property);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public void setProperty(PropertyEntry tuple) {
        this.setProperty((Property) tuple.getProperty(), tuple.getValue());
    }

    public <V> void setProperty(Property<V> property, V value) {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.put(property, value);
    }

    @Override
    public String toString() {
        return this.lens.toString();
    }

}
