package org.spongepowered.common.event.tracking;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

import javax.annotation.Nullable;

public final class CauseStack {

    private final Deque<Tuple<IPhaseState, PhaseContext>> states;

    public CauseStack(int size) {
        this.states = new ArrayDeque<>(size);
    }

    @Nullable
    public IPhaseState peekState() {
        final Tuple<IPhaseState, PhaseContext> peek = this.states.peek();
        return peek == null ? null : peek.getFirst();
    }

    @Nullable
    public PhaseContext peekContext() {
        final Tuple<IPhaseState, PhaseContext> peek = this.states.peek();
        return peek == null ? null : peek.getSecond();
    }

    public Tuple<IPhaseState, PhaseContext> pop() {
        final Tuple<IPhaseState, PhaseContext> tuple = this.states.pop();
        return tuple;
    }

    @Nullable
    public TrackingPhase current() {
        final Tuple<IPhaseState, PhaseContext> tuple = this.states.peek();
        return tuple == null ? null : tuple.getFirst().getPhase();
    }

    public CauseStack push(Tuple<IPhaseState, PhaseContext> tuple) {
        checkNotNull(tuple, "Tuple cannot be null!");
        checkArgument(tuple.getSecond().isComplete(), "Phase context must be complete: %s", tuple);
        this.states.push(tuple);
        return this;
    }

    public CauseStack push(IPhaseState state, PhaseContext context) {
        return push(new Tuple<>(state, context));
    }

    public boolean isEmpty() {
        return this.states.isEmpty();
    }

    public int size() {
        return this.states.size();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.states);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final CauseStack other = (CauseStack) obj;
        return Objects.equals(this.states, other.states);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("states", this.states)
                .toString();
    }

    @Nullable
    public Tuple<IPhaseState, PhaseContext> peek() {
        return this.states.peek();
    }
}
