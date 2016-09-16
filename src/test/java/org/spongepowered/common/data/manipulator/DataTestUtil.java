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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.extra.fluid.FluidTypes;
import org.spongepowered.common.SpongeGame;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.DataRegistrar;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.type.SpongeCommonFluidType;
import org.spongepowered.common.data.util.DataProcessorDelegate;
import org.spongepowered.common.data.util.ImplementationRequiredForTest;
import org.spongepowered.common.registry.type.data.KeyRegistryModule;
import org.spongepowered.common.registry.util.RegistryModuleLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class DataTestUtil {

    private DataTestUtil() {}

    @SuppressWarnings("unchecked")
    static List<Object[]> generateManipulatorTestObjects() throws Exception {
        KeyRegistryModule.getInstance().registerDefaults();
        generateKeyMap();
        setupCatalogTypes();
        SpongeGame mockGame = mock(SpongeGame.class);

        when(mockGame.getDataManager()).thenReturn(SpongeDataManager.getInstance());
        DataRegistrar.setupSerialization(mockGame);
        final List<Object[]> list = new ArrayList<>();

        final Map<Class<? extends DataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>> manipulatorBuilderMap = getBuilderMap();
        final Map<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>> delegateMap = getDelegateMap();
        delegateMap.entrySet().stream().filter(entry -> isValidForTesting(entry.getKey())).forEach(entry -> {
            list.add(new Object[]{entry.getKey().getSimpleName(), entry.getKey(), manipulatorBuilderMap.get(entry.getKey())});
        });
        return list;
    }

    private static boolean isValidForTesting(Class<?> clazz) {
        return !Modifier.isInterface(clazz.getModifiers()) && !Modifier.isAbstract(clazz.getModifiers())
               && clazz.getAnnotation(ImplementationRequiredForTest.class) == null;
    }

    @SuppressWarnings("unchecked")
    private static void generateKeyMap() throws Exception {
        Field mapGetter = KeyRegistryModule.class.getDeclaredField("fieldMap");
        mapGetter.setAccessible(true);
        final Map<String, Key<?>> mapping = (Map<String, Key<?>>) mapGetter.get(KeyRegistryModule.getInstance());
        for (Field field : Keys.class.getDeclaredFields()) {
            if (!mapping.containsKey(field.getName().toLowerCase())) {
                continue;
            }
            Field modifierField = Field.class.getDeclaredField("modifiers");
            modifierField.setAccessible(true);
            modifierField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(null, mapping.get(field.getName().toLowerCase()));
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>> getDelegateMap() throws Exception {
        final Field delegateField = SpongeDataManager.class.getDeclaredField("processorMap");
        delegateField.setAccessible(true);
        return (Map<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>>) delegateField.get(SpongeDataManager.getInstance());

    }

    @SuppressWarnings("unchecked")
    private static Map<Class<? extends DataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>> getBuilderMap() throws Exception {
        final Field builderMap = SpongeDataManager.class.getDeclaredField("builderMap");
        builderMap.setAccessible(true);
        return (Map<Class<? extends DataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>>) builderMap.get(SpongeDataManager.getInstance());
    }


    public static void setStaticFinalField(Field field, Object value) throws ReflectiveOperationException {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, value);
    }

    private static void setupCatalogTypes() {
        for (Field field : FluidTypes.class.getDeclaredFields()) {
            try {
                setStaticFinalField(field, new SpongeCommonFluidType(field.getName().toLowerCase()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}
