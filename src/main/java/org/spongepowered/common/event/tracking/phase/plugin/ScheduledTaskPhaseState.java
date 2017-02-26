package org.spongepowered.common.event.tracking.phase.plugin;

import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.block.BlockPhaseState;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhaseState;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;

/**
 * Used for tasks scheduled with both the Sponge scheduler, and the built-in 'scheduled task' system in MinecraftServer
 */
public class ScheduledTaskPhaseState extends PluginPhaseState {

    @Override
    public boolean canSwitchTo(IPhaseState state) {
        return state instanceof BlockPhaseState || state instanceof EntityPhaseState || state == GenerationPhase.State.TERRAIN_GENERATION;
    }

    @Override
    public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
        phaseContext.getCapturedBlockSupplier().ifPresentAndNotEmpty(blocks -> {
            TrackingUtil.processBlockCaptures(blocks, causeTracker, this, phaseContext);
        });
    }

}
