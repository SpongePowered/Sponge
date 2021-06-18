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

import net.minecraft.world.level.biome.Biome;
import org.spongepowered.api.world.biome.BiomeAttributes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Biome.ClimateParameters.class)
public abstract class Biome_ClimateParametersMixin_API implements BiomeAttributes {

    // @formatter:off
    @Shadow @Final private float temperature;
    @Shadow @Final private float humidity;
    @Shadow @Final private float altitude;
    @Shadow @Final private float weirdness;
    @Shadow @Final private float offset;
    // @formatter:on

    @Override
    public float temperature() {
        return this.temperature;
    }

    @Override
    public float humidity() {
        return this.humidity;
    }

    @Override
    public float altitude() {
        return this.altitude;
    }

    @Override
    public float weirdness() {
        return this.weirdness;
    }

    @Override
    public float offset() {
        return this.offset;
    }
}
