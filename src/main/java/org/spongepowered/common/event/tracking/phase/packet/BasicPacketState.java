package org.spongepowered.common.event.tracking.phase.packet;

public class BasicPacketState extends PacketState<BasicPacketContext> {

    @Override
    public BasicPacketContext createContext() {
        return new BasicPacketContext(this);
    }
}
