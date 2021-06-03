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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.map.color.MapColor;
import org.spongepowered.api.map.color.MapColorType;
import org.spongepowered.api.map.color.MapShade;
import org.spongepowered.api.map.color.MapShades;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.MapUtil;

public final class SpongeMapColorBuilder implements MapColor.Builder {

    private @Nullable MapColorType color = null;
    private MapShade shade = MapShades.BASE.get();

    @Override
    public MapColor.Builder shade(final MapShade shade) {
        this.shade = shade;
        return this;
    }

    @Override
    public MapColor.Builder darker() {
        return this.shade(MapShades.DARKER.get());
    }

    @Override
    public MapColor.Builder darkest() {
        return this.shade(MapShades.DARKEST.get());
    }

    @Override
    public MapColor.Builder base() {
        return this.shade(MapShades.BASE.get());
    }

    @Override
    public MapColor.Builder dark() {
        return this.shade(MapShades.DARK.get());
    }

    @Override
    public MapColor.Builder baseColor(final MapColorType mapColorType) {
        this.color = mapColorType;
        return this;
    }

    @Override
    public MapColor.Builder fromContainer(final DataView container) {
        if (!container.contains(Constants.Map.COLOR_INDEX)) {
            return this;
        }
        this.reset();
        final int colorInt = container.getInt(Constants.Map.COLOR_INDEX).get();
        this.color = MapUtil.getMapColorTypeByColorIndex(colorInt).orElseThrow(() -> new InvalidDataException("Invalid map color " + colorInt));

        container.getInt(Constants.Map.SHADE_NUM).ifPresent(mapShadeInt -> this.shade = MapUtil.getMapShadeById(mapShadeInt));

        return this;
    }

    @Override
    public MapColor build() throws IllegalStateException {
        Preconditions.checkState(this.color != null, "Color has not been set yet");
        return new SpongeMapColor(this.color, this.shade);
    }

    @Override
    public MapColor.Builder from(final MapColor value) {
        final SpongeMapColor mapColorBridge = (SpongeMapColor)value;
        this.color = mapColorBridge.type();
        this.shade = mapColorBridge.shade();
        return this;
    }

    @Override
    public MapColor.Builder reset() {
        this.color = null;
        this.shade = MapShades.BASE.get();
        return this;
    }
}
