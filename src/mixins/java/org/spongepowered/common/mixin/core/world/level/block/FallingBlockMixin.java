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
package org.spongepowered.common.mixin.core.world.level.block;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.math.vector.Vector3d;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(FallingBlock.class)
public abstract class FallingBlockMixin {

    @Redirect(
        method = "tick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;getY()I")
    )
    public int impl$checkFallable(final BlockPos pos, final BlockState state,
        final ServerLevel world, final BlockPos samePos,
        final Random random,
        final CallbackInfo ci
    ) {
        if (pos.getY() < 0) {
            return pos.getY();
        }
        if (!ShouldFire.CONSTRUCT_ENTITY_EVENT_PRE) {
            return pos.getY();
        }
        final EntityType<org.spongepowered.api.entity.FallingBlock> fallingBlock = EntityTypes.FALLING_BLOCK.get();
        final org.spongepowered.api.world.server.ServerWorld spongeWorld = (org.spongepowered.api.world.server.ServerWorld) world;
        final BlockSnapshot snapshot = spongeWorld.createSnapshot(pos.getX(), pos.getY(), pos.getZ());
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(snapshot);
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.FALLING_BLOCK);
            final ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(frame.currentCause(),
                ServerLocation.of(
                    (org.spongepowered.api.world.server.ServerWorld) world, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D),
                new Vector3d(0, 0, 0),
                fallingBlock
            );
            if (SpongeCommon.post(event)) {
                return -1;
            }
            return pos.getY();
        }
    }

}
