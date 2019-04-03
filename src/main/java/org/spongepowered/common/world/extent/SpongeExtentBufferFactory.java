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
package org.spongepowered.common.world.extent;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.world.extent.ArchetypeVolume;
import org.spongepowered.api.world.extent.ExtentBufferFactory;
import org.spongepowered.api.world.extent.MutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.common.util.gen.ArrayMutableBlockBuffer;
import org.spongepowered.common.util.gen.ByteArrayMutableBiomeBuffer;
import org.spongepowered.common.world.schematic.GlobalPalette;
import org.spongepowered.common.world.schematic.SpongeArchetypeVolume;

import java.util.Collections;

public final class SpongeExtentBufferFactory implements ExtentBufferFactory {

    public static final SpongeExtentBufferFactory INSTANCE = new SpongeExtentBufferFactory();

    private SpongeExtentBufferFactory() {
    }

    @Override
    public MutableBiomeVolume createBiomeBuffer(Vector3i min, Vector3i size) {
        return new ByteArrayMutableBiomeBuffer(GlobalPalette.getBiomePalette(), min, size);
    }

    @Override
    public MutableBiomeVolume createThreadSafeBiomeBuffer(Vector3i min, Vector3i size) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public MutableBlockVolume createBlockBuffer(Vector3i min, Vector3i size) {
        return new ArrayMutableBlockBuffer(min, size);
    }

    @Override
    public MutableBlockVolume createThreadSafeBlockBuffer(Vector3i min, Vector3i size) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public ArchetypeVolume createArchetypeVolume(Vector3i size, Vector3i origin) {
        MutableBlockVolume backing = new ArrayMutableBlockBuffer(origin.mul(-1), size);
        return new SpongeArchetypeVolume(backing, ImmutableMap.of(), Collections.emptyList());
    }

}
