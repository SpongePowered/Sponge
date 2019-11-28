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
package org.spongepowered.common.mixin.api.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.mixin.api.mcp.entity.EntityLivingBaseMixin_API;

import java.util.Optional;

import javax.annotation.Nullable;

// All living implementors of ArmorEquipable
@Mixin({EntityArmorStand.class, EntityGiantZombie.class, EntityPlayerMP.class, AbstractSkeleton.class, EntityZombie.class, EntityHuman.class})
public abstract class ArmorEquippableMixin_API extends EntityLivingBaseMixin_API implements ArmorEquipable {

    @Override
    public Optional<ItemStack> getItemInHand(HandType handType) {
        checkNotNull(handType, "HandType cannot be null!");
        final net.minecraft.item.ItemStack nmsItem = this.getHeldItem((EnumHand) (Object) handType);
        return Optional.of(ItemStackUtil.fromNative(nmsItem));
    }

    @Override
    public void setItemInHand(HandType handType, @Nullable ItemStack itemInHand) {
        checkNotNull(handType, "HandType cannot be null!");
        this.setHeldItem((EnumHand) (Object) handType, ItemStackUtil.toNative(itemInHand).func_77946_l());
    }
}
