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

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;

@Mixin(LivingEntity.class)
public interface EntityLivingBaseAccessor {

    @Accessor("HAND_STATES")
    static DataParameter<Byte> accessor$getHandStatesParameter() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("HEALTH")
    static DataParameter<Float> accessor$getHealthParameter() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("POTION_EFFECTS")
    static DataParameter<Integer> accessor$getPotionEffectsParameter() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("HIDE_PARTICLES")
    static DataParameter<Boolean> accessor$getHideParticlesParameter() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("ARROW_COUNT_IN_ENTITY")
    static DataParameter<Integer> accessor$getArrowCountInEntityParameter() {
        throw new IllegalStateException("Untransformed Accessor!");
    }


    @Accessor("lastDamage") float accessor$getLastDamage();

    @Accessor("lastDamage") void accessor$setLastDamage(float lastDamage);

    @Accessor("recentlyHit") int accessor$getRecentlyHitValue();

    @Accessor("dead") boolean accessor$isLivingDead();

    @Accessor("revengeTarget") @Nullable LivingEntity accessor$getRevengeTarget();

    @Invoker("canBlockDamageSource")  boolean accessor$canBlockDamageSource(DamageSource damageSourceIn);

    @Accessor("armorArray") NonNullList<ItemStack> accessor$getArmorArray();
    
    @Accessor("activeItemStack") void accessor$setActiveItemStack(ItemStack stack);
}
