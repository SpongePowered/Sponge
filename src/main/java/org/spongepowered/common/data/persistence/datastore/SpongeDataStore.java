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
package org.spongepowered.common.data.persistence.datastore;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataStore;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.util.Tuple;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class SpongeDataStore implements DataStore {

    private Map<Key<?>, Tuple<BiConsumer<DataView, ?>, Function<DataView, Optional<?>>>>  queriesByKey;
    private Collection<TypeToken<? extends DataHolder>> tokens;

    public SpongeDataStore(Map<Key<?>, Tuple<BiConsumer<DataView, ?>, Function<DataView, Optional<?>>>> queriesByKey, Collection<TypeToken<? extends DataHolder>> token) {
        this.queriesByKey = queriesByKey;
        this.tokens = token;
    }

    @Override
    public Collection<TypeToken<? extends DataHolder>> getSupportedTokens() {
        return this.tokens;
    }

    @Override
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public DataView serialize(DataManipulator dataManipulator, DataView view) {
        for (Map.Entry<Key<?>, Tuple<BiConsumer<DataView, ?>, Function<DataView, Optional<?>>>> entry : this.queriesByKey.entrySet()) {
            final BiConsumer serializer = entry.getValue().getFirst();
            dataManipulator.get((Key) entry.getKey()).ifPresent(value -> serializer.accept(view, value));
        }
        return view;
    }

    @Override
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public void deserialize(DataManipulator.Mutable dataManipulator, DataView view) {
        for (Map.Entry<Key<?>, Tuple<BiConsumer<DataView, ?>, Function<DataView, Optional<?>>>> entry : this.queriesByKey.entrySet()) {
            final Function<DataView, Optional<?>> deserializer = entry.getValue().getSecond();
            deserializer.apply(view).ifPresent(value -> dataManipulator.set((Key) entry.getKey(), value));
        }
    }

}
