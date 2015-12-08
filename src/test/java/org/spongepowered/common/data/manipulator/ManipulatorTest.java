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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.util.PEBKACException;
import org.spongepowered.common.SpongeGame;
import org.spongepowered.common.data.DataRegistrar;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.key.KeyRegistry;
import org.spongepowered.common.data.util.DataProcessorDelegate;
import org.spongepowered.common.data.util.ImplementationRequiredForTest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RunWith(Parameterized.class)
public class ManipulatorTest {

    @SuppressWarnings({"unchecked"})
    @Parameterized.Parameters(name = "{index} Data: {0}")
    public static Iterable<Object[]> data() {
        try { // Setting up keys
            Method mapGetter = KeyRegistry.class.getDeclaredMethod("getKeyMap");
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
            final DataManager dataManager = SpongeDataManager.getInstance();
            Mockito.when(mockGame.getServiceManager()).thenReturn(mockServiceManager);
            when(mockServiceManager.provide(DataManager.class)).thenReturn(Optional.of(dataManager));
            DataRegistrar.setupSerialization(mockGame);
        } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        final List<Object[]> list = new ArrayList<>();
        try {
            final SpongeDataManager registry = SpongeDataManager.getInstance();
            final Field manipulatorMap = SpongeDataManager.class.getDeclaredField("processorMap");
            manipulatorMap.setAccessible(true);

            final Field builderMap = SpongeDataManager.class.getDeclaredField("builderMap");
            builderMap.setAccessible(true);
            final Map<Class<? extends DataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>> manipulatorBuilderMap =
                (Map<Class<? extends DataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>>) builderMap.get(registry);

            final Map<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>> delegateMap =
                (Map<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>>) manipulatorMap.get(registry);
            for (Map.Entry<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>> entry : delegateMap.entrySet()) {
                if (Modifier.isInterface(entry.getKey().getModifiers()) || Modifier.isAbstract(entry.getKey().getModifiers())) {
                    continue;
                }
                if (entry.getKey().getAnnotation(ImplementationRequiredForTest.class) != null) {
                    continue;
                }
                list.add(new Object[] {entry.getKey().getSimpleName(), entry.getKey(), manipulatorBuilderMap.get(entry.getKey()) });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;

    }

    private String dataName;
    private Class<? extends DataManipulator<?, ?>> manipulatorClass;
    private DataManipulatorBuilder<?, ?> builder;


    public ManipulatorTest(String simpleName, Class<? extends DataManipulator<?, ?>> manipulatorClass, DataManipulatorBuilder<?, ?> builder) {
        this.manipulatorClass = manipulatorClass;
        this.dataName = simpleName;
        this.builder = builder;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateData() {
        try {
            final Constructor<?> ctor = this.manipulatorClass.getConstructor();
            DataManipulator<?, ?> manipulator = (DataManipulator<?, ?>) ctor.newInstance();
            manipulator.asImmutable();
        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException("All Sponge provided DataManipulator implementations require a no-args constructor! \n"
                + "If the manipulator needs to be parametarized, please understand that there needs to "
                + "be a default at the least.", e);
        } catch (InstantiationException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to construct manipulator: " + this.dataName, e);
        } catch (Exception e) {
            throw new RuntimeException("There was an unknown exception, probably with validation of the Immutable copy for: "
                + this.manipulatorClass.getSimpleName() + ". \n It may be required to use @ImplementationRequiredForTest on "
                + "the DataManipulator implementation class to avoid testing a DataManipulator dependent on a CatalogType.", e);
        }
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testSerialization() {
        try {
            final Constructor<?> ctor = this.manipulatorClass.getConstructor();
            final DataManipulator<?, ?> manipulator = (DataManipulator<?, ?>) ctor.newInstance();

            final DataContainer container = manipulator.toContainer();

            if (this.builder != null) {
                final Optional<DataManipulator<?, ?>> optional = (Optional<DataManipulator<?, ?>>) this.builder.build(container);
                if (!optional.isPresent()) {
                    throw new IllegalArgumentException("[Serialization]: A builder did not deserialize the data manipulator: "
                        + this.dataName + "\n[Serialization]: Providing the DataContainer: " + container.toString());
                } else {
                    final DataManipulator<?, ?> deserialized = this.builder.build(container).get();
                    assertThat(manipulator.equals(deserialized), is(true));
                }
            }
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException e) {
            throw new PEBKACException("Exceptions thrown trying to construct: " + this.dataName, e);
        } catch (Exception e) {
            throw new RuntimeException("There was an unknown exception trying to test " + this.dataName
                               + ". Probably because the DataManipulator relies on an implementation class.", e);
        }
    }

}
