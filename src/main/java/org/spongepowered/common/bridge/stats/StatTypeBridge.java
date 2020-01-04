package org.spongepowered.common.bridge.stats;

import org.spongepowered.api.CatalogKey;

public interface StatTypeBridge {

    CatalogKey bridge$getKey();

    void bridge$setKey(CatalogKey key);
}
