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

import com.google.common.base.Preconditions;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.item.inventory.ArmorEquipable;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.world.inventory.InventoryBridge;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.comp.EquipmentInventoryLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.PlayerInventoryLens;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

// All living implementors of ArmorEquipable
@Mixin({ArmorStand.class, Mob.class, Player.class})
public abstract class TraitMixin_ArmorEquipable_Inventory_API implements ArmorEquipable {

    // TODO can we implement canEquip?
    // We might want to allow plugins to set any item
    // but we should least expose checks if an item can be equipped normally

    @Override
    public boolean canEquip(final EquipmentType type) {
        return true;
    }

    @Override
    public boolean canEquip(final EquipmentType type, @Nullable final ItemStack equipment) {
        return true;
    }

    @Override
    public Optional<ItemStack> equipped(final EquipmentType type) {
        final InventoryAdapter inv = ((InventoryBridge) this).bridge$getAdapter();
        final EquipmentInventoryLens lens = this.impl$equipmentInventory(inv);
        final Fabric fabric = inv.inventoryAdapter$getFabric();
        return Optional.of(ItemStackUtil.fromNative(lens.getSlotLens(type).getStack(fabric)));
    }

    @Override
    public boolean equip(final EquipmentType type, @Nullable final ItemStack equipment) {
        final InventoryAdapter inv = ((InventoryBridge) this).bridge$getAdapter();
        final EquipmentInventoryLens lens = this.impl$equipmentInventory(inv);
        final Fabric fabric = inv.inventoryAdapter$getFabric();
        return lens.getSlotLens(type).setStack(fabric, ItemStackUtil.toNative(equipment));
    }

    @Override
    public ItemStack itemInHand(HandType handType) {
        Preconditions.checkNotNull(handType);
        final net.minecraft.world.item.ItemStack nmsItem = ((LivingEntity) (Object)this).getItemInHand((InteractionHand) (Object) handType);
        return ItemStackUtil.fromNative(nmsItem);
    }

    @Override
    public void setItemInHand(HandType handType, @Nullable ItemStack itemInHand) {
        Preconditions.checkNotNull(handType);
        ((LivingEntity) (Object)this).setItemInHand((InteractionHand) (Object) handType, ItemStackUtil.toNative(itemInHand).copy());
    }

    @Override
    public ItemStack head() {
        return this.equipped(EquipmentTypes.HEAD).get();
    }

    @Override
    public void setHead(ItemStack head) {
        this.equip(EquipmentTypes.HEAD, head);
    }

    @Override
    public ItemStack chest() {
        return this.equipped(EquipmentTypes.CHEST).get();
    }

    @Override
    public void setChest(ItemStack chest) {
        this.equip(EquipmentTypes.CHEST, chest);
    }

    @Override
    public ItemStack legs() {
        return this.equipped(EquipmentTypes.LEGS).get();
    }

    @Override
    public void setLegs(ItemStack legs) {
        this.equip(EquipmentTypes.LEGS, legs);
    }

    @Override
    public ItemStack feet() {
        return this.equipped(EquipmentTypes.FEET).get();
    }

    @Override
    public void setFeet(ItemStack feet) {
        this.equip(EquipmentTypes.FEET, feet);
    }

    private EquipmentInventoryLens impl$equipmentInventory(final InventoryAdapter adapter) {
        final Lens rootLens = adapter.inventoryAdapter$getRootLens();
        if (rootLens instanceof EquipmentInventoryLens) {
            return (EquipmentInventoryLens) rootLens;
        } else if (rootLens instanceof PlayerInventoryLens) {
            return ((PlayerInventoryLens) rootLens).getEquipmentLens();
        } else {
            throw new IllegalStateException("Unexpected lens for Equipable Inventory " + rootLens.getClass().getName());
        }
    }
}
