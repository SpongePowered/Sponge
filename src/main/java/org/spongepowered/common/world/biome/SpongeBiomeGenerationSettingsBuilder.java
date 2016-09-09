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
package org.spongepowered.common.world.biome;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeGenerationSettings.Builder;
import org.spongepowered.api.world.biome.GroundCoverLayer;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.Populator;

import java.util.List;

public class SpongeBiomeGenerationSettingsBuilder implements BiomeGenerationSettings.Builder {

    private float min = 0;
    private float max = 0;
    private final List<GroundCoverLayer> groundCover = Lists.newArrayList();
    private final List<Populator> populators = Lists.newArrayList();
    private final List<GenerationPopulator> genpop = Lists.newArrayList();

    @Override
    public Builder from(BiomeGenerationSettings value) {
        this.min = value.getMinHeight();
        this.max = value.getMaxHeight();
        this.groundCover.clear();
        this.groundCover.addAll(value.getGroundCoverLayers());
        this.genpop.clear();
        this.genpop.addAll(value.getGenerationPopulators());
        this.populators.clear();
        this.populators.addAll(value.getPopulators());
        return this;
    }

    @Override
    public Builder reset() {
        this.min = 0;
        this.max = 0;
        this.groundCover.clear();
        this.populators.clear();
        this.genpop.clear();
        return this;
    }

    @Override
    public Builder minHeight(float height) {
        this.min = height;
        return this;
    }

    @Override
    public Builder maxHeight(float height) {
        this.max = height;
        return this;
    }

    @Override
    public Builder groundCover(GroundCoverLayer... coverLayers) {
        checkNotNull(coverLayers, "coverLayers");
        this.groundCover.clear();
        for (GroundCoverLayer layer : coverLayers) {
            this.groundCover.add(checkNotNull(layer, "layer"));
        }
        return this;
    }

    @Override
    public Builder groundCover(Iterable<GroundCoverLayer> coverLayers) {
        checkNotNull(coverLayers, "coverLayers");
        this.groundCover.clear();
        for (GroundCoverLayer layer : coverLayers) {
            this.groundCover.add(checkNotNull(layer, "layer"));
        }
        return this;
    }

    @Override
    public Builder generationPopulators(GenerationPopulator... genpop) {
        checkNotNull(genpop, "genpop");
        this.genpop.clear();
        for (GenerationPopulator pop : genpop) {
            this.genpop.add(checkNotNull(pop, "pop"));
        }
        return this;
    }

    @Override
    public Builder generationPopulators(Iterable<GenerationPopulator> genpop) {
        checkNotNull(genpop, "genpop");
        this.genpop.clear();
        for (GenerationPopulator pop : genpop) {
            this.genpop.add(checkNotNull(pop, "pop"));
        }
        return this;
    }

    @Override
    public Builder populators(Populator... populators) {
        checkNotNull(populators, "populators");
        this.populators.clear();
        for (Populator pop : populators) {
            this.populators.add(checkNotNull(pop, "pop"));
        }
        return this;
    }

    @Override
    public Builder populators(Iterable<Populator> populators) {
        checkNotNull(populators, "populators");
        this.populators.clear();
        for (Populator pop : populators) {
            this.populators.add(checkNotNull(pop, "pop"));
        }
        return this;
    }

    @Override
    public BiomeGenerationSettings build() throws IllegalStateException {
        SpongeBiomeGenerationSettings settings = new SpongeBiomeGenerationSettings();
        settings.setMinHeight(this.min);
        settings.setMaxHeight(this.max);
        settings.getGroundCoverLayers().addAll(this.groundCover);
        settings.getPopulators().addAll(this.populators);
        settings.getGenerationPopulators().addAll(this.genpop);
        return settings;
    }

}
