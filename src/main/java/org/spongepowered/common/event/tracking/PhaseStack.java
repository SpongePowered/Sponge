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
package org.spongepowered.common.event.tracking;


import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.util.Preconditions;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Consumer;


/**
 * A simple stack that couples a {@link IPhaseState} and
 * {@link PhaseContext}. As states are pushed, they can likewise
 * be popped and include any contextual data with the {@link PhaseContext}.
 * Note that the {@link PhaseContext} must be marked as {@link PhaseContext#isComplete()},
 * otherwise an {@link IllegalArgumentException} is thrown.
 */
final class PhaseStack {

    private static final int DEFAULT_QUEUE_SIZE = 16;

    private final Deque<PhaseContext<?>> phases;

    PhaseStack() {
        this(PhaseStack.DEFAULT_QUEUE_SIZE);
    }

    private PhaseStack(int size) {
        this.phases = new ArrayDeque<>(size);
    }

    PhaseContext<?> peek() {
        final PhaseContext<?> phase = this.phases.peek();
        return phase == null ? PhaseContext.empty() : phase;
    }

    IPhaseState<?> peekState() {
        final PhaseContext<?> peek = this.phases.peek();
        return peek == null ? GeneralPhase.State.COMPLETE : peek.state;
    }

    PhaseContext<?> peekContext() {
        final PhaseContext<?> peek = this.phases.peek();
        return peek == null ? PhaseContext.empty() : peek;
    }

    PhaseContext<?> pop() {
        return this.phases.pop();
    }

    PhaseStack push(IPhaseState<?> state, PhaseContext<?> context) {
        Objects.requireNonNull(context, "Tuple cannot be null!");
        Preconditions.checkArgument(context.state == state, () -> String.format("Illegal IPhaseState not matching PhaseContext: %s", context));
        Preconditions.checkArgument(context.isComplete(), () -> String.format("Phase context must be complete: %s", context));
        this.phases.push(context);
        return this;
    }

    public void forEach(Consumer<PhaseContext<?>> consumer) {
        this.phases.forEach(consumer);
    }

    public boolean isEmpty() {
        return this.phases.isEmpty();
    }

    public int size() {
        return this.phases.size();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.phases);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final PhaseStack other = (PhaseStack) obj;
        return Objects.equals(this.phases, other.phases);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PhaseStack.class.getSimpleName() + "[", "]")
                .add("phases=" + this.phases)
                .toString();
    }

    /**
     * We basically want to iterate through the phases to determine if there's multiple of one state re-entering
     * when it shouldn't. To do this, we have to build a miniature map based on arrays
     * @param state The phase state to check
     * @param phaseContext The phase context to check against for runaways
     */
    @SuppressWarnings("rawtypes")
    boolean checkForRunaways(IPhaseState<?> state, @Nullable PhaseContext<?> phaseContext) {
        // first, check if the state is expected for re-entrance:
        final int totalCount = this.phases.size();
        if (totalCount < 2) {
            return false;
        }
        // So first, we want to collect all the states into an array as they are pushed to the stack,
        // which means that we should see the re-entrant phase pretty soon.
        final Object[] allContexts = this.phases.toArray();
        // Now we can actually iterate through the array
        for (int index = 0; index < allContexts.length; index++) {
            if (index < allContexts.length - 1) { // We can't go further than the length, cause that's the top of the stack
                final PhaseContext<?> latestContext = (PhaseContext<?>) allContexts[index];
                final IPhaseState<?> latestState = latestContext.state;
                if (latestState == state && latestState == ((PhaseContext<?>) allContexts[index + 1]).state && (phaseContext == null || latestContext.isRunaway(phaseContext))) {
                    // Found a consecutive duplicate and can now print out
                    return true;
                }
            }
        }
        return false;


    }
}
