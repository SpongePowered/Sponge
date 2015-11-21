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
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.persistence.SerializationManager;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeGame;
import org.spongepowered.common.data.SpongeDataRegistry;
import org.spongepowered.common.data.SpongeSerializationRegistry;
import org.spongepowered.common.data.key.KeyRegistry;
import org.spongepowered.common.data.util.DataProcessorDelegate;
import org.spongepowered.common.service.persistence.SpongeSerializationManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SpongeImpl.class)
public class ManipulatorTest {

    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void setupKeys() {
        try { // Setting up keys
            Method mapGetter = KeyRegistry.class.getDeclaredMethod("getKeyMap", new Class[0]);
            mapGetter.setAccessible(true);
            final Map<String, Key<?>> mapping = (Map<String, Key<?>>) mapGetter.invoke(null);
            for (Field field : Keys.class.getDeclaredFields()) {
                if (!mapping.containsKey(field.getName().toLowerCase())) {
                    continue;
                }
                Field modifierField = Field.class.getDeclaredField("modifiers");
                modifierField.setAccessible(true);
                modifierField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                field.set(null, mapping.get(field.getName().toLowerCase()));
            }
            SpongeGame mockGame = mock(SpongeGame.class);

            final ServiceManager mockServiceManager = mock(ServiceManager.class);
            final SerializationManager serializationManager = SpongeSerializationManager.getInstance();
            Mockito.when(mockGame.getServiceManager()).thenReturn(mockServiceManager);
            when(mockServiceManager.provide(SerializationManager.class)).thenReturn(Optional.of(serializationManager));
            SpongeSerializationRegistry.setupSerialization(mockGame);
        } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateData() {
        try {
            final SpongeDataRegistry registry = SpongeDataRegistry.getInstance();
            final Field manipulatorMap = SpongeDataRegistry.class.getDeclaredField("processorMap");
            manipulatorMap.setAccessible(true);

            final Map<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>> delegateMap =
                (Map<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>>) manipulatorMap.get(registry);
            for (Map.Entry<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>> entry : delegateMap.entrySet()) {
                if (Modifier.isInterface(entry.getKey().getModifiers()) || Modifier.isAbstract(entry.getKey().getModifiers())) {
                    continue;
                }
                try {
                    final Constructor<?> ctor = entry.getKey().getConstructor();
                    System.out.println("Found " + entry.getKey().getCanonicalName() + " and will attempt to construct it!");
                    DataManipulator<?, ?> manipulator = (DataManipulator<?, ?>) ctor.newInstance();
                    System.out.println("Success!");

                    manipulator.asImmutable();
                    System.out.println("Created immutable copy!");
                } catch (NoSuchMethodException e) {
                    System.out.println("Found no no-args constructor for: " + entry.getKey().getCanonicalName());
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    System.out.println("Failed to construct manipulator: " + entry.getKey().getCanonicalName());
                } catch (Exception e) {
                    System.out.println("There was an unknown exception, probably with validation of the Immutable copy");
                    e.printStackTrace();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testSerialization() {
        try {
            final SpongeDataRegistry registry = SpongeDataRegistry.getInstance();
            final Field manipulatorMap = SpongeDataRegistry.class.getDeclaredField("processorMap");
            manipulatorMap.setAccessible(true);

            final Map<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>> delegateMap =
                (Map<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>>) manipulatorMap.get(registry);

            final Field builderMap = SpongeDataRegistry.class.getDeclaredField("builderMap");
            builderMap.setAccessible(true);
            final Map<Class<? extends DataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>> manipulatorBuilderMap =
                (Map<Class<? extends DataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>>) builderMap.get(registry);

            for (Map.Entry<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>> entry : delegateMap.entrySet()) {
                if (Modifier.isInterface(entry.getKey().getModifiers()) || Modifier.isAbstract(entry.getKey().getModifiers())) {
                    continue;
                }
                try {
                    final Constructor<?> ctor = entry.getKey().getConstructor();
                    System.out.println("[Serialization]: Found " + entry.getKey().getCanonicalName() + " and will attempt to construct it!");
                    final DataManipulator<?, ?> manipulator = (DataManipulator<?, ?>) ctor.newInstance();

                    final DataContainer container = manipulator.toContainer();

                    DataManipulatorBuilder<?, ?> builder = manipulatorBuilderMap.get(manipulator.getClass());
                    if (builder != null) {
                        final Optional<DataManipulator<?, ?>> optional = (Optional<DataManipulator<?, ?>>) builder.build(container);
                        if (!optional.isPresent()) {
                            System.err.println("[Serialization]: A builder did not deserialize the data manipulator: " + manipulator.getClass().getCanonicalName());
                            System.err.println("[Serialization]: Providing the DataContainer: " + container.toString());
                        } else {
                            final DataManipulator<?, ?> deserialized = builder.build(container).get();
                            assert manipulator.equals(deserialized);
                        }
                    }
                    System.out.println("[Serialization]: Safely de-serialized!");
                } catch (NoSuchMethodException | InstantiationException | InvocationTargetException e) {
                    System.out.println("Exceptions thrown! ");
                    e.printStackTrace();
                } catch (Exception e) {
                    System.out.println("There was an unknown exception, probably because Sponge was not initialized...");
                    e.printStackTrace();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }



}
