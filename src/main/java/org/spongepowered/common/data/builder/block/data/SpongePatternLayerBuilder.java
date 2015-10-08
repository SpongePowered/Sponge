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
package org.spongepowered.common.data.builder.block.data;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import org.spongepowered.api.Game;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.type.BannerPatternShape;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.service.persistence.DataBuilder;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.meta.SpongePatternLayer;

import java.util.Optional;

/**
 * The de-facto builder for a {@link PatternLayer}.
 */
public class SpongePatternLayerBuilder implements DataBuilder<PatternLayer> {

    private static final DataQuery ID = of("BannerShapeId");
    private static final DataQuery COLOR = of("DyeColor");
    private final Game game;

    public SpongePatternLayerBuilder(Game game) {
        this.game = game;
    }

    @Override
    public Optional<PatternLayer> build(final DataView container) throws InvalidDataException {
        checkNotNull(container);
        if (!container.contains(ID) || !container.contains(COLOR)) {
            throw new InvalidDataException("The provided container does not contain the data to make a PatternLayer!");
        }
        String id = container.getString(ID).get();

        // We can get these pattern shapes from the game registry willy nilly, however, we still need to validate
        // that the pattern exists, if it doesn't, well, thow an InvalidDataException!
        Optional<BannerPatternShape> shapeOptional = this.game.getRegistry().getType(BannerPatternShape.class, id);
        if (!shapeOptional.isPresent()) {
            throw new InvalidDataException("The provided container has an invalid banner pattern shape entry!");
        }

        // Now we need to validate the dye color of course...
        String dyeColorId = container.getString(COLOR).get();
        Optional<DyeColor> colorOptional = this.game.getRegistry().getType(DyeColor.class, dyeColorId);
        if (!colorOptional.isPresent()) {
            throw new InvalidDataException("The provided container has an invalid dye color entry!");
        }
        return Optional.<PatternLayer>of(new SpongePatternLayer(shapeOptional.get(), colorOptional.get()));
    }
}
