package org.spongepowered.common.event.tracking.phase.generation;

import net.minecraft.world.WorldServer;
import org.spongepowered.api.world.World;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

public class GenerationContext<G extends GenerationContext<G>> extends PhaseContext<G> {

    private World world;

    GenerationContext(IPhaseState<? extends G> state) {
        super(state);
    }

    @SuppressWarnings("unchecked")
    public G world(net.minecraft.world.World world) {
        this.world = (World) world;
        return (G) this;
    }

    @SuppressWarnings("unchecked")
    public G world(World world) {
        this.world = world;
        return (G) this;
    }

    @SuppressWarnings("unchecked")
    public G world(IMixinWorldServer world) {
        this.world = world.asSpongeWorld();
        return (G) this;
    }

    @SuppressWarnings("unchecked")
    public G world(WorldServer worldServer) {
        this.world = (World) worldServer;
        return (G) this;
    }

    public World getWorld() {
        return this.world;
    }
}
