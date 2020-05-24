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
package org.spongepowered.common.mixin.optimization.mcp.tileentity;

import net.minecraft.block.ChestBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.mixin.core.tileentity.LockableLootTileEntityMixin;

@Mixin(ChestTileEntity.class)
public abstract class ChestTileEntityMixin_Optimization_TileEntity extends LockableLootTileEntityMixin {

    @Shadow protected float lidAngle;
    @Shadow protected int numPlayersUsing;
    @Shadow private int ticksSinceSync;
    @Shadow protected float prevLidAngle;

    @Shadow protected abstract void shadow$playSound(SoundEvent soundIn);
    @Shadow public static int shadow$calculatePlayersUsingSync(World p_213977_0_, LockableTileEntity p_213977_1_, int p_213977_2_, int p_213977_3_, int p_213977_4_, int p_213977_5_, int p_213977_6_) {
        throw new UnsupportedOperationException("Shadowed calculatePlayersUsingSync");
    }

    /**
     * @author bloodmc - July 21st, 2016
     *
     * @reason Overwritten in case chests ever attempt to tick
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void impl$DisableTickingChestsOnServer(final CallbackInfo ci) {
        ++this.ticksSinceSync;
        ci.cancel();
    }

    @Inject(method = "playSound", at = @At("HEAD"), cancellable = true)
    private void impl$onPlaySound(SoundEvent soundIn, CallbackInfo ci) {
        if (!this.shadow$getBlockState().has(ChestBlock.TYPE)) {
            ci.cancel();
        }
    }

    @Inject(method = "openInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/ChestTileEntity;onOpenOrClose()V"))
    private void impl$onOpenInventory(PlayerEntity player, CallbackInfo ci) {
        this.impl$doOpenLogic();
    }

    @Inject(method = "closeInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/ChestTileEntity;onOpenOrClose()V"))
    private void impl$onCloseInventory(PlayerEntity player, CallbackInfo ci) {
        this.impl$doCloseLogic();
    }

    // Moved out of tick loop
    private void impl$doOpenLogic() {
        int i = this.pos.getX();
        int j = this.pos.getY();
        int k = this.pos.getZ();

        this.numPlayersUsing = shadow$calculatePlayersUsingSync(this.world, (LockableTileEntity) (Object) this, this.ticksSinceSync, i, j, k, this.numPlayersUsing);
        this.prevLidAngle = this.lidAngle;
        float f = 0.1F;
        if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F) {
            this.shadow$playSound(SoundEvents.BLOCK_CHEST_OPEN);
        }
    }

    // Moved out of tick loop
    private void impl$doCloseLogic() {
        if (this.numPlayersUsing == 0/* && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F*/) {
            float f1 = this.lidAngle;
            if (this.numPlayersUsing > 0) {
                this.lidAngle += 0.1F;
            } else {
                this.lidAngle -= 0.1F;
            }

            if (this.lidAngle > 1.0F) {
                this.lidAngle = 1.0F;
            }

            float f2 = 0.5F;
            if (this.lidAngle < 0.5F && f1 >= 0.5F) {
                this.shadow$playSound(SoundEvents.BLOCK_CHEST_CLOSE);
            }

            if (this.lidAngle < 0.0F) {
                this.lidAngle = 0.0F;
            }
        }
    }

}

