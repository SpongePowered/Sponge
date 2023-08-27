package org.spongepowered.vanilla.mixin.core.server.network;

import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.network.channel.SpongeChannelManager;

@Mixin(value = ServerCommonPacketListenerImpl.class, priority = 999)
public abstract class ServerCommonPacketListenerImplMixin_Vanilla {
    @Shadow @Final protected MinecraftServer server;

    @Inject(method = "handleCustomPayload", at = @At(value = "HEAD"))
    private void vanilla$onHandleCustomPayload(final ServerboundCustomPayloadPacket packet, final CallbackInfo ci) {
        // For some reason, "ServerboundCustomPayloadPacket" is released in the processPacket
        // method of its class, only applicable to this packet, so just retain here.
        // TODO investigate packet.getData().retain();

        final SpongeChannelManager channelRegistry = (SpongeChannelManager) Sponge.channelManager();
        this.server.execute(() -> channelRegistry.handlePlayPayload((EngineConnection) this, packet.payload()));
    }
}
