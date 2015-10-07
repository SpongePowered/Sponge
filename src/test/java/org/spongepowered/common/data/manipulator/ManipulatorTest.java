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
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.persistence.SerializationService;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.SpongeGame;
import org.spongepowered.common.data.SpongeDataRegistry;
import org.spongepowered.common.data.SpongeSerializationRegistry;
import org.spongepowered.common.data.key.KeyRegistry;
import org.spongepowered.common.data.util.DataProcessorDelegate;
import org.spongepowered.common.service.persistence.SpongeSerializationService;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Sponge.class)
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
            final SerializationService serializationService = new SpongeSerializationService();
            Mockito.when(mockGame.getServiceManager()).thenReturn(mockServiceManager);
            when(mockServiceManager.provide(SerializationService.class)).thenReturn(Optional.of(serializationService));
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
                    final Constructor<?> ctor = entry.getKey().getConstructor(null);
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
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }


}
