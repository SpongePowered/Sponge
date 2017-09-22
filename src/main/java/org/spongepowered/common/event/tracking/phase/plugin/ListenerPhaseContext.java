package org.spongepowered.common.event.tracking.phase.plugin;

import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.interfaces.event.forge.IMixinWorldTickEvent;

public class ListenerPhaseContext extends PluginPhaseContext<ListenerPhaseContext> {

    EntityPlayerMP capturePlayer;

    Object object;

    ListenerPhaseContext(IPhaseState<ListenerPhaseContext> state) {
        super(state);
    }

    public ListenerPhaseContext event(Object obj) {
        this.object = obj;
        return this;
    }

    public IMixinWorldTickEvent getTickEvent() {
        return (IMixinWorldTickEvent) this.object;
    }

}
