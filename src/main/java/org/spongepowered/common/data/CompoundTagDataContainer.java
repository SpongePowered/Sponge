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
package org.spongepowered.common.data;

import net.minecraft.nbt.CompoundTag;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

import java.util.Optional;

/**
 * A {@link DataContainer} backed directly by a Minecraft {@link CompoundTag}.
 */
public final class CompoundTagDataContainer extends CompoundTagDataView implements DataContainer {

    public CompoundTagDataContainer(final CompoundTag compound) {
        this(compound, SafetyMode.ALL_DATA_CLONED);
    }

    public CompoundTagDataContainer(final CompoundTag compound, final DataView.SafetyMode safety) {
        super(compound, safety);
    }

    @Override
    public Optional<DataView> parent() {
        return Optional.empty();
    }

    @Override
    public DataContainer set(final DataQuery path, final Object value) {
        return (DataContainer) super.set(path, value);
    }

    @Override
    public DataContainer set(final DataQuery path, final String value) {
        return (DataContainer) super.set(path, value);
    }

    @Override
    public DataContainer set(final DataQuery path, final boolean value) {
        return (DataContainer) super.set(path, value);
    }

    @Override
    public DataContainer set(final DataQuery path, final byte value) {
        return (DataContainer) super.set(path, value);
    }

    @Override
    public DataContainer set(final DataQuery path, final short value) {
        return (DataContainer) super.set(path, value);
    }

    @Override
    public DataContainer set(final DataQuery path, final int value) {
        return (DataContainer) super.set(path, value);
    }

    @Override
    public DataContainer set(final DataQuery path, final long value) {
        return (DataContainer) super.set(path, value);
    }

    @Override
    public DataContainer set(final DataQuery path, final float value) {
        return (DataContainer) super.set(path, value);
    }

    @Override
    public DataContainer set(final DataQuery path, final double value) {
        return (DataContainer) super.set(path, value);
    }

    @Override
    public DataContainer set(final String key, final Object value) {
        return (DataContainer) super.set(key, value);
    }

    @Override
    public DataContainer set(final String key, final String value) {
        return (DataContainer) super.set(key, value);
    }

    @Override
    public DataContainer set(final String key, final boolean value) {
        return (DataContainer) super.set(key, value);
    }

    @Override
    public DataContainer set(final String key, final byte value) {
        return (DataContainer) super.set(key, value);
    }

    @Override
    public DataContainer set(final String key, final short value) {
        return (DataContainer) super.set(key, value);
    }

    @Override
    public DataContainer set(final String key, final int value) {
        return (DataContainer) super.set(key, value);
    }

    @Override
    public DataContainer set(final String key, final long value) {
        return (DataContainer) super.set(key, value);
    }

    @Override
    public DataContainer set(final String key, final float value) {
        return (DataContainer) super.set(key, value);
    }

    @Override
    public DataContainer set(final String key, final double value) {
        return (DataContainer) super.set(key, value);
    }

    @Override
    public DataContainer remove(final DataQuery path) {
        return (DataContainer) super.remove(path);
    }
}
