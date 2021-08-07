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

import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.DataUtil;

import java.util.Optional;

/**
 * A pointless but necessary class because the Forge workspace cannot handle calling to default
 * super methods without incompatible class change errors
 */
public final class DataHolderProcessor {

    public static <E> Optional<E> bridge$get(final SpongeDataHolderBridge bridge, Key<? extends Value<E>> key) {
        return bridge.bridge$getManipulator().get(key);
    }

    public static <E> DataTransactionResult bridge$offer(final SpongeDataHolderBridge bridge, final Key<? extends Value<E>> key, final E value) {
        final DataManipulator.Mutable manipulator = bridge.bridge$getManipulator();
        final Value.Immutable<E> immutableValue = manipulator.getValue(key).map(Value::asImmutable).orElse(null);
        final DataTransactionResult.Builder builder = DataTransactionResult.builder();
        if (immutableValue != null) {
            builder.replace(immutableValue);
        }
        manipulator.set(key, value);
        builder.success(manipulator.getValue(key).get().asImmutable());

        DataUtil.syncDataToTag(bridge);

        return builder.result(DataTransactionResult.Type.SUCCESS).build();
    }

    public static <E> DataTransactionResult bridge$remove(final SpongeDataHolderBridge bridge, final Key<? extends Value<E>> key) {
        final DataManipulator.Mutable manipulator = bridge.bridge$getManipulator();
        final Optional<? extends Value<E>> value = manipulator.getValue(key);
        if (value.isPresent()) {
            manipulator.remove(key);
        }
        DataUtil.syncDataToTag(bridge);
        return value.map(Value::asImmutable).map(DataTransactionResult::successRemove)
                .orElseGet(DataTransactionResult::successNoData);
    }

    private DataHolderProcessor() {
    }
}
