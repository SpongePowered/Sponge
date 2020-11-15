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
package org.spongepowered.common.config;

import io.leangen.geantyref.GenericTypeReflector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.registry.GameRegistry;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.CatalogType;

import java.lang.reflect.Type;
import java.util.Optional;

import javax.inject.Inject;

/**
 * A {@link TypeSerializer} implementation that allows CatalogType values to be used in object-mapped classes.
 */
public final class CatalogTypeTypeSerializer implements TypeSerializer<CatalogType> {
    public static final Class<CatalogType> TYPE = CatalogType.class;

    private final GameRegistry registry;

    @Inject
    CatalogTypeTypeSerializer(final GameRegistry registry) {
        this.registry = registry;
    }

    @Override
    public CatalogType deserialize(final Type type, final ConfigurationNode value) throws SerializationException {
        final Optional<? extends CatalogType> ret = this.registry.getCatalogRegistry()
                        .get(GenericTypeReflector.erase(type).asSubclass(CatalogType.class), ResourceKey.resolve(value.getString()));
        if (!ret.isPresent()) {
            throw new SerializationException("Input '" + value.raw() + "' was not a valid value for type " + type);
        }
        return ret.get();
    }

    @Override
    public void serialize(final Type type, final @Nullable CatalogType obj, final ConfigurationNode value) throws SerializationException {
        if (obj == null) {
            value.set(null);
        } else {
            value.set(obj.getKey().getFormatted());
        }
    }
}
