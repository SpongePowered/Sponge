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
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.event.damage.MinecraftFallingBlockDamageSource;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.mixin.core.entity.MixinEntity;

@Mixin(EntityFallingBlock.class)
public abstract class MixinEntityFallingBlock extends MixinEntity implements FallingBlock {


    @Shadow public IBlockState fallTile;
    @Shadow public boolean hurtEntities;
    @Shadow public int fallHurtMax;
    @Shadow public float fallHurtAmount;
    @Shadow public NBTTagCompound tileEntityData;


    @Inject(method = "onUpdate",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockToAir(Lnet/minecraft/util/math/BlockPos;)Z",
            shift = At.Shift.AFTER
        ),
        cancellable = true
    )
    private void onWorldSetBlockToAir(CallbackInfo ci) {
        final BlockPos pos = new BlockPos((EntityFallingBlock) (Object) this);
        if (((IMixinWorld) this.world).isFake()) {
            this.world.setBlockToAir(pos);
            return;
        }
        // Ideally, at this point we should still be in the EntityTickState and only this block should
        // be changing. What we need to do here is throw the block event specifically for setting air
        // and THEN if this one cancels, we should kill this entity off, unless we want some duplication
        // of falling blocks
        final PhaseData currentPhaseData = PhaseTracker.getInstance().getCurrentPhaseData();
        this.world.setBlockToAir(pos);
        // By this point, we should have one captured block at least.
        if (!TrackingUtil.processBlockCaptures(currentPhaseData.context.getCapturedBlockSupplier(), currentPhaseData.state, currentPhaseData.context)) {
            // So, it's been cancelled, we want to absolutely remove this entity.
            // And we want to stop the entity update at this point.
            this.setDead();
            ci.cancel();
        }
    }

    @Redirect(method = "fall",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"
        )
    )
    private boolean spongeAttackFallingOrAnvil(Entity entity, DamageSource source, float damage) {
        if (entity.world.isRemote) {
            return entity.attackEntityFrom(source, damage);
        }
        boolean isAnvil = this.fallTile.getBlock() == Blocks.ANVIL;
        try {
            if (isAnvil) {
                DamageSource.ANVIL = new MinecraftFallingBlockDamageSource("anvil", (EntityFallingBlock) (Object) this);
                return entity.attackEntityFrom(DamageSource.ANVIL, damage);
            } else {
                DamageSource.FALLING_BLOCK = new MinecraftFallingBlockDamageSource("fallingblock", (EntityFallingBlock) (Object) this);
                return entity.attackEntityFrom(DamageSource.FALLING_BLOCK, damage);
            }
        } finally {
            if (isAnvil) {
                DamageSource.ANVIL = source;
            } else {
                DamageSource.FALLING_BLOCK = source;
            }
        }
    }
}
