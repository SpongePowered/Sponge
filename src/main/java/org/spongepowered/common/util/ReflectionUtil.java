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
package org.spongepowered.common.util;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.ClassUtils.isAssignable;

import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.ImmutableDataCachingUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * A handy utility for doing some neat things with generics and reflection.
 * This is primarily used for {@link ImmutableDataCachingUtil} to create
 * {@link ImmutableDataManipulator}s and {@link ImmutableValue}s for caching.
 *
 * <p>Note that this utility is not at all safe to create complex objects
 * that require pre-processing, it's always simpler to just call the
 * constructors.</p>
 */
public final class ReflectionUtil {

    private ReflectionUtil() {}

    public static <T> T createUnsafeInstance(final Class<T> objectClass, Object... args)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        if (args == null) {
            args = new Object[] {null};
        }
        return findConstructor(objectClass, args).newInstance(args);
    }

    public static <T> T createInstance(final Class<T> objectClass, Object... args) {
        checkArgument(!Modifier.isAbstract(objectClass.getModifiers()), "Cannot construct an instance of an abstract class!");
        checkArgument(!Modifier.isInterface(objectClass.getModifiers()), "Cannot construct an instance of an interface!");
        if (args == null) {
            args = new Object[] {null};
        }
        final Constructor<T> ctor = findConstructor(objectClass, args);
        try {
            return ctor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            Sponge.getLogger().error("Couldn't find an appropriate constructor for " + objectClass.getCanonicalName()
            + "with the args: " + Arrays.toString(args), e);
        }
        throw new IllegalArgumentException("Couldn't find an appropriate constructor for " + objectClass.getCanonicalName()
         + "the args: " + Arrays.toString(args));
    }

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> findConstructor(final Class<T> objectClass, Object... args) {
        final Constructor<?>[] ctors = objectClass.getConstructors();
        if (args == null) {
            args = new Object[] {null};
        }
        // labeled loops
        dance:
        for (final Constructor<?> ctor : ctors) {
            final Class<?>[] paramTypes = ctor.getParameterTypes();
            if (paramTypes.length != args.length) {
                continue; // we haven't found the right constructor
            }
            for (int i = 0; i < paramTypes.length; i++) {
                final Class<?> parameter = paramTypes[i];
                if (!isAssignable(args[i] == null ? null : args[i].getClass(), parameter, true)) {
                    continue dance; // continue the outer loop since we didn't find the right one
                }
            }
            // We've found the right constructor, now to actually construct it!
            return (Constructor<T>) ctor;
        }
        throw new IllegalArgumentException("Applicable constructor not found!");
    }

}
