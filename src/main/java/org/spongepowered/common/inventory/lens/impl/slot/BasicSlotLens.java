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
package org.spongepowered.common.inventory.lens.impl.slot;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.InvalidOrdinalException;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.AbstractLens;
import org.spongepowered.common.inventory.lens.slots.SlotLens;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Base Lens for Slots
 */
public class BasicSlotLens extends AbstractLens implements SlotLens {

    public static final Translation SLOT_NAME = new SpongeTranslation("slot.name");

    protected int maxStackSize = -1;

    public BasicSlotLens(int index) {
        this(index, SlotAdapter.class);
    }

    public BasicSlotLens(int index, Class<? extends Inventory> adapterType) {
        super(index, 1, adapterType);
    }

    @Override
    public Translation getName(Fabric fabric) {
        return BasicSlotLens.SLOT_NAME;
    }

    @Override
    public InventoryAdapter getAdapter(Fabric fabric, Inventory parent) {
        return new SlotAdapter(fabric, this, parent);
    }

    @Override
    public List<Lens> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public List<Lens> getSpanningChildren() {
        return Collections.emptyList();
    }

    @Override
    public int getOrdinal(Fabric fabric) {
        return this.base;
    }

    @Override
    public ItemStack getStack(Fabric fabric, int ordinal) {
        if (ordinal != 0) {
            throw new InvalidOrdinalException("Non-zero slot ordinal");
        }
        return this.getStack(fabric);
    }

    @Override
    public ItemStack getStack(Fabric fabric) {
        return checkNotNull(fabric, "Target inventory").fabric$getStack(this.base);
    }

    @Override
    public boolean setStack(Fabric fabric, int ordinal, ItemStack stack) {
        if (ordinal != 0) {
            throw new InvalidOrdinalException("Non-zero slot ordinal");
        }
        return this.setStack(fabric, stack);
    }

    @Override
    public boolean setStack(Fabric fabric, ItemStack stack) {
        checkNotNull(fabric, "Target inventory").fabric$setStack(this.base, stack);
        return true;
    }

    @Override
    public Lens getLens(int index) {
        return this;
    }

    @Override
    public Map<Property<?>, Object> getProperties(int index) {
        return Collections.emptyMap();
    }

    @Override
    public boolean has(Lens lens) {
        return false;
    }

    @Override
    public boolean isSubsetOf(Collection<Lens> c) {
        return false;
    }

    @Override
    public SlotLens getSlotLens(int ordinal) {
        if (ordinal != 0) {
            throw new InvalidOrdinalException("Non-zero slot ordinal");
        }
        return this;
    }

    @Override
    public List<SlotLens> getSlots() {
        return Collections.singletonList(this);
    }

    @Override
    public String toString(int deep) {
        return "[" + this.base + "]";
    }

    @Override
    public String toString() {
        return this.toString(0);
    }

}
