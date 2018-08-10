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

import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.query.SpongeQueryOperation;

public final class InventoryPropertyQueryOperation extends SpongeQueryOperation<InventoryProperty<?, ?>> {

    private final InventoryProperty<?, ?> property;

    public InventoryPropertyQueryOperation(InventoryProperty<?, ?> property) {
        super(QueryOperationTypes.INVENTORY_PROPERTY);
        this.property = property;
    }

    @Override
    public boolean matches(Lens lens, Lens parent, Fabric inventory) {
        if (parent == null) {
            return false;
        }
        for (InventoryProperty<?, ?> lensProperty : parent.getProperties(lens)) {
            if (this.property.matches(lensProperty)) {
                return true;
            }
        }
        return false;
    }

}
