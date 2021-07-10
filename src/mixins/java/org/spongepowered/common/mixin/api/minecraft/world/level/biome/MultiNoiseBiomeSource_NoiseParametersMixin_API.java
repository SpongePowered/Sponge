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

import it.unimi.dsi.fastutil.doubles.DoubleList;
import org.spongepowered.api.world.biome.provider.multinoise.MultiNoiseConfig;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

@Mixin(MultiNoiseBiomeSource.NoiseParameters.class)
@Implements(@Interface(iface = MultiNoiseConfig.class, prefix = "multiNoiseConfig$"))
public abstract class MultiNoiseBiomeSource_NoiseParametersMixin_API implements MultiNoiseConfig {

    // @formatter:off
    @Shadow public abstract int shadow$firstOctave();
    @Shadow public abstract DoubleList shadow$amplitudes();
    // @formatter:on

    @Intrinsic
    public int multiNoiseConfig$firstOctave() {
        return this.shadow$firstOctave();
    }

    @Intrinsic
    public List<Double> multiNoiseConfig$amplitudes() {
        return this.shadow$amplitudes();
    }
}
