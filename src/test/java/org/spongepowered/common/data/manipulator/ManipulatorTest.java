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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeGame;
import org.spongepowered.common.data.DataRegistrar;
import org.spongepowered.common.data.key.KeyRegistry;
import org.spongepowered.common.data.util.DataProcessorDelegate;
import org.spongepowered.common.data.SpongeDataManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.Set;

@RunWith(Parameterized.class)
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
            final DataManager dataManager = SpongeDataManager.getInstance();
            Mockito.when(mockGame.getServiceManager()).thenReturn(mockServiceManager);
            when(mockServiceManager.provide(DataManager.class)).thenReturn(Optional.of(dataManager));
            DataRegistrar.setupSerialization(mockGame);
        } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateData() {
        try {
            final SpongeDataManager registry = SpongeDataManager.getInstance();
            final Field manipulatorMap = SpongeDataManager.class.getDeclaredField("processorMap");
            manipulatorMap.setAccessible(true);

    @Test
    public void testValueEquals() {
        try {
            final Constructor<?> ctor = this.manipulatorClass.getConstructor();
            final DataManipulator<?, ?> manipulator = (DataManipulator<?, ?>) ctor.newInstance();
            final ImmutableDataManipulator<?, ?> immutable = manipulator.asImmutable();
            final Set<ImmutableValue<?>> manipulatorValues = manipulator.getValues();
            final Set<ImmutableValue<?>> immutableValues = immutable.getValues();
            assertThat("The ImmutableDataManipulator is missing values present from the DataManipulator! " + this.dataName,
                manipulatorValues.containsAll(immutableValues), is(true));
            assertThat("The DataManipulator is missing values present from the ImmutableDataManipulator! " + this.dataName,
                immutableValues.containsAll(manipulatorValues), is(true));
        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException("All Sponge provided DataManipulator implementations require a no-args constructor! \n"
                                                    + "If the manipulator needs to be parametarized, please understand that there needs to "
                                                    + "be a default at the least.", e);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to construct manipulator: " + this.dataName, e);
        }
    }

    @Test
    public void testSameKeys() {
        try {
            final Constructor<?> ctor = this.manipulatorClass.getConstructor();
            final DataManipulator<?, ?> manipulator = (DataManipulator<?, ?>) ctor.newInstance();
            final ImmutableDataManipulator<?, ?> immutableDataManipulator = manipulator.asImmutable();
            final Set<Key<?>> mutableKeys = manipulator.getKeys();
            final Set<Key<?>> immutableKeys = immutableDataManipulator.getKeys();
            assertThat("The DataManipulator and ImmutableDataManipulator have differing keys!\n"
                + "This shouldn't be the case as a DataManipulator is contractually obliged to store the exact same"
                + "key/values as the ImmutableDataManipulator and vice versa.\n"
                + "The mutable manipulator in question: " + this.dataName +"\n"
                + "The immutable manipulator in question: " + immutableDataManipulator.getClass().getSimpleName(),
                mutableKeys.equals(immutableKeys), is(true));
        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException("All Sponge provided DataManipulator implementations require a no-args constructor! \n"
                                                    + "If the manipulator needs to be parametarized, please understand that there needs to "
                                                    + "be a default at the least.", e);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to construct manipulator: " + this.dataName, e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void testGetValues() {
        try {
            final Constructor<?> ctor = this.manipulatorClass.getConstructor();
            final DataManipulator<?, ?> manipulator = (DataManipulator<?, ?>) ctor.newInstance();
            final ImmutableDataManipulator<?, ?> immutableDataManipulator = manipulator.asImmutable();
            final Set<Key<?>> keys = manipulator.getKeys();
            for (Key<? extends BaseValue<?>> key : keys) {
                Optional<?> mutable = manipulator.get((Key) key);
                Optional<?> immutable = immutableDataManipulator.get((Key) key);
                assertThat("The DataManipulator failed to retrieve a value that a key was registered for!\n"
                    + "The manipulator in question: " + this.dataName, mutable.isPresent(), is(true));
                assertThat("The ImmutableDataManipulator failed to retrieve a value that a key was registered for!\n"
                    + "The manipulator in question: " + immutableDataManipulator.getClass().getSimpleName(), immutable.isPresent(), is(true));
                assertThat("The returned values do not equal eachother!\n"
                    + "DataManipulator: " + this.dataName + "\nImmutableDataManipulator: "
                    + immutableDataManipulator.getClass().getSimpleName(), mutable.equals(immutable), is(true));
            }
        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException("All Sponge provided DataManipulator implementations require a no-args constructor! \n"
                                                    + "If the manipulator needs to be parametarized, please understand that there needs to "
                                                    + "be a default at the least.", e);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to construct manipulator: " + this.dataName, e);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSerialization() {
        try {
            final SpongeDataManager registry = SpongeDataManager.getInstance();
            final Field manipulatorMap = SpongeDataManager.class.getDeclaredField("processorMap");
            manipulatorMap.setAccessible(true);

            final Map<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>> delegateMap =
                (Map<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>>) manipulatorMap.get(registry);

            final Field builderMap = SpongeDataManager.class.getDeclaredField("builderMap");
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
//                    e.printStackTrace();
                } catch (Exception e) {
                    System.out.println("There was an unknown exception, probably because Sponge was not initialized...");
//                    e.printStackTrace();
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
