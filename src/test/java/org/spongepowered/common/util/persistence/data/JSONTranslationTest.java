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
package org.spongepowered.common.util.persistence.data;

import org.junit.Test;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.common.data.persistence.DataSerializers;
import org.spongepowered.common.data.persistence.JsonDataFormat;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class JSONTranslationTest {

    @Test
    public void testJsonToContainerUUID() throws IOException {
        final JsonDataFormat json = new JsonDataFormat();
        final UUID uuid = UUID.randomUUID();
        final DataContainer first = DataContainer.createNew();
        first.set(DataQuery.of("uuid"), DataSerializers.UUID_DATA_SERIALIZER.translate(uuid));
        final DataContainer translatedContainer = json.read(json.write(first));
        assertEquals(first, translatedContainer);
    }

    @Test
    public void testJsonToContainerNumber() throws IOException {
        final JsonDataFormat json = new JsonDataFormat();
        final DataContainer first = DataContainer.createNew().set(DataQuery.of("number"), 1.0);
        final DataContainer translatedContainer = json.read(json.write(first));
        assertEquals(first, translatedContainer);
    }

    @Test
    public void testJsonToContainerDouble() throws IOException {
        final JsonDataFormat json = new JsonDataFormat();
        final DataContainer first = DataContainer.createNew().set(DataQuery.of("double"), 1.5);
        final DataContainer translatedContainer = json.read(json.write(first));
        assertEquals(first, translatedContainer);
    }

    @Test
    public void testJsonToContainerInteger() throws IOException {
        final JsonDataFormat json = new JsonDataFormat();
        final DataContainer first = DataContainer.createNew().set(DataQuery.of("integer"), 5);
        final DataContainer translatedContainer = json.read(json.write(first));
        assertEquals(first, translatedContainer);
    }
}
