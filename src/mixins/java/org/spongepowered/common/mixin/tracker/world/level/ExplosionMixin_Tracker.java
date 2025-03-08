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
package org.spongepowered.common.mixin.tracker.world.level;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.event.tracking.BlockChangeFlagManager;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.transaction.effect.AddBlockLootDropsEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.ExplodeBlockEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.SpawnDestructBlocksEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.WorldBlockChangeCompleteEffect;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.WorldPipeline;
import org.spongepowered.common.util.Constants;

import java.util.function.BiConsumer;

@Mixin(Explosion.class)
public abstract class ExplosionMixin_Tracker {

    @WrapOperation(method = "finalizeExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"))
    private void tracker$onPopResource(final Level level, final BlockPos pos, final ItemStack item, final Operation<Void> original) {
        if (level.isClientSide) {
            original.call(level, pos, item);
        }
        // else: This is built into the SpawnDestructBlocksEffect
    }

    @WrapOperation(method = "finalizeExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;onExplosionHit(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Explosion;Ljava/util/function/BiConsumer;)V"))
    private void tracker$onExplosionHit(final BlockState block, final Level level, final BlockPos pos, final Explosion explosion, final BiConsumer<ItemStack, BlockPos> dropConsumer, final Operation<BlockState> original) {
        if (level.isClientSide || explosion.getBlockInteraction() == Explosion.BlockInteraction.TRIGGER_BLOCK) {
            original.call(block, level, pos, explosion, dropConsumer);
            return;
        }

        final PhaseContext<@NonNull ?> context = PhaseTracker.getInstance().getPhaseContext();
        ((TrackedWorldBridge) level).bridge$startBlockChange(pos, Blocks.AIR.defaultBlockState(), 3)
            .ifPresent(builder -> {
                final WorldPipeline build = builder
                    .addEffect(AddBlockLootDropsEffect.getInstance())
                    .addEffect(ExplodeBlockEffect.getInstance())
                    .addEffect(SpawnDestructBlocksEffect.getInstance())
                    .addEffect(WorldBlockChangeCompleteEffect.getInstance())
                    .build();
                build.processEffects(context, block, Blocks.AIR.defaultBlockState(), pos,
                    null,
                    BlockChangeFlagManager.fromNativeInt(3),
                    Constants.World.DEFAULT_BLOCK_CHANGE_LIMIT);
            });
    }
}
