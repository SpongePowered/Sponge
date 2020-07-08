package org.spongepowered.common.event.impl;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;

// Do you see what the brits have forced us to do this time?
// TODO Minecraft 1.14 - Fix generic getters in event-impl-gen
@Deprecated
public final class RegisterCommandEventImpl<C extends CommandRegistrar<?>> implements RegisterCommandEvent<C> {

    private final Cause cause;
    private final Game game;
    private final TypeToken<C> token;
    private final C registrar;

    public RegisterCommandEventImpl(Cause cause, Game game, TypeToken<C> token, C registrar) {
        this.cause = cause;
        this.game = game;
        this.token = token;
        this.registrar = registrar;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    @Override
    public Game getGame() {
        return this.game;
    }

    @Override
    public TypeToken<C> getGenericType() {
        return this.token;
    }

    @Override
    public C getRegistrar() {
        return this.registrar;
    }
}
