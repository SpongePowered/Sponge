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
package org.spongepowered.common.mixin.core.entity;

import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.mixin.core.entity.living.MixinEntityLivingBase;

import java.util.Optional;

import javax.annotation.Nullable;

// All implementors of ArmorEquipable
@Mixin({EntityArmorStand.class, EntityGiantZombie.class, EntityPlayerMP.class, EntitySkeleton.class, EntityZombie.class, EntityHuman.class})
@Implements(@Interface(iface = ArmorEquipable.class, prefix = "equipable$"))
public abstract class MixinArmorEquipable extends MixinEntityLivingBase {

    private static final int SLOT_HAND = 0;
    private static final int SLOT_BOOTS = 1;
    private static final int SLOT_LEGGINGS = 2;
    private static final int SLOT_CHESTPLATE = 3;
    private static final int SLOT_HELMET = 4;

    public Optional<ItemStack> equipable$getHelmet() {
        @Nullable final net.minecraft.item.ItemStack itemStack = this.getEquipmentInSlot(SLOT_HELMET);
        return Optional.ofNullable(itemStack == null ? null : (ItemStack) itemStack.copy());
    }

    public void equipable$setHelmet(ItemStack helmet) {
        if (helmet == null) {
            this.setCurrentItemOrArmor(SLOT_HELMET, null);
        } else {
            this.setCurrentItemOrArmor(SLOT_HELMET, (net.minecraft.item.ItemStack) helmet.copy());
        }
    }

    public Optional<ItemStack> equipable$getChestplate() {
        @Nullable final net.minecraft.item.ItemStack itemStack = this.getEquipmentInSlot(SLOT_CHESTPLATE);
        return Optional.ofNullable(itemStack == null ? null : ((ItemStack) itemStack.copy()));
    }

    public void equipable$setChestplate(ItemStack chestplate) {
        if (chestplate == null) {
            this.setCurrentItemOrArmor(SLOT_CHESTPLATE, null);
        } else {
            this.setCurrentItemOrArmor(SLOT_CHESTPLATE, (net.minecraft.item.ItemStack) chestplate.copy());
        }
    }

    public Optional<ItemStack> equipable$getLeggings() {
        @Nullable final net.minecraft.item.ItemStack itemStack = this.getEquipmentInSlot(SLOT_LEGGINGS);
        return Optional.ofNullable(itemStack == null ? null : ((ItemStack) itemStack.copy()));
    }

    public void equipable$setLeggings(ItemStack leggings) {
        if (leggings == null) {
            this.setCurrentItemOrArmor(SLOT_LEGGINGS, null);
        } else {
            this.setCurrentItemOrArmor(SLOT_LEGGINGS, ((net.minecraft.item.ItemStack) leggings.copy()));
        }
    }

    public Optional<ItemStack> equipable$getBoots() {
        @Nullable final net.minecraft.item.ItemStack itemStack = this.getEquipmentInSlot(SLOT_BOOTS);
        return Optional.ofNullable(itemStack == null ? null : ((ItemStack) itemStack.copy()));
    }

    public void equipable$setBoots(ItemStack boots) {
        if (boots == null) {
            this.setCurrentItemOrArmor(SLOT_BOOTS, null);
        } else {
            this.setCurrentItemOrArmor(SLOT_BOOTS, ((net.minecraft.item.ItemStack) boots.copy()));
        }
    }

    public Optional<ItemStack> equipable$getItemInHand() {
        @Nullable final net.minecraft.item.ItemStack itemStack = this.getEquipmentInSlot(SLOT_HAND);
        return Optional.ofNullable(itemStack == null ? null : ((ItemStack) itemStack.copy()));
    }

    public void equipable$setItemInHand(ItemStack itemInHand) {
        if (itemInHand == null) {
            this.setCurrentItemOrArmor(SLOT_HAND, null);
        } else {
            this.setCurrentItemOrArmor(SLOT_HAND, ((net.minecraft.item.ItemStack) itemInHand.copy()));
        }
    }
}
