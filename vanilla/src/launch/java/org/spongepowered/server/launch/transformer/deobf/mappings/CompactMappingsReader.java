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

import com.google.common.collect.ImmutableMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class CompactMappingsReader {

    private static final String SEPARATOR = " ";

    private static final char CLASS_COUNT_IDENTIFIER = 't';
    private static final char CLASS_IDENTIFIER = 'c';
    private static final char FIELD_IDENTIFIER = 'f';
    private static final char METHOD_IDENTIFIER = 'm';
    private static final char COMMENT_IDENTIFIER = '#';

    private final ImmutableMap.Builder<String, ClassMappings> mappings = ImmutableMap.builder();
    private final ImmutableMap.Builder<String, String> reverseClasses = ImmutableMap.builder();

    public void read(URL resource) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8))) {
            read(reader);
        }
    }

    public void read(BufferedReader reader) throws IOException {
        String currentClass = null;
        String currentMappedName = null;
        ImmutableMap.Builder<MemberDescriptor, String> currentFields = null;
        ImmutableMap.Builder<MemberDescriptor, String> currentMethods = null;

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() < 3) {
                continue;
            }

            char identifier = line.charAt(0);
            if (identifier == COMMENT_IDENTIFIER) {
                continue;
            }

            String[] args = line.substring(2).split(SEPARATOR);

            switch (line.charAt(0)) {
                case CLASS_IDENTIFIER:
                    if (currentClass != null) {
                        this.mappings.put(currentClass, new ClassMappings(currentMappedName, currentFields.build(), currentMethods.build()));
                    }

                    currentClass = args[0];
                    currentMappedName = args[1];
                    if (!currentClass.equals(currentMappedName)) {
                        this.reverseClasses.put(currentMappedName, currentClass);
                    }

                    currentFields = ImmutableMap.builder();
                    currentMethods = ImmutableMap.builder();
                    break;
                case FIELD_IDENTIFIER:
                    if (currentClass == null) {
                        throw new IllegalStateException("Field mapping without a class");
                    }

                    currentFields.put(new MemberDescriptor(args[0], args[1]), args[2]);
                    break;
                case METHOD_IDENTIFIER:
                    if (currentClass == null) {
                        throw new IllegalStateException("Method mapping without a class");
                    }

                    currentMethods.put(new MemberDescriptor(args[0], args[1]), args[2]);
                    break;
                case CLASS_COUNT_IDENTIFIER:
                    // Skip for now
                    break;
                default:
                    throw new IllegalArgumentException("Invalid line: " + line);
            }
        }

        if (currentClass != null) {
            this.mappings.put(currentClass, new ClassMappings(currentMappedName, currentFields.build(), currentMethods.build()));
        }
    }

    public ImmutableMap<String, ClassMappings> getMappings() {
        return this.mappings.build();
    }

    public ImmutableMap<String, String> getReverseClasses() {
        return this.reverseClasses.build();
    }

}
