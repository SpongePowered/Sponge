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

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.InvalidDataFormatException;
import org.spongepowered.api.data.persistence.StringDataFormat;
import org.spongepowered.common.SpongeCatalogType;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

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

    public HoconDataFormat(final ResourceKey key) {
        super(key);
    }

    @Override
    public DataContainer read(final String input) throws InvalidDataException, IOException {
        return HoconDataFormat.readFrom(() -> new BufferedReader(new StringReader(input)));
    }

    @Override
    public DataContainer readFrom(final Reader input) throws InvalidDataException, IOException {
        return HoconDataFormat.readFrom(() -> HoconDataFormat.createBufferedReader(input));
    }

    @Override
    public DataContainer readFrom(final InputStream input) throws InvalidDataFormatException, IOException {
        return HoconDataFormat.readFrom(() -> new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)));
    }

    private static DataContainer readFrom(final Callable<BufferedReader> source) throws InvalidDataFormatException {
        final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .source(source)
                .build();
        try {
            final CommentedConfigurationNode node = loader.load();
            return ConfigurateTranslator.instance().translate(node);
        } catch (final ConfigurateException ex) {
            throw new InvalidDataFormatException(ex);
        }
    }

    @Override
    public String write(final DataView data) throws IOException {
        final StringWriter writer = new StringWriter();
        HoconDataFormat.writeTo(() -> new BufferedWriter(writer), data);
        return writer.toString();
    }

    @Override
    public void writeTo(final Writer output, final DataView data) throws IOException {
        HoconDataFormat.writeTo(() -> HoconDataFormat.createBufferedWriter(output), data);
    }

    @Override
    public void writeTo(final OutputStream output, final DataView data) throws IOException {
        HoconDataFormat.writeTo(() -> new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8)), data);
    }

    private static void writeTo(final Callable<BufferedWriter> sink, final DataView data) throws IOException {
        final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .sink(sink)
                .build();
        final ConfigurationNode node = loader.createNode();
        ConfigurateTranslator.instance().translateDataToNode(node, data);
        try {
            loader.save(node);
        } catch (final ConfigurateException ex) {
            throw new IOException(ex);
        }
    }

    private static BufferedReader createBufferedReader(final Reader reader) {
        if (reader instanceof BufferedReader) {
            return (BufferedReader) reader;
        }

        return new BufferedReader(reader);
    }

    private static BufferedWriter createBufferedWriter(final Writer writer) {
        if (writer instanceof BufferedWriter) {
            return (BufferedWriter) writer;
        }

        return new BufferedWriter(writer);
    }

}
