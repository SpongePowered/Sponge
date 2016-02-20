package org.spongepowered.common.event.tracking;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

public final class PhaseData {

    public final PhaseContext context;
    public final IPhaseState state;

    public PhaseData(PhaseContext context, IPhaseState state) {
        this.context = checkNotNull(context, "Context cannot be null!");
        this.state = checkNotNull(state, "State cannot be null!");
    }

    public PhaseContext getContext() {
        return this.context;
    }

    public IPhaseState getState() {
        return this.state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PhaseData phaseData = (PhaseData) o;
        return Objects.equals(this.context, phaseData.context) &&
               Objects.equals(this.state, phaseData.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.context, this.state);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
            .add("context", this.context)
            .add("state", this.state)
            .toString();
    }
}
