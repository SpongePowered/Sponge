package org.spongepowered.common.event.lifecycle;

import io.leangen.geantyref.TypeToken;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.GenericEvent;

public abstract class AbstractLifecycleGenericEvent<T> extends AbstractLifecycleEvent implements GenericEvent<T> {

    protected final TypeToken<T> token;

    public AbstractLifecycleGenericEvent(final Cause cause, final Game game, final TypeToken<T> token) {
        super(cause, game);
        this.token = token;
    }

    @Override
    public final TypeToken<T> getParamType() {
        return this.token;
    }
}
