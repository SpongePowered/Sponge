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
package org.spongepowered.common.item.inventory.lens.impl.comp;

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.api.data.Property.Operator;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.Inventory2DAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.comp.Inventory2DLens;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;

public class Inventory2DLensImpl extends OrderedInventoryLensImpl implements Inventory2DLens {

    protected final int width;
    protected final int height;

    protected final int xBase;
    protected final int yBase;

    public Inventory2DLensImpl(int base, int width, int height, SlotProvider slots) {
        this(base, width, height, width, Inventory2DAdapter.class, slots);
    }

    protected Inventory2DLensImpl(int base, int width, int height, Class<? extends Inventory> adapterType, SlotProvider slots) {
        this(base, width, height, width, adapterType, slots);
    }

    protected Inventory2DLensImpl(int base, int width, int height, int rowStride, Class<? extends Inventory> adapterType, SlotProvider slots) {
        this(base, width, height, rowStride, 0, 0, adapterType, slots);
    }

    protected Inventory2DLensImpl(int base, int width, int height, int rowStride, int xBase, int yBase, Class<? extends Inventory> adapterType, SlotProvider slots) {
        super(base, width * height, rowStride, adapterType, slots);

        checkArgument(width > 0, "Invalid width: %s", width);
        checkArgument(height > 0, "Invalid height: %s", height);
        checkArgument(rowStride >= width, "Invalid stride: stride=%s, width=%s", rowStride, width);

        this.width = width;
        this.height = height;
        this.xBase = xBase;
        this.yBase = yBase;

        this.init(slots, true);
    }

    @Override
    protected void init(SlotProvider slots) {
        //this.init(slots, true);
    }

    /**
     * Basic initializer for two dimensional inventories. Adds child slots
     * directly to this lens starting at {@link #base} and creating a
     * rectangular array of slots {@link #width} by {@link #height} assuming
     * that the target inventory is {@link #stride} slots wide.
     *
     * @param slots the provider of the slots
     * @param spanning Set to true to create spanning slots, false to create
     *      normal child slots
     */
    protected void init(SlotProvider slots, boolean spanning) {
        for (int y = 0, ord = 0, slot = this.base; y < this.height; y++, slot += (this.stride - this.width)) {
            for (int x = 0; x < this.width; x++, slot++, ord++) {
                SlotLens slotLens = slots.getSlot(slot);
//                System.err.printf(">> %s ord: %-4d x: %-4d y: %-4d slot: %-4d => %s\n",
//                        this.getClass().getSimpleName(), ord, x, y, slot, slotLens.getClass().getSimpleName());
                if (spanning) {
                    this.addSpanningChild(slotLens, new SlotIndex(ord), new SlotPos(this.xBase + x, this.yBase + y));
                } else {
                    this.addChild(slotLens, new SlotIndex(ord), new SlotPos(this.xBase + x, this.yBase + y));
                }
            }
        }
        super.cache();
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public SlotLens getSlot(SlotPos pos) {
        if (pos.getOperator() != Operator.EQUAL) {
            return null;
        }

        return (SlotLens) this.spanningChildren.get(pos.getY()).lens.getLens(pos.getX());
    }

    @Override
    public int getRealIndex(Fabric inv, int ordinal) {
        if (!this.checkOrdinal(ordinal)) {
            return -1;
        }

        return this.base + ((ordinal / this.width) * this.stride) + (ordinal % this.width);
    }

    @Override
    public InventoryAdapter getAdapter(Fabric inv, Inventory parent) {
        return new Inventory2DAdapter(inv, this, parent);
    }

}
