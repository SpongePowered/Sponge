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
package org.spongepowered.common.mixin.realtime.world.level.block.entity;

import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.MixinTargetHelper;
import org.spongepowered.common.bridge.RealTimeTrackingBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.mixin.core.world.level.block.entity.BlockEntityMixin;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin_RealTime extends BlockEntityMixin {

    @Shadow private int brewTime;

    @Redirect(
        method = "serverTick",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;brewTime:I",
            opcode = Opcodes.PUTFIELD, ordinal = 0
        )
    )
    private static void realTimeImpl$adjustForRealTimeBrewTime(final BrewingStandBlockEntity self, final int modifier) {
        final BrewingStandBlockEntityMixin_RealTime mixinSelf = MixinTargetHelper.cast(self);
        if (((WorldBridge) mixinSelf.level).bridge$isFake()) {
            mixinSelf.brewTime = modifier;
            return;
        }
        final int ticks = (int) ((RealTimeTrackingBridge) mixinSelf.level).realTimeBridge$getRealTimeTicks();
        mixinSelf.brewTime = Math.max(0, mixinSelf.brewTime - ticks);
    }

}
