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
package org.spongepowered.common.mixin.core.entity.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.event.damage.MinecraftFallingBlockDamageSource;
import org.spongepowered.common.mixin.core.entity.MixinEntity;

@Mixin(EntityFallingBlock.class)
public abstract class MixinEntityFallingBlock extends MixinEntity implements FallingBlock {


    @Shadow public IBlockState fallTile;
    @Shadow public boolean hurtEntities;
    @Shadow public int fallHurtMax;
    @Shadow public float fallHurtAmount;
    @Shadow public NBTTagCompound tileEntityData;

    private DamageSource original;
    private boolean isAnvil;

    @Inject(method = "fall(FF)V", at = @At(value = "JUMP", opcode = Opcodes.IFEQ, ordinal = 1))
    public void beforeFall(float distance, float damageMultipleier, CallbackInfo callbackInfo) {
        this.isAnvil = this.fallTile.getBlock() == Blocks.ANVIL;
        this.original = this.isAnvil ? DamageSource.ANVIL : DamageSource.FALLING_BLOCK;
        if (this.isAnvil) {
            DamageSource.ANVIL = new MinecraftFallingBlockDamageSource("anvil", (EntityFallingBlock) (Object) this);
        } else {
            DamageSource.FALLING_BLOCK = new MinecraftFallingBlockDamageSource("fallingblock", (EntityFallingBlock) (Object) this);
        }
    }

    @Inject(method = "fall(FF)V", at = @At("RETURN"))
    public void afterFall(float distance, float damageMultiplier, CallbackInfo ci) {
        if (this.original == null) {
            return;
        }
        if (this.isAnvil) {
            DamageSource.ANVIL = this.original;
        } else {
            DamageSource.FALLING_BLOCK = this.original;
        }
    }

}
