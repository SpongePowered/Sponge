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

import static com.google.common.base.Preconditions.checkState;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import org.spongepowered.api.scheduler.ScheduledUpdate;
import org.spongepowered.api.scheduler.TaskPriority;
import org.spongepowered.api.util.TemporalUnits;
import org.spongepowered.api.world.Location;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.IMixinNextTickListEntry;
import org.spongepowered.common.util.VecHelper;

import java.time.Duration;

@Mixin(NextTickListEntry.class)
@Implements(@Interface(iface = ScheduledUpdate.class, prefix = "update$"))
public abstract class MixinNextTickListEntry<T> implements IMixinNextTickListEntry {

    @Shadow public abstract Object shadow$getTarget();
    @Shadow @Final public BlockPos position;
    @Shadow @Final public long scheduledTime;
    @Shadow @Final public TickPriority priority;

    private Location location;
    private World world;

    @Override
    public void setWorld(World world) {
        checkState(this.location == null, "World already known");
        this.location = new Location((org.spongepowered.api.world.World) world, VecHelper.toVector3i(this.position));
        this.world = world;
    }

    public Location update$getLocation() {
        return this.location;
    }

    /**
     * Gets the target of this scheduled update.
     *
     * @return The target
     */
    public T update$getTarget() {
        return (T) shadow$getTarget();
    }

    /**
     * Gets the {@link Duration delay} until this update
     * should cause the block to update.
     *
     * @return The delay until this SBU should cause the block to update
     */
    public Duration update$getDelay() {

    }

    /**
     * Gets the priority of this scheduled block update.
     *
     * @return The priority of this scheduled block update
     */
    public TaskPriority update$getPriority() {

    }

    /**
     * Gets the {@link ScheduledUpdate.State} of this scheduled update.
     *
     * @return The state
     */
    public ScheduledUpdate.State update$getState() {

    }

    /**
     * Cancels this scheduled update.
     *
     * @return Returns if the scheduled update was successfully cancelled
     */
    public boolean update$cancel() {

    }

}
