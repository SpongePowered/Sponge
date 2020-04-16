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
package org.spongepowered.server.launch.transformer.deobf.mappings;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public final class McpCsvReader {

    public enum MappingType {
        METHOD("func_", "methods.csv"), FIELD("field_", "fields.csv");

        private final String prefix;
        private final String file;

        MappingType(String prefix, String file) {
            this.prefix = prefix;
            this.file = file;
        }

    }

    private static final Splitter CSV_SPLITTER = Splitter.on(',').limit(3);

    private final ImmutableMap.Builder<String, String> fields = ImmutableMap.builder();
    private final ImmutableMap.Builder<String, String> methods = ImmutableMap.builder();

    public void read(Path dir) throws IOException {
        read(dir.resolve(MappingType.FIELD.file), MappingType.FIELD);
        read(dir.resolve(MappingType.METHOD.file), MappingType.METHOD);
    }

    public void read(Path file, MappingType type) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file, UTF_8)) {
            read(reader, type);
        }
    }

    public void read(BufferedReader reader, MappingType type) throws IOException {
        switch (type) {
            case FIELD:
                read(reader, type, this.fields);
                break;
            case METHOD:
                read(reader, type, this.methods);
                break;
        }
    }

    private void read(BufferedReader reader, MappingType type, ImmutableMap.Builder<String, String> builder) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (!(line = line.trim()).startsWith(type.prefix)) {
                continue; // TODO: Warning?
            }

            Iterator<String> parts = CSV_SPLITTER.split(line).iterator();
            builder.put(parts.next(), parts.next());
        }
    }

    public ImmutableMap<String, String> getFields() {
        return this.fields.build();
    }

    public ImmutableMap<String, String> getMethods() {
        return this.methods.build();
    }

}
