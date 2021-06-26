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
package org.spongepowered.common.mixin.core.world.entity;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.action.LightningEvent;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

import java.util.List;

@Mixin(LightningBolt.class)
public abstract class LightningBoltMixin extends EntityMixin {

    private final List<Entity> impl$struckEntities = Lists.newArrayList();
    private boolean impl$effect = false; // TODO never set?

    @Redirect(method = "spawnFire",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean impl$throwEventForChangingBlocks(final net.minecraft.world.level.Level world, final BlockPos pos, final BlockState blockState) {
        return this.impl$strikeBlockAndAddSnapshot(world, pos, blockState);
    }

    @Redirect(method = "spawnFire(I)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean impl$throwEventForChangingBlockDuringUpdate(final net.minecraft.world.level.Level world, final BlockPos pos, final BlockState blockState) {
        return this.impl$strikeBlockAndAddSnapshot(world, pos, blockState);
    }

    private boolean impl$strikeBlockAndAddSnapshot(final net.minecraft.world.level.Level world, final BlockPos pos, final BlockState blockState) {
        if (!this.impl$effect && ((World) world).containsBlock(pos.getX(), pos.getY(), pos.getZ())) {
            final Vector3i pos3i = VecHelper.toVector3i(pos);
            final Transaction<BlockSnapshot> transaction = new Transaction<>(
                SpongeBlockSnapshotBuilder.pooled()
                    .blockState(world.getBlockState(pos))
                    .world((ServerLevel) world)
                    .position(pos3i)
                    .build(),
                SpongeBlockSnapshotBuilder.pooled()
                    .blockState(blockState)
                    .world((ServerLevel) world)
                    .position(pos3i)
                    .build());
            return true;
        }
        return false;
    }

    @Redirect(method = "tick()V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;thunderHit(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LightningBolt;)V"))
    private void impl$AddEntityToListForEvent(final net.minecraft.world.entity.Entity mcEntity, final ServerLevel level, final LightningBolt lightningBolt) {
        if (!this.impl$effect) {
            final Entity entity = (Entity) mcEntity;
            if (!this.impl$struckEntities.contains(entity)) {
                this.impl$struckEntities.add(entity);
            }
        }
    }

    @Inject(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LightningBolt;remove()V"))
    private void impl$ThrowEventAndProcess(final CallbackInfo ci) {
        if (this.removed || this.level.isClientSide) {
            return;
        }
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final LightningEvent.Strike strike = SpongeEventFactory.createLightningEventStrike(frame.currentCause(), this.impl$struckEntities);
            Sponge.eventManager().post(strike);

            if (!strike.isCancelled()) {
                for (final Entity e : strike.entities()) {
                    ((net.minecraft.world.entity.Entity) e).thunderHit((ServerLevel) this.level, (LightningBolt) (Object) this);
                }
                SpongeCommon.post(SpongeEventFactory.createLightningEventPost(frame.currentCause()));
            }
        }
    }


}
