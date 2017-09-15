package org.spongepowered.common.event.tracking.phase.plugin;

public class ExplosionContext extends PluginPhaseContext<ExplosionContext> {

    public ExplosionContext() {
        super(PluginPhase.State.CUSTOM_EXPLOSION);
    }
}
