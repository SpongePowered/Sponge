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
package org.spongepowered.common.world.volume;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.world.volume.Volume;

import java.util.function.Function;

public abstract class AbstractVolume implements Volume {

    protected Function<BlockPos, IBlockState> blockProvider;
    protected final Vector3i min;
    protected final Vector3i max;
    protected final Vector3i size;

    protected AbstractVolume(Vector3i min, Vector3i max) {
        this.min = min;
        this.max = max;
        this.size = this.max.sub(this.min);
    }


    @Override
    public Vector3i getBlockMin() {
        return null;
    }

    @Override
    public Vector3i getBlockMax() {
        return null;
    }

    @Override
    public Vector3i getBlockSize() {
        return null;
    }

    @Override
    public boolean containsBlock(int x, int y, int z) {
        return false;
    }

    @Override
    public boolean isAreaAvailable(int x, int y, int z) {
        return false;
    }

    @Override
    public Volume getView(Vector3i newMin, Vector3i newMax) {
        return null;
    }
}
