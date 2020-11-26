package org.spongepowered.common.event.lifecycle;

import org.spongepowered.api.Game;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.lifecycle.RegisterWorldEvent;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.common.world.server.SpongeWorldManager;

import java.util.Objects;

public final class RegisterWorldEventImpl extends AbstractLifecycleEvent implements RegisterWorldEvent {

    private final SpongeWorldManager worldManager;

    public RegisterWorldEventImpl(final Cause cause, final Game game, final SpongeWorldManager worldManager) {
        super(cause, game);

        this.worldManager = worldManager;
    }

    @Override
    public boolean register(final ResourceKey key, final WorldArchetype archetype) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(archetype);

        return this.worldManager.registerPendingWorld(key, archetype);
    }
}
