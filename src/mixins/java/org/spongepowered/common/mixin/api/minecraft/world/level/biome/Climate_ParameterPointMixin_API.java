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
package org.spongepowered.common.mixin.api.minecraft.world.level.biome;

import net.minecraft.world.level.biome.Climate;
import org.spongepowered.api.util.Range;
import org.spongepowered.api.world.biome.BiomeAttributes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Climate.ParameterPoint.class)
public abstract class Climate_ParameterPointMixin_API implements BiomeAttributes {

    // @formatter:off
    @Shadow @Final private Climate.Parameter continentalness;
    @Shadow @Final private Climate.Parameter temperature;
    @Shadow @Final private Climate.Parameter humidity;
    @Shadow @Final private Climate.Parameter erosion;
    @Shadow @Final private Climate.Parameter depth;
    @Shadow @Final private Climate.Parameter weirdness;
    @Shadow @Final private long offset;
    // @formatter:on

    @Override
    public Range<Float> temperature() {
        return (Range<Float>) (Object) this.temperature;
    }

    @Override
    public Range<Float> humidity() {
        return (Range<Float>) (Object) this.humidity;
    }

    @Override
    public Range<Float> continentalness() {
        return (Range<Float>) (Object) this.continentalness;
    }

    @Override
    public Range<Float> erosion() {
        return (Range<Float>) (Object) this.erosion;
    }

    @Override
    public Range<Float> depth() {
        return (Range<Float>) (Object) this.depth;
    }

    @Override
    public Range<Float> weirdness() {
        return (Range<Float>) (Object) this.weirdness;
    }

    @Override
    public float offset() {
        return Climate.unquantizeCoord(this.offset);
    }
}
