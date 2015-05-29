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
package org.spongepowered.common.configuration;

import com.google.common.base.Optional;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.InvalidTypeException;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.translator.ConfigurateTranslator;
import org.spongepowered.api.service.persistence.DataBuilder;
import org.spongepowered.api.service.persistence.SerializationService;
import org.spongepowered.common.Sponge;

/**
 * An implementation of {@link TypeSerializer} so that DataSerializables can be
 * provided in {@link ObjectMapper}-using classes.
 */
public class DataSerializableTypeSerializer implements TypeSerializer {

    @Override
    public boolean isApplicable(TypeToken<?> type) {
        return type.getRawType().isAssignableFrom(DataSerializable.class);
    }

    @Override
    public Object deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        if (!isApplicable(type)) {
            throw new InvalidTypeException(type);
        }

        Optional<SerializationService> serviceOpt = Sponge.getGame().getServiceManager().provide(SerializationService.class);
        if (!serviceOpt.isPresent()) {
            throw new ObjectMappingException("No serialization service is present!");
        }
        @SuppressWarnings("unchecked") // generics are meanies :(.....
        Optional<DataBuilder<?>> builderOpt = (Optional) serviceOpt.get().getBuilder(type.getRawType().asSubclass(DataSerializable.class));

        if (!builderOpt.isPresent()) {
            throw new ObjectMappingException("No data builder is registered for " + type);
        }
        Optional<?> built = builderOpt.get().build(ConfigurateTranslator.instance().translateFrom(value));
        if (!built.isPresent()) {
            throw new ObjectMappingException("Unable to build instance of " + type);
        }
        return built.get();
    }

    @Override
    public void serialize(TypeToken<?> type, Object obj, ConfigurationNode value) throws ObjectMappingException {
        if (!isApplicable(type)) {
            throw new InvalidTypeException(type);
        }

        DataContainer container = ((DataSerializable) obj).toContainer();
        value.setValue(ConfigurateTranslator.instance().translateData(container));
    }
}
