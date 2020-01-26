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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.EquipmentInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.MainPlayerInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.PlayerInventoryLens;

import java.util.Optional;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
@Mixin(InventoryPlayer.class)
public abstract class InventoryPlayerMixin_API implements PlayerInventory {

    @Shadow public int currentItem;
    @Shadow public EntityPlayer player;

    @Shadow public abstract int getSizeInventory();
    @Shadow public abstract ItemStack getStackInSlot(int index);


    @Nullable private MainPlayerInventoryAdapter api$main;
    @Nullable private EquipmentInventoryAdapter api$equipment;
    @Nullable private SlotAdapter api$offhand;

    @SuppressWarnings("ConstantConditions")
    @Override
    public Optional<Player> getCarrier() {
        return Optional.ofNullable((Player) this.player);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public MainPlayerInventory getMain() {
        if (this.api$main == null && ((InventoryAdapter) this).bridge$getRootLens() instanceof PlayerInventoryLens) {
            final Lens lens = ((InventoryAdapter) this).bridge$getRootLens();
            final Fabric fabric = ((InventoryAdapter) this).bridge$getFabric();
            this.api$main = (MainPlayerInventoryAdapter) ((PlayerInventoryLens) lens).getMainLens().getAdapter(fabric, this);
        }
        return this.api$main;
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
