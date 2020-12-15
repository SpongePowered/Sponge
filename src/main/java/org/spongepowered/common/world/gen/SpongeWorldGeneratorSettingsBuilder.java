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
package org.spongepowered.common.world.gen;

import com.mojang.serialization.Lifecycle;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.api.world.gen.WorldGeneratorSettings;
import org.spongepowered.common.accessor.world.gen.DimensionGeneratorSettingsAccessor;

import java.util.Optional;

public final class SpongeWorldGeneratorSettingsBuilder implements WorldGeneratorSettings.Builder {

    private long seed;
    private boolean generateFeatures, generateBonusChest;

    @Override
    public WorldGeneratorSettings.Builder reset() {
        this.seed = 0L;
        this.generateFeatures = true;
        this.generateBonusChest = false;
        return this;
    }

    @Override
    public WorldGeneratorSettings.Builder seed(final long seed) {
        this.seed = seed;
        return this;
    }

    @Override
    public WorldGeneratorSettings.Builder generateFeatures(final boolean generateFeatures) {
        this.generateFeatures = generateFeatures;
        return this;
    }

    @Override
    public WorldGeneratorSettings.Builder generateBonusChest(final boolean generateBonusChest) {
        this.generateBonusChest = generateBonusChest;
        return this;
    }

    @Override
    public WorldGeneratorSettings build() {
        return (WorldGeneratorSettings) DimensionGeneratorSettingsAccessor.invoker$construct(this.seed, this.generateFeatures,
            this.generateBonusChest, new SimpleRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental()), Optional.empty());
    }
}
