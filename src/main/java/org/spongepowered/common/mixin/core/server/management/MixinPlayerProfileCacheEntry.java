package org.spongepowered.common.mixin.core.server.management;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.interfaces.server.management.IMixinPlayerProfileCacheEntry;

@Mixin(targets = "net/minecraft/server/management/PlayerProfileCache$ProfileEntry")
public abstract class MixinPlayerProfileCacheEntry implements IMixinPlayerProfileCacheEntry {

}
