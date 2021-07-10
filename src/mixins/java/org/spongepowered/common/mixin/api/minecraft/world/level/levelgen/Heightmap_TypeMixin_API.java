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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.util.BlockReaderAwareMatcher;
import org.spongepowered.api.world.HeightType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Predicate;
import net.minecraft.world.level.levelgen.Heightmap;

@Mixin(value = Heightmap.Types.class)
public abstract class Heightmap_TypeMixin_API implements HeightType {

    // @formatter:off
    @Shadow @Final private Predicate<net.minecraft.world.level.block.state.BlockState> isOpaque;
    // @formatter:on

    @Override
    @NonNull
    public BlockReaderAwareMatcher<BlockState> matcher() {
        return (state, volume, position) -> this.isOpaque.test((net.minecraft.world.level.block.state.BlockState) state);
    }

}
