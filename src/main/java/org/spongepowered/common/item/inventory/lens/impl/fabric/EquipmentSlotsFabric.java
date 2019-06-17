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
package org.spongepowered.common.item.inventory.lens.impl.fabric;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.impl.slots.SlotLensImpl;

import java.util.Collection;
import java.util.Collections;

@SuppressWarnings("unchecked")
public class EquipmentSlotsFabric implements Fabric {
    private static final EntityEquipmentSlot[] SLOTS;
    private static final int MAX_STACK_SIZE = 64;

    static {
        EntityEquipmentSlot[] values = EntityEquipmentSlot.values();
        SLOTS = new EntityEquipmentSlot[values.length];
        for (EntityEquipmentSlot slot : values) {
            SLOTS[slot.getSlotIndex()] = slot;
        }
    }

    private final Living living;

    public EquipmentSlotsFabric(Living living) {
        this.living = living;
    }

    @Override
    public Collection<Living> allInventories() {
        return Collections.singleton(this.living);
    }

    @Override
    public Living get(int index) {
        return this.living;
    }

    @Override
    public ItemStack getStack(int index) {
        return ((EntityLivingBase) this.living).getItemStackFromSlot(SLOTS[index]);
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        ((EntityLivingBase) this.living).setItemStackToSlot(SLOTS[index], stack);
    }

    @Override
    public int getMaxStackSize() {
        return MAX_STACK_SIZE;
    }

    @Override
    public Translation getDisplayName() {
        return SlotLensImpl.SLOT_NAME;
    }

    @Override
    public int getSize() {
        return SLOTS.length;
    }

    @Override
    public void clear() {
        EntityLivingBase entity = (EntityLivingBase) this.living;
        for (EntityEquipmentSlot slot : SLOTS) {
            entity.setItemStackToSlot(slot, ItemStack.EMPTY);
        }
    }

    @Override
    public void markDirty() {
    }
}
