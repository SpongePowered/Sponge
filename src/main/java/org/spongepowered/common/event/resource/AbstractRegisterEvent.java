package org.spongepowered.common.event.resource;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Engine;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.GenericEvent;
import org.spongepowered.api.event.impl.AbstractEvent;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractRegisterEvent<E extends Engine, T> extends AbstractEvent implements GenericEvent<E> {

    private final Cause cause;
    private final TypeToken<E> type;
    private final List<T> list = new LinkedList<>();

    public AbstractRegisterEvent(Cause cause, TypeToken<E> type) {
        this.cause = cause;
        this.type = type;
    }

    @Override
    public Cause getCause() {
        return cause;
    }

    @Override
    public TypeToken<E> getGenericType() {
        return type;
    }

    public void register(@NonNull T obj) {
        list.add(Preconditions.checkNotNull(obj));
    }

    protected List<T> getInternalList() {
        return list;
    }
}
