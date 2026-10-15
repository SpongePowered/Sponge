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
package org.spongepowered.common.mixin.gametest;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.StructureUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Until 1.21.3, test positions are measured from the structure block, which sits one block below the structure, so
 * {@code (0, 1, 0)} is the structure's bottom layer. Measuring from the structure's own origin, as 1.21.3 does, lets the
 * gametests address their structures the same way on every version. Forge already does this by default (its
 * {@code fixGameTestBlockPosAsserts} config), so there is nothing to change there.
 */
@Mixin(GameTestHelper.class)
public abstract class GameTestHelperMixin_GameTest {

    @WrapOperation(
        method = {"absolutePos", "relativePos", "absoluteVec", "relativeVec"},
        at = @At(value = "INVOKE", target = "Lnet/minecraft/gametest/framework/GameTestInfo;getStructureBlockPos()Lnet/minecraft/core/BlockPos;"),
        require = 0
    )
    private BlockPos gametest$measureFromStructureOrigin(final GameTestInfo info, final Operation<BlockPos> original) {
        return StructureUtils.getStructureOrigin(info.getStructureBlockEntity());
    }
}
