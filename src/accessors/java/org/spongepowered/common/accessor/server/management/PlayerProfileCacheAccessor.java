package org.spongepowered.common.accessor.server.management;

import net.minecraft.server.management.PlayerProfileCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(PlayerProfileCache.class)
public interface PlayerProfileCacheAccessor {

    @Accessor("usernameToProfileEntryMap") Map<String, PlayerProfileCache_ProfileEntryAccessor> accessor$getUsernameToProfileEntryMap();

}
