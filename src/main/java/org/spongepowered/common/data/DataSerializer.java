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
package org.spongepowered.common.data;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.registry.RegistryType;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DataSerializer {

    public static Object serialize(final DataView.SafetyMode safetyMode, final Object value) {
        if (value instanceof DataView) {
            switch (safetyMode) {
                case ALL_DATA_CLONED:
                case CLONED_ON_SET:
                    final MemoryDataView view = new MemoryDataContainer(safetyMode);
                    for (final Map.Entry<DataQuery, Object> entry : ((DataView) value).values(false).entrySet()) {
                        view.set(entry.getKey(), entry.getValue());
                    }
                    return view;
                default:
                    return value;
            }
        }

        if (value instanceof DataSerializable) {
            return ((DataSerializable) value).toContainer();
        }
        final Optional<? extends DataTranslator<?>> translator = SpongeDataManager.INSTANCE.translator(value.getClass());
        if (translator.isPresent()) {
            final DataTranslator serializer = translator.get();
            return serializer.translate(value);
        }

        final Optional<RegistryType<Object>> optRegistryType = SpongeDataManager.INSTANCE.findRegistryTypeFor(value.getClass());
        if (optRegistryType.isPresent()) {
            final ResourceKey valueKey = Sponge.game().registry(optRegistryType.get()).valueKey(value);
            // TODO if we serialize into a DataView - deserialize needs to do it too
            //            final DataView view = this.createView(path);
            //            view.set(DataQuery.of("registryroot"), registry.root());
            //            view.set(DataQuery.of("registrylocation"), registry.location());
            //            view.set(DataQuery.of("valuekey"), valueKey);
            //            view.set(DataQuery.of("scope"), scope);
            return valueKey.toString();
        }
        if (value instanceof ResourceKey) {
            return value.toString();
        }
        if (value instanceof Collection) {
            return DataSerializer.serializeCollection(safetyMode, (Collection<?>) value);
        }
        if (value instanceof Map) {
            return DataSerializer.serializeMap((Map<?, ?>) value);
        }
        if (value.getClass().isArray()) {
            return DataSerializer.serializeArray(safetyMode, value);
        }
        return value;
    }

    private static DataContainer serializeMap(final Map<?, ?> value) {
        final DataContainer map = DataContainer.createNew();
        for (Map.Entry<?, ?> entry : value.entrySet()) {
            final DataQuery mapKey = DataSerializer.serializeMapKey(entry.getKey());
            map.set(mapKey, entry.getValue());
        }
        return map;
    }

    private static ImmutableList<Object> serializeCollection(final DataView.SafetyMode safetyMode, final Collection<?> value) {
        final ImmutableList.Builder<Object> builder = ImmutableList.builder();
        for (final Object object : value) {
            builder.add(DataSerializer.serialize(safetyMode, object));
        }
        return builder.build();
    }

    private static DataQuery serializeMapKey(final Object value) {
        if (value instanceof DataQuery) {
            return ((DataQuery) value);
        }
        if (value.getClass().isEnum()) {
            return DataQuery.of(((Enum<?>) value).name());
        }
        if (value instanceof UUID) {
            return DataQuery.of(value.toString());
        }
        if (value instanceof ResourceKey) {
            return DataQuery.of(value.toString());
        }
        if (value instanceof String) {
            return DataQuery.of(value.toString());
        }
        if (value instanceof Integer) {
            return DataQuery.of(value.toString());
        }
        final Optional<RegistryType<Object>> registryTypeFor = SpongeDataManager.INSTANCE.findRegistryTypeFor(value.getClass());
        if (registryTypeFor.isPresent()) {
            return DataQuery.of(Sponge.game().findRegistry(registryTypeFor.get()).get().valueKey(value).toString());
        }
        throw new UnsupportedOperationException("Unsupported map-key type " + value.getClass());
    }

    @NotNull
    private static Object serializeArray(final DataView.SafetyMode safetyMode, final Object value) {
        switch (safetyMode) {
            case ALL_DATA_CLONED:
            case CLONED_ON_SET:
                break;
            default:
                return value;
        }
        if (value instanceof byte[]) {
            return ArrayUtils.clone((byte[]) value);
        }
        if (value instanceof short[]) {
            return ArrayUtils.clone((short[]) value);
        }
        if (value instanceof int[]) {
            return ArrayUtils.clone((int[]) value);
        }
        if (value instanceof long[]) {
            return ArrayUtils.clone((long[]) value);
        }
        if (value instanceof float[]) {
            return ArrayUtils.clone((float[]) value);
        }
        if (value instanceof double[]) {
            return ArrayUtils.clone((double[]) value);
        }
        if (value instanceof boolean[]) {
            return ArrayUtils.clone((boolean[]) value);
        }
        return ArrayUtils.clone((Object[]) value);
    }
}
