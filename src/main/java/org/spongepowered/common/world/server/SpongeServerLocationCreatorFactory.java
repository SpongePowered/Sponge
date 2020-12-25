package org.spongepowered.common.world.server;

import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerLocationCreator;
import org.spongepowered.api.world.server.ServerWorld;

public final class SpongeServerLocationCreatorFactory implements ServerLocationCreator.Factory {

    @Override
    public LocatableBlock of(final ServerWorld world, final int x, final int y, final int z) {
        return new SpongeLocatableBlock(world, x, y, z);
    }
}
