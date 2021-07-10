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
package org.spongepowered.common.event.tracking.context.transaction;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.context.transaction.type.TransactionType;
import org.spongepowered.common.util.PrettyPrinter;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BiConsumer;

@DefaultQualifier(NonNull.class)
public abstract class GameTransaction<E extends Event & Cancellable> {

    private final TransactionType<? extends E> transactionType;
    final ResourceKey worldKey;
    boolean cancelled = false;

    // Children Definitions
    @Nullable LinkedList<ResultingTransactionBySideEffect> sideEffects;

    // LinkedList node definitions
    @Nullable GameTransaction<@NonNull ?> previous;
    @Nullable GameTransaction<@NonNull ?> next;

    GameTransaction(final TransactionType<? extends E> transactionType, final ResourceKey worldKey) {
        this.transactionType = transactionType;
        this.worldKey = worldKey;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", GameTransaction.class.getSimpleName() + "[", "]")
            .toString();
    }

    public TransactionType<? extends E> getTransactionType() {
        return this.transactionType;
    }


    Deque<ResultingTransactionBySideEffect> getEffects() {
        if (this.sideEffects == null) {
            this.sideEffects = new LinkedList<>();
        }
        return this.sideEffects;
    }

    public void addLast(final ResultingTransactionBySideEffect effect) {
        if (this.sideEffects == null) {
            this.sideEffects = new LinkedList<>();
        }
        this.sideEffects.addLast(effect);
    }

    public final boolean hasChildTransactions() {
        return this.sideEffects != null && this.sideEffects.stream().anyMatch(effect -> effect.head != null);
    }

    public final boolean hasAnyPrimaryChildrenTransactions() {
        if (this.sideEffects == null) {
            return false;
        }
        for (final ResultingTransactionBySideEffect sideEffect : this.sideEffects) {
            @Nullable GameTransaction<@NonNull ?> transaction = sideEffect.head;
            while (transaction != null) {
                if (transaction.transactionType.isPrimary() || transaction.hasChildTransactions()) {
                    return true;
                }
                transaction = transaction.next;
            }
        }
        return false;
    }

    public abstract Optional<BiConsumer<PhaseContext<@NonNull ?>, CauseStackManager.StackFrame>> getFrameMutator(
        @Nullable GameTransaction<@NonNull ?> parent
    );

    public abstract void addToPrinter(PrettyPrinter printer);

    public boolean acceptTileRemoval(final @Nullable BlockEntity tileentity) {
        return false;
    }

    public boolean acceptTileAddition(final BlockEntity tileEntity) {
        return false;
    }

    public boolean acceptTileReplacement(final @Nullable BlockEntity existing, final BlockEntity proposed) {
        return false;
    }

    public boolean acceptEntityDrops(final Entity entity) {
        return false;
    }

    public boolean isUnbatchable() {
        return false;
    }

    public abstract Optional<E> generateEvent(
        PhaseContext<@NonNull ?> context,
        @Nullable GameTransaction<@NonNull ?> parent,
        ImmutableList<GameTransaction<E>> transactions,
        Cause currentCause
    );

    void handleEmptyEvent() {
        this.markCancelled();
    }

    public abstract void restore();

    public void markCancelled() {
        this.cancelled = true;
        if (this.sideEffects != null) {
            for (final ResultingTransactionBySideEffect sideEffect : this.sideEffects) {
                if (sideEffect.head != null) {
                    @Nullable GameTransaction<@NonNull ?> node = sideEffect.head;
                    while (node != null) {
                        node.markCancelled();
                        node = node.next;
                    }
                }
            }
        }
    }

    public abstract boolean markCancelledTransactions(E event, ImmutableList<? extends GameTransaction<E>> transactions);

    public void postProcessEvent(final PhaseContext<@NonNull ?> context, final E event) {

    }

    public void markEventAsCancelledIfNecessary(final E event) {
        if (this.cancelled) {
            event.setCancelled(true);
        }
    }

    boolean acceptDrops(final PrepareBlockDropsTransaction transaction) {
        return false;
    }

    boolean shouldBuildEventAndRestartBatch(final GameTransaction<@NonNull ?> pointer, final PhaseContext<@NonNull ?> context) {
        return this.getTransactionType() != pointer.getTransactionType()
            || !this.worldKey.equals(pointer.worldKey);
    }
}
