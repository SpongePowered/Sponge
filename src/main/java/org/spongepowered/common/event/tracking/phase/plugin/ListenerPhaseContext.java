package org.spongepowered.common.event.tracking.phase.plugin;

import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.common.event.tracking.IPhaseState;

public class ListenerPhaseContext extends PluginPhaseContext<ListenerPhaseContext> {

    EntityPlayerMP capturePlayer;

    ListenerPhaseContext(IPhaseState<ListenerPhaseContext> state) {
        super(state);
    }
}
