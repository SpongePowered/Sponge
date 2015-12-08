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
package org.spongepowered.common.mixin.core.entity;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.event.MinecraftFallingBlockDamageSource;

import java.util.ArrayList;
import java.util.Iterator;

@Mixin(EntityFallingBlock.class)
public abstract class MixinEntityFallingBlock extends MixinEntity implements FallingBlock {


    @Shadow public IBlockState fallTile;
    @Shadow public boolean hurtEntities;
    @Shadow public int fallHurtMax;
    @Shadow public float fallHurtAmount;
    @Shadow public NBTTagCompound tileEntityData;
    @Shadow public boolean canSetAsBlock;

    private DamageSource original;
    private boolean isAnvil;

    @Inject(method = "fall(FF)V", at = @At(value = "JUMP", opcode = Opcodes.IFEQ, ordinal = 1))
    public void beforeFall(float distance, float damageMultipleier, CallbackInfo callbackInfo) {
        this.isAnvil = this.fallTile.getBlock() == Blocks.anvil;
        this.original = this.isAnvil ? DamageSource.anvil : DamageSource.fallingBlock;
        if (this.isAnvil) {
            DamageSource.anvil = new MinecraftFallingBlockDamageSource("anvil", (EntityFallingBlock) (Object) this);
        } else {
            DamageSource.fallingBlock = new MinecraftFallingBlockDamageSource("fallingblock", (EntityFallingBlock) (Object) this);
        }
    }

    @Inject(method = "fall(FF)V", at = @At("RETURN"))
    public void afterFall(float distance, float damageMultiplier, CallbackInfo ci) {
        if (this.original == null) {
            return;
        }
        if (this.isAnvil) {
            DamageSource.anvil = this.original;
        } else {
            DamageSource.fallingBlock = this.original;
        }
    }

    /**
     * @author gabizou - November 22, 2015
     *
     * Purpose: This will overwrite the damage source to properly use our damage source objects.
     */
    @Overwrite
    public void fall(float distance, float damageMultiplier)
    {
        Block block = this.fallTile.getBlock();

        if (this.hurtEntities) {
            int i = MathHelper.ceiling_float_int(distance - 1.0F);

            if (i > 0) {
                ArrayList arraylist = Lists.newArrayList(this.worldObj.getEntitiesWithinAABBExcludingEntity((EntityFallingBlock) (Object) this, this.getEntityBoundingBox()));
                boolean flag = block == Blocks.anvil;
                // Sponge Start - re-assign the damage sources for equality checks
                DamageSource original;
                if (flag) {
                    original = DamageSource.anvil;
                    DamageSource.anvil = new MinecraftFallingBlockDamageSource("anvil", (EntityFallingBlock) (Object) this);
                } else {
                    original = DamageSource.fallingBlock;

                }
                // Sponge End
                DamageSource damagesource = flag ? DamageSource.anvil : DamageSource.fallingBlock;
                Iterator iterator = arraylist.iterator();

                while (iterator.hasNext()) {
                    Entity entity = (Entity) iterator.next();
                    entity
                        .attackEntityFrom(damagesource, (float) Math.min(MathHelper.floor_float((float) i * this.fallHurtAmount), this.fallHurtMax));

                }
                // Sponge Start - Re-assign back to the original damage sources so as to not leak entities
                if (flag) {
                    DamageSource.anvil = original;
                } else {
                    DamageSource.fallingBlock = original;
                }
                // Sponge End

                if (flag && (double) this.rand.nextFloat() < 0.05000000074505806D + (double) i * 0.05D) {
                    int j = ((Integer) this.fallTile.getValue(BlockAnvil.DAMAGE)).intValue();
                    ++j;

                    if (j > 2) {
                        this.canSetAsBlock = true;
                    } else {
                        this.fallTile = this.fallTile.withProperty(BlockAnvil.DAMAGE, Integer.valueOf(j));
                    }
                }
            }
        }
    }

}
