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
package org.spongepowered.common.mixin.tracker.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.common.event.tracking.TrackingUtil;

@Mixin(BoneMealItem.class)
public abstract class BoneMealItemMixin_Tracker {

    /**
     * @author gabizou - March 20th, 2019 - 1.12.2
     * @reason To allow growing to be captured via bonemeal without
     * explicitly attempting to capture the growth, we can enter an
     * alternate phase blockState that will specifically track the block
     * changes as growth, and therefor will be able to throw
     * {@link ChangeBlockEvent.All} without specifying the cases
     * where the phase blockState needs to throw the event during another
     * phases's capturing.
     */
    @WrapOperation(
        method = "growCrop",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/BonemealableBlock;performBonemeal(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/BonemealSource;)V"
        ),
        // Forge and NeoForge grow from their own applyBonemeal instead, see their BoneMealItemMixin_*_Tracker
        require = 0,
        expect = 0
    )
    private static void tracker$wrapGrowWithPhaseEntry(
        final BonemealableBlock block, final ServerLevel level, final RandomSource random, final BlockPos pos, final BlockState state,
        final BonemealSource source, final Operation<Void> original, @Local(argsOnly = true) final ItemStack stack
    ) {
        TrackingUtil.growWithBonemeal(level, pos, state, stack, () -> original.call(block, level, random, pos, state, source));
    }
}
