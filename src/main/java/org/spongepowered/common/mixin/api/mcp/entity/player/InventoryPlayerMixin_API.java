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
package org.spongepowered.common.mixin.api.mcp.entity.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.entity.PrimaryPlayerInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.adapter.impl.comp.EquipmentInventoryAdapter;
import org.spongepowered.common.inventory.adapter.impl.comp.PrimaryPlayerInventoryAdapter;
import org.spongepowered.common.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.minecraft.PlayerInventoryLens;

import java.util.Optional;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
@Mixin(net.minecraft.entity.player.PlayerInventory.class)
public abstract class InventoryPlayerMixin_API implements PlayerInventory {

    @Shadow public int currentItem;
    @Shadow public PlayerEntity player;

    @Shadow public abstract int getSizeInventory();
    @Shadow public abstract ItemStack getStackInSlot(int index);


    @Nullable private PrimaryPlayerInventoryAdapter api$primary;
    @Nullable private EquipmentInventoryAdapter api$equipment;
    @Nullable private EquipmentInventoryAdapter api$armor;
    @Nullable private SlotAdapter api$offhand;

    @SuppressWarnings("ConstantConditions")
    @Override
    public Optional<Player> getCarrier() {
        return Optional.ofNullable((Player) this.player);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public PrimaryPlayerInventory getPrimary() {
        if (this.api$primary == null && ((InventoryAdapter) this).bridge$getRootLens() instanceof PlayerInventoryLens) {
            final Lens lens = ((InventoryAdapter) this).bridge$getRootLens();
            final Fabric fabric = ((InventoryAdapter) this).bridge$getFabric();
            this.api$primary = (PrimaryPlayerInventoryAdapter) ((PlayerInventoryLens) lens).getPrimaryInventoryLens().getAdapter(fabric, this);
        }
        return this.api$primary;
    }

    @Override
    public EquipmentInventory getArmor() {
        if (this.api$armor == null && ((InventoryAdapter) this).bridge$getRootLens() instanceof PlayerInventoryLens) {
            final Lens lens = ((InventoryAdapter) this).bridge$getRootLens();
            final Fabric fabric = ((InventoryAdapter) this).bridge$getFabric();
            this.api$armor = (EquipmentInventoryAdapter) ((PlayerInventoryLens) lens).getArmorLens().getAdapter(fabric, this);
        }
        return this.api$armor;
    }

    @Override
    public EquipmentInventory getEquipment() {
        if (this.api$equipment == null && ((InventoryAdapter) this).bridge$getRootLens() instanceof PlayerInventoryLens) {
            final Lens lens = ((InventoryAdapter) this).bridge$getRootLens();
            final Fabric fabric = ((InventoryAdapter) this).bridge$getFabric();
            this.api$equipment = (EquipmentInventoryAdapter) ((PlayerInventoryLens) lens).getEquipmentLens().getAdapter(fabric, this);
        }
        return this.api$equipment;
    }

    @Override
    public Slot getOffhand() {
        if (this.api$offhand == null && ((InventoryAdapter) this).bridge$getRootLens() instanceof PlayerInventoryLens) {
            final Lens lens = ((InventoryAdapter) this).bridge$getRootLens();
            final Fabric fabric = ((InventoryAdapter) this).bridge$getFabric();
            this.api$offhand = (SlotAdapter) ((PlayerInventoryLens) lens).getOffhandLens().getAdapter(fabric, this);
        }
        return this.api$offhand;
    }

}
