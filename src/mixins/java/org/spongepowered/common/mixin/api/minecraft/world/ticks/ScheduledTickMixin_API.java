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

import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;
import org.spongepowered.api.scheduler.ScheduledUpdate;
import org.spongepowered.api.scheduler.TaskPriority;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.ticks.TickNextTickDataBridge;

import java.time.Duration;

@Mixin(ScheduledTick.class)
public class ScheduledTickMixin_API<T> implements ScheduledUpdate<T> {
    @Shadow @Final private T type;
    @Shadow @Final private TickPriority priority;

    @Override
    public T target() {
        return this.type;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Duration delay() {
        return ((TickNextTickDataBridge<T>) this).bridge$getScheduledDelayWhenCreated();
    }

    @Override
    public TaskPriority priority() {
        return (TaskPriority) (Object) this.priority;
    }

    @SuppressWarnings("unchecked")
    @Override
    public State state() {
        return ((TickNextTickDataBridge<T>) this).bridge$internalState();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean cancel() {
        return ((TickNextTickDataBridge<T>) this).bridge$cancelForcibly();
    }

    @SuppressWarnings("unchecked")
    @Override
    public World<?, ?> world() {
        return ((TickNextTickDataBridge<T>) this).bridge$getLocation().world();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Location<?, ?> location() {
        return ((TickNextTickDataBridge<T>) this).bridge$getLocation();
    }
}
