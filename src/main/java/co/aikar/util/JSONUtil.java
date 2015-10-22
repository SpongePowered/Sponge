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
package co.aikar.util;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides Utility methods that assist with generating JSON Objects
 */
public final class JSONUtil {

    private static final Gson gson = new Gson();

    private JSONUtil() {
    }

    /**
     * Creates a key/value "JSONPair" object
     *
     * @param key
     * @param obj
     * @return
     */
    public static JSONPair pair(String key, Object obj) {
        return new JSONPair(key, obj);
    }

    public static JSONPair pair(long key, Object obj) {
        return new JSONPair(String.valueOf(key), obj);
    }

    /**
     * Creates a new JSON object from multiple JsonPair key/value pairs
     *
     * @param data
     * @return
     */
    public static Map createObject(JSONPair... data) {
        return appendObjectData(new LinkedHashMap(), data);
    }

    /**
     * This appends multiple key/value Obj pairs into a JSON Object
     *
     * @param parent
     * @param data
     * @return
     */
    public static Map appendObjectData(Map parent, JSONPair... data) {
        for (JSONPair JSONPair : data) {
            parent.put(JSONPair.key, JSONPair.val);
        }
        return parent;
    }

    /**
     * This builds a JSON array from a set of data
     *
     * @param data
     * @return
     */
    public static List toArray(Object... data) {
        return Lists.newArrayList(data);
    }

    /**
     * These help build a single JSON array using a mapper function
     *
     * @param collection
     * @param mapper
     * @param <E>
     * @return
     */
    public static <E> JsonArray toArrayMapper(E[] collection, Function<E, Object> mapper) {
        return toArrayMapper(Lists.newArrayList(collection), mapper);
    }

    public static <E> JsonArray toArrayMapper(Iterable<E> collection, Function<E, Object> mapper) {
        JsonArray array = new JsonArray();
        for (E e : collection) {
            Object object = mapper.apply(e);
            if (object != null) {
                array.add(gson.toJsonTree(object));
            }
        }
        return array;
    }

    /**
     * These help build a single JSON Object from a collection, using a mapper
     * function
     *
     * @param collection
     * @param mapper
     * @param <E>
     * @return
     */
    public static <E> Map toObjectMapper(E[] collection, Function<E, JSONPair> mapper) {
        return toObjectMapper(Lists.newArrayList(collection), mapper);
    }

    public static <E> Map toObjectMapper(Iterable<E> collection, Function<E, JSONPair> mapper) {
        Map object = Maps.newLinkedHashMap();
        for (E e : collection) {
            JSONPair JSONPair = mapper.apply(e);
            if (JSONPair != null) {
                object.put(JSONPair.key, JSONPair.val);
            }
        }
        return object;
    }

    /**
     * Simply stores a key and a value, used internally by many methods below.
     */
    public static class JSONPair {

        final String key;
        final Object val;

        JSONPair(String key, Object val) {
            this.key = key;
            this.val = val;
        }
    }

    public static String toJsonString(Object obj) {
        return gson.toJson(obj);
    }
}
