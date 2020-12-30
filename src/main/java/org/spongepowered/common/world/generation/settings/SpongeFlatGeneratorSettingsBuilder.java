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
package org.spongepowered.common.world.generation.settings;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.generation.settings.FlatGeneratorSettings;
import org.spongepowered.api.world.generation.settings.structure.StructureGenerationSettings;

public final class SpongeFlatGeneratorSettingsBuilder implements FlatGeneratorSettings.Builder {

    @Override
    public FlatGeneratorSettings.Builder structureSettings(final StructureGenerationSettings settings) {
        return null;
    }

    @Override
    public FlatGeneratorSettings.Builder addLayer(final int height, final BlockState block) {
        return null;
    }

    @Override
    public FlatGeneratorSettings.Builder removeLayer(final int height) {
        return null;
    }

    @Override
    public FlatGeneratorSettings.Builder performDecoration(final boolean performDecoration) {
        return null;
    }

    @Override
    public FlatGeneratorSettings.Builder populateLakes(final boolean populateLakes) {
        return null;
    }

    @Override
    public FlatGeneratorSettings.Builder from(final FlatGeneratorSettings value) {
        return null;
    }

    @Override
    public @NonNull FlatGeneratorSettings build() {
        return null;
    }
}
