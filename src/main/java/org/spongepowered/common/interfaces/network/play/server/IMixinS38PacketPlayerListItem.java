package org.spongepowered.common.interfaces.network.play.server;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldSettings;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;

import javax.annotation.Nullable;

public interface IMixinS38PacketPlayerListItem {

    void addEntry(GameProfile profile, int latency, WorldSettings.GameType gameMode, @Nullable IChatComponent displayName);

    void addEntry(TabListEntry entry);

}
