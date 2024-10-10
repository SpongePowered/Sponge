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
package org.spongepowered.common.event.tracking.context.transaction.pipeline;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.context.transaction.EffectTransactor;
import org.spongepowered.common.event.tracking.context.transaction.ResultingTransactionBySideEffect;
import org.spongepowered.common.event.tracking.context.transaction.TransactionalCaptureSupplier;
import org.spongepowered.common.event.tracking.context.transaction.effect.EffectResult;
import org.spongepowered.common.event.tracking.context.transaction.effect.InteractionAtArgs;
import org.spongepowered.common.event.tracking.context.transaction.effect.InteractionUseItemOnBlockEffect;

import java.util.List;
import java.util.Objects;

public final class UseItemOnBlockPipeline {

    private final ServerLevel worldIn;
    private final ServerPlayer player;
    private final InteractionHand hand;
    private final BlockHitResult blockRaytraceResult;
    private final BlockState blockstate;
    private final ItemStack copiedStack;
    private final List<ResultingTransactionBySideEffect<UseItemOnBlockPipeline, ItemInteractionResult, InteractionAtArgs, ItemInteractionResult>> effects;
    private final TransactionalCaptureSupplier transactor;


    public UseItemOnBlockPipeline(ServerLevel worldIn,
                                  ServerPlayer playerIn,
                                  InteractionHand handIn,
                                  BlockHitResult blockRaytraceResultIn,
                                  BlockState blockstate,
                                  ItemStack copiedStack,
                                  final TransactionalCaptureSupplier transactor) {

        this.worldIn = worldIn;
        this.player = playerIn;
        this.hand = handIn;
        this.blockRaytraceResult = blockRaytraceResultIn;
        this.blockstate = blockstate;
        this.copiedStack = copiedStack;
        this.effects = List.of(
            new ResultingTransactionBySideEffect<>(InteractionUseItemOnBlockEffect.getInstance())
        );
        this.transactor = transactor;
    }

    public ItemInteractionResult processInteraction(PhaseContext<?> context) {
        var interaction = ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        for (final var effect : this.effects) {
            try (final EffectTransactor ignored = context.getTransactor().pushEffect(effect)) {
                final InteractionAtArgs args = new InteractionAtArgs(this.worldIn, this.player, this.hand, this.blockRaytraceResult, this.blockstate, this.copiedStack);
                final EffectResult<ItemInteractionResult> result = effect.effect.processSideEffect(
                    this,
                    interaction,
                    args
                );
                if (result.hasResult) {
                    final @Nullable ItemInteractionResult resultingState = result.resultingState;
                    interaction = Objects.requireNonNullElse(resultingState, interaction);
                }
            }
        }
        return interaction;
    }

    public TransactionalCaptureSupplier transactor() {
        return this.transactor;
    }
}
