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
package org.spongepowered.common.world.generation;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.spongepowered.api.world.generation.config.WorldGenerationConfig;
import org.spongepowered.common.accessor.world.gen.DimensionGeneratorSettingsAccessor;
import org.spongepowered.common.server.BootstrapProperties;

import java.util.Objects;
import java.util.Optional;

public final class SpongeWorldGenerationConfigMutableBuilder implements WorldGenerationConfig.Mutable.Builder {

    private long seed;
    private boolean generateFeatures, generateBonusChest;

    @Override
    public WorldGenerationConfig.Mutable.Builder seed(final long seed) {
        this.seed = seed;
        return this;
    }

    @Override
    public WorldGenerationConfig.Mutable.Builder generateFeatures(final boolean generateFeatures) {
        this.generateFeatures = generateFeatures;
        return this;
    }

    @Override
    public WorldGenerationConfig.Mutable.Builder generateBonusChest(final boolean generateBonusChest) {
        this.generateBonusChest = generateBonusChest;
        return this;
    }

    @Override
    public WorldGenerationConfig.Mutable.Builder reset() {
        final WorldGenSettings defaultSettings = BootstrapProperties.worldGenSettings;
        this.seed = defaultSettings.seed();
        this.generateFeatures = defaultSettings.generateFeatures();
        this.generateBonusChest = defaultSettings.generateBonusChest();
        return this;
    }

    @Override
    public WorldGenerationConfig.Mutable.Builder from(final WorldGenerationConfig value) {
        this.seed = Objects.requireNonNull(value, "value").seed();
        this.generateFeatures = value.generateFeatures();
        this.generateBonusChest = value.generateBonusChest();
        return this;
    }

    @Override
    public WorldGenerationConfig.Mutable build() {
        return (WorldGenerationConfig.Mutable) DimensionGeneratorSettingsAccessor.invoker$new(this.seed, this.generateFeatures,
            this.generateBonusChest, new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable()), Optional.empty());
    }
}
