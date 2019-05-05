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
package org.spongepowered.common.item.inventory.query.operation;

import org.spongepowered.api.data.property.PropertyMatcher;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.common.item.inventory.custom.CustomInventory;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.query.SpongeQueryOperation;

import java.util.Collection;

@SuppressWarnings("unchecked")
public final class InventoryPropertyMatcherQueryOperation extends SpongeQueryOperation<PropertyMatcher<?>> {

    private final PropertyMatcher propertyMatcher;

    public InventoryPropertyMatcherQueryOperation(PropertyMatcher propertyMatcher) {
        super(QueryTypes.PROPERTY);
        this.propertyMatcher = propertyMatcher;
    }

    @Override
    public boolean matches(Lens lens, Lens parent, Fabric inventory) {
        if (parent == null) {
            return false;
        }

        // Check for custom inventory properties first
        // TODO check if this works
        Collection<?> invs = inventory.allInventories();
        if (invs.size() > 0) {
            Object inv = invs.iterator().next();
            if (inv instanceof CustomInventory) {
                Object value = ((CustomInventory) inv).getProperties().get(this.propertyMatcher.getProperty());
                if (this.propertyMatcher.matches(value)) {
                    return true;
                }
            }
        }

        // Check for lens properties
        final Object value = parent.getProperties(lens).get(this.propertyMatcher.getProperty());
        return this.propertyMatcher.matches(value);
    }

}
