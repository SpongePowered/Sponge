package org.spongepowered.common.event.tracking.phase.packet;

public class InventoryPacketContext extends PacketContext<InventoryPacketContext> {
    public InventoryPacketContext(PacketState<? extends InventoryPacketContext> state) {
        super(state);
    }
}
