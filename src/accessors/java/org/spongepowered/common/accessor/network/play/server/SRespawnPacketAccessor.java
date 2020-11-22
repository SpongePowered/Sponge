package org.spongepowered.common.accessor.network.play.server;

import net.minecraft.network.play.server.SRespawnPacket;
import net.minecraft.world.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SRespawnPacket.class)
public interface SRespawnPacketAccessor {

    @Accessor("gameType") void accessor$setGameType(GameType gameType);
}
