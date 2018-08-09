/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.registry.type.world;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.teleport.TeleportHelperFilter;
import org.spongepowered.api.world.teleport.TeleportHelperFilters;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.world.teleport.ConfigTeleportHelperFilter;
import org.spongepowered.common.world.teleport.DefaultTeleportHelperFilter;
import org.spongepowered.common.world.teleport.FlyingTeleportHelperFilter;
import org.spongepowered.common.world.teleport.NoPortalTeleportHelperFilter;
import org.spongepowered.common.world.teleport.SurfaceOnlyTeleportHelperFilter;

@RegisterCatalog(TeleportHelperFilters.class)
public class TeleportHelperFilterRegistryModule extends AbstractCatalogRegistryModule<TeleportHelperFilter>
    implements AdditionalCatalogRegistryModule<TeleportHelperFilter> {

    @Override
    public void registerAdditionalCatalog(TeleportHelperFilter extraCatalog) {
        checkNotNull(extraCatalog, "TeleportHelperFilter cannot be null!");
        final CatalogKey key = extraCatalog.getKey();
        checkState(!this.map.containsKey(key),
                "TeleportHelperFilter must have a unique id!");
        this.map.put(key, extraCatalog);
    }

    @Override
    public void registerDefaults() {
        final ConfigTeleportHelperFilter config = new ConfigTeleportHelperFilter();
        register(CatalogKey.minecraft("config"), config);
        register(config);
        final DefaultTeleportHelperFilter defaultTeleporter = new DefaultTeleportHelperFilter();
        register(CatalogKey.minecraft("default"), defaultTeleporter);
        register(defaultTeleporter);
        final FlyingTeleportHelperFilter flying = new FlyingTeleportHelperFilter();
        register(CatalogKey.minecraft("flying"), flying);
        register(flying);
        final NoPortalTeleportHelperFilter noPortal = new NoPortalTeleportHelperFilter();
        register(CatalogKey.minecraft("no_portal"), noPortal);
        register(noPortal);
        final SurfaceOnlyTeleportHelperFilter surface = new SurfaceOnlyTeleportHelperFilter();
        register(CatalogKey.minecraft("surface_only"), surface);
        register(surface);
    }

}
