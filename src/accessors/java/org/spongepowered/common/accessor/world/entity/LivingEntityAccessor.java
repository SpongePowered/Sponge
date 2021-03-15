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
package org.spongepowered.common.accessor.world.entity;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.common.UntransformedAccessorError;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Accessor("DATA_LIVING_ENTITY_FLAGS")
    static EntityDataAccessor<Byte> accessor$DATA_LIVING_ENTITY_FLAGS() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DATA_HEALTH_ID")
    static EntityDataAccessor<Float> accessor$DATA_HEALTH_ID() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DATA_EFFECT_COLOR_ID")
    static EntityDataAccessor<Integer> accessor$DATA_EFFECT_COLOR_ID() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DATA_EFFECT_AMBIENCE_ID")
    static EntityDataAccessor<Boolean> accessor$DATA_EFFECT_AMBIENCE_ID() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DATA_ARROW_COUNT_ID")
    static EntityDataAccessor<Integer> accessor$DATA_ARROW_COUNT_ID() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DATA_STINGER_COUNT_ID")
    static EntityDataAccessor<Integer> accessor$DATA_STINGER_COUNT_ID() {
        throw new UntransformedAccessorError();
    }

    @Accessor("SLEEPING_POS_ID")
    static EntityDataAccessor<Optional<BlockPos>> accessor$SLEEPING_POS_ID() {
        throw new UntransformedAccessorError();
    }

    @Accessor("dead") boolean accessor$dead();

    @Accessor("lastHurt") float accessor$lastHurt();

    @Accessor("lastHurt") void accessor$lastHurt(final float lastHurt);

    @Accessor("lastHurtByMob") @Nullable LivingEntity accessor$lastHurtByMob();
    
    @Accessor("autoSpinAttackTicks") int accessor$autoSpinAttackTicks();

    @Accessor("useItem") void accessor$useItem(final ItemStack useItem);

    @Invoker("getExperienceReward") int invoker$getExperienceReward(final Player player);

    @Invoker("isDamageSourceBlocked") boolean invoker$isDamageSourceBlocked(final DamageSource source);

}
