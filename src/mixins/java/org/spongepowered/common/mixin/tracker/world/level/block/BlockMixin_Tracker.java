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
package org.spongepowered.common.mixin.tracker.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.level.block.TrackedBlockBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.transaction.EffectTransactor;
import org.spongepowered.common.util.ReflectionUtil;

@Mixin(Block.class)
public abstract class BlockMixin_Tracker implements TrackedBlockBridge {

    private final boolean tracker$hasNeighborLogicOverridden = ReflectionUtil.isNeighborChangedDeclared(this.getClass());
    private final boolean tracker$hasEntityInsideLogicOverridden = ReflectionUtil.isEntityInsideDeclared(this.getClass());

    @Nullable private static EffectTransactor tracker$effectTransactorForDrops = null;


    @Override
    public boolean bridge$overridesNeighborNotificationLogic() {
        return this.tracker$hasNeighborLogicOverridden;
    }

    @Override
    public boolean bridge$hasEntityInsideLogic() {
        return this.tracker$hasEntityInsideLogicOverridden;
    }

    /**
     * This is a scattering approach to checking that all block spawns being
     * attempted are going to be prevented if the block changes are currently
     * restoring.
     *
     * @author gabizou - August 16th, 2020 - Minecraft 1.14.4
     * @param ci The callback info
     */
    @Inject(
        method = {
            "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V",
            "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;)V",
            "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V"
        },
        at = @At("HEAD"),
        cancellable = true
    )
    private static void tracker$cancelOnBlockRestoration(final CallbackInfo ci) {
        if (Thread.currentThread() == PhaseTracker.SERVER.getSidedThread()) {
            if (PhaseTracker.SERVER.getPhaseContext().isRestoring()) {
                ci.cancel();
            }
        }
    }

    @Inject(
        method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V",
        at = @At("HEAD")
    )
    private static void tracker$captureBlockProposedToBeSpawningDrops(final BlockState state, final Level worldIn,
        final BlockPos pos, final CallbackInfo ci) {
        final PhaseTracker server = PhaseTracker.SERVER;
        if (server.getSidedThread() != Thread.currentThread()) {
            return;
        }
        final PhaseContext<@NonNull ?> context = server.getPhaseContext();
        BlockMixin_Tracker.tracker$effectTransactorForDrops = context.getTransactor()
            .logBlockDrops(context, worldIn, pos, state, null);
    }

    @Inject(
        method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;)V",
        at = @At("HEAD")
    )
    private static void tracker$captureBlockProposedToBeSpawningDrops(
        final BlockState state, final LevelAccessor worldIn,
        final BlockPos pos, final @Nullable BlockEntity tileEntity, final CallbackInfo ci
    ) {
        if (!(worldIn instanceof Level)) {
            return; // In the name of my father, and his father before him, I cast you out!
        }
        final PhaseTracker server = PhaseTracker.SERVER;
        if (server.getSidedThread() != Thread.currentThread()) {
            return;
        }
        final PhaseContext<@NonNull ?> context = server.getPhaseContext();
        BlockMixin_Tracker.tracker$effectTransactorForDrops = context.getTransactor()
            .logBlockDrops(context, (Level) worldIn, pos, state, tileEntity);
    }

    @Inject(
        method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
        at = @At("HEAD")
    )
    private static void tracker$captureBlockProposedToBeSpawningDrops(final BlockState state, final Level worldIn,
        final BlockPos pos, final @Nullable BlockEntity tileEntity, final Entity entity, final ItemStack itemStack,
        final CallbackInfo ci) {
        final PhaseTracker server = PhaseTracker.SERVER;
        if (server.getSidedThread() != Thread.currentThread()) {
            return;
        }
        final PhaseContext<@NonNull ?> context = server.getPhaseContext();
        BlockMixin_Tracker.tracker$effectTransactorForDrops = context.getTransactor()
            .logBlockDrops(context, worldIn, pos, state, tileEntity);
    }


    @Inject(
        method = {
            "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V",
            "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;)V",
            "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V"
        },
        at = @At("TAIL")
    )
    private static void tracker$closeEffectIfCapturing(final CallbackInfo ci) {
        final PhaseTracker server = PhaseTracker.SERVER;
        if (server.getSidedThread() != Thread.currentThread()) {
            return;
        }
        final PhaseContext<@NonNull ?> context = server.getPhaseContext();
        context.getTransactor().completeBlockDrops(BlockMixin_Tracker.tracker$effectTransactorForDrops);
    }
}
