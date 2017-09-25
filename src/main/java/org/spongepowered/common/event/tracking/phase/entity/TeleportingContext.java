package org.spongepowered.common.event.tracking.phase.entity;

import net.minecraft.world.WorldServer;
import org.spongepowered.common.event.tracking.IPhaseState;

public class TeleportingContext extends EntityContext<TeleportingContext> {

    private WorldServer targetWorld;

    TeleportingContext(
        IPhaseState<? extends TeleportingContext> state) {
        super(state);
    }

    public WorldServer getTargetWorld() {
        return targetWorld;
    }

    public TeleportingContext setTargetWorld(WorldServer targetWorld) {
        this.targetWorld = targetWorld;
        return this;
    }
}
