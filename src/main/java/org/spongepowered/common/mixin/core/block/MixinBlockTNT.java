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

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.util.StaticMixinHelper;

import java.util.List;

import javax.annotation.Nullable;

@Mixin(BlockTNT.class)
public abstract class MixinBlockTNT extends MixinBlock {

    private boolean blockRemovalEnabled = false;
    @Nullable private User blockOwner;
    @Nullable private User blockNotifier;
    private boolean fireFlag = false;

    @Inject(method = "onBlockAdded", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockTNT;onBlockDestroyedByPlayer"
            + "(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;)V", shift = At.Shift.BEFORE), cancellable = true)
    public void processBlockAddedCauseCapture(World world, BlockPos pos, IBlockState state, CallbackInfo ci) {
        // We capture the block owner and notifier here so that it can be used in
        // the cause we create at a later time.
        IMixinChunk chunk = ((IMixinChunk) world.getChunkFromBlockCoords(pos));
        this.blockOwner = chunk.getBlockOwner(pos).orElse(null);
        this.blockNotifier = chunk.getBlockNotifier(StaticMixinHelper.powerProvidingBlockPos).orElse(null);
        this.fireFlag = true;
    }

    @Redirect(method = "onBlockAdded", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockToAir"
            + "(Lnet/minecraft/util/BlockPos;)Z"))
    public boolean processBlockAddedCancellation(World world, BlockPos pos) {
        // Prevent the block in our current position from being set to AIR if the
        // event just posted has been cancelled.
        if (this.blockRemovalEnabled) {
            world.setBlockToAir(pos);
        }

        return true;
    }

    @Inject(method = "onNeighborBlockChange", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockTNT;onBlockDestroyedByPlayer"
            + "(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;)V", shift = At.Shift.BEFORE), cancellable = true)
    public void processNeighourBlockChangeCauseCapture(World world, BlockPos pos, IBlockState state, Block neighborBlock, CallbackInfo ci)  {
        // We capture the block owner and notifier here so that it can be used in
        // the cause we create at a later time.
        IMixinChunk chunk = ((IMixinChunk) world.getChunkFromBlockCoords(pos));
        this.blockOwner = chunk.getBlockOwner(pos).orElse(null);
        this.blockNotifier = chunk.getBlockNotifier(StaticMixinHelper.neighborNotifySourceBlockPos).orElse(null);
        this.fireFlag = true;
    }

    @Redirect(method = "onNeighborBlockChange", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockToAir"
            + "(Lnet/minecraft/util/BlockPos;)Z"))
    public boolean processNeighourBlockChangeCancellation(World world, BlockPos pos)  {
        // Prevent the block in our current position from being set to AIR if the
        // event just posted has been cancelled.
        if (this.blockRemovalEnabled) {
            world.setBlockToAir(pos);
        }

        return true;
    }

    @Inject(method = "onBlockDestroyedByExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityTNTPrimed;<init>"
            + "(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/EntityLivingBase;)V"), cancellable = true)
    public void processBlockDestroyedByExplosion(World world, BlockPos pos, Explosion explosionIn, CallbackInfo ci) {
        if (!this.fireFlag) {
            List<Object> cause = Lists.newArrayList();

            // Reset state
            StaticMixinHelper.explosionCauseBuilderRootSet = false;

            if (this.blockNotifier != null) {
                cause.add(NamedCause.notifier(this.blockNotifier));
                StaticMixinHelper.explosionCauseBuilderRootSet = true;
            }

            if (this.blockOwner != null) {
                cause.add(NamedCause.owner(this.blockOwner));
                StaticMixinHelper.explosionCauseBuilderRootSet = true;
            }

            cause.add(NamedCause.source(this));

            // Reset state
            this.fireFlag = false;

            ExplosionEvent.Pre event = SpongeEventFactory.createExplosionEventPre(Cause.of(cause), (org.spongepowered.api.world.World) world);
            if (SpongeImpl.postEvent(event)) {
                Block.spawnAsEntity(world, pos, new ItemStack((Block) (Object) this));
                ci.cancel();
            } else {
                // Capture cause builder
                StaticMixinHelper.explosionCauseBuilder = cause;
                this.fireFlag = true;
            }
        }
    }

    @Inject(method = "onBlockDestroyedByPlayer", at = @At("HEAD"))
    public void process(World world, BlockPos pos, IBlockState state, CallbackInfo ci) {
        if (!this.fireFlag) {
            List<Object> cause = Lists.newArrayList();

            if (StaticMixinHelper.blockDestroyPlayer != null) {
                cause.add(StaticMixinHelper.blockDestroyPlayer);
            }

            cause.add(NamedCause.source(this));

            ExplosionEvent.Pre event = SpongeEventFactory.createExplosionEventPre(Cause.of(cause), (org.spongepowered.api.world.World) world);
            if (SpongeImpl.postEvent(event)) {
                ci.cancel();
            }
        } else {
            this.fireFlag = false;
        }
    }

    @Inject(method = "explode", at = @At("HEAD"))
    public void enableBlockRemoval(World world, BlockPos pos, IBlockState state, EntityLivingBase igniter, CallbackInfo ci) {
        // Block removals are true by default
        this.blockRemovalEnabled = true;
    }

    @Inject(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityTNTPrimed;<init>(Lnet/minecraft/world/World;"
            + "DDDLnet/minecraft/entity/EntityLivingBase;)V"), cancellable = true)
    public void processPreExplosion(World world, BlockPos pos, IBlockState state, @Nullable EntityLivingBase igniter, CallbackInfo ci) {
        List<Object> cause = Lists.newArrayList();

        // Reset state
        StaticMixinHelper.explosionCauseBuilderRootSet = false;

        if (this.blockNotifier != null) {
            cause.add(NamedCause.notifier(this.blockNotifier));
            StaticMixinHelper.explosionCauseBuilderRootSet = true;
        }

        if (this.blockOwner != null) {
            cause.add(NamedCause.owner(this.blockOwner));
            StaticMixinHelper.explosionCauseBuilderRootSet = true;
        }

        cause.add(NamedCause.source(this));

        // Reset state
        this.blockOwner = null;
        this.blockNotifier = null;

        ExplosionEvent.Pre event = SpongeEventFactory.createExplosionEventPre(Cause.of(cause), (org.spongepowered.api.world.World) world);
        if (SpongeImpl.postEvent(event)) {
            // Disable block removal if the event was cancelled
            this.blockRemovalEnabled = false;
            ci.cancel();
        } else {
            // Capture cause builder
            StaticMixinHelper.explosionCauseBuilder = cause;
        }
    }

    @Redirect(method = "onBlockActivated", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockToAir"
            + "(Lnet/minecraft/util/BlockPos;)Z"))
    public boolean processBlockActivatedCancellation(World world, BlockPos pos) {
        // Prevent the block in our current position from being set to AIR if the
        // event just posted has been cancelled.
        if (this.blockRemovalEnabled) {
            world.setBlockToAir(pos);
        }

        return true;
    }

}
