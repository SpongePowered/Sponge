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
package org.spongepowered.common.map.color;

import com.google.common.base.Preconditions;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.map.color.MapColor;
import org.spongepowered.api.map.color.MapColorType;
import org.spongepowered.common.registry.type.map.MapColorRegistryModule;

import javax.annotation.Nullable;
import java.util.Optional;

public class SpongeMapColorBuilder extends AbstractDataBuilder<MapColor> implements MapColor.Builder {
    @Nullable
    private MapColorType color = null;
    private int shade = 0;

    public SpongeMapColorBuilder() {
        super(MapColor.class, 1);
    }

    @Override
    public MapColor.Builder base() {
        this.shade = 0;
        return this;
    }

    @Override
    public MapColor.Builder dark() {
        this.shade = 1;
        return this;
    }

    @Override
    public MapColor.Builder darker() {
        this.shade = 2;
        return this;
    }

    @Override
    public MapColor.Builder darkest() {
        this.shade = 3;
        return this;
    }

    @Override
    public MapColor.Builder baseColor(MapColorType mapColorType) {
        color = mapColorType;
        return this;
    }

    @Override
    public MapColor build() throws IllegalStateException {
        Preconditions.checkState(this.color != null, "Color has not been set yet");
        return new SpongeMapColor(this.color, this.shade);
    }

    @Override
    protected Optional<MapColor> buildContent(DataView container) throws InvalidDataException {
        if (!container.contains(DataQuery.of("mapIndex"))
                || !container.contains(DataQuery.of("shade"))) {
            return Optional.empty();
        }
        int colorInt = container.getInt(DataQuery.of("mapIndex")).get();
        Optional<MapColorType> color = MapColorRegistryModule.getByColorValue(colorInt);
        return color.map(mapColorType -> new SpongeMapColor(
                mapColorType,
                container.getInt(DataQuery.of("shade")).get()));
    }

    @Override
    public DataBuilder<MapColor> from(MapColor value) {
        SpongeMapColor mapColorBridge = (SpongeMapColor)value;
        this.color = mapColorBridge.getType();
        this.shade = mapColorBridge.getShade();
        return this;
    }
}
