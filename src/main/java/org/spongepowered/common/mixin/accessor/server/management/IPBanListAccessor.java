package org.spongepowered.common.mixin.accessor.server.management;

import net.minecraft.server.management.IPBanList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.net.SocketAddress;

@Mixin(IPBanList.class)
public interface IPBanListAccessor {

    @Invoker("addressToString") String accessor$addressToString(SocketAddress address);

}
