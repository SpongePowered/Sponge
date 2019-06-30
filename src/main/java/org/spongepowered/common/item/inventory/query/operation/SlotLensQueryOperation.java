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

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.query.SpongeQueryOperation;
import org.spongepowered.common.item.inventory.query.SpongeQueryOperationTypes;

public final class SlotLensQueryOperation extends SpongeQueryOperation<ImmutableSet<Inventory>> {

    private final ImmutableSet<Inventory> inventories;

    public SlotLensQueryOperation(ImmutableSet<Inventory> inventories) {
        super(SpongeQueryOperationTypes.SLOT_LENS);
        this.inventories = inventories;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean matches(Lens lens, Lens parent, Fabric inventory) {
        for (Inventory inv : this.inventories) {
            for (Inventory slot : inv.slots()) {
                if (((SlotAdapter) slot).bridge$getRootLens().equals(lens)) {
                    return true;
                }
            }
        }
        return false;
    }

}
