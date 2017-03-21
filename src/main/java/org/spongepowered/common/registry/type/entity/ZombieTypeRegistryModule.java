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
package org.spongepowered.common.registry.type.entity;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.type.SpongeZombieType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("deprecation")
public class ZombieTypeRegistryModule implements AlternateCatalogRegistryModule<org.spongepowered.api.data.type.ZombieType> {

    public static ZombieTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(org.spongepowered.api.data.type.ZombieTypes.class)
    public final Map<String, org.spongepowered.api.data.type.ZombieType> types = Maps.newHashMap();

    @Override
    public void registerDefaults() {
        this.types.put("minecraft:normal", new SpongeZombieType("minecraft:normal", "Zombie"));
        this.types.put("minecraft:villager", new SpongeZombieType("minecraft:villager", "ZombieVillager"));
        this.types.put("minecraft:husk", new SpongeZombieType("minecraft:husk", "Husk"));
    }

    @Override
    public Optional<org.spongepowered.api.data.type.ZombieType> getById(String id) {
        if (!id.contains(":")) {
            id = "minecraft:" + id;
        }
        return Optional.ofNullable(this.types.get(id));
    }

    @Override
    public Collection<org.spongepowered.api.data.type.ZombieType> getAll() {
        return ImmutableSet.copyOf(this.types.values());
    }

    private ZombieTypeRegistryModule() {
    }

    @Override
    public Map<String, org.spongepowered.api.data.type.ZombieType> provideCatalogMap() {
        final HashMap<String, org.spongepowered.api.data.type.ZombieType> map = new HashMap<>();
        for (Map.Entry<String, org.spongepowered.api.data.type.ZombieType> entry : this.types.entrySet()) {
            map.put(entry.getKey().replace("minecraft:", ""), entry.getValue());
        }
        return map;
    }

    static final class Holder {
        static final ZombieTypeRegistryModule INSTANCE = new ZombieTypeRegistryModule();
    }

}
