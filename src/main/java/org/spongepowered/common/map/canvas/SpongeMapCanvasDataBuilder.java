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
package org.spongepowered.common.map.canvas;

import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.map.MapCanvas;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.MapUtil;

import java.util.Objects;
import java.util.Optional;

public final class SpongeMapCanvasDataBuilder extends AbstractDataBuilder<MapCanvas> {

    public SpongeMapCanvasDataBuilder() {
        super(MapCanvas.class, 1);
    }

    @Override
    protected Optional<MapCanvas> buildContent(final DataView container) throws InvalidDataException {
        Objects.requireNonNull(container, "container cannot be null");
        if (!container.contains(Constants.Map.MAP_CANVAS)) {
            return Optional.empty();
        }
        final byte[] canvas = MapUtil.getMapCanvasFromContainer(container);
        return Optional.of(new SpongeMapByteCanvas(canvas));
    }

}
