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

import net.minecraft.block.PortalInfo;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.Direction;
import net.minecraft.util.TeleportationRepositioner;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.common.UntransformedAccessorError;

import java.util.Random;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Accessor("DATA_SHARED_FLAGS_ID")
    static DataParameter<Byte> accessor$DATA_SHARED_FLAGS_ID() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DATA_AIR_SUPPLY_ID")
    static DataParameter<Integer> accessor$DATA_AIR_SUPPLY_ID() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DATA_CUSTOM_NAME")
    static DataParameter<String> accessor$DATA_CUSTOM_NAME() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DATA_CUSTOM_NAME_VISIBLE")
    static DataParameter<Boolean> accessor$DATA_CUSTOM_NAME_VISIBLE() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DATA_SILENT")
    static DataParameter<Boolean> accessor$DATA_SILENT() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DATA_NO_GRAVITY")
    static DataParameter<Boolean> accessor$DATA_NO_GRAVITY() {
        throw new UntransformedAccessorError();
    }

    @Accessor("remainingFireTicks") int accessor$remainingFireTicks();

    @Accessor("remainingFireTicks") void accessor$remainingFireTicks(final int remainingFireTicks);

    @Accessor("random") Random accessor$random();

    @Accessor("isInsidePortal") void accessor$isInsidePortal(final boolean isInsidePortal);

    @Accessor("portalTime") int accessor$portalTime();

    @Accessor("portalTime") void accessor$portalTime(final int portalTime);

    @Accessor("portalEntrancePos") BlockPos accessor$portalEntrancePos();

    @Accessor("portalEntrancePos") void accessor$portalEntrancePos(final BlockPos portalEntrancePos);

    @Invoker("setRot") void invoker$setRot(final float yRot, final float xRot);

    @Invoker("getEncodeId") String invoker$getEncodeId();

    @Invoker("removePassenger") void invoker$removePassenger(final Entity passenger);

    @Invoker("setSharedFlag") void invoker$setSharedFlag(final int flag, final boolean value);

    @Invoker("getFireImmuneTicks") int invoker$getFireImmuneTicks();

    @Invoker("getPermissionLevel") int invoker$getPermissionLevel();

    @Invoker("removeAfterChangingDimensions") void invoker$removeAfterChangingDimensions();

    @Invoker("getRelativePortalPosition") Vector3d invoker$getRelativePortalPosition(Direction.Axis axis, TeleportationRepositioner.Result result);

    @Invoker("findDimensionEntryPoint") PortalInfo invoker$findDimensionEntryPoint(ServerWorld targetWorld);

}
