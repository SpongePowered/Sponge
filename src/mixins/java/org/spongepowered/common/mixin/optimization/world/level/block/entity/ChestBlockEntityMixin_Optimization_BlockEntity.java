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

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.mixin.core.world.level.block.entity.RandomizableContainerBlockEntityMixin;

@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin_Optimization_BlockEntity extends RandomizableContainerBlockEntityMixin {

    @Shadow protected float openness;
    @Shadow protected int openCount;
    @Shadow private int tickInterval;
    @Shadow protected float oOpenness; // old openness

    @Shadow protected abstract void shadow$playSound(SoundEvent soundIn);
    @Shadow public static int shadow$getOpenCount(final Level p_213977_0_, final BaseContainerBlockEntity p_213977_1_, final int p_213977_2_,
            final int p_213977_3_, final int p_213977_4_, final int p_213977_5_, final int p_213977_6_) {
        throw new UnsupportedOperationException("Shadowed getOpenCount");
    }

    /**
     * @author bloodmc - July 21st, 2016
     *
     * @reason Overwritten in case chests ever attempt to tick
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void impl$DisableTickingChestsOnServer(final CallbackInfo ci) {
        ++this.tickInterval;
        ci.cancel();
    }

    @Inject(method = "playSound", at = @At("HEAD"), cancellable = true)
    private void impl$onPlaySound(final SoundEvent soundIn, final CallbackInfo ci) {
        if (!this.shadow$getBlockState().hasProperty(ChestBlock.TYPE)) {
            ci.cancel();
        }
    }

    @Inject(method = "startOpen", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/ChestBlockEntity;signalOpenCount()V"))
    private void impl$onOpenInventory(final Player player, final CallbackInfo ci) {
        this.impl$doOpenLogic();
    }

    @Inject(method = "stopOpen", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/ChestBlockEntity;signalOpenCount()V"))
    private void impl$onCloseInventory(final Player player, final CallbackInfo ci) {
        this.impl$doCloseLogic();
    }

    // Moved out of tick loop
    private void impl$doOpenLogic() {
        final int x = this.worldPosition.getX();
        final int y = this.worldPosition.getY();
        final int z = this.worldPosition.getZ();
        ++this.tickInterval;
        this.openCount = ChestBlockEntityMixin_Optimization_BlockEntity.shadow$getOpenCount(this.level, (BaseContainerBlockEntity) (Object) this,
                this.tickInterval, x, y, z, this.openCount);
        this.oOpenness = this.openness;
        // final float lvt_4_1_ = 0.1F; // inlined
        if (this.openCount > 0 && this.openness == 0.0F) {
            this.shadow$playSound(SoundEvents.CHEST_OPEN);
        }
    }

    // Moved out of tick loop
    private void impl$doCloseLogic() {
        if (this.openCount == 0 /*&& this.openness > 0.0F || this.openCount > 0 && this.openness < 1.0F*/) {
            final float lvt_5_1_ = this.openness;
            if (this.openCount > 0) {
                this.openness += 0.1F;
            } else {
                this.openness -= 0.1F;
            }

            if (this.openness > 1.0F) {
                this.openness = 1.0F;
            }

            // final float lvt_6_1_ = 0.5F; // inlined
            if (this.openness < 0.5F && lvt_5_1_ >= 0.5F) {
                this.shadow$playSound(SoundEvents.CHEST_CLOSE);
            }

            if (this.openness < 0.0F) {
                this.openness = 0.0F;
            }
        }
    }

}

