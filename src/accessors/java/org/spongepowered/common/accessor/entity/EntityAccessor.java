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

import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Random;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Accessor("DATA_SHARED_FLAGS_ID") static DataParameter<Byte> accessor$getDATA_SHARED_FLAGS_ID() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("DATA_AIR_SUPPLY_ID") static DataParameter<Integer> accessor$getDATA_AIR_SUPPLY_ID() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("DATA_CUSTOM_NAME") static DataParameter<String> accessor$getDATA_CUSTOM_NAME() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("DATA_CUSTOM_NAME_VISIBLE") static DataParameter<Boolean> accessor$getDATA_CUSTOM_NAME_VISIBLE() {
        throw new IllegalStateException("Untransformed accessor!");
    }

    @Accessor("DATA_SILENT") static DataParameter<Boolean> accessor$getDATA_SILENT() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("DATA_NO_GRAVITY") static DataParameter<Boolean> accessor$getDATA_NO_GRAVITY() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("random") Random accessor$getRandom();

    @Accessor("remainingFireTicks") int accessor$getRemainingFireTicks();

    @Accessor("remainingFireTicks") void accessor$setRemainingFireTicks(int remainingFireTicks);

    @Accessor("isInsidePortal") void accessor$setInsidePortal(boolean insidePortal);

    @Accessor("portalTime") void accessor$setPortalTime(int portalTime);

    @Accessor("portalEntrancePos") void accessor$setPortalEntrancePos(BlockPos portalEntrancePos);

    @Invoker("setSharedFlag") void accessor$setSharedFlag(int flag, boolean set);

    @Invoker("getFireImmuneTicks") int accessor$getFireImmuneTicks();

    @Invoker("getPermissionLevel") int accessor$getPermissionLevel();

    @Invoker("getEncodeId") String accessor$getEncodeId();

    @Invoker("removePassenger") void accessor$removePassenger(Entity entity);

    @Invoker("setRot") void accessor$setRot(float yaw, float pitch);
}
