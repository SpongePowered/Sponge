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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.context.transaction.type.NoOpTransactionType;
import org.spongepowered.common.util.PrettyPrinter;

import java.util.Optional;
import java.util.function.BiConsumer;

@DefaultQualifier(NonNull.class)
public final class WrapperTransaction<T extends Event & Cancellable> extends GameTransaction<T> {

    public WrapperTransaction() {
        super(new NoOpTransactionType<>(true, "wrapper"));
    }

    @Override
    public Optional<BiConsumer<PhaseContext<@NonNull ?>, CauseStackManager.StackFrame>> getFrameMutator(
        @Nullable final GameTransaction<@NonNull ?> parent) {
        return Optional.empty();
    }

    @Override
    public void addToPrinter(final PrettyPrinter printer) {

    }

    @Override
    public Optional<T> generateEvent(final PhaseContext<@NonNull ?> context,
        @Nullable final GameTransaction<@NonNull ?> parent,
        final ImmutableList<GameTransaction<T>> gameTransactions, final Cause currentCause) {
        return Optional.empty();
    }

    @Override
    public void restore(final PhaseContext<@NonNull ?> context, final T event) {

    }

    @Override
    public boolean markCancelledTransactions(final T event,
        final ImmutableList<? extends GameTransaction<T>> gameTransactions) {
        return true;
    }

    @Override protected boolean shouldBuildEventAndRestartBatch(final GameTransaction<@NonNull ?> pointer,
        final PhaseContext<@NonNull ?> context) {
        return super.shouldBuildEventAndRestartBatch(pointer, context);
    }
}
