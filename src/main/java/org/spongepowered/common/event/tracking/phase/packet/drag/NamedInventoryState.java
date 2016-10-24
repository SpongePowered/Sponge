package org.spongepowered.common.event.tracking.phase.packet.drag;

import org.spongepowered.common.event.tracking.phase.packet.BasicInventoryPacketState;

public abstract class NamedInventoryState extends BasicInventoryPacketState {

    private final String name;

    public NamedInventoryState(String name, int stateId, int stateMask) {
        super(stateId, stateMask);
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
