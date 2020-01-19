package org.spongepowered.common.event.tracking.phase.tick;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.lang.ref.WeakReference;

final class ServerTickState extends TickPhaseState<ServerTickState.ServerTickContext> {

    @Override
    public boolean spawnEntityOrCapture(final ServerTickContext context, final Entity entity) {
        return false;
    }

    @Override
    protected ServerTickContext createNewContext(final PhaseTracker tracker) {
        return new ServerTickContext(this, tracker);
    }

    public static class ServerTickContext extends TickContext<ServerTickContext> {

        WeakReference<MinecraftServer> server;

        public ServerTickContext server(final MinecraftServer server) {
            this.server = new WeakReference<>(server);
            return this;
        }


        ServerTickContext(final IPhaseState<? extends ServerTickContext> phaseState, final PhaseTracker tracker) {
            super(phaseState, tracker);
        }
    }
}
