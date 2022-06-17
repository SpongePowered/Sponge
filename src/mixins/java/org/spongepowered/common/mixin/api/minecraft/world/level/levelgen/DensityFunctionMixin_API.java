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
package org.spongepowered.common.mixin.api.minecraft.world.level.levelgen;

import net.minecraft.world.level.levelgen.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.math.vector.Vector3i;

@Mixin(DensityFunction.class)
public interface DensityFunctionMixin_API extends org.spongepowered.api.world.generation.config.noise.DensityFunction {

    // @formatter:off
    @Shadow double shadow$minValue();
    @Shadow double shadow$maxValue();
    @Shadow double shadow$compute(final DensityFunction.FunctionContext var1);
    // @formatter:on

    @Override
    default double min() {
        return this.shadow$minValue();
    }

    @Override
    default double max() {
        return this.shadow$maxValue();
    }

    @Override
    default double compute(final Vector3i pos) {
        return this.compute(pos.x(), pos.y(), pos.z());
    }

    @Override
    default double compute(final int x, final int y, final int z) {
        return this.shadow$compute(new DensityFunction.SinglePointContext(x, y, z));
    }
}
