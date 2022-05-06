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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.common.UntransformedAccessorError;
import org.spongepowered.common.UntransformedInvokerError;

import java.util.OptionalLong;

@Mixin(DimensionType.class)
public interface DimensionTypeAccessor {

    @Invoker("<init>")
    static DimensionType invoker$new(final OptionalLong $$0, final boolean $$1, final boolean $$2, final boolean $$3, final boolean $$4,
            final double $$5, final boolean $$6, final boolean $$7, final boolean $$8, final boolean $$9, final int $$10, final int $$11,
            final int $$12, final TagKey<Block> $$13, final ResourceLocation $$14, final float $$15) {
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
}
