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
package org.spongepowered.common.data.type;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.PaintingVariant;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.type.ArtType;
import org.spongepowered.common.util.Preconditions;

import java.util.Objects;
import java.util.Optional;

public final class SpongeArtTypeBuilder implements ArtType.Builder {

    private int width;
    private int height;
    @Nullable private ResourceLocation assetId;

    public SpongeArtTypeBuilder() {
        this.reset();
    }

    @Override
    public ArtType.Builder dimensions(final int width, final int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    @Override
    public ArtType.Builder asset(final ResourceKey assetId) {
        this.assetId = (ResourceLocation) (Object) assetId;
        return this;
    }

    @Override
    public ArtType.Builder from(final ArtType value) {
        if ((Object) value instanceof final PaintingVariant variant) {
            this.width = variant.width();
            this.height = variant.height();
            this.assetId = variant.assetId();
        }
        return this;
    }

    @Override
    public ArtType.Builder reset() {
        this.width = 0;
        this.height = 0;
        this.assetId = null;
        return this;
    }

    @Override
    public ArtType build() {
        Preconditions.checkArgument(this.width >= 0, "width must set");
        Preconditions.checkArgument(this.height >= 0, "height must set");
        Objects.requireNonNull(this.assetId, "assetId");
        return (ArtType) (Object) new PaintingVariant(this.width, this.height, this.assetId, Optional.empty(), Optional.empty());
    }
}
