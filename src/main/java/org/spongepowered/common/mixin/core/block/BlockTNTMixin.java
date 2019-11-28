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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.bridge.entity.item.EntityTNTPrimedBridge;
import org.spongepowered.common.bridge.explosives.FusedExplosiveBridge;
import org.spongepowered.common.event.ShouldFire;

import javax.annotation.Nullable;

@Mixin(BlockTNT.class)
public abstract class BlockTNTMixin extends BlockMixin {

    private boolean primeCancelled;

    private boolean impl$onRemove(final World world, final BlockPos pos) {
        final boolean removed = !this.primeCancelled && world.func_175698_g(pos);
        this.primeCancelled = false;
        return removed;
    }

    @Inject(method = "explode",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"),
        locals = LocalCapture.CAPTURE_FAILSOFT,
        cancellable = true
    )
    private void impl$ThrowPrimeAndMaybeCancel(final World worldIn, final BlockPos pos, final IBlockState state, @Nullable
        final EntityLivingBase igniter, final CallbackInfo ci, final EntityTNTPrimed tnt) {
        ((EntityTNTPrimedBridge) tnt).bridge$setDetonator(igniter);
        if (ShouldFire.PRIME_EXPLOSIVE_EVENT_PRE) {
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                if (igniter != null) {
                    frame.addContext(EventContextKeys.IGNITER, (Living) igniter);
                }
                if (!((FusedExplosiveBridge) tnt).bridge$shouldPrime()) {
                    ci.cancel();
                }
            }
        }
    }

    @Inject(
        method = "onExplosionDestroy",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"),
        locals = LocalCapture.CAPTURE_FAILSOFT,
        cancellable = true
    )
    private void impl$CheckIfCanPrimeFromExplosion(
        final World worldIn, final BlockPos pos, final Explosion explosionIn, final CallbackInfo ci, final EntityTNTPrimed tnt) {
        if (ShouldFire.PRIME_EXPLOSIVE_EVENT_PRE) {
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.addContext(EventContextKeys.DAMAGE_TYPE, DamageTypes.EXPLOSIVE);
                if (!((FusedExplosiveBridge) tnt).bridge$shouldPrime()) {
                    ci.cancel();
                }
            }
        }

    }

    @Redirect(method = "onBlockAdded", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockToAir(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean impl$removePostSetAir(final World world, final BlockPos pos) {
        // Called when TNT is placed next to a charge
        return impl$onRemove(world, pos);
    }

    @Redirect(method = "neighborChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockToAir(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean impl$removeNeighbor(final World world, final BlockPos pos) {
        // Called when TNT receives charge
        return impl$onRemove(world, pos);
    }

    @Redirect(method = "onBlockActivated", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z"))
    private boolean impl$removeActivated(final World world, final BlockPos pos, final IBlockState state, final int flag) {
        // Called when player manually ignites TNT
        final boolean removed = !this.primeCancelled && world.func_180501_a(pos, state, flag);
        this.primeCancelled = false;
        return removed;
    }

    @Redirect(method = "onEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockToAir(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean impl$removeonCollide(final World world, final BlockPos pos) {
        // Called when the TNT is hit with a flaming arrow
        return impl$onRemove(world, pos);
    }

}
