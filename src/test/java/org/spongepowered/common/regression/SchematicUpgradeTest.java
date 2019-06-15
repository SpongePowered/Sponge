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
package org.spongepowered.common.regression;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.world.schematic.Schematic;
import org.spongepowered.lwts.runner.LaunchWrapperTestRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

@RunWith(LaunchWrapperTestRunner.class)
public class SchematicUpgradeTest {

    @Test
    public void testUpgradingv1Tov2() throws IOException {
        final ClassLoader classLoader = this.getClass().getClassLoader();
        try (final InputStream v1InputStream = classLoader.getResource("loadv1.schematic").openStream();
             final InputStream v2InputStream = classLoader.getResource("loadv2.schematic").openStream();
             final GZIPInputStream v1GzipInputStream = new GZIPInputStream(v1InputStream);
             final GZIPInputStream v2GzipInputStream = new GZIPInputStream(v2InputStream)){
                final DataContainer v1Container = DataFormats.NBT.readFrom(v1GzipInputStream);
                final Schematic v1Schem = DataTranslators.SCHEMATIC.translate(v1Container);
                final DataContainer v2Container = DataFormats.NBT.readFrom(v2GzipInputStream);
                final Schematic v2Schem = DataTranslators.SCHEMATIC.translate(v2Container);
                assertEquals(v1Schem, v2Schem);
        }
    }

}
