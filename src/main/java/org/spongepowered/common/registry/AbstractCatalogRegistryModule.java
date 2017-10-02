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

import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractCatalogRegistryModule<C extends CatalogType> implements AlternateCatalogRegistryModule<C> {

    protected final RegistryMap<C> map = new RegistryMap<>();

    protected void register(final C value) {
        this.register(value.getKey(), value);
    }

    protected void register(final CatalogKey key, final C value) {
        checkState(!this.map.containsKey(key), "duplicate value for key %s", key);
        this.map.put(value.getKey(), value);
    }

    @Override
    public final Optional<C> get(final CatalogKey key) {
        return this.map.getOptional(key);
    }

    @Override
    public final Optional<C> getById(final String id) {
        return AlternateCatalogRegistryModule.super.getById(id);
    }

    @Override
    public final Collection<C> getAll() {
        return this.map.values();
    }

    @Override
    public final Map<String, C> provideCatalogMap() {
        return this.map.forCatalogRegistration();
    }
}
