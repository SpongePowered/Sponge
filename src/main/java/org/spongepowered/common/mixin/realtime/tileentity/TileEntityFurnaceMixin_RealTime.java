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

import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.common.bridge.RealTimeTrackingBridge;
import org.spongepowered.common.bridge.world.WorldBridge;

@Mixin(value = FurnaceTileEntity.class, priority = 1001)
public abstract class TileEntityFurnaceMixin_RealTime extends TileEntity {

    @Shadow private int furnaceBurnTime;
    @Shadow private int cookTime;
    @Shadow private int totalCookTime;

    @Redirect(method = "update",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/tileentity/TileEntityFurnace;furnaceBurnTime:I",
            opcode = Opcodes.PUTFIELD
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/tileentity/TileEntityFurnace;isBurning()Z",
                opcode = 1
            ),
            to = @At(
                value = "FIELD",
                target = "Lnet/minecraft/tileentity/TileEntityFurnace;world:Lnet/minecraft/world/World;",
                opcode = Opcodes.GETFIELD,
                ordinal = 0
            )
        )
    )
    private void realTimeImpl$adjustForRealTimeBurnTime(final FurnaceTileEntity self, final int modifier) {
        if (((WorldBridge) this.field_145850_b).bridge$isFake()) {
            this.furnaceBurnTime = modifier;
            return;
        }
        final int ticks = (int) ((RealTimeTrackingBridge) this.func_145831_w()).realTimeBridge$getRealTimeTicks();
        this.furnaceBurnTime = Math.max(0, this.furnaceBurnTime - Math.max(1, ticks - 1));
    }

    @Redirect(
        method = "update",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/tileentity/TileEntityFurnace;cookTime:I",
            opcode = Opcodes.PUTFIELD
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/tileentity/TileEntityFurnace;canSmelt()Z",
                ordinal = 1
            ),
            to = @At(
                value = "FIELD",
                target = "Lnet/minecraft/tileentity/TileEntityFurnace;totalCookTime:I",
                opcode = Opcodes.GETFIELD,
                ordinal = 0
            )
        )
    )
    private void realTimeImpl$adjustForRealTimeCookTime(final FurnaceTileEntity self, final int modifier) {
        if (((WorldBridge) this.field_145850_b).bridge$isFake()) {
            this.cookTime = modifier;
            return;
        }
        final int ticks = (int) ((RealTimeTrackingBridge) this.func_145831_w()).realTimeBridge$getRealTimeTicks();
        this.cookTime = Math.min(this.totalCookTime, this.cookTime + ticks);
    }

    @Redirect(
        method = "update",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/tileentity/TileEntityFurnace;cookTime:I",
            opcode = Opcodes.PUTFIELD
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I"
            ),
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/block/BlockFurnace;setState(ZLnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"
            )
        )
    )
    private void realTimeImpl$adjustForRealTimeCookTimeCooldown(final FurnaceTileEntity self, final int modifier) {
        if (((WorldBridge) this.field_145850_b).bridge$isFake()) {
            this.cookTime = modifier;
            return;
        }
        final int ticks = (int) ((RealTimeTrackingBridge) this.func_145831_w()).realTimeBridge$getRealTimeTicks();
        this.cookTime = MathHelper.func_76125_a(this.cookTime - (2 * ticks), 0, this.totalCookTime);
    }

}
