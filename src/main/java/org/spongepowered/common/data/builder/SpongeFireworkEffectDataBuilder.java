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
package org.spongepowered.common.data.builder;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.FireworkEffectBuilder;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.service.persistence.DataBuilder;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.item.SpongeFireworkEffect;

import java.awt.Color;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpongeFireworkEffectDataBuilder implements DataBuilder<FireworkEffect> {

    @Override
    public Optional<FireworkEffect> build(DataView container) throws InvalidDataException {
        checkNotNull(container);
        if (!container.contains(SpongeFireworkEffect.TYPE)
                || !container.contains(SpongeFireworkEffect.COLORS)
                || !container.contains(SpongeFireworkEffect.FADES)
                || !container.contains(SpongeFireworkEffect.TRAILS)
                || !container.contains(SpongeFireworkEffect.FLICKERS)) {
            throw new InvalidDataException("The container does not have data pertaining to FireworkEffect!");
        }
        String type = container.getString(SpongeFireworkEffect.TYPE).get();
        Optional<FireworkShape> oShape = Sponge.getGame().getRegistry().getType(FireworkShape.class, type);
        if(!oShape.isPresent()) throw new InvalidDataException("The container has an invalid type; " + type);

        List<Integer> intColors = container.getIntegerList(SpongeFireworkEffect.COLORS).get();
        List<Color> colors = Lists.newArrayList();
        colors.addAll(intColors.stream().map(Color::new).collect(Collectors.toList()));

        List<Integer> intFades = container.getIntegerList(SpongeFireworkEffect.FADES).get();
        List<Color> fades = Lists.newArrayList();
        fades.addAll(intFades.stream().map(Color::new).collect(Collectors.toList()));

        boolean trails = container.getBoolean(SpongeFireworkEffect.TRAILS).get();
        boolean flickers = container.getBoolean(SpongeFireworkEffect.FLICKERS).get();
        FireworkEffectBuilder builder = Sponge.getGame().getRegistry().createBuilder(FireworkEffectBuilder.class);
        return Optional.of(builder.colors(colors)
                .fades(fades)
                .flicker(flickers)
                .trail(trails)
                .shape(oShape.get())
                .build());
    }
}
