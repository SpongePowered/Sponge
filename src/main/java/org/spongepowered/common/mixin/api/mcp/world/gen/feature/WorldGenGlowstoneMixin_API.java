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
package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.GlowStoneFeature;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.Glowstone;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Random;

@Mixin(GlowStoneFeature.class)
public abstract class WorldGenGlowstoneMixin_API extends Feature implements Glowstone {

    private VariableAmount api$clusterheight = VariableAmount.baseWithRandomAddition(0, 12);
    private VariableAmount api$height = VariableAmount.baseWithRandomAddition(4, 120);
    private VariableAmount api$count = VariableAmount.baseWithRandomAddition(1, 10);
    private VariableAmount api$attempts = VariableAmount.fixed(1500);

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.GLOWSTONE;
    }

    @Override
    public void populate(final org.spongepowered.api.world.World worldIn, final Extent extent, final Random random) {
        final Vector3i min = extent.getBlockMin();
        final Vector3i size = extent.getBlockSize();
        final World world = (World) worldIn;
        final BlockPos position = new BlockPos(min.getX(), min.getY(), min.getZ());
        final int n = this.api$count.getFlooredAmount(random);
        int x;
        int y;
        int z;

        for (int i = 0; i < n; i++) {
            x = random.nextInt(size.getX());
            z = random.nextInt(size.getZ());
            y = this.api$height.getFlooredAmount(random);
            func_180709_b(world, random, position.func_177982_a(x, y, z));
        }
    }

    @Override
    public VariableAmount getClustersPerChunk() {
        return this.api$count;
    }

    @Override
    public void setClustersPerChunk(final VariableAmount count) {
        this.api$count = count;
    }

    @Override
    public VariableAmount getAttemptsPerCluster() {
        return this.api$attempts;
    }

    @Override
    public void setAttemptsPerCluster(final VariableAmount attempts) {
        this.api$attempts = attempts;
    }

    @Override
    public VariableAmount getClusterHeight() {
        return this.api$clusterheight;
    }

    @Override
    public void setClusterHeight(final VariableAmount height) {
        this.api$clusterheight = height;
    }

    @Override
    public VariableAmount getHeight() {
        return this.api$height;
    }

    @Override
    public void setHeight(final VariableAmount height) {
        this.api$height = checkNotNull(height);
    }


}
