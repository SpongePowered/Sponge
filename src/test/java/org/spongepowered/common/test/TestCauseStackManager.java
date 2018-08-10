package org.spongepowered.common.test;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.common.event.SpongeCauseStackManager;

import java.util.Optional;

@Singleton
public class TestCauseStackManager extends SpongeCauseStackManager {

    @Inject
    private Cause cause;

    @Inject
    private StackFrame frame;

    TestCauseStackManager() {
    }

    @Override
    public Cause getCurrentCause() {
        return this.cause;
    }

    @Override
    public EventContext getCurrentContext() {
        return EventContext.empty();
    }

    @Override
    public CauseStackManager pushCause(Object obj) {
        return this;
    }

    @Override
    public Object popCause() {
        return this.cause;
    }

    @Override
    public void popCauses(int n) {

    }

    @Override
    public Object peekCause() {
        return this.cause;
    }

    @Override
    public StackFrame pushCauseFrame() {
        return this.frame;
    }

    @Override
    public void popCauseFrame(StackFrame handle) {

    }

    @Override
    public <T> CauseStackManager addContext(EventContextKey<T> key, T value) {
        return this;
    }

    @Override
    public <T> Optional<T> getContext(EventContextKey<T> key) {
        return Optional.empty();
    }

    @Override
    public <T> T requireContext(EventContextKey<T> key) {
        return null;
    }

    @Override
    public <T> Optional<T> removeContext(EventContextKey<T> key) {
        return Optional.empty();
    }
}
