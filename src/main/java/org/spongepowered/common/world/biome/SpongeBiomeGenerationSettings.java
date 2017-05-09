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

import com.google.common.collect.Lists;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.GroundCoverLayer;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.common.util.NonNullArrayList;

import java.util.List;
import java.util.stream.Collectors;

public class SpongeBiomeGenerationSettings implements BiomeGenerationSettings {

    private float minHeight;
    private float maxHeight;

    private final List<Populator> populators = new NonNullArrayList<>();
    private final List<GenerationPopulator> generationPopulators = Lists.newArrayList();
    private final List<GroundCoverLayer> groundCoverLayers = Lists.newArrayList();

    public SpongeBiomeGenerationSettings() {

    }

    @Override
    public float getMinHeight() {
        return this.minHeight;
    }

    @Override
    public void setMinHeight(float height) {
        this.minHeight = height;
    }

    @Override
    public float getMaxHeight() {
        return this.maxHeight;
    }

    @Override
    public void setMaxHeight(float height) {
        this.maxHeight = height;
    }

    @Override
    public List<GroundCoverLayer> getGroundCoverLayers() {
        return this.groundCoverLayers;
    }

    @Override
    public List<GenerationPopulator> getGenerationPopulators() {
        return this.generationPopulators;
    }

    @Override
    public List<GenerationPopulator> getGenerationPopulators(Class<? extends GenerationPopulator> type) {
        return this.generationPopulators.stream().filter((p) -> type.isAssignableFrom(p.getClass())).collect(Collectors.toList());
    }

    @Override
    public List<Populator> getPopulators() {
        return this.populators;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Populator> List<T> getPopulators(Class<T> type) {
        return (List<T>) this.populators.stream().filter((p) -> type.isAssignableFrom(p.getClass())).collect(Collectors.toList());
    }

    @Override
    public BiomeGenerationSettings copy() {
        SpongeBiomeGenerationSettings settings = new SpongeBiomeGenerationSettings();
        settings.minHeight = this.minHeight;
        settings.maxHeight = this.maxHeight;
        settings.groundCoverLayers.addAll(this.groundCoverLayers);
        settings.populators.addAll(this.populators);
        settings.generationPopulators.addAll(this.generationPopulators);
        return settings;
    }

}
