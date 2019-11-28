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
package org.spongepowered.common.data.builder.item;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;

public class SpongeFireworkEffectDataBuilder extends AbstractDataBuilder<FireworkEffect> implements DataBuilder<FireworkEffect> {

    private final static int SUPPORTED_VERSION = 1;

    public SpongeFireworkEffectDataBuilder() {
        super(FireworkEffect.class, 1);
    }

    @Override
    protected Optional<FireworkEffect> buildContent(DataView container) throws InvalidDataException {
        if (container.contains(Constants.Item.Fireworks.FIREWORK_SHAPE, Constants.Item.Fireworks.FIREWORK_COLORS, Constants.Item.Fireworks.FIREWORK_FADE_COLORS,
                Constants.Item.Fireworks.FIREWORK_FLICKERS, Constants.Item.Fireworks.FIREWORK_TRAILS)) {
            final String fireworkShapeId = DataUtil.getData(container, Constants.Item.Fireworks.FIREWORK_SHAPE, String.class);
            final Optional<FireworkShape> shapeOptional = Sponge.getRegistry().getType(FireworkShape.class, fireworkShapeId);
            if (!shapeOptional.isPresent()) {
                throw new InvalidDataException("Could not find the FireworkShape for the provided id: " + fireworkShapeId);
            }
            final FireworkShape shape = shapeOptional.get();
            final boolean trails = DataUtil.getData(container, Constants.Item.Fireworks.FIREWORK_TRAILS, Boolean.class);
            final boolean flickers = DataUtil.getData(container, Constants.Item.Fireworks.FIREWORK_FLICKERS, Boolean.class);
            final List<Color> colors = container.getSerializableList(Constants.Item.Fireworks.FIREWORK_COLORS, Color.class).get();
            final List<Color> fadeColors = container.getSerializableList(Constants.Item.Fireworks.FIREWORK_FADE_COLORS, Color.class).get();
            return Optional.of(FireworkEffect.builder()
                    .colors(colors)
                    .flicker(flickers)
                    .fades(fadeColors)
                    .trail(trails)
                    .shape(shape)
                    .build());

        }
        return Optional.empty();
    }

}
