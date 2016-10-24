package org.spongepowered.common.event.tracking.phase.packet.drag;

import org.spongepowered.common.event.tracking.phase.packet.PacketPhase;

public abstract class DragInventoryStopState extends NamedInventoryState {

    public DragInventoryStopState(String name, int buttonId) {
        super(name, PacketPhase.MODE_DRAG | buttonId | PacketPhase.DRAG_STATUS_STOPPED | PacketPhase.CLICK_OUTSIDE_WINDOW, PacketPhase.MASK_DRAG);
    }

}
