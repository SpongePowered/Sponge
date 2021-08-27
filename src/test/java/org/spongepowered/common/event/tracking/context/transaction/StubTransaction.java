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
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.context.transaction.type.NoOpTransactionType;
import org.spongepowered.common.event.tracking.context.transaction.type.TransactionType;
import org.spongepowered.common.util.PrettyPrinter;

import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BiConsumer;

public class StubTransaction extends GameTransaction<StubEvent> {

    private static final TransactionType<StubEvent> TYPE = new NoOpTransactionType<>(true, "stub");
    private final String name;

    protected StubTransaction() {
        super(StubTransaction.TYPE);
        this.name = "Stub";
    }

    protected StubTransaction(String name) {
        super(StubTransaction.TYPE);
        this.name = name;
    }

    @Override
    public Optional<BiConsumer<PhaseContext<@NonNull ?>, CauseStackManager.StackFrame>> getFrameMutator(
        @Nullable GameTransaction<@NonNull ?> parent
    ) {
        return Optional.empty();
    }

    @Override
    public void addToPrinter(PrettyPrinter printer) {

    }

    @Override
    public Optional<StubEvent> generateEvent(
        PhaseContext<@NonNull ?> context, @Nullable GameTransaction<@NonNull ?> parent,
        ImmutableList<GameTransaction<StubEvent>> gameTransactions, Cause currentCause
    ) {
        return Optional.empty();
    }

    @Override
    public void restore(
        PhaseContext<@NonNull ?> context, StubEvent event
    ) {

    }

    @Override
    public boolean markCancelledTransactions(
        StubEvent event, ImmutableList<? extends GameTransaction<StubEvent>> gameTransactions
    ) {
        return false;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", StubTransaction.class.getSimpleName() + "[", "]")
            .add("name='" + this.name + "'")
            .toString();
    }
}
