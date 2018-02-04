package org.spongepowered.test.data;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.generator.GenericDataGenerator;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;

import java.util.ArrayList;
import java.util.Optional;

@SuppressWarnings("unchecked")
@Plugin(id = "data_test", name = "Data Test", description = "A plugin to custom data.")
public class DataTest {

    public static Key<ListValue<String>> LINES_KEY = DummyObjectProvider.createFor(Key.class, "LINES_KEY");
    public static Key<Value<String>> AUTHOR_KEY = DummyObjectProvider.createFor(Key.class, "AUTHOR_KEY");
    public static Key<OptionalValue<String>> PUBLISHER_KEY = DummyObjectProvider.createFor(Key.class, "PUBLISHER_KEY");

    public static DataRegistration<BookData, ImmutableBookData> MY_BOOK_DATA;

    @Listener
    public void onRegisterKeys(GameRegistryEvent.Register<Key<?>> event) {
        // Create the key
        LINES_KEY = Key.builder()
                .type(new TypeToken<ListValue<String>>() {})
                .query(DataQuery.of("Lines"))
                .name("Lines")
                .id("lines")
                .build();
        // Register the key
        event.register(LINES_KEY);
        // Create the key
        AUTHOR_KEY = Key.builder()
                .type(new TypeToken<Value<String>>() {})
                .query(DataQuery.of("Author"))
                .name("Author")
                .id("author")
                .build();
        // Register the key
        event.register(AUTHOR_KEY);
        // Create the key
        PUBLISHER_KEY = Key.builder()
                .type(new TypeToken<OptionalValue<String>>() {})
                .query(DataQuery.of("Publisher"))
                .name("Publisher")
                .id("publisher")
                .build();
        // Register the key
        event.register(PUBLISHER_KEY);
    }

    @Listener
    public void onRegisterData(GameRegistryEvent.Register<DataRegistration<?,?>> event) {
        MY_BOOK_DATA = GenericDataGenerator.builder()
                .key(LINES_KEY, new ArrayList<>())
                .key(AUTHOR_KEY, "Unknown")
                .key(PUBLISHER_KEY, Optional.empty())
                .interfaces(BookData.class, ImmutableBookData.class)
                .id("book_data")
                .name("Book Data")
                .build();
        final BookData bookData = MY_BOOK_DATA.getDataManipulatorBuilder().create();
        System.out.println("DEFAULT AUTHOR: " + bookData.getAuthor());
        System.out.println("DEFAULT PUBLISHER: " + bookData.getPublisher());
        bookData.setAuthor("Cybermaxke");
        bookData.setPublisher("Cybermaxke");
        System.out.println("NEW AUTHOR: " + bookData.getAuthor());
        System.out.println("NEW PUBLISHER: " + bookData.getPublisher());

        event.register(MY_BOOK_DATA);
    }
}
