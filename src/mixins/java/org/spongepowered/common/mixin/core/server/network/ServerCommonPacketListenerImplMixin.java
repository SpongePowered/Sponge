package org.spongepowered.common.mixin.core.server.network;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin {
    @Shadow @Final protected Connection connection;
    @Shadow @Final protected MinecraftServer server;
    @Shadow public abstract void shadow$send(final Packet<?> $$0, @org.jetbrains.annotations.Nullable final PacketSendListener $$1);

    @Inject(method = "handleResourcePackResponse", at = @At("HEAD"))
    public void impl$handleResourcePackResponse(final ServerboundResourcePackPacket packet, final CallbackInfo callbackInfo) {
        PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListener) this, this.server);
    }
}
