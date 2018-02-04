package org.spongepowered.test.data;

import org.spongepowered.api.data.generator.KeyValue;
import org.spongepowered.api.data.manipulator.DataManipulator;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public interface BookData extends DataManipulator<BookData, ImmutableBookData> {

    @KeyValue("lines")
    List<String> getLines();

    @KeyValue("author")
    String getAuthor();

    @KeyValue("author")
    void setAuthor(String author);

    @KeyValue("publisher")
    Optional<String> getPublisher();

    @KeyValue("publisher")
    void setPublisher(@Nullable String publisher);
}
