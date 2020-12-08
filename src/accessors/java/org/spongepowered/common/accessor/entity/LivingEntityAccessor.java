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
package org.spongepowered.common.accessor.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Accessor("DATA_LIVING_ENTITY_FLAGS") static DataParameter<Byte> accessor$getDATA_LIVING_ENTITY_FLAGS() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("DATA_HEALTH_ID") static DataParameter<Float> accessor$getDATA_HEALTH_ID() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("DATA_EFFECT_COLOR_ID") static DataParameter<Integer> accessor$getDATA_EFFECT_COLOR_ID() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("DATA_EFFECT_AMBIENCE_ID") static DataParameter<Boolean> accessor$getDATA_EFFECT_AMBIENCE_ID() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("DATA_ARROW_COUNT_ID") static DataParameter<Integer> accessor$getDATA_ARROW_COUNT_ID() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("DATA_STINGER_COUNT_ID") static DataParameter<Integer> accessor$getDATA_STINGER_COUNT_ID() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("SLEEPING_POS_ID") static DataParameter<Optional<BlockPos>> accessor$getSLEEPING_POS_ID() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("lastHurt") float accessor$getLastHurt();

    @Accessor("lastHurt") void accessor$setLastHurt(float lastHurt);

    @Accessor("dead") boolean accessor$getDead();

    @Accessor("lastHurtByMob") @Nullable LivingEntity accessor$getLastHurtByMob();

    @Accessor("useItem") void accessor$setUseItem(ItemStack useItem);

    @Invoker("isDamageSourceBlocked")  boolean accessor$getIsDamageSourceBlocked(DamageSource isDamageSourceBlocked);

    @Invoker("getExperienceReward") int accessor$getExperienceReward(PlayerEntity player);
}
