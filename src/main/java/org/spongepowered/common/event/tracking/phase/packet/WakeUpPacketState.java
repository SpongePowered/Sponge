package org.spongepowered.common.event.tracking.phase.packet;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import org.spongepowered.common.event.tracking.PhaseContext;

public class WakeUpPacketState extends BasicPacketState {

    @Override
    public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
        context.addBlockCaptures();
    }

}
