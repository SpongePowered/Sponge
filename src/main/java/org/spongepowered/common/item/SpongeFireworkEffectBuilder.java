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
package org.spongepowered.common.item;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.item.FireworkShapes;
import org.spongepowered.api.util.Color;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class SpongeFireworkEffectBuilder implements FireworkEffect.Builder {

    private boolean trail = false;
    private boolean flicker = false;
    private List<Color> colors = Lists.newArrayList();
    private List<Color> fades = Lists.newArrayList();
    private Supplier<? extends FireworkShape> shape = FireworkShapes.BALL;

    @Override
    public SpongeFireworkEffectBuilder trail(final boolean trail) {
        this.trail = trail;
        return this;
    }

    @Override
    public SpongeFireworkEffectBuilder flicker(final boolean flicker) {
        this.flicker = flicker;
        return this;
    }

    @Override
    public SpongeFireworkEffectBuilder color(final Color color) {
        checkNotNull(color);
        this.colors.add(color);
        return this;
    }

    @Override
    public SpongeFireworkEffectBuilder colors(final Color... colors) {
        checkNotNull(colors);
        Collections.addAll(this.colors, colors);
        return this;
    }

    @Override
    public SpongeFireworkEffectBuilder colors(final Iterable<Color> colors) {
        checkNotNull(colors);
        for (final Color color : colors) {
            this.colors.add(color);
        }
        return this;
    }

    @Override
    public SpongeFireworkEffectBuilder fade(final Color color) {
        checkNotNull(color);
        this.fades.add(color);
        return this;
    }

    @Override
    public SpongeFireworkEffectBuilder fades(final Color... colors) {
        checkNotNull(colors);
        Collections.addAll(this.fades, colors);
        return this;
    }

    @Override
    public SpongeFireworkEffectBuilder fades(final Iterable<Color> colors) {
        checkNotNull(colors);
        for (final Color color : colors) {
            this.fades.add(color);
        }
        return this;
    }

    @Override
    public SpongeFireworkEffectBuilder shape(final FireworkShape shape) {
        checkNotNull(shape);
        this.shape = () -> shape;
        return this;
    }

    @Override
    public FireworkEffect build() {
        return new SpongeFireworkEffect(this.flicker, this.trail, this.colors, this.fades, this.shape.get());
    }

    @Override
    public FireworkEffect.Builder from(final FireworkEffect value) {
        return this.trail(value.hasTrail())
            .colors(value.getColors())
            .fades(value.getFadeColors())
            .shape(value.getShape())
            .flicker(value.flickers());
    }

    @Override
    public SpongeFireworkEffectBuilder reset() {
        this.trail = false;
        this.flicker = false;
        this.colors = Lists.newArrayList();
        this.fades = Lists.newArrayList();
        this.shape = FireworkShapes.BALL;
        return this;
    }
}
