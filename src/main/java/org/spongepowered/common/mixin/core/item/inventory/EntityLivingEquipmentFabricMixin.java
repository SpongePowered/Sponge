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
package org.spongepowered.common.mixin.core.item.inventory;

import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.item.inventory.InventoryBridge;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.impl.slots.SlotLensImpl;

import java.util.Collection;
import java.util.Collections;

@Mixin(LivingEntity.class)
public abstract class EntityLivingEquipmentFabricMixin implements Fabric, InventoryBridge {

    @Shadow public abstract ItemStack getItemStackFromSlot(EquipmentSlotType slotIn);

    @Shadow public abstract void setItemStackToSlot(EquipmentSlotType slotIn, ItemStack stack);

    private static final EquipmentSlotType[] SLOTS;
    private static final int MAX_STACK_SIZE = 64;

    static {
        EquipmentSlotType[] values = EquipmentSlotType.values();
        SLOTS = new EquipmentSlotType[values.length];
        for (EquipmentSlotType slot : values) {
            SLOTS[slot.func_188452_c()] = slot;
        }
    }

    @Override
    public Collection<InventoryBridge> fabric$allInventories() {
        return Collections.singleton(this);
    }

    @Override
    public InventoryBridge fabric$get(int index) {
        return this;
    }

    @Override
    public ItemStack fabric$getStack(int index) {
        return this.getItemStackFromSlot(SLOTS[index]);
    }

    @Override
    public void fabric$setStack(int index, ItemStack stack) {
        this.setItemStackToSlot(SLOTS[index], stack);
    }

    @Override
    public int fabric$getMaxStackSize() {
        return MAX_STACK_SIZE;
    }

    @Override
    public Translation fabric$getDisplayName() {
        return SlotLensImpl.SLOT_NAME;
    }

    @Override
    public int fabric$getSize() {
        return SLOTS.length;
    }

    @Override
    public void fabric$clear() {
        for (EquipmentSlotType slot : SLOTS) {
            this.setItemStackToSlot(slot, ItemStack.field_190927_a);
        }
    }

    @Override
    public void fabric$markDirty() {
    }
}
