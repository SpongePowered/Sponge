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
package org.spongepowered.common.bridge.data;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.SpongeDataManager;

import java.util.List;
import java.util.Optional;

public interface CustomDataHolderBridge {

    void bridge$clearCustomData();

    default <E> Optional<E> bridge$getCustom(Key<? extends Value<E>> key) {
        return this.bridge$getManipulator().get(key);
    }

    default <E> DataTransactionResult bridge$offerCustom(Key<? extends Value<E>> key, E value) {
        final DataManipulator.Mutable manipulator = this.bridge$getManipulator();
        final Value.Immutable<E> immutableValue = manipulator.getValue(key).map(Value::asImmutable).orElse(null);
        final DataTransactionResult.Builder builder = DataTransactionResult.builder();
        if (immutableValue != null) {
            builder.replace(immutableValue);
        }
        manipulator.set(key, value);
        builder.success(manipulator.getValue(key).get().asImmutable());

        SpongeDataManager.getInstance().syncCustomToTag(this);

        return builder.result(DataTransactionResult.Type.SUCCESS).build();
    }

    default <E> DataTransactionResult bridge$removeCustom(Key<? extends Value<E>> key) {
        final Optional<? extends Value<E>> value = this.bridge$getManipulator().getValue(key);
        return value.map(Value::asImmutable).map(DataTransactionResult::successRemove)
                .orElseGet(DataTransactionResult::successNoData);
    }

    DataManipulator.Mutable bridge$getManipulator();

    default void bridge$addFailedData(ImmutableList<DataView> failedData) {
        this.bridge$getFailedData().addAll(failedData);
        SpongeDataManager.getInstance().syncCustomToTag(this);
    }

    List<DataView> bridge$getFailedData();
}
