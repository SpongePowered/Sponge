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
package org.spongepowered.common.data.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class DataUtil {


    public static final DataFixer spongeDataFixer = new DataFixer(Constants.Sponge.SPONGE_DATA_VERSION);
    private static final Supplier<InvalidDataException> INVALID_DATA_EXCEPTION_SUPPLIER = InvalidDataException::new;

    static {
        spongeDataFixer.registerFix(FixTypes.LEVEL, new SpongeLevelFixer());
        spongeDataFixer.registerFix(FixTypes.ENTITY, new EntityTrackedUser());
        spongeDataFixer.registerFix(FixTypes.PLAYER, new PlayerRespawnData());
    }

    @SuppressWarnings("rawtypes")
    public static <T> T getData(final DataView dataView, final Key<? extends Value<T>> key) throws InvalidDataException {
        checkDataExists(dataView, checkNotNull(key).getQuery());
        final Object object;
        final TypeToken<?> elementToken = key.getElementToken();
        // Order matters here
        // We always check DataSerializeable first, since this should override
        // any other handling (e.g. for CatalogTypes)
        if (elementToken.isSubtypeOf(TypeToken.of(DataSerializable.class))) {
            object = dataView.getSerializable(key.getQuery(), (Class<DataSerializable>) elementToken.getRawType())
                .orElseThrow(() -> new InvalidDataException("Missing value for key: " + key.getId()));
        } else if (elementToken.isSubtypeOf(TypeToken.of(CatalogType.class))) {
            object = dataView.getCatalogType(key.getQuery(), (Class<CatalogType>) elementToken.getRawType())
                .orElseThrow(() -> new InvalidDataException("Missing value for key: " + key.getId()));
        } else if (elementToken.isSubtypeOf(TypeToken.of(Text.class))) {
            final String input = dataView.getString(key.getQuery())
                .orElseThrow(() -> new InvalidDataException("Missing value for key: " + key.getId()));
            object = TextSerializers.PLAIN.deserialize(input);
        } else if (elementToken.isSubtypeOf(TypeToken.of(List.class))) {
            final Optional<?> opt;
            if (elementToken.isSubtypeOf(TypeTokens.LIST_DATA_SERIALIZEABLE_TOKEN)) {
                final Class<?> listElement = TypeTokenHelper.getGenericParam(elementToken, 0);
                opt = dataView.getSerializableList(key.getQuery(), (Class) listElement);
            } else {
                opt = dataView.getList(key.getQuery());
            }
            object = opt.orElseThrow(() -> new InvalidDataException("Missing value for key: " + key.getId()));
        } else if (elementToken.isSubtypeOf(TypeToken.of(Set.class))) {
            final List<?> objects = dataView.getList(key.getQuery()).orElse(Collections.emptyList());
            object = new HashSet<Object>(objects);
        } else if (elementToken.isSubtypeOf(TypeToken.of(Map.class))) {
            object = dataView.getMap(key.getQuery()).orElseThrow(() -> new InvalidDataException("Missing value for key: " + key.getId()));
        } else if (elementToken.isSubtypeOf(TypeToken.of(Enum.class))) {
            object = Enum.valueOf((Class<Enum>) elementToken.getRawType(), dataView.getString(key.getQuery())
                .orElseThrow(() -> new InvalidDataException("Missing value for key: " + key.getId())));
        } else {
            final Optional<? extends DataTranslator<?>> translator = SpongeDataManager.getInstance().getTranslator(elementToken.getRawType());
            if (translator.isPresent()) {
                object = translator.map(trans -> trans.translate(dataView.getView(key.getQuery()).orElseThrow(() -> new InvalidDataException("Missing value for key: " + key.getId()))))
                    .orElseThrow(() -> new InvalidDataException("Could not translate translateable: " + key.getId()));
            } else {
                object = dataView.get(key.getQuery())
                    .orElseThrow(() -> new InvalidDataException("Could not translate translateable: " + key.getId()));
            }
        }

        return (T) object;
    }


    public static <T> T getData(final DataView dataView, final Key<?> key, final Class<T> clazz) throws InvalidDataException {
        checkDataExists(dataView, checkNotNull(key).getQuery());
        final Object object = dataView.get(key.getQuery()).orElseThrow(dataNotFound());
        if (clazz.isInstance(object)) {
            return (T) object;
        }
        throw new InvalidDataException("Could not cast to the correct class type!");
    }
}
