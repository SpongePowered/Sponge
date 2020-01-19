package org.spongepowered.common.event.tracking.phase.tick;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.lang.ref.WeakReference;

final class WorldTickState extends TickPhaseState<WorldTickState.WorldTickContext> {

    @Override
    public boolean spawnEntityOrCapture(final WorldTickContext context, final Entity entity) {
        return false;
    }

    @Override
    protected WorldTickContext createNewContext(final PhaseTracker tracker) {
        return new WorldTickContext(this, tracker);
    }

    public static class WorldTickContext extends TickContext<WorldTickContext> {

        WeakReference<ServerWorld> serverWorld;

        public WorldTickContext server(final ServerWorld server) {
            this.serverWorld = new WeakReference<>(server);
            return this;
        }


        WorldTickContext(final IPhaseState<? extends WorldTickContext> phaseState, final PhaseTracker tracker) {
            super(phaseState, tracker);
        }
    }
}
