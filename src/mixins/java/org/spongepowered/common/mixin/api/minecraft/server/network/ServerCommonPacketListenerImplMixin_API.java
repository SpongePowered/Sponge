package org.spongepowered.common.mixin.api.minecraft.server.network;

import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ServerCommonPacketListenerImpl.class, priority = 999)
public abstract class ServerCommonPacketListenerImplMixin_API {
    @Shadow @Final public Connection connection;
    @Shadow @Final public int latency;

    @Shadow public abstract void shadow$disconnect(net.minecraft.network.chat.Component reason);
}
