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

import net.minecraft.util.BlockPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import org.spongepowered.api.block.ScheduledBlockUpdate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.IMixinBlockUpdate;
import org.spongepowered.common.util.VecHelper;

@Mixin(NextTickListEntry.class)
public class MixinNextTickListEntry implements ScheduledBlockUpdate, IMixinBlockUpdate {

    @Shadow public int priority;
    @Shadow public long scheduledTime;
    @Shadow public BlockPos position;

    private Location location;
    private World world;

    @Override
    public void setWorld(World world) {
        checkState(this.location == null, "World already known");
        this.location = new Location((Extent) world, VecHelper.toVector(this.position));
        this.world = world;
    }

    @Override
    public Location getLocation() {
        checkState(this.location != null, "Unable to determine location at this time");
        return this.location;
    }

    @Override
    public int getTicks() {
        if (this.world == null) {
            return Integer.MAX_VALUE;
        }
        return (int) (this.scheduledTime - this.world.getWorldInfo().getWorldTotalTime());
    }

    @Override
    public void setTicks(int ticks) {
        if (this.world == null) {
            return;
        }
        this.scheduledTime = this.world.getWorldInfo().getWorldTotalTime() + ticks;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

}
