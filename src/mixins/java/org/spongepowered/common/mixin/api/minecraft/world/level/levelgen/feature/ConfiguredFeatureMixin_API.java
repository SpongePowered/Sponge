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
package org.spongepowered.common.mixin.api.minecraft.world.level.levelgen.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import org.spongepowered.api.world.generation.feature.FeatureConfig;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.MissingImplementationException;
import org.spongepowered.math.vector.Vector3i;

import java.util.Random;

@Mixin(ConfiguredFeature.class)
public abstract class ConfiguredFeatureMixin_API<
        F extends Feature<FC>,
        FC extends FeatureConfiguration,
        APIF extends org.spongepowered.api.world.generation.feature.Feature<APIFC>,
        APIFC extends FeatureConfig>
        implements org.spongepowered.api.world.generation.feature.ConfiguredFeature<APIF, APIFC> {

    // @formatter:off
    @Shadow @Final private F feature;
    @Shadow @Final private FC config;
    @Shadow public abstract boolean shadow$place(WorldGenLevel $$0, ChunkGenerator $$1, Random $$2, BlockPos $$3);
    // @formatter:on

    @Override
    public APIF feature() {
        return (APIF) this.feature;
    }

    @Override
    public APIFC config() {
        return (APIFC) this.config;
    }

    @Override
    public boolean place(ServerWorld world, Vector3i origin) {
        // TODO
        throw new MissingImplementationException("ConfiguredFeature", "place");
    }
}
