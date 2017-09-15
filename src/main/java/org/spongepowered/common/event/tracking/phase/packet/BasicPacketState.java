package org.spongepowered.common.event.tracking.phase.packet;

public class BasicPacketState extends PacketState<BasicPacketContext> {

    @Override
    public BasicPacketContext start() {
        return new BasicPacketContext(this);
    }
}
