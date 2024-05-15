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
package org.spongepowered.common.data.builder.meta;


import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.meta.BannerPatternLayer;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.type.BannerPatternShape;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.Preconditions;

import java.util.Objects;
import java.util.Optional;

/**
 * The de-facto builder for a {@link BannerPatternLayer}.
 */
public final class SpongePatternLayerBuilder extends AbstractDataBuilder<BannerPatternLayer> implements BannerPatternLayer.Builder,
        DataBuilder<BannerPatternLayer> {

    private DyeColor color;
    private BannerPatternShape shape;

    public SpongePatternLayerBuilder() {
        super(BannerPatternLayer.class, 1);
    }

    @Override
    protected Optional<BannerPatternLayer> buildContent(final DataView container) throws InvalidDataException {
        Objects.requireNonNull(container);
        if (!container.contains(Constants.TileEntity.Banner.SHAPE) || !container.contains(Constants.TileEntity.Banner.COLOR)) {
            return Optional.empty();
        }
        final BannerPatternShape shape = container.getRegistryValue(Constants.TileEntity.Banner.SHAPE, RegistryTypes.BANNER_PATTERN_SHAPE,
                Sponge.game()).orElseThrow(() -> new InvalidDataException("The provided container has an invalid banner pattern shape entry!"));


        // Now we need to validate the dye color of course...
        final DyeColor color = container.getRegistryValue(Constants.TileEntity.Banner.COLOR, RegistryTypes.DYE_COLOR, Sponge.game())
                .orElseThrow(() -> new InvalidDataException("The provided container has an invalid dye color entry!"));


        final Holder<BannerPattern> mcPattern = Holder.direct((BannerPattern) (Object) shape);
        return Optional.of((BannerPatternLayer) (Object) new BannerPatternLayers.Layer(mcPattern, (net.minecraft.world.item.DyeColor) (Object) color));
    }

    @Override
    public SpongePatternLayerBuilder reset() {
        this.shape = null;
        this.color = null;
        return this;
    }

    @Override
    public BannerPatternLayer.Builder pattern(final BannerPatternShape shape) {
        this.shape = Objects.requireNonNull(shape);
        return this;
    }

    @Override
    public BannerPatternLayer.Builder color(final DyeColor color) {
        this.color = Objects.requireNonNull(color);
        return this;
    }

    @Override
    public BannerPatternLayer.Builder from(final BannerPatternLayer value) {
        this.shape = value.shape();
        this.color = value.color();
        return this;
    }

    @Override
    public BannerPatternLayer build() {
        Preconditions.checkState(this.shape != null);
        Preconditions.checkState(this.color != null);
        final Holder<BannerPattern> mcPattern = Holder.direct((BannerPattern) (Object) shape);
        return (BannerPatternLayer) (Object) new BannerPatternLayers.Layer(mcPattern, (net.minecraft.world.item.DyeColor) (Object) color);
    }
}
