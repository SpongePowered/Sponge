package org.spongepowered.common.world.server;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Optional;

public final class SpongeServerLocationBuilder extends AbstractDataBuilder<ServerLocation> {

    public SpongeServerLocationBuilder() {
        super(ServerLocation.class, 1);
    }

    @Override
    protected Optional<ServerLocation> buildContent(final DataView container) throws InvalidDataException {
        final ResourceKey worldKey = container.getResourceKey(Queries.WORLD_KEY)
                .orElseThrow(() -> new InvalidDataException("Missing [" + Queries.WORLD_KEY + "] entry"));
        final int x = container.getInt(Queries.POSITION_X)
                .orElseThrow(() -> new InvalidDataException("Missing [" + Queries.POSITION_X + "] entry"));
        final int y = container.getInt(Queries.POSITION_Y)
                .orElseThrow(() -> new InvalidDataException("Missing [" + Queries.POSITION_Y + "] entry"));
        final int z = container.getInt(Queries.POSITION_Z)
                .orElseThrow(() -> new InvalidDataException("Missing [" + Queries.POSITION_Z + "] entry"));
        return Optional.of(ServerLocation.of(worldKey, x, y, z));
    }
}
