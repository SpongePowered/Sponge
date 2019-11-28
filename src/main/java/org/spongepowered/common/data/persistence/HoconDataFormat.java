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
package org.spongepowered.common.data.persistence;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.InvalidDataFormatException;
import org.spongepowered.api.data.persistence.StringDataFormat;
import org.spongepowered.common.SpongeCatalogType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

public class HoconDataFormat extends SpongeCatalogType implements StringDataFormat {

    public HoconDataFormat(String id) {
        super(id);
    }

    @Override
    public DataContainer read(String input) throws InvalidDataException, IOException {
        return readFrom(() -> new BufferedReader(new StringReader(input)));
    }

    @Override
    public DataContainer readFrom(Reader input) throws InvalidDataException, IOException {
        return readFrom(() -> createBufferedReader(input));
    }

    @Override
    public DataContainer readFrom(InputStream input) throws InvalidDataFormatException, IOException {
        return readFrom(() -> new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)));
    }

    private static DataContainer readFrom(Callable<BufferedReader> source) throws IOException {
        HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .setSource(source)
                .build();
        CommentedConfigurationNode node = loader.load();
        return ConfigurateTranslator.instance().translateFrom(node);
    }

    @Override
    public String write(DataView data) throws IOException {
        StringWriter writer = new StringWriter();
        writeTo(() -> new BufferedWriter(writer), data);
        return writer.toString();
    }

    @Override
    public void writeTo(Writer output, DataView data) throws IOException {
        writeTo(() -> createBufferedWriter(output), data);
    }

    @Override
    public void writeTo(OutputStream output, DataView data) throws IOException {
        writeTo(() -> new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8)), data);
    }

    private static void writeTo(Callable<BufferedWriter> sink, DataView data) throws IOException {
        HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .setSink(sink)
                .build();
        ConfigurationNode node = ConfigurateTranslator.instance().translateData(data);
        loader.save(node);
    }

    private static BufferedReader createBufferedReader(Reader reader) {
        if (reader instanceof BufferedReader) {
            return (BufferedReader) reader;
        }

        return new BufferedReader(reader);
    }

    private static BufferedWriter createBufferedWriter(Writer writer) {
        if (writer instanceof BufferedWriter) {
            return (BufferedWriter) writer;
        }

        return new BufferedWriter(writer);
    }

}
