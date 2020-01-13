package org.spongepowered.common.bridge.world;

import net.minecraft.entity.Entity;
import org.spongepowered.common.event.tracking.context.SpongeProxyBlockAccess;

/**
 * A specialized {@link WorldBridge} or {@link ServerWorldBridge}
 * that has extra {@link org.spongepowered.common.event.tracking.PhaseTracker} related
 * methods that otherwise bear no other changes to the game.
 */
public interface TrackedWorldBridge {

    boolean bridge$forceSpawnEntity(Entity entity);

    SpongeProxyBlockAccess bridge$getProxyAccess();
}
