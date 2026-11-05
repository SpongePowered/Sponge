package org.spongepowered.common.world.portal;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.portal.Portal;
import org.spongepowered.api.world.portal.PortalLogic;
import org.spongepowered.api.world.server.ServerLocation;

public enum NoOpPortalTeleportBehavior implements PortalLogic.TeleportBehavior {
    INSTANCE;

    @Override
    public ServerLocation spawnLocation(ServerLocation from, ServerLocation to, Entity entity) {
        return to;
    }

}
