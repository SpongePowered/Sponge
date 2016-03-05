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
package org.spongepowered.common.registry.type.block;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.type.SpongeSkullType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class SkullTypeRegistryModule implements CatalogRegistryModule<SkullType> {

    @RegisterCatalog(SkullTypes.class)
    private final Map<String, SkullType> skullTypeMap = new LinkedHashMap<>();

    @Override
    public Optional<SkullType> getById(String id) {
        return Optional.ofNullable(this.skullTypeMap.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<SkullType> getAll() {
        return ImmutableList.copyOf(this.skullTypeMap.values());
    }

    @Override
    public void registerDefaults() {
        this.skullTypeMap.put("skeleton", new SpongeSkullType((byte) 0, "skeleton"));
        this.skullTypeMap.put("wither_skeleton", new SpongeSkullType((byte)1, "wither_skeleton"));
        this.skullTypeMap.put("zombie", new SpongeSkullType((byte) 2, "zombie"));
        this.skullTypeMap.put("player", new SpongeSkullType((byte) 3, "player"));
        this.skullTypeMap.put("creeper", new SpongeSkullType((byte) 4, "creeper"));
        this.skullTypeMap.put("ender_dragon", new SpongeSkullType((byte) 5, "ender_dragon"));
    }
}
