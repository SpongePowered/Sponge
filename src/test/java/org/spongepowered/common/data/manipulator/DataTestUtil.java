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
package org.spongepowered.common.data.manipulator;

import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.SpongeManipulatorRegistry;
import org.spongepowered.common.data.util.DataProcessorDelegate;
import org.spongepowered.common.data.util.ImplementationRequiredForTest;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class DataTestUtil {

    private DataTestUtil() {}

    static List<Object[]> generateManipulatorTestObjects() throws Exception {
        final Map<Class<? extends DataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>> manipulatorBuilderMap = getBuilderMap();
        final Map<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>> delegateMap = getDelegateMap();
        return delegateMap.entrySet().stream()
                .filter(entry -> isValidForTesting(entry.getKey()))
                .flatMap(entry -> {
                    String name = entry.getKey().getSimpleName();
                    Class<? extends DataManipulator<?, ?>> key = entry.getKey();
                    DataManipulatorBuilder<?, ?> builder = manipulatorBuilderMap.get(key);

                    return Stream.of(new Object[]{name, key, builder, false}, new Object[]{name, key, builder, true});
                })
                .collect(Collectors.toList());
    }

    private static boolean isValidForTesting(Class<?> clazz) {
        return !Modifier.isInterface(clazz.getModifiers()) && !Modifier.isAbstract(clazz.getModifiers())
               && clazz.getAnnotation(ImplementationRequiredForTest.class) == null;
    }

    @SuppressWarnings("unchecked")
    private static Map<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>> getDelegateMap() throws Exception {
        final Field delegateField = SpongeManipulatorRegistry.class.getDeclaredField("dataProcessorDelegates");
        delegateField.setAccessible(true);
        return (Map<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>>) delegateField.get(SpongeManipulatorRegistry.getInstance());

    }

    @SuppressWarnings("unchecked")
    private static Map<Class<? extends DataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>> getBuilderMap() throws Exception {
        final Field builderMap = SpongeDataManager.class.getDeclaredField("builderMap");
        builderMap.setAccessible(true);
        return (Map<Class<? extends DataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>>) builderMap.get(SpongeDataManager.getInstance());
    }

}
