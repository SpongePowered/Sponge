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
package org.spongepowered.common.data.meta;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.meta.BannerPatternLayer;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.type.BannerPatternShape;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.common.util.Constants;

public final class SpongePatternLayer implements BannerPatternLayer {

    private final BannerPatternShape id;
    private final DyeColor color;

    public SpongePatternLayer(final BannerPatternShape id, final DyeColor color) {
        this.id = id;
        this.color = color;
    }

    @Override
    public BannerPatternShape shape() {
        return this.id;
    }

    @Override
    public DyeColor color() {
        return this.color;
    }

    @Override
    public int contentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final ResourceKey idKey = Sponge.game().registries().registry(RegistryTypes.BANNER_PATTERN_SHAPE).valueKey(this.id);
        final ResourceKey colorKey = Sponge.game().registries().registry(RegistryTypes.DYE_COLOR).valueKey(this.color);
        return DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, this.contentVersion())
            .set(Constants.TileEntity.Banner.SHAPE, idKey)
            .set(Constants.TileEntity.Banner.COLOR, colorKey);
    }

}
