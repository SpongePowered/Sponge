package org.spongepowered.common.event.tracking.phase.packet.drag;

import org.spongepowered.common.event.tracking.phase.packet.PacketPhase;

public final class DragInventoryStartState extends NamedInventoryState {

    public DragInventoryStartState(String name, int buttonId) {
        super(name, PacketPhase.MODE_DRAG | buttonId | PacketPhase.DRAG_STATUS_STARTED | PacketPhase.CLICK_OUTSIDE_WINDOW, PacketPhase.MASK_DRAG);
    }

}
