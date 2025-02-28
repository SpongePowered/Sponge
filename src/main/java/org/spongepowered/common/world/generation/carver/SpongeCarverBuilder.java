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
package org.spongepowered.common.world.generation.carver;

import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.generation.carver.Carver;
import org.spongepowered.api.world.generation.carver.CarverType;

public final class SpongeCarverBuilder implements Carver.Builder {

    @Nullable private WorldCarver<?> type;
    @Nullable private CarverConfiguration config;

    public SpongeCarverBuilder() {
        this.reset();
    }

    @Override
    public Carver.Builder reset() {
        this.type = null;
        this.config = null;
        return this;
    }

    @Override
    public Carver.Builder type(final CarverType type) {
        this.type = (WorldCarver<?>) type;
        return this;
    }

    @Override
    public Carver.Builder from(final Carver carver) {
        this.type(carver.type());
        this.config = ((ConfiguredWorldCarver<?>) (Object) carver).config();
        return this;
    }

    @Override
    public Carver build() {
        return (Carver) (Object) new ConfiguredWorldCarver<>((WorldCarver<? super CarverConfiguration>) this.type, this.config);
    }
}
