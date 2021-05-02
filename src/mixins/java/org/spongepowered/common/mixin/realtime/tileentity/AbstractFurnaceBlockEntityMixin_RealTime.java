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
package org.spongepowered.common.mixin.realtime.tileentity;

import org.objectweb.asm.Opcodes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.common.bridge.RealTimeTrackingBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.mixin.core.world.level.block.entity.BlockEntityMixin;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

@Mixin(value = AbstractFurnaceBlockEntity.class, priority = 1001)
public abstract class AbstractFurnaceBlockEntityMixin_RealTime extends BlockEntityMixin {

    @Shadow private int litTime;
    @Shadow private int cookingProgress;
    @Shadow private int cookingTotalTime;

    @Redirect(method = "tick",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;litTime:I",
            opcode = Opcodes.PUTFIELD
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;isLit()Z",
                opcode = 1
            ),
            to = @At(
                value = "FIELD",
                target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;level:Lnet/minecraft/world/level/Level;",
                opcode = Opcodes.GETFIELD,
                ordinal = 0
            )
        )
    )
    private void realTimeImpl$adjustForRealTimeBurnTime(final AbstractFurnaceBlockEntity self, final int modifier) {
        if (((WorldBridge) this.level).bridge$isFake()) {
            this.litTime = modifier;
            return;
        }
        final int ticks = (int) ((RealTimeTrackingBridge) this.level).realTimeBridge$getRealTimeTicks();
        this.litTime = Math.max(0, this.litTime - Math.max(1, ticks - 1));
    }

    @Redirect(
        method = "tick",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;cookingProgress:I",
            opcode = Opcodes.PUTFIELD
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;canBurn(Lnet/minecraft/world/item/crafting/Recipe;)Z",
                ordinal = 1
            ),
            to = @At(
                value = "FIELD",
                target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;cookingTotalTime:I",
                opcode = Opcodes.GETFIELD,
                ordinal = 0
            )
        )
    )
    private void realTimeImpl$adjustForRealTimeCookTime(final AbstractFurnaceBlockEntity self, final int modifier) {
        if (((WorldBridge) this.level).bridge$isFake()) {
            this.cookingProgress = modifier;
            return;
        }
        final int ticks = (int) ((RealTimeTrackingBridge) this.level).realTimeBridge$getRealTimeTicks();
        this.cookingProgress = Math.min(this.cookingTotalTime, this.cookingProgress + ticks);
    }

    @Redirect(
        method = "tick",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;cookingProgress:I",
            opcode = Opcodes.PUTFIELD
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/util/Mth;clamp(III)I"
            ),
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
            )
        )
    )
    private void realTimeImpl$adjustForRealTimeCookTimeCooldown(final AbstractFurnaceBlockEntity self, final int modifier) {
        if (((WorldBridge) this.level).bridge$isFake()) {
            this.cookingProgress = modifier;
            return;
        }
        final int ticks = (int) ((RealTimeTrackingBridge) this.level).realTimeBridge$getRealTimeTicks();
        this.cookingProgress = Mth.clamp(this.cookingProgress - (2 * ticks), 0, this.cookingTotalTime);
    }

}
