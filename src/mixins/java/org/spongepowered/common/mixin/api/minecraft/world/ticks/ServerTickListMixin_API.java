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
package org.spongepowered.common.mixin.api.minecraft.world.ticks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.ScheduledUpdate;
import org.spongepowered.api.scheduler.ScheduledUpdateList;
import org.spongepowered.api.scheduler.TaskPriority;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.ticks.TickNextTickDataBridge;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mixin(LevelTicks.class)
public abstract class ServerTickListMixin_API<T> implements ScheduledUpdateList<T> {

    @Shadow @Final private Predicate<T> tickCheck;
    @Shadow @Final private Queue<ScheduledTick<T>> toRunThisTick;
    @Shadow @Final private Set<ScheduledTick<T>> toRunThisTickSet;

    @Shadow public abstract boolean shadow$hasScheduledTick(BlockPos param0, T param1);
    @Shadow protected abstract void shadow$addTickData(ScheduledTick<T> data);

    @Override
    public ScheduledUpdate<T> schedule(
            final int x, final int y, final int z, final T target, final Duration delay, final TaskPriority priority
    ) {
        return this.schedule(x, y, z, target, Ticks.ofWallClockTime(Sponge.server(), delay.toMillis(), ChronoUnit.MILLIS));
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    public ScheduledUpdate<T> schedule(
            final int x, final int y, final int z, final T target, final Ticks tickDelay, final TaskPriority priority
    ) {
        final ScheduledTick<T> scheduledUpdate = new ScheduledTick<>(new BlockPos(x, y, z), target, tickDelay.ticks() + this.level.getGameTime(), (TickPriority) (Object) priority);
        if (!this.tickCheck.test(target)) {
            ((TickNextTickDataBridge<T>) scheduledUpdate).bridge$createdByList((LevelTicks<T>) (Object) this);
            this.shadow$addTickData(scheduledUpdate);
        }
        return (ScheduledUpdate<T>) scheduledUpdate;
    }

    @Override
    public boolean isScheduled(final int x, final int y, final int z, final T target) {
        return this.shadow$hasScheduledTick(new BlockPos(x, y, z), target);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<? extends ScheduledUpdate<T>> scheduledAt(final int x, final int y, final int z) {
        if (!this.currentlyTicking.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableCollection(this.tickNextTickSet.stream()
            .filter(data -> data.pos().getX() == x && data.pos().getZ() == z && data.pos().getY() == y)
            .map(data -> (ScheduledUpdate<T>) data)
            .collect(Collectors.toList()));
    }
}
