package org.spongepowered.common.event.tracking.phase.block;

import net.minecraft.util.math.BlockPos;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;

public final class TileEntityInvalidatingPhaseState extends BlockPhaseState {

    @Override
    public boolean canSwitchTo(IPhaseState<?> state) {
        return true;
    }

    @Override
    public void unwind(GeneralizedContext context) {

    }

    @Override
    public boolean shouldCaptureBlockChangeOrSkip(GeneralizedContext phaseContext,
            BlockPos pos) {
        return false;
    }

    @Override
    public boolean tracksBlockSpecificDrops() {
        return false;
    }

    @Override
    public boolean requiresBlockCapturing() {
        return false;
    }
}
