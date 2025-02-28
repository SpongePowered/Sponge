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
package org.spongepowered.common.world.generation.feature;

import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import org.spongepowered.api.world.generation.feature.Feature;
import org.spongepowered.api.world.generation.feature.FeatureType;

import java.util.Objects;

public final class SpongeFeatureBuilder implements Feature.Builder {

    private net.minecraft.world.level.levelgen.feature.Feature<?> type;
    private FeatureConfiguration config;

    public SpongeFeatureBuilder() {
        this.reset();
    }

    @Override
    public Feature.Builder type(final FeatureType type) {
        this.type = (net.minecraft.world.level.levelgen.feature.Feature<?>) type;
        return this;
    }

    @Override
    public Feature.Builder reset() {
        this.type = null;
        this.config = null;
        return this;
    }

    @Override
    public Feature.Builder from(final org.spongepowered.api.world.generation.feature.Feature feature) {
        this.type(feature.type());
        this.config = ((ConfiguredFeature) (Object) feature).config();
        return this;
    }

    @Override
    public Feature build() {
        Objects.requireNonNull(this.type, "config");
        Objects.requireNonNull(this.config, "config");
        return (Feature) (Object) new ConfiguredFeature<>((net.minecraft.world.level.levelgen.feature.Feature<? super FeatureConfiguration>) this.type, this.config);
    }
}
