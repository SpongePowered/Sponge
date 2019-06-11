package org.spongepowered.common.mixin.core.network.play.server;

import net.minecraft.network.play.server.SPacketPlayerListItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SPacketPlayerListItem.class)
public interface AccessorSPacketPlayerListItem {

    @Accessor("players")
    List<SPacketPlayerListItem.AddPlayerData> spongeBridge$getPlayerDatas();

}
