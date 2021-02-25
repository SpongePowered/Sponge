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

import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.value.Value;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ByteToBooleanContentUpdater implements DataContentUpdater {

    private final int from;
    private final int to;
    private final List<DataQuery> queries;

    public ByteToBooleanContentUpdater(int from, int to, Key<Value<Boolean>>... booleanKeys) {
        this.from = from;
        this.to = to;
        this.queries = Arrays.stream(booleanKeys).map(k -> DataQuery.of(k.getKey().getValue())).collect(Collectors.toList());
    }

    @Override
    public int getInputVersion() {
        return this.from;
    }

    @Override
    public int getOutputVersion() {
        return this.to;
    }

    @Override
    public DataView update(DataView content) {
        for (DataQuery query : this.queries) {
            final byte aByte = content.getByte(query).orElse((byte) 1);
            content.remove(query); // Delete boolean saved as byte
            content.set(query, aByte == 1);
        }
        return content;
    }
}
