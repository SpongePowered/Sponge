package org.spongepowered.common.inventory.menu.handler;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.item.inventory.menu.ClickType;
import org.spongepowered.common.SpongeCatalogType;

public final class SpongeClickType<T> extends SpongeCatalogType implements ClickType<T> {

    public SpongeClickType(final ResourceKey key) {
        super(key);
    }
}
