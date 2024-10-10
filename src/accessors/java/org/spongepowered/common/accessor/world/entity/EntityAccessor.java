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

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.common.UntransformedAccessorError;

import java.util.Optional;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Accessor("DATA_SHARED_FLAGS_ID")
    static EntityDataAccessor<Byte> accessor$DATA_SHARED_FLAGS_ID() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DATA_AIR_SUPPLY_ID")
    static EntityDataAccessor<Integer> accessor$DATA_AIR_SUPPLY_ID() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DATA_CUSTOM_NAME")
    static EntityDataAccessor<Optional<Component>> accessor$DATA_CUSTOM_NAME() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DATA_CUSTOM_NAME_VISIBLE")
    static EntityDataAccessor<Boolean> accessor$DATA_CUSTOM_NAME_VISIBLE() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DATA_SILENT")
    static EntityDataAccessor<Boolean> accessor$DATA_SILENT() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DATA_NO_GRAVITY")
    static EntityDataAccessor<Boolean> accessor$DATA_NO_GRAVITY() {
        throw new UntransformedAccessorError();
    }

    @Accessor("remainingFireTicks") int accessor$remainingFireTicks();

    @Accessor("remainingFireTicks") void accessor$remainingFireTicks(final int remainingFireTicks);

    @Accessor("random") RandomSource accessor$random();


    @Accessor("levelCallback") EntityInLevelCallback accessor$levelCallback();

    @Invoker("getEncodeId") @Nullable String invoker$getEncodeId();

    @Invoker("removePassenger") void invoker$removePassenger(final Entity passenger);

    @Invoker("setSharedFlag") void invoker$setSharedFlag(final int flag, final boolean value);

    @Invoker("getFireImmuneTicks") int invoker$getFireImmuneTicks();

    @Invoker("getPermissionLevel") int invoker$getPermissionLevel();

    @Invoker("unsetRemoved") void invoker$unsetRemoved();

}
