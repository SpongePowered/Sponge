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
package org.spongepowered.common.mixin.core.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ITickList;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.TickPriority;
import org.spongepowered.api.scheduler.ScheduledUpdate;
import org.spongepowered.api.scheduler.ScheduledUpdateList;
import org.spongepowered.api.scheduler.TaskPriority;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;

@Mixin(ITickList.class)
public interface MixinITickList<T> extends ScheduledUpdateList<T> {
    @Shadow void scheduleTick(BlockPos pos, T itemIn, int scheduledTime);
    @Shadow void scheduleTick(BlockPos pos, T itemIn, int scheduledTime, TickPriority priority);
    @Shadow boolean isTickPending(BlockPos pos, T obj);
    @Shadow boolean isTickScheduled(BlockPos pos, T itemIn);

    @SuppressWarnings("unchecked")
    @Override
    default ScheduledUpdate<T> schedule(int x, int y, int z, T target, Duration delay, TaskPriority priority) {
        final BlockPos targetPos = new BlockPos(x, y, z);
        // TODO - this is really over complicated to schedule new ticks...
        scheduleTick(targetPos, target, (int) delay.toNanos() / 50, (TickPriority) (Object) priority);

        return (ScheduledUpdate<T>) new NextTickListEntry<>(targetPos, target);
    }

    @Override
    default boolean isScheduled(int x, int y, int z, T target) {
        return isTickScheduled(new BlockPos( x,
            y, z), target);
    }

    @Override
    default Collection<ScheduledUpdate<T>> getScheduledAt(int x, int y, int z) {
        return Collections.emptyList(); // TODO - Split up ScheduledUpdateList to have subclasses maybe?
    }
}
