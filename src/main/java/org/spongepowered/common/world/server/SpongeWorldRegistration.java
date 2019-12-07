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

import com.google.common.base.MoreObjects;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.world.server.WorldRegistration;
import org.spongepowered.common.bridge.world.dimension.DimensionTypeBridge;
import org.spongepowered.common.mixin.accessor.world.dimension.DimensionTypeAccessor;

public final class SpongeWorldRegistration implements WorldRegistration {

    private final CatalogKey key;
    private final String directoryName;

    public SpongeWorldRegistration(CatalogKey key, String directoryName) {
        this.key = key;
        this.directoryName = directoryName;
    }

    public SpongeWorldRegistration(DimensionType dimensionType) {
        this(((DimensionTypeBridge) dimensionType).bridge$getKey(), ((DimensionTypeAccessor) dimensionType).accessor$getDirectory());
    }

    @Override
    public CatalogKey getKey() {
        return this.key;
    }

    @Override
    public String getDirectoryName() {
        return this.directoryName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SpongeWorldRegistration that = (SpongeWorldRegistration) o;
        return this.key.equals(that.key) && this.directoryName.equals(that.directoryName);
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("key", this.key)
            .add("directoryName", this.directoryName)
            .toString();
    }
}
