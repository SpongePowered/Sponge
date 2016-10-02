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
import org.spongepowered.api.data.type.ZombieType;
import org.spongepowered.api.data.type.ZombieTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.type.SpongeZombieType;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class ZombieTypeRegistryModule implements CatalogRegistryModule<ZombieType> {

    public static ZombieTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(ZombieTypes.class)
    public final Map<String, ZombieType> types = Maps.newHashMap();

    @Override
    public void registerDefaults() {
        this.types.put("normal", new SpongeZombieType("minecraft:normal"));
        this.types.put("villager", new SpongeZombieType("minecraft:villager"));
        this.types.put("husk", new SpongeZombieType("minecraft:husk"));
    }

    @Override
    public Optional<ZombieType> getById(String id) {
        if (!id.contains(":")) {
            id = "minecraft:" + id;
        }
        return Optional.ofNullable(types.get(id));
    }

    @Override
    public Collection<ZombieType> getAll() {
        return ImmutableSet.copyOf(this.types.values());
    }

    private ZombieTypeRegistryModule() {
    }

    static final class Holder {
        static final ZombieTypeRegistryModule INSTANCE = new ZombieTypeRegistryModule();
    }

}
