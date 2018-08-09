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

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.event.cause.entity.teleport.TeleportType;
import org.spongepowered.api.event.cause.entity.teleport.TeleportTypes;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.type.SpongeTeleportType;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;

import java.util.Locale;

@RegisterCatalog(TeleportTypes.class)
public final class TeleportTypeRegistryModule extends AbstractCatalogRegistryModule<TeleportType>
    implements AlternateCatalogRegistryModule<TeleportType>, AdditionalCatalogRegistryModule<TeleportType> {

    public static TeleportTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void registerAdditionalCatalog(TeleportType extraCatalog) {
        final String id = checkNotNull(extraCatalog).getKey().toString();
        final String key = id.toLowerCase(Locale.ENGLISH);
        checkArgument(!key.contains("sponge:"), "Cannot register spoofed teleport type!");
        checkArgument(!key.contains("minecraft:"), "Cannot register spoofed teleport type!");
        checkArgument(!this.map.containsKey(extraCatalog.getKey()), "Cannot register an already registered TeleportType: %s", key);
        this.map.put(extraCatalog.getKey(), extraCatalog);

    }

    @Override
    public void registerDefaults() {
        register(CatalogKey.minecraft("command"), new SpongeTeleportType("minecraft:command", "Command"));
        register(CatalogKey.minecraft("entity_teleport"), new SpongeTeleportType("minecraft:entity_teleport", "Entity Teleport"));
        register(CatalogKey.minecraft("portal"), new SpongeTeleportType("minecraft:portal", "Portal"));
        register(CatalogKey.sponge("plugin"), new SpongeTeleportType("sponge:plugin", "Plugin"));
        register(CatalogKey.sponge("unknown"), new SpongeTeleportType("sponge:unknown", "Unknown"));
    }

    TeleportTypeRegistryModule() {
    }

    private static final class Holder {
        static final TeleportTypeRegistryModule INSTANCE = new TeleportTypeRegistryModule();
    }
}
