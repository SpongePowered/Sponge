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

package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class ImmutableCatalogRegistryModule<T extends CatalogType> implements AlternateCatalogRegistryModule<T> {

    private final ImmutableMap<String, T> mapping = build();
    private final Function<String, String> keyTransformer;

    public ImmutableCatalogRegistryModule() {
        this.keyTransformer = (input) -> input;
    }

    public ImmutableCatalogRegistryModule(Function<String, String> keyTransformer) {
        this.keyTransformer = checkNotNull(keyTransformer, "keyTransformer");
    }

    protected ImmutableMap<String, T> internalMapping() {
        return this.mapping;
    }

    @Override
    public final Optional<T> getById(String id) {
        return Optional.ofNullable(this.mapping.get(mapId(checkNotNull(id, "id"))));
    }

    private String mapId(String id) {
        return this.keyTransformer.apply(id.toLowerCase());
    }

    @Override
    public final Collection<T> getAll() {
        return ImmutableSet.copyOf(this.mapping.values());
    }

    @Override
    public Map<String, T> provideCatalogMap() {
        return this.mapping;
    }

    protected abstract void collect(BiConsumer<String, T> consumer);

    private ImmutableMap<String, T> build() {
        final Map<String, T> aliasMap = Maps.newHashMap();
        final Map<String, T> idMap = Maps.newHashMap();
        final BiConsumer<String, T> consumer = (key, value) -> {
            checkNotNull(key, "key");
            checkNotNull(value, "value");
            checkNotNull(value.getId(), "value's id");
            String lowerKey = value.getId().toLowerCase();
            checkState(!idMap.containsKey(lowerKey) || idMap.get(lowerKey) == value, "An element with this id is already registered!");
            aliasMap.put(key.toLowerCase(), value);
            idMap.put(lowerKey, value);
        };
        collect(consumer);
        // Mapping by id is stronger
        aliasMap.putAll(idMap);
        // Remove invalid names
        aliasMap.remove("");
        return ImmutableMap.copyOf(aliasMap);
    }

    @SuppressWarnings("unchecked")
    protected final void addUnsafe(BiConsumer<String, T> consumer, Object type, String... aliases) {
        add(consumer, (T) type, aliases);
    }

    protected void add(BiConsumer<String, T> consumer, T type, String... aliases) {
        checkNotNull(aliases, "aliases");
        if (aliases.length == 0) {
            consumer.accept(type.getId(), type);
        } else {
            for (String alias : aliases) {
                consumer.accept(alias, type);
            }
        }
    }

    @Override
    public final void registerDefaults() {
    }

}
