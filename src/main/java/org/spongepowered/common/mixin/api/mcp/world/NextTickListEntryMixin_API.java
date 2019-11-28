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
package org.spongepowered.common.mixin.api.mcp.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import org.spongepowered.api.block.ScheduledBlockUpdate;
import org.spongepowered.api.world.Location;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.NextTickListEntryBridge;

@Mixin(NextTickListEntry.class)
public class NextTickListEntryMixin_API implements ScheduledBlockUpdate {

    @Shadow @Final public BlockPos position;
    @Shadow public int priority;
    @Shadow public long scheduledTime;

    private Location<org.spongepowered.api.world.World> location;
    private World world;

    @Override
    public Location<org.spongepowered.api.world.World> getLocation() {
        return ((NextTickListEntryBridge) this).bridge$getLocation();
    }

    @Override
    public int getTicks() {
        if (this.world == null) {
            return Integer.MAX_VALUE;
        }
        return (int) (this.scheduledTime - this.world.func_72912_H().func_82573_f());
    }

    @Override
    public void setTicks(int ticks) {
        if (this.world == null) {
            return;
        }
        this.scheduledTime = this.world.func_72912_H().func_82573_f() + ticks;
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
