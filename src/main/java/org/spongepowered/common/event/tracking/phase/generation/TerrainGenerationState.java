package org.spongepowered.common.event.tracking.phase.generation;

public class TerrainGenerationState extends GeneralGenerationPhaseState.Generic {

    TerrainGenerationState() {
        super("TERRAIN_GENERATION");
    }

    @Override
    public GenericGenerationContext createPhaseContext() {
        return super.createPhaseContext()
            .addCaptures()
            ;
    }
}
