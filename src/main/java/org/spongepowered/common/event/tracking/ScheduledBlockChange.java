package org.spongepowered.common.event.tracking;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.world.BlockChangeFlag;

public final class ScheduledBlockChange {

    public final PhaseContext<?> context;
    public final BlockPos pos;
    public final BlockState state;
    public final BlockChangeFlag flag;

    public ScheduledBlockChange(final PhaseContext<?> context, final BlockPos pos, final BlockState state, final BlockChangeFlag flag) {
        this.context = context;
        this.pos = pos;
        this.state = state;
        this.flag = flag;
    }
}
