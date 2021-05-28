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
package org.spongepowered.common.mixin.inventory.impl.world.entity;

import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.inventory.InventoryBridge;
import org.spongepowered.common.bridge.world.inventory.LensGeneratorBridge;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.LensRegistrar;
import org.spongepowered.common.inventory.lens.impl.comp.EquipmentInventoryLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.inventory.lens.slots.SlotLens;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin_EquipmentFabric_Inventory implements Fabric, InventoryBridge, LensGeneratorBridge {

    @Shadow public abstract ItemStack shadow$getItemBySlot(EquipmentSlot slotIn);
    @Shadow public abstract void shadow$setItemSlot(EquipmentSlot slotIn, ItemStack stack);

    private static final int MAX_STACK_SIZE = 64;

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
        return this.shadow$getItemBySlot(EquipmentSlot.values()[index]);
    }

    @Override
    public void fabric$setStack(int index, ItemStack stack) {
        this.shadow$setItemSlot(EquipmentSlot.values()[index], stack);
    }

    @Override
    public int fabric$getMaxStackSize() {
        return LivingEntityMixin_EquipmentFabric_Inventory.MAX_STACK_SIZE;
    }

    @Override
    public int fabric$getSize() {
        return EquipmentSlot.values().length;
    }

    @Override
    public void fabric$clear() {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            this.shadow$setItemSlot(slot, ItemStack.EMPTY);
        }
    }

    @Override
    public void fabric$markDirty() {
    }

    @Override
    public SlotLensProvider lensGeneratorBridge$generateSlotLensProvider() {
        return new LensRegistrar.BasicSlotLensProvider(this.fabric$getSize());
    }

    @Override
    public Lens lensGeneratorBridge$generateLens(SlotLensProvider slotLensProvider) {
        Map<EquipmentType, SlotLens> equipmentLenses = new LinkedHashMap<>();
        for (int i = 0, slotsLength = EquipmentSlot.values().length; i < slotsLength; i++) {
            EquipmentSlot slot = EquipmentSlot.values()[i];
            equipmentLenses.put((EquipmentType) (Object) slot, slotLensProvider.getSlotLens(i));
        }
        return new EquipmentInventoryLens(equipmentLenses);
    }

}
