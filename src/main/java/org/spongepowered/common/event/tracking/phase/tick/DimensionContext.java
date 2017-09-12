package org.spongepowered.common.event.tracking.phase.tick;

import net.minecraft.world.WorldServer;
import org.spongepowered.api.world.World;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import javax.annotation.Nullable;

public final class DimensionContext extends TickContext<DimensionContext> {

    @Nullable private World world;

    DimensionContext() {
        super(TickPhase.Tick.DIMENSION);
    }

    public DimensionContext world(WorldServer worldServer) {
        this.world = ((World) worldServer);
        return this;
    }

    public DimensionContext world(IMixinWorldServer worldServer) {
        this.world = ((World) worldServer);
        return this;
    }

    public World getWorld() throws IllegalStateException {
        if (this.world == null) {
            throw new IllegalStateException("Expected to be ticking on a world!");
        }
        return this.world;
    }
}
