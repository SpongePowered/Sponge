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
import net.minecraft.world.level.Level;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.api.scheduler.ScheduledUpdate;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.ticks.LevelTicksBridge;
import org.spongepowered.common.bridge.world.ticks.TickNextTickDataBridge;
import org.spongepowered.common.util.Preconditions;

import java.time.Duration;

@Mixin(ScheduledTick.class)
public abstract class ScheduledTickMixin<T> implements TickNextTickDataBridge<T> {

    @Shadow @Final private BlockPos pos;
    @Shadow @Final private long triggerTick;

    @MonotonicNonNull private ServerLocation impl$location;
    @MonotonicNonNull private LevelTicks<T> impl$parentTickList;
    private long impl$scheduledTime;
    private ScheduledUpdate.State impl$state = ScheduledUpdate.State.WAITING;

    @SuppressWarnings("unchecked")
    @Override
    public void bridge$createdByList(final LevelTicks<T> tickList) {
        this.impl$parentTickList = tickList;
        this.impl$scheduledTime = ((LevelTicksBridge<T>) tickList).bridge$getGameTime().getAsLong();
    }

    @Override
    public void bridge$setWorld(final Level world) {
        Preconditions.checkState(this.impl$location == null, "World already known");
        final BlockPos position = this.pos;
        this.impl$location = ServerLocation.of((ServerWorld) world, position.getX(), position.getY(), position.getZ());
    }

    @Override
    public ServerLocation bridge$getLocation() {
        Preconditions.checkState(this.impl$location != null, "Unable to determine location at this time");
        return this.impl$location;
    }

    @Override
    public ScheduledUpdate.State bridge$internalState() {
        if (this.impl$parentTickList == null) {
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
        if (this.impl$parentTickList == null) {
            return false;
        }
        if (this.impl$state == ScheduledUpdate.State.FINISHED) {
            return false;
        }
        this.impl$state = ScheduledUpdate.State.CANCELLED;
        return true;
    }

    @Override
    public Duration bridge$getScheduledDelayWhenCreated() {
        return Ticks.duration(this.triggerTick - this.impl$scheduledTime);
    }


}
