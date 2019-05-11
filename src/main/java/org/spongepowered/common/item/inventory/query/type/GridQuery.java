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
package org.spongepowered.common.item.inventory.query.type;

import com.flowpowered.math.vector.Vector2i;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.item.inventory.EmptyInventoryImpl;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.GridInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.UniqueCustomSlotProvider;
import org.spongepowered.common.item.inventory.lens.comp.GridInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;
import org.spongepowered.common.item.inventory.query.SpongeQuery;

public class GridQuery extends SpongeQuery {

    private final Vector2i offset;
    private final Vector2i size;

    public GridQuery(Vector2i offset, Vector2i size) {
        this.offset = offset;
        this.size = size;
    }

    @Override
    public Inventory execute(InventoryAdapter inventory) {

        if (!(inventory instanceof GridInventoryAdapter)) {
            return new EmptyInventoryImpl(inventory);
        }

        GridInventoryAdapter gridAdapter = (GridInventoryAdapter) inventory;

        Vector2i max = gridAdapter.getDimensions();
        if (max.getX() < offset.getX() + size.getX() && max.getY() < offset.getY() + size.getY()) {
            // queried grid does not fit inventory
            return new EmptyInventoryImpl(inventory);
        }

        // Get slots for new grid
        UniqueCustomSlotProvider slotProvider = new UniqueCustomSlotProvider();

        for (int dy = 0; dy < size.getY(); dy++) {
            for (int dx = 0; dx < size.getX(); dx++) {
                slotProvider.add(gridAdapter.getSlotLens(offset.getX() + dx, offset.getY() + dy));
            }
        }

        // build new grid lens
        GridInventoryLens lens = new GridInventoryLensImpl(size.getX(), size.getY(), slotProvider);
        return new GridInventoryAdapter(inventory.getFabric(), lens, inventory);
    }


}
