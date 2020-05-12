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

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.CatalogType;
import org.spongepowered.common.SpongeImpl;

import java.util.Optional;

/**
 * A {@link TypeSerializer} implementation that allows CatalogType values to be used in object-mapped classes.
 */
public class CatalogTypeTypeSerializer implements TypeSerializer<CatalogType> {
    public static final TypeToken<CatalogType> TYPE = TypeToken.of(CatalogType.class);
    public static final CatalogTypeTypeSerializer INSTANCE = new CatalogTypeTypeSerializer();

    private CatalogTypeTypeSerializer() {}

    @Override
    public CatalogType deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        Optional<? extends CatalogType> ret = SpongeImpl.getRegistry().getType(type.getRawType().asSubclass(CatalogType.class), value.getString());
        if (!ret.isPresent()) {
            throw new ObjectMappingException("Input '" + value.getValue() + "' was not a valid value for type " + type);
        }
        return ret.get();
    }

    @Override
    public void serialize(TypeToken<?> type, CatalogType obj, ConfigurationNode value) throws ObjectMappingException {
        value.setValue(obj.getId());
    }
}
