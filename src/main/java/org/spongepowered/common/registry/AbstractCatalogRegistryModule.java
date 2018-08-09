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
package org.spongepowered.common.registry;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractCatalogRegistryModule<C extends CatalogType> implements AlternateCatalogRegistryModule<C> {

    protected final RegistryMapWrapper<C> map;
    protected final String defaultModIdToPrepend;

    protected AbstractCatalogRegistryModule() {
        this("minecraft");
    }

    protected AbstractCatalogRegistryModule(String defaultModIdToPrepend) {
        this.defaultModIdToPrepend = defaultModIdToPrepend;
        this.map = new RegistryMapWrapper<>(new HashMap<>());
    }

    protected AbstractCatalogRegistryModule(Map<CatalogKey, C> map) {
        this.map = new RegistryMapWrapper<>(map);
        this.defaultModIdToPrepend = "minecraft";
    }

    protected AbstractCatalogRegistryModule(Map<CatalogKey, C> map, String defaultModIdToPrepend) {
        this.map = new RegistryMapWrapper<>(map);
        this.defaultModIdToPrepend = defaultModIdToPrepend;
    }

    protected void register(final C value) {
        this.register(value.getKey(), value);
    }

    protected void register(final CatalogKey key, final C value) {
        checkState(!this.map.containsKey(key), "duplicate value %s for key %s", value, key);
        this.map.put(key, value);
    }

    @Override
    public final Optional<C> get(final CatalogKey key) {
        return this.map.getOptional(key);
    }

    @SuppressWarnings("deprecation")
    @Override
    public final Optional<C> getById(final String id) {
        // Here we can check for default prefixes.
        final String key = checkNotNull(id).toLowerCase(Locale.ENGLISH);
        if (!key.contains(":")) {
            return get(CatalogKey.of(this.defaultModIdToPrepend, id));
        }
        return this.map.getOptional(CatalogKey.resolve(id));

    }

    @Override
    public final Collection<C> getAll() {
        return this.map.values().stream().filter(this::filterAll).collect(ImmutableList.toImmutableList());
    }

    protected String marshalFieldKey(String key) {
        return key;
    }

    protected boolean filterAll(C element) {
        return true;
    }

    @Override
    public Map<String, C> provideCatalogMap() {
        return this.map.forCatalogRegistration(this::marshalFieldKey);
    }
}
