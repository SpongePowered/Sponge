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

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.launch.Launch;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static final Marker STUPID_REFLECTION = MarkerManager.getMarker("REFLECTION_BULLSHIT");
    private static final Class<?>[] NEIGHBOR_CHANGED_METHOD_ARGS = {
        BlockState.class,
        Level.class,
        BlockPos.class,
        Block.class,
        BlockPos.class,
        boolean.class
    };
    private static final Class<?>[] ENTITY_INSIDE_METHOD_ARGS = {
        BlockState.class,
        Level.class,
        BlockPos.class,
        Entity.class
    };
    private static final Class<?>[] STEP_ON_METHOD_ARGS = {
        Level.class,
        BlockPos.class,
        Entity.class
    };

    public static boolean isNeighborChangedDeclared(final Class<?> targetClass) {
        return ReflectionUtil.doesMethodExist(
            targetClass,
            Block.class,
            "neighborChanged",
            ReflectionUtil.NEIGHBOR_CHANGED_METHOD_ARGS
        );
    }

    public static boolean isEntityInsideDeclared(final Class<?> targetClass) {
        return ReflectionUtil.doesMethodExist(
            targetClass,
            BlockBehaviour.class,
            "entityInside",
            ReflectionUtil.ENTITY_INSIDE_METHOD_ARGS
        );
    }

    public static boolean isStepOnDeclared(final Class<?> targetClass) {
        return ReflectionUtil.doesMethodExist(
            targetClass,
            Block.class,
            "stepOn",
            ReflectionUtil.STEP_ON_METHOD_ARGS
        );
    }

    public static boolean doesMethodExist(
        final Class<?> targetClass,
        final Class<?> ignoredClass,
        final String methodName,
        final Class<?>[] methodParameters
    ) {
        final String targetMethodForEnvironment = Launch.instance().developerEnvironment() ? methodName : methodName;
        try {
            final Class<?> declaringClass = targetClass.getMethod(targetMethodForEnvironment, methodParameters).getDeclaringClass();
            return !ignoredClass.equals(declaringClass);
        } catch (final NoSuchMethodException e) {
            SpongeCommon.logger().fatal(ReflectionUtil.STUPID_REFLECTION, "Could not find desired method {} under environment method name {}", methodName, targetMethodForEnvironment);
            return true;
        }

    }

    public static <T> T createUnsafeInstance(final Class<T> objectClass, Object... args)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        if (args == null) {
            args = new Object[] {null};
        }
        final Constructor<T>tConstructor = ReflectionUtil.findConstructor(objectClass, args);
        try {
            return tConstructor.newInstance(args);
        } catch (final Exception e) {
            final Object[] deconstructedArgs = ReflectionUtil.deconstructArray(args).toArray();
            return tConstructor.newInstance(deconstructedArgs);
        }
    }

    public static <T> T createInstance(final Class<T> objectClass, Object... args) {
        checkArgument(!Modifier.isAbstract(objectClass.getModifiers()), "Cannot construct an instance of an abstract class!");
        checkArgument(!Modifier.isInterface(objectClass.getModifiers()), "Cannot construct an instance of an interface!");
        if (args == null) {
            args = new Object[] {null};
        }
        final Constructor<T> ctor = ReflectionUtil.findConstructor(objectClass, args);
        try {
            return ctor.newInstance(args);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            SpongeCommon.logger().error("Couldn't find an appropriate constructor for " + objectClass.getCanonicalName()
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
                for (final Object object : args) {
                    if (object != null) { // hahahah
                        if (object.getClass().isArray()) {
                            final Object[] objects = ReflectionUtil.deconstructArray(args).toArray();
                            return ReflectionUtil.findConstructor(objectClass, objects);
                        }
                    }
                }
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
        throw new IllegalArgumentException("Applicable constructor not found for class: " + objectClass.getCanonicalName() + " with args: " + Arrays.toString(args));
    }

    private static List<Object> deconstructArray(final Object[] objects) {
        final List<Object> list = new ArrayList<>();
        for (final Object object : objects) {
            if (object == null) {
                list.add(null);
                continue;
            }
            if (object.getClass().isArray()) {
                list.addAll(ReflectionUtil.deconstructArray((Object[]) object));
            } else {
                list.add(object);
            }
        }
        return list;
    }

    private ReflectionUtil() {}
}
