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
package org.spongepowered.common.mixin.core.world.ticks;


import net.kyori.adventure.util.Ticks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.SavedTick;
import net.minecraft.world.ticks.ScheduledTick;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.scheduler.ScheduledUpdate;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.accessor.world.ticks.LevelChunkTicksAccessor;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.data.SpongeDataHolderBridge;
import org.spongepowered.common.bridge.world.ticks.TickNextTickDataBridge;
import org.spongepowered.common.data.holder.SpongeMutableDataHolder;
import org.spongepowered.common.util.Preconditions;

import java.time.Duration;

@Mixin(ScheduledTick.class)
public abstract class ScheduledTickMixin<T> implements TickNextTickDataBridge<T>, SpongeMutableDataHolder, DataCompoundHolder, CreatorTrackedBridge {

    @Shadow @Final private BlockPos pos;
    @Shadow @Final private long triggerTick;

    @MonotonicNonNull private ServerLocation impl$location;
    @MonotonicNonNull private LevelChunkTicks<T> impl$parentLevelChunkTicks;
    private long impl$scheduledTime;
    private ScheduledUpdate.State impl$state = ScheduledUpdate.State.WAITING;

    private @Nullable CompoundTag impl$compound;

    @Override
    public void bridge$createdByList(final ServerLevel level, final LevelChunkTicks<T> levelChunkTicks) {
        this.impl$parentLevelChunkTicks = levelChunkTicks;
        this.impl$scheduledTime = level.getLevelData().getGameTime();
        this.impl$location = ServerLocation.of((ServerWorld) level, this.pos.getX(), this.pos.getY(), this.pos.getZ());
    }

    @Override
    public ServerLocation bridge$getLocation() {
        Preconditions.checkState(this.impl$location != null, "Unable to determine location at this time");
        return this.impl$location;
    }

    @Override
    public ScheduledUpdate.State bridge$internalState() {
        if (this.impl$parentLevelChunkTicks == null) {
            return ScheduledUpdate.State.CANCELLED;
        }
        return this.impl$state;
    }

    @Override
    public void bridge$setState(final ScheduledUpdate.State state) {
        this.impl$state = state;
    }

    @Override
    public boolean bridge$cancelForcibly() {
        if (this.impl$parentLevelChunkTicks == null) {
            return false;
        }
        if (this.impl$state == ScheduledUpdate.State.FINISHED) {
            return false;
        }
        // While we don't try to clean up more thoroughly, the ticks per position has side effects.
        ((LevelChunkTicksAccessor) this.impl$parentLevelChunkTicks).accessor$ticksPerPosition().remove(this);
        this.impl$state = ScheduledUpdate.State.CANCELLED;
        return true;
    }

    @Override
    public Duration bridge$getScheduledDelayWhenCreated() {
        return Ticks.duration(this.triggerTick - this.impl$scheduledTime);
    }

    @Override
    public CompoundTag data$getCompound() {
        return this.impl$compound;
    }

    @Override
    public void data$setCompound(final CompoundTag nbt) {
        this.impl$compound = nbt;
    }

    @Inject(method = "toSavedTick", at = @At(value = "RETURN"))
    private void impl$onToSaveSkipCancelled(final long $$0, final CallbackInfoReturnable<SavedTick<T>> cir) {
        ((SpongeDataHolderBridge) (Object) cir.getReturnValue()).bridge$mergeDeserialized(((SpongeDataHolderBridge) this).bridge$getManipulator());
    }
}
