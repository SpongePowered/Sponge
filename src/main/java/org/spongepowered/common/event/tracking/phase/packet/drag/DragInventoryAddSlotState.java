package org.spongepowered.common.event.tracking.phase.packet.drag;

import org.spongepowered.common.event.tracking.phase.packet.PacketPhase;

public final class DragInventoryAddSlotState extends NamedInventoryState {

    public DragInventoryAddSlotState(String name, int buttonId) {
        super(name, PacketPhase.MODE_DRAG | buttonId | PacketPhase.DRAG_STATUS_ADD_SLOT | PacketPhase.CLICK_INSIDE_WINDOW, PacketPhase.MASK_DRAG);
    }

}
