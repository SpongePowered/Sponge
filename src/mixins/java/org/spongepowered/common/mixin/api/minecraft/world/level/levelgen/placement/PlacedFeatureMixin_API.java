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
package org.spongepowered.common.mixin.api.minecraft.world.level.levelgen.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.api.world.generation.feature.Feature;
import org.spongepowered.api.world.generation.feature.FeatureType;
import org.spongepowered.api.world.generation.feature.PlacementModifier;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

import java.util.List;

@Mixin(PlacedFeature.class)
public abstract class PlacedFeatureMixin_API implements org.spongepowered.api.world.generation.feature.PlacedFeature {

    // @formatter:off
    @Shadow @Final private Holder<net.minecraft.world.level.levelgen.feature.ConfiguredFeature<?, ?>> feature;
    @Shadow @Final private List<net.minecraft.world.level.levelgen.placement.PlacementModifier> placement;
    @Shadow public abstract boolean shadow$place(final WorldGenLevel $$0, final ChunkGenerator $$1, final RandomSource $$2, final BlockPos $$3);
    // @formatter:on

    @Override
    public <F extends FeatureType> Feature feature() {
        return (Feature) (Object) this.feature.value();
    }

    @Override
    public List<PlacementModifier> placementModifiers() {
        return (List) this.placement;
    }

    @Override
    public boolean place(final ServerWorld world, final Vector3i pos) {
        return this.shadow$place(((WorldGenLevel) world), (ChunkGenerator) world.generator(), ((WorldGenLevel) world).getRandom(), VecHelper.toBlockPos(pos));
    }

    @Override
    public boolean place(final ServerLocation location) {
        return this.place(location.world(), location.blockPosition());
    }
}
