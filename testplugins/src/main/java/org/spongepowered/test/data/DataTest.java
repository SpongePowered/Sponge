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
package org.spongepowered.test.data;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.generator.GenericDataGenerator;
import org.spongepowered.api.data.generator.MappedDataGenerator;
import org.spongepowered.api.data.generator.VariantDataGenerator;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.immutable.ImmutableMappedData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableVariantData;
import org.spongepowered.api.data.manipulator.mutable.MappedData;
import org.spongepowered.api.data.manipulator.mutable.VariantData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

@SuppressWarnings("unchecked")
@Plugin(id = "data_test", name = "Data Test", description = "A plugin to custom data.")
public class DataTest {

    public static Key<ListValue<String>> LINES_KEY =
            DummyObjectProvider.createFor(Key.class, "LINES_KEY");
    public static Key<Value<String>> AUTHOR_KEY =
            DummyObjectProvider.createFor(Key.class, "AUTHOR_KEY");
    public static Key<OptionalValue<String>> PUBLISHER_KEY =
            DummyObjectProvider.createFor(Key.class, "PUBLISHER_KEY");
    public static Key<Value<Integer>> REVISIONS_KEY =
            DummyObjectProvider.createFor(Key.class, "REVISIONS_KEY");
    public static Key<Value<Boolean>> MY_BOOLEAN_KEY =
            DummyObjectProvider.createFor(Key.class, "MY_BOOLEAN_KEY");
    public static Key<MapValue<String, Boolean>> MY_STRING_TO_BOOLEAN_KEY =
            DummyObjectProvider.createFor(Key.class, "MY_STRING_TO_BOOLEAN_KEY");
    public static Key<Value<int[]>> MY_INT_ARRAY_KEY =
            DummyObjectProvider.createFor(Key.class, "MY_INT_ARRAY_KEY");

    public static DataRegistration<BookData, ImmutableBookData> MY_BOOK_DATA =
            DummyObjectProvider.createFor(DataRegistration.class, "MY_BOOK_DATA");
    public static DataRegistration<? extends VariantData<Boolean, ?, ?>, ? extends ImmutableVariantData<Boolean, ?, ?>> MY_BOOLEAN_DATA =
            DummyObjectProvider.createFor(DataRegistration.class, "MY_BOOLEAN_DATA");
    public static DataRegistration<? extends MappedData<String, Boolean, ?, ?>, ? extends ImmutableMappedData<String, Boolean, ?, ?>> MY_STRING_TO_BOOLEAN_DATA =
            DummyObjectProvider.createFor(DataRegistration.class, "MY_STRING_TO_BOOLEAN_DATA");
    public static DataRegistration<? extends VariantData<int[], ?, ?>, ? extends ImmutableVariantData<int[], ?, ?>> MY_INT_ARRAY_DATA =
            DummyObjectProvider.createFor(DataRegistration.class, "MY_INT_ARRAY_DATA");

    @Listener
    public void onRegisterKeys(GameRegistryEvent.Register<Key<?>> event) {
        LINES_KEY = Key.builder()
                .type(new TypeToken<ListValue<String>>() {})
                .query(DataQuery.of("Lines"))
                .name("Lines")
                .id("lines")
                .build();
        event.register(LINES_KEY);

        AUTHOR_KEY = Key.builder()
                .type(new TypeToken<Value<String>>() {})
                .query(DataQuery.of("Author"))
                .name("Author")
                .id("author")
                .build();
        event.register(AUTHOR_KEY);

        PUBLISHER_KEY = Key.builder()
                .type(new TypeToken<OptionalValue<String>>() {})
                .query(DataQuery.of("Publisher"))
                .name("Publisher")
                .id("publisher")
                .build();
        event.register(PUBLISHER_KEY);

        REVISIONS_KEY = Key.builder()
                .type(new TypeToken<Value<Integer>>() {})
                .query(DataQuery.of("Revisions"))
                .name("Revisions")
                .id("revisions")
                .build();
        event.register(REVISIONS_KEY);

        MY_BOOLEAN_KEY = Key.builder()
                .type(new TypeToken<Value<Boolean>>() {})
                .query(DataQuery.of("MyBoolean"))
                .name("MyBoolean")
                .id("my_boolean")
                .build();
        event.register(MY_BOOLEAN_KEY);

        MY_STRING_TO_BOOLEAN_KEY = Key.builder()
                .type(new TypeToken<MapValue<String, Boolean>>() {})
                .query(DataQuery.of("MyStringToBooleanMap"))
                .name("MyStringToBooleanMap")
                .id("my_string_to_boolean_map")
                .build();
        event.register(MY_STRING_TO_BOOLEAN_KEY);

        MY_INT_ARRAY_KEY = Key.builder()
                .type(new TypeToken<Value<int[]>>() {})
                .query(DataQuery.of("MyIntArray"))
                .name("MyIntArray")
                .id("my_int_array_key")
                .build();
        event.register(MY_INT_ARRAY_KEY);
    }

    @Listener
    public void onRegisterData(GameRegistryEvent.Register<DataRegistration<?,?>> event) {
        MY_BOOK_DATA = GenericDataGenerator.builder()
                .key(LINES_KEY, new ArrayList<>())
                .key(AUTHOR_KEY, "Unknown")
                .key(PUBLISHER_KEY, Optional.empty())
                .key(REVISIONS_KEY, 1)
                .interfaces(BookData.class, ImmutableBookData.class)
                .id("book_data")
                .name("Book Data")
                .build();
        event.register(MY_BOOK_DATA);

        final BookData bookData = MY_BOOK_DATA.getDataManipulatorBuilder().create();
        System.out.println("DEFAULT AUTHOR: " + bookData.getAuthor());
        System.out.println("DEFAULT PUBLISHER: " + bookData.getPublisher());
        System.out.println("DEFAULT PUBLISHER VALUE: " + bookData.publisher());
        bookData.setAuthor("Cybermaxke").setPublisher("Cybermaxke");
        System.out.println("NEW AUTHOR: " + bookData.getAuthor());
        System.out.println("NEW PUBLISHER: " + bookData.getPublisher());
        System.out.println("NEW PUBLISHER VALUE: " + bookData.publisher());
        System.out.println("CONTAINER: " + bookData.toContainer());
        final BookData loadedData = MY_BOOK_DATA.getDataManipulatorBuilder().create();
        loadedData.from(bookData.toContainer());
        System.out.println("LOADED: " + bookData);
        System.out.println("CONTAINER: " + bookData.toContainer());

        final ImmutableBookData immutableBookData = bookData.asImmutable();
        final ImmutableBookData immutableBookData1 = immutableBookData.setAuthor("Test");
        System.out.println("EQUAL: " + (immutableBookData == immutableBookData1));
        System.out.println("NEW AUTHOR: " + immutableBookData1.getAuthor());
        System.out.println("NEW PUBLISHER: " + immutableBookData1.getPublisher());
        System.out.println("NEW PUBLISHER VALUE: " + immutableBookData1.publisher());

        MY_BOOLEAN_DATA = VariantDataGenerator.builder()
                .key(MY_BOOLEAN_KEY)
                .defaultValue(false)
                .id("my_boolean_data")
                .name("My Boolean Data")
                .build();
        event.register(MY_BOOLEAN_DATA);

        final VariantData<Boolean, ?, ?> booleanData = MY_BOOLEAN_DATA.getDataManipulatorBuilder().create();
        System.out.println("DEFAULT BOOLEAN: " + booleanData.type().get());
        booleanData.set(MY_BOOLEAN_KEY, true);
        System.out.println("NEW BOOLEAN: " + booleanData.type().get());
        booleanData.set(MY_BOOLEAN_KEY, false);
        System.out.println("NEW BOOLEAN 2: " + booleanData.get(MY_BOOLEAN_KEY).orElse(null));

        MY_STRING_TO_BOOLEAN_DATA = MappedDataGenerator.builder()
                .key(MY_STRING_TO_BOOLEAN_KEY)
                .defaultValue(new HashMap<>())
                .id("my_string_to_boolean_data")
                .name("My String To Boolean Data")
                .build();
        event.register(MY_STRING_TO_BOOLEAN_DATA);

        MappedData<String, Boolean, ?, ?> myStringToBooleanData = MY_STRING_TO_BOOLEAN_DATA.getDataManipulatorBuilder().create();
        System.out.println("PRESENT: " + myStringToBooleanData.get("Test").isPresent());
        myStringToBooleanData.put("Test", true);
        System.out.println("VALUE: " + myStringToBooleanData.get("Test").orElse(null));
        myStringToBooleanData.put("Test", false);
        System.out.println("MAP_VALUE: " + myStringToBooleanData.getMapValue());
        System.out.println("VALUE: " + myStringToBooleanData.get("Test").orElse(null));
        myStringToBooleanData.remove("Test");
        System.out.println("PRESENT: " + myStringToBooleanData.get("Test").isPresent());
        System.out.println("MAP_VALUE: " + myStringToBooleanData.getMapValue());

        MY_INT_ARRAY_DATA = VariantDataGenerator.builder()
                .key(MY_INT_ARRAY_KEY)
                .defaultValue(new int[0])
                .id("my_int_array_data")
                .name("My Int Array Data")
                .build();
        event.register(MY_INT_ARRAY_DATA);

        final VariantData<int[], ?, ?> intArrayData = MY_INT_ARRAY_DATA.getDataManipulatorBuilder().create();
        System.out.println("ORIGINAL INT ARRAY: " + Arrays.toString(intArrayData.get(MY_INT_ARRAY_KEY).get()));
        intArrayData.set(MY_INT_ARRAY_KEY, new int[] { 0, 5, 6, 7 });
        System.out.println("NEW INT ARRAY: " + Arrays.toString(intArrayData.get(MY_INT_ARRAY_KEY).get()));
    }
}
