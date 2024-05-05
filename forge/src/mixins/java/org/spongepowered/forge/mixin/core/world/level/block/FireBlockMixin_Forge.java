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
package org.spongepowered.forge.mixin.core.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.server.level.ServerLevelBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;

@Mixin(FireBlock.class)
public class FireBlockMixin_Forge {
    // forge changes the signature of this method
    @Inject(method = "tryCatchFire",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"),
            require = 0,
            expect = 0,
            cancellable = true)
    private void impl$onCatchFirePreCheck(
            final Level world, final BlockPos pos, final int chance, final RandomSource random, final int age, final Direction dir, final CallbackInfo callbackInfo) {
        if (!world.isClientSide) {
            if (ShouldFire.CHANGE_BLOCK_EVENT_PRE && SpongeCommonEventFactory.callChangeBlockEventPre((ServerLevelBridge) world, pos).isCancelled()) {
                callbackInfo.cancel();
            }
        }
    }

    @Inject(method = "tryCatchFire",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"),
            require = 0,
            expect = 0,
            cancellable = true)
    private void impl$onCatchFirePreCheckOther(
        final Level world, final BlockPos pos, final int chance, final RandomSource random, final int age, final Direction dir, final CallbackInfo callbackInfo) {
        if (!world.isClientSide) {
            if (ShouldFire.CHANGE_BLOCK_EVENT_PRE && SpongeCommonEventFactory.callChangeBlockEventPre((ServerLevelBridge) world, pos).isCancelled()) {
                callbackInfo.cancel();
            }
        }
    }
}
