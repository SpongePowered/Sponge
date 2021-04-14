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

import static org.spongepowered.api.data.persistence.DataQuery.of;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.StringDataFormat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public final class JsonDataFormat implements StringDataFormat {

    public static final String ARRAYTYPE = "_arraytype";
    public static final String VALUE = "value";
    public static final String BYTE = "byte";
    public static final String INT = "int";
    public static final String LONG = "long";

    public static DataContainer serialize(Gson gson, Object o) throws IOException {
        DataViewJsonWriter writer = new DataViewJsonWriter();
        gson.toJson(o, o.getClass(), writer);
        return writer.getResult();
    }

    @Override
    public DataContainer read(String input) throws IOException {
        return this.readFrom(new StringReader(input));
    }

    @Override
    public DataContainer readFrom(Reader input) throws InvalidDataException, IOException {
        try (JsonReader reader = new JsonReader(input)) {
            return JsonDataFormat.readFrom(reader);
        }
    }

    @Override
    public DataContainer readFrom(InputStream input) throws IOException {
        try (JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)))) {
            return JsonDataFormat.readFrom(reader);
        }
    }

    private static DataContainer readFrom(JsonReader reader) throws IOException {
        return JsonDataFormat.createContainer(reader);
    }

    private static DataContainer createContainer(JsonReader reader) throws IOException {
        DataContainer container = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);
        JsonDataFormat.readView(reader, container);
        return container;
    }

    private static void readView(JsonReader reader, DataView view) throws IOException {
        reader.beginObject();

        while (reader.hasNext()) {
            DataQuery key = of(reader.nextName());

            if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                // Check this early so we don't need to copy the view
                final DataView subView = view.createView(key);
                JsonDataFormat.readView(reader, subView);
                // handle special array types
                subView.getString(DataQuery.of(ARRAYTYPE)).ifPresent(type -> {
                    view.remove(key);
                    view.set(key, JsonDataFormat.readArray(type, subView));
                });
            } else {
                view.set(key, JsonDataFormat.read(reader));
            }
        }

        reader.endObject();
    }

    @Nullable
    private static Object read(JsonReader reader) throws IOException {
        JsonToken token = reader.peek();
        switch (token) {
            case BEGIN_OBJECT:
                final DataContainer container = JsonDataFormat.createContainer(reader);
                // handle special array types
                return container.getString(DataQuery.of(ARRAYTYPE)).map(s -> JsonDataFormat.readArray(s, container)).orElse(container);
            case BEGIN_ARRAY:
                return JsonDataFormat.readArray(reader);
            case BOOLEAN:
                return reader.nextBoolean();
            case NULL:
                reader.nextNull();
                return null;
            case STRING:
                return reader.nextString();
            case NUMBER:
                return JsonDataFormat.readNumber(reader);
            default:
                throw new IOException("Unexpected token: " + token);
        }
    }

    private static Object readArray(String type, DataView container) {
        final Object value = container.get(of(VALUE)).get();
        final List<Object> list = new ArrayList<>();
        if (value instanceof Collection) {
            list.addAll(((Collection<?>) value));
        } else if (value.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(value); i++) {
                list.add(Array.get(value, i));
            }
        }
        switch (type) {
            case INT:
                return list.stream().mapToInt(n -> (int) n).toArray();
            case BYTE:
                final byte[] bytes = new byte[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    bytes[i] = (byte) list.get(i);
                }
                return bytes;
            case LONG:
                return list.stream().mapToLong(n -> (long) n).toArray();
            default:
                throw new IllegalArgumentException("Unknown type " + type);

        }
    }

    private static Number readNumber(JsonReader reader) throws IOException {
        // Similar to https://github.com/zml2008/configurate/blob/master/configurate-gson/src/main/java/ninja/leaping/configurate/gson/GsonConfigurationLoader.java#L113
        // Not sure what's the best way to detect the type of number

        String number = reader.nextString();
        if (number.contains(".")) {
            return Double.parseDouble(number);
        }
        long nextLong = Long.parseLong(number);
        int nextInt = (int) nextLong;
        if (nextInt == nextLong) {
            return nextInt;
        }
        return nextLong;
    }

    private static List<?> readArray(JsonReader reader) throws IOException {
        reader.beginArray();

        List<Object> result = new ArrayList<>();
        while (reader.hasNext()) {
            result.add(JsonDataFormat.read(reader));
        }

        reader.endArray();
        return result;
    }

    @Override
    public void writeTo(OutputStream output, DataView data) throws IOException {
        try (JsonWriter writer = new JsonWriter(new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8)))) {
            JsonDataFormat.writeView(writer, data);
        }
    }

    @Override
    public String write(DataView data) throws IOException {
        StringWriter writer = new StringWriter();
        this.writeTo(writer, data);
        return writer.toString();
    }

    @Override
    public void writeTo(Writer output, DataView data) throws IOException {
        try (JsonWriter writer = new JsonWriter(output)) {
            JsonDataFormat.writeView(writer, data);
        }
    }

    private static void writeView(JsonWriter writer, DataView view) throws IOException {
        writer.beginObject();

        for (Map.Entry<DataQuery, Object> entry : view.values(false).entrySet()) {
            writer.name(entry.getKey().asString('.'));
            JsonDataFormat.write(writer, entry.getValue());
        }

        writer.endObject();
    }

    private static void write(JsonWriter writer, @Nullable Object value) throws IOException {
        if (value == null) {
            writer.nullValue();
        } else if (value instanceof Boolean) {
            writer.value((Boolean) value);
        } else if (value instanceof Number) {
            writer.value((Number) value);
        } else if (value instanceof String) {
            writer.value((String) value);
        } else if (value instanceof Iterable) {
            JsonDataFormat.writeArray(writer, (Iterable<?>) value);
        } else if (value instanceof Map) {
            JsonDataFormat.writeMap(writer, (Map<?, ?>) value);
        } else if (value instanceof DataSerializable) {
            JsonDataFormat.writeView(writer, ((DataSerializable) value).toContainer());
        } else if (value instanceof DataView) {
            JsonDataFormat.writeView(writer, (DataView) value);
        } else if (value instanceof int[]) {
            JsonDataFormat.writeArray(writer, (int[]) value);
        } else if (value instanceof long[]) {
            JsonDataFormat.writeArray(writer, (long[]) value);
        } else if (value instanceof byte[]) {
            JsonDataFormat.writeArray(writer, (byte[]) value);
        } else {
            throw new IllegalArgumentException("Unable to translate object to JSON: " + value);
        }
    }

    private static void writeArray(JsonWriter writer, byte[] array) throws IOException {
        writer.beginObject();
        writer.name(ARRAYTYPE);
        writer.value(BYTE);
        writer.name(VALUE);
        writer.beginArray();
        for (byte value : array) {
            writer.value(value);
        }
        writer.endArray();
        writer.endObject();
    }

    private static void writeArray(JsonWriter writer, int[] array) throws IOException {
        writer.beginObject();
        writer.name(ARRAYTYPE);
        writer.value(INT);
        writer.name(VALUE);
        writer.beginArray();
        for (int value : array) {
            writer.value(value);
        }
        writer.endArray();
        writer.endObject();
    }

    private static void writeArray(JsonWriter writer, long[] array) throws IOException {
        writer.beginObject();
        writer.name(ARRAYTYPE);
        writer.value(LONG);
        writer.name(VALUE);
        writer.beginArray();
        for (long value : array) {
            writer.value(value);
        }
        writer.endArray();
        writer.endObject();
    }

    private static void writeArray(JsonWriter writer, Iterable<?> iterable) throws IOException {
        writer.beginArray();
        for (Object value : iterable) {
            JsonDataFormat.write(writer, value);
        }
        writer.endArray();
    }

    private static void writeMap(JsonWriter writer, Map<?, ?> map) throws IOException {
        writer.beginObject();

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            if (key instanceof DataQuery) {
                key = ((DataQuery) key).asString('.');
            }

            writer.name(key.toString());
            JsonDataFormat.write(writer, entry.getValue());
        }

        writer.endObject();
    }

}
