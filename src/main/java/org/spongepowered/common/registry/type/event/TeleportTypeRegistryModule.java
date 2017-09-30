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
package org.spongepowered.common.registry.type.event;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.event.cause.entity.teleport.TeleportType;
import org.spongepowered.api.event.cause.entity.teleport.TeleportTypes;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.type.SpongeTeleportType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class TeleportTypeRegistryModule implements AlternateCatalogRegistryModule<TeleportType>, AdditionalCatalogRegistryModule<TeleportType> {

    public static TeleportTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(TeleportTypes.class)
    private final Map<String, TeleportType> teleportTypeMappings = new HashMap<>();

    @Override
    public Optional<TeleportType> getById(String id) {
        return Optional.ofNullable(this.teleportTypeMappings.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<TeleportType> getAll() {
        return ImmutableList.copyOf(this.teleportTypeMappings.values());
    }

    @Override
    public void registerAdditionalCatalog(TeleportType extraCatalog) {
        final String id = checkNotNull(extraCatalog).getId();
        final String key = id.toLowerCase(Locale.ENGLISH);
        checkArgument(!key.contains("sponge:"), "Cannot register spoofed teleport type!");
        checkArgument(!key.contains("minecraft:"), "Cannot register spoofed teleport type!");
        checkArgument(!this.teleportTypeMappings.containsKey(key), "Cannot register an already registered TeleportType: %s", key);
        this.teleportTypeMappings.put(key, extraCatalog);

    }

    @Override
    public void registerDefaults() {
        this.teleportTypeMappings.put("minecraft:command", new SpongeTeleportType("minecraft:command", "Command"));
        this.teleportTypeMappings.put("minecraft:entity_teleport", new SpongeTeleportType("minecraft:entity_teleport", "Entity Teleport"));
        this.teleportTypeMappings.put("minecraft:portal", new SpongeTeleportType("minecraft:portal", "Portal"));
        this.teleportTypeMappings.put("sponge:plugin", new SpongeTeleportType("sponge:plugin", "Plugin"));
        this.teleportTypeMappings.put("sponge:unknown", new SpongeTeleportType("sponge:unknown", "Unknown"));
    }

    @Override
    public Map<String, TeleportType> provideCatalogMap() {
        final HashMap<String, TeleportType> map = new HashMap<>();
        for (Map.Entry<String, TeleportType> entry : this.teleportTypeMappings.entrySet()) {
            map.put(entry.getKey().replace("minecraft:", "").replace("sponge:", ""), entry.getValue());
        }
        return map;
    }

    TeleportTypeRegistryModule() {
    }

    private static final class Holder {
        static final TeleportTypeRegistryModule INSTANCE = new TeleportTypeRegistryModule();
    }
}
