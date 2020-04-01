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
package org.spongepowered.common.world.server;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.dimension.DimensionType;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class WorldRegistration {

    private final String directoryName;
    private final DimensionType dimensionType;
    private WorldSettings defaultSettings;

    public WorldRegistration(String directoryName, DimensionType dimensionType, @Nullable WorldSettings defaultSettings) {
        this.directoryName = checkNotNull(directoryName);
        this.dimensionType = checkNotNull(dimensionType);
        this.defaultSettings = defaultSettings;
    }

    public String getDirectoryName() {
        return this.directoryName;
    }

    public DimensionType getDimensionType() {
        return this.dimensionType;
    }

    public WorldSettings getDefaultSettings() {
        return this.defaultSettings;
    }

    public void setDefaultSettings(@Nullable WorldSettings defaultSettings) {
        this.defaultSettings = defaultSettings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final WorldRegistration that = (WorldRegistration) o;
        return this.directoryName.equals(that.directoryName);
    }

    @Override
    public int hashCode() {
        return this.directoryName.hashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("directoryName", this.directoryName)
            .toString();
    }
}
