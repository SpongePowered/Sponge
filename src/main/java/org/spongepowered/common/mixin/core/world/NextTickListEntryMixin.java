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
import net.minecraft.world.World;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.api.world.Location;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.NextTickListEntryBridge;
import org.spongepowered.common.util.VecHelper;

import javax.annotation.Nullable;

@Mixin(NextTickListEntry.class)
public class NextTickListEntryMixin implements NextTickListEntryBridge {

    @Shadow @Final public BlockPos position;
    @Shadow public int priority;

    @Nullable @MonotonicNonNull private Location<org.spongepowered.api.world.World> impl$location;

    @Override
    public void bridge$setWorld(World world) {
        checkState(this.impl$location == null, "World already known");
        this.impl$location = new Location<>((org.spongepowered.api.world.World) world, VecHelper.toVector3i(this.position));
    }

    @Override
    public Location<org.spongepowered.api.world.World> bridge$getLocation() {
        checkState(this.impl$location != null, "Unable to determine location at this time");
        return this.impl$location;
    }


}
