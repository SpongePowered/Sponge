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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.teleport.TeleportHelperFilter;
import org.spongepowered.api.world.teleport.TeleportHelperFilters;
import org.spongepowered.common.world.teleport.ConfigTeleportHelperFilter;
import org.spongepowered.common.world.teleport.DefaultTeleportHelperFilter;
import org.spongepowered.common.world.teleport.FlyingTeleportHelperFilter;
import org.spongepowered.common.world.teleport.NoPortalTeleportHelperFilter;
import org.spongepowered.common.world.teleport.SurfaceOnlyTeleportHelperFilter;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class TeleportHelperFilterRegistryModule implements AdditionalCatalogRegistryModule<TeleportHelperFilter> {

    @RegisterCatalog(TeleportHelperFilters.class)
    private final Map<String, TeleportHelperFilter> filterMap = Maps.newHashMap();

    private final Map<String, TeleportHelperFilter> idMap = Maps.newHashMap();

    @Override
    public Optional<TeleportHelperFilter> getById(String id) {
        return Optional.ofNullable(this.idMap.get(id.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<TeleportHelperFilter> getAll() {
        return ImmutableList.copyOf(this.idMap.values());
    }

    @Override
    public void registerAdditionalCatalog(TeleportHelperFilter extraCatalog) {
        checkNotNull(extraCatalog, "TeleportHelperFilter cannot be null!");
        checkState(!this.idMap.containsKey(extraCatalog.getId().toLowerCase(Locale.ENGLISH)),
                "TeleportHelperFilter must have a unique id!");
        this.idMap.put(extraCatalog.getId(), extraCatalog);
    }

    @Override
    public void registerDefaults() {
        this.filterMap.put("config", new ConfigTeleportHelperFilter());
        this.filterMap.put("default", new DefaultTeleportHelperFilter());
        this.filterMap.put("flying", new FlyingTeleportHelperFilter());
        this.filterMap.put("no_portal", new NoPortalTeleportHelperFilter());
        this.filterMap.put("surface_only", new SurfaceOnlyTeleportHelperFilter());

        this.filterMap.forEach((key, value) -> this.idMap.put(value.getId().toLowerCase(Locale.ENGLISH), value));
    }

}
