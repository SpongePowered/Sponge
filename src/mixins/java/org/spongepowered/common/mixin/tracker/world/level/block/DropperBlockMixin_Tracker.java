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

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;

@Mixin(DropperBlock.class)
public abstract class DropperBlockMixin_Tracker {

    // DropperBlock overrides dispenseFrom, so it needs its own DISPENSE phase, as DispenserBlockMixin_Tracker gives dispensers
    @WrapMethod(method = "dispenseFrom")
    private void tracker$createContextOnDispensing(final ServerLevel worldIn, final BlockState state, final BlockPos pos, final Operation<Void> original) {
        final SpongeBlockSnapshot spongeBlockSnapshot = ((TrackedWorldBridge) worldIn).bridge$createSnapshot(state, pos, BlockChangeFlags.ALL);
        final LevelChunkBridge mixinChunk = (LevelChunkBridge) worldIn.getChunkAt(pos);
        try (final @Nullable PhaseContext<@NonNull ?> context = BlockPhase.State.DISPENSE
                .createPhaseContext(PhaseTracker.getWorldInstance(worldIn))
                .source(spongeBlockSnapshot)
                .creator(() -> mixinChunk.bridge$getBlockCreatorUUID(pos))
                .notifier(() -> mixinChunk.bridge$getBlockNotifierUUID(pos))) {
            context.buildAndSwitch();
            original.call(worldIn, state, pos);
        }
    }
}
