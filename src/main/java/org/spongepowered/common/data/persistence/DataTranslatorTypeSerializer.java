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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Objects;

public final class DataTranslatorTypeSerializer<T> implements TypeSerializer<T> {

    private final DataTranslator<T> dataTranslator;

    private DataTranslatorTypeSerializer(final DataTranslator<T> dataTranslator) {
        this.dataTranslator = Objects.requireNonNull(dataTranslator, "dataTranslator");
    }

    @Override
    public T deserialize(final Type type, final ConfigurationNode value) throws SerializationException {
        try {
            return this.dataTranslator.translate(ConfigurateTranslator.instance().translate(value));
        } catch (final InvalidDataException e) {
            // Since the value in the config node might be null, return null if an error occurs.
            throw new SerializationException(e);
        }
    }

    @Override
    public void serialize(final Type type, final @Nullable T obj, final ConfigurationNode value) throws SerializationException {
        if (obj == null) {
            value.set(null);
        }

        try {
            ConfigurateTranslator.instance().translateDataToNode(value, this.dataTranslator.translate(obj));
        } catch (final InvalidDataException e) {
            throw new SerializationException(value, type, "Could not serialize. Data was invalid.", e);
        }

    }

    public static <T> DataTranslatorTypeSerializer<T> from(final DataTranslator<T> dataTranslator) {
        return new DataTranslatorTypeSerializer<>(dataTranslator);
    }

}
