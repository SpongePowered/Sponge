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
package org.spongepowered.common.mixin.inventory.api;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.PrimaryPlayerInventory;
import org.spongepowered.api.item.inventory.entity.UserInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.entity.player.SpongeUserInventory;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.adapter.impl.comp.EquipmentInventoryAdapter;
import org.spongepowered.common.inventory.adapter.impl.comp.PrimaryPlayerInventoryAdapter;
import org.spongepowered.common.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.inventory.lens.impl.minecraft.PlayerInventoryLens;

import java.util.Optional;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
@Mixin(SpongeUserInventory.class)
public abstract class SpongeUserInventoryMixin_API implements UserInventory<User>, InventoryAdapter {

    @Nullable private User impl$carrier;
    @Nullable private PrimaryPlayerInventoryAdapter impl$grid;
    @Nullable private EquipmentInventoryAdapter impl$armor;
    @Nullable private EquipmentInventoryAdapter impl$equipment;
    @Nullable private SlotAdapter impl$offhand;

    @SuppressWarnings("unchecked")
    @Inject(method = "<init>*", at = @At("RETURN"), remap = false)
    private void onConstructed(final SpongeUser playerIn, final CallbackInfo ci) {
        this.impl$carrier = ((User) playerIn);
    }

    @Override
    public Optional<User> getCarrier() {
        return Optional.ofNullable(this.impl$carrier);
    }

    @Override
    public PrimaryPlayerInventory getPrimary() {
        if (this.impl$grid == null) {
            this.impl$grid = (PrimaryPlayerInventoryAdapter) ((PlayerInventoryLens) this.inventoryAdapter$getRootLens()).getPrimaryInventoryLens().getAdapter(this.inventoryAdapter$getFabric(), this);
        }
        return this.impl$grid;
    }

    @Override
    public EquipmentInventory getArmor() {
        if (this.impl$armor == null) {
            this.impl$armor = (EquipmentInventoryAdapter) ((PlayerInventoryLens) this.inventoryAdapter$getRootLens()).getArmorLens().getAdapter(this.inventoryAdapter$getFabric(), this);
        }
        return this.impl$armor;
    }

    @Override
    public EquipmentInventory getEquipment() {
        if (this.impl$equipment == null) {
            this.impl$equipment = (EquipmentInventoryAdapter) ((PlayerInventoryLens) this.inventoryAdapter$getRootLens()).getEquipmentLens().getAdapter(this.inventoryAdapter$getFabric(), this);
        }
        return this.impl$equipment;
    }

    @Override
    public Slot getOffhand() {
        if (this.impl$offhand == null) {
            this.impl$offhand = (SlotAdapter) ((PlayerInventoryLens) this.inventoryAdapter$getRootLens()).getOffhandLens().getAdapter(this.inventoryAdapter$getFabric(), this);
        }
        return this.impl$offhand;
    }

}
