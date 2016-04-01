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

import net.minecraft.block.BlockTNT;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.entity.IMixinEntityTNTPrimed;
import org.spongepowered.common.interfaces.entity.explosive.IMixinFusedExplosive;

@Mixin(BlockTNT.class)
public abstract class MixinBlockTNT extends MixinBlock {

    private static final String TARGET_PRIME = "Lnet/minecraft/world/World;spawnEntityInWorld"
            + "(Lnet/minecraft/entity/Entity;)Z";
    private static final String TARGET_PRIME_SOUND = "Lnet/minecraft/world/World;playSoundAtEntity"
            + "(Lnet/minecraft/entity/Entity;Ljava/lang/String;FF)V";
    private static final String TARGET_REMOVE = "Lnet/minecraft/world/World;setBlockToAir"
            + "(Lnet/minecraft/util/BlockPos;)Z";

    private EntityLivingBase igniter;
    private boolean primeCancelled;

    private boolean onRemove(World world, BlockPos pos) {
        boolean removed = !this.primeCancelled && world.setBlockToAir(pos);
        this.primeCancelled = false;
        return removed;
    }

    @Inject(method = "explode", at = @At("INVOKE"))
    public void prePrime(World world, BlockPos pos, IBlockState state, EntityLivingBase igniter, CallbackInfo ci) {
        this.igniter = igniter;
    }

    @Redirect(method = "explode", at = @At(value = "INVOKE", target = TARGET_PRIME))
    public boolean onPrime(World world, Entity tnt) {
        IMixinEntityTNTPrimed mixin = (IMixinEntityTNTPrimed) tnt;
        mixin.setDetonator(this.igniter);
        this.primeCancelled = !mixin.shouldPrime(Cause.of(NamedCause.of(NamedCause.IGNITER, this.igniter)));
        return !this.primeCancelled && world.spawnEntityInWorld(tnt);
    }

    @Redirect(method = "explode", at = @At(value = "INVOKE", target = TARGET_PRIME_SOUND))
    public void onPrimeSound(World world, Entity tnt, String sound, float volume, float pitch) {
        if (!this.primeCancelled) {
            world.playSoundAtEntity(tnt, sound, volume, pitch);
        }
    }

    @Redirect(method = "onBlockDestroyedByExplosion", at = @At(value = "INVOKE", target = TARGET_PRIME))
    public boolean onPrimePostExplosion(World world, Entity tnt) {
        // Called when prime triggered by explosion
        return ((IMixinFusedExplosive) tnt).shouldPrime(Cause.source(DamageTypes.EXPLOSIVE).build())
                && world.spawnEntityInWorld(tnt);
    }

    @Redirect(method = "onBlockAdded", at = @At(value = "INVOKE", target = TARGET_REMOVE))
    public boolean onRemovePostAddded(World world, BlockPos pos) {
        // Called when TNT is placed next to a charge
        return onRemove(world, pos);
    }

    @Redirect(method = "onNeighborBlockChange", at = @At(value = "INVOKE", target = TARGET_REMOVE))
    public boolean onRemovePostCharge(World world, BlockPos pos) {
        // Called when TNT receives charge
        return onRemove(world, pos);
    }

    @Redirect(method = "onBlockActivated", at = @At(value = "INVOKE", target = TARGET_REMOVE))
    public boolean onRemovePostInteract(World world, BlockPos pos) {
        // Called when player manually ignites TNT
        return onRemove(world, pos);
    }

    @Redirect(method = "onEntityCollidedWithBlock", at = @At(value = "INVOKE", target = TARGET_REMOVE))
    public boolean onRemovePostCollision(World world, BlockPos pos) {
        // Called when the TNT is hit with a flaming arrow
        return onRemove(world, pos);
    }

}
