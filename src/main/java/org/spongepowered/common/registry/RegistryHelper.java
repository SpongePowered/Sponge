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
package org.spongepowered.common.registry;

import org.spongepowered.common.SpongeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class RegistryHelper {

    public static boolean mapFields(Class<?> apiClass, Map<String, ?> mapping) {
        return mapFields(apiClass, mapping, null);
    }

    public static boolean mapFields(Class<?> apiClass, Map<String, ?> mapping, Set<String> ignoredFields) {
        return mapFields(apiClass, fieldName -> mapping.get(fieldName.toLowerCase(Locale.ENGLISH)), ignoredFields, false);
    }

    public static boolean mapFields(Class<?> apiClass, Function<String, ?> mapFunction) {
        return mapFields(apiClass, mapFunction, null, false);
    }

    public static boolean mapFields(Class<?> apiClass, Function<String, ?> mapFunction, Set<String> ignoredFields) {
        return mapFields(apiClass, mapFunction, ignoredFields, false);
    }

    public static boolean mapFieldsIgnoreWarning(Class<?> apiClass, Map<String, ?> mapping) {
        return mapFields(apiClass, fieldname -> mapping.get(fieldname.toLowerCase(Locale.ENGLISH)), null, true);
    }

    public static boolean mapFields(Class<?> apiClass, Function<String, ?> mapFunction, Set<String> ignoredFields, boolean ignore) {
        boolean mappingSuccess = true;
        for (Field f : apiClass.getDeclaredFields()) {
            final String fieldName = f.getName();
            if (ignoredFields != null && ignoredFields.contains(fieldName)) {
                continue;
            }
            try {
                Object value = mapFunction.apply(fieldName);
                if (value == null) {
                    // check for minecraft id
                    value = mapFunction.apply("minecraft:" + fieldName);
                }
                if (value == null && !ignore) {
                    SpongeImpl.getLogger().warn("Skipping {}.{}", f.getDeclaringClass().getName(), fieldName);
                    continue;
                }
                f.set(null, value);
            } catch (Exception e) {
                if (!ignore) {
                    SpongeImpl.getLogger().error("Error while mapping {}.{}", f.getDeclaringClass().getName(), fieldName, e);
                }
                mappingSuccess = false;
            }
        }
        return mappingSuccess;
    }

    public static boolean setFactory(Class<?> apiClass, Object factory) {
        try {
            apiClass.getDeclaredField("factory").set(null, factory);
            return true;
        } catch (Exception e) {
            SpongeImpl.getLogger().error("Error while setting factory on {}", apiClass, e);
            return false;
        }
    }

    public static void setFinalStatic(Class<?> clazz, String fieldName, Object newValue) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            Field modifiers = field.getClass().getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(null, newValue);
        } catch (Exception e) {
            SpongeImpl.getLogger().error("Error while setting field {}.{}", clazz.getName(), fieldName, e);
        }
    }
}
