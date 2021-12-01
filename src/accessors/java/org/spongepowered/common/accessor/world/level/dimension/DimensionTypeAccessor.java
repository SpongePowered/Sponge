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
package org.spongepowered.common.accessor.world.level.dimension;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.common.UntransformedAccessorError;
import org.spongepowered.common.UntransformedInvokerError;

import java.util.OptionalLong;

@Mixin(DimensionType.class)
public interface DimensionTypeAccessor {

    @Invoker("<init>") static DimensionType invoker$new(final OptionalLong var1, final boolean var2, final boolean var3, final boolean var4,
        final boolean var5, final double var6, final boolean var8, final boolean var9, final boolean var10, final boolean var11, final int var12, final int var13,
        final int var14, final ResourceLocation var15, final ResourceLocation var16, final float var17
    ) {
        throw new UntransformedInvokerError();
    }

    @Accessor("DEFAULT_OVERWORLD") static DimensionType accessor$DEFAULT_OVERWORLD() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DEFAULT_OVERWORLD_CAVES") static DimensionType accessor$DEFAULT_OVERWORLD_CAVES() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DEFAULT_NETHER") static DimensionType accessor$DEFAULT_NETHER() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DEFAULT_END") static DimensionType accessor$DEFAULT_END() {
        throw new UntransformedAccessorError();
    }

    @Accessor("fixedTime") OptionalLong accessor$fixedTime();

    @Accessor("effectsLocation") ResourceLocation accessor$effectsLocation();

    @Accessor("ambientLight") float accessor$ambientLight();

    @Accessor("infiniburn") ResourceLocation accessor$infiniburn();
}
