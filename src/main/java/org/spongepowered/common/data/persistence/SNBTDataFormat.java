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

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.TagParser;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.InvalidDataFormatException;
import org.spongepowered.api.data.persistence.StringDataFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public final class SNBTDataFormat implements StringDataFormat {

    private static BufferedReader createBufferedReader(final Reader reader) {
        if (reader instanceof BufferedReader) {
            return (BufferedReader) reader;
        }

        return new BufferedReader(reader);
    }

    @Override
    public DataContainer read(final String input) throws InvalidDataException, IOException {
        try {
            return NBTTranslator.INSTANCE.translate(TagParser.parseTag(input));
        } catch (final CommandSyntaxException e) {
            throw new InvalidDataException(e);
        }
    }

    @Override
    public DataContainer readFrom(final Reader input) throws InvalidDataException {
        try {
            return NBTTranslator.INSTANCE.translate(
                    TagParser.parseTag(SNBTDataFormat.createBufferedReader(input).lines().collect(Collectors.joining("\n"))));
        } catch (final CommandSyntaxException e) {
            throw new InvalidDataException(e);
        }
    }

    @Override
    public DataContainer readFrom(final InputStream input) throws InvalidDataFormatException {
        try {
            return NBTTranslator.INSTANCE.translate(TagParser.parseTag(
                    new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"))));
        } catch (final CommandSyntaxException e) {
            throw new InvalidDataException(e);
        }
    }

    @Override
    public String write(final DataView data) throws IOException {
        return NBTTranslator.INSTANCE.translate(data).toString();
    }

    @Override
    public void writeTo(final Writer output, final DataView data) throws IOException {
        output.write(NBTTranslator.INSTANCE.translate(data).toString());
    }

    @Override
    public void writeTo(final OutputStream output, final DataView data) throws IOException {
        output.write(NBTTranslator.INSTANCE.translate(data).toString().getBytes(StandardCharsets.UTF_8));
    }
}
