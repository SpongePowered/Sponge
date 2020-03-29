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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.Opt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.event.SpongeEventFactoryTest;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.PEBKACException;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.util.TypeTokenHelper;
import org.spongepowered.lwts.runner.LaunchWrapperParameterized;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RunWith(LaunchWrapperParameterized.class)
public class ManipulatorTest {

    @Parameterized.Parameters(name = "{index} Data: {0} generateValues: {3}")
    public static Iterable<Object[]> data() throws Exception {
        return DataTestUtil.generateManipulatorTestObjects();
    }

    private String dataName;
    private Class<? extends DataManipulator<?, ?>> manipulatorClass;
    private DataManipulatorBuilder<?, ?> builder;
    private boolean generateValues;

    public ManipulatorTest(String simpleName, Class<? extends DataManipulator<?, ?>> manipulatorClass, DataManipulatorBuilder<?, ?> builder, boolean generateValues) {
        this.manipulatorClass = manipulatorClass;
        this.dataName = simpleName;
        this.builder = builder;
        this.generateValues = generateValues;
    }

    @SuppressWarnings("unchecked")
    private DataManipulator<?, ?> createManipulator() {
        try {
            final Constructor<?> ctor = this.manipulatorClass.getConstructor();
            DataManipulator<?, ?> manipulator = (DataManipulator<?, ?>) ctor.newInstance();

            if (this.generateValues) {
                final Set<Key<?>> keys = manipulator.getKeys();

                for (Key<?> key: keys) {
                    Optional<Object> value = createValueElement((Key) key);
                    value.ifPresent(o -> manipulator.set((Key) key, o));
                }
            }
            return manipulator;
        } catch (InstantiationException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to construct manipulator: " + this.dataName, e);
        } catch (Exception e) {
            throw new RuntimeException("There was an unknown exception, probably with validation of the Immutable copy for: "
                    + this.manipulatorClass.getSimpleName() + ". \n It may be required to use @ImplementationRequiredForTest on "
                    + "the DataManipulator implementation class to avoid testing a DataManipulator dependent on a CatalogType.", e);
        }
    }

    @Test
    public void testCreateData() {
        try {
            DataManipulator<?, ?> manipulator = this.createManipulator();
            manipulator.asImmutable();
        } catch (Exception e) {
            throw new RuntimeException("There was an unknown exception, probably with validation of the Immutable copy for: "
                    + this.manipulatorClass.getSimpleName() + ". \n It may be required to use @ImplementationRequiredForTest on "
                    + "the DataManipulator implementation class to avoid testing a DataManipulator dependent on a CatalogType.", e);
        }
    }

    @Test
    public void testValueEquals() {
        final DataManipulator<?, ?> manipulator = this.createManipulator();
        final ImmutableDataManipulator<?, ?> immutable = manipulator.asImmutable();
        final Set<ImmutableValue<?>> manipulatorValues = manipulator.getValues();
        final Set<ImmutableValue<?>> immutableValues = immutable.getValues();
        assertThat("The ImmutableDataManipulator is missing values present from the DataManipulator! " + this.dataName,
                manipulatorValues.containsAll(immutableValues), is(true));
        assertThat("The DataManipulator is missing values present from the ImmutableDataManipulator! " + this.dataName,
                immutableValues.containsAll(manipulatorValues), is(true));
    }

    @Test
    public void testMutableImmutable() {
        final DataManipulator<?, ?> manipulator = this.createManipulator();
        final ImmutableDataManipulator<?, ?> immutableDataManipulator = manipulator.asImmutable();
        final DataManipulator<?, ?> newManipulator = immutableDataManipulator.asMutable();

        assertThat("The DataManipulator constructed by ImmutableDataManipulator#asMutable is not "
                        + "equal to original DataManipulator!\n"
                        + "This shouldn't be the case, as aDataManipulator constructed from an ImmutableDataManipulator "
                        + "should store exactly the same keys and values as the original DataManipulator, and therefore "
                        + "be equal to it.\n"
                        + "The mutable manipulator in question: " + this.dataName + "\n"
                        + "The immutable manipulator in question: " + immutableDataManipulator.getClass().getSimpleName(),
                manipulator.getValues().equals(newManipulator.getValues()), is(true));
    }

    @Test
    public void testSameKeys() {
            final DataManipulator<?, ?> manipulator = this.createManipulator();
            final ImmutableDataManipulator<?, ?> immutableDataManipulator = manipulator.asImmutable();
            final Set<Key<?>> mutableKeys = manipulator.getKeys();
            final Set<Key<?>> immutableKeys = immutableDataManipulator.getKeys();
            assertThat("The DataManipulator and ImmutableDataManipulator have differing keys!\n"
                            + "This shouldn't be the case as a DataManipulator is contractually obliged to store the exact same"
                            + "key/values as the ImmutableDataManipulator and vice versa.\n"
                            + "The mutable manipulator in question: " + this.dataName +"\n"
                            + "The immutable manipulator in question: " + immutableDataManipulator.getClass().getSimpleName(),
                    immutableKeys, equalTo(mutableKeys));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void testGetValues() {
            final DataManipulator<?, ?> manipulator = this.createManipulator();
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
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> createValueElement(Key<?> type) {
        Class<T> elementClass = (Class<T>) type.getElementToken().getRawType();
        if (Optional.class.isAssignableFrom(elementClass)) {
            Class<?> wrappedType = TypeTokenHelper.getGenericParam(type.getElementToken(), 0);
            // The innermost optional is the actual type of the Key. The outer optional
            // indicates to the caller that we were able to create something for this key.
            return (Optional) Optional.of(Optional.of(createType(wrappedType)));
        } else if (List.class.isAssignableFrom(elementClass)) {
            Class<?> wrappedType = TypeTokenHelper.getGenericParam(type.getElementToken(), 0);

            return (Optional) Optional.of(Lists.newArrayList(createType(wrappedType)));
        }
        return Optional.empty();
    }


    @SuppressWarnings("unchecked")
    private <T> T createType(Class<T> type) {
        if (CatalogType.class.isAssignableFrom(type)) {
            return (T) Sponge.getRegistry().getAllOf((Class<CatalogType>) type).iterator().next();
        } else if (isBuildable(type)) {
            return (T) createFromBuilder(type);
        } else {
            return (T) SpongeEventFactoryTest.mockParam(type);
        }
    }

    private boolean isBuildable(Class<?> type) {
        // The 'child' methods on Text.Builder interact oddly
        // with our code, so skip building Text for now
        if (type == Text.class) {
            return false;
        }
        // EntitySnapshot can't be built with null values
        // We need a dummy EntitySnapshot
        // Skip building EntitySnapshot for now
        if (type == EntitySnapshot.class) {
            return false;
        }
        try {
            type.getMethod("builder");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private Object createFromBuilder(Class<?> type) {
        try {
            Method methodbuilder = type.getMethod("builder");
            Class<?> builderType = methodbuilder.getReturnType();
            Method methodBuild = builderType.getMethod("build");
            Object builderObject = methodbuilder.invoke(nullValue());

            for (Method builderSetter: builderType.getMethods()) {
                if (!this.isBuilderSetter(builderType, type, builderSetter)) {
                    continue;
                }
                Class<?> paramType = builderSetter.getParameterTypes()[0];
                Object param = this.createBuilderParam(paramType);

                builderSetter.invoke(builderObject, param);
            }

            Object finalType = methodBuild.invoke(builderObject);
            return finalType;

        } catch (Exception e) {
            throw new RuntimeException("Exception while creating builder for type " + type, e);
        }
    }

    private Object createBuilderParam(Class<?> paramType) {
        // Many builder methods require that that integer parameters be greater than 0
        if (paramType.equals(int.class) || paramType.equals(Integer.class)) {
            return 1;
        }
        return this.createType(paramType);
    }

    private boolean isBuilderSetter(Class<?> builderType, Class<?> type, Method method) {
        if (method.getReturnType().equals(builderType) && method.getParameterCount() == 1) {
            // Don't try to call any methods that take the type we're building
            Class<?> paramType = method.getParameterTypes()[0];
            return !paramType.equals(type)
                    && !paramType.equals(Class.class)
                    && !paramType.equals(DataManipulator.class)
                    && !paramType.equals(ImmutableDataManipulator.class)
                    && !method.getName().startsWith("from");
        }
        return false;
    }
    @SuppressWarnings("unchecked")
    @Test
    public void testSerialization() {
        try {
            final DataManipulator<?, ?> manipulator = this.createManipulator();

            final DataContainer container = manipulator.toContainer();
            if (this.builder != null) {
                final Optional<DataManipulator<?, ?>> optional;
                try {
                    optional = (Optional<DataManipulator<?, ?>>) this.builder.build(container);
                } catch (Exception e) {
                    printExceptionBuildingData(container, e);
                    throw e;
                }
                if (!optional.isPresent()) {
                    printEmptyBuild(container);
                    throw new IllegalArgumentException("[Serialization]: A builder did not translate the data manipulator: "
                            + this.dataName + "\n[Serialization]: Providing the DataContainer: " + container.toString());
                }

                final DataManipulator<?, ?> deserialized = this.builder.build(container).get();
                final boolean equals = manipulator.equals(deserialized);
                if (!equals) {
                    printNonEqual(container, manipulator, deserialized);
                }
                assertThat(manipulator, equalTo(deserialized));
            }
        } catch (Exception e) {
            throw new RuntimeException("There was an unknown exception trying to test " + this.dataName
                    + ". Probably because the DataManipulator relies on an implementation class.", e);
        }
    }

    private void printNonEqual(DataContainer container, DataManipulator<?,?> original, DataManipulator<?, ?> deserialized) {
        final PrettyPrinter printer = new PrettyPrinter(60).centre().add("Unequal Data").hr()
                .add("Something something equals....")
                .add()
                .add("Provided manipulators don't equal eachother.");
        printRemaining(container, printer);

    }

    private void printEmptyBuild(DataContainer container) {
        final PrettyPrinter printer = new PrettyPrinter(60).centre().add("Did not build data!").hr()
                .add("Something something builders....")
                .add()
                .add("Provided container didn't get built into a manipulator!");
        printRemaining(container, printer);
    }

    private void printExceptionBuildingData(DataContainer container, Exception exception) {
        final PrettyPrinter printer = new PrettyPrinter(60).centre().add("Could not build data!").hr()
                .add(exception)
                .add("Something something data....")
                .add()
                .add("Here's the provided container:");
        printRemaining(container, printer);
    }

    private void printRemaining(DataContainer container, PrettyPrinter printer) {
        printContainerToPrinter(printer, container, 2);
        printer.add()
                .add("Manipulator class: " + this.manipulatorClass)
                .print(System.err);
    }


    private static void printContainerToPrinter(PrettyPrinter printer, DataView container, int indentation) {
        for (DataQuery dataQuery : container.getKeys(false)) {
            final Object o = container.get(dataQuery).get();
            final StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < indentation; i++) {
                stringBuilder.append(" ");
            }
            final List<String> parts = dataQuery.getParts();

            printer.add(stringBuilder.toString() + "- Query: " + parts.get(parts.size() - 1));
            if (o instanceof DataView) {
                // Paginate the internal views.
                printContainerToPrinter(printer, (DataView) o, indentation * 2);
            } else {
                printer                .add(stringBuilder.toString() + "- Value: " + o);

            }
        }
    }
}
