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
package org.spongepowered.common.mixin.core.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;

import java.util.Random;

@Mixin(FireBlock.class)
public abstract class BlockFireMixin extends BlockMixin {

    @Redirect(method = "updateTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z",
            ordinal = 1))
    private boolean impl$onFireSpread(final World world, final BlockPos pos, final BlockState state, final int updateFlag) {
        if (!((WorldBridge) world).bridge$isFake() && ShouldFire.CHANGE_BLOCK_EVENT_PRE) {
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.addContext(EventContextKeys.FIRE_SPREAD, (org.spongepowered.api.world.World) world);
                if (SpongeCommonEventFactory.callChangeBlockEventPre((WorldServerBridge) world, pos).isCancelled()) {
                    return false;
                }
            }

        }
        return world.func_180501_a(pos, state, updateFlag);
    }

    @Inject(method = "catchOnFire",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z"),
        require = 0,
        expect = 0,
        cancellable = true)
    private void impl$onCatchFirePreCheck(
        final World world, final BlockPos pos, final int chance, final Random random, final int age, final CallbackInfo callbackInfo) {
        if (!world.field_72995_K) {
            if (SpongeCommonEventFactory.callChangeBlockEventPre((WorldServerBridge) world, pos).isCancelled()) {
                callbackInfo.cancel();
            }
        }
    }

    @Inject(method = "catchOnFire",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockToAir(Lnet/minecraft/util/math/BlockPos;)Z"),
        require = 0,
        expect = 0,
        cancellable = true)
    private void impl$onCatchFirePreCheckOther(
        final World world, final BlockPos pos, final int chance, final Random random, final int age, final CallbackInfo callbackInfo) {
        if (!world.field_72995_K) {
            if (SpongeCommonEventFactory.callChangeBlockEventPre((WorldServerBridge) world, pos).isCancelled()) {
                callbackInfo.cancel();
            }
        }
    }

}
