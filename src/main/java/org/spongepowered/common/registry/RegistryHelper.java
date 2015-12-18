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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.common.SpongeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Function;

public class RegistryHelper {

    private static final Logger logger;

    static {
        Logger l;
        try {
            l = SpongeImpl.getLogger();
        } catch (ExceptionInInitializerError | IllegalStateException e) {
            l = LogManager.getLogger("Sponge"); // Running test suite
        }
        logger = l;
    }

    public static boolean mapFields(Class<?> apiClass, Map<String, ?> mapping) {
        return mapFields(apiClass, fieldName -> mapping.get(fieldName.toLowerCase()));
    }

    public static boolean mapFieldsIgnoreWarning(Class<?> apiClass, Map<String, ?> mapping) {
        return mapFields(apiClass, fieldname -> mapping.get(fieldname.toLowerCase()), true);
    }

    public static boolean mapFields(Class<?> apiClass, Function<String, ?> mapFunction) {
        return mapFields(apiClass, mapFunction, false);
    }

    public static boolean mapFields(Class<?> apiClass, Function<String, ?> mapFunction, boolean ignore) {
        boolean mappingSuccess = true;
        for (Field f : apiClass.getDeclaredFields()) {
            try {
                Object value = mapFunction.apply(f.getName());
                if (value == null && !ignore) {
                    logger.warn("Skipping {}.{}", f.getDeclaringClass().getName(), f.getName());
                    continue;
                }
                f.set(null, value);
            } catch (Exception e) {
                logger.error("Error while mapping {}.{}", f.getDeclaringClass().getName(), f.getName(), e);
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
            logger.error("Error while setting factory on {}", apiClass, e);
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
            logger.error("Error while setting field {}.{}", clazz.getName(), fieldName, e);
        }
    }
}
