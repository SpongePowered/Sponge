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

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.ticks.LevelChunkTicks;
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
import org.spongepowered.common.bridge.world.ticks.LevelTicksBridge;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.LongPredicate;

@Mixin(LevelTicks.class)
public abstract class LevelTicksMixin_API<T> implements ScheduledUpdateList<T> {

    // @formatter:off
    @Shadow @Final private LongPredicate tickCheck;
    @Shadow @Final private Long2ObjectMap<LevelChunkTicks<T>> allContainers;
    @Shadow @Final private List<ScheduledTick<T>> alreadyRunThisTick;

    @Shadow public abstract boolean shadow$hasScheduledTick(BlockPos param0, T param1);
    @Shadow public abstract void shadow$schedule(ScheduledTick<T> data);
    // @formatter:on


    @Override
    public ScheduledUpdate<T> schedule(
        final int x, final int y, final int z, final T target, final Duration delay, final TaskPriority priority
    ) {
        return this.schedule(
            x, y, z, target, Ticks.ofWallClockTime(Sponge.server(), delay.toMillis(), ChronoUnit.MILLIS));
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    public ScheduledUpdate<T> schedule(
        final int x, final int y, final int z, final T target, final Ticks tickDelay, final TaskPriority priority
    ) {
        Objects.requireNonNull(tickDelay);
        if (tickDelay.isInfinite()) {
            throw new IllegalArgumentException("Delay cannot be infinite!");
        }
        final var blockPos = new BlockPos(x, y, z);
        final var level = ((LevelTicksBridge<T>) this).bridge$level();
        final var gameTime = level.getLevelData().getGameTime();
        final var subCount = level.nextSubTickCount();
        final var scheduledUpdate = new ScheduledTick<>(
            target, blockPos, tickDelay.ticks() + gameTime, (TickPriority) (Object) priority, subCount);
        if (this.tickCheck.test(ChunkPos.asLong(blockPos))) {
            this.shadow$schedule(scheduledUpdate);
        }
        return (ScheduledUpdate<T>) (Object) scheduledUpdate;
    }

    @Override
    public boolean isScheduled(final int x, final int y, final int z, final T target) {
        return this.shadow$hasScheduledTick(new BlockPos(x, y, z), target);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<? extends ScheduledUpdate<T>> scheduledAt(final int x, final int y, final int z) {
        if (!this.alreadyRunThisTick.isEmpty()) {
            return Collections.emptySet();
        }
        final long longPos = ChunkPos.asLong(new BlockPos(x, y, z));
        final LevelChunkTicks<T> $$2 = this.allContainers.get(longPos);

        return $$2.getAll()
            .filter(data -> data.pos().getX() == x && data.pos().getZ() == z && data.pos().getY() == y)
            .map(data -> (ScheduledUpdate<T>) (Object) data)
            .toList();
    }
}
