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

import static com.google.common.base.Preconditions.checkState;

import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

/**
 * A {@link JsonWriter} that serializes to a {@link DataContainer}
 * instead of a JSON string.
 *
 * Inspired by GSON's {@link com.google.gson.internal.bind.JsonTreeWriter}.
 */
public final class DataViewJsonWriter extends JsonWriter {

    private static final Writer UNWRITABLE_WRITER = new Writer() {
        @Override public void write(char[] buffer, int offset, int counter) {
            throw new AssertionError();
        }
        @Override public void flush() throws IOException {
            throw new AssertionError();
        }
        @Override public void close() throws IOException {
            throw new AssertionError();
        }
    };

    private final List<Object> stack = new ArrayList<>();
    @Nullable private DataQuery pendingKey;
    private DataContainer result = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);

    public DataViewJsonWriter() {
        super(UNWRITABLE_WRITER);
    }

    public DataContainer getResult() throws IOException {
        close();
        return this.result;
    }

    private Object peek() {
        return this.stack.get(this.stack.size() - 1);
    }

    private Object pop() {
        return this.stack.remove(this.stack.size() - 1);
    }

    @SuppressWarnings("unchecked")
    private void put(@Nullable Object value) {
        if (this.pendingKey != null) {
            ((DataView) peek()).set(this.pendingKey, value);
            this.pendingKey = null;
        } else {
            ((List<Object>) peek()).add(value);
        }
    }

    @Override
    public JsonWriter beginArray() {
        List<Object> list = new ArrayList<>();
        put(list);
        this.stack.add(list);
        return this;
    }

    @Override
    public JsonWriter endArray() {
        checkState(!this.stack.isEmpty() && this.pendingKey == null && pop() instanceof List);
        return this;
    }

    @Override
    public JsonWriter beginObject() {
        if (this.stack.isEmpty()) {
            this.stack.add(this.result);
            return this;
        }

        Object parent = peek();
        if (parent instanceof DataView) {
            checkState(this.pendingKey != null);
            ((DataView) parent).createView(this.pendingKey);
            this.pendingKey = null;
            return this;
        }

        put(DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED));
        return this;
    }

    @Override
    public JsonWriter endObject() {
        checkState(!this.stack.isEmpty() && this.pendingKey == null && pop() instanceof DataView);
        return this;
    }

    @Override
    public JsonWriter name(String name) {
        checkState(!this.stack.isEmpty() && this.pendingKey == null && peek() instanceof DataView);
        this.pendingKey = DataQuery.of(name);
        return this;
    }

    @Override
    public JsonWriter value(String value) {
        put(value);
        return this;
    }

    @Override
    public JsonWriter nullValue() {
        put(null);
        return this;
    }

    @Override
    public JsonWriter value(boolean value) {
        put(value);
        return this;
    }

    @Override
    public JsonWriter value(double value) {
        put(value);
        return this;
    }

    @Override
    public JsonWriter value(long value) {
        put(value);
        return this;
    }

    @Override
    public JsonWriter value(Number value) {
        put(value);
        return this;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws IOException {
        if (!this.stack.isEmpty()) {
            throw new IOException("Incomplete document");
        }
    }

}
