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
package org.spongepowered.common.mixin.optimization.world.level.block.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.mixin.core.world.level.block.entity.BlockEntityMixin;

@Mixin(EnderChestBlockEntity.class)
public abstract class EnderChestBlockEntityMixin_Optimization_BlockEntity extends BlockEntityMixin {

    @Shadow public float openness;
    @Shadow public int openCount;
    @Shadow public float oOpenness;
    @Shadow private int tickInterval;

    /**
     * @author bloodmc - July 21st, 2016
     * @author gabizou - March 31st, 2020 - Minecraft 1.14.4
     *
     * @reason Overwritten in case ender chests ever attempt to tick
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void impl$IgnoreTicking(final CallbackInfo ci) {
        if (++this.tickInterval % 20 * 4 == 0) {
            this.level.blockEvent(this.worldPosition, Blocks.ENDER_CHEST, 1, this.openCount);
        }
        this.oOpenness = this.openness;
        ci.cancel();
    }


    @Inject(method = "startOpen", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;blockEvent(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;II)V"))
    private void impl$onOpenChest(final CallbackInfo ci) {
        this.impl$doOpenLogic();
    }

    @Inject(method = "stopOpen", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;blockEvent(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;II)V"))
    private void impl$onCloseChest(final CallbackInfo ci) {
        this.impl$doCloseLogic();
    }

    // Moved out of tick loop
    private void impl$doOpenLogic() {
        final int lvt_1_1_ = this.worldPosition.getX();
        final int lvt_2_1_ = this.worldPosition.getY();
        final int lvt_3_1_ = this.worldPosition.getZ();
        // final float lvt_4_1_ = 0.1F; // compiler-inlined
        final double lvt_7_2_;
        if (this.openCount > 0 && this.openness == 0.0F) {
            final double lvt_5_1_ = (double)lvt_1_1_ + 0.5D;
            lvt_7_2_ = (double)lvt_3_1_ + 0.5D;
            this.level.playSound((Player)null, lvt_5_1_, (double)lvt_2_1_ + 0.5D, lvt_7_2_, SoundEvents.ENDER_CHEST_OPEN,
                    SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
        }
    }

    // Moved out of tick loop
    private void impl$doCloseLogic() {
        final int lvt_1_1_ = this.worldPosition.getX();
        final int lvt_2_1_ = this.worldPosition.getY();
        final int lvt_3_1_ = this.worldPosition.getZ();
        final double lvt_7_2_;

        if (this.openCount == 0) { /*&& this.openness > 0.0F || this.openCount > 0 && this.openness < 1.0F) {
            float lvt_5_2_ = this.openness;
            if (this.openCount > 0) {
                this.openness += 0.1F;
            } else {
                this.openness -= 0.1F;
            }

            if (this.openness > 1.0F) {
                this.openness = 1.0F;
            }

            float lvt_6_1_ = 0.5F;
            if (this.openness < 0.5F && lvt_5_2_ >= 0.5F) {*/
                lvt_7_2_ = (double)lvt_1_1_ + 0.5D;
                final double lvt_9_1_ = (double)lvt_3_1_ + 0.5D;
                this.level.playSound((Player)null, lvt_7_2_, (double)lvt_2_1_ + 0.5D, lvt_9_1_, SoundEvents.ENDER_CHEST_CLOSE,
                        SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
            // }

            if (this.openness < 0.0F) {
                this.openness = 0.0F;
            }
        }
    }

}
