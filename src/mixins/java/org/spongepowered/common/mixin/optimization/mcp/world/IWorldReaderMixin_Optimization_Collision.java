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
package org.spongepowered.common.mixin.optimization.mcp.world;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.WorldGenRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.stream.Stream;

@Mixin(value = IWorldReader.class, priority = 1500)
public interface IWorldReaderMixin_Optimization_Collision {

    @Shadow default Stream<BlockState> getBlockStatesIfLoaded(final AxisAlignedBB aabb) { throw new AssertionError(); }

    // TODO: Mixin probably doesn't support this yet......... so may have to become an overwrite
    @SuppressWarnings("deprecation")
    @Redirect(method = "getBlockStatesIfLoaded", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/IWorldReader;hasChunksAt(IIIIII)Z"))
    default boolean activeCollision$ignoreHasChunksAt(final IWorldReader world, final int xStart, final int yStart, final int zStart,
        final int xEnd, final int yEnd, final int zEnd) {
        return !(world instanceof WorldGenRegion) || world.hasChunksAt(xStart, yStart, zStart, xEnd, yEnd, zEnd);
    }

}
