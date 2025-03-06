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

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.spongepowered.api.world.generation.feature.Feature;
import org.spongepowered.api.world.generation.feature.PlacedFeature;

import java.util.ArrayList;
import java.util.List;

public final class SpongePlacedFeatureBuilder implements PlacedFeature.Builder {

    private Holder<ConfiguredFeature<?, ?>> feature;
    private List<PlacementModifier> modifiers = new ArrayList<>();

    public SpongePlacedFeatureBuilder() {
        this.reset();
    }

    @Override
    public PlacedFeature.Builder reset() {
        this.feature = null;
        this.modifiers.clear();
        return this;
    }

    @Override
    public PlacedFeature.Builder from(final PlacedFeature feature) {
        this.feature(feature.feature());
        this.modifiers.clear();
        this.modifiers.addAll((List) feature.placementModifiers());
        return this;
    }

    @Override
    public PlacedFeature.Builder feature(final Feature feature) {
        this.feature = Holder.direct((ConfiguredFeature<?, ?>) (Object) feature);
        return this;
    }

    @Override
    public PlacedFeature.Builder addModifier(final org.spongepowered.api.world.generation.feature.PlacementModifier modifier) {
        this.modifiers.add((net.minecraft.world.level.levelgen.placement.PlacementModifier) modifier);
        return this;
    }

    @Override
    public PlacedFeature build() {
        return (PlacedFeature) (Object) new net.minecraft.world.level.levelgen.placement.PlacedFeature(this.feature, this.modifiers);
    }
}
