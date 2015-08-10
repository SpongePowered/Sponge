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

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.SpongeDataRegistry;

import com.google.common.base.Optional;

@SuppressWarnings("unchecked")
public class DataUtil {

    public static DataView checkDataExists(final DataView dataView, final DataQuery query) throws InvalidDataException {
        if (!checkNotNull(dataView).contains(checkNotNull(query))) {
            throw new InvalidDataException("Missing data for query: " + query.asString('.'));
        } else {
            return dataView;
        }
    }

    public static <T> T getData(final DataView dataView, final Key<? extends BaseValue<T>> key) {
        checkDataExists(dataView, checkNotNull(key).getQuery());
        final Object object = dataView.get(key.getQuery()).get();
        return (T) object;
    }

    public static <T> T getData(final DataView dataView, final Key<?> key, Class<T> clazz) {
        checkDataExists(dataView, checkNotNull(key).getQuery());
        final Object object = dataView.get(key.getQuery()).get();
        if (clazz.isInstance(object)) {
            return (T) object;
        } else {
            throw new InvalidDataException("Could not cast to the correct class type!");
        }
    }

    public static <T> T getData(final DataView dataVew, final DataQuery query, Class<T> data) throws InvalidDataException {
        checkDataExists(dataVew, query);
        final Object object = dataVew.get(query).get();
        if (data.isInstance(object)) {
            return (T) object;
        } else {
            throw new InvalidDataException("Data does not match!");
        }
    }

    // These two methods are provided because Eclipse can't handle the generic capture bounds in MixinDataHolder#offer(DataManipulator<?, ?>) and MixinDataHolder#offer(DataManipulator<?, ?>, MergeFunction)

    public static <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> DataTransactionResult offerWildcard(
        DataManipulator<?, ?> manipulator, DataHolder dataHolder) {
        final Optional<DataProcessor<T, I>> optional = SpongeDataRegistry.getInstance().getProcessor((Class<T>) manipulator.getClass());
        if (optional.isPresent()) {
            return optional.get().set(dataHolder, (T) manipulator);
        }
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    public static <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> DataTransactionResult offerWildcard(
        DataManipulator<?, ?> manipulator, DataHolder dataHolder, MergeFunction function) {
        final Optional<DataProcessor<T, I>> optional = SpongeDataRegistry.getInstance().getProcessor((Class<T>) manipulator.getClass());
        if (optional.isPresent()) {
            return optional.get().set(dataHolder, (T) manipulator, checkNotNull(function));
        }
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    // These two methods below are because OpenJDK6 Can't handle the two methods above when being passed DataManipulator<?, ?>

    @SuppressWarnings("rawtypes")
    public static DataTransactionResult offerPlain(DataManipulator manipulator, DataHolder dataHolder) {
        final Optional<DataProcessor> optional = SpongeDataRegistry.getInstance().getWildDataProcessor(manipulator.getClass());
        if (optional.isPresent()) {
            return optional.get().set(dataHolder, manipulator);
        }
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    @SuppressWarnings("rawtypes")
    public static DataTransactionResult offerPlain(DataManipulator manipulator, DataHolder dataHolder, MergeFunction function) {
        final Optional<DataProcessor> optional = SpongeDataRegistry.getInstance().getWildDataProcessor(manipulator.getClass());
        if (optional.isPresent()) {
            return optional.get().set(dataHolder, manipulator, checkNotNull(function));
        }
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }
}
