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

import com.google.common.collect.Maps;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class WorldPropertyRegistryModule {

    private final Map<String, WorldProperties> worldPropertiesNameMappings = Maps.newHashMap();
    private final Map<UUID, WorldProperties> worldPropertiesUuidMappings = Maps.newHashMap();


    public static WorldPropertyRegistryModule getInstance() {
        return Holder.instance;
    }

    public void registerWorldProperties(WorldProperties properties) {
        this.worldPropertiesUuidMappings.put(properties.getUniqueId(), properties);
        this.worldPropertiesNameMappings.put(properties.getWorldName(), properties);
    }

    public Optional<WorldProperties> getWorldProperties(String name) {
        return Optional.ofNullable(this.worldPropertiesNameMappings.get(checkNotNull(name)));
    }

    public boolean isWorldRegistered(String name) {
        return this.worldPropertiesNameMappings.containsKey(checkNotNull(name));
    }

    public Optional<WorldProperties> getWorldProperties(UUID uuid) {
        return Optional.ofNullable(this.worldPropertiesUuidMappings.get(checkNotNull(uuid)));
    }

    public boolean isWorldRegistered(UUID uuid) {
        return this.worldPropertiesNameMappings.containsKey(checkNotNull(uuid));
    }

    public Collection<WorldProperties> getAllWorldProperties() {
        return Collections.unmodifiableCollection(this.worldPropertiesNameMappings.values());
    }


    private WorldPropertyRegistryModule() { }

    private static final class Holder {

        private static final WorldPropertyRegistryModule instance = new WorldPropertyRegistryModule();
    }

}
