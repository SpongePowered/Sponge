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
package org.spongepowered.common.inventory.adapter.impl;

import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.property.store.common.InventoryPropertyProvider;

import java.util.Map;
import java.util.Optional;

public interface InventoryPropertyHolder extends Inventory {


    @Override
    default <V> Optional<V> getProperty(Inventory child, Property<V> property) {
        return InventoryPropertyProvider.getProperty(this, child, property);
    }

    @Override
    default <V> Optional<V> getProperty(Property<V> property) {
        if (parent() == this) {
            return InventoryPropertyProvider.getRootProperty(this, property);
        }
        return parent().getProperty(this, property);
    }

    @Override
    default Map<Property<?>, ?> getProperties() {
        return SpongeImpl.getPropertyRegistry().getPropertiesFor(this);
    }

}