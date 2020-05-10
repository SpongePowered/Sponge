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
package org.spongepowered.common.data.persistence;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.InvalidDataException;

public class DataTranslatorTypeSerializer<T> implements TypeSerializer<T> {

    private final DataTranslator<T> dataTranslator;

    private DataTranslatorTypeSerializer(DataTranslator<T> dataTranslator) {
        this.dataTranslator = checkNotNull(dataTranslator, "dataTranslator");
    }

    @Override
    public T deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        try {
            return this.dataTranslator.translate(ConfigurateTranslator.instance().translate(value));
        } catch (InvalidDataException e) {
            // Since the value in the config node might be null, return null if an error occurs.
            throw new ObjectMappingException(e);
        }
    }

    @Override
    public void serialize(TypeToken<?> type, T obj, ConfigurationNode value) throws ObjectMappingException {
        try {
            ConfigurateTranslator.instance().translateDataToNode(value, this.dataTranslator.translate(obj));
        } catch (InvalidDataException e) {
            throw new ObjectMappingException("Could not serialize. Data was invalid.", e);
        }

    }

    public static <T> DataTranslatorTypeSerializer<T> from(DataTranslator<T> dataTranslator) {
        return new DataTranslatorTypeSerializer<>(dataTranslator);
    }

}
