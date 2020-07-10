package org.spongepowered.common.event.lifecycle;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.Engine;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;

public final class StartingEngineEventImpl<E extends Engine> implements StartingEngineEvent<E> {

    private final Cause cause;
    private final TypeToken<E> genericType;
    private final E engine;
    private final Game game;

    public StartingEngineEventImpl(final Cause cause, final TypeToken<E> genericType, final Game game, final E engine) {
        this.cause = cause;
        this.genericType = genericType;
        this.game = game;
        this.engine = engine;
    }

    @Override
    public E getEngine() {
        return this.engine;
    }

    @Override
    public TypeToken<E> getGenericType() {
        return this.genericType;
    }

    @Override
    public Game getGame() {
        return this.game;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }
}
