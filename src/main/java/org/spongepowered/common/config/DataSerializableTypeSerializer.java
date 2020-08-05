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
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.util.TypeTokens;
import org.spongepowered.common.data.persistence.ConfigurateTranslator;

import java.util.function.Predicate;

/**
 * An implementation of {@link TypeSerializer} so that DataSerializables can be
 * provided in {@link ObjectMapper}-using classes.
 */
public class DataSerializableTypeSerializer implements TypeSerializer<DataSerializable> {
    public static final DataSerializableTypeSerializer INSTANCE = new DataSerializableTypeSerializer();
    public static final TypeToken<DataSerializable> TYPE = TypeToken.of(DataSerializable.class);

    private DataSerializableTypeSerializer() {}

    public static Predicate<TypeToken<DataSerializable>> predicate() {
        // We have a separate type serializer for CatalogTypes, so we explicitly discount them here.
        // See https://github.com/SpongePowered/SpongeCommon/issues/1348
        return x -> TYPE.isSupertypeOf(x)
                && !CatalogTypeTypeSerializer.TYPE.isSupertypeOf(x)
                && !TypeTokens.TEXT_TOKEN.isSupertypeOf(x);
    }

    @Override
    public DataSerializable deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        Class<?> clazz = type.getRawType();
        if (clazz.isAssignableFrom(CatalogType.class)) {
            return (DataSerializable) CatalogTypeTypeSerializer.INSTANCE.deserialize(type, value);
        }
        return Sponge.getDataManager()
                .deserialize(clazz.asSubclass(DataSerializable.class), ConfigurateTranslator.instance().translate(value))
                .orElseThrow(() -> new ObjectMappingException("Could not translate DataSerializable of type: " + clazz.getName()));
    }

    @Override
    public void serialize(TypeToken<?> type, DataSerializable obj, ConfigurationNode value) throws ObjectMappingException {
        if (obj instanceof CatalogType) {
            CatalogTypeTypeSerializer.INSTANCE.serialize(type, (CatalogType) obj, value);
        } else {
            ConfigurateTranslator.instance().translateDataToNode(value, obj.toContainer());
        }
    }
}
